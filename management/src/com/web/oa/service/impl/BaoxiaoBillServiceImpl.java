package com.web.oa.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.web.oa.mapper.BaoxiaoBillMapper;
import com.web.oa.pojo.BaoxiaoBill;
import com.web.oa.pojo.BaoxiaoBillExample;
import com.web.oa.pojo.BaoxiaoBillExample.Criteria;
import com.web.oa.service.BaoxiaoService;

@Service
public class BaoxiaoBillServiceImpl implements BaoxiaoService {

	
	@Autowired
	private BaoxiaoBillMapper baoxiaoBillMapper;
	
	//显示报销列表信息
	
	@Override
	public List<BaoxiaoBill> findBaoxiaoBillByListUser(Long userid) {
		
       //创建一个BaoxiaoBillExample
		BaoxiaoBillExample example = new BaoxiaoBillExample();
		//得到createCriteria方法
		Criteria criteria = example.createCriteria();
		//得到userid
		criteria.andUserIdEqualTo(userid);
		
		return   baoxiaoBillMapper.selectByExample(example) ;
	}
	
	//删除
	@Override
	public void deleteBaoxioaBillId(Long id) {
		baoxiaoBillMapper.deleteByPrimaryKey(id);
	}
	
	//报销申请
	@Override
	public void saveBaoxiaoBill(BaoxiaoBill baoxiaoBill) {
		//获取请假单ID
		Long id = baoxiaoBill.getId();
		//新增保存
		if(id==null) {
			//新增
			baoxiaoBillMapper.insert(baoxiaoBill);
		}
		//更新保存
		else {
			//保存
			baoxiaoBillMapper.updateByPrimaryKey(baoxiaoBill);
		}
	}
	
	//查看审核记录
	@Override
	public BaoxiaoBill findBaoxiaoBillById(Long id) {
		BaoxiaoBill baoxiaoBill = baoxiaoBillMapper.selectByPrimaryKey(id);
		return baoxiaoBill;
	}
//分页
	@Override
	public PageInfo<BaoxiaoBill> findListUser(Integer pagenow, Integer pagesize, Long id) {
		PageInfo<BaoxiaoBill> ps = null;
		
		Page<BaoxiaoBill> startPage = PageHelper.startPage(pagenow, pagesize);
		
		//创建BaoxiaoBillExample对象
		BaoxiaoBillExample example = new BaoxiaoBillExample();
		
		Criteria criteria = example.createCriteria();
		
		criteria.andUserIdEqualTo(id);
		
		List<BaoxiaoBill> selectByExample = baoxiaoBillMapper.selectByExample(example);
		ps = new 	PageInfo<BaoxiaoBill>(selectByExample);
		return ps; 
	}
	
	
	

//	@Override
//	public List<BaoxiaoBill> findLeaveBillListByUser(long id) {
//		//创建BaoxiaoBillExample对象
//		BaoxiaoBillExample example = new BaoxiaoBillExample();
//		
//		Criteria criteria = example.createCriteria();
//		
//		criteria.andUserIdEqualTo(id);
//		
//		return baoxiaoBillMapper.selectByExample(example);
//	}

}
