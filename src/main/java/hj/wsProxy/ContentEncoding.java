package hj.wsProxy;

import org.springframework.util.StringUtils;

/**
 * Created by heiko on 26.07.15.
 */
public enum ContentEncoding {

    GZIP,
    DEFLATE,
    NONE;


    public static ContentEncoding forValue(String value) {
        if(StringUtils.isEmpty(value)) return NONE;
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            return NONE;
        }
    }

}
