package com.ruoyi.auth.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.constant.CacheConstants;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.enums.UserStatus;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.ip.IpUtils;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.RemoteUserService;
import com.ruoyi.his.api.RemotePatientService;
import com.ruoyi.his.api.RemoteDoctorService;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.api.model.LoginUser;

/**
 * 登录校验方法
 * 
 * @author ruoyi
 */
@Component
public class SysLoginService
{
    @Autowired
    private RemoteUserService remoteUserService;

    @Autowired
    private RemotePatientService remotePatientService;
    
    @Autowired
    private RemoteDoctorService remoteDoctorService;

    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private SysRecordLogService recordLogService;

    @Autowired
    private RedisService redisService;

    /**
     * 登录
     */
    public LoginUser login(String username, String password)
    {
        // 用户名或密码为空 错误
        if (StringUtils.isAnyBlank(username, password))
        {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户/密码必须填写");
            throw new ServiceException("用户/密码必须填写");
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户密码不在指定范围");
            throw new ServiceException("用户密码不在指定范围");
        }
        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户名不在指定范围");
            throw new ServiceException("用户名不在指定范围");
        }
        // IP黑名单校验
        String blackStr = Convert.toStr(redisService.getCacheObject(CacheConstants.SYS_LOGIN_BLACKIPLIST));
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr()))
        {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "很遗憾，访问IP已被列入系统黑名单");
            throw new ServiceException("很遗憾，访问IP已被列入系统黑名单");
        }
        // 查询用户信息
        R<LoginUser> userResult = remoteUserService.getUserInfo(username, SecurityConstants.INNER);

        if (R.FAIL == userResult.getCode())
        {
            throw new ServiceException(userResult.getMsg());
        }

        LoginUser userInfo = userResult.getData();
        SysUser user = userResult.getData().getSysUser();
        if (UserStatus.DELETED.getCode().equals(user.getDelFlag()))
        {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "对不起，您的账号已被删除");
            throw new ServiceException("对不起，您的账号：" + username + " 已被删除");
        }
        if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户已停用，请联系管理员");
            throw new ServiceException("对不起，您的账号：" + username + " 已停用");
        }
        passwordService.validate(user, password);
        recordLogService.recordLogininfor(username, Constants.LOGIN_SUCCESS, "登录成功");
        recordLoginInfo(user.getUserId());
        
        // 判断是否需要完善信息 - 通过角色标识判断用户类型
        boolean isPatient = false;
        boolean isDoctor = false;
        boolean needComplete = false;
        Long userId = user.getUserId();
        Long deptId = null;
        
        // 检查用户角色标识
        if (userInfo.getRoles() != null)
        {
            for (String roleKey : userInfo.getRoles())
            {
                if ("patient".equals(roleKey))
                {
                    isPatient = true;
                }
                if ("doctor".equals(roleKey))
                {
                    isDoctor = true;
                }
            }
        }
        
        if (isPatient) {
            R<Map<String, Object>> patientR = remotePatientService.getPatientByUserId(userId, SecurityConstants.INNER);
            if (R.FAIL == patientR.getCode()) {
                throw new ServiceException(patientR.getMsg());
            }
            needComplete = patientR.getData() == null;
        } else if (isDoctor) {
            if ("0".equals(user.getStatus())) {
                // 如果用户已经有 deptId，就不需要完善信息
                if (user.getDeptId() != null) {
                    needComplete = false;
                    deptId = user.getDeptId();
                    userInfo.setDeptId(deptId);
                } else {
                    R<Map<String, Object>> dr = remoteDoctorService.getDoctorByUserId(userId, SecurityConstants.INNER);
                    if (R.FAIL == dr.getCode()) {
                        throw new ServiceException(dr.getMsg());
                    }
                    needComplete = dr.getData() == null;
                    // 如果医生数据存在，把deptId存入loginUser用于数据权限
                    if (!needComplete && dr.getData() != null) {
                        Object deptIdObj = dr.getData().get("deptId");
                        if (deptIdObj != null) {
                            deptId = Long.valueOf(deptIdObj.toString());
                            userInfo.setDeptId(deptId);
                            user.setDeptId(deptId);
                        }
                    }
                }
            } else {
                // 医生状态禁用直接拦截登录
                recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "账号待审核，请联系管理员");
                throw new ServiceException("对不起，您的账号：" + username + " 待审核");
            }
        }
        
        userInfo.setNeedCompleteInfo(needComplete);
        return userInfo;
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId)
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        // 更新用户登录IP
        sysUser.setLoginIp(IpUtils.getIpAddr());
        // 更新用户登录时间
        sysUser.setLoginDate(DateUtils.getNowDate());
        remoteUserService.recordUserLogin(sysUser, SecurityConstants.INNER);
    }

    /**
     * 退出
     */
    public void logout(String loginName)
    {
        recordLogService.recordLogininfor(loginName, Constants.LOGOUT, "退出成功");
    }

    /**
     * 解锁
     */
    public void unlock(String password)
    {
        String username = SecurityUtils.getUsername();
        // 或密码为空 错误
        if (StringUtils.isEmpty(password))
        {
            throw new ServiceException("密码不能为空");
        }
        // 查询用户信息
        R<LoginUser> userResult = remoteUserService.getUserInfo(username, SecurityConstants.INNER);

        if (R.FAIL == userResult.getCode())
        {
            throw new ServiceException(userResult.getMsg());
        }

        SysUser user = userResult.getData().getSysUser();
        if (!SecurityUtils.matchesPassword(password, user.getPassword()))
        {
            throw new ServiceException("密码错误，请重新输入");
        }
    }

    /**
     * 注册
     */
    public void register(String username, String password)
    {
        // 用户名或密码为空 错误
        if (StringUtils.isAnyBlank(username, password))
        {
            throw new ServiceException("用户/密码必须填写");
        }
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            throw new ServiceException("账户长度必须在2到20个字符之间");
        }
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            throw new ServiceException("密码长度必须在5到20个字符之间");
        }

        // 注册用户信息
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);
        sysUser.setNickName(username);
        sysUser.setPwdUpdateDate(DateUtils.getNowDate());
        sysUser.setPassword(SecurityUtils.encryptPassword(password));
        R<?> registerResult = remoteUserService.registerUserInfo(sysUser, SecurityConstants.INNER);

        if (R.FAIL == registerResult.getCode())
        {
            throw new ServiceException(registerResult.getMsg());
        }
        recordLogService.recordLogininfor(username, Constants.REGISTER, "注册成功");
    }

    /**
     * 注册（新接口）
     */
    public void register(com.ruoyi.auth.form.RegisterBody registerBody)
    {
        String username = registerBody.getUsername();
        String password = registerBody.getPassword();
        String userType = registerBody.getUserType();
        String nickName = registerBody.getNickName();
        String phonenumber = registerBody.getPhonenumber();

        // 用户名或密码为空 错误
        if (StringUtils.isAnyBlank(username, password, userType, nickName, phonenumber))
        {
            throw new ServiceException("用户/密码/身份/姓名/手机号必须填写");
        }
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            throw new ServiceException("账户长度必须在2到20个字符之间");
        }
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            throw new ServiceException("密码长度必须在5到20个字符之间");
        }

        // 禁止注册admin账号
        if ("admin".equals(username))
        {
            throw new ServiceException("超级管理员账号不开放自助注册");
        }

        // 注册用户信息，user_type保持默认值"00"
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);
        sysUser.setNickName(nickName);
        sysUser.setPhonenumber(phonenumber);
        sysUser.setPwdUpdateDate(DateUtils.getNowDate());
        sysUser.setPassword(SecurityUtils.encryptPassword(password));
        sysUser.setUserType(userType); // 临时设置用于角色分配，后续通过角色判断类型

        // 设置用户类型相关信息
        if ("patient".equals(userType))
        {
            sysUser.setStatus(UserStatus.OK.getCode()); // 患者直接启用
        }
        else if ("doctor".equals(userType))
        {
            sysUser.setStatus(UserStatus.OK.getCode()); // 医生需先登录完善档案
        }
        else
        {
            throw new ServiceException("不支持的用户身份类型");
        }

        R<?> registerResult = remoteUserService.registerUserInfo(sysUser, SecurityConstants.INNER);

        if (R.FAIL == registerResult.getCode())
        {
            throw new ServiceException(registerResult.getMsg());
        }
        recordLogService.recordLogininfor(username, Constants.REGISTER, "注册成功");
    }
}
