package com.quanhai.dingdingdemo.controller.test.interfaceTest.intfaces.impls;

import com.quanhai.dingdingdemo.controller.test.interfaceTest.intfaces.DongWu;
import org.springframework.stereotype.Component;

@Component
public class LaoShu implements DongWu {
    @Override
    public void eat() {
        System.out.println("得吃~(得吃的小曲)");
    }

    @Override
    public void jiao() {
        System.out.println("属鼠我呀~不中嘞~");
    }
}
