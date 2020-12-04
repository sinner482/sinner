package com.shop.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.shop.pojo.Employee;
import com.shop.service.EmployeeService;

public class CustomerTaskAssignee implements TaskListener {
	@Override
	public void notify(DelegateTask delegateTask) {

		
		// 调用业务类查询出当前待办人的上级 
		//在非spring中拿到spring容器
	WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
		

	//获取到employeeService
	EmployeeService employeeService = (EmployeeService) webApplicationContext.getBean("employeeService");
	
	//在非spring中环境获取到session
	//获取到request
	HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	
	//获取当前对象(从session中取出)
    Employee employee = (Employee) request.getSession().getAttribute(Constants.GLOBLE_USER_SESSION);
    
    //从当前session中 获取到登录的对象中的manageId
	long  manageId = employee.getManagerId();
	// 调用业务类的方法
	Employee manager = employeeService.findEmployeeManagerByManagerId(manageId);
	
	
	//从查询到当前的待办人信息  返回给上一级
	delegateTask.setAssignee(manager.getName());
	
	
	
	
	
		
		
		
		
		
		
		
		
		
		
		
	}

}
