package com.example.xinli.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.xinli.entity.Scale;
import com.example.xinli.entity.ScaleRecord;
import com.example.xinli.mapper.ScaleRecordMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScaleRecordService extends ServiceImpl<ScaleRecordMapper, ScaleRecord> {

    @Autowired
    private ScaleService scaleService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ScaleRecord submitRecord(Long userId, Long scaleId, Integer totalScore, Integer percentScore) {
        ScaleRecord record = new ScaleRecord();
        record.setUserId(userId);
        record.setScaleId(scaleId);
        record.setTotalScore(totalScore);
        record.setPercentScore(percentScore);

        Scale scale = scaleService.getById(scaleId);
        if (scale != null) {
            record.setScaleName(scale.getScaleName());
            record.setConclusion(buildConclusion(scale, totalScore));
        } else {
            record.setScaleName("未知量表");
            record.setConclusion("测评完成，请结合自身实际情况调整心态。");
        }

        this.save(record);
        return record;
    }

    /**
     * 通用结论生成：从 scale.conclusionJson 里读 ranges
     * 结构：
     * {
     *   "ranges": [
     *     {"max": 4, "text": "无明显焦虑..."},
     *     {"max": 9, "text": "轻度焦虑..."},
     *     ...
     *     {"max": 9999, "text": "重度..."}
     *   ]
     * }
     * 按原始分（totalScore）从低到高找第一个满足 totalScore <= max 的区间
     */
    private String buildConclusion(Scale scale, Integer totalScore) {
        String json = scale.getConclusionJson();
        if (json == null || json.isBlank()) {
            return "测评完成，请结合自身实际情况调整心态。";
        }
        try {
            JsonNode root = MAPPER.readTree(json);
            JsonNode ranges = root.get("ranges");
            if (ranges != null && ranges.isArray()) {
                for (JsonNode r : ranges) {
                    int max = r.get("max").asInt();
                    if (totalScore <= max) {
                        return r.get("text").asText();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "测评完成，请结合自身实际情况调整心态。";
    }
}