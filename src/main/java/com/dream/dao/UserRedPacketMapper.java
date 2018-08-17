package com.dream.dao;

import com.dream.pojo.UserRedPacket;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRedPacketMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(UserRedPacket record);

    int insertSelective(UserRedPacket record);

    UserRedPacket selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserRedPacket record);

    int updateByPrimaryKey(UserRedPacket record);
    /**
     * 插入抢红包信息
     */
    int grapRedPacket(UserRedPacket userRedPacket);
}