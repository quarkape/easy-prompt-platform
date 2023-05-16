package club.hue.conf;

import club.hue.util.BearerUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;

/**
 *      目的: shiro 的 session 管理
 *      自定义session规则，实现前后分离，在跨域等情况下使用token方式进行登录验证才需要，否则没必须使用本类。
 *      shiro默认使用 ServletContainerSessionManager 来做 session 管理，它是依赖于浏览器的 cookie 来维护 session 的,
 *      调用 storeSessionId 方法保存 sessionId 到 cookie 中
 *      为了支持无状态会话，我们就需要继承 DefaultWebSessionManager
 *      自定义生成 sessionId 则要实现 SessionIdGenerator
 *
 */
public class ShiroSession extends DefaultWebSessionManager {
    /**
     * 定义的请求头中使用的标记key，用来传递 token
     */
    private static final String BEARER_TOKEN = "bearer";
    private static final String REFERENCED_SESSION_ID_SOURCE = "Stateless request";

    public ShiroSession() {
        super();
        //设置 shiro session 失效时间
        setGlobalSessionTimeout(MILLIS_PER_HOUR * 24);
    }
    /**
     * 获取sessionId，原本是根据sessionKey来获取一个sessionId
     * 重写的部分多了一个把获取到的token设置到request的部分。这是因为app调用登陆接口的时候，是没有token的，登陆成功后，产生了token,我们把它放到request中，返回结
     * 果给客户端的时候，把它从request中取出来，并且传递给客户端，客户端每次带着这个token过来，就相当于是浏览器的cookie的作用，也就能维护会话了
     * @param request ServletRequest
     * @param response ServletResponse
     * @return Serializable
     */
    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        // 获取请求头中的 AUTH_TOKEN 的值，如果请求头中有 AUTH_TOKEN 则其值为sessionId。shiro就是通过sessionId 来控制的
        String bearer = WebUtils.toHttp(request).getHeader(BEARER_TOKEN);

        if (StringUtils.isEmpty(bearer)){
            // 如果没有携带id参数则按照父类的方式在cookie进行获取sessionId
            return super.getSessionId(request, response);
        } else {
            // 请求头中如果有 authToken, 则其值为sessionId
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE, REFERENCED_SESSION_ID_SOURCE);
            // sessionId
            JSONObject obj = BearerUtil.decodeBearer(bearer);
            String sessionId = obj.getString("shsessionid");
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, sessionId);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);
            return sessionId;
        }
    }
}

