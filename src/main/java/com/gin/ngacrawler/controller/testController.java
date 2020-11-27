package com.gin.ngacrawler.controller;

import com.gin.ngacrawler.entity.Reply;
import com.gin.ngacrawler.service.ScanService;
import com.gin.ngacrawler.service.ScanServiceImpl;
import com.gin.ngacrawler.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.*;
import java.util.List;

@RestController
@RequestMapping("test")
@Validated
@Slf4j
public class testController {
    @Autowired
    private ScanService scanService;

    @RequestMapping("t")
    public Object test() {
        ScanServiceImpl bean = SpringContextUtil.getBean(ScanServiceImpl.class);
        bean.searchAllPost(25020670,-547859);
//        bean.searchAllPost(39841854,-547859);

        return null;
    }

    @RequestMapping("scan")
    public void scan(Integer start, Integer end) {
        scanService.scanAllPages(15666793, start, end, null);
    }

    @RequestMapping("search")
    public List<Reply> search(Long authorId, String keyword, String dateStart, String dateEnd) {
        String[] keywords = new String[1];
        if (keyword.contains(",")) {
            keywords = keyword.split(",");
        } else if (keyword.contains(" ")) {
            keywords = keyword.split(" ");
        } else {
            keywords[0] = keyword;
        }

        Long s = dateStart != null ? LocalDate.parse(dateStart).toEpochSecond(LocalTime.MIN, ZoneOffset.of("+8")) : null;
        Long e = dateEnd != null ? LocalDate.parse(dateEnd).toEpochSecond(LocalTime.MAX, ZoneOffset.of("+8")) : null;

        return scanService.searchReplies(authorId, keywords, s, e);
    }
}
