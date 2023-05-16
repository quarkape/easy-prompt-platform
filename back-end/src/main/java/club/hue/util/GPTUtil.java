package club.hue.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class GPTUtil {
    private static final String API_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "xx";
    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient.Builder()
//            .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890)))
            .connectTimeout(180, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .build();

    public HashMap<String, Object> chat(String prompt, Double temperature, Double ppval, Double pfval, Integer n) throws IOException {
        String newPrompt = prompt.replaceAll("\n", "").replaceAll("\"", "\\\\\"");
        String requestBody = "{\"model\":\"gpt-3.5-turbo\",\"n\":" + n + ",\"temperature\":"+ temperature +",\"presence_penalty\":"+ ppval +",\"frequency_penalty\":"+pfval + ",\"messages\":[{\"role\":\"user\",\"content\":\"" + newPrompt + "\"}]}";
//        System.out.println(requestBody);
        return dealGPTResultUtil(requestBody);
    }

    public HashMap<String, Object> chatSimple(String prompt, Double temperature, Double ppval, Double pfval) throws IOException {
        String newPrompt = prompt.replaceAll("\n", "").replaceAll("\"", "\\\\\"");
//        System.out.println(newPrompt);
        String requestBody = "{\"model\":\"gpt-3.5-turbo\",\"n\":1,\"temperature\":" + temperature + ",\"presence_penalty\":" + ppval + ",\"frequency_penalty\":" + pfval + ",\"messages\":[{\"role\":\"user\",\"content\":\"" + newPrompt + "\"}]}";
        return dealGPTResultUtil(requestBody);
    }

    /**
     * 封装gpt请求处理函数
     * @param requestBody 请求体
     * @return 各种数据
     * @throws IOException err
     */
    public HashMap<String, Object> dealGPTResultUtil(String requestBody) throws IOException {
        JSONObject resObject = OKHttpUtilPost(client, API_ENDPOINT, API_KEY, requestBody);
        String content = resObject.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        int promptTokens = resObject.getJSONObject("usage").getInteger("prompt_tokens");
        int completionTokens = resObject.getJSONObject("usage").getInteger("completion_tokens");
        String finishReason = resObject.getJSONArray("choices").getJSONObject(0).getString("finish_reason");
        HashMap<String, Object> map = new HashMap<>();
        map.put("content", content);
        map.put("promptTokens", promptTokens);
        map.put("completionTokens", completionTokens);
        map.put("finishReason", finishReason);
        return map;
    }

    /**
     * 根据gpt封装okhttp
     * @param client okhttp实例
     * @param url 访问api
     * @param apiKey key
     * @param requestBody 请求体
     * @return 数据
     * @throws IOException err
     */
    public static JSONObject OKHttpUtilPost(OkHttpClient client, String url, String apiKey, String requestBody) throws IOException {
//        System.out.println(requestBody);
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Connection", "close")
                .post(RequestBody.create(mediaType, requestBody))
                .build();
        Call call = client.newCall(request);
        call.timeout().timeout(180, TimeUnit.SECONDS);
        Response response = call.execute();
        String responseBody = response.body().string();
//        System.out.println(responseBody);
        JSONObject obj = JSON.parseObject(responseBody);
        return obj;
    }
}