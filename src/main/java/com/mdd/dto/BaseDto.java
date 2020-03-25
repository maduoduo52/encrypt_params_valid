package com.mdd.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Maduo
 * @date 2020/3/20 13:45
 */
@Data
public class BaseDto {

    @NotBlank(message = "tenantId不能为空")
    private String tenantId;

    @NotBlank(message = "shipperCode不能为空")
    private String shipperCode;

    /**
     * id
     */
    protected Long id;

    /**
     * 版本号
     */
    protected Integer version;
}
