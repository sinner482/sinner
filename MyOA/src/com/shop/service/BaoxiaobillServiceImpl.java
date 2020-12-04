package com.shop.service;

import java.util.List;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shop.mapper.BaoxiaobillMapper;
import com.shop.pojo.Baoxiaobill;


@Service("baoxiaobillService")
public class BaoxiaobillServiceImpl implements BaoxiaobillService {
	
	@Autowired
	private BaoxiaobillMapper  baoxiaobillMapper;
	
	@Override
	public Baoxiaobill findTaskListByState(long state) {
		
		return this.baoxiaobillMapper.selectByPrimaryKey(state);
	}
	

	
	
	
	
}
