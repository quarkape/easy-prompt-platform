package club.hue.util;

public class CaptchaGenerator {

    private static final int captchaLen = 6;

    public static String generateCaptcha() {
        String characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String captcha = "";
        for (int i=0;i<captchaLen;i++) {
            int pos = (int) Math.floor(Math.random()*62);
            captcha = captcha + characters.charAt(pos);
        }
        return captcha;
    }
}
