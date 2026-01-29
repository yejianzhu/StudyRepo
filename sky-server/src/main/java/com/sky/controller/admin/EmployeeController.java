package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@Tag(name = "员工管理接口")//swagger注解
@RestController
@RequestMapping("/admin/employee")
@Slf4j//自动生成log对象,进行日志输出,无需再手动声明
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;//该类为yml配置绑定类,获取yml文件的数据,这个类主要用于生成令牌
    private int id;

    /**
     * 登录功能
     * 注意!注意!注意!客户端登录网站时,拦截器会校验令牌是否合法
     * 但这一操作是spring自动进行的,不需要手动写代码,所以可能会忽略
     * 这一操作具体需要到com.sky.interceptor包和com.sky.config包下查看
     * 而且需要仔细看,因为这一部分不太懂
     * 开始运行程序,会先扫描配置类,然后开始拦截,通过拦截器后,访问Controller层的方法
     *
     * @param employeeLoginDTO 员工登录DTO
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        //进行登录
        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());//将员工id放进token中
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),//yml中配置的密钥
                jwtProperties.getAdminTtl(),//yml中配置的令牌有效时间
                claims);//claims意为身份声明,键值对形式,即用户的一些信息,这里是员工的id

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工
     *
     * @param employeeDTO 员工DTO
     * @return Result
     */
    @Operation(summary = "新增员工")
    @PostMapping
    public Result save(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工:{}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     * 员工分页查询
     * 请求参数可以放在Restful风格URL中(路径参数或请求参数两种形式)和请求体中,该方法的请求参数就是放在请求体中
     *
     * @param employeePageQueryDTO
     * @return Result<PageResult>
     */
    @GetMapping("/page")
    @Operation(summary = "员工分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("分页查询参数: {}", employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);//查询出总记录数和当前页码数据信息
        return Result.success(pageResult);
    }

    /**
     * 启用禁用员工账号
     *
     * @param status 员工账号状态,为路径参数,在URL中,使用@PathVariable注解
     * @param id     被操作的员工id,为查询参数,传递到后端时,一样在URL,但Restful风格的URL中@PostMapping等注解只关注路径,不需要展示出来
     * @return Result
     */
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("启用禁用员工账号:{},{}", status, id);
        employeeService.startOrStop(status, id);
        return Result.success();
    }


    /**
     * 根据员工id查询回显员工信息
     * @param id 员工id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Employee> getEmployeeById(@PathVariable Long id) {
        log.info("要查询回显的员工id:{}", id);
        Employee employee = employeeService.getEmployeeById(id);
        return Result.success(employee);
    }

    /**
     * 编辑员工信息
     * @param employeeDTO 接受JSON格式的参数用@RequestBody
     * @return
     */
    @PutMapping
    public Result editEmployee(@RequestBody EmployeeDTO employeeDTO) {
        log.info("编辑员工信息:{}", employeeDTO);
        employeeService.editEmployee(employeeDTO);
        return Result.success();
    }

    /**
     * 修改员工密码
     * 前端代码有问题,不按照接口文档设计
     * 前端会发送旧密码和校验过的新密码(只有一个),但不会发送员工id
     * @param passwordEditDTO
     * @return
     */
    @PutMapping("/editPassword")
    public Result editPassword(@RequestBody PasswordEditDTO passwordEditDTO) {
        passwordEditDTO.setEmpId(BaseContext.getCurrentId());//由于前端没发id过来,这里需要自己设计
        log.info("修改密码:{}", passwordEditDTO);
        employeeService.editPassword(passwordEditDTO);
        return Result.success();
    }
}
