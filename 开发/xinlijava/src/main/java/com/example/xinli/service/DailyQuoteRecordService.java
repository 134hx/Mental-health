package com.example.xinli.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.xinli.entity.DailyQuoteRecord;
import com.example.xinli.mapper.DailyQuoteRecordMapper;
import org.springframework.stereotype.Service;

@Service
public class DailyQuoteRecordService extends ServiceImpl<DailyQuoteRecordMapper, DailyQuoteRecord> {
}