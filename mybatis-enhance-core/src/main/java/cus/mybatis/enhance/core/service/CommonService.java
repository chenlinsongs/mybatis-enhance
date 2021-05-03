package cus.mybatis.enhance.core.service;

import cus.mybatis.enhance.core.criteria.Example;
import cus.mybatis.enhance.core.mapper.CommonMapper;
import cus.mybatis.enhance.core.page.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * P 主键类型
 * T entity实体
 * D dao接口
 * */
public class CommonService<P,T,M extends CommonMapper<P,T>> {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 持久层对象
     */
    @Autowired
    protected M mapper;

    public long countByExample(Example example){
        Page page = example.getPage();
        long count;
        if(page != null){
            example.setPage(null);
            count = mapper.countByExample(example);
            example.setPage(page);
        }else {
            count = mapper.countByExample(example);
        }
        return count;
    }

    public long deleteByExample(Example example){
        return mapper.deleteByExample(example);
    }

    public int deleteByPrimaryKey(P id){
        return mapper.deleteByPrimaryKey(id);
    }

    public int insert(T record){
        return mapper.insert(record);
    }

    public int insertSelective(T record){
        return mapper.insertSelective(record);
    }

    public List<T> selectByExample(Example example){
        return mapper.selectByExample(example);
    }

    public T selectByPrimaryKey(P id){
        return mapper.selectByPrimaryKey(id);
    }

    public int updateByExampleSelective(T record, Example example){
        return mapper.updateByExampleSelective(record,example);
    }

    public int updateByExample(T record, Example example){
        return mapper.updateByExample(record,example);
    }

    public int updateByPrimaryKeySelective(T record){
        return mapper.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(T record){
        return mapper.updateByPrimaryKey(record);
    }

}
