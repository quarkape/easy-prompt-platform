package club.hue.controller;

import club.hue.mapper.LoginMapper;
import club.hue.util.*;
import club.hue.vo.ResultVO;
import club.hue.vo.ResultVOUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Result;
import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class LoginController {

    @Autowired
    private LoginMapper loginMapper;

    @Autowired
    private RedisTemplate<String, Serializable> redis;

    @RequestMapping("/login")
    public ResultVO login(String loginToken, HttpServletRequest req) {
        String decodedLoginToken = new String(Base64.getDecoder().decode(loginToken));
        JSONObject obj = JSONObject.parseObject(decodedLoginToken);
        String encodedPassword = DigestUtils.md5DigestAsHex(obj.getString("password").getBytes());

        // 判断用户名类型: id | email
        String username = obj.getString("username");
        Pattern pattern1 = Pattern.compile("^[a-zA-Z0-9.*_-]+@[a-zA-Z0-9.*_-]+(\\.[a-zA-Z0-9_-]+)+[a-zA-Z]+$");
        Matcher matcher1 = pattern1.matcher(username);
        Pattern pattern2 = Pattern.compile("\\d{6}");
        Matcher matcher2 = pattern2.matcher(username);
        HashMap<String, Object> user = null;

        if (matcher1.matches()) {
            // 使用邮箱登录
            user = loginMapper.getUser(null, username, encodedPassword);
        } else if (matcher2.matches()) {
            // 使用ID登录
            user = loginMapper.getUser(username, null, encodedPassword);
        }
        if (user == null) {
            return ResultVOUtil.err(1, "用户名或密码有误");
        }


        String userid = (String) user.get("userid");
        String rolenum = String.valueOf(user.get("rolenum"));

        // 新增一条登录记录
        String loginInfo[] = GetLoginInfoUtil.getLoginInfo(req);
        loginMapper.addLoginRecord(loginInfo[0], userid, loginInfo[1]);

        // 用户存在，在shiro上注册用户
        Subject currentUser = SecurityUtils.getSubject();

        // 用户角色+用户id
        UsernamePasswordToken userToken = new UsernamePasswordToken(userid, encodedPassword, rolenum);
        try {
            currentUser.login(userToken);

            String authToken = (String) currentUser.getSession().getId();
            String bearer = BearerUtil.generateBearer(userid, authToken);

            HashMap<String, Object> authMap = new HashMap<>();
            authMap.put("userid", userid);
            authMap.put("rolenum", rolenum);
            authMap.put("bearer", bearer);
            redis.opsForValue().set(bearer, authMap, Duration.ofDays(1L));

            HashMap<String, Object> map = new HashMap<>();
            map.put("bearer", bearer);
            map.put("rolenum", rolenum);
            return ResultVOUtil.ok(map);

            // 处理用户名和密码检验中的各种异常情况
        } catch (Exception e) {
            return ResultVOUtil.err(1, "认证出错，请联系管理员");
        }
    }

    @RequestMapping("/auto-login")
    public ResultVO autoLogin(String bearer, HttpServletRequest req) {
        if (bearer == null) {
            return ResultVOUtil.err(2, "会话过期");
        }
        if (redis.hasKey(bearer)) {
            // 判断bearer是否有效
            JSONObject obj = BearerUtil.decodeBearer(bearer);
            String userid = obj.getString("userid");
            if (userid == null) {
                return ResultVOUtil.err(3, "会话无效");
            }
            // 新增一条登录记录
            String loginInfo[] = GetLoginInfoUtil.getLoginInfo(req);
            loginMapper.addLoginRecord(loginInfo[0], userid, loginInfo[1]);
            // 重置bearer有效期时间
            redis.expire(bearer, 1, TimeUnit.DAYS);
            return ResultVOUtil.oks();
        }
        return ResultVOUtil.err(1, "会话过期");
    }

    @RequestMapping("/personal-profile")
    public ResultVO getPersonalProfile(HttpServletRequest request) {
        String userid = CheckAuthorizationUtil.check(request, redis);
        if (userid == null) {
            return ResultVOUtil.err(1, "未登录或已过期");
        }
        HashMap<String, String> map = loginMapper.getUserProfile(userid);
        return ResultVOUtil.ok(map);
    }

    @RequestMapping("/logout")
    public ResultVO logout(HttpServletRequest request) {
        String bearer = request.getHeader("bearer");
        if (bearer != null) {
            redis.delete(bearer);
        }
        return ResultVOUtil.oks();
    }

    /**
     * 注册获取验证码
     */
    @RequestMapping("/get-captcha")
    public ResultVO getCaptcha(String username, String password, String email) {
        HashMap<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", password);
        map.put("email", email);
        String captcha = CaptchaGenerator.generateCaptcha();
        map.put("captcha", captcha);
        String registerToken = UUID.randomUUID() + "";
        redis.opsForValue().set(registerToken, map,  Duration.ofMinutes(3));
        // 发送邮件
        String message = "<p>欢迎注册使用Easy Prompt。</p><p>你的注册随机验证码是：</p><p><b>" + captcha + "</b></p><p>验证码3分钟内有效，请尽快使用</p>";
        try {
            SendMailUtil.sendEmail(email, message, "注册 Easy Prompt 验证码");
        } catch(Exception e) {
            return ResultVOUtil.err(1, "邮件发送失败");
        }
        return ResultVOUtil.ok(registerToken);
    }

    @RequestMapping("/register")
    public ResultVO register(String registerToken, String captcha) {
        if (!redis.hasKey(registerToken)) {
            return ResultVOUtil.err(1, "验证码过期");
        }
        HashMap<String, String> map = (HashMap<String, String>) redis.opsForValue().get(registerToken);
        if (!map.get("captcha").equals(captcha)) {
            return ResultVOUtil.err(2, "验证码错误");
        }
        Integer res = loginMapper.getUserByEmail(map.get("email"));
        if (res != null) {
            return ResultVOUtil.err(3, "当前邮箱已经注册");
        }
        int addRes = loginMapper.addUser(map.get("username"), DigestUtils.md5DigestAsHex(map.get("password").getBytes()), 2, map.get("email"));
        if (addRes > 0) {
            // 销毁redis数据
            redis.delete(registerToken);
            return ResultVOUtil.oks();
        }
        return ResultVOUtil.err(4, "注册失败，请重试");
    }
}
