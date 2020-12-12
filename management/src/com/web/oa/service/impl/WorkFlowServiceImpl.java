package com.web.oa.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Constant;
import com.github.pagehelper.util.StringUtil;
import com.web.oa.mapper.BaoxiaoBillMapper;
import com.web.oa.pojo.BaoxiaoBill;
import com.web.oa.service.WorkFlowService;
import com.web.oa.utils.Constants;

@Service
public class WorkFlowServiceImpl implements WorkFlowService {
	
	@Autowired
	private TaskService taskService;
	
	@Autowired
	private RuntimeService runtimeService;
	
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private HistoryService historyService;
	
	@Autowired
	private BaoxiaoBillMapper  baoxiaoBillMapper;
	
	//查询待办事务所有信息
	@Override
	public List<Task> findByListTask(String name) {
		List<Task> list = taskService.createTaskQuery()
		.taskAssignee(name)
		.orderByTaskCreateTime()
		
		.desc()
		
		.list();
		
		return list;
	}
	
	
	//报销申请提交
	@Override
	public void saveStartProcess(Long baoxiaoId, String username) {
		//使用当前对象获取到流程定义的key（对象的名称就是流程定义的key）
		String key = Constants.BAOXIAO_KEY;
		System.out.println("==============================="+key);
		//创建Map集合用来传递数据
		Map<String,Object> map = new HashMap<String, Object>();
		
		//inputUser是流程图中定义的数据
		map.put("inputUser", username);
		
		//拼接数据
		String a = key+'.'+baoxiaoId;
		
		map.put("a", a);
		
		runtimeService.startProcessInstanceByKey(key, a, map);
		
	}

	//添加流程
	@Override
	public void saveNewDeploye(InputStream in, String finename) {
	try {
				//2：将File类型的文件转化成ZipInputStream流
		ZipInputStream zipInputStream = new ZipInputStream(in);
		repositoryService.createDeployment()//创建流程
		.name(finename)//添加流程名称
		.addZipInputStream(zipInputStream)
		.deploy();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//查看流程
	@Override
	public List<Deployment> findDeploymentList() {
		//创建部署对象查询
		List<Deployment> list = repositoryService
		.createDeploymentQuery()
		.orderByDeploymenTime()
		.asc()
		.list();
		return list;
	}
	@Override
	public List<ProcessDefinition> findProcessDefinitionList() {
		//创建流程定义
		List<ProcessDefinition> list = repositoryService
		.createProcessDefinitionQuery()
		.orderByProcessDefinitionVersion()
		.asc()
		.list();
		
		return list;
	}

	//查看审核信息
	@Override
	public List<Comment> findCommentByBaoxiaoBillId(long id) {
		System.out.println("---------------------------"+id);
		//拼接
		String bussiness_key = Constants.BAOXIAO_KEY +"."+id;
		
		System.out.println("====================="+bussiness_key);
		
		HistoricProcessInstance pi = this.historyService
				.createHistoricProcessInstanceQuery()
				.processInstanceBusinessKey(bussiness_key)
				.singleResult();
		
		System.out.println("---------------------------------------"+pi);
		
		List<Comment> list = this.taskService.getProcessInstanceComments(pi.getId());
		System.out.println("======================="+list);
		if(list!=null&&list.size()>0) {
			return list;
		}
		return null;
	}

	//办理任务
	@Override
	public BaoxiaoBill findBaoxiaoBillByTaskId(String taskId) {
		//使用任务Id
		Task task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
		ProcessInstance pi = this.runtimeService.createProcessInstanceQuery()
										.processInstanceId(task.getProcessInstanceId())
										.singleResult();
		String bussiness_key = pi.getBusinessKey();
		System.out.println(bussiness_key);
		String id = "";
		if (StringUtils.isNotBlank(bussiness_key)) {
			id = bussiness_key.split("\\.")[1];
		}
		
		BaoxiaoBill bill = baoxiaoBillMapper.selectByPrimaryKey(Long.parseLong(id));
	
		return bill;
	}
	@Override
	public List<Comment> findCommentByTaskId(String taskId) {
		//使用任务Id
		Task task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
		String processId = task.getProcessInstanceId();
		
		List<Comment> list = this.taskService.getProcessInstanceComments(processId);
		return list;
	}
	@Override
	public List<String> findOutComeListByTaskId(String taskId) {
		//创建一个连线名称的集合用来返回存放信息
		List<String> list = new ArrayList<String>();
		//使用任务ID,查询对象
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		//获取流程定义ID
		String processDefinitionId = task.getProcessDefinitionId();		
		//查询ProcessDefinitionEntiy对象
		ProcessDefinitionEntity processDefinitionEntiy = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefinitionId);
		//使用任务对象Task获取流程实例ID
		String processInstanceId = task.getProcessInstanceId();
		//使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()
		.processInstanceId(processInstanceId)
		.singleResult();
		//获取当前活动的id
		String id = pi.getActivityId();
		//获取当前的活动
		ActivityImpl findActivity = processDefinitionEntiy.findActivity(id);
		//获取当前活动完成之后连线的名称
		List<PvmTransition> transitions = findActivity.getOutgoingTransitions();
		if(transitions!=null&&transitions.size()>0) {
			for (PvmTransition pvmTransition : transitions) {
				String name = (String) pvmTransition.getProperty("name");
				if(StringUtils.isNoneBlank(name)) {
					list.add(name);
				}else {
					list.add("默认提交");
				}
			}
		}
		return list;
	}

	//默认提交
	@Override
	public void saveSubmitTask(long id, String taskId, String comemnt, String outcome, String username) {
		//使用任务Id，查询任务对象，获取流程实例Id
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		//获取流程实例Id
		String processInstanceId = task.getProcessInstanceId();
		//加当前任务的审核人
		Authentication.setAuthenticatedUserId(username);
		//添加批注
		taskService.addComment(taskId, processInstanceId, comemnt);
		/**
		 * 2：如果连线的名称是“默认提交”，那么就不需要设置，如果不是，就需要设置流程变量
		 * 在完成任务之前，设置流程变量，按照连线的名称，去完成任务
				 流程变量的名称：outcome
				 流程变量的值：连线的名称
		 */
		//创建一个Map集合用来存放返回的连线
		Map<String,Object> map = new HashMap<String, Object>();
		if(outcome!=null&& !outcome.equals("默认提交")) {
			map.put("message", outcome);
			taskService.complete(taskId, map);
		}else {
			taskService.complete(taskId);
		}
		/**
		 * 5：在完成任务之后，判断流程是否结束
   			如果流程结束了，更新请假单表的状态从1变成2（审核中-->审核完成）
		 */
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()
		.processInstanceId(processInstanceId)
		.singleResult();
		
		//流程结束了
		if(pi==null) {
			//更新请假单表的状态从1变成2
			BaoxiaoBill baoxiaoBill = baoxiaoBillMapper.selectByPrimaryKey(id);
			baoxiaoBill.setState(2);
			baoxiaoBillMapper.updateByPrimaryKey(baoxiaoBill);
		}
	}

	//强制删除
	@Override
	public void deleteProcessDefinitionByDeploymentId(String deploymentId) {
		this.repositoryService.deleteDeployment(deploymentId, true);
		
	}

	@Override
	public ProcessDefinition findProcessDefinitionByTaskId(String taskId) {
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		System.out.println("=================================="+task);
		//获取流程定义ID
		String processDefinitionId = task.getProcessDefinitionId();
		//查询流程定义的对象
		ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()//创建流程定义查询对象，对应表act_re_procdef 
					.processDefinitionId(processDefinitionId)//使用流程定义ID查询
					.singleResult();
		return pd;
	}
	
	//查看流程图
	@Override
	public Map<String, Object> findCoordingByTask(String taskId) {
		//创建一个Map集合用来存放坐标
		Map<String,Object> map = new HashMap<String, Object>();
		
		//使用任务Id
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		
		//获取流程定义的Id
		String id = task.getProcessDefinitionId();
		
		//获取流程定义的实体对象（对应.bpmn文件中的数据;
		ProcessDefinitionEntity entity = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(id);
		
		//流程实例Id
		String parentTaskId = task.getProcessInstanceId();
		
		//使用流程实例ID，查询正在执行的执行对象表，获取当前活动对应的流程实例对象
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()
		.processInstanceId(parentTaskId)
		.singleResult();
		
		//获取当前活动的Id
		String activityId = pi.getActivityId();
		
		//获取当前活动对象
		ActivityImpl findActivity = entity.findActivity(activityId);
		
		//获取坐标x,y,宽,高
		map.put("x", findActivity.getX());
		map.put("y", findActivity.getY());
		map.put("width", findActivity.getWidth());
		map.put("height", findActivity.getHeight());
		
		return map;
	}
	@Override
	public Task findTaskByBussinessKey(String bUSSINESS_KEY) {
		Task task = taskService.createTaskQuery().processInstanceBusinessKey(bUSSINESS_KEY).singleResult();
		return task;
	}

	//获取流程图照片
	@Override
	public InputStream findImageInputStream(String deploymentId,String imageName) {
		
		return repositoryService.getResourceAsStream(deploymentId,imageName);
	}


	
	
	
}
