package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import io.swagger.annotations.ApiOperation;
import lombok.var;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PutMapping;

import java.time.LocalDateTime;
import java.util.List;

//Spring的注解，表示这是一个服务层的组件。Spring会自动将其作为服务类进行管理
@Service
public class EmployeeServiceImpl implements EmployeeService {

    //Spring的依赖注入注解，用于将 EmployeeMapper 实例自动注入到这个服务类中
    @Autowired
    //EmployeeMapper 是数据访问层（DAO），负责查询数据库中的员工信息
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端传过来的明文进行md5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     *
     * @param employeeDTO
     */
    public void save(EmployeeDTO employeeDTO) {
        //为了方便封装前端传过来的数据，才使用的dto
        //传到持久层就最好使用实体，所以这里就要做一个对象转换
        Employee employee = new Employee();

        //对象属性拷贝，不用一个个去set,从源employeeDTO烤过去employee
        BeanUtils.copyProperties(employeeDTO,employee);

        //设置DTO中没有的,1表示正常，0表示锁定
        //使用常量，方便管理数据
        employee.setStatus(StatusConstant.ENABLE);

        //设置默认密码
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //设置创建时间和修改时间
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());

        //设置当前记录创建人id和修改人id
        //employee.setCreateUser(BaseContext.getCurrentId());
        //employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }

    /**
     * 分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO){
        //开始分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());

        Page<Employee> page=employeeMapper.pageQuery(employeePageQueryDTO);

        long total = page.getTotal();
        List<Employee> records=page.getResult();
        return new PageResult(total,records); 
    }

    public void startOrStop(Integer status, Long id){
       //new Employee.var alt+enter
//        Employee employee = new Employee();
//        employee.setStatus(status);
//        employee.setId(id);

        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();

        employeeMapper.update(employee);

    }

    /**
     * 根据id查询员工
     *
     * @param id
     * @return
     */
    public Employee getEmployeeInfoById(Long id){
        Employee employee = employeeMapper.getEmployeeInfoById(id);

        employee.setPassword("******");

        return employee;
    }

    /**
     * 编辑员工信息
     *
     * @param employeeDTO
     * @return
     */
    public void editEmployeeInfo(EmployeeDTO employeeDTO){
        //可以直接用之前写好的update，但是那个参数是employee
        //所以现在要做转换，用到拷贝，然后再赋值，赋值当前用户id的时候要用到baseContext
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);

        //employee.setUpdateTime(LocalDateTime.now());
       // employee.setUpdateUser(BaseContext.getCurrentId());

        //在查询语句有报错，原因为命名
        employeeMapper.update(employee);

    }
}
