package com.quanhai.dingdingdemo.model.yiDa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeAndPrcsIns {
    private Long id;
    private String itemCode;
    private String prcsInstanceCode;

}
