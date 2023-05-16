package club.hue.conf;

import club.hue.mapper.LoginMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class UserRealm extends AuthorizingRealm {

    @Autowired
    LoginMapper loginMapper;

    // 进入设置了拦截的页面就会触发这个方法
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        Subject subject = SecurityUtils.getSubject();

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        Set<String> set = new HashSet<>();
        info.setStringPermissions(set);
        // 这里的obj就是 doGetAuthenticationInfo() 返回的第一个参数
        int rolenum = Integer.parseInt(subject.getPrincipal().toString());
        // String role = (String) obj.get("role");
        // 获取roleid和rolename的对应关系
        List<HashMap<String, Object>> roleList = loginMapper.getRolePattern();
        for (int i=0;i<roleList.size();i++) {
            HashMap<String, Object> map = roleList.get(i);
            if (map.get("rolenum").equals(rolenum)) {
                info.addRole((String) map.get("rolename"));
            }
        }
        return info;
    }

    // LoginController里面的checkLogin中的currentUser.login()会触发这个函数
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
        UsernamePasswordToken userToken = (UsernamePasswordToken) token;
        String username = userToken.getUsername();
        String encodedPassword = String.valueOf(userToken.getPassword());
        String rolenum = String.valueOf(userToken.getHost());
        return new SimpleAuthenticationInfo(rolenum, encodedPassword, username);
    }
}
