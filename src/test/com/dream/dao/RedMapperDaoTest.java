package com.dream.dao;

import com.dream.config.RedPacketWebAppInitializer;
import com.dream.config.RootConfig;
import com.dream.config.WebConfig;
import com.dream.pojo.RedPacket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class) //使用junit4进行测试
@ContextConfiguration(classes = {RootConfig.class, WebConfig.class})
public class RedMapperDaoTest extends AbstractJUnit4SpringContextTests {
    @Autowired
    private RedPacketMapper mapper = null;

    @Test
    public void testRedMapper(){
        Integer id = 1;
        RedPacket rr = mapper.selectByPrimaryKey(id);
        System.out.println("耿"+rr);
    }
}
