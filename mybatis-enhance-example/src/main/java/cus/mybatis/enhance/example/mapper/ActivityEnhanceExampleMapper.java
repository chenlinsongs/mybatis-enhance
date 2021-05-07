package cus.mybatis.enhance.example.mapper;

import cus.mybatis.enhance.core.mapper.CommonMapper;
import cus.mybatis.enhance.example.entity.Activity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityEnhanceExampleMapper extends CommonMapper<Integer, Activity> {
    List<Activity> get();
}
