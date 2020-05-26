package com.mdd.annotation;

import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONTokener;
import com.alibaba.fastjson.JSON;
import com.mdd.enums.ExceptionEnum;
import com.mdd.util.AESCodeUtil;
import com.mdd.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.validator.constraints.Length;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

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
        String key = "wmsapi0123456789";

        String reqData;
        Object rvt;
        if (reqAop.valiedArgs()) {

            long start = System.currentTimeMillis();
            reqData =  validParamsByAnnotation(pjp, reqAop, validList, key);
            log.warn("===>参数校验耗时：{}ms，校验结果：{}, 解密结果：{}", System.currentTimeMillis() - start, JSON.toJSONString(validList), reqData);
            if (!CollectionUtils.isEmpty(validList)) {
                //参数校验不通过将错误信息存入日志
                return Result.validationError(ExceptionEnum.PARAMS_VALIED_ERROR.getCode(), validList);
            }

            if (StringUtils.isEmpty(reqData)) {
                return Result.validationError(ExceptionEnum.REQUEST_BODY_NOT_NULL.getMessage());
            }

            //设置请求体为解密之后的json串
            Object[] params = {reqData};
            rvt = pjp.proceed(params);
        }else {
            rvt = pjp.proceed();
        }

        if (!(rvt instanceof Result)) {
            log.error(ExceptionEnum.RES_MUST_RESULT.getMessage());
            throw new RuntimeException(ExceptionEnum.RES_MUST_RESULT.getMessage());
        }
        Result result = (Result) rvt;
        if (reqAop.aes()) {
            if (result.getCode() == HttpStatus.HTTP_OK && !Objects.isNull(result.getData())) {
                // 返回参数加密
                result.setData(AESCodeUtil.encode(JSON.toJSONString(result.getData()), key));
            }
        }
        return result;
    }

    private String validParamsByAnnotation(ProceedingJoinPoint pjp, ReqAop reqAop, List<String> validList, String key) {
        Object[] arguments = pjp.getArgs();
        if (Objects.isNull(arguments)) {
            throw new RuntimeException(ExceptionEnum.REQUEST_BODY_NOT_NULL.getMessage());
        }
        // 解密请求参数
        String decryValue;

        try {
            decryValue = AESCodeUtil.decode((String) arguments[0], key);
        } catch (Exception e) {
            log.error("===> 请求参数解密异常：{}", e);
            throw new RuntimeException(ExceptionEnum.PARAMS_DECODE_ERROR.getMessage());
        }

        if (decryValue == null) {
            throw new RuntimeException(ExceptionEnum.PARAMS_DECODE_ERROR.getMessage());
        }
        Object object = new JSONTokener(decryValue).nextValue();

        //获取需要校验的类信息
        Class claz = reqAop.clazz();
        if(object instanceof JSONObject){
            //存放解密参数信息
            Map<String, Object> paramMap = JSON.parseObject(decryValue);

            validField(paramMap, validList, claz);
        }else if (object instanceof JSONArray) {
            com.alibaba.fastjson.JSONArray jsonArray = JSON.parseArray(decryValue);

            for (Object o : jsonArray) {
                Map<String, Object> listParamMap = JSON.parseObject(JSON.toJSONString(o));
                validField(listParamMap, validList, claz);
            }

        }else {
            throw new RuntimeException();
        }

        return decryValue;
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
                    Object o = null;
                    if (paramMap != null) {
                        o = paramMap.get(field.getName());
                    }
                    if (o != null) {
                        Object object = new JSONTokener(o.toString()).nextValue();
                        if (object instanceof cn.hutool.json.JSONArray) {
                            com.alibaba.fastjson.JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(object));
                            for (Object o1 : jsonArray) {
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
            }
            //获取参数值
            Object o = null;
            if (paramMap != null) {
                o = paramMap.get(field.getName());
            }
            //获取NotBlank注解
            NotBlank blank = field.getAnnotation(NotBlank.class);
            if (blank != null) {
                valideNull(validList, o, blank.message());
            }
            //获取NotNull注解
            NotNull notNull = field.getAnnotation(NotNull.class);
            if (notNull != null) {
                valideNull(validList, o, notNull.message());
            }
            Length length = field.getAnnotation(Length.class);
            if (length != null) {
                String val = o + "";
                if (val.length() > length.max()) {
                    validList.add(length.message());
                }
            }
            Pattern pattern = field.getAnnotation(Pattern.class);
            if (pattern != null) {
                if (StringUtils.isNotBlank(pattern.regexp()) && !Objects.isNull(o)) {
                    boolean flag = java.util.regex.Pattern.matches(pattern.regexp(), o.toString());
                    if (!flag) {
                        validList.add(pattern.message());
                    }
                }
            }
            FormatLimit formatLimit = field.getAnnotation(FormatLimit.class);
            if (formatLimit != null && !Objects.isNull(o) && StringUtils.isNotBlank(o.toString())) {
                String[] formates = formatLimit.forages();
                boolean contains = Arrays.asList(formates).contains(o.toString());
                if (!contains) {
                    validList.add(formatLimit.message());
                }
            }
            Min min = field.getAnnotation(Min.class);
            if (min != null && !Objects.isNull(o)) {
                if (o instanceof Integer) {
                    int val = Integer.parseInt(o + "");
                    if (val < min.value()) {
                        validList.add(min.message());
                    }
                }
            }
            DecimalMin decimalMin = field.getAnnotation(DecimalMin.class);
            if (decimalMin != null && !Objects.isNull(o)) {
                BigDecimal o1 ;
                if (o instanceof BigDecimal) {
                    o1 = (BigDecimal) o;
                    BigDecimal m = new BigDecimal(decimalMin.value());
                    if (o1.compareTo(m) < 0) {
                        validList.add(decimalMin.message());
                    }
                }else if (o instanceof Integer) {
                    o1 = new BigDecimal((Integer) o);
                    BigDecimal m = new BigDecimal(decimalMin.value());
                    if (o1.compareTo(m) < 0) {
                        validList.add(decimalMin.message());
                    }
                }
            }
        }
    }

    private void valideNull(List<String> validList, Object o, String message) {
        if (o instanceof String) {
            if (StringUtils.isBlank((String) o)) {
                validList.add(message);
            }
        } else {
            if (Objects.isNull(o)) {
                validList.add(message);
            }
        }
    }
}
