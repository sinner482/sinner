package com.web.oa.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.coyote.InputBuffer;

import com.web.oa.pojo.BaoxiaoBill;

public interface WorkFlowService {
	//查询所有待办事务信息
	public List<Task> findByListTask(String name);
	
	//报销申请提交
	public void saveStartProcess(Long baoxiaoId,String username);
	
	//添加流程
	public void saveNewDeploye(InputStream in,String finename);
	
	//查看流程
	List<Deployment> findDeploymentList();
	
	List<ProcessDefinition> findProcessDefinitionList();
	
	//查看审核信息
	List<Comment> findCommentByBaoxiaoBillId(long id);
	
	//办理任务
	public BaoxiaoBill findBaoxiaoBillByTaskId(String taskId);
	
	public List<Comment> findCommentByTaskId(String taskId);
	
	public List<String> findOutComeListByTaskId(String taskId);
	
	//提交任务
	public void saveSubmitTask(long id,String taskId,String comemnt,String outcome,String username);
	
	//删除
	public void deleteProcessDefinitionByDeploymentId(String deploymentId);

	//查看流程图
	ProcessDefinition findProcessDefinitionByTaskId(String taskId);
	
	Map<String, Object> findCoordingByTask(String taskId);
	
	Task findTaskByBussinessKey(String bUSSINESS_KEY);
	//得到流程图的照片
	InputStream findImageInputStream(String deploymentId,String imageName);
}
