package com.seven.chat.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/06/22 13:15
 */
@ServerEndpoint("/websocket/{params}")
@Component
public class WebSocketServer {

    private Logger logger =  LoggerFactory.getLogger(getClass());

    // concurrent包的线程安全Set，用来存放每个客户端对应的WebSocket对象
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

    // websocket session
    private Session session;

    // sid
    private String sid;

    // 登录用户的用户名
    private String loginUser;

    // 房间号
    private Integer roomNo;

    /**
     * websocket连接成功后调用的方法
     * @param session
     * @param params
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("params") String params) {
        logger.info("params : "+params);
        String strs[] = params.split("[,]");
        this.session = session;
        this.sid = strs[0];
        this.loginUser = strs[1];
        this.roomNo = Integer.valueOf(strs[2]);
        // 判断webSocketSet是否已有此sid
        Iterator<WebSocketServer> iterator = webSocketSet.iterator();
        while (iterator.hasNext()) {
            WebSocketServer webSocketServer = iterator.next();
            if (this.sid.equals(webSocketServer.sid)) {
                // 找到则踢出
                try {
                    webSocketServer.sendMessage("code_force_logout");
                    webSocketSet.remove(webSocketServer);
                    logger.warn("有一用户被踢出！当前在线总人数为：" + getOnlineCount());
                } catch (IOException e) {
                    logger.error("踢出失败！当前在线总人数为：" + getOnlineCount());
                    e.printStackTrace();
                }
            }
        }
        // 加入set中，this：此时连接到服务端的客户端与服务端的连接信息，这里指WebSocketServer（主要是其成员变量session，维护了一个客户端与服务端的会话）
        webSocketSet.add(this);
        logger.info("有新连接加入！当前在线总人数为：" + getOnlineCount());

        // 获取房间在线人数
        int peopleNum = getRoomOnlineCount(this.roomNo);
        if (peopleNum > 1) {
            // 找到房间内除了自身外的其他任意一个人（随机），并向他请求 RSAPubKey 及 aesKeyCipherText
            requestRSAPubKey();
        }

        // 推送房间信息给房间里所有的用户
        pushRoomMsg(peopleNum, "onOpen");
    }

    /**
     * 客户端关闭连接后调用的方法
     */
    @OnClose
    public void onClose() {
        // 从set中移除
        webSocketSet.remove(this);
        logger.info("有一连接关闭！当前在线总人数为：" + getOnlineCount());
        // 推送房间信息给房间里所有的用户
        int peopleNum = getRoomOnlineCount(this.roomNo);
        pushRoomMsg(peopleNum, "onClose");
    }

    /**
     * 收到客户端后调用的方法
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        String[] strs = message.split(",");
        String msg = strs[0];
        String requestSid = "";   // 请求者的sid
        String requestedSid = ""; // 被请求的sid
        String public_key = "";
        String aesKeyCipherText = "";
        if (strs.length > 1) {
            // var data = "sendRSAPubKey" + "," + requestSid + "," + sid +"," + real_private_key + "," + aesKeyCipherText; sid为：被请求者的sid, real_private_key我已经把它作为公钥了！
            if ("sendRSAPubKey".equals(msg)) {
                requestSid = strs[1];
                requestedSid = strs[2];
                public_key = strs[3];
                aesKeyCipherText = strs[4];
            }
            // var data = "successGetAesKey" + "," + requestSid + "," +requestedSid;
            if ("successGetAesKey".equals(msg)) {
                requestSid = strs[1];
                requestedSid = strs[2];
            }
        }
        logger.info("来自客户端的消息：" + message);
        for (WebSocketServer dialogue : webSocketSet) {
            try {
                // this.roomNo : 此时正在发送消息的用户的房间号；为了查找整个WebSocketSet容器中有哪些与他在同一个房间
                if (dialogue.roomNo.equals(this.roomNo)) {
                    if ("sendRSAPubKey".equals(msg)) {
                        if ( !dialogue.sid.equals(requestedSid) ) {
                            // 将public_key、aesCipher等信息发送给房间内除了被请求者的其他人
                            dialogue.sendMessage("receiveRSAPubKey"+","+requestSid+","+requestedSid+","+public_key+","+aesKeyCipherText);
                        }
                    } else if ("successGetAesKey".equals(msg)) {
                        // 告知请求者与被请求者：“密钥协商成功，可愉快的聊天啦~”
//                        if ( dialogue.sid.equals(requestSid) || dialogue.sid.equals(requestedSid) ) {
//                            dialogue.sendMessage("aesConsultSuccess"+","+requestSid+","+requestedSid);
//                        }
                        // 只告知请求者：“密钥协商成功，可愉快的聊天啦~”
                        if ( dialogue.sid.equals(requestSid) ) {
                            dialogue.sendMessage("aesConsultSuccess"+","+requestSid+","+requestedSid);
                        }
                    } else {
                        dialogue.sendMessage(message+","+this.sid+","+this.loginUser);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    /**
     * 发生错误时回调的方法
     * @param session
     * @param throwable
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("发生错误！（有可能是有人非正常退出）");
//        throwable.printStackTrace();
    }

    /**
     * 服务端主动推送消息给客户端（聊天信息、特殊指令）* * * * * *
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 自定义推送消息（如：特殊指令（强制登出等）、在线时长达多少横幅消息展示等）
     * @param message
     * @param socketId
     */
    public void sendInfo(String message, String socketId) {
        for (WebSocketServer dialogue : webSocketSet) {
            try {
                if (socketId == null) {
                    // 推送给所有用户，到时候推送给房间里所有的用户，拿到房间号获取房间信息（用户id，sid）
                    // 如房间号为520的房间：里有2个用户，他们的sid也会存入房间信息表里（room）
                    dialogue.sendMessage(message);

                } else if (socketId.equals(dialogue.sid)) {
                    // 只推送给这个sid对应的用户
                    dialogue.sendMessage(message);

                    // TODO 判断是否是特殊指定，如：强制登出那就需要清除webSocket

                    // 并将其从webSocketSet中清除
                    boolean removeResult = webSocketSet.remove(dialogue);
                    if (removeResult) {
                        logger.warn("有一连接被踢出！当前在线总人数为：" + getOnlineCount());
                    }
                }
            } catch (Exception e) {
                logger.error("sendInfo >> Exception : "+e);
                continue;
            }
        }
    }

    /**
     * 获取总在线人数
     * @return
     */
    public static synchronized int getOnlineCount() {
        return webSocketSet.size();
    }

    /**
     * 获取房间在线人数
     * @param roomNo
     * @return
     */
    public static int getRoomOnlineCount(Integer roomNo) {
        int size = 0;
        for(WebSocketServer socketServer : webSocketSet) {
            if (socketServer.roomNo.equals(roomNo)) {
                size ++;
            }
        }
        return size;
    }

    /**
     * 推送房间内的相关信息给客户端
     * @param peopleNum
     * @param flag : onOpen 成功连接，onClose 关闭连接
     */
    public void pushRoomMsg(int peopleNum, String flag) {
        if (peopleNum > 0) {
            for (WebSocketServer socketServer : webSocketSet) {
                try {
                    // this.roomNo 代表是当前调用者的房间号
                    if (socketServer.roomNo.equals(this.roomNo)) {
                        socketServer.sendMessage(flag+","+this.sid+","+this.loginUser+","+this.roomNo+","+peopleNum);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 请求 RSAPubKey 及 aesKeyCipherText
     */
    public void requestRSAPubKey() {
        for (WebSocketServer socketServer : webSocketSet) {
            try {
                // 找到房间内除了自身外的其他任意一个人（随机），并向他请求 RSAPubKey 及 aesKeyCipherText, this.sid 代表请求者的sid
                if (socketServer.roomNo.equals(this.roomNo) && !socketServer.sid.equals(this.sid)) {
                    socketServer.sendMessage("requestRSAPubKey"+","+this.sid);
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}