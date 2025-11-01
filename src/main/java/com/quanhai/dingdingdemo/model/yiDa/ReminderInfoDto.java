package com.quanhai.dingdingdemo.model.yiDa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;

@Data
public class ReminderInfoDto {

    private ArrayList<String> executorIdList;

    private String pcbName;

}
