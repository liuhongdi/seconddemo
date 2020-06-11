
local userId = KEYS[1]
local buyNum = tonumber(KEYS[2])

local skuId = KEYS[3]
local perSkuLim = tonumber(KEYS[4])

local actId = KEYS[5]
local perActLim = tonumber(KEYS[6])

local orderTime = KEYS[7]

--用到的各个hash
local user_sku_hash = 'sec_'..actId..'_u_sku_hash'
local user_act_hash = 'sec_'..actId..'_u_act_hash'
local sku_amount_hash = 'sec_'..actId..'_sku_amount_hash'
local second_log_hash = 'sec_'..actId..'_log_hash'

--当前sku是否还有库存
local skuAmountStr = redis.call('hget',sku_amount_hash,skuId)
if skuAmountStr == false then
        redis.log(redis.LOG_NOTICE,'skuAmountStr is nil ')
        return '-3'
end;
local skuAmount = tonumber(skuAmountStr)
 redis.log(redis.LOG_NOTICE,'sku:'..skuId..';skuAmount:'..skuAmount)
 if skuAmount <= 0 then
   return '0'
end

redis.log(redis.LOG_NOTICE,'perActLim:'..perActLim)
local userActKey = userId..'_'..actId
--当前用户已购买此活动多少件
 if perActLim > 0 then
   local userActNumInt = 0
   local userActNum = redis.call('hget',user_act_hash,userActKey)
   if userActNum == false then
      redis.log(redis.LOG_NOTICE,'userActKey:'..userActKey..' is nil')
      userActNumInt = buyNum
   else
      redis.log(redis.LOG_NOTICE,userActKey..':userActNum:'..userActNum..';perActLim:'..perActLim)
      local curUserActNumInt = tonumber(userActNum)
      userActNumInt =  curUserActNumInt+buyNum
   end
   if userActNumInt > perActLim then
       return '-2'
   end
 end

local goodsUserKey = userId..'_'..skuId
redis.log(redis.LOG_NOTICE,'perSkuLim:'..perSkuLim)
--当前用户已购买此sku多少件
if perSkuLim > 0 then
   local goodsUserNum = redis.call('hget',user_sku_hash,goodsUserKey)
   local goodsUserNumint = 0
   if goodsUserNum == false then
      redis.log(redis.LOG_NOTICE,'goodsUserNum is nil')
      goodsUserNumint = buyNum
   else
      redis.log(redis.LOG_NOTICE,'goodsUserNum:'..goodsUserNum..';perSkuLim:'..perSkuLim)
      local curSkuUserNumint = tonumber(goodsUserNum)
      goodsUserNumint =  curSkuUserNumint+buyNum
   end

   redis.log(redis.LOG_NOTICE,'------goodsUserNumint:'..goodsUserNumint..';perSkuLim:'..perSkuLim)
   if goodsUserNumint > perSkuLim then
       return '-1'
   end
end

--判断是否还有库存满足当前秒杀数量
if skuAmount >= buyNum then
     local decrNum = 0-buyNum
     redis.call('hincrby',sku_amount_hash,skuId,decrNum)
     redis.log(redis.LOG_NOTICE,'second success:'..skuId..'-'..buyNum)

     if perSkuLim > 0 then
         redis.call('hincrby',user_sku_hash,goodsUserKey,buyNum)
     end

     if perActLim > 0 then
         redis.call('hincrby',user_act_hash,userActKey,buyNum)
     end

     local orderKey = userId..'_'..skuId..'_'..buyNum..'_'..orderTime
     local orderStr = '1'
     redis.call('hset',second_log_hash,orderKey,orderStr)

   return orderKey
else
   return '0'
end