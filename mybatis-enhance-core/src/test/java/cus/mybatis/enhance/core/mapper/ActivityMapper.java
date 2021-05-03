package cus.mybatis.enhance.core.mapper;

import cus.mybatis.enhance.core.entity.Activity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityMapper{

    List<Activity> get();

}
