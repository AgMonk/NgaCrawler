package com.gin.ngacrawler.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gin.ngacrawler.dao.ReplyDAO;
import com.gin.ngacrawler.entity.Reply;
import com.gin.ngacrawler.util.NgaPost;
import com.gin.ngacrawler.util.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author bx002
 */
@Service
@Transactional
@Slf4j
public class ScanServiceImpl extends ServiceImpl<ReplyDAO, Reply> implements ScanService {
    private final Integer AUTO_SCAN_TID;
    private final ThreadPoolTaskExecutor readExecutor;
    private final String cookie;

    public ScanServiceImpl(ThreadPoolTaskExecutor readExecutor, ConfigService configService) {
        this.readExecutor = readExecutor;
        cookie = configService.getCookie("nga").getValue();
        AUTO_SCAN_TID = Integer.valueOf(configService.getConfig("autoScanTid", null).getValue());
    }

    @Override
    public Integer scanPage(Integer tid, String page, Integer authorId) {
        JSONObject pageJson = NgaPost.scanThread(tid, page, cookie, authorId);
        Integer currentPage = pageJson.getInteger("page");
        JSONArray replies = pageJson.getJSONArray("replies");
        List<Reply> replyList = replies.stream()
                .map(r -> new Reply((JSONObject) r))
                .flatMap(Reply::stream)
                .collect(Collectors.toList());
        List<String> idList = replyList.stream().map(Reply::getId).collect(Collectors.toList());
        //查询已存在的回复
        List<Reply> existsReplies = listByIds(idList);
        //移除 已存在回复 评论回复
        List<Reply> newReplyList = replyList.stream()
                .filter(r -> r.getContent() != null)
                .filter(r -> !existsReplies.contains(r))
                .collect(Collectors.toList());
        //保存新回复
        int size = newReplyList.size();
        if (size != 0) {
            log.info("请求 tid={} 获得{}条回复 第 {} 页 保存 {} 条回复", tid, replyList.size(), page, size);
            saveBatch(newReplyList);
        } else {
            log.warn("请求 tid={} 获得{}条回复 第 {} 页 保存 {} 条回复", tid, replyList.size(), page, size);
        }
        return currentPage;
    }

    @Override
    public void scanAllPages(Integer tid, Integer startFromPage, Integer endPage, Integer authorId) {
        endPage = endPage == null ? scanPage(tid, "e", authorId) : endPage;
        if (endPage == 1) {
            return;
        }
        startFromPage = startFromPage == null || startFromPage < 0 ? Math.max(1, endPage - 3) : startFromPage;
        log.info("tid={} 从{}页到{}页 请求开始 ", tid, startFromPage, endPage - 1);
        long start = System.currentTimeMillis();
        for (int i = startFromPage; i < endPage; i++) {
            scanPage(tid, String.valueOf(i), authorId);
        }
        long end = System.currentTimeMillis();
        log.info("tid={} 从{}页到{}页  请求完毕 耗时 {}", tid, startFromPage, endPage - 1, Request.timeCost(start, end));
    }

    @Override
    public List<Reply> searchReplies(Long authorId, String[] keyword, Long startSecond, Long endSecond) {
        QueryWrapper<Reply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("1", 1);
        if (authorId != null) {
            queryWrapper.eq("author_id", authorId);
        }
        if (startSecond != null) {
            queryWrapper.ge("post_date_timestamp", startSecond);
        }
        if (endSecond != null) {
            queryWrapper.le("post_date_timestamp", endSecond);
        }
        StringBuilder match = new StringBuilder();
        for (String s : keyword) {
            match.append("and  match(content) against('").append(s).append("' in boolean mode)");
        }
        queryWrapper.last(match.toString());

        Stream<Reply> stream = list(queryWrapper).stream();
        for (String s : keyword) {
            stream = stream.filter(r -> r.getContent().contains(s));
        }
        return stream.collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0/20 * * * ?")
    public void autoScanNew() {
        scanAllPages(AUTO_SCAN_TID, null, null, null);

        ArrayList<Integer> excludeLists = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            searchPost(39841854, i + 1, 0, excludeLists);
        }
    }

    /**
     * 系统重启后接续之前自动扫描
     * @param
     * @return void
     * @author bx002
     * @date 2020/11/27 15:36
     */
    @Override
    public void autoScanContinue() {
        QueryWrapper<Reply> qw = new QueryWrapper<>();
        qw.select("MAX(lou) lou").eq("tid", AUTO_SCAN_TID);
        Integer pageStart = getOne(qw).getLou() / 20 - 3;
        log.info("接续之前的扫描 从 {} 页开始", pageStart);
        scanAllPages(AUTO_SCAN_TID, pageStart, null, null);
    }


    public List<Integer> searchPost(Integer authorId, Integer page, Integer fid, List<Integer> excludeLists) {
        List<Integer> tidList = NgaPost.findPostTid(authorId, page, cookie, fid);
        if (tidList == null) {
            return null;
        }
        tidList = tidList.stream().filter(t -> !excludeLists.contains(t)).collect(Collectors.toList());
        excludeLists.addAll(tidList);
        log.info("用户{} 第 {} 页回复 涉及 {} 个帖子", authorId, page, tidList.size());
        tidList.removeIf(i -> i == 15666793);
        for (Integer tid : tidList) {
            scanAllPages(tid, 1, null, authorId);
        }
        return tidList;
    }


    public void searchAllPost(Integer authorId, Integer fid) {
        ArrayList<Integer> list = new ArrayList<>(NgaPost.findAllPostTid(authorId, cookie, fid));

    }

}
