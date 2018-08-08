package cn.itshaw.redisdm.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

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
