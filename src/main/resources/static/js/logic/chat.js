var websocket = null;   // websocket对象
var loginUser = null;   // 登录用户名
var sid = null;         // 登录用户的sid
var requestSid_1=null;  // 请求者的sid
var requestedSid_1=null;// 被请求者的sid
var URL = null;         // URL
var roomNo = null;      // 房间号
var peopleNum = null;   // 房间人数
var real_public_key;    // 生成的真正的公钥
var real_private_key;   // 生成的真正的私钥
var aesKey;             // 被请求者生成的aesKey
var consult_public_key; // 协商后获得的RSA公钥
var consult_aesKey;     // 协商后获得的aesKey
var encryptSwitch = 1;  // 加解密开关：1为加密，0为不加密（到时候由后台统一管理）
var index = 0;          // 聊天消息span的动态id值
var chat = {
    init : function() {
        // 生成RSA密钥对
        if (encryptSwitch == 1) {
            chat.genRSAKeyPair();
            if (real_public_key == undefined || real_private_key == undefined) {
                console.error("RSA密钥对初始化失败!");
                return 0;
            }
        }
        // 初始化URL
        var basePath = common.basePath; // http://localhost:8066/chat/

        // 获取当前登录用户
        loginUser = $("#loginUser").val();

        // 获取登录用户对应的sid
        sid = $("#sid").val();

        // 获取用户被分配到的房间号
        roomNo = $("#roomNo").val();

        // 获取房间类型
        var roomType = $("#roomType").val();

        if (basePath == null || basePath == '' || loginUser == null || loginUser == '' ||
            sid == null || sid == '' || roomNo == null || roomNo == '' || roomType == null || roomType == '') {
            // TODO 弹出提示框
            console.error("参数初始化失败!");
            return 0;
        }

        // 相关设置
        if (roomType == "private") {
            $("#chat-title-nav").text("私密聊");
        } else if (roomType == "public") {
            $("#chat-title-nav").text("多人聊");
            $(".chat-title").css("background", "#3c3c3c");
            $(".chat-content").css("background", "#b2b2b7cc");
        } else if (roomType == "customization") {
            $("#chat-title-nav").text("自定义聊");
        }

        URL = basePath + "websocket/" + sid + "," + loginUser + "," +roomNo;
        URL = URL.replace("https", "ws").replace("http", "ws");
        console.log("URL : "+URL);
    },
    genRSAKeyPair : function() {
        var crypto = common.rsa.genKeyPair();
        // 获取公钥
        real_public_key = crypto.getPublicKey();
        // 获取私钥
        real_private_key = crypto.getPrivateKey();
    },
    getConnect : function() {
        // 判断当前浏览器是否支持WebSocket
        if ('WebSocket' in window) {
            websocket = new WebSocket(URL);
        } else {
            layer.msg("Current device not support websocket !");
            return;
        }

        if (websocket == null) {
            console.log("");
            layer.msg("websocket 连接发生错误 ！");
            return;
        }

        // 连接发生错误的回调方法
        websocket.onerror = function () {
            chat.showMessage("websocket 连接发生错误！");
            console.log("onerror : websocket 连接发生错误 ！");
            layer.msg("websocket 连接发生错误 ！");
        };

        // 连接成功后的回调方法
        websocket.onopen = function () {
            console.log("websocket 连接成功...");
        };

        // 连接关闭的回调方法
        websocket.onclose = function (event) {
            chat.showMessage("websocket 连接已断开！");
            console.log("onclose : websocket 连接发生错误 ！");
            // 重连
            // chat.getConnect();
        };

        // 接收到消息的回调方法
        websocket.onmessage = function (msg) {
            var m = msg.data.split(",");
            // 获取用户发送的消息
            var message = m[0];

            // 前端规则匹配，根据用户消息指令的不同，前端完成不同的页面展示
            if (message == "code_force_logout") {
                // 强制登出
                layer.alert('您的账号已在别处登录，请别激动！',{
                    skin: 'layui-layer-molv' //样式类名
                    ,closeBtn: 0
                },function(){
                    // 因为此用户在后台已经被清除session会话了，这里直接去登录界面就行，就不需要去logout了
                    // common.basePath : http://localhost:8066/chat/
                    window.location = common.basePath;
                });
                return;
            }
            // socketServer.sendMessage(flag+","+this.sid+","+this.loginUser+","+this.roomNo+","+peopleNum);
            if (message == "onOpen" || message == "onClose") {
                // 获取发送者的房间号
                var sendRoomNo = m[3];
                // 判断发送者的房间号 与 当前用户的房间号是否一致，一致才真正推送房间信息（双重保险保证聊天信息不串房间，因为后台已经是根据房间号来推送消息了）
                if (roomNo == sendRoomNo) {
                    // 展示房间人数
                    peopleNum = m[4];
                    $("#people-num").text(m[4]);
                    // 展示房间号
                    $("#room-no").text(roomNo);
                }
            }
            if (encryptSwitch == 1) {
                // socketServer.sendMessage("requestRSAPubKey"+","+this.sid); // this.sid为请求者的sid
                if (message == "requestRSAPubKey") {
                    // 有用户来请求RSA公钥
                    if (real_public_key == undefined || real_private_key == undefined) {
                        // 提示密钥协商失败
                        return;
                    }
                    // 获取请求者的sid
                    var requestSid = m[1];

                    // 生成AES key（被请求者去调用的）
                    aesKey = common.aes.genKey();
                    console.log("aesKey : "+aesKey);
                    /*
                     * 这里我将：真正的real_public_key作为私钥来加密数据（在公网上传输的都是公钥，用私钥加密数据，对方拿到你的公钥来解密）
                     *          真正的real_private_key作为公钥在公网上传输
                     *          为何要这样：貌似js的RSA加解密算法有点问题，不能够实现：长的私钥加密，短的公钥解密，但可以短的加密，长的解密，所以这里我才在公网上传输长的
                     */
                    // 用RSA私钥加密aesKey 并发送给房间内除了被请求者的其它人
                    var aesKeyCipherText = common.rsa.encryptPublic(real_public_key, aesKey);
                    var data = "sendRSAPubKey" + "," + requestSid + "," + sid +"," + real_private_key + "," + aesKeyCipherText; // sid为：被请求者的sid
                    chat.send(data);
                    // 提示“密钥协商中，请稍候...”
                    return;
                }
                // dialogue.sendMessage("receiveRSAPubKey"+","+requestSid+","+requestedSid+","+public_key+","+aesKeyCipherText);
                if (message == "receiveRSAPubKey") {
                    var requestSid = m[1];
                    var requestedSid = m[2];
                    consult_public_key = m[3];
                    consult_aesKey = common.rsa.decryptPrivate(consult_public_key, m[4]);
                    console.log("consult_aesKey : "+consult_aesKey);
                    if (consult_aesKey != undefined) {
                        common.aes.key = consult_aesKey;
                        // 判断是否是原始请求者，是的话才告知房间内的被请求者已成功收到AES密钥
                        if (sid == requestSid) {
                            var data = "successGetAesKey" + "," + requestSid + "," +requestedSid;
                            chat.send(data);
                        } else {
                            console.log("aesKey已更新完毕!");
                        }
                    }
                    return;
                }
            }

            // 判断消息是否需要解密
            if (encryptSwitch == 1 && peopleNum > 1 && message != "onOpen" && message != "onClose" && message != "aesConsultSuccess") {
                message = common.aes.decrypt(message);
            }
            // 消息format
            message = chat.msgFormat(message);

            // dialogue.sendMessage("aesConsultSuccess"+","+requestSid+","+requestedSid);
            if (message == "aesConsultSuccess") {
                requestSid_1 = m[1];
                requestedSid_1 = m[2];
                chat.showMessage(message);
                return;
            }

            // 获取发送消息者的sid
            var sendSid = m[1];
            // 获取发送消息者的用户名
            var sendMsgUser = m[2];
            // 判断当前登录用户的sid是否为发送消息用户的sid（用sid来比较，比用用户名来比较更可靠）
            if (sid == sendSid) {
                // 当前用户（需要清除当前用户input框内的内容）
                $("#send-content").val("");
                chat.showMessage(message, 1, loginUser);
            } else {
                // 非当前用户（其他用户）
                chat.showMessage(message, 0, sendMsgUser);
            }
        };
    },
    showMessage : function(message, flag , user) {
        var html = "";
        if (message == "onOpen") {
            html = '<div class="sys-msg"><span class="sys-msg-span">[系统消息:] ['+user+'] 已进入房间...</span></div>';
        } else if (message == "onClose") {
            html = '<div class="sys-msg"><span class="sys-msg-span">[系统消息:] ['+user+'] 已离开房间...</span></div>';
        } else if (message == "aesConsultSuccess") {
            if (sid == requestSid_1) {
                html = '<div class="sys-msg"><span class="sys-msg-span">[系统消息:] 密钥协商成功，可愉快的聊天啦~</span></div>';
            } else if (sid == requestedSid_1) {
                html = '<div class="sys-msg"><span class="sys-msg-span">[系统消息:] 密钥协商成功啦~</span></div>';
            }
        } else {
            if (flag == 1) {        // 当前用户
                html = '<div class="chat-item item-right clearfix"><span class="img fr"></span><span class="message fr" id="message-'+ ++index +'">'+message+'</span></div>';
            } else if (flag == 0) { // 非当前用户
                html='<div class="chat-item item-left clearfix rela"><span class="abs uname">'+user+'</span><span class="img fl"></span><span class="fl message" id="message-'+ ++index +'">'+message+'</span></div>';
            }
        }
        $('.chat-content').append(html);
        adjustMsgWidth(flag, index);
        scrollTop();
    },
    send : function(data) {
        console.log("send ... ");
        if (data == undefined) {
            if (websocket != null) {
                var sendContent = $("#send-content").val();
                // 当房间人数大于1时，才进行加密传输
                if (encryptSwitch == 1 && peopleNum > 1 && sendContent.length > 0) {
                    sendContent = common.aes.encrypt(sendContent);
                }
                try {
                    if (sendContent.length > 0) {
                        // 判断 websocket 当前状态：CONNECTING = 0, OPEN = 1, CLOSING = 2, CLOSED = 3
                        if (websocket.readyState === websocket.CLOSING || websocket.readyState === websocket.CLOSED) {
                            // 重连
                            // chat.getConnect();
                        }
                        if (websocket.readyState === websocket.OPEN) {
                            websocket.send( sendContent );
                        }
                    } else {
                        layer.msg("请输入内容");
                    }
                } catch (e) {
                    console.error(e);
                    console.error("websocket 连接已经关闭");
                }
            } else {
                console.error("websocket 没有连接成功")
            }
        } else {
            try {
                websocket.send( data );
            } catch (e) {
                console.error(e);
                console.error("websocket 连接已经关闭");
            }
        }
    },
    msgFormat : function(message) {
        var msg = '';
        for(var i=0; i<message.length; i++) {
            var char = message.charAt(i);
            if (char == ' ') {
                char = "&nbsp;"
            }
            msg += char;
        }
        return msg;
    },
    getHeight : function () {
        console.log("window : "+$(window).height());
        return $(window).height() - $(".chat-title").height() - $(".chat-bottom").height();
    },
    setHeight : function () {
        $(".chat-content").height(chat.getHeight());
    },
    blur : function () {
        setTimeout(function () {
            window.scrollTo(0, 0);
        },20);
    }
};
function scrollTop() {
    document.getElementById("chat-content").scrollTop = 99999999999;
}
function adjustMsgWidth(flag, index) {
    if (flag == 1 || flag == 0) { // 当前用户 或 非当前用户
        // 判断span的宽度，此时window的宽度
        var winHeight = $(window).height();
        var winWidth = $(window).width();
        var message_right_width = $("#message-"+index+"").width();
        if (winHeight > winWidth) {
            // 说明是竖屏
            if ( (message_right_width / winWidth) >= 0.67 ) {
                $("#message-"+index+"").width(67 + '%');
            }
        } else {
            // 说明是宽屏
            if ( (message_right_width / winWidth) >= 0.80 ) {
                $("#message-"+index+"").width(80 + '%');
            }
        }
    }
}

// 监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
window.onbeforeunload = function () {
    websocket.close();
};

window.onresize = function(){
    chat.setHeight();
    // TODO 下次要做到屏幕尺寸变化，再次判断消息的是否要换行问题，需要遍历每一行消息
};

document.onkeydown = function(event){
    var e = event || window.event || arguments.callee.caller.arguments[0];
    if(e && e.keyCode==13){ // enter 键
        chat.send();
    }
};

$(function () {
    console.log("chat 页面 -> start ...... ");

    // 设置聊天窗口高度
    chat.setHeight();

    // init 若初始化数据失败，则不发起websocket连接
    var result = chat.init();
    if (result != 0){
        chat.getConnect();
    }
});