package com.seven.chat.entity;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.Date;

/**
 * @Description: 房间实体
 * @Author: Seven
 * @Date: 2019/06/24 19:57
 */
public class Room {

    // 主键id
    private String id;

    // 房间号
    private Integer roomNumber;

    // 房间类型：1私聊房 2群聊房
    private String roomType;

    // 房间是否满员：1未满员 2满员
    private String isFull;

    // 用户的socketId
    private String socketId;

    // 房间状态：1未使用 2使用中 3失效
    private String status;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;

}
