package com.web.oa.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import com.web.oa.pojo.BaoxiaoBill;

public interface BaoxiaoService {
	
	//查询所有
	
	public List<BaoxiaoBill> findBaoxiaoBillByListUser(Long userid);
	
	//删除
	public void deleteBaoxioaBillId(Long id);
	
	//添加
	public void saveBaoxiaoBill(BaoxiaoBill baoxiaoBill);
	
	//查看审核记录
	public BaoxiaoBill findBaoxiaoBillById(Long id);
	
	//分页
	public PageInfo<BaoxiaoBill> findListUser(Integer pagenow,Integer pagesize,Long id);
	
}
