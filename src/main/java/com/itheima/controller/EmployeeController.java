package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.entity.Employee;
import com.itheima.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录
     * @param employee
     * @param request
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(@RequestBody Employee employee, HttpServletRequest request){
        log.info("用户登录账号{}",employee.getUsername());

        //1、处理用户提交的密码
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据用户名查询数据
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee employee1 = employeeService.getOne(queryWrapper);

        //3、 是否有结果
         if(employee1==null){
             return R.error("用户不存在");
         }
        //4、密码是否一致
        if(!employee1.getPassword().equals(password) ){
            return R.error("密码错误");
        }
        //5、员工状态是否启用
        if(employee1.getStatus() == 0){
            return R.error("账号禁用中……");
        }
        //6、登陆成功将用户id存入session，拦截器用
        request.getSession().setAttribute("employee",employee1.getId());
        return R.success(employee1);
    }

    /**
     * 退出登录
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        log.info("退出账号");
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增用户
     * 问题：如果用户已经存在会抛出异常
     * 解决：配置全局异常处理器
     * @param employee
     * @param request
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Employee employee,HttpServletRequest request){
        log.info("保存的用户名：{}",employee.getUsername());
        //设置用户密码，并加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        //设置用户创建时间、更改时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        //设置创建人、更改人
//        employee.setCreateUser((Long) request.getSession().getAttribute("employee"));
//        employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));
        //保存
        employeeService.save(employee);
        return R.success("保存成功");
    }

    /**
     * 员工分页展示
     * @param page
     * @param pageSize
     * @param name
     * @return
     */

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("分页查询{}",name);
        //分页构造器
        Page<Employee> pageInfo = new Page<>(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 数据回显
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("修改的id{}",id);
        if(id==null){
            return R.error("id为空");
        }
        Employee employee = employeeService.getById(id);

        return R.success(employee);
    }


    @PutMapping
    public R<String> update(@RequestBody Employee employee,HttpServletRequest request){
        log.info("修改的id{}",employee.getId());
//        //设置用户创建时间、更改时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        //设置创建人、更改人
//        employee.setCreateUser((Long) request.getSession().getAttribute("employee"));
//        employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));
        //保存
        employeeService.updateById(employee);
        return R.success("修改成功");
    }
}
