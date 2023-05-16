package club.hue.util;

import javax.servlet.http.HttpServletRequest;

public class GetLoginInfoUtil {

    public static String[] getLoginInfo(HttpServletRequest req) {
        // 获取真实IP地址
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0) {
            ip = req.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0) {
            ip = req.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0) {
            ip = req.getRemoteAddr();
        }
        ip = "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;

        // 获取设备类型
        String ua = req.getHeader("User-Agent").toUpperCase();
        String[] deviceTypes = {"ANDROID", "WINDOWS", "IPAD", "IPHONE", "MAC"};
        String deviceType = "other";
        for (String type : deviceTypes) {
            if (ua.contains(type.toUpperCase())) {
                deviceType = type;
            }
        }

        return new String[]{ip, deviceType};
    }

}
