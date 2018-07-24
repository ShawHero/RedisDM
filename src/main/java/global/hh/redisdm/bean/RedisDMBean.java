package global.hh.redisdm.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Function:
 * <P> 版权所有 ©2013 Biostime Inc.. All Rights Reserved
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 * User: 12360
 * Date: 2018/7/20
 * Time: 17:26
 */
@Data
@ApiModel("Redis迁移Bean")
public class RedisDMBean {
    @ApiModelProperty("sourceAddress")
    private List<RedisNode> sourceAddress;
    @ApiModelProperty("targetAddress")
    private List<RedisNode> targetAddress;
    @ApiModelProperty("pattern")
    private String pattern;
}
