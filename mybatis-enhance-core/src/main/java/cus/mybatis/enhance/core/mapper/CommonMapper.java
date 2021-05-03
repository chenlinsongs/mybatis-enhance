
package cus.mybatis.enhance.core.mapper;

import org.apache.ibatis.annotations.Param;
import cus.mybatis.enhance.core.criteria.Example;

import java.util.List;

/**
 * 公共的dao
 */
public interface CommonMapper<P,T>{

    long countByExample(Example example);

    long deleteByExample(Example example);

    int deleteByPrimaryKey(P id);

    int insert(Object object);

    int insertSelective(Object record);

    List<T> selectByExample(Example example);

    T selectByPrimaryKey(P id);

    int updateByExampleSelective(@Param("record") Object record, @Param("example") Example example);

    int updateByExample(@Param("record") Object record, @Param("example") Example example);

    int updateByPrimaryKeySelective(Object record);

    int updateByPrimaryKey(Object record);
}