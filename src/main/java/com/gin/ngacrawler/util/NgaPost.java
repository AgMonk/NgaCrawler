package com.gin.ngacrawler.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * nga请求工具类
 *
 * @author bx002
 */
@Slf4j
public class NgaPost {
    private static final Pattern PATTERN_NUMBERS = Pattern.compile("^\\d+$");

    private final static String URL_READ = "https://bbs.nga.cn/read.php?&__output=11";
    private final static String URL_search_post="https://bbs.nga.cn/thread.php?searchpost=1&__output=11";
    /**
     * 扫描帖子
     * @param tid
     * @param page
     * @param cookie
     * @param authorId
     * @return
     */
    public static JSONObject scanThread(Integer tid, String page, String cookie, Integer authorId) {
        JSONObject json = new JSONObject();
        if (!"e".equals(page) && !PATTERN_NUMBERS.matcher(page).find()) {
            page="1";
        }
        String url = URL_READ + "&tid=" + tid + "&page=" + page;
        if (authorId!=null) {
            url+="&authorid="+authorId;
        }
        String result = Request.create(url)
                .setCookie(cookie)
                .get().getResult();
        JSONObject response = JSONObject.parseObject(result);
        JSONObject data = response.getJSONObject("data");
        json.put("replies",data.getJSONArray("__R"));
        json.put("page",data.getInteger("__PAGE"));
        return json;
    }

    /**
     * 搜索用户回复返回TID
     * @return
     */
    public static List<Integer> findPostTid(Integer authorId, Integer page, String cookie, Integer fid){
        Set<Integer> tidSet = new HashSet<>();
        page = page==null||page<1?1:page;

        log.info("请求用户{} 在版面 {} 回复过的tid 第{}页",authorId,fid,page);
        String url = URL_search_post+"&authorid="+authorId+"&fid="+fid+"&page="+page;
        String result = Request.create(url)
                .setCookie(cookie)
                .get().getResult();
        JSONObject response = JSONObject.parseObject(result);
        JSONArray errors = response.getJSONArray("error");

        if (errors!=null) {
            //发生错误
            errors.forEach(o -> {
                if (o!=null) {
                    log.error("{}",o);
                }
            });
            return null;
        }
        JSONObject data = response.getJSONObject("data");

        JSONArray tArray =null;
        JSONObject tObj=null;
        try {
            tArray = data.getJSONArray("__T");
        } catch (ClassCastException ignored) {

        }
        try {
            tObj = data.getJSONObject("__T");
        } catch (ClassCastException ignored) {
        }
        if (tArray!=null) {
            tArray.forEach(o -> {

                JSONObject o1 = (JSONObject) o;
                if (o1.getString("error")==null) {
                    tidSet.add(o1.getInteger("tid"));
                }
            });
        }
        if (tObj!=null) {
            tObj.forEach((s, o) -> {
                JSONObject o1 = (JSONObject) o;
                if (o1.getString("error")==null) {
                    tidSet.add(((JSONObject) o).getInteger("tid"));
                }
            });
        }
        return new ArrayList<>(tidSet);
    }

    public static Set<Integer> findAllPostTid(Integer authorId, String cookie, Integer fid){

        Integer endPage = findEndPage(1, 128, authorId, cookie, fid);
        List<Callable<List<Integer>>> tasks = new ArrayList<>();
        for (int i = 1; i < endPage+1; i++) {
            int finalI = i;
            tasks.add(()-> findPostTid(authorId, finalI,cookie,fid));
        }

        List<List<Integer>> allPost = TasksUtil.executeTasks(tasks, 60, null, "allPost", 10);
        return allPost.stream().flatMap(Collection::stream).collect(Collectors.toSet());


    }

    //查找回复最大页数
    private static Integer findEndPage(Integer page, Integer step, Integer authorId, String cookie, Integer fid){
        if (findPostTid(authorId, page+step, cookie, fid) != null) {
            return findEndPage(page+step,step,authorId,cookie,fid);
        }
        if (step==1) {
            return page;
        }
        return findEndPage(page,step/2,authorId,cookie,fid);
    }
}
