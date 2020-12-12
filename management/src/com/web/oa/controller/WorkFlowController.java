package com.web.oa.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.web.oa.pojo.ActiveUser;
import com.web.oa.pojo.BaoxiaoBill;
import com.web.oa.service.WorkFlowService;

@Controller
public class WorkFlowController {
	
	@Autowired
	private WorkFlowService workFlowService;
	
	//流程部署
	@RequestMapping("/deployProcess")
	public String deployProcess(String processName,MultipartFile fileName) {
		
		try {
			workFlowService.saveNewDeploye(fileName.getInputStream(),processName);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		//重定向
		return "redirect:/processDefinitionList";
	}
	
	//查看流程
	@RequestMapping("/processDefinitionList")
	public ModelAndView processDefinitionList() {
		//创建ModelAndView对象
		ModelAndView mv = new ModelAndView();
		//1:查询部署对象信息，对应表（act_re_deployment）
		List<Deployment> depList = workFlowService.findDeploymentList();
		//2:查询定义流程信息
		List<ProcessDefinition> pdList = workFlowService.findProcessDefinitionList();
		
		mv.addObject("depList", depList);
		mv.addObject("pdList", pdList);
		
		mv.setViewName("workflow_list");
		return mv;
	}
	
	//办理任务
	@RequestMapping("/viewTaskForm")
	public ModelAndView viewTaskForm(String taskId) {
		//创建一个ModelAndView对象
		ModelAndView mv = new ModelAndView();
		
		BaoxiaoBill baoxiaoBill = this.workFlowService.findBaoxiaoBillByTaskId(taskId);
		
		List<Comment> Comment = this.workFlowService.findCommentByTaskId(taskId);
		
		List<String> outComeListTaskId = this.workFlowService.findOutComeListByTaskId(taskId);
		
		mv.addObject("baoxiaoBill",baoxiaoBill);
		mv.addObject("commentList",Comment);
		mv.addObject("outcomeList",outComeListTaskId);
		mv.addObject("taskId",taskId);
		
		mv.setViewName("approve_baoxiao");
		return mv;
		
	}
	
	//提交任务
	@RequestMapping("/submitTask")
	public String submitTask(long id,String taskId,String comment,String outcome,HttpSession session) {

		//通过session获取到监听里面的GLOBLE_USER_SESSION
//		String username = ((Employee) session.getAttribute(Constants.GLOBLE_USER_SESSION)).getName();
		//获取用户信息
		ActiveUser activiti = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		String username2 = activiti.getUsername();//代办人名字
		this.workFlowService.saveSubmitTask(id, taskId, comment, outcome, username2);
		//重定向
		return "redirect:/myTaskList";
	}
	
	//删除
	@RequestMapping("/delDeployment")
	public String delDeployment (String deploymentId) {
		//使用部署流程定义的Id
		workFlowService.deleteProcessDefinitionByDeploymentId(deploymentId);
		return "redirect:/processDefinitionList";
		
	}
	/**
	 * 查看流程图
	 * @throws Exception 
	 */
	//获取图片
	@RequestMapping(value = "/viewImage")
	public String viewImage(String deploymentId,String imageName,HttpServletResponse response) throws Exception {
	//2：获取资源文件表（act_ge_bytearray）中资源图片输入流InputStream
	//调用Service层的方法
		InputStream in= workFlowService.findImageInputStream(deploymentId, imageName);
		
		//3：从response对象获取输出流
		OutputStream out = response.getOutputStream();
		//4：将输入流中的数据读取出来，写到输出流中
		
		for(int b=-1;(b=in.read())!=-1;) {
			out.write(b);
		}
		out.close();
		in.close();
		
		return null;
		
	}
}
