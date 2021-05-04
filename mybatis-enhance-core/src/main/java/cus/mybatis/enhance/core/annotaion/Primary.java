package cus.mybatis.enhance.core.annotaion;

import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by chenlinsong on 2018/8/13.
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
@Component
public @interface Primary{
    String name();
}
