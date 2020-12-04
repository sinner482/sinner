package com.shop.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.shop.pojo.Baoxiaobill;
import com.shop.pojo.BaoxiaobillExample;
import com.shop.service.BaoxiaobillService;
import com.shop.utils.Constants;

@Controller
public class BaoXiaoBillcontrolle {
	
	@Autowired
	private BaoxiaobillService baoxiaobillService;
	
	@RequestMapping(value="myBaoxiaoBill")
	public ModelAndView taskList(HttpSession session) {
		//创建一个ModelAndView对象
		ModelAndView ma = new ModelAndView();
		
		//通过session获取监听对象
		Baoxiaobill baoxiaoBill = (Baoxiaobill) session.getAttribute(Constants.GLOBLE_USER_SESSION);
		 
		Baoxiaobill findTaskListByState = this.baoxiaobillService.findTaskListByState(baoxiaoBill.getState());
		
		return null;
	}
	
}
