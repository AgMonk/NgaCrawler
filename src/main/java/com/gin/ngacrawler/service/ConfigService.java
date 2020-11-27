package com.gin.ngacrawler.service;


import com.gin.ngacrawler.entity.Config;

import java.util.List;

/**
 * 配置
 */
public interface ConfigService {
    Config getCookie(String name);

    Config getConfig(String name, String type);



}
