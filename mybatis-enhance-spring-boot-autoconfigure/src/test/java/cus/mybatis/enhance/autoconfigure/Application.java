package cus.mybatis.enhance.autoconfigure;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan(value = "cus.mybatis.enhance.core.mapper")
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        new SpringApplication(Application.class).run(args);
    }
}
