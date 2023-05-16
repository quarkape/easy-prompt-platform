package club.hue.controller;

import club.hue.mapper.PromptMapper;
import club.hue.util.CharacterEscapeUtil;
import club.hue.util.CheckAuthorizationUtil;
import club.hue.util.GPTUtil;
import club.hue.vo.ResultVO;
import club.hue.vo.ResultVOUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.UUID;

@RestController
public class PromptController {

    @Autowired
    private RedisTemplate<String, Serializable> redis;

    @Autowired
    private PromptMapper promptMapper;

    @RequestMapping("/chat-simple")
    @RequiresRoles(value = {"vip", "user", "visitor"}, logical = Logical.OR)
    public ResultVO chatSimple(String prompt, String token, String temperature, String ppval, String pfval, HttpServletRequest req) throws IOException {
        // 判断当前用户是否登录
        String userid = CheckAuthorizationUtil.check(req, redis);
        if (userid == null) {
            return ResultVOUtil.err(1, "未登录或已过期");
        }
        // 判断字符是否超过限制
        if (prompt.length() > 4000) {
            return ResultVOUtil.err(2, "超过最长字符长度限制");
        }
        String newToken = null;
        JSONArray chats = new JSONArray();
        if (token == null || token == "") {
            newToken = UUID.randomUUID() + "";
            promptMapper.initChatItem(newToken, userid, "[]");
        } else {
            chats = (JSONArray) redis.opsForValue().get(token);
        }

        // 发送内容
        JSONObject obj = new JSONObject();
        obj.put("type", 0);
        obj.put("time_stamp", System.currentTimeMillis()+"");
        JSONObject configObj = new JSONObject();
        configObj.put("temperature", temperature);
        configObj.put("ppval", ppval);
        configObj.put("pfval", pfval);
        obj.put("config", configObj);
        obj.put("content", prompt);

        // 拼接prompt，以支持连续对话
        String newPrompt = "";
        for (int i = 0; i < chats.size(); i++) {
            JSONObject item = chats.getJSONObject(i);
            newPrompt += item.getString("content") + "\\n\\n";
        }
        newPrompt += prompt.replaceAll("\n", "").replaceAll("\\n\\n", "");
        HashMap<String, Object> res = new GPTUtil().chatSimple(newPrompt, Double.parseDouble(temperature), Double.parseDouble(ppval), Double.parseDouble(pfval));

        // 返回内容
        JSONObject resObj = new JSONObject();
        resObj.put("type", 1);
        resObj.put("time_stamp", System.currentTimeMillis()+"");
        resObj.put("tokens", res.get("completionTokens"));
        resObj.put("content", res.get("content"));
        chats.add(resObj);
        String resObjStr = "{\"type\":1,\"time_stamp\":"+System.currentTimeMillis()+",\"tokens\":"+res.get("completionTokens")+",\"content\":\""+CharacterEscapeUtil.escapeCharacters((String) res.get("content"))+"\"}";


        obj.put("tokens", res.get("promptTokens"));
        chats.add(obj);
        String objStr = "{\"type\":0,\"time_stamp\":"+System.currentTimeMillis()+",\"temperature\":"+temperature+",\"ppval\":"+ppval+",\"pfval\":"+pfval+",\"content\":\""+ CharacterEscapeUtil.escapeCharacters(prompt) +"\"}";


        // redis保留30分钟的记录
        redis.opsForValue().set((token == null || token == "") ? newToken : token, chats, Duration.ofDays(1));

        // 将当前内容存储或更新至MySQL
        promptMapper.addChatItem(objStr, resObjStr, (token == null || token == "") ? newToken : token);

        if (newToken != null) {
            res.put("token", newToken);
        }
        return ResultVOUtil.ok(res);
    }
}