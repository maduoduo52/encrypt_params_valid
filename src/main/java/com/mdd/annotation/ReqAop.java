package com.mdd.annotation;

import com.mdd.dto.BaseDto;

import java.lang.annotation.*;

/**
 * @author Maduo
 * @date 2020/3/20 16:48
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReqAop {

    /**
     * 返回数据是否加密
     *
     * @return
     */
    boolean aes() default false;

    /**
     * 是否参数校验
     *
     * @return
     */
    boolean valiedArgs() default false;

    /**
     * 当需要获取账号时获取账号的类型是什么
     * @return
     */
    Class<? extends BaseDto> clazz() default BaseDto.class;
}
