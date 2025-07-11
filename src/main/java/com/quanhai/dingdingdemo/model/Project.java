package com.quanhai.dingdingdemo.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;


@Data
@TableName("project")
public class Project {
    private Long id;                // 自增主键
    private String projectId;       // 项目ID
    private String name;            // 项目名称
    private String description;     // 描述
    private LocalDate created;           // 创建时间
    private String creatorId;       // 创建者ID
    private LocalDate startDate;         // 开始日期
    private LocalDate endDate;           // 结束日期
    private LocalDate updated;           // 更新时间
    private Boolean isArchived;     // 是否归档
    private Boolean isSuspended;    // 是否暂停
    private Boolean isTemplate;     // 是否为模板
    private String logo;            // 项目logo
    private String organizationId;  // 组织ID
    private String visibility;      // 可见范围
    private String uniqueIdPrefix;  // 唯一ID前缀

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", projectId='" + projectId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", created=" + created +
                ", creatorId='" + creatorId + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", updated=" + updated +
                ", isArchived=" + isArchived +
                ", isSuspended=" + isSuspended +
                ", isTemplate=" + isTemplate +
                ", logo='" + logo + '\'' +
                ", organizationId='" + organizationId + '\'' +
                ", visibility='" + visibility + '\'' +
                ", uniqueIdPrefix='" + uniqueIdPrefix + '\'' +
                '}';
    }
}