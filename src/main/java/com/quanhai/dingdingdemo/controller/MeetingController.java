package com.quanhai.dingdingdemo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quanhai.dingdingdemo.mapper.MeetingMapper;
import com.quanhai.dingdingdemo.model.Resp.Result;
import com.quanhai.dingdingdemo.model.Resp.ResultUtil;
import com.quanhai.dingdingdemo.model.metting.Meeting;
import com.quanhai.dingdingdemo.model.metting.MeetingInfo;
import com.quanhai.dingdingdemo.model.metting.MeetingItem;
import com.quanhai.dingdingdemo.service.MeetingInfoService;
import com.quanhai.dingdingdemo.service.MeetingItemService;
import com.quanhai.dingdingdemo.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/meeting")
public class MeetingController {

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MeetingMapper meetingMapper;

    @GetMapping("/getAll")
    public Result getAll() {
        List<Meeting> meetingList = null;
        try {
            meetingList = meetingMapper.getMeetingList();
        } catch (Exception e) {
            return ResultUtil.fail(e.getMessage());
        }
        return ResultUtil.success(meetingList);
    }

    // Meeting增删改查接口
    @PostMapping("/add")
    public Result addMeeting(@RequestBody Meeting meeting) {
        try {
            boolean result = meetingService.save(meeting);
            return result ? ResultUtil.success(meeting) : ResultUtil.fail("添加会议失败");
        } catch (Exception e) {
            return ResultUtil.fail(e.getMessage());
        }
    }

    @GetMapping("/get/{id}")
    public Result getMeeting(@PathVariable Integer id) {
        try {
            Meeting meeting = meetingService.getById(id);
            return meeting != null ? ResultUtil.success(meeting) : ResultUtil.fail("会议不存在");
        } catch (Exception e) {
            return ResultUtil.fail(e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result updateMeeting(@RequestBody Meeting meeting) {
        try {
            boolean result = meetingService.updateById(meeting);
            return result ? ResultUtil.success(meeting) : ResultUtil.fail("更新会议失败");
        } catch (Exception e) {
            return ResultUtil.fail(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteMeeting(@PathVariable Integer id) {
        try {
            boolean result = meetingService.removeById(id);
            LambdaQueryWrapper<MeetingInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MeetingInfo::getMeetingId, id);
            meetingInfoService.remove(queryWrapper);
            return result ? ResultUtil.success("删除成功") : ResultUtil.fail("删除会议失败");
        } catch (Exception e) {
            return ResultUtil.fail(e.getMessage());
        }
    }

    // MeetingInfo增删改查接口
    @Autowired
    private MeetingInfoService meetingInfoService;

    @PostMapping("/info/add")
    public Result addMeetingInfo(@RequestBody MeetingInfo meetingInfo) {
        try {
            boolean result = meetingInfoService.save(meetingInfo);
            return result ? ResultUtil.success(meetingInfo) : ResultUtil.fail("添加会议信息失败");
        } catch (Exception e) {
            return ResultUtil.fail(e.getMessage());
        }
    }

    @GetMapping("/info/get/{id}")
    public Result getMeetingInfo(@PathVariable Integer id) {
        try {
            MeetingInfo meetingInfo = meetingInfoService.getById(id);
            return meetingInfo != null ? ResultUtil.success(meetingInfo) : ResultUtil.fail("会议信息不存在");
        } catch (Exception e) {
            return ResultUtil.fail(e.getMessage());
        }
    }

    @PutMapping("/info/update")
    public Result updateMeetingInfo(@RequestBody MeetingInfo meetingInfo) {
        try {
            boolean result = meetingInfoService.updateById(meetingInfo);
            return result ? ResultUtil.success(meetingInfo) : ResultUtil.fail("更新会议信息失败");
        } catch (Exception e) {
            return ResultUtil.fail(e.getMessage());
        }
    }

    @DeleteMapping("/info/delete/{id}")
    public Result deleteMeetingInfo(@PathVariable Integer id) {
        try {
            boolean result = meetingInfoService.removeById(id);
            return result ? ResultUtil.success("删除成功") : ResultUtil.fail("删除会议信息失败");
        } catch (Exception e) {
            return ResultUtil.fail(e.getMessage());
        }
    }

    // MeetingItem增删改查接口
    @Autowired
    private MeetingItemService meetingItemService;

    @PostMapping("/item/add")
    public Result addMeetingItem(@RequestBody MeetingItem meetingItem) {
        try {
            boolean result = meetingItemService.save(meetingItem);
            return result ? ResultUtil.success(meetingItem) : ResultUtil.fail("添加会议项失败");
        } catch (Exception e) {
            return ResultUtil.fail(e.getMessage());
        }
    }

    @GetMapping("/item/get/{id}")
    public Result getMeetingItem(@PathVariable Integer id) {
        try {
            MeetingItem meetingItem = meetingItemService.getById(id);
            return meetingItem != null ? ResultUtil.success(meetingItem) : ResultUtil.fail("会议项不存在");
        } catch (Exception e) {
            return ResultUtil.fail(e.getMessage());
        }
    }

    @PutMapping("/item/update")
    public Result updateMeetingItem(@RequestBody MeetingItem meetingItem) {
        try {
            boolean result = meetingItemService.updateById(meetingItem);
            return result ? ResultUtil.success(meetingItem) : ResultUtil.fail("更新会议项失败");
        } catch (Exception e) {
            return ResultUtil.fail(e.getMessage());
        }
    }

    @DeleteMapping("/item/delete/{id}")
    public Result deleteMeetingItem(@PathVariable Integer id) {
        try {
            boolean result = meetingItemService.removeById(id);
            return result ? ResultUtil.success("删除成功") : ResultUtil.fail("删除会议项失败");
        } catch (Exception e) {
            return ResultUtil.fail(e.getMessage());
        }
    }
}
