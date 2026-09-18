package com.example.xinli.util;

import com.example.xinli.config.AiConfig;
import com.example.xinli.entity.AiMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class AiChatUtil {
    private static final Logger log = LoggerFactory.getLogger(AiChatUtil.class);

    @Autowired
    private AiConfig aiConfig;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT = """
            你是校园心理倾诉陪伴助手，倾听用户情绪，给予共情、安慰、支持。
            不要给出医学诊断，不要下病情判断。
            如果用户有强烈自伤自杀想法，提醒尽快联系学校心理老师。
            回答温和简短口语化，不要输出markdown，不要长篇大论。
            """;

    public String chatWithHistory(List<AiMessage> historyList) {
        List<Msg> messages = new ArrayList<>();
        messages.add(new Msg("system", SYSTEM_PROMPT));
        for (AiMessage m : historyList) {
            messages.add(new Msg(m.getRole(), m.getContent()));
        }

        ChatReq bodyObj = new ChatReq(aiConfig.getModelName(), messages, false);
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(bodyObj);
            log.info("请求大模型JSON：{}",jsonBody);
        } catch (Exception e) {
            log.error("序列化失败",e);
            return "参数序列化失败";
        }

        Request request = new Request.Builder()
                .url(aiConfig.getApiUrl())
                .header("Authorization", "Bearer " + aiConfig.getApiKey())
                // ⚠️这里全部使用英文短横杠！！！ Content-Type
                .header("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            log.info("http响应码：{}",response.code());
            if (!response.isSuccessful() || response.body() == null) {
                return "AI服务访问失败，httpCode:"+response.code();
            }
            String respStr = response.body().string();
            log.info("大模型返回原始数据：{}",respStr);
            JsonNode root = objectMapper.readTree(respStr);
            return root.at("/choices/0/message/content").asText("AI未返回内容");
        } catch (IOException e) {
            log.error("调用大模型网络异常",e);
            return "AI网络调用异常";
        }
    }

    public record Msg(String role, String content) {}
    public record ChatReq(String model, List<Msg> messages, boolean stream) {}
}

