package cus.mybatis.enhance.core.lambda;

import java.io.Serializable;

/**
 *Setter method interface definition
 */
@FunctionalInterface
public interface ISetter<T, U> extends Serializable {
    void accept(T t, U u);
}
