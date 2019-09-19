package com.seven.chat.domain;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Discription: 系統常量
 * @Author Zaki Chen
 * @date 2019/3/19 13:48
 **/
public class SystemConstant {
    /**
     * 系统管理员角色
     */
    public static final String ROLE_ADMIN = "2";
    /**
     * 普通用户角色
     */
    public static final String ROLE_NORMAL = "1";
    /**
     * API request succeed code
     */
    public static final int RESPONSE_SUCCESS_CODE = 0;

    /**
     * API request failed code
     */
    public static final int RESPONSE_FAIL_CODE = 2;

    /**
     * API request succeed message
     */
    public static final String RESPONSE_SUCCESS_MSG = "success";

    /**
     * API request fail message
     */
    public static final String RESPONSE_FAIL_MSG = "fail";

    /**
     * ATM状态报文 MsgType
     */
    public static final String MSG_TYPE_STATUS_ATM = "ATMStatus";
    /**
     * VTM状态报文 MsgType
     */
    public static final String MSG_TYPE_STATUS_VTM = "VTMStatus";
    /**
     * 交易报文 MsgType
     */
    public static final String MSG_TYPE_TRANSACTION = "Transaction";
    /**
     * 吞卡报文 MsgType
     */
    public static final String MSG_TYPE_RETAINCARD = "RetainedCard";

    /**
     * 用户Token 集合
     */
    public static final Map<String,String> USER_TOKEN_MAP = new HashMap<>();

    /**
     *系统是否是第一次运行标志，决定是否要初始化基础数据
     */
    public static boolean IS_FIRST_RUN = false;

    /**
     * 用户单点登录，用户与sid的映射map
     */
    public static Map<String, String> USER_SOCKET_MAP = new HashMap<>();

    /**
     * sid与HttpSession的映射map
     */
    public static Map<String, Object> SOCKET_SESSION = new HashMap<>();

    /**
     * privateRoomNo Set
     */
    public static Set<Integer> privateRoomNoSet = new HashSet<Integer>();

    /**
     * publicRoomNo Set
     */
    public static Set<Integer> publicRoomNoSet = new HashSet<Integer>();

    /**
     * customizationRoomNo Set
     */
    public static Set<Integer> customizationRoomNoSet = new HashSet<Integer>();


}