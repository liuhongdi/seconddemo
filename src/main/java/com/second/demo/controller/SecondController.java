package com.second.demo.controller;


import com.second.demo.service.SecondService;
import com.second.demo.util.ServerResponseUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/*
*
* 秒杀功能演示
* 返回：json
* status = 0: 成功
* status = 1: 失败
* msg: 提示信息
*
** */
@Controller
@RequestMapping("/second")
public class SecondController {

    @Resource
    private SecondService secondService;


    //静态页
    @GetMapping("/index")
    public String Index(ModelMap modelMap) {

        return "second/index";
    }


    //添加活动中的sku,
    //参数:活动id,sku的id,sku的库存数量，当前sku针对单个用户的购买数量限制
    @GetMapping("/skuadd")
    @ResponseBody
    public Object skuAdd(@RequestParam(value="actid",required = true,defaultValue = "") String actId,
                      @RequestParam(value="skuid",required = true,defaultValue = "") String skuId,
                      @RequestParam(value="amount",required = true,defaultValue = "0") int amount) {
        if (actId.equals("")) {
            return new ServerResponseUtil(1,"activity id不可为空","");
        }
        if (skuId.equals("")) {
            return new ServerResponseUtil(1,"sku id不可为空","");
        }
        if (amount<=0) {
            return new ServerResponseUtil(1,"sku库存必須大于0","");
        }

        boolean isSucc = secondService.skuAdd(actId,skuId,amount);
        int status = 1;
        String msg = "";

        if (isSucc == true) {
            status = 0;
            msg = "add sku amount success";
        } else {
            status = 1;
            msg = "add sku amount failed";
        }

        ServerResponseUtil response = new ServerResponseUtil(status,msg,"");
        return response;
    }


    //秒杀指定sku
    //参数: 活动id,用户id,购买数量,sku的id,用户购买当前sku的数量限制，用户购买当前活动中商品的数量限制
    //说明：用户id应从session或jwt获取，为方便测试做了传递
    @GetMapping("/skusecond")
    @ResponseBody
    public Object skuSecond(@RequestParam(value="actid",required = true,defaultValue = "") String actId,
                         @RequestParam(value="userid",required = true,defaultValue = "") String userId,
                         @RequestParam(value="buynum",required = true,defaultValue = "0") int buyNum,
                         @RequestParam(value="skuid",required = true,defaultValue = "") String skuId,
                         @RequestParam(value="perskulim",required = true,defaultValue = "0") int perSkuLim,
                         @RequestParam(value="peractlim",required = true,defaultValue = "0") int perActLim
                         ) {

        if (actId.equals("")) {
            return new ServerResponseUtil(1,"用户id不可为空","");
        }
        if (userId.equals("")) {
            return new ServerResponseUtil(1,"活动id不可为空","");
        }
        if (skuId.equals("")) {
            return new ServerResponseUtil(1,"sku id不可为空","");
        }
        if (buyNum<=0) {
            return new ServerResponseUtil(1,"购买数量必須大于0","");
        }

        String result = secondService.skuSecond(actId,userId,buyNum,skuId,perSkuLim,perActLim);

        String msg = "";
        int status = 1;

        if (result.equals("-1")) {
            msg = "已超出当前活动每件sku允许每人秒杀的数量";
            status = 1;
        }else if (result.equals("-2")) {
            msg = "已超出当前活动允许每人秒杀的数量";
            status = 1;
        }else if (result.equals("-3")) {
            msg = "sku不存在或秒杀数量未设置";
            status = 1;
        } else if (result.equals("0")) {
            msg = "库存数量不足，秒杀失败";
            status = 1;
        } else {
            msg = "秒杀成功;秒杀编号:"+result;
            status = 0;
        }

        ServerResponseUtil response = new ServerResponseUtil(status,msg,"");
        return response;
    }


}
