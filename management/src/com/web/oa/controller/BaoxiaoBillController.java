package com.web.oa.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.github.pagehelper.PageInfo;
import com.web.oa.pojo.ActiveUser;
import com.web.oa.pojo.BaoxiaoBill;
import com.web.oa.service.BaoxiaoService;
import com.web.oa.service.WorkFlowService;
import com.web.oa.utils.Constants;

import oracle.net.aso.a;

@Controller
public class BaoxiaoBillController {

	@Autowired
	private BaoxiaoService  baoxiaoService;
	
	@Autowired
	private WorkFlowService workFlowService;
	//获取index界面
	@RequestMapping("/main")
	public String main(ModelMap model) {
		//通过shiro中的      SecurityUtils.getSubject() 来获取用户信息
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		
		model.addAttribute("activeUser",activeUser);
		return "index";
	}
	
	//显示报销单的列表信息
	@RequestMapping("/myBaoxiaoBill")
	public String baoxiao(ModelMap model,String pagenow) {
		if(pagenow == null) {
			pagenow = "1";
		}else {
			pagenow=pagenow;
		}
		
		//获取所有用户信息    
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		
		long id = activeUser.getId();
		//调用BaoxiaoService层的方法
		PageInfo<BaoxiaoBill> billStartPage = baoxiaoService.findListUser(Integer.parseInt(pagenow), 1, id);
		List<BaoxiaoBill> list = billStartPage.getList();
		model.addAttribute("baoxiaoList", list);
		model.addAttribute("totalpage",billStartPage.getPages());
		model.addAttribute("pagenow", pagenow);
		return "baoxiaobill";
	}
	
	//删除报销信息
	@RequestMapping("/leaveBillAction_delete" )
	public String shanchu(Long id) {
		baoxiaoService.deleteBaoxioaBillId(id);
		return "redirect:/myBaoxiaoBill";
		
	}
	
	//报销申请
	@RequestMapping("/saveStartBaoxiao")
	public String shenqing(BaoxiaoBill baoxiaoBill,HttpSession session) {
		//设置当前时间
		baoxiaoBill.setCreatdate(new Date());
		//获取数据
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		//设置申请人Id
		baoxiaoBill.setUserId(activeUser.getId());
		//更新状态从0变成1（初始录入-->审核中）
		baoxiaoBill.setState(1);
		
		baoxiaoService.saveBaoxiaoBill(baoxiaoBill);
		
		workFlowService.saveStartProcess(baoxiaoBill.getId(),activeUser.getUsername());

		
		return "redirect:/myTaskList";
		
	}
	
	//显示待办事务信息
	@RequestMapping("/myTaskList")
	public ModelAndView affair(HttpSession session) {
		
		//创建ModelAndView对象
		ModelAndView mv = new ModelAndView();
		
		//获取信息
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		
		//调用WorkFlowService中的方法
		List<Task> list = workFlowService.findByListTask(activeUser.getUsername());
		
		mv.addObject("taskList", list);
		mv.setViewName("workflow_task");
		
		return mv;
	}
	
	//查看审核记录
	@RequestMapping("/viewHisComment")
	public String shenhe(long id,ModelMap model) {
		//1：使用报销单ID，查询报销单对象
		BaoxiaoBill baoxiaoBill = baoxiaoService.findBaoxiaoBillById(id);
		if(baoxiaoBill == null) {
			model.addAttribute("errorMsg", "当前没有审核记录");
		}else {
			model.addAttribute("baoxiaoBill", baoxiaoBill);
		}
		
		//2：使用请假单ID，查询历史的批注信息
		List<Comment> list = workFlowService.findCommentByBaoxiaoBillId(id);
		if(list!=null&&list.size()>0) {
			model.addAttribute("commentList", list);
		}else {
			model.addAttribute("errorMsg", "当前没有历史批注信息");

		}
		
		
		return "workflow_commentlist";
		
	}
	
	//查看当前流程图
	@RequestMapping("/viewCurrentImageByBill")
	public String  viewCurrentImageByBill(long billId,ModelMap model) {
		//拼接数据
		String BUSSINESS_KEY = Constants.BAOXIAO_KEY + "." + billId;		
		//1：获取任务ID，获取任务对象，使用任务对象获取流程定义ID，查询流程定义对象
		//调用service层的方法
		Task task = this.workFlowService.findTaskByBussinessKey(BUSSINESS_KEY);
		//调用service层的方法
		ProcessDefinition pd = workFlowService.findProcessDefinitionByTaskId(task.getId());
		
		System.out.println("------------------>"+pd);
		model.addAttribute("deploymentId", pd.getDeploymentId());
		model.addAttribute("imageName", pd.getDiagramResourceName());
		
		/**二：查看当前活动，获取当期活动对应的坐标x,y,width,height，将4个值存放到Map<String,Object>中*/
		Map<String, Object> map = workFlowService.findCoordingByTask(task.getId());
		
		model.addAttribute("acs", map);
		
		return "viewimage";
	}
	
	//显示红框数据
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
	
	
}
