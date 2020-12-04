package com.shop.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.shop.pojo.Employee;
import com.shop.pojo.Leavebill;
import com.shop.service.LeavebillService;
import com.shop.service.WorkFlowService;
import com.shop.utils.Constants;

@Controller
public class WorkFlowController {

	@Autowired
	private WorkFlowService workFlowService;

	@Autowired
	private LeavebillService leaveBillService;

	// 部署流程
	@RequestMapping(value = "/deployProcess")
	public String deployProcess(MultipartFile fileName, String processName) {

		try {
			this.workFlowService.saveNewDeploy(fileName.getInputStream(), processName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "add_process";
	}
	
	// 请假申请
	@RequestMapping(value="/saveStartLeave")
	public String insert(Leavebill leaveBill,HttpSession session)
	{
		//手动封装一些数据集字段
		
		//封装的时间
		leaveBill.setLeavedate(new Date());
		
		// 封装的状态
		leaveBill.setState(1); 
		
		//获取session对象
	   Employee employee = 	(Employee) session.getAttribute(Constants.GLOBLE_USER_SESSION);
	   
	   	//封装的id
	   leaveBill.setUserId(employee.getId());
	   
	   
	   this.leaveBillService.insert(leaveBill);
	   
	   
//	   System.out.println("leaveBill.getId():"+leaveBill.getId());
	   
	   
	   
	   // 启动流程
	   this.workFlowService.saveStartLeave(leaveBill.getId(),employee.getName());
	   
	   return "redirect:/taskList";
	   
		
	}
	//待办事务
	@RequestMapping(value="/taskList")
	public ModelAndView taskList(HttpSession session)
	{
		
	ModelAndView mv = new ModelAndView();
		
	  Employee employee = (Employee) session.getAttribute(Constants.GLOBLE_USER_SESSION);
	  
	  List<Task> taskList  = this.workFlowService.findTaskListByName(employee.getName());  
	  
	  mv.addObject("taskList", taskList);
	  
	  mv.setViewName("workflow_task");
	  return mv;
	  
	  
	}
	//获取到请假批注
	@RequestMapping(value="/viewTaskForm")
	public ModelAndView viewTaskForm(String taskId)
	{
		//创建ModelAndView对象
		ModelAndView mv = new ModelAndView();
		
		//调用service层的方法
		Leavebill leavebill = this.workFlowService.findLeaveBillByTaskId(taskId);
		
		
	   List<Comment> commentList = this.workFlowService.findCommentListByTaskId(taskId);
		
		mv.addObject("bill", leavebill);
		mv.addObject("commentList", commentList);
		mv.addObject("taskId", taskId);
		mv.setViewName("approve_leave");
		return mv;
	}
	//提交批注信息   并跳转到待办事务
	@RequestMapping(value="/submitTask")
	public String submitTask(String id,String taskId,String comment,HttpSession session)
	{
		
	   Employee em = (Employee) session.getAttribute(Constants.GLOBLE_USER_SESSION); 	
	   
	   String username = em.getName();
	   
	   // 添加批注
	   this.workFlowService.submitTask(id,taskId,comment,username);
	   
	   return "redirect:/taskList";
	   
	   
	}
	
	
	/**
	 * 查看当前流程图（查看当前活动节点，并使用红色的框标注）
	 */
	@RequestMapping("/viewCurrentImage")
	public String viewCurrentImage(String taskId,ModelMap model){
		/**一：查看流程图*/
		//1：获取任务ID，获取任务对象，使用任务对象获取流程定义ID，查询流程定义对象
		ProcessDefinition pd = workFlowService.findProcessDefinitionByTaskId(taskId);

		model.addAttribute("deploymentId", pd.getDeploymentId());
		model.addAttribute("imageName", pd.getDiagramResourceName());
		/**二：查看当前活动，获取当期活动对应的坐标x,y,width,height，将4个值存放到Map<String,Object>中*/
		Map<String, Object> map = workFlowService.findCoordingByTask(taskId);

		model.addAttribute("acs", map);
		return "viewimage";
	}
	
	/**
	 * 查看流程图
	 * @throws Exception 
	 */
	@RequestMapping("/viewImage")
	public String viewImage(String deploymentId,String imageName,HttpServletResponse response) throws Exception{

		//2：获取资源文件表（act_ge_bytearray）中资源图片输入流InputStream
		InputStream in = workFlowService.findImageInputStream(deploymentId,imageName);
		//3：从response对象获取输出流
		OutputStream out = response.getOutputStream();
		//4：将输入流中的数据读取出来，写到输出流中
		for(int b=-1;(b=in.read())!=-1;){
			out.write(b);
		}
		out.close();
		in.close();
		return null;
	}
	
}
