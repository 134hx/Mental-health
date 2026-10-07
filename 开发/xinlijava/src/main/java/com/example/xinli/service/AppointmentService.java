package com.example.xinli.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.xinli.entity.Appointment;
import com.example.xinli.mapper.AppointmentMapper;
import org.springframework.stereotype.Service;

@Service
public class AppointmentService extends ServiceImpl<AppointmentMapper, Appointment> {

}

