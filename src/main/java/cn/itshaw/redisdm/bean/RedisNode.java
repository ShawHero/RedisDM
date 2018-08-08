package cn.itshaw.redisdm.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Redis节点信息")
public class RedisNode {
    @ApiModelProperty("ip")
    private String ip;
    @ApiModelProperty("port")
    private Integer port;
    @ApiModelProperty("password")
    private String password;
}
