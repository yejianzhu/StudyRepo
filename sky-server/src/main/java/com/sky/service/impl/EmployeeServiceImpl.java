package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordEditFailedException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.sky.constant.PasswordConstant.DEFAULT_PASSWORD;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
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
            //账号不存在,抛出异常可以看一下com.sky.handler包下的全局异常处理器
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码加密,前端传来的密码进行加密,与从数据库中查出来的密码进行比对
        //DigestUtils为spring自带的工具可以进行文本加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        if (!password.equals(employee.getPassword())) {
            //密码错误,抛出异常
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus().equals(StatusConstant.DISABLE)) {
            //账号被锁定,抛出异常
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     *
     * @param employeeDTO 员工DTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        //将DTO对象转换为实体类,实体类对应了数据库的表(entity对象)
        //对象属性拷贝BeanUtils工具类(spring提供),copyProperties属性拷贝(从第一个参数拷贝到第二个参数,要求对应名称一致)
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        //设置账号状态
        employee.setStatus(StatusConstant.ENABLE);//StatusConstant为自定义的常量类
        //设置默认密码,进行MD5进行加密
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
//        //设置账号创建时间和修改时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        //设置账号由谁创建和修改,记录id
//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.insert(employee);
    }

    /**
     * 员工分页查询
     * PageHelper实现分页查询,需要引入依赖
     * Page类有属性:
     * private int pageNum;页码
     * private int pageSize;每页记录数
     * private long startRow;//开始行
     * private long endRow;//结束行
     * private long total;//总记录数
     * private int pages;//页数
     *
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //开始分页:page页码,pageSize每页记录数
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        //分页查询,用Page类对象接受分页查询结果,Page继承了ArrayList接口
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        //得到page对象的参数
        long total = page.getTotal();//1)总记录数
        List<Employee> result = page.getResult();//Page继承了ArrayList接口,page.getResult()实际上就是将page对象赋值

        return new PageResult(total, result);
    }


    /**
     * 启用禁用员工账号
     *
     * @param status 员工账号状态
     * @param id     被操作的员工id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        //对数据进行封装
        Employee employee = Employee.builder()
                .id(id)
                .status(status)
//                .updateTime(LocalDateTime.now())
//                .updateUser(BaseContext.getCurrentId())
                .build();
        employeeMapper.update(employee);
    }

    /**
     * 根据员工id查询回显员工信息
     *
     * @param id
     * @return
     */
    @Override
    public Employee getEmployeeById(Long id) {
        Employee employee = employeeMapper.getEmployeeById(id);
        employee.setPassword("******");//密码处理一下不让浏览器知道
        return employee;
    }

    /**
     * 编辑员工信息
     *
     * @param employeeDTO
     */
    @Override
    public void editEmployee(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);//对象属性拷贝
        //更新一些信息
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(employee);//复用update方法
    }

    /**
     * 员工修改密码
     *
     * @param passwordEditDTO
     */
    @Override
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        String oldPassword = DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes());
        String newPassword = DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes());
        //比较新旧密码是否一致
        if (oldPassword.equals(newPassword)) {
            throw new PasswordEditFailedException(MessageConstant.PASSWORD_EDIT_FAILED);
        }
        //根据id获取数据库中的密码
        String password = employeeMapper.selectPassword(passwordEditDTO.getEmpId());
        //如果数据库中的密码与传过来的旧密码不一致
        if (!password.equals(oldPassword)) {
            throw new PasswordEditFailedException(MessageConstant.PASSWORD_EDIT_FAILED);
        }
        //修改密码
        Employee employee = Employee.builder()
                .id(passwordEditDTO.getEmpId())
                .password(newPassword)
                .build();
        employeeMapper.update(employee);
    }


}
