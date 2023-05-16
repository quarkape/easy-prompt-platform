package club.hue;

import club.hue.util.CharacterEscapeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

@SpringBootTest
class AigcEvelApplicationTests {


    @Test
    void contextLoads() {
        String str = "\"123\"";
        System.out.println(str.replaceAll("\"", "\\\\\"").replaceAll("1", "9"));
    }

}
