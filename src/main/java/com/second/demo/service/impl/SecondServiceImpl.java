package com.second.demo.service.impl;

import com.second.demo.service.SecondService;
import com.second.demo.util.RedisHashUtil;
import com.second.demo.util.RedisLuaUtil;
import com.second.demo.util.TimeUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SecondServiceImpl implements SecondService {

    @Resource
    private RedisHashUtil redisHashUtil;

    @Resource
    private RedisLuaUtil redisLuaUtil;

    /*
    *    添加一个秒杀活动的sku
    *    actId:活动id
    *     skuId:sku id
    *     amount:sku的库存数
    * */
    @Override
    public boolean skuAdd(String actId,String skuId,int amount) {

        String nameAmount = "sec_"+actId+"_sku_amount_hash";
        boolean isSuccAmount = redisHashUtil.setHashValue(nameAmount,skuId,amount);

        if (isSuccAmount) {
            return true;
        } else {
            return false;
        }
    }

    /*
    * 秒杀功能，
    * 调用second.lua脚本
    * actId:活动id
    * userId:用户id
    * buyNum:购买数量
    * skuId:sku的id
    * perSkuLim:每个用户购买当前sku的个数限制
    * perActLim：每个用户购买当前活动内所有sku的总数量限制
    * 返回:
    * 秒杀的结果
    *  * */
    @Override
    public String skuSecond(String actId,String userId,int buyNum,String skuId,int perSkuLim,int perActLim) {

        //时间字串，用来区分秒杀成功的订单
        int START = 100000;
        int END = 900000;
        int rand_num = ThreadLocalRandom.current().nextInt(END - START + 1) + START;
        String order_time = TimeUtil.getTimeNowStr()+"-"+rand_num;

        List<String> keyList = new ArrayList();
        keyList.add(userId);
        keyList.add(String.valueOf(buyNum));

        keyList.add(skuId);
        keyList.add(String.valueOf(perSkuLim));

        keyList.add(actId);
        keyList.add(String.valueOf(perActLim));

        keyList.add(order_time);

        String result = redisLuaUtil.runLuaScript("second.lua",keyList);
        System.out.println("------------------lua result:"+result);

        return result;
    }
}
