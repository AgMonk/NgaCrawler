package com.gin.ngacrawler.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gin.ngacrawler.entity.Config;
import com.gin.ngacrawler.entity.Reply;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyDAO extends BaseMapper<Reply> {
}
