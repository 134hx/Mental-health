package com.example.xinli.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.xinli.entity.Scale;
import com.example.xinli.mapper.ScaleMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ScaleService extends ServiceImpl<ScaleMapper, Scale> {

    public List<Scale> listAll(){
        return this.list();
    }
}

