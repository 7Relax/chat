package com.seven.chat;

import com.seven.chat.dao.mapper.UserMapper;
import com.seven.chat.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ChatApplicationTests {

	@Autowired
	private UserMapper userMapper;

	@Test
	public void test1() {
		User user = userMapper.getUserById("123");
		System.out.println(user);
	}

}