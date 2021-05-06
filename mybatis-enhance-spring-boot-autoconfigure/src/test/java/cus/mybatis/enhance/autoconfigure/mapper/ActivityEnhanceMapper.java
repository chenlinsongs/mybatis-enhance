package cus.mybatis.enhance.autoconfigure.mapper;

import cus.mybatis.enhance.autoconfigure.entity.Activity;
import cus.mybatis.enhance.core.annotaion.MyBatisDao;
import cus.mybatis.enhance.core.mapper.CommonMapper;

@MyBatisDao
public interface ActivityEnhanceMapper extends CommonMapper<Integer, Activity> {
}
