package com.mdd.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Maduo
 * @date 2020/3/24 14:45
 */
@AllArgsConstructor
@Getter
public enum ExceptionEnum {

    PARAMS_VALIED_ERROR("params.valied.error", "参数校验异常"),

    PARAMS_DECODE_ERROR("params.decode.error", "参数解密异常"),

    REQUEST_BODY_NOT_NULL("request.body.not.null", "请求body不能为空"),

    RES_MUST_RESULT("res.must.be.result", "返回对象必须是Result对象"),

    REQ_PARAM_MUST_NOT_BE_NULL("req.param.must.not.be.null", "请求参数不能为空"),

    CUSTOMER_NOT_EXIST("customer.not.exist", "用户不存在"),

    CALLBACK_URL_NOT_EXIST("callback.url.not.exist", "回调地址不存在"),

    PAGE_DATA_IS_NULL("page.data.is.null", "分页数据为空"),

    ;

    private String code;

    private String message;
}
