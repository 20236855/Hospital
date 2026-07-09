package com.ruoyi.hisdoctor.controller;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.hisdoctor.service.ConsultInvitationService;

/**
 * 联合会诊邀请（Redis 存储，30min TTL）
 */
@RestController
@RequestMapping("/consult")
public class ConsultInvitationController extends BaseController {

    @Autowired
    private ConsultInvitationService consultInvitationService;

    /**
     * 发起联合会诊邀请
     */
    @PostMapping("/invite")
    public AjaxResult sendInvitation(@RequestBody Map<String, Object> params) {
        String senderDoctorName = str(params, "senderDoctorName");
        String location = str(params, "location");
        String importantInfo = str(params, "importantInfo");
        Long senderDoctorId = toLong(params.get("senderDoctorId"));
        List<Long> targetDeptIds = parseLongList(params.get("targetDeptIds"));

        if (senderDoctorName.isEmpty()) {
            return error("请填写发起会诊的医生");
        }
        if (location.isEmpty()) {
            return error("请填写会诊地点");
        }
        if (targetDeptIds.isEmpty()) {
            return error("请选择需要联合会诊的科室");
        }

        List<String> targetDeptNames = new ArrayList<>();
        for (Long deptId : targetDeptIds) {
            targetDeptNames.add(String.valueOf(deptId));
        }

        try {
            Map<String, Object> result = consultInvitationService.sendInvitation(
                    senderDoctorName, location, importantInfo, senderDoctorId,
                    targetDeptIds, targetDeptNames);
            return success(result);
        } catch (Exception e) {
            return error("联合会诊邀请发送失败：" + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    /**
     * 查询当前医生所在科室收到的联合会诊邀请
     */
    @GetMapping("/invitations")
    public AjaxResult listInvitations() {
        List<Map<String, Object>> rows = consultInvitationService.listMyInvitations();
        long unreadCount = rows.stream().filter(item -> !Boolean.TRUE.equals(item.get("accepted"))).count();
        AjaxResult result = AjaxResult.success(rows);
        result.put("unreadCount", unreadCount);
        return result;
    }

    /**
     * 接受联合会诊邀请
     */
    @PostMapping("/accept")
    public AjaxResult acceptInvitation(@RequestParam String invitationId) {
        if (invitationId == null || invitationId.isEmpty()) {
            return error("缺少邀请ID");
        }
        boolean ok = consultInvitationService.acceptInvitation(invitationId);
        return ok ? success() : error("接受失败，邀请不存在或已接受");
    }

    private String str(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val == null ? "" : String.valueOf(val).trim();
    }

    private List<Long> parseLongList(Object value) {
        List<Long> result = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                Long id = toLong(item);
                if (id != null && !result.contains(id)) {
                    result.add(id);
                }
            }
        }
        return result;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.valueOf(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
