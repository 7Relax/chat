package com.seven.chat.utils;

import org.apache.commons.codec.binary.Base64;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 非对称加密算法DH算法组件
 * 非对称算法一般是用来传送对称加密算法的密钥来使用的，所以这里我们用DH算法模拟密钥传送
 * 对称加密AES算法继续做我们的数据加解密
 * 一、概述
 *   1、与对称加密算法的主要差别在于，加密和解密的密钥不相同，一个公开（公钥），一个保密（私钥）。主要解决了对称加密算法密钥分配管理的问题，提高了算法安全性。
 *   2、非对称加密算法的加密、解密的效率比较低。在算法设计上，非对称加密算法对待加密的数据长度有着苛刻的要求。例如RSA算法要求待加密的数据不得大于53个字节。
 *   3、非对称加密算法主要用于交换对称加密算法的密钥，而非数据交换。
 *   4、java6提供实现了DH和RSA两种算法。Bouncy Castle提供了E1Gamal算法支持。除了上述三种算法还有一个ECC算法，目前没有相关的开源组件提供支持。
 *
 * 二、模型分析
 *   我们还是以甲乙双方发送数据为模型进行分析
 *   1、甲方（消息发送方，下同）构建密钥对（公钥+私钥），甲方公布公钥给乙方（消息接收方，下同）
 *   2、乙方以甲方发送过来的公钥作为参数来构造密钥对（公钥+私钥），将构造出来的公钥公布给甲方
 *   3、甲方用“甲方的私钥+乙方的公钥”构造本地密钥
 *   4、乙方用“乙方的私钥+甲方的公钥”构造本地的密钥
 *   5、这个时候，甲乙两方本地新构造出来的密钥应该一样。然后就可以使用AES这类对称加密算法结合密钥进行数据的安全传送了。传送过程参考AES的相关算法
 *
 * 三、总结
 *   1、非对称加密算法主要用来传递密钥的，而且性能较低。但是安全性超强。非对称加密算法能加密的数据长度也受限
 *   2、用非对称加密算法算出甲乙双方本地的密钥后，可以选择DES/AES/DESede这些对称加密算法进行数据的传送了
 */
public class DHCoder {

    /**
     * 非对称密钥算法
     */
    public static final String KEY_ALGORITHM = "DH";

    /**
     * 本地密钥算法，即对称加密算法。可选AES、DES、DESede
     */
    public static final String SECRET_ALGORITHM = "AES";

    /**
     * 密钥长度，DH算法的默认密钥长度是1024
     * 密钥长度必须是64的倍数，在512到1024位之间
     * */
    private static final int KEY_SIZE = 512;

    /**
     * 公钥
     */
    private static final String PUBLIC_KEY = "DHPublicKey";

    /**
     * 私钥
     */
    private static final String PRIVATE_KEY = "DHPrivateKey";

    /**
     * 初始化甲方密钥
     * @return Map 甲方密钥的Map
     * */
    public static Map<String,Object> initKey() throws Exception {
        //实例化密钥生成器
        KeyPairGenerator keyPairGenerator=KeyPairGenerator.getInstance(KEY_ALGORITHM);
        //初始化密钥生成器
        keyPairGenerator.initialize(KEY_SIZE);
        //生成密钥对
        KeyPair keyPair=keyPairGenerator.generateKeyPair();
        //甲方公钥
        DHPublicKey publicKey=(DHPublicKey) keyPair.getPublic();
        //甲方私钥
        DHPrivateKey privateKey=(DHPrivateKey) keyPair.getPrivate();
        //将密钥存储在map中
        Map<String,Object> keyMap=new HashMap<String,Object>();
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;

    }

    /**
     * 初始化乙方密钥
     * @param key 甲方密钥（这个密钥是通过第三方途径传递的）
     * @return Map 乙方密钥的Map
     * */
    public static Map<String,Object> initKey(byte[] key) throws Exception {
        //解析甲方的公钥
        //转换公钥的材料
        X509EncodedKeySpec x509KeySpec=new X509EncodedKeySpec(key);
        //实例化密钥工厂
        KeyFactory keyFactory=KeyFactory.getInstance(KEY_ALGORITHM);
        //产生公钥
        PublicKey pubKey=keyFactory.generatePublic(x509KeySpec);
        //由甲方的公钥构造乙方密钥
        DHParameterSpec dhParamSpec=((DHPublicKey)pubKey).getParams();
        //实例化密钥生成器
        KeyPairGenerator keyPairGenerator=KeyPairGenerator.getInstance(keyFactory.getAlgorithm());
        //初始化密钥生成器
        keyPairGenerator.initialize(dhParamSpec);
        //产生密钥对
        KeyPair keyPair=keyPairGenerator.genKeyPair();
        //乙方公钥
        DHPublicKey publicKey=(DHPublicKey)keyPair.getPublic();
        //乙方私钥
        DHPrivateKey privateKey=(DHPrivateKey)keyPair.getPrivate();
        //将密钥存储在Map中
        Map<String,Object> keyMap=new HashMap<String,Object>();
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }
    /**
     * 加密
     * @param
     * @param key 密钥
     * @return byte[] 加密数据
     * */
    public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        // 生成本地密钥
        SecretKey secretKey = new SecretKeySpec(key, SECRET_ALGORITHM);
        // 数据加密
        Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }
    /**
     * 解密
     * @param data 待解密数据
     * @param key 密钥
     * @return byte[] 解密数据
     * */
    public static byte[] decrypt(byte[] data,byte[] key) throws Exception{
        // 生成本地密钥
        SecretKey secretKey = new SecretKeySpec(key, SECRET_ALGORITHM);
        // 数据解密
        Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }
    /**
     * 构建密钥
     * @param publicKey 公钥
     * @param privateKey 私钥
     * @return byte[] 本地密钥
     * */
    public static byte[] getSecretKey(byte[] publicKey,byte[] privateKey) throws Exception{
        // 实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        // 初始化公钥
        // 密钥材料转换
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);
        // 产生公钥
        PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
        // 初始化私钥
        // 密钥材料转换
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey);
        // 产生私钥
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);
        // 实例化
        KeyAgreement keyAgree = KeyAgreement.getInstance(keyFactory.getAlgorithm());
        // 初始化
        keyAgree.init(priKey);
        keyAgree.doPhase(pubKey, true);
        // 生成本地密钥
        SecretKey secretKey=keyAgree.generateSecret(SECRET_ALGORITHM);
        return secretKey.getEncoded();
    }
    /**
     * 取得私钥
     * @param keyMap 密钥map
     * @return byte[] 私钥
     * */
    public static byte[] getPrivateKey(Map<String,Object> keyMap){
        Key key=(Key)keyMap.get(PRIVATE_KEY);
        return key.getEncoded();
    }
    /**
     * 取得公钥
     * @param keyMap 密钥map
     * @return byte[] 公钥
     * */
    public static byte[] getPublicKey(Map<String,Object> keyMap) throws Exception{
        Key key=(Key) keyMap.get(PUBLIC_KEY);
        return key.getEncoded();
    }
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.out.println("DH算法密钥长度："+KEY_SIZE);
        // 甲方是消息发送方，乙方是接收方
        // 生成甲方的密钥对
        Map<String,Object> keyMap1 = DHCoder.initKey();
        // 甲方的公钥
        byte[] publicKey1 = DHCoder.getPublicKey(keyMap1);
        // 甲方的私钥
        byte[] privateKey1 = DHCoder.getPrivateKey(keyMap1);
        System.out.println("甲方公钥："+Base64.encodeBase64String(publicKey1));
        System.out.println("甲方公钥长度："+Base64.encodeBase64String(publicKey1).length());
        System.out.println("甲方私钥："+Base64.encodeBase64String(privateKey1));
        System.out.println("甲方私钥长度："+Base64.encodeBase64String(privateKey1).length());

        // 由甲方的公钥作为参数产生的乙方的密钥对
        Map<String,Object> keyMap2 = DHCoder.initKey(publicKey1);
        byte[] publicKey2 = DHCoder.getPublicKey(keyMap2);
        byte[] privateKey2 = DHCoder.getPrivateKey(keyMap2);
        System.out.println("乙方公钥："+Base64.encodeBase64String(publicKey2));
        System.out.println("乙方私钥："+Base64.encodeBase64String(privateKey2));

        // 组装甲方的本地密钥，由乙方的公钥和甲方的私钥组合而成
        byte[] key1 = DHCoder.getSecretKey(publicKey2, privateKey1);
        System.out.println("甲方的本地密钥："+Base64.encodeBase64String(key1));

        // 组装乙方的本地加密密钥，由甲方的公钥和乙方的私钥组合而成
        byte[] key2 = DHCoder.getSecretKey(publicKey1, privateKey2);
        System.out.println("乙方的本地密钥："+Base64.encodeBase64String(key2));

        System.out.println("================密钥对构造完毕，开始进行加密数据的传输=============");
        String str = "密码交换算法12343214321";
        str = "abcdefghijklmnopqrstuvwxyz";
        System.out.println("===========甲方向乙方发送加密数据==============");
        System.out.println("原文 : "+str);
        System.out.println(str.length());

        System.out.println("===========使用甲方本地密钥对进行数据加密==============");
        //甲方进行数据的加密
        byte[] code1 = DHCoder.encrypt(str.getBytes(), key1);
        System.out.println("加密后的数据："+Base64.encodeBase64String(code1));

        System.out.println("===========使用乙方本地密钥对数据进行解密==============");
        //乙方进行数据的解密
        byte[] decode1 = DHCoder.decrypt(code1, key2);
        System.out.println("乙方解密后的数据："+new String(decode1));







        System.out.println();
        System.out.println("========================反向进行操作，乙方向甲方发送数据=============================");

        str="乙方向甲方发送数据：啦啦啦";

        System.out.println("原文:"+str);

        //使用乙方本地密钥对数据进行加密
        byte[] code2 = DHCoder.encrypt(str.getBytes(), key2);
        System.out.println("===========使用乙方本地密钥对进行数据加密==============");
        System.out.println("加密后的数据："+Base64.encodeBase64String(code2));

        System.out.println("=============乙方将数据传送给甲方======================");
        System.out.println("===========使用甲方本地密钥对数据进行解密==============");

        //甲方使用本地密钥对数据进行解密
        byte[] decode2 = DHCoder.decrypt(code2, key1);

        System.out.println("甲方解密后的数据："+new String(decode2));

    }

}