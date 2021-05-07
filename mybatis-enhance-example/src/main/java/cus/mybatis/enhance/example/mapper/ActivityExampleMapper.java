package cus.mybatis.enhance.example.mapper;

import cus.mybatis.enhance.example.entity.Activity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityExampleMapper {

    List<Activity> get();

}
