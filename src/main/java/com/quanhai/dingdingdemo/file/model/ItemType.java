package com.quanhai.dingdingdemo.file.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@TableName("doc_item_type")
public class ItemType {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String itemPrefix;

    private String typeName;

}
