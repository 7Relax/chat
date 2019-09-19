var loginUser = "";
var sid = "";
var navigation = {
    init : function() {
        // 1、调整导航块的相对位置
        adjust();
        // 2、将登录用户存入此页面的全局变量中
        loginUser = $("#loginUser").val();
        // 3、sid
        sid = $("#sid").val();
    },
    private : function () {
        window.location.href = common.basePath + "privateOrPublicRoom" + "/" + loginUser+","+"private"+","+sid;
    },
    public : function () {
        window.location.href = common.basePath + "privateOrPublicRoom" + "/" + loginUser+","+"public"+","+sid;
    },
    customization : function () {
        // window.location.href = common.basePath + "customization" + "/" + loginUser;
        layer.msg('敬请期待...^_^...');
    },
    findRoom : function () {
        layer.msg('敬请期待...^_^...');
        // var html = "<div>全新的页面</div>";
        // var pageii = layer.open({
        //     type: 1
        //     ,content: html
        //     ,anim: 'up'
        //     ,style: 'position:fixed; left:0; top:0; width:100%; height:100%; border: none; -webkit-animation-duration: .5s; animation-duration: .5s;'
        // });
    },
    authorInfo : function () {
        layer.msg('敬请期待...^_^...');

    },
    setting : function () {
        layer.msg('敬请期待...^_^...');
    }
};
// 动态调整导航块在页面中的位置
function adjust() {
    var winHeight = $(window).height();
    var winWidth = $(window).width();
    var whole = $(".nav-whole").height();
    var h1 = (winHeight - whole) / 2; // 居中
    if (winHeight > winWidth) {
        // 说明是竖屏
        $('.nav-whole').css('marginTop',h1/2);
    } else {
        // 说明是宽屏
        $('.nav-whole').css('marginTop',h1);
    }
}
window.onresize = function(){
    // 检测到屏幕尺寸变化后
    adjust();
};
$(function(){
    console.log("navigation 页面 -> start ...... ");
    navigation.init();
});