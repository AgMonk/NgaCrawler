package com.gin.ngacrawler.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.stream.Stream;

/**
 * @author bx002
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)
@TableName(value = "t_replies")
public class Reply {
    String id;
    String alterInfo;
    Long authorId;
    String content;
    Long fid;
    Integer lou;
    Long pid;
    Long postDateTimestamp;
    Long tid;
    @TableField(exist = false)
    JSONArray comment;

    public Reply(JSONObject json) {
        if (json==null || json.getString("content")==null) {
            return;
        }
        content = json.getString("content");
        alterInfo = json.getString("alterinfo");
        authorId = json.getLong("authorid");
        fid = json.getLong("fid");
        pid = json.getLong("pid");
        postDateTimestamp = json.getLong("postdatetimestamp");
        postDateTimestamp = postDateTimestamp!=null?postDateTimestamp: json.getLong("postdate");
        tid = json.getLong("tid");
        lou = json.getInteger("lou");

        alterInfo = alterInfo != null ? alterInfo : "";
        if (pid==0) {
            id = (tid+alterInfo).trim();
        }else{
            id = (pid+alterInfo).trim();
        }

        comment = json.getJSONArray("comment");

        if (content.length()>15000) {
            content = content.substring(0,15000);
        }
    }

    /**
     * 流化评论
     * @return
     */
    public Stream<Reply> stream(){
        Stream<Reply> thisStream = Stream.of(this);
        if (comment!=null) {
            Stream<Reply> commentStream = comment.stream().map(re -> new Reply((JSONObject) re));
            return Stream.concat(commentStream,thisStream);
        }else{
            return thisStream;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Reply reply = (Reply) o;

        return id.equals(reply.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
