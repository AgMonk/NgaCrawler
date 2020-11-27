package com.gin.ngacrawler.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gin.ngacrawler.entity.Config;
import org.apache.ibatis.annotations.CacheNamespace;
import org.springframework.stereotype.Repository;

@Repository
@CacheNamespace
public interface ConfigDAO extends BaseMapper<Config> {
}
