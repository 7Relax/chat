package com.seven.chat.utils;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import org.apache.commons.codec.binary.Base64;

/**
 * 一、概述
 *
 *  1、RSA是基于大数因子分解难题。目前各种主流计算机语言都支持RSA算法的实现
 *  2、java6支持RSA算法
 *  3、RSA算法可以用于数据加密和数字签名
 *  4、RSA算法相对于DES/AES等对称加密算法，他的速度要慢的多
 *  5、总原则：（公钥加密，私钥解密）/（私钥加密，公钥解密）
 *
 * 二、模型分析
 *
 * RSA算法构建密钥对简单的很，这里我们还是以甲乙双方发送数据为模型
 * 1、甲方在本地构建密钥对（公钥+私钥），并将公钥公布给乙方
 * 2、甲方将数据用私钥进行加密，发送给乙方
 * 3、乙方用甲方提供的公钥对数据进行解密
 *
 * 如果乙方向传送数据给甲方：
 * 4、乙方用公钥对数据进行加密，然后传送给甲方
 * 5、甲方用私钥对数据进行解密
 *
 *  R S A 算法要求待加密的数据在密钥长度为512时不得大于53个字节
 * （密钥长度为1024时加密数据不大于117，2024时为245，规律是2^10的话，则加密的数据为2^(10-3)-11 即 2^7-11=117  ）
 *
 * 非对称加密算法RSA算法组件
 * 非对称算法一般是用来传送对称加密算法的密钥来使用的，相对于DH算法，RSA算法只需要一方构造密钥，不需要
 * 大费周章的构造各自本地的密钥对了。DH算法只能算是非对称算法的底层实现。而RSA算法算法实现起来较为简单
 */
public class RSACoder {
    // 非对称密钥算法
    public static final String KEY_ALGORITHM = "RSA";

    /**
     * R S A 算法的密钥长度必须是64的倍数，在512到65536位之间（2^9 ~ 2^16-1）
     */
    private static final int KEY_SIZE = 1024;

    // 公钥
    private static final String PUBLIC_KEY = "RSAPublicKey";

    // 私钥
    private static final String PRIVATE_KEY = "RSAPrivateKey";

    /**
     * 初始化密钥对
     * @return Map 甲方密钥的Map
     * */
    public static Map<String,Object> initKey() throws Exception{
        //实例化密钥生成器
        KeyPairGenerator keyPairGenerator=KeyPairGenerator.getInstance(KEY_ALGORITHM);
        //初始化密钥生成器
        keyPairGenerator.initialize(KEY_SIZE);
        //生成密钥对
        KeyPair keyPair=keyPairGenerator.generateKeyPair();
        //甲方公钥
        RSAPublicKey publicKey=(RSAPublicKey) keyPair.getPublic();
        //甲方私钥
        RSAPrivateKey privateKey=(RSAPrivateKey) keyPair.getPrivate();
        //将密钥存储在map中
        Map<String,Object> keyMap=new HashMap<String,Object>();
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }


    /**
     * 私钥加密
     * @param data 待加密数据
     * @param key 密钥
     * @return byte[] 加密数据
     */
    public static byte[] encryptByPrivateKey(byte[] data, byte[] key) throws Exception{

        //取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec=new PKCS8EncodedKeySpec(key);
        KeyFactory keyFactory=KeyFactory.getInstance(KEY_ALGORITHM);
        //生成私钥
        PrivateKey privateKey=keyFactory.generatePrivate(pkcs8KeySpec);
        //数据加密
        Cipher cipher=Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }
    /**
     * 公钥加密
     * @param data 待加密数据
     * @param key 密钥
     * @return byte[] 加密数据
     */
    public static byte[] encryptByPublicKey(byte[] data,byte[] key) throws Exception{

        //实例化密钥工厂
        KeyFactory keyFactory=KeyFactory.getInstance(KEY_ALGORITHM);
        //初始化公钥
        //密钥材料转换
        X509EncodedKeySpec x509KeySpec=new X509EncodedKeySpec(key);
        //产生公钥
        PublicKey pubKey=keyFactory.generatePublic(x509KeySpec);

        //数据加密
        Cipher cipher=Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return cipher.doFinal(data);
    }
    /**
     * 私钥解密
     * @param data 待解密数据
     * @param key 密钥
     * @return byte[] 解密数据
     * */
    public static byte[] decryptByPrivateKey(byte[] data,byte[] key) throws Exception{
        //取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec=new PKCS8EncodedKeySpec(key);
        KeyFactory keyFactory=KeyFactory.getInstance(KEY_ALGORITHM);
        //生成私钥
        PrivateKey privateKey=keyFactory.generatePrivate(pkcs8KeySpec);
        //数据解密
        Cipher cipher=Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }
    /**
     * 公钥解密
     * @param data 待解密数据
     * @param key 密钥
     * @return byte[] 解密数据
     * */
    public static byte[] decryptByPublicKey(byte[] data,byte[] key) throws Exception{

        //实例化密钥工厂
        KeyFactory keyFactory=KeyFactory.getInstance(KEY_ALGORITHM);
        //初始化公钥
        //密钥材料转换
        X509EncodedKeySpec x509KeySpec=new X509EncodedKeySpec(key);
        //产生公钥
        PublicKey pubKey=keyFactory.generatePublic(x509KeySpec);
        //数据解密
        Cipher cipher=Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, pubKey);
        return cipher.doFinal(data);
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
        // 初始化密钥
        // 生成密钥对
        Map<String,Object> keyMap = RSACoder.initKey();
        // 公钥
        byte[] publicKey = RSACoder.getPublicKey(keyMap);
        // 私钥
        byte[] privateKey = RSACoder.getPrivateKey(keyMap);

        System.out.println("公钥："+Base64.encodeBase64String(publicKey));
        System.out.println("私钥："+Base64.encodeBase64String(privateKey));

        System.out.println("================密钥对构造完毕,甲方将公钥公布给乙方，开始进行加密数据的传输=============");
        String str = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyza";
        System.out.println("str's length : "+str.length());
        System.out.println("===========甲方向乙方发送加密数据==============");
        System.out.println("原文:"+str);
        // 甲方进行数据的加密
        byte[] code1 = RSACoder.encryptByPrivateKey(str.getBytes(), privateKey);
        System.out.println("加密后的数据："+Base64.encodeBase64String(code1));
        // 乙方进行数据的解密
        System.out.println("===========乙方使用甲方提供的公钥对数据进行解密==============");
        byte[] decode1 = RSACoder.decryptByPublicKey(code1, publicKey);
        System.out.println("乙方解密后的数据："+new String(decode1));

        System.out.println("===========反向进行操作，乙方向甲方发送数据==============");
        str = "乙方向甲方发送数据RSA算法";
        System.out.println("原文:"+str);

        // 乙方使用甲方的公钥对数据进行加密
        byte[] code2 = RSACoder.encryptByPublicKey(str.getBytes(), publicKey);
        System.out.println("===========乙方使用甲方的公钥对数据进行加密==============");
        System.out.println("加密后的数据："+Base64.encodeBase64String(code2));

        System.out.println("=============乙方将数据传送给甲方======================");
        System.out.println("===========甲方使用私钥对数据进行解密==============");

        //甲方使用私钥对数据进行解密
        byte[] decode2=RSACoder.decryptByPrivateKey(code2, privateKey);

        System.out.println("甲方解密后的数据："+new String(decode2));
    }
}