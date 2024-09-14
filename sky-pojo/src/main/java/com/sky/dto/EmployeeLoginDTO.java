package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

//来自Lombok库，这个注解会自动为类生成常见的方法，例如 getter、setter、toString、equals、hashCode 等
@Data
// 这是Swagger注解，用于描述该类的用途，帮助生成REST API文档
@ApiModel(description = "员工登录时传递的数据模型")
public class EmployeeLoginDTO implements Serializable {

    //这些注解用于描述类中的字段，帮助生成Swagger文档时，对字段的用途进行说明
    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码")
    private String password;

}
