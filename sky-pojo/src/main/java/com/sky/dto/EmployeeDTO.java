package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeeDTO implements Serializable {

    private Long id;//员工id

    private String username;//账号

    private String name;//姓名

    private String phone;//电话

    private String sex;//性别

    private String idNumber;//身份证号码

}
