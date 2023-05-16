package club.hue.util;

import org.springframework.data.redis.core.RedisTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.HashMap;

/**
 * 从bearer里面提取userid并在redis里面进行比对
 */

public class CheckAuthorizationUtil {

    public static String check(HttpServletRequest req, RedisTemplate<String, Serializable> redis) {
        String bearer = req.getHeader("bearer");
        String userid= new BearerUtil().decodeBearer(bearer).getString("userid");
        HashMap<String, Object> userMap = (HashMap<String, Object>) redis.opsForValue().get(bearer);
        if (!userid.equals(userMap.get("userid"))) {
            return null;
        }
        return userid;
    }
}
