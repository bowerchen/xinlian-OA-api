package com.xinlian.emos.wx.db.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@Document("message")
public class MessageEntity implements Serializable {

    @Id
    private String _id;

    @Indexed(unique = true)
    private String uuid;

    @Indexed
    private Long senderId;

    private String senderPhoto = "https://xinlian-sign-1255543036.cos.ap-guangzhou.myqcloud.com/thumb/27904268.png";

    private String senderName;

    private String msg;

    @Indexed
    private Date sendTime;
}
