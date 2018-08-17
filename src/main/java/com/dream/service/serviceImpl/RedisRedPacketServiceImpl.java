package com.dream.service.serviceImpl;

import com.dream.pojo.UserRedPacket;
import com.dream.service.RedisRedPacketService;
import org.apache.log4j.helpers.DateTimeDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Service
public class RedisRedPacketServiceImpl implements RedisRedPacketService {
    private static final String PREFIX = "red_packet_list_";
    //每次取出1000条，避免一次取出消耗太多内存
    private static final int TIME_SIZE = 1000;
    @Autowired
    private RedisTemplate redisTemplate = null;
    @Autowired
    private DataSource dataSource = null;

    //开启新线程运行
    @Async
    public void saveUserRedPacketByRedis(Integer redPacketId, Integer unitAmount) {
        System.out.println("开始保存数据");
        long begin = System.currentTimeMillis();
        BoundListOperations ops = redisTemplate.boundListOps(PREFIX+redPacketId);
        long size = ops.size();
        long times = size % TIME_SIZE == 0 ?size / TIME_SIZE : size/TIME_SIZE+1;
        int count = 0;
        List<UserRedPacket> urps = new ArrayList<>(TIME_SIZE);
        for (int i = 0; i < times; i++) {
            //获取至多TIME_SIZE个抢红包信息
            List userIdList = null;
            if (i == 0)
                userIdList = ops.range(i * TIME_SIZE,(i+1)*TIME_SIZE);
            else
                userIdList = ops.range(i * TIME_SIZE + 1,(i+1)*TIME_SIZE);
            urps.clear();
            //保存红包信息
            for (int j = 0; j < userIdList.size(); j++) {
                String args = userIdList.get(j).toString();
                String[] arr = args.split("-");
                String userIdStr = arr[0];
                String timeStr = arr[1];
                Integer userId = Integer.parseInt(userIdStr);
                Long time = Long.parseLong(timeStr);
                //生成抢红包信息
                UserRedPacket urp = new UserRedPacket();
                urp.setRedPacketId(redPacketId);
                urp.setUserId(userId);
                urp.setAmount(unitAmount);
                urp.setGrabTime(new Timestamp(time));
                urp.setNote("抢红包"+redPacketId);
                urps.add(urp);
            }
            count += executeBatch(urps);
        }
        redisTemplate.delete(PREFIX+redPacketId);
        long end = System.currentTimeMillis();
        System.out.println("保存数据结束，耗时"+(end - begin)+"毫秒，共"+count+"条记录被保存");
    }

    private int executeBatch(List<UserRedPacket> urps) {
        Connection con = null;
        Statement stmt = null;
        int[] cnt = null;
        try{
            con = dataSource.getConnection();
            con.setAutoCommit(false);
            stmt = con.createStatement();
            for (UserRedPacket urp : urps){
                String sql = "update t_red_packet set stock = stock - 1 where id ="+urp.getRedPacketId();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String sql2 = "insert into t_user_red_packet(red_packet_id,user_id,amount,grab_time,note) values("+urp.getRedPacketId()+","+
                        urp.getUserId()+","+urp.getAmount()+",'"+df.format(urp.getGrabTime())+"'"+",'"+urp.getNote()+"')";
                stmt.addBatch(sql);
                stmt.addBatch(sql2);
            }
            cnt = stmt.executeBatch();
            con.commit();
        }catch (SQLException e){
            throw new RuntimeException("抢红包批量执行程序错误");
        }finally {
            try{
                if (con != null && !con.isClosed())
                    con.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        return cnt.length/2;
    }
}
