package cus.mybatis.enhance.core.service;

import cus.mybatis.enhance.core.criteria.Example;

import java.util.List;

public interface CommonService<P,T> {

    long countByExample(Example example);

    long deleteByExample(Example example);

    int deleteByPrimaryKey(P id);

    int insert(T record);

    int insertSelective(T record);

    List<T> selectByExample(Example example);

    T selectByPrimaryKey(P id);

    int updateByExampleSelective(T record, Example example);

    int updateByExample(T record, Example example);

    int updateByPrimaryKeySelective(T record);

    int updateByPrimaryKey(T record);
}
