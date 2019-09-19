package com.seven.chat.entity;

import java.util.Date;

/**
 * @Description: 用户实体
 * @Author: Seven
 * @Date: 2019/06/23 12:21
 */
public class User {

    // 主键id
    private String id;

    // 用户名
	private String username;

    // 用户密码
    private String password;

    // 性别：1 男  2 女
    private String gender;

    // 1：管理员 2：普通用户
    private String role;

    // 1：在线 2：离线
    private String status;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;

    // 最后登录时间
    private Date lastLoginTime;

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", gender='" + gender + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", lastLoginTime=" + lastLoginTime +
                '}';
    }

}