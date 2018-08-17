package com.dream.service;

/**
 *
 */
public interface UserRedPacketService {
    int grapRedPacket(Integer redPacketId,Integer userId);
    int grapRedPacketForVersion(Integer redPacketId,Integer userId);
    //乐观锁，失败重试10hs
    int grapRedPacketForVersionWithTime(Integer redPacketId,Integer userId);
    Long grapRedPacketByRedis(Integer redPacketId,Integer userId);
}
