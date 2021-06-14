package cus.mybatis.enhance.core.service;

import cus.mybatis.enhance.core.criteria.Example;
import cus.mybatis.enhance.core.mapper.CommonMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * P 主键类型
 * T entity实体
 * D dao接口
 * */
public abstract class AbstractService<P,T,M extends CommonMapper<P,T>> implements CommonService<P,T>{

    /**
     * 持久层对象
     */
    @Autowired
    protected M mapper;

    @Override
    public long countByExample(Example example) {
        return mapper.countByExample(example);
    }

    @Override
    public long deleteByExample(Example example){
        return mapper.deleteByExample(example);
    }

    @Override
    public int deleteByPrimaryKey(P id){
        return mapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(T record){
        return mapper.insert(record);
    }

    @Override
    public int insertSelective(T record){
        return mapper.insertSelective(record);
    }

    @Override
    public List<T> selectByExample(Example example){
        return mapper.selectByExample(example);
    }

    @Override
    public T selectByPrimaryKey(P id){
        return mapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByExampleSelective(T record, Example example){
        return mapper.updateByExampleSelective(record,example);
    }

    @Override
    public int updateByExample(T record, Example example){
        return mapper.updateByExample(record,example);
    }

    @Override
    public int updateByPrimaryKeySelective(T record){
        return mapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(T record){
        return mapper.updateByPrimaryKey(record);
    }

}
