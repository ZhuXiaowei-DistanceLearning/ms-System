package com.zxw.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zxw
 * @date 2020/7/22 14:32
 */
@RestController
public class TestController {
    @GetMapping("/order")
    public ResponseEntity test(){
        return ResponseEntity.ok("order01");
    }

}
