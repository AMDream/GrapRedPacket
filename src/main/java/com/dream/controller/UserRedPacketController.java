package com.dream.controller;

import com.dream.service.UserRedPacketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Controller
@RequestMapping("/userRedPacket")
public class UserRedPacketController {
    @Autowired
    private UserRedPacketService urs = null;

    /**
     * 不加锁或者加行级锁抢红包
     */
    @RequestMapping("/grapRedPacket")
    @ResponseBody
    public Map<String,Object> grapRedPacket(Integer redPacketId,Integer userId){
        int result = urs.grapRedPacket(redPacketId,userId);
        boolean flag = result > 0;
        Map<String,Object> retMap = new HashMap<>();
        retMap.put("success",flag);
        retMap.put("message",flag?"抢红包成功":"抢红包失败");
        return retMap;
    }

    /**
     * 使用乐观锁抢红包
     */
    @RequestMapping("/grapRedPacketForVersion")
    @ResponseBody
    public Map<String,Object> grapRedPacketForVersion(Integer redPacketId,Integer userId){
//        int result = urs.grapRedPacketForVersion(redPacketId,userId);
        //乐观锁使用时间戳
        int result = urs.grapRedPacketForVersionWithTime(redPacketId,userId);
        boolean flag = result > 0;
        Map<String,Object> retMap = new HashMap<>();
        retMap.put("success",flag);
        retMap.put("message",flag?"抢红包成功":"抢红包失败");
        return retMap;
    }
    /**
     * Redis实现抢红包
     */
    @RequestMapping("/grapRedPacketByRedis")
    @ResponseBody
    public Map<String,Object> grapRedPacketByRedis(Integer redPacketId,Integer userId){
        Long result = urs.grapRedPacketByRedis(redPacketId,userId);
        boolean flag = result > 0;
        Map<String,Object> retMap = new HashMap<>();
        retMap.put("success",flag);
        retMap.put("message",flag?"抢红包成功":"抢红包失败");
        return retMap;
    }
}
