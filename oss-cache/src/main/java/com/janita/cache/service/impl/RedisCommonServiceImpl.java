package com.janita.cache.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.janita.cache.service.CommonSortQuery;
import com.janita.cache.service.RedisCommonService;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 利用redis的SET及HASH配合对对象进行分页查询
 */
public class RedisCommonServiceImpl implements RedisCommonService {
    private static final String ID = "id";
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource(name = "redisTemplate")
    private SetOperations<String, String> setOperations;
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, Object> hashOperations;

    /**
     * 把JSON的ID先存入SET:keySpace中，keySpace-id
     * 再把此JSON对象存到HASH:keySpace:id-data
     * @param keySpace
     * @param data
     * @return
     */
    @Override
    public JSONObject save(String keySpace, JSONObject data) {
        String id = data.getString(ID);
        if (StringUtils.isEmpty(id)) {
            id = randomId();
            //把id存入键为keySpace的SET中，这样就可以知道某个keySpace下面有多少个id了
            setOperations.add(keySpace, id);
            data.put(ID, id);
            //生成联合键，存入
            hashOperations.putAll(combineKey(keySpace, id), data);
        } else {
            //覆盖之前的
            hashOperations.putAll(combineKey(keySpace, id), data);
        }
        return data;
    }

    /**
     * 从HASH(keySpace:id)取出对象
     * @param keySpace
     * @param id
     * @return
     */
    @Override
    public JSONObject get(String keySpace, String id) {
        String combineKey = combineKey(keySpace, id);
        if (redisTemplate.hasKey(combineKey)) {
            Map<String, Object> result = hashOperations.entries(combineKey);
            return JSONObject.parseObject(JSONObject.toJSONString(result));
        }
        return null;
    }

    /**
     * 删除SET及HASH中的id
     * @param keySpace
     * @param id
     */
    @Override
    public void delete(String keySpace, String id) {
        setOperations.remove(keySpace, id);
        redisTemplate.delete(combineKey(keySpace, id));
    }

    /**
     * 把空间为keySpace下面的所有的HASH取出
     * 如：keySpace:id1-data1,keySpace:id2-data2....
     * @param keySpace
     * @return
     */
    @Override
    public JSONArray findAll(final String keySpace) {
        final Set<String> ids = setOperations.members(keySpace);
        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }

        List<Object> dataList = redisTemplate.executePipelined(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer keySerializer = redisTemplate.getKeySerializer();
                for (String id : ids) {
                    connection.hGetAll(keySerializer.serialize(combineKey(keySpace, id)));
                }
                return null;
            }
        });

        if (CollectionUtils.isEmpty(dataList)) {
            return null;
        }

        JSONArray results = new JSONArray();
        for (Object data : dataList) {
            results.add(JSONObject.parseObject(JSONObject.toJSONString(data)));
        }

        return results;
    }

    /**
     * 分页查询出
     * @param keySpace  空间
     * @param pageable  分页参数
     * @return  keySpace下面的对象
     */
    @Override
    public Page findAll(final String keySpace, Pageable pageable) {
        if (pageable == null) {
            //若没有分页参数，则查询全部
            return new PageImpl(this.findAll(keySpace));
        }

        final String property = pageable.getSort() == null ? null : pageable.getSort().iterator().next().getProperty();
        final String direction = pageable.getSort() == null ? null : pageable.getSort().iterator().next().getDirection().toString();

        //SET集合的总元素数
        long total = setOperations.size(keySpace);
        //分页原理：利用SET中的id分页，取出本页的id后再根据keySpace:id去HASH中查询对象
        final List<Object> ids = redisTemplate.sort(new CommonSortQuery(keySpace, pageable.getOffset(),
                pageable.getPageSize(), property, direction));

        List<Object> dataList = redisTemplate.executePipelined(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer keySerializer = redisTemplate.getKeySerializer();
                for (Object id : ids) {
                    connection.hGetAll(keySerializer.serialize(combineKey(keySpace, (String) id)));
                }
                return null;
            }
        });

        if (CollectionUtils.isEmpty(dataList)) {
            return null;
        }

        JSONArray results = new JSONArray();
        for (Object data : dataList) {
            results.add(JSONObject.parseObject(JSONObject.toJSONString(data)));
        }

        return new PageImpl(results, pageable, total);
    }

    /**
     * 获取一个UUID，并去掉"-"
     */
    private static String randomId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * @param keySpace  存放某个类型的key的空间
     * @param id        id
     * @return  把key为id的键放到空间keySpace下面
     */
    private static String combineKey(String keySpace, String id) {
        return keySpace + ":" + id;
    }
}
