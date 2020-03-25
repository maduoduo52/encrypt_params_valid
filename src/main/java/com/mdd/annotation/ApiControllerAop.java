package com.mdd.annotation;

import com.alibaba.fastjson.JSON;
import com.mdd.util.AESCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Maduo
 * @date 2020/3/20 16:51
 */
@Aspect
@Component
@Order(1)
@Slf4j
public class ApiControllerAop {

    @Around("@annotation(reqAop)")
    public Object around(ProceedingJoinPoint pjp, ReqAop reqAop) throws Throwable {
        log.info("====>>>ReqAop aop 开始启动 ");

        List<String> validList = new ArrayList<>();
        if (reqAop.valiedArgs()) {
            String key = "wmsapi0123456789";

            validParamsByAnnotation(pjp, reqAop, validList, key);
        }

        if (!CollectionUtils.isEmpty(validList)) {
            //参数校验不通过将错误信息存入日志

            throw new RuntimeException(JSON.toJSONString(validList));
        }
        Object rvt = pjp.proceed();

        return rvt;
    }



    private void validParamsByAnnotation(ProceedingJoinPoint pjp, ReqAop reqAop, List<String> validList, String key) throws Exception {
        Object[] arguments = pjp.getArgs();
        // 解密请求参数
        String decryValue = AESCodeUtil.decryptAES((String) arguments[0], key);

        //存放解密参数信息
        Map<String, Object> paramMap = JSON.parseObject(decryValue);
        //获取需要校验的类信息
        Class claz = reqAop.clazz();

        //获取需要校验类的全部属性信息
        Field[] fields = FieldUtils.getAllFields(claz);

        for (Field field : fields) {
            //获取参数值
            Object o = paramMap.get(field.getName());
            //获取NotBlank注解
            NotBlank blank = field.getAnnotation(NotBlank.class);
            if (blank != null) {
                if (Objects.isNull(o)) {
                    validList.add(blank.message());
                }
            }
            //获取NotNull注解
            NotNull notNull = field.getAnnotation(NotNull.class);
            if (notNull != null) {
                if (Objects.isNull(o)) {
                    validList.add(notNull.message());
                }
            }
        }
    }
}
