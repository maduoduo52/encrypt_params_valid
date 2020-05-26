package com.mdd.enums;

import lombok.Getter;

/**
 * @author Maduo
 * @date 2020/3/20 14:27
 */
@Getter
public enum ResultCodeEnum {

    SUCCESS(200,"操作成功"),

    ERROR(500,"操作失败"),

    VALIDATION_ERROR(999,"参数校验失败"),

    SAFETY_CHECK(100,"安全校验失败"),

    RESUBMIT_ERROR(406,"请求重复提交"),

    WMS_SUCCESS_CODE(0, "操作成功"),

    PARAM_FORMATE_ERROR(407, "参数格式化异常，请检查参数格式"),

    WMS_REQ_ERROR(408, "WMS数据交互异常"),

    API_ERROR(409, ""),

    CUSTOMER_NOT_EXIST(410, "客户数据不存在"),

    CREATE_TOKEN_ERROR(411, "token获取失败"),

    METHOD_NOT_ALLOWED(405, "Method not allowed"),
    ;

    ;
    /**
     * 状态值
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String desc;

    ResultCodeEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
