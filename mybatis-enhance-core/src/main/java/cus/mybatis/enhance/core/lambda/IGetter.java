package cus.mybatis.enhance.core.lambda;

import java.io.Serializable;

/**
 *Getter method interface definition
 */
@FunctionalInterface
public interface IGetter<T> extends Serializable {
    Object apply(T source);
}
