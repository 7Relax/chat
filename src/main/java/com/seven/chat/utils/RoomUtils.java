package com.seven.chat.utils;

import java.util.Random;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/07/06 10:31
 */
public class RoomUtils {

    /**
     * 获取随机数
     * @param num: 需要产生几位数的随便数
     * @return 返回的随机数
     */
    public static Integer randomNumber(int num) {
        int m = 1;
        for(int i=0; i<num; i++) {
            m *= 10;
        }
        return new Random().nextInt(m);
    }

    public static void main(String[] args) {
        System.out.println(RoomUtils.randomNumber(1));
    }

}