package com.quanhai.dingdingdemo.controller.test.interfaceTest.intfaces.impls;

import com.quanhai.dingdingdemo.controller.test.interfaceTest.intfaces.DongWu;
import org.springframework.stereotype.Component;

@Component
public class Dog implements DongWu {
    @Override
    public void eat() {
        System.out.println("大狗大狗嚼嚼嚼");
    }

    @Override
    public void jiao() {
        System.out.println("大狗大狗叫叫叫");
    }
}
