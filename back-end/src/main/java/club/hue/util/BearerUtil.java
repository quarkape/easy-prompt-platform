package club.hue.util;

import com.alibaba.fastjson.JSONObject;
import java.util.*;

/**
 * 生成bear
 *
 */
public class BearerUtil {

    /**
     * 根据用户id和shiro的id生成bearer标识字符串
     * @param userid 用户id
     * @param autoToken shiro session，非自己生成
     * @return bearer标识
     */

    public static String generateBearer(String userid, String autoToken) {
        String token = UUID.randomUUID() + "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DATE, calendar.get(calendar.DATE + 1));
        String bearerStr = "{\"token\":\"" + token + "\",\"shsessionid\":\"" + autoToken + "\",\"exp\":" + calendar.getTimeInMillis()  + ",\"userid\":\"" + userid + "\"}";
        String bearer = Base64.getEncoder().encodeToString(bearerStr.getBytes());
        return bearer;
    }

    public static JSONObject decodeBearer(String bearer) {
        String decodedBearer = new String(Base64.getDecoder().decode(bearer.getBytes()));
        JSONObject obj = JSONObject.parseObject(decodedBearer);
        return obj;
    }

}
