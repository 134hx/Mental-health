package com.example.xinli.controller;

import com.example.xinli.common.Result;
import com.example.xinli.entity.Scale;
import com.example.xinli.entity.ScaleQuestion;
import com.example.xinli.service.ScaleQuestionService;
import com.example.xinli.service.ScaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/scale")
public class ScaleController {
    @Autowired
    private ScaleService scaleService;
    @Autowired
    private ScaleQuestionService scaleQuestionService;

    @GetMapping("/list")
    public Result<List<Scale>> list(){
        List<Scale> list = scaleService.listAll();
        return Result.success(list);
    }

    @GetMapping("/question/{scaleId}")
    public Result<List<ScaleQuestion>> getQuestion(@PathVariable Long scaleId){
        List<ScaleQuestion> questionList = scaleQuestionService.getByScaleId(scaleId);
        return Result.success(questionList);
    }
}
