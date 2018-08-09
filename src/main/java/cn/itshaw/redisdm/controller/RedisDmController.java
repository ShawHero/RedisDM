package cn.itshaw.redisdm.controller;

import java.util.*;

import cn.itshaw.redisdm.bean.RedisDMBean;
import cn.itshaw.redisdm.bean.RedisNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.*;

@Slf4j
@Api(tags="Redis数据迁移")
@RestController
@RequestMapping("/")
public class RedisDmController {

    @ApiOperation("Redis数据迁移")
    @PostMapping("/redisdm/")
    public  @ApiParam("迁移是否成功") String redisDM(@RequestBody RedisDMBean redisDMBean) throws Exception {
        int count;
        try {
            String x = validate(redisDMBean);
            if (x != null) {
                return x;
            }
            count = redisDataMove(redisDMBean.getSourceAddress(),redisDMBean.getTargetAddress(),redisDMBean.getPattern());
        }catch (Exception ex){
            return "数据迁移失败！"+ex.getMessage();
        }
        return  "数据迁移成功！迁移key的数据为："+count;
    }

    private String validate(@RequestBody RedisDMBean redisDMBean) {
        if(CollectionUtils.isEmpty(redisDMBean.getSourceAddress())){
            return "来源地址不能为空！";
        }else if(CollectionUtils.isEmpty(redisDMBean.getTargetAddress())){
            return "目标地址不能为空！";
        }else  if(StringUtils.isEmpty(redisDMBean.getPattern())){
            return "匹配规则不能为空！！";
        }
        return null;
    }

    /**
     *
     * @param sourceAddress
     * @param targetAddress
     * @param pattern
     */
    public static int redisDataMove(List<RedisNode> sourceAddress,List<RedisNode> targetAddress,String pattern){
        int sumCount = 0;
        for(RedisNode targetAddr : targetAddress){

            Jedis targetJedis = new Jedis(targetAddr.getIp(), targetAddr.getPort());

            if(!StringUtils.isEmpty(targetAddr.getPassword())) {
                targetJedis.auth(targetAddr.getPassword());
            }

            for(RedisNode sourceAddr : sourceAddress){

                Jedis sourceJedis = new Jedis(sourceAddr.getIp(), sourceAddr.getPort());

                if(!StringUtils.isEmpty(sourceAddr.getPassword())) {
                    sourceJedis.auth(sourceAddr.getPassword());
                }

                ScanParams scanParams = new ScanParams();
                scanParams.count(10000);
                scanParams.match(pattern);
                String cursor = "0";
                ScanResult<String> scanResult = sourceJedis.scan(cursor, scanParams);
                int count = 0;
                while (!scanResult.getStringCursor().equals(cursor)) {
                    for(String key : scanResult.getResult()){
                        String keyType;
                        try{
                            keyType = sourceJedis.type(key);

                            if(StringUtils.isEmpty(keyType)){
                                continue;
                            }
                        }catch (Exception ex){
                            continue;
                        }
                        Long expire = sourceJedis.ttl(key);
                        log.info("key:" + key + ",keyType:" + keyType + ",expire:" + expire);
                        targetJedis.expire(key, expire.intValue());
                        switch (keyType) {
                            case "string":
                                String strValues = sourceJedis.get(key);
                                targetJedis.set(key, strValues);
                                count++;
                                break;
                            case "hash":
                                Map<String, String> hashValues = sourceJedis.hgetAll(key);
                                targetJedis.hmset(key, hashValues);
                                count++;
                                break;
                            case "set":
                                Set<String> setValues = sourceJedis.smembers(key);
                                targetJedis.sadd(key, setValues.toArray(new String[setValues.size()]));
                                count++;
                                break;
                            case "list":
                                List<String> listValues = sourceJedis.lrange(key, 0, sourceJedis.llen(key) - 1);
                                targetJedis.lpush(key, listValues.toArray(new String[listValues.size()]));
                                count++;
                                break;
                            case "zset":
                                Set<String> zsetValues = sourceJedis.zrange(key,0,-1);
                                for(String member : zsetValues){
                                    Double score = sourceJedis.zscore(key, member);
                                    targetJedis.zadd(key,score,member);
                                }
                                count++;
                                break;
                            case "bitmaps":
                            case "hyperloglogs":
                            case "geospatial":
                            case "none":
                            default:
                                log.info("type of '{}' does not support!", keyType);
                                break;
                        }
                    }
                    scanResult = sourceJedis.scan(scanResult.getStringCursor(), scanParams);
                }
                sumCount = sumCount + count;
                log.info("from :" +sourceAddr+ ",to:"+targetAddr+ "  move success! count size:" + count);
            }

        }
        log.info("move success! sum count size:" + sumCount);
        return sumCount;
    }
}