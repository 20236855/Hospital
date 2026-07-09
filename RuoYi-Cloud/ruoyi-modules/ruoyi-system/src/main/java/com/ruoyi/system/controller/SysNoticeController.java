package com.ruoyi.system.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.api.domain.SysDept;
import com.ruoyi.system.api.model.LoginUser;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.system.service.ISysNoticeReadService;
import com.ruoyi.system.service.ISysNoticeService;

/**
 * 公告 信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping({"/notice", "/system/notice"})
public class SysNoticeController extends BaseController
{
    private static final String CONSULT_INVITE_BIZ_TYPE = "CONSULT_INVITE";

    private static final int CONSULT_INVITE_LIMIT = 50;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private ISysNoticeService noticeService;

    @Autowired
    private ISysNoticeReadService noticeReadService;

    @Autowired
    private ISysDeptService deptService;

    /**
     * 获取通知公告列表
     */
    @RequiresPermissions("system:notice:list")
    @GetMapping("/list")
    public TableDataInfo list(SysNotice notice)
    {
        startPage();
        List<SysNotice> list = noticeService.selectNoticeList(notice);
        return getDataTable(list);
    }

    /**
     * 根据通知公告编号获取详细信息
     */
    @GetMapping(value = "/{noticeId}")
    public AjaxResult getInfo(@PathVariable Long noticeId)
    {
        return success(noticeService.selectNoticeById(noticeId));
    }

    /**
     * 新增通知公告
     */
    @RequiresPermissions("system:notice:add")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysNotice notice)
    {
        notice.setCreateBy(SecurityUtils.getUsername());
        return toAjax(noticeService.insertNotice(notice));
    }

    /**
     * 修改通知公告
     */
    @RequiresPermissions("system:notice:edit")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysNotice notice)
    {
        notice.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(noticeService.updateNotice(notice));
    }

    /**
     * 首页顶部公告列表（返回全部正常公告，带当前用户已读标记，最多5条）
     */
    @GetMapping("/listTop")
    @ResponseBody
    public AjaxResult listTop()
    {
        Long userId = SecurityUtils.getUserId();
        List<SysNotice> list = noticeReadService.selectNoticeListWithReadStatus(userId, 5);
        long unreadCount = list.stream().filter(n -> !n.getIsRead()).count();
        AjaxResult result = AjaxResult.success(list);
        result.put("unreadCount", unreadCount);
        return result;
    }

    /**
     * 标记公告已读
     */
    @PostMapping("/markRead")
    @ResponseBody
    public AjaxResult markRead(Long noticeId)
    {
        Long userId = SecurityUtils.getUserId();
        noticeReadService.markRead(noticeId, userId);
        return success();
    }

    /**
     * 批量标记已读
     */
    @PostMapping("/markReadAll")
    @ResponseBody
    public AjaxResult markReadAll(String ids)
    {
        Long userId = SecurityUtils.getUserId();
        Long[] noticeIds = Convert.toLongArray(ids);
        noticeReadService.markReadBatch(userId, noticeIds);
        return success();
    }

    /**
     * 发起联合会诊邀请。
     *
     * 不新建业务表：邀请正文存入 sys_notice.notice_content，接受状态复用 sys_notice_read。
     */
    @PostMapping("/consult/invite")
    @ResponseBody
    public AjaxResult createConsultInvitation(@RequestBody Map<String, Object> params)
    {
        String senderDoctorName = trimToEmpty(params.get("senderDoctorName"));
        String location = trimToEmpty(params.get("location"));
        String importantInfo = trimToEmpty(params.get("importantInfo"));
        Long senderDoctorId = toLong(params.get("senderDoctorId"));
        List<Long> targetDeptIds = parseLongList(params.get("targetDeptIds"));

        if (StringUtils.isBlank(senderDoctorName))
        {
            return error("请填写发起会诊的医生");
        }
        if (StringUtils.isBlank(location))
        {
            return error("请填写会诊地点");
        }
        if (targetDeptIds.isEmpty())
        {
            return error("请选择需要联合会诊的科室");
        }

        List<String> targetDeptNames = resolveDeptNames(targetDeptIds);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("bizType", CONSULT_INVITE_BIZ_TYPE);
        payload.put("senderDoctorId", senderDoctorId);
        payload.put("senderDoctorName", senderDoctorName);
        payload.put("location", location);
        payload.put("importantInfo", importantInfo);
        payload.put("targetDeptIds", targetDeptIds);
        payload.put("targetDeptNames", targetDeptNames);
        payload.put("targetDeptTokens", buildDeptTokens(targetDeptIds));
        payload.put("inviterUserId", SecurityUtils.getUserId());
        payload.put("inviterUserName", SecurityUtils.getUsername());

        SysNotice notice = new SysNotice();
        notice.setNoticeTitle(truncate("联合会诊邀请 - " + senderDoctorName, 50));
        notice.setNoticeType("1");
        notice.setStatus("0");
        notice.setCreateBy(SecurityUtils.getUsername());
        try
        {
            notice.setNoticeContent(OBJECT_MAPPER.writeValueAsString(payload));
        }
        catch (JsonProcessingException e)
        {
            return error("联合会诊邀请内容生成失败");
        }

        return toAjax(noticeService.insertNotice(notice));
    }

    /**
     * 查询当前医生所在科室收到的联合会诊邀请。
     */
    @GetMapping("/consult/invitations")
    @ResponseBody
    public AjaxResult consultInvitationList()
    {
        Long deptId = currentDeptId();
        if (deptId == null)
        {
            AjaxResult result = AjaxResult.success(new ArrayList<>());
            result.put("unreadCount", 0);
            return result;
        }

        List<SysNotice> notices = noticeService.selectConsultInvitationList(
            SecurityUtils.getUserId(), deptToken(deptId), CONSULT_INVITE_LIMIT);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (SysNotice notice : notices)
        {
            Map<String, Object> payload = parseConsultPayload(notice);
            if (payload == null)
            {
                continue;
            }
            payload.put("noticeId", notice.getNoticeId());
            payload.put("noticeTitle", notice.getNoticeTitle());
            payload.put("createBy", notice.getCreateBy());
            payload.put("createTime", notice.getCreateTime());
            payload.put("accepted", notice.getIsRead());
            rows.add(payload);
        }

        long unreadCount = rows.stream().filter(item -> !Boolean.TRUE.equals(item.get("accepted"))).count();
        AjaxResult result = AjaxResult.success(rows);
        result.put("unreadCount", unreadCount);
        return result;
    }

    /**
     * 接受联合会诊邀请。接受状态复用 sys_notice_read。
     */
    @PostMapping("/consult/accept")
    @ResponseBody
    public AjaxResult acceptConsultInvitation(Long noticeId)
    {
        if (noticeId == null)
        {
            return error("缺少邀请ID");
        }
        Long deptId = currentDeptId();
        if (deptId == null)
        {
            return error("当前账号未绑定科室，无法接受联合会诊邀请");
        }

        SysNotice notice = noticeService.selectNoticeById(noticeId);
        if (notice == null || !"0".equals(notice.getStatus()) || !isConsultInviteForDept(notice, deptId))
        {
            return error("该联合会诊邀请不存在或不属于当前科室");
        }

        noticeReadService.markRead(noticeId, SecurityUtils.getUserId());
        return success();
    }

    /**
     * 已读用户列表数据
     */
    @RequiresPermissions("system:notice:list")
    @GetMapping("/readUsers/list")
    @ResponseBody
    public TableDataInfo readUsersList(Long noticeId, String searchValue)
    {
        startPage();
        List<?> list = noticeReadService.selectReadUsersByNoticeId(noticeId, searchValue);
        return getDataTable(list);
    }

    /**
     * 删除通知公告
     */
    @RequiresPermissions("system:notice:remove")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@PathVariable Long[] noticeIds)
    {
        noticeReadService.deleteByNoticeIds(noticeIds);
        return toAjax(noticeService.deleteNoticeByIds(noticeIds));
    }

    private Long currentDeptId()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null)
        {
            return null;
        }
        if (loginUser.getDeptId() != null)
        {
            return loginUser.getDeptId();
        }
        if (loginUser.getSysUser() != null)
        {
            return loginUser.getSysUser().getDeptId();
        }
        return null;
    }

    private boolean isConsultInviteForDept(SysNotice notice, Long deptId)
    {
        Map<String, Object> payload = parseConsultPayload(notice);
        if (payload == null)
        {
            return false;
        }
        String targetDeptTokens = trimToEmpty(payload.get("targetDeptTokens"));
        return targetDeptTokens.contains(deptToken(deptId));
    }

    private Map<String, Object> parseConsultPayload(SysNotice notice)
    {
        if (notice == null || StringUtils.isBlank(notice.getNoticeContent()))
        {
            return null;
        }
        try
        {
            Map<String, Object> payload = OBJECT_MAPPER.readValue(
                notice.getNoticeContent(), new TypeReference<Map<String, Object>>() {});
            if (!CONSULT_INVITE_BIZ_TYPE.equals(payload.get("bizType")))
            {
                return null;
            }
            return payload;
        }
        catch (JsonProcessingException e)
        {
            return null;
        }
    }

    private List<Long> parseLongList(Object value)
    {
        List<Long> result = new ArrayList<>();
        if (value instanceof List<?> list)
        {
            for (Object item : list)
            {
                Long id = toLong(item);
                if (id != null && !result.contains(id))
                {
                    result.add(id);
                }
            }
            return result;
        }
        String text = trimToEmpty(value);
        if (StringUtils.isNotBlank(text))
        {
            for (String part : text.split(","))
            {
                Long id = toLong(part);
                if (id != null && !result.contains(id))
                {
                    result.add(id);
                }
            }
        }
        return result;
    }

    private List<String> resolveDeptNames(List<Long> deptIds)
    {
        List<String> names = new ArrayList<>();
        for (Long deptId : deptIds)
        {
            SysDept dept = deptService.selectDeptById(deptId);
            names.add(dept != null ? dept.getDeptName() : String.valueOf(deptId));
        }
        return names;
    }

    private String buildDeptTokens(List<Long> deptIds)
    {
        StringBuilder builder = new StringBuilder("|");
        for (Long deptId : deptIds)
        {
            builder.append(deptId).append("|");
        }
        return builder.toString();
    }

    private String deptToken(Long deptId)
    {
        return "|" + deptId + "|";
    }

    private Long toLong(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Number number)
        {
            return number.longValue();
        }
        String text = trimToEmpty(value);
        if (StringUtils.isBlank(text))
        {
            return null;
        }
        try
        {
            return Long.valueOf(text);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private String trimToEmpty(Object value)
    {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String truncate(String text, int maxLength)
    {
        if (text == null || text.length() <= maxLength)
        {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
