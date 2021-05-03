package cus.mybatis.enhance.core;

import cus.mybatis.enhance.core.criteria.Example;
import cus.mybatis.enhance.core.entity.Activity;
import cus.mybatis.enhance.core.mapper.ActivityEnhanceMapper;
import cus.mybatis.enhance.core.mapper.ActivityMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@MapperScan(value = "cus.mybatis.enhance.core.mapper")
@SpringBootTest(classes = {Application.class})
public class ActivityTest {

    Logger logger = LoggerFactory.getLogger(ActivityTest.class);

    @Autowired
    ActivityMapper activityMapper;

    @Autowired
    ActivityEnhanceMapper activityEnhanceMapper;


//    @Test
//    public void propertiesDaoConditionTest(){
//        CommonExample example = new CommonExample(Properties.class);
//        example.createCriteria().andLike(Properties.getFieldName(),"ch%");
//        List list = propertiesDao.selectByExample(example);
//        Assert.assertNotNull(list);
//        Assert.assertTrue(list.size() > 0);
//        logger.info("----++++:"+list.get(0).toString());
//        long count = propertiesDao.countByExample(example);
//        logger.info("count----++++:"+count);
//    }

    @Test
    public void propertiesServiceTest(){
        List list = activityMapper.get();
        Assert.assertNotNull(list);
        Assert.assertTrue(list.size() > 0);
        logger.error("service----++++:"+list.get(0).toString());
    }

    @Test
    public void setActivityMapperTest(){
        Activity activity = activityEnhanceMapper.selectByPrimaryKey(1);
       logger.info("+++++:"+activity.toString());
    }

    @Test
    public void exampleTest(){
        Example<Activity> example = new Example(Activity.class);
        example.createCriteria().andEqualTo(Activity::getId,"1");
        List<Activity> activities = activityEnhanceMapper.selectByExample(example);
        activities.stream().map(Activity::getId).collect(Collectors.toList());
        logger.info("+++++:"+activities.toString());
    }


}
