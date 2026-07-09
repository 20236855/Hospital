package com.ruoyi.hisdoctor.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.model.LoginUser;

/**
 * 联合会诊邀请服务（Redis 存储，30 分钟自动过期）
 */
@Service
public class ConsultInvitationService {

    private static final Logger log = LoggerFactory.getLogger(ConsultInvitationService.class);

    private static final String PREFIX_INVITATION = "consult:invitation:";
    private static final String PREFIX_DEPT = "consult:invitation:dept:";
    private static final long TTL_MINUTES = 30;

    @Autowired
    private RedisService redisService;

    /**
     * 发起联合会诊邀请
     */
    public Map<String, Object> sendInvitation(String senderDoctorName, String location,
            String importantInfo, Long senderDoctorId, List<Long> targetDeptIds, List<String> targetDeptNames) {

        String invitationId = UUID.randomUUID().toString().replace("-", "");
        Long inviterUserId = SecurityUtils.getUserId();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("invitationId", invitationId);
        payload.put("senderDoctorId", senderDoctorId);
        payload.put("senderDoctorName", senderDoctorName);
        payload.put("location", location);
        payload.put("importantInfo", importantInfo);
        payload.put("targetDeptIds", targetDeptIds);
        payload.put("targetDeptNames", targetDeptNames);
        payload.put("inviterUserId", inviterUserId);
        payload.put("inviterUserName", SecurityUtils.getUsername());
        payload.put("acceptedUserIds", new ArrayList<>());
        payload.put("acceptedUserNames", new ArrayList<>());
        payload.put("createTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 直接存 Map 对象，让 fastjson 原生序列化
        try {
            redisService.setCacheObject(PREFIX_INVITATION + invitationId, payload, TTL_MINUTES, TimeUnit.MINUTES);
            log.info("已存储邀请 {}", invitationId);
        } catch (Exception e) {
            log.error("存储邀请 {} 失败: {}", invitationId, e.toString(), e);
            throw new RuntimeException("Redis存储邀请失败: " + e.getMessage(), e);
        }

        // 加入目标科室的邀请集合（用逗号分隔的纯 String 存储）
        for (Long deptId : targetDeptIds) {
            String deptKey = PREFIX_DEPT + deptId;
            try {
                String joined = redisService.getCacheObject(deptKey);
                Set<String> ids = joined != null
                        ? new LinkedHashSet<>(Arrays.asList(joined.split(",")))
                        : new LinkedHashSet<>();
                ids.add(invitationId);
                redisService.setCacheObject(deptKey, String.join(",", ids), TTL_MINUTES, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.error("更新科室集合 {} 失败: {}", deptKey, e.toString(), e);
                throw new RuntimeException("Redis更新科室集合失败: " + e.getMessage(), e);
            }
        }

        return payload;
    }

    /**
     * 查询当前医生所在科室收到的联合会诊邀请
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listMyInvitations() {
        Long deptId = currentDeptId();
        if (deptId == null) {
            return Collections.emptyList();
        }

        String deptKey = PREFIX_DEPT + deptId;
        String joined = redisService.getCacheObject(deptKey);
        if (joined == null || joined.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> invitationIds = new LinkedHashSet<>(Arrays.asList(joined.split(",")));
        Long currentUserId = SecurityUtils.getUserId();
        List<Map<String, Object>> result = new ArrayList<>();

        for (String invId : invitationIds) {
            // 直接取回 Map 对象
            Object raw = redisService.getCacheObject(PREFIX_INVITATION + invId);
            if (raw == null) {
                continue;
            }
            Map<String, Object> item;
            if (raw instanceof Map) {
                item = (Map<String, Object>) raw;
            } else {
                continue;
            }

            List<Object> acceptedUserIds = (List<Object>) item.getOrDefault("acceptedUserIds", Collections.emptyList());
            boolean accepted = false;
            for (Object uid : acceptedUserIds) {
                if (uid instanceof Number && ((Number) uid).longValue() == currentUserId) {
                    accepted = true;
                    break;
                }
            }
            item.put("accepted", accepted);
            result.add(item);
        }

        return result;
    }

    /**
     * 接受联合会诊邀请
     */
    @SuppressWarnings("unchecked")
    public boolean acceptInvitation(String invitationId) {
        Object raw = redisService.getCacheObject(PREFIX_INVITATION + invitationId);
        if (!(raw instanceof Map)) {
            return false;
        }
        Map<String, Object> payload = (Map<String, Object>) raw;
        Long currentUserId = SecurityUtils.getUserId();

        List<Object> acceptedUserIds = (List<Object>) payload.getOrDefault("acceptedUserIds", new ArrayList<>());
        for (Object uid : acceptedUserIds) {
            if (uid instanceof Number && ((Number) uid).longValue() == currentUserId) {
                return false; // 已接受过
            }
        }

        acceptedUserIds.add(currentUserId);
        List<String> acceptedUserNames = (List<String>) payload.getOrDefault("acceptedUserNames", new ArrayList<>());
        acceptedUserNames.add(SecurityUtils.getUsername());
        payload.put("acceptedUserIds", acceptedUserIds);
        payload.put("acceptedUserNames", acceptedUserNames);

        Long remaining = redisService.getExpire(PREFIX_INVITATION + invitationId);
        if (remaining > 0) {
            redisService.setCacheObject(PREFIX_INVITATION + invitationId, payload, remaining, TimeUnit.SECONDS);
        }
        return true;
    }

    private Long currentDeptId() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getDeptId() != null) {
            return loginUser.getDeptId();
        }
        if (loginUser != null && loginUser.getSysUser() != null
                && loginUser.getSysUser().getDeptId() != null) {
            return loginUser.getSysUser().getDeptId();
        }
        return null;
    }
}
