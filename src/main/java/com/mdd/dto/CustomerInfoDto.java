package com.mdd.dto;

import com.mdd.annotation.FormatLimit;
import lombok.Data;

import javax.validation.constraints.NotBlank;


/**
 * 用户信息表
 * @author Maduo
 * @date 2020-03-20 14:09:44
 */
@Data
public class CustomerInfoDto extends BaseDto{
  /**
   * 用户名称
   */
  @NotBlank(message = "userName不能为空")
  private String userName;
  /**
   * 密码
   */
  private String password;
  /**
   * 盐值
   */
  @NotBlank(message = "salt不能为空")
  private String salt;
  /**
   * 公司名称
   */
  @NotBlank(message = "companyName不能为空")
  private String companyName;
  /**
   * 公司地址
   */
  private String companyAddress;
  /**
   * 联系人名称
   */
  private String personName;
  /**
   * 联系人电话
   */
  private String personPhone;
  /**
   * 用户加密解密key
   */
  private String userKey;
  /**
   * 备注
   */
  private String baRemark;

  @FormatLimit(forages = {"0", "1"}, message = "status不符合约定")
  private Integer status;
}
