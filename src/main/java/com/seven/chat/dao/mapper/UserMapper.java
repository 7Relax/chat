package com.seven.chat.dao.mapper;

import com.seven.chat.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/06/23 12:16
 */
public interface UserMapper {

    @Select("select * from user where id = #{id}")
    User getUserById(String id);

    @Select("select * from user where username = #{username}")
    User getUserByUsername(String username);

    @Insert("insert into user (id, username, password, gender, role, status, create_time) " +
            "values ( #{id}, #{username}, #{password}, #{gender}, #{role}, #{status}, #{createTime} ) ")
    Integer addUser(User user);

}