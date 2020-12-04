package com.shop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shop.mapper.LeavebillMapper;
import com.shop.pojo.Leavebill;


@Service
public class LeavebillServiceImpl implements LeavebillService {

	
	@Autowired
	private LeavebillMapper leaveBillMapper;
	@Override
	public void insert(Leavebill leaveBill) {
		
		this.leaveBillMapper.insert(leaveBill);
	}

}
