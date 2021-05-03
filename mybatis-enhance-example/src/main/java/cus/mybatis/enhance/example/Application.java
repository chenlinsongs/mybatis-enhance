package cus.mybatis.enhance.example;

import cus.mybatis.enhance.example.mapper.ActivityEnhanceExampleMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@MapperScan(value = "cus.mybatis.enhance.example.mapper")
@SpringBootApplication()
public class Application {



    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplication(Application.class).run(args);
        ActivityEnhanceExampleMapper activityEnhanceMapper = context.getBean(ActivityEnhanceExampleMapper.class);
        activityEnhanceMapper.selectByPrimaryKey(1);
    }
}
