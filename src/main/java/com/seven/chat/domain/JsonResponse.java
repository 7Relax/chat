package com.seven.chat.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: Response Entity
 * @Author: Seven
 * @Date: 2019/07/04 20:30
 */
public class JsonResponse {

	/**
	 * build success response without data
	 *
	 * @return
	 */
	public static Map<String, Object> buildSuccess() {
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("resultCode", SystemConstant.RESPONSE_SUCCESS_CODE);
		resultMap.put("resultMessage", SystemConstant.RESPONSE_SUCCESS_MSG);
		return resultMap;
	}

	/**
	 * build success response with extra data
	 *
	 * @param data
	 * @return
	 */
	public static Map<String, Object> buildSuccess(Object data) {
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("resultCode", SystemConstant.RESPONSE_SUCCESS_CODE);
		resultMap.put("resultMessage", SystemConstant.RESPONSE_SUCCESS_MSG);
		resultMap.put("data", data);
		return resultMap;
	}

	/**
	 * build failure response message without data
	 *
	 * @return
	 */
	public static Map<String, Object> buildFailure() {
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("resultCode", SystemConstant.RESPONSE_FAIL_CODE);
		resultMap.put("resultMessage", SystemConstant.RESPONSE_FAIL_MSG);
		return resultMap;
	}

	public static Map<String, Object> buildFailure(Object data) {
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("resultCode", SystemConstant.RESPONSE_FAIL_CODE);
		resultMap.put("resultMessage", SystemConstant.RESPONSE_FAIL_MSG);
		resultMap.put("data", data);
		return resultMap;
	}

	public static Map<String, Object> buildFailure(String message) {
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("resultCode", SystemConstant.RESPONSE_FAIL_CODE);
		resultMap.put("resultMessage", message);
		return resultMap;
	}

	/**
	 * build customized response without data
	 *
	 * @param respCode
	 * @param respMsg
	 * @return
	 */
	public static Map<String, Object> buildCustomizedResponse(int respCode, String respMsg) {
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("resultCode", respCode);
		resultMap.put("resultMessage", respMsg);
		return resultMap;
	}

	/**
	 * build customized response with data
	 *
	 * @param respCode
	 * @param respMsg
	 * @param data
	 * @return
	 */
	public static Map<String, Object> buildCustomizedResponse(int respCode, String respMsg, Object data) {
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("resultCode", respCode);
		resultMap.put("resultMessage", respMsg);
		resultMap.put("data", data);
		return resultMap;
	}

	/**
	 *返回任意参数名和对应index的数据Map
	 * @param keys
	 * @param datas
	 * @return
	 */
	public static Map<String, Object> buildCustomizedResponse(String[] keys, Object[] datas) {
		Map<String, Object> resultMap = new HashMap<>();
		for (int i = 0; i < keys.length; i++) {
			if (i < datas.length) {
				resultMap.put(keys[i], datas[i]);
			} else {
				resultMap.put(keys[i], null);
			}
		}
		return resultMap;
	}
}