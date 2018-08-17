package com.dream.service;

import com.dream.pojo.RedPacket;

public interface RedPacketService {
    RedPacket getRedPacket(Integer id);
    int decreaseRedPacket(Integer id);
}
