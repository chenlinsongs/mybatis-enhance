package cus.mybatis.enhance.core.utils;

public class StringUtil {

    public static String camelCaseToUnderscores(String camel){
        String underscore;
        underscore = String.valueOf(Character.toLowerCase(camel.charAt(0)));
        for (int i = 1; i < camel.length(); i++) {
            underscore += Character.isLowerCase(camel.charAt(i)) ? String.valueOf(camel.charAt(i))
                    : "_" + Character.toLowerCase(camel.charAt(i));
        }
        return underscore;
    }
}
