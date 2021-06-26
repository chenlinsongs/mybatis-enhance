package cus.mybatis.enhance.autoconfigure;

import cus.mybatis.enhance.autoconfigure.entity.Activity;
import cus.mybatis.enhance.autoconfigure.mapper.ActivityEnhanceMapper;
import cus.mybatis.enhance.autoconfigure.mapper.ActivityMapper;
import cus.mybatis.enhance.core.criteria.Example;
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
@MapperScan(value = "cus.mybatis.enhance.autoconfigure.mapper")
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
        Example<Activity> example = new Example();
        example.createCriteria().andEqualTo(Activity::getId,"1");
        List<Activity> activities = activityEnhanceMapper.selectByExample(example);
        activities.stream().map(Activity::getId).collect(Collectors.toList());
        logger.info("+++++:"+activities.toString());
    }

    @Test
    public void insertTest(){
        Activity activity = new Activity();
        activity.setId(3);
        activity.setName("23");
        activityEnhanceMapper.insert(activity);
        System.out.println();

    }

    @Test
    public void insertSelectiveTest(){
        Activity activity = new Activity();
        activity.setName("23");
        activityEnhanceMapper.insertSelective(activity);
        System.out.println();
    }

    @Test
    public void updateSelectiveByExampleTest(){
        Activity activity = new Activity();
        activity.setName("23999");
        Example<Activity> example = new Example();
        example.createCriteria().andEqualTo(Activity::getId,"4");

        activityEnhanceMapper.updateByExampleSelective(activity,example);
        System.out.println();
    }

    @Test
    public void countByExampleTest(){
        Example<Activity> example = new Example();
        example.createCriteria().andEqualTo(Activity::getId,"4");

        long count = activityEnhanceMapper.countByExample(example);
        System.out.println("count:"+count);
    }

    @Test
    public void deleteByPrimaryKeyTest(){
       int row = activityEnhanceMapper.deleteByPrimaryKey(4);
       assert row == 1;
    }

    @Test
    public void deleteByExampleKeyTest(){
        Example<Activity> example = new Example();
        example.createCriteria().andEqualTo(Activity::getName,"23");
        long row = activityEnhanceMapper.deleteByExample(example);
    }

    @Test
    public void selectByExampleKeyTest(){
        Example<Activity> example = new Example();
//        example.createCriteria().andEqualTo(Activity::getName,"244");
        example.setOrderByClause("id desc");
        List<Activity> activities = activityEnhanceMapper.selectByExample(example);
        System.out.println("++:"+activities.toString());
    }

    @Test
    public void updateByExampleSqlTest(){
        Example<Activity> example = new Example();
        example.createCriteria().andEqualTo(Activity::getId,"3");

        Activity activity = new Activity();
        activity.setId(3);
        activity.setName("abc");
        activityEnhanceMapper.updateByExample(activity,example);
    }

    @Test
    public void updateByPrimarySelectiveSqlTest(){
        Activity activity = new Activity();
        activity.setId(3);
        activity.setName("abc1234");
        activityEnhanceMapper.updateByPrimaryKeySelective(activity);
    }

    @Test
    public void updateByPrimaryKeySqlTest(){
        Activity activity = new Activity();
        activity.setId(3);
        activity.setName("abc123445");
        activityEnhanceMapper.updateByPrimaryKey(activity);
    }

    @Test
    public void customGetSqlTest(){
       List<Activity> activities = activityEnhanceMapper.get();
       if (activities != null){
           logger.info(activities.toString());
       }
    }

    @Test
    public void customUpdateSqlTest(){
        activityEnhanceMapper.updateById(3);
    }





}
