package com.mdd.annotation;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
        if (!Objects.isNull(arguments)) {
            throw new RuntimeException("请求body不能为空");
        }
        // 解密请求参数
        String decryValue;
        try {
            decryValue = AESCodeUtil.decryptAES((String) arguments[0], key);
        } catch (Exception e) {
            log.error("===>参数解密异常：{}", e);
            throw new RuntimeException("参数解密异常");
        }

        //存放解密参数信息
        Map<String, Object> paramMap = JSON.parseObject(decryValue);
        //获取需要校验的类信息
        Class claz = reqAop.clazz();

        validField(paramMap, validList, claz);
    }

    private void validField(Map<String, Object> paramMap, List<String> validList, Class clazz) {
        //获取需要校验类的全部属性信息
        Field[] fields = FieldUtils.getAllFields(clazz);
        for (Field field : fields) {
            //处理List类型属性
            if (List.class.isAssignableFrom(field.getType())) {
                Type t = field.getGenericType();
                if (t instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) t;
                    //得到对象list中实例的类型
                    Class clz = (Class) pt.getActualTypeArguments()[0];
                    Object o = paramMap.get(field.getName());
                    if (o instanceof JSONArray) {
                        for (Object o1 : ((JSONArray)o)) {
                            //获取list对象的属性
                            Map<String, Object> listParamMap = JSON.parseObject(JSON.toJSONString(o1));
                            if (!listParamMap.isEmpty()) {
                                //递归校验List参数信息
                                validField(listParamMap, validList, clz);
                            }
                        }
                    }
                }
            }
            //获取参数值
            Object o = paramMap.get(field.getName());
            //获取NotBlank注解
            NotBlank blank = field.getAnnotation(NotBlank.class);
            if (blank != null) {
                if (Objects.isNull(o)) {
                    validList.add(clazz.getSimpleName() + "." + blank.message());
                }
            }
            //获取NotNull注解
            NotNull notNull = field.getAnnotation(NotNull.class);
            if (notNull != null) {
                if (Objects.isNull(o)) {
                    validList.add(clazz.getSimpleName() + "." + notNull.message());
                }
            }
        }
    }
}
