package com.janita.sso.server.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 公共的验证接口
 */
@Controller
@RequestMapping("sso")
public class ValidateController {
	
	@RequestMapping("verify")
	@ResponseBody
	public JSONObject verify(HttpServletRequest request, @RequestParam String token) {
		HttpSession session = request.getSession();
		JSONObject result = new JSONObject();
		if(session.getAttribute("token") != null && token.equals(session.getAttribute("token"))) {
			result.put("code", "success");
			result.put("msg", "认证成功");
		} else {
			result.put("code", "failure");
			result.put("msg", "token已失效，请重新登录！");
		}
		return result;
	}
	
}
