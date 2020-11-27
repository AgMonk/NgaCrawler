package com.gin.ngacrawler.service;

import com.gin.ngacrawler.entity.Reply;

import java.util.List;

public interface ScanService {

    Integer scanPage(Integer tid, String page, Integer authorId);

    void scanAllPages(Integer tid, Integer startFromPage, Integer endPage, Integer authorId);


    List<Reply> searchReplies(Long authorId, String[] keyword, Long startSecond, Long endSecond);

    void autoScanContinue();
}
