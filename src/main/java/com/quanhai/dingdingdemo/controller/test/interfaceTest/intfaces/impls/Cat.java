package com.quanhai.dingdingdemo.controller.test.interfaceTest.intfaces.impls;

import com.quanhai.dingdingdemo.controller.test.interfaceTest.intfaces.DongWu;
import org.springframework.stereotype.Component;

@Component
public class Cat implements DongWu {
    @Override
    public void eat() {
        System.out.println("哈基米南北绿豆");
    }

    @Override
    public void jiao() {
        System.out.println("曼波~");
    }
}
