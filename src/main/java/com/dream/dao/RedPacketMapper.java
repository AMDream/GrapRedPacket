package com.dream.dao;

import com.dream.pojo.RedPacket;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RedPacketMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(RedPacket record);

    int insertSelective(RedPacket record);

    int updateByPrimaryKeySelective(RedPacket record);

    int updateByPrimaryKey(RedPacket record);
    /**
     * 获取红包信息
     */
    RedPacket selectByPrimaryKey(Integer id);
    /**
     * 加锁获取红包信息
     */
    RedPacket getRedPacketForUpdate(Integer id);
    /**
     * 扣减抢红包数
     */
    int decreaseRedPacket(Integer id);
    /**
     * 乐观锁扣减红包数
     */
    int decreaseRedPacketForVersion(@Param("id") Integer id, @Param("version") Integer version);
}