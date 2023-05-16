package club.hue.conf;

import club.hue.util.BearerUtil;
import club.hue.vo.ResultVOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * 拦截器，主要作用有两个
 * 第一个作用是解决跨域问题，放行options请求
 * 第二个作用是对用户token进行检验，与redis建立通信
 * @author huewhom
 * @date 20209/11/5
 */
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate<String, Serializable> redis;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 因为每一次跨域请求会有两次请求，所以第一次options请求应该将状态码设置为200，才能够保证下一次正常的请求请求到数据
        if (request.getMethod().equals(RequestMethod.OPTIONS.name())) {
            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
            response.setHeader("Access-Control-Allow-Methods", request.getMethod());
            response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
            response.setStatus(HttpStatus.OK.value());
            return false;
        }

        String bearer = request.getHeader("bearer");

        if (bearer != null && redis.hasKey(bearer)) {
            redis.expire(bearer, 1, TimeUnit.DAYS);
            return true;
        }
        // 没有携带token
        response.setStatus(402);
        return false;
    }
}

