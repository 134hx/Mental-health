package com.example.xinli.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.xinli.entity.Scale;
import com.example.xinli.entity.ScaleRecord;
import com.example.xinli.mapper.ScaleRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScaleRecordService extends ServiceImpl<ScaleRecordMapper, ScaleRecord> {
    @Autowired
    private ScaleService scaleService;

    // 增加percentScore入参
    public ScaleRecord submitRecord(Long userId, Long scaleId, Integer totalScore, Integer percentScore) {
        ScaleRecord record = new ScaleRecord();
        record.setUserId(userId);
        record.setScaleId(scaleId);
        record.setTotalScore(totalScore);
        record.setPercentScore(percentScore); // 保存百分制

        // 存入量表名称到冗余字段
        Scale scale = scaleService.getById(scaleId);
        if(scale != null){
            record.setScaleName(scale.getScaleName());
        }else{
            record.setScaleName("未知量表");
        }

        String conclusion;
        if(scaleId.equals(1L)){
            // GAD‑7 0‑21分
            if(totalScore <=4){
                conclusion = "无明显焦虑：最近状态平稳，保持规律作息，维持当前生活节奏。";
            }else if(totalScore <=9){
                conclusion = "轻度焦虑：偶尔存在焦虑情绪，建议多运动，和朋友倾诉，适当放松。";
            }else if(totalScore <=14){
                conclusion = "中度焦虑：焦虑感受比较明显，建议尝试正念放松，如果持续难受建议寻求心理疏导。";
            }else{
                conclusion = "重度焦虑：焦虑已经明显影响日常生活，强烈建议联系学校心理咨询中心进行专业干预。";
            }
        }else if(scaleId.equals(2L)){
            // SCL‑90简易版 10‑50分
            if(totalScore <=19){
                conclusion = "心理健康状态良好：心态较为平稳，能够较好应对日常学习生活压力。";
            }else if(totalScore <=29){
                conclusion = "轻微心理压力：存在少量压力困扰，可以自我调节，多参加户外活动。";
            }else if(totalScore <=39){
                conclusion = "中度心理压力：多项指标存在不适，建议进行心理倾诉，必要时寻求心理老师帮助。";
            }else{
                conclusion = "较重心理困扰：多项心理症状明显，建议尽快联系学校心理咨询获取专业支持。";
            }
        }else{
            conclusion = "测评完成，请结合自身实际情况调整心态。";
        }
        record.setConclusion(conclusion);
        this.save(record);
        return record;
    }
}
