package com.web.oa.service;

import java.util.List;

import com.web.oa.pojo.MenuTree;
import com.web.oa.pojo.SysPermission;
import com.web.oa.pojo.SysRole;
import com.web.oa.pojo.SysUserRole;




public interface SysService {
	
//	根据用户账号查询用户信息
//	public Employee findSysUserByUserCode(String userCode)throws Exception;
	
	//根据用户id查询权限范围的菜单
	public List<SysPermission> findMenuListByUserId(String userid) throws Exception;
	
	//根据用户id查询权限范围的url
	public List<SysPermission> findPermissionListByUserId(String userid) throws Exception;
	
	public List<MenuTree> loadMenuTree();
	
	public List<SysRole> findAllRoles();
	
	public SysRole findRolesAndPermissionsByUserId(String userId);
	
	public void addRoleAndPermissions(SysRole role,int[] permissionIds);
	
	//查询所有menu类permission
	public List<SysPermission> findAllMenus();
	
	public void addSysPermission(SysPermission permission);
	
	//根据用户ID查询其所有的菜单和权限
	public List<SysPermission> findMenuAndPermissionByUserId(String userId);
	public List<MenuTree> getAllMenuAndPermision();
	
	//根据角色ID查询权限
	public List<SysPermission> findPermissionsByRoleId(String roleId);
	
	public void updateRoleAndPermissions(String roleId,int[] permissionIds);

	public List<SysRole> findRolesAndPermissions();
	//用户添加
	public int saveSysUserRole(SysUserRole sysRole);
	//用户删除
	public int deleteById(String id);
}
