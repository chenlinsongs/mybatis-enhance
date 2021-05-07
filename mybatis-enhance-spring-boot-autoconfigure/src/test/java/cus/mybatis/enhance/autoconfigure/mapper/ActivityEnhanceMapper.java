package cus.mybatis.enhance.autoconfigure.mapper;

import cus.mybatis.enhance.autoconfigure.entity.Activity;
import cus.mybatis.enhance.core.annotation.MyBatisDao;
import cus.mybatis.enhance.core.mapper.CommonMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@MyBatisDao
public interface ActivityEnhanceMapper extends CommonMapper<Integer, Activity> {
    List<Activity> get();

    void updateById(@Param(value = "id") int id);
}
