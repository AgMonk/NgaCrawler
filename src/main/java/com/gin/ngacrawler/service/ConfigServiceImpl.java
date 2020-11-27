package com.gin.ngacrawler.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gin.ngacrawler.dao.ConfigDAO;
import com.gin.ngacrawler.entity.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
/**
 * @author bx002
 */
@Slf4j
@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigDAO,Config> implements ConfigService {
    @Override
    public Config getCookie(String name) {
        return getConfig(name,"cookie");
    }

    @Override
    public Config getConfig(String name, String type) {
        QueryWrapper<Config> qw = new QueryWrapper<>();
        qw.eq("name",name);
        if (!StringUtils.isEmpty(type)) {
            qw.eq("type",type);
        }
        return getOne(qw);
    }


}
