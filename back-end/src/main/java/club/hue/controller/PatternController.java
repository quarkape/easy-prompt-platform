package club.hue.controller;


import club.hue.mapper.PatternMapper;
import club.hue.util.CheckAuthorizationUtil;
import club.hue.util.GPTUtil;
import club.hue.vo.ResultVO;
import club.hue.vo.ResultVOUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
public class PatternController {

    @Autowired
    private PatternMapper patternMapper;

    private final static int NUM = 1;

    @Resource
    private RedisTemplate<String, Serializable> redis;

    /**
     * 获取所有公共模板列表
     * @param field 所属领域
     * @param keywords 搜索关键字
     * @param page 当前页
     * @return 模板列表
     */
    @RequestMapping("/pattern-list")
    @RequiresRoles(value = {"vip", "user", "visitor"}, logical = Logical.OR)
    public ResultVO getPatternList(String field, String keywords, int page) {
        List<HashMap<String, String>> list = patternMapper.getPatternList(field, keywords, page, 15);
        int total = patternMapper.getPatternCount(field, keywords);
        HashMap<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", total);
        return ResultVOUtil.ok(map);
    }

    /**
     * 获取模板的领域列表
     * @return 模板领域列表
     */
    @RequestMapping("/pattern-field")
    @RequiresRoles(value = {"vip", "user", "visitor"}, logical = Logical.OR)
    public ResultVO getPatternField() {
        return ResultVOUtil.ok(patternMapper.getPatternField());
    }

    /**
     * 获取单个模板内容
     * @param patternid 模板id
     * @return 模板信息及配置
     */
    @RequestMapping("/pattern")
    @RequiresRoles(value = {"vip", "user", "visitor"}, logical = Logical.OR)
    public ResultVO getPattern(String patternid) {
        HashMap<String, Object> pattern = patternMapper.getPattern(patternid);
        return ResultVOUtil.ok(pattern);
    }

    /**
     * 获取我的pattern信息，传递的参数不一样
     * @param patternid
     * @param req
     * @return
     */
    @RequestMapping("/get-mypattern-info")
    @RequiresRoles(value = {"vip", "user", "visitor"}, logical = Logical.OR)
    public ResultVO getMyPatternInfo(String patternid, HttpServletRequest req) {
        String userid = CheckAuthorizationUtil.check(req, redis);
        if (userid == null) {
            return ResultVOUtil.err(1, "当前用户未登录");
        }
        HashMap<String, Object> map = patternMapper.getMyPatternInfo(patternid, userid);
        return ResultVOUtil.ok(map);
    }

    /**
     * 与chatgpt交互
     * @param patternid 模板id
     * @param prompt 提示内容
     * @param stepid 步骤id
     * @param optoken 当前任务token
     * @param temperature 提示参数
     * @return gpt生成结果
     * @throws IOException 报错
     */
    @RequestMapping("/prompt-conv")
    @RequiresRoles(value = {"vip", "user", "visitor"}, logical = Logical.OR)
    public ResultVO promptConversation(String patternid, String prompt, String stepid, String optoken, Double temperature, Double ppval, Double pfval, HttpServletRequest req) throws IOException {
        String userid = CheckAuthorizationUtil.check(req, redis);
        if (userid == null) {
            return ResultVOUtil.err(2, "没有登录或已经超时");
        }
        // 请求的时候携带了token但是redis当中不存在记录
        JSONObject resObj = new JSONObject();
        JSONObject patternJSON;
        String newOpToken = null;
        if (optoken != null && !optoken.equals("")) {
            if (Boolean.FALSE.equals(redis.hasKey(optoken))) {
                return ResultVOUtil.err(1, "任务不存在或任务已过期，请刷新以开始新的任务");
            }
            patternJSON = (JSONObject) redis.opsForValue().get(optoken);
        } else {
            // 如果还没有opToken，则下发一个新的opToken并返回给用户，用户每次都判断是否有opToken返回，如果有的话，就存入本地
            newOpToken = UUID.randomUUID() + "";
            patternJSON = JSONObject.parseObject((String) patternMapper.getMyPatternInfo(patternid, userid).get("config"));
            redis.opsForValue().set(newOpToken, patternJSON);
            resObj.put("opToken", newOpToken);
        }
        // 从redis中取出所有steps
        JSONArray steps = patternJSON.getJSONArray("steps");
        for (int i=0;i<steps.size();i++) {
            // 如果是当前步骤
            JSONObject step = steps.getJSONObject(i);
            if (step.getString("id").equals(stepid)) {
                // 做最后的清理
//                prompt = prompt.replaceAll("\\$\\{\\{", "").replaceAll("}}\\$", "");
                // 将用户要进行请求的内容存储
                JSONObject inputContent = new JSONObject();
                inputContent.put("id", UUID.randomUUID() + "");
                inputContent.put("type", 0); // 文本类型，0-用户发送，1-gpt回复
                inputContent.put("content", prompt);
                if (step.getJSONArray("chats") == null) {
                    JSONArray chats = new JSONArray();
                    chats.add(inputContent);
                    step.put("chats", chats);
                } else {
                    step.getJSONArray("chats").add(inputContent);
                }
                // 开始请求gpt
                HashMap<String, Object> map = new GPTUtil().chat(prompt, temperature, ppval, pfval, NUM);
                // 将生成内容存储
                // 此时肯定存在chats了
                JSONObject generatedContent = new JSONObject();
                String resId = UUID.randomUUID() + "";
                generatedContent.put("id", resId);
                generatedContent.put("type", 1); // 1-gpt生成内容
                generatedContent.put("content", map.get("content"));
                step.getJSONArray("chats").add(generatedContent);
                // 将修改后的内容更新至redis当中
                // 如果newOpToken为空，说明当前存在opToken，则使用原来存在的optoken
                redis.opsForValue().set(newOpToken==null?optoken:newOpToken, patternJSON, Duration.ofDays(1));
                // 返回内容，还有最开始可能会有的opToken
                resObj.put("id", resId);
                resObj.put("type", 1); // 1-gpt生成内容
                resObj.put("content", map.get("content"));
                resObj.put("promptTokens", map.get("promptTokens"));
                resObj.put("completionTokens", map.get("completionTokens"));
                resObj.put("finishReason", map.get("finishReason"));
                return ResultVOUtil.ok(resObj);
            }
        }
        return ResultVOUtil.err(2, "操作失败");
    }

    /**
     * 设置最佳生成内容，作用是能够在一定程度上保存用户数据
     * @param optoken 这一次pattern操作的uuid
     * @param stepid 当前步骤的id
     * @param idealid 当前理想生成文本的id
     * @return 保存数据的结果
     */
    @RequestMapping("/prompt-ideal")
    @RequiresRoles(value = {"vip", "user", "visitor"}, logical = Logical.OR)
    public ResultVO promptIdealContent(String optoken, String stepid, String idealid) {
        // 能够进行下一步，应该会有opToken，主要检验opToken是否过期即可
        if (optoken != null && !optoken.equals("")) {
            if (Boolean.FALSE.equals(redis.hasKey(optoken))) {
                return ResultVOUtil.err(1, "任务不存在或任务已过期，请刷新以开始新的任务");
            }
        } else {
            return ResultVOUtil.err(4, "当前操作任务不存在");
        }
        // 从redis中取出所有steps
        JSONObject patternJSON = (JSONObject) redis.opsForValue().get(optoken);
        JSONArray steps = patternJSON.getJSONArray("steps");
        for (int i=0;i<steps.size();i++) {
            // 如果是当前步骤
            JSONObject step = steps.getJSONObject(i);
            if (step.getString("id").equals(stepid)) {
                if (idealid != null) {
                    JSONArray chats = step.getJSONArray("chats");
                    for (int n = 0; n < chats.size(); n++) {
                        // 遍历到理想内容对应的id
                        if (chats.getJSONObject(n).getString("id").equals(idealid)) {
                            if (chats.getJSONObject(n).getInteger("type") == 1) {
                                chats.getJSONObject(n).put("ideal", 1);
                                redis.opsForValue().set(optoken, patternJSON, Duration.ofDays(1)); // 更新当前的内容
                                return ResultVOUtil.oks();
                            } else {
                                // 如果设置的理想文本是用户发送的内容，则提示错误
                                // type: 0-用户发送, 1-GPT生成
                                return ResultVOUtil.err(1, "最佳内容应该是GPT生成的内容");
                            }
                        }
                    }
                } else {
                    return ResultVOUtil.err(2, "请选择最佳生成内容");
                }
            }
        }
        return ResultVOUtil.err(3, "没有找到匹配项");
    }

    /**
     * 获取用户收藏的pattern列表，自己创建的pattern默认会进入这个列表当中
     * @param req 请求
     * @return 收藏列表以及该列表的总数
     */
    @RequestMapping("/pattern-mine")
    @RequiresRoles(value = {"vip", "user"}, logical = Logical.OR)
    public ResultVO getMyPattern(String field, String keywords, HttpServletRequest req) {
        String userid = CheckAuthorizationUtil.check(req, redis);
        if (userid == null) {
            return ResultVOUtil.err(1, "未登录或已过期");
        }
        List<HashMap<String, Object>> patterns = patternMapper.getMyPattern(userid, field, keywords);
        Integer total = patternMapper.getMyPatternCount(userid, field, keywords);
        HashMap<String, Object> map = new HashMap<>();
        map.put("patterns", patterns);
        map.put("total", total);
        return ResultVOUtil.ok(map);
    }

    /**
     * 获取特定id的提示模板
     * @param id 提示模板的id
     * @return 模板配置内容
     */
    @RequestMapping("/pattern-config")
    @RequiresRoles(value = {"vip", "user", "visitor"}, logical = Logical.OR)
    public ResultVO getPatternConfig(String id) {
        // 保证这个id是能够被获取的
        return ResultVOUtil.ok(patternMapper.getPatternConfig(id));
    }

    /**
     * 用户收藏一个pattern
     * 相当于直接新建一个pattern
     * @param patternid pattern的id
     * @param req httpservletrequest
     * @return 操作状态
     */
    @RequestMapping("/add-pattern-mine")
    @RequiresRoles(value = {"vip", "user"}, logical = Logical.OR)
    @Transactional(rollbackFor = Exception.class)
    public ResultVO addPatternToMine(String patternid, HttpServletRequest req) {
        // 判断用户身份
        String userid = CheckAuthorizationUtil.check(req, redis);
        if (userid == null) {
            return ResultVOUtil.err(1, "未登录或已过期");
        }
        // 检验传入参数
        if (patternid == null) {
            return ResultVOUtil.err(2, "提示模板不存在");
        }
        // 检验模板数量限制
        int limitRes = patternMapper.checkPatternLimitation(userid);
        if (limitRes == 1) {
            // 如果超出了数量限制
            return ResultVOUtil.err(3, "超出数量限制");
        }
        // 添加到我的库的时候，新建一个pattern，只是基本信息与被复制的提示模板一致
        // uuid是新插入的id，patternid是被复制的id
        String uuid = UUID.randomUUID() + "";
        int res = patternMapper.addPattern(uuid, userid, patternid);
        int resConfig = patternMapper.addPatternConfig(uuid, patternid);
        if (res + resConfig < 2) {
            return ResultVOUtil.err(4, "操作失败");
        }
        return ResultVOUtil.oks();
    }

    /**
     * 修改提示模板的配置内容（不是基本信息）
     * @param config 配置字符串
     * @param id 提示模板id
     * @return 操作状态
     */
    @RequestMapping("/modify-pattern-config")
    @RequiresRoles(value = {"vip", "user"}, logical = Logical.OR)
    public ResultVO modifyPatternConfig(String config, String id, HttpServletRequest req) {
        // 检查用户是否具有这个pattern的修改权限
        String userid = CheckAuthorizationUtil.check(req, redis);
        if (userid == null) {
            return ResultVOUtil.err(1, "未登录或已过期");
        }
        int res = patternMapper.modifyPatternConfig(config, id, userid);
        if (res > 0) {
            return ResultVOUtil.oks();
        }
        return ResultVOUtil.err(2, "权限不足");
    }

    @RequestMapping("/modify-pattern-info")
    @RequiresRoles(value = {"vip", "user"}, logical = Logical.OR)
    @Transactional(rollbackFor = Exception.class)
    public ResultVO modifyPatternInfo(String patternid, String ispublic, String name, String desc, String field, HttpServletRequest req) {
        String userid = CheckAuthorizationUtil.check(req, redis);
        if (userid == null) {
            return ResultVOUtil.err(1, "未登录或登录已过期");
        }
        // 如果pattern为空，则意味着插入一项
        Integer hasOwnership;
        if (!patternid.equals("")) {
            hasOwnership = patternMapper.checkPatternOwnership(patternid, userid);
            if (hasOwnership == null) {
                return ResultVOUtil.err(3, "无权进行操作");
            }
        }
        String newPatternId = UUID.randomUUID() + "";
        String pid = patternid.equals("") ? newPatternId : patternid;
        int res = patternMapper.modifyPatternInfo(pid, name, desc, userid, field, Integer.parseInt(ispublic), 0);
        // 由于此方法中，pattern可能是新建的，因此有必要初始化一下config
        int ress = patternMapper.modifyPatternConfig("{\"steps\": []}", pid, userid);
        if (res < 0 && ress < 0) {
            return ResultVOUtil.err(2, "操作失败");
        }
        return ResultVOUtil.ok(pid);
    }

    /**
     * 删除模板
     * @param id 模板id
     * @param req 请求
     * @return 操作状态
     */
    @Transactional(rollbackFor = Exception.class)
    @RequestMapping("/delete-pattern")
    @RequiresRoles(value = {"vip", "user"}, logical = Logical.OR)
    public ResultVO deletePatern(String id, HttpServletRequest req) {
        String userid = CheckAuthorizationUtil.check(req, redis);
        if (userid == null) {
            return ResultVOUtil.err(1, "当前用户未登录或登录已失效");
        }
        // 先删除pattern信息表，在删除配置表
//        int res2 = patternMapper.deletePattternConfig(id, userid);
//        int res1 = patternMapper.deletePattern(id, userid);
        int res = patternMapper.deletePattern(id, userid);
        if (res < 2) {
            return ResultVOUtil.err(2, "操作失败");
        }
        return ResultVOUtil.oks();
    }

    @RequestMapping("/example-pattern")
    @RequiresRoles(value = {"vip", "user", "visitor"}, logical = Logical.OR)
    public ResultVO getExamplePattern() {
        final String owner = "100000";
        return ResultVOUtil.ok(patternMapper.getPatternExample(owner));
    }

}
