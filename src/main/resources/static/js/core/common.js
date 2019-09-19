var common = {
    basePath: "",
    aes: {
        encrypt: function(text) {
            var src = CryptoJS.enc.Utf8.parse(text);                         // 将明文格式化（也可以直接用text）
            var key = CryptoJS.enc.Utf8.parse(common.aes.key);               // 将AESKey格式化
            var iv = CryptoJS.enc.Utf8.parse(common.aes.iv);                 // 将密钥偏移量格式化
            var encryptedData = CryptoJS.AES.encrypt(src, key, {             // 加密后的数据（默认用了Base64编码）
                iv: iv,                                                      // 设置偏移量
                mode: CryptoJS.mode.CBC,                                     // 设置加密模式（CBC）
                padding: CryptoJS.pad.Pkcs7                                  // 设置补码方式（PKCS7Padding）
            });
            // console.log("加密前："+text);
            // console.log("common.aes.key : "+common.aes.key);
            // console.log("加密后："+encryptedData.toString());
            return encryptedData.toString();
        },
        decrypt: function (encryptedData) {
            var key = CryptoJS.enc.Utf8.parse(common.aes.key);               // 将AESKey格式化
            var iv = CryptoJS.enc.Utf8.parse(common.aes.iv);                 // 将密钥偏移量格式化
            var decryptedData = CryptoJS.AES.decrypt(encryptedData, key, {   // 解密后的数据
                iv: iv,
                mode: CryptoJS.mode.CBC,
                padding: CryptoJS.pad.Pkcs7
            });
            // console.log("解密前："+encryptedData);
            // console.log("common.aes.key : "+common.aes.key);
            // console.log("解密后："+decryptedData.toString(CryptoJS.enc.Utf8));
            return decryptedData.toString(CryptoJS.enc.Utf8);                // 解密后的数据toString（明文）
        },
        iv: "6861432665065732",                                              // 密钥偏移量
        key: "origin",
        genKey: function() {
            var str = "^&#3@34a@12!$#fda$&s@!*" + new Date().getTime();
            var ss = sha256_digest(str);
            var key = ss.substring(0, 16);
            common.aes.key = key;
            return key;
        }
    },
    rsa: {
        genKeyPair: function() {
            return new JSEncrypt({default_key_size: common.rsa.keySize}); // 支持 512、1024、2048、4096
        },
        encryptPublic: function(public_key, text) {
            var crypto = new JSEncrypt({default_key_size: common.rsa.keySize});
            crypto.setPublicKey(public_key);
            return crypto.encrypt(text);
        },
        decryptPrivate: function(private_key, encryptedData) {
            var crypto = new JSEncrypt({default_key_size: common.rsa.keySize});
            crypto.setPrivateKey(private_key);
            return crypto.decrypt(encryptedData);
        },
        keySize: 1024
    },
    dh: {
        calc: function(g, n, p) {
            math.config({
                number: 'BigNumber',
                precision: 64
            });
            return math.eval(g + "^" + n + " mod " + p).toFixed();
        },
        genAB: function() {
            var q = 19, a = 15; // 两个全局公开的参数，整数a是素数q的一个原根
            var aKey="",bKey="";
            for (var i=0; i<50; i++) {
                var aSecretKey = Math.floor(Math.random() * q);           // 生成用户A的密钥（随机数，小于q）
                var APublicKey = common.dh.calc(a, aSecretKey, q);           // 计算A的公开密钥
                var bSecretKey = Math.floor(Math.random() * q);
                var BPublicKey = common.dh.calc(a, bSecretKey,q);
                var aGenKeyCode = common.dh.calc(BPublicKey, aSecretKey, q); // A产生共享密钥
                var bGenKeyCode = common.dh.calc(APublicKey, bSecretKey, q);
                aKey += aGenKeyCode;
                bKey += bGenKeyCode;
            }
            document.getElementById('aText').innerText = aKey;
            document.getElementById('bText').innerText = bKey;
            document.getElementById('abResult').innerText = (aKey == bKey);
        },
    }
};
$.fn.extend($.fn,
    {
        ajax: function(url, params, callback, type, contentType, dataType, async) {
            params = JSON.stringify(params);
            url = common.basePath + url;
            type = type || 'post';
            contentType = contentType || 'application/json';
            dataType = dataType || 'json';
            async = async || true;
            $.ajax({
                    url: url,                 // url
                    data: params,             // params
                    type: type,               // "post"
                    contentType: contentType, // "application/json"
                    dataType: dataType,       // "json"
                    async: async,             // 默认是true：异步，false：同步
                    beforeSend: function (XMLHttpRequest) {
                    },
                    success: function (response) {
                        try {
                            callback.call(this, response);
                        } catch (e) {
                            console.error(e);
                        }
                    },
                    error: function (XMLHttpRequest, textStatus, errorThrown) {
                    }
                }
            );
        }
    }, {
        testABC: function(data) {
            alert(data);
        }
    }
);