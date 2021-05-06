package cus.mybatis.enhance.autoconfigure.mapper;

import cus.mybatis.enhance.autoconfigure.entity.Activity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityMapper{

    List<Activity> get();

}
