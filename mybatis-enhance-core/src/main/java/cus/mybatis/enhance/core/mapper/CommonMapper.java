
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

    int deleteByPrimaryKey(@Param("primaryValue")P id);

    int insert(T object);

    int insertSelective(T record);

    List<T> selectByExample(Example example);

    T selectByPrimaryKey(@Param("primaryValue")P id);

    int updateByExampleSelective(@Param("record") T record, @Param("example") Example example);

    int updateByExample(@Param("record") T record, @Param("example") Example example);

    int updateByPrimaryKeySelective(T record);

    int updateByPrimaryKey(T record);
}