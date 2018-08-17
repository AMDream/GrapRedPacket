package com.dream.service.serviceImpl;

import com.dream.dao.RedPacketMapper;
import com.dream.pojo.RedPacket;
import com.dream.service.RedPacketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
public class RedPacketServiceImpl implements RedPacketService {
    @Autowired
    private RedPacketMapper dao = null;
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public RedPacket getRedPacket(Integer id) {
        return dao.selectByPrimaryKey(id);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int decreaseRedPacket(Integer id) {
        return dao.decreaseRedPacket(id);
    }
}
