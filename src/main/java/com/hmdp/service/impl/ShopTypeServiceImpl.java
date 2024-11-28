package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IShopTypeService typeService;

    @Override
    public Result queryShopTypeList() {
        //        List<ShopType> typeList = typeService
//                .query().orderByAsc("sort").list();
//        return Result.ok(typeList);
        // 1.从redis里查询商铺类型缓存
        Long length = stringRedisTemplate.opsForList().size("cache:shopType");
        String typeListJson = stringRedisTemplate.opsForList().index("cache:shopType",length);
        // 2.如果存在，直接返回
        if (StrUtil.isNotBlank(typeListJson)) {
            return Result.ok(JSONUtil.toList(typeListJson, ShopType.class));
        }
        // 3.如果不存在，从数据库从获取
        List<ShopType> typeList = typeService.query().orderByAsc("sort").list();
        // 4.如果数据库不存在，返回商铺类型不存在
        if (typeList.isEmpty()) {
            return Result.fail("商铺类型不存在！");
        }
        // 5.如果存在，存入redis缓存
        stringRedisTemplate.opsForList().leftPushAll("cache:shopType", JSONUtil.toJsonStr(typeList));
        // 6.返回
        return Result.ok(typeList);
    }
}
