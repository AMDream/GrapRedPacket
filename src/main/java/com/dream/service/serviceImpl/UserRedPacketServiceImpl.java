package com.dream.service.serviceImpl;

import com.dream.dao.RedPacketMapper;
import com.dream.dao.UserRedPacketMapper;
import com.dream.pojo.RedPacket;
import com.dream.pojo.UserRedPacket;
import com.dream.service.RedisRedPacketService;
import com.dream.service.UserRedPacketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import redis.clients.jedis.Jedis;


@Service
public class UserRedPacketServiceImpl implements UserRedPacketService {
    @Autowired
    private UserRedPacketMapper userDao = null;
    @Autowired
    private RedPacketMapper redDao = null;
    @Autowired
    private RedisTemplate redisTemplate = null;
    @Autowired
    private RedisRedPacketService redisRedPacketService = null;

    private static final Integer FAILED = 0;
    private static final Integer SUCCESS = 1;

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int grapRedPacket(Integer redPacketId, Integer userId) {
        //获取红包信息
//        RedPacket redPacket = redDao.selectByPrimaryKey(redPacketId);
        //加锁获取红包信息
        RedPacket redPacket = redDao.getRedPacketForUpdate(redPacketId);
        if (redPacket.getStock() > 0){
            redDao.decreaseRedPacket(redPacketId);
            //生成抢红包信息
            UserRedPacket userRedPacket = new UserRedPacket();
            userRedPacket.setRedPacketId(redPacketId);
            userRedPacket.setUserId(userId);
            userRedPacket.setAmount(redPacket.getUnitAmount());
            userRedPacket.setNote("抢红包"+redPacketId);
            int result = userDao.grapRedPacket(userRedPacket);
            return result;
        }
        //失败
        return FAILED;
    }

    /**
     * 使用乐观锁抢红包，
     * 失败率较高
     */
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int grapRedPacketForVersion(Integer redPacketId,Integer userId){
        RedPacket redPacket = redDao.selectByPrimaryKey(redPacketId);
        if (redPacket.getStock() > 0){
            int update = redDao.decreaseRedPacketForVersion(redPacketId,redPacket.getVersion());
            //如果没有数据更新，则说明其他线程已经修改过数据，本次抢红包失败
            if (update == 0)
                return FAILED;
            //生成红包信息
            UserRedPacket userRedPacket = new UserRedPacket();
            userRedPacket.setRedPacketId(redPacketId);
            userRedPacket.setUserId(userId);
            userRedPacket.setAmount(redPacket.getUnitAmount());
            userRedPacket.setNote("抢红包"+redPacketId);
            int result = userDao.grapRedPacket(userRedPacket);
            return result;
        }
        return FAILED;
    }
    /**
     * 乐观锁：失败使用补偿策略：按时间戳重入
     * 或者次数
     * 成功率是高了，但是时间长了....
     */
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int grapRedPacketForVersionWithTime(Integer redPacketId,Integer userId){
        long begin = System.currentTimeMillis();
        while (true) {
            long end = System.currentTimeMillis();
            //尝试超过100hs将退出
            if (end - begin > 100)
                return FAILED;
            RedPacket redPacket = redDao.selectByPrimaryKey(redPacketId);
            if (redPacket.getStock() > 0){
                int update = redDao.decreaseRedPacketForVersion(redPacketId,redPacket.getVersion());
                if (update == 0)
                    continue;
                //生成抢红包信息
                UserRedPacket urp = new UserRedPacket();
                urp.setRedPacketId(redPacketId);
                urp.setUserId(userId);
                urp.setAmount(redPacket.getUnitAmount());
                urp.setNote("抢红包"+redPacketId);
                //插入抢红包信息
                int result = userDao.grapRedPacket(urp);
                return result;
            }else //一旦没有库存，则马上返回
                return FAILED;
        }

    }

    //lua脚本
    String script = "local listKey = 'red_packet_list_'..KEYS[1]\n"
            +"local redPacket = 'red_packet_'..KEYS[1]\n"
            +"local stock = tonumber(redis.call('hget',redPacket,'stock'))\n"
            +"if stock <= 0 then return 0 end\n"
            +"stock = stock - 1\n"
            +"redis.call('hset',redPacket,'stock',tostring(stock))\n"
            +"redis.call('rpush',listKey,ARGV[1])\n"
            +"if stock == 0 then return 2 end \n"
            +"return 1\n";
    //在缓存Lua脚本后，使用该变量保存redis返回的32位SHA1编码，使用它去执行缓存的lua脚本
    String sha1 = null;

    @Override
    public Long grapRedPacketByRedis(Integer redPacketId, Integer userId) {
        String args = userId+"-"+System.currentTimeMillis();
        Long result = null;
        //获取底层Redis操作对象
        Jedis jedis = (Jedis) redisTemplate.getConnectionFactory().getConnection().getNativeConnection();
        try{
            if (sha1 == null){
                //如果脚本没有加载过，那么进行加载，这样就返回一个sha1编码
                sha1 = jedis.scriptLoad(script);
            }
            //执行脚本，返回结果
            Object res = jedis.evalsha(sha1,1,redPacketId+"",args);
            result = (Long)res;
            //返回2时最后一个红包，此时将红包信息通过异步保存到数据库中
            if (result == 2){
                //获取单个小额红包
                String unitAmountStr = jedis.hget("red_packet_"+redPacketId,"unit_amount");
                Integer unitAmount = Integer.parseInt(unitAmountStr);
                System.out.println("thread_name="+Thread.currentThread().getName());
                redisRedPacketService.saveUserRedPacketByRedis(redPacketId,unitAmount);
            }
        }finally {
            if (jedis != null && jedis.isConnected())
                jedis.close();
        }
        return result;
    }
}
