package com.web.oa.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.web.oa.pojo.ActiveUser;
import com.web.oa.pojo.Employee;
import com.web.oa.pojo.EmployeeCustom;
import com.web.oa.pojo.MenuTree;
import com.web.oa.pojo.SysPermission;
import com.web.oa.pojo.SysRole;
import com.web.oa.pojo.SysUserRole;
import com.web.oa.service.EmployeeService;
import com.web.oa.service.SysService;
@Controller
public class UserController {

	@Autowired
	private EmployeeService employeeService;
	@Autowired
	private SysService sysService;
	//验证登录
	@RequestMapping("/login")
	public String login(HttpServletRequest request,Model model){
		
		String exceptionName = (String) request.getAttribute("shiroLoginFailure");
		
		if (exceptionName != null) {
			if (UnknownAccountException.class.getName().equals(exceptionName)) {
				model.addAttribute("errorMsg", "用户账号不存在");
			} else if (IncorrectCredentialsException.class.getName().equals(exceptionName)) {
				model.addAttribute("errorMsg", "密码不正确");
			} else if("randomcodeError".equals(exceptionName)) {
				model.addAttribute("errorMsg", "验证码不正确");
			}
			else {
				model.addAttribute("errorMsg", "未知错误");
			}
		}
		return "login";
	}
	
	/*
	 * SELECT e1.*,e2.name FROM employee e1 INNER JOIN employee e2 
			WHERE e1.manager_id=e2.id;
	 * */
	//显示所有数据
	@RequestMapping("/findUserList")
	public ModelAndView findUserList(String userId) {
		ModelAndView mv = new ModelAndView();
		List<SysRole> allRoles = sysService.findAllRoles();
		List<EmployeeCustom> list = employeeService.findUserAndRoleList();
		
		mv.addObject("userList", list);
		mv.addObject("allRoles", allRoles);
		
		mv.setViewName("userlist");
		return mv;
	}
	
	//权限设置
	//重新分配待办人
	@RequestMapping("/assignRole")
	@ResponseBody
	public Map<String, String> assignRole(String roleId,String userId) {
		Map<String, String> map = new HashMap<>(); 
		try {
			employeeService.updateEmployeeRole(roleId, userId);
			map.put("msg", "分配权限成功");
		} catch (Exception e) {
			e.printStackTrace();
			map.put("msg", "分配权限失败");
		}
		return map;
	}
	//角色添加
	@RequestMapping("/toAddRole")
	public ModelAndView toAddRole() {
		List<MenuTree> allPermissions = sysService.loadMenuTree();
		List<SysPermission> menus = sysService.findAllMenus();
		List<SysRole> permissionList = sysService.findRolesAndPermissions();
		
		ModelAndView mv = new ModelAndView();
		mv.addObject("allPermissions", allPermissions);
		mv.addObject("menuTypes", menus);
		mv.addObject("roleAndPermissionsList", permissionList);
		mv.setViewName("rolelist");
		
		return mv;
		
	}
	//保存用户权限
	@RequestMapping("/saveRoleAndPermissions")
	public String saveRoleAndPermissions(SysRole role,int[] permissionIds) {
		//设置role主键，使用uuid
		String uuid = UUID.randomUUID().toString();
		role.setId(uuid);
		//默认可用
		role.setAvailable("1");
		
		sysService.addRoleAndPermissions(role, permissionIds);
		
		return "redirect:/toAddRole";
	}
	//新建权限
	@RequestMapping("/saveSubmitPermission")
	public String saveSubmitPermission(SysPermission permission) {
		if (permission.getAvailable() == null) {
			permission.setAvailable("0");
		}
		sysService.addSysPermission(permission);
		return "redirect:/toAddRole";
	}
	//角色列表
	@RequestMapping("/findRoles")  
	public ModelAndView findRoles() {
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		List<SysRole> roles = sysService.findAllRoles();
		List<MenuTree> allMenuAndPermissions = sysService.getAllMenuAndPermision();
		
		ModelAndView mv = new ModelAndView();
		mv.addObject("allRoles", roles);
		mv.addObject("activeUser",activeUser);
		mv.addObject("allMenuAndPermissions", allMenuAndPermissions);
		
		mv.setViewName("permissionlist");
		return mv;
	}
	
	@RequestMapping("/loadMyPermissions")
	@ResponseBody
	public List<SysPermission> loadMyPermissions(String roleId) {
		List<SysPermission> list = sysService.findPermissionsByRoleId(roleId);
		
		for (SysPermission sysPermission : list) {
			System.out.println(sysPermission.getId()+","+sysPermission.getType()+"\n"+sysPermission.getName() + "," + sysPermission.getUrl()+","+sysPermission.getPercode());
		}
		return list;
	}
	
	@RequestMapping("/updateRoleAndPermission")
	public String updateRoleAndPermission(String roleId,int[] permissionIds) {
		sysService.updateRoleAndPermissions(roleId, permissionIds);
		return "redirect:/findRoles";		
	}
	
	//查看当前员工的角色和权限列表
	@RequestMapping("/viewPermissionByUser")
	@ResponseBody
	public SysRole viewPermissionByUser(String userName) {
		SysRole sysRole = sysService.findRolesAndPermissionsByUserId(userName);

		System.out.println(sysRole.getName()+"," +sysRole.getPermissionList());
		return sysRole;
	}
	//添加信息
	@RequestMapping("/saveUser")
	public String saveUser(Employee user) {
		Md5Hash md5 = new Md5Hash(user.getPassword(), "eteokues", 2);
		String password = md5.toString();
		user.setPassword(password);
		user.setSalt("eteokues");
		int saveUserEmployee = employeeService.saveUserEmployee(user);
		
		SysUserRole sysRole = new SysUserRole();
		sysRole.setSysUserId(user.getName());
		sysRole.setSysRoleId(user.getRole().toString());
		int saveSysRole = sysService.saveSysUserRole(sysRole);
		return "redirect:/findUserList";	
	}
	
	//根据员工级别查找下一级别主管
	@RequestMapping("/findNextManager")
	@ResponseBody
	public List<Employee> findNextManager(int level) {
		level++; //加一，表示下一个级别
		List<Employee> list = employeeService.findEmployeeByLevel(level);
		System.out.println(list);
		return list;
		
	}
	//删除
	@RequestMapping("/deleteById")
	public String deleteById(String role) {
		int deleteById = sysService.deleteById(role);
		System.out.println("======="+deleteById);
		return "redirect:/findRoles";
	}
	
 //	@RequestMapping("/login")
//	public String login(String username,String password,HttpSession session,Model model){
//
//		//2：使用用户名作为查询条件，查询员工表，获取当前用户名对应的信息
//		Employee emp = employeeService.findEmployeeByName(username);
//		if (emp != null) {
//			if (emp.getPassword().equals(password)) {
//				//3：将查询的对象（惟一）放置到Session中
//				session.setAttribute(Constants.GLOBLE_USER_SESSION, emp);
//				return "index";
//			} else {
//				model.addAttribute("errorMsg", "帐号或密码错误");
//				return "login";
//			}
//		} else {
//			model.addAttribute("errorMsg", "帐号或密码错误");
//			return "login";
//		}
//    Subject subject = SecurityUtils.getSubject();
//    UsernamePasswordToken token = new UsernamePasswordToken(username, password);
//    try {
//		subject.login(token);
//	} catch(UnknownAccountException e1){
//		model.addAttribute("errorMsg", "用户账号不存在");
//	} catch(IncorrectCredentialsException e2) {
//		model.addAttribute("errorMsg", "密码不正确");
//	} catch (Exception e) {
//    	model.addAttribute("errorMsg", "未知错误");
//	}
//	}

//	@RequestMapping("/logout")
//	public String logout(HttpSession session){
//		//清空Session
//		session.invalidate();
//		return "redirect:login.jsp";
//	}
	
}
