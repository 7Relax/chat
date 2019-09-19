package com.seven.chat.controller;

import com.seven.chat.domain.SystemConstant;
import com.seven.chat.utils.RoomUtils;
import com.seven.chat.utils.UUIDTool;
import com.seven.chat.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ScheduledExecutorTask;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/07/06 11:25
 */
@Controller
public class NavigationController {

    /**
     * 私密聊的房间人数
     */
    private static final int roomSize = 2;

    /**
     * 房间随机数的位数
     */
    private static final int randomNum = 3;

    /**
     * WebSocketServer
     */
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 私密聊 or 多人聊
     * @param httpSession
     * @param params
     * @return
     */
    @GetMapping("/privateOrPublicRoom/{params}")
    public String privateRoom(HttpSession httpSession, @PathVariable("params") String params) {

        String[] strs = params.split("[,]");
        String loginUser = strs[0];
        String roomType = strs[1];
        String sid = strs[2];

        if (!StringUtils.isEmpty(loginUser) && !StringUtils.isEmpty(roomType) && !StringUtils.isEmpty(sid)) {
            System.out.println(loginUser);
            System.out.println(sid);

            // 获取随机房间号
            Integer roomNo = RoomUtils.randomNumber(randomNum);

            // 暂时手动写一些房间号，待查找房间功能完善后，就不需要了
            if ("private".equals(roomType)) {
                Integer[] nums = {66, 520};
                roomNo = nums[new Random().nextInt(nums.length)];
            } else if ("public".equals(roomType)) {
                Integer[] nums = {168, 666};
                roomNo = nums[new Random().nextInt(nums.length)];
            }

            // 判断房间号是否被占用（私密聊、多人聊、自定义聊所分配到的房间号都不能相同）
            String flag = "";
            do {
                // 再次循环需要初始化
                flag = "";
                if ("private".equals(roomType)) {
                    if ( SystemConstant.publicRoomNoSet.contains(roomNo) || SystemConstant.customizationRoomNoSet.contains(roomNo) ) {
                        // 需要再次do while
                        flag = "change";
                        // 重新产生一个房间号
                        roomNo = RoomUtils.randomNumber(randomNum);
                    }
                    // 判断房间人数是否已满
                    int size = WebSocketServer.getRoomOnlineCount(roomNo);
                    // 私密聊房间人数默认是2人
                    if (size >= roomSize) {
                        // 房间人数已满，换一个房间
                        flag = "change";
                        roomNo = RoomUtils.randomNumber(randomNum);
                    }
                    // 将用过的房间号存入set中
                    SystemConstant.privateRoomNoSet.add(roomNo);
                } else if ("public".equals(roomType)){
                    if ( SystemConstant.privateRoomNoSet.contains(roomNo) || SystemConstant.customizationRoomNoSet.contains(roomNo) ) {
                        flag = "change";
                        roomNo = RoomUtils.randomNumber(randomNum);
                    }
                    SystemConstant.publicRoomNoSet.add(roomNo);
                } else if ("customization".equals(roomType)) {
                    if ( SystemConstant.privateRoomNoSet.contains(roomNo) || SystemConstant.publicRoomNoSet.contains(roomNo) ) {
                        flag = "change";
                        roomNo = RoomUtils.randomNumber(randomNum);
                    }
                    SystemConstant.customizationRoomNoSet.add(roomNo);
                }
            } while (flag.equals("change"));

            httpSession.setAttribute("roomNo", roomNo);
            httpSession.setAttribute("sid", sid);
            httpSession.setAttribute("loginUser", loginUser);
            httpSession.setAttribute("roomType", roomType);

            // 重写向到chat
            return "redirect:/chat";
        }

        return "main/login";
    }

    /**
     * 自定义聊
     * @param httpSession
     * @param loginUser
     * @return
     */
    @GetMapping("/customization/{loginUser}")
    public String customization(HttpSession httpSession, @PathVariable("loginUser") String loginUser) {
        // 获取传来的房间号

        // 获取传来的房间密码

        // 获取设置房间人数


        // 重写向到chat
        return "redirect:/chat";
    }

    /**
     * 查找房间
     * @return
     */
    @ResponseBody
    @GetMapping("findRoom")
    public String findRoom() {


        return "test";
    }

    public String deal(HttpSession session, String username, Map<String, Object> map) {
        /*
         * 要避免同一个浏览器的HttpSession会是同一个而造成的逻辑错乱
         * 判断登录用户的HttpSession和目前 SOCKET_SESSION 库中的HtppSession是否有相同的
         * 情况1：账号相同，HttpSession不同（1：可能在别处登录，2：可能在不同浏览器登录）：清除map中的HttpSession并踢出
         * 情况2：账号相同，HttpSession相同（可能在同一个浏览器登录）：踢出
         * 情况3：账号不同，HttpSession相同（同一个浏览器登录多个用户）：提示“一个浏览器不能登录多个账号！”
         * 情况4：账号不同，HttpSession不同：正常登录
         */
        // 从map中先获取sid信息，若获取到了则说明此用户已经登录过了，可以将前者踢出了（单点登录）
        String sid = SystemConstant.USER_SOCKET_MAP.get(username);
        HttpSession httpSession = (HttpSession) SystemConstant.SOCKET_SESSION.get(sid);
        boolean flag = true;
        if (httpSession == null) {
            // 遍历map找HttpSession
            Set<Map.Entry<String, Object>> entrySet = SystemConstant.SOCKET_SESSION.entrySet();
            for (Map.Entry<String, Object> entry : entrySet) {
                httpSession = (HttpSession) entry.getValue();
                if ( !session.equals(httpSession) ) {
                    // 返回session是否相同
                    flag = false;
                }
            }
        }
        // 返回session是否相同 true为相同，false为不相同
        if ( !session.equals(httpSession) ) {
            flag = false;
        }
        if (sid != null) {
            // 登录过，因此只要账号相同，则踢出（包括了情况1和情况2：都是踢出）
            webSocketServer.sendInfo("code_force_logout", sid);
            // 账号相同，HttpSession不同
            if ( !flag ) {
                SystemConstant.SOCKET_SESSION.remove(sid);
                sid = UUIDTool.getUUID();
                SystemConstant.USER_SOCKET_MAP.put(username, sid);
                SystemConstant.SOCKET_SESSION.put(sid, session);
                session.setAttribute("sid", sid);
                session.setAttribute("loginUser", username);
            }
            return "redirect:/chat";
        } else {
            // 未登录过
            // 情况3：账号不同，HttpSession相同
            if ( flag ) {
                map.put("msg", "一个浏览器不能登录多个账号");
                return "main/login";
            } else {
                // 情况4：账号不同，HttpSession不同：正常登录：将 username 与 sid 绑定在一起
                sid = UUIDTool.getUUID();
                SystemConstant.USER_SOCKET_MAP.put(username, sid);

                // 拼接sid到URL上，前端可以通过去URL上获取sid，但不建义用这种方式 (org.springframework.web.servlet.mvc.support.RedirectAttributes)
                // redirectAttributes.addAttribute("sid", sid);

                // 将 sid 与 session 会话绑定在一起
                SystemConstant.SOCKET_SESSION.put(sid, session);

                // 通过session传递socketId
                session.setAttribute("sid", sid);

                // 通过session传递当前登录成功用户
//                session.setAttribute("loginUser", username);

                // 登录成功 重定向到
                return "redirect:/chat";
            }
        }

    }

}