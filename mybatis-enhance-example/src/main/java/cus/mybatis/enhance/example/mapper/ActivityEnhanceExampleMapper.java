package cus.mybatis.enhance.example.mapper;

import cus.mybatis.enhance.core.annotaion.MyBatisDao;
import cus.mybatis.enhance.core.mapper.CommonMapper;
import cus.mybatis.enhance.example.entity.ActivityExample;

import java.util.List;

@MyBatisDao
public interface ActivityEnhanceExampleMapper extends CommonMapper<Integer, ActivityExample> {
    List<ActivityExample> get();
}
