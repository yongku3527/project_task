package com.quanhai.dingdingdemo.model;

import lombok.Data;

import java.time.LocalDate;

// 项目时间信息类
@Data
public class ProjectTimeInfo {
        private final LocalDate earliestDate;
        private final LocalDate latestDate;

        public ProjectTimeInfo(LocalDate earliestDate, LocalDate latestDate) {
            this.earliestDate = earliestDate;
            this.latestDate = latestDate;
        }

}