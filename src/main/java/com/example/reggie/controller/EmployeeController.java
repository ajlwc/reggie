package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.entity.Employee;
import com.example.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request,@RequestBody Employee employee){
        //1.对请求参数进行MD5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2.查看数据库是否有相同的用户名
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee one = employeeService.getOne(lambdaQueryWrapper);
        if (one == null){
            return R.error("登录失败");
        }
        //3.密码是否正确
        if (!one.getPassword().equals(password)){
            return R.error("登录失败");
        }
        //4.用户是否被锁定
        if (one.getStatus() != 1){
            return R.error("用户被锁定");
        }
        //5.将用户信息保存到session中
        request.getSession().setAttribute("employee",one.getId());
        return R.success(one);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 添加员工
     */
    @PostMapping
    public R<String> add(HttpServletRequest request, @RequestBody Employee employee){
        //1.对密码进行MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //2.设置创建时间和更新时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        //3.设置创建者和更新者
        Long emp = (Long) request.getSession().getAttribute("employee");
        employee.setCreateUser(emp);
        employee.setUpdateUser(emp);
        employeeService.save(employee);
        return R.success("添加成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        //降序排列
        lambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);
        employeeService.page(pageInfo,lambdaQueryWrapper);
        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee){
        employee.setUpdateTime(LocalDateTime.now());
        Long empId = (Long)request.getSession().getAttribute("employee");
        employee.setUpdateUser(empId);
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    /**
     *根据id查询员工信息
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息...");
        Employee emp = employeeService.getById(id);
        if (emp != null){
            return R.success(emp);
        }
        return R.error("没有查询带对应员工信息");
    }
}
