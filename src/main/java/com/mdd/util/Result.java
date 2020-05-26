package com.mdd.util;

import com.mdd.enums.ResultCodeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Maduo
 * @date 2020/3/20 14:26
 */
@Getter
@Setter
@ToString
public final class Result {

    private int code;
    private Object data;
    private String message;

    /**
     * 私有无参构造 不允许外部私自新加返回状态
     */
    private Result() {
    }

    /**
     * 带参数构造
     *
     * @param resultCodeEnum 返回结果枚举
     */
    public Result(ResultCodeEnum resultCodeEnum) {
        this.code = resultCodeEnum.getStatus();
        this.message = resultCodeEnum.getDesc();
    }

    /**
     * 带参数构造
     *
     * @param resultCodeEnum 返回结果编码
     * @param msg            结果描述
     */
    public Result(ResultCodeEnum resultCodeEnum, String msg, Object data) {
        this.code = resultCodeEnum.getStatus();
        this.message = msg;
        if (null == msg || "".equals(msg)) {
            this.message = resultCodeEnum.getDesc();
        }
        this.data = data;
    }

    /**
     * 无数据异常返回（code=500）
     *
     * @param msg 结果描述
     * @return
     */
    public static Result error(String msg) {
        return new Result(ResultCodeEnum.ERROR, msg, null);
    }

    public static Result error(ResultCodeEnum codeEnum, String msg) {
        return new Result(codeEnum, msg, null);
    }

    public static Result error(ResultCodeEnum codeEnum) {
        return new Result(codeEnum, codeEnum.getDesc(), null);
    }
    /**
     * 默认返回结果
     *
     * @return
     */
    public static Result success(Object data) {
        return new Result(ResultCodeEnum.SUCCESS, null, data);
    }

    /**
     * 有数据成功返回（code=200）
     *
     * @param data 返回数据
     * @param msg  结果描述
     * @return
     */
    public static Result success(Object data, String msg) {
        return new Result(ResultCodeEnum.SUCCESS, msg, data);
    }

    /**
     * 无数据成功返回（code=200）
     *
     * @param msg 结果描述
     * @return
     */
    public static Result success(String msg) {
        return new Result(ResultCodeEnum.SUCCESS, msg, null);
    }

    public static Result success() {
        return new Result(ResultCodeEnum.SUCCESS, "成功", null);
    }

    /**
     * 参数验证失败异常返回（code=999）
     *
     * @param msg 提示信息
     * @return
     */
    public static Result validationError(String msg, Object data) {
        return new Result(ResultCodeEnum.VALIDATION_ERROR, msg, data);
    }

    /**
     * 参数验证失败异常返回（code=999）
     *
     * @param msg 提示信息
     * @return
     */
    public static Result validationError(String msg) {
        return new Result(ResultCodeEnum.VALIDATION_ERROR, msg, null);
    }

    /**
     * 参数验证失败异常返回（code=999）
     *
     * @param msg 提示信息
     * @return
     */
    public static Result resubmitError(String msg) {
        return new Result(ResultCodeEnum.RESUBMIT_ERROR, msg, null);
    }

    /**
     * 参数验证失败异常返回（code=999）
     *
     * @param msg 提示信息
     * @return
     */
    public static Result paramsFormatException() {
        return new Result(ResultCodeEnum.PARAM_FORMATE_ERROR, null, null);
    }

    /**
     * 安全校验失败
     *
     * @param msg
     * @return
     */
    public static Result safetyCheck(String msg) {
        if (msg == null) {
            return new Result(ResultCodeEnum.SAFETY_CHECK);
        } else {
            return new Result(ResultCodeEnum.SAFETY_CHECK, msg, null);
        }
    }

    /**
     * 首字母小写
     *
     * @param name
     * @return
     */
    private static String captureName(String name) {
        char[] cs = name.toCharArray();
        cs[0] += 32;
        return String.valueOf(cs);
    }
}
