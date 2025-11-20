package com.quanhai.dingdingdemo.controller.test.interfaceTest.service;

import com.quanhai.dingdingdemo.controller.test.interfaceTest.intfaces.DongWu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ServiceLoader;

@Component
public class DongWuOperation {


    @Autowired
    private List<DongWu> dongWus;

     public void 照顾动物们() {
         for (DongWu dongWu : dongWus) {
             dongWu.eat();
             dongWu.jiao();
         }
    }

}
