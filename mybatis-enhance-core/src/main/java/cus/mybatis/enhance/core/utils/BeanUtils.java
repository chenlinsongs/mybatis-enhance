package cus.mybatis.enhance.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cus.mybatis.enhance.core.lambda.IGetter;
import cus.mybatis.enhance.core.lambda.ISetter;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeanUtils {

    static Logger Log = LoggerFactory.getLogger(BeanUtils.class);

    /**
     *The mapping of cache class lambda
     */
    private static Map<Class, SerializedLambda> CLASS_LAMBDA_CACHE = new ConcurrentHashMap<>();

    /***
     *Convert method reference to property name
     * @param fn
     * @return
     */
    public static String convertToFieldName(IGetter fn) {
        SerializedLambda lambda = getSerializedLambda(fn);
        String methodName = lambda.getImplMethodName();
        String prefix = null;
        if(methodName.startsWith("get")){
            prefix = "get";
        }
        else if(methodName.startsWith("is")){
            prefix = "is";
        }
        if(prefix == null){
            throw new RuntimeException("invalid getter method:" + methodName);
        }
        return uncapFirst(substringAfter(methodName, prefix));
    }

    /**
     * 获取方法所在的类名称
     * */
    public static String getImplClassName(IGetter fn){
        SerializedLambda lambda = getSerializedLambda(fn);
        String implClassName = lambda.getImplClass();
        return implClassName.replace('/','.');
    }

    /***
     *Convert setter method reference to property name
     * @param fn
     * @return
     */
    public static <T,R> String convertToFieldName(ISetter<T,R> fn) {
        SerializedLambda lambda = getSerializedLambda(fn);
        String methodName = lambda.getImplMethodName();
        if(!methodName.startsWith("set")){
            Log.warn ("invalid setter method:" + methodName);
        }
        return uncapFirst(substringAfter(methodName, "set"));
    }

    /***
     *Get the lambda corresponding to the class
     * @param fn
     * @return
     */
    private static SerializedLambda getSerializedLambda(Serializable fn){
        //Check whether the cache already exists
        SerializedLambda lambda = CLASS_LAMBDA_CACHE.get(fn.getClass());
        if(lambda == null){
            synchronized (fn){
                lambda = CLASS_LAMBDA_CACHE.get(fn.getClass());
                if (lambda == null){
                    try {// extract serializedLambda and cache
                        Method method = fn.getClass().getDeclaredMethod("writeReplace");
                        method.setAccessible(Boolean.TRUE);
                        lambda = (SerializedLambda) method.invoke(fn);
                        CLASS_LAMBDA_CACHE.put(fn.getClass(), lambda);
                    }
                    catch (Exception e){
                        Log.error ("get serializedLambda exception, class =" + fn.getClass().getSimpleName(), e);
                    }
                }
            }
        }
        return lambda;
    }

    private static String substringAfter(String target,String prefix){
        return target.substring(prefix.length());
    }

    private static String uncapFirst(String target){
        return target.substring(0,1).toLowerCase()+target.substring(1);
    }

}

