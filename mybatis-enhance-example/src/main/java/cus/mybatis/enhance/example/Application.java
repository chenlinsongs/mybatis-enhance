package cus.mybatis.enhance.example;

import cus.mybatis.enhance.example.entity.ActivityExample;
import cus.mybatis.enhance.example.mapper.ActivityEnhanceExampleMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

@MapperScan(value = "cus.mybatis.enhance.example.mapper")
@SpringBootApplication()
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplication(Application.class).run(args);
        ActivityEnhanceExampleMapper activityEnhanceMapper = context.getBean(ActivityEnhanceExampleMapper.class);
        ActivityExample activityExample = activityEnhanceMapper.selectByPrimaryKey(3);
        if (activityExample != null){
            System.out.println("_____:"+activityExample.toString());
        }
        List<ActivityExample> activityExamples = activityEnhanceMapper.get();
        System.out.println("+++++:"+activityExamples.toString());
    }
}
