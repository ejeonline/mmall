package com.example.mmall.service.user.impl;

import com.example.mmall.common.exception.UserBizException;
import com.example.mmall.mapper.base.BaseEmployeeMapper;
import com.example.mmall.mapper.sys.SysFunctionCellMapper;
import com.example.mmall.mapper.user.MmallUserMapper;
import com.example.mmall.model.base.BaseEmployee;
import com.example.mmall.model.sys.SysFunctionCell;
import com.example.mmall.model.user.MmallUser;
import com.example.mmall.service.user.MmallUserSerice;
import com.example.mmall.util.JwtTokenUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class MmallUserSericeImpl implements MmallUserSerice {

    @Autowired
    MmallUserMapper mmallUserMapper;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    SysFunctionCellMapper sysFunctionCellMapper;
    @Autowired
    BaseEmployeeMapper baseEmployeeMapper;

    @Override
    public boolean insertMmallUser(String username, String password, String email, String phone, String question, String answer, int role) {
        MmallUser mmallUser = new MmallUser();
        mmallUser.setUsername(username);
        mmallUser.setPassword(password);
        mmallUser.setEmail(email);
        mmallUser.setPhone(phone);
        mmallUser.setQuestion(question);
        mmallUser.setAnswer(answer);
        mmallUser.setRole(role);
        mmallUser.setCreateTime(new Date());
        mmallUser.setUpdateTime(new Date());
        int i = mmallUserMapper.insertSelective(mmallUser);
        return i>0 ? true:false;
    }


    @Override
    public PageInfo<MmallUser> getUserInfoList(int pageNum,int pageSize) {

        PageHelper.startPage(pageNum,pageSize);

        List<MmallUser> getListUser = mmallUserMapper.getUserInfoList();
        PageInfo<MmallUser> pageInfo = new PageInfo<>(getListUser);

        return pageInfo;
    }

    /** 根据用户id修改用户信息资料
     * @author zy
     * @date 9:53 2019/4/8
     */
    @Override
    public boolean updateUserInfo(int userId,String phone) {
        MmallUser mmallUser = new MmallUser();
        mmallUser.setId(userId);
        mmallUser.setQuestion(phone);
        int i = mmallUserMapper.updateByPrimaryKeySelective(mmallUser);
        return i>0 ? true:false;
    }

    //登陆
    @Override
    public String login(String username, String password) throws UserBizException{
        String token = null;
        //查询此账号是否存在
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if(userDetails == null){
            throw new UserBizException(UserBizException.USER_IS_NULL,"用户不存在");
        }
        //校验账号密码
        if(!password.equals(userDetails.getPassword())){
            throw UserBizException.USER_LOGIN_ERROR;
        }
        try {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            token = jwtTokenUtil.generateToken(userDetails);

            //保存在redis中
          /*  if(!StringUtil.isEmpty(token)){
                RedisClientUtil.set(CommonConst.SERVICE_TOKEN+username,token,30);
            }*/
        } catch (AuthenticationException e) {
            log.error("登录异常:{}", e.getMessage());
        }
        return token;
    }

    @Override
    public MmallUser selectByUserName(String userName) {
        return mmallUserMapper.checkUsername(userName);
    }

    @Override
    public List<SysFunctionCell> getPermissionList(String userId) {
        return sysFunctionCellMapper.getPermissionList(userId);
    }

    @Override
    public boolean etidBatchUserNoStatusById(int employeeStatus, String[] userIdArr) {
        try {

            Map<String,Object> map = new HashMap<>();
            List<String> list = new ArrayList<>();
            for(int i=0;i<userIdArr.length;i++){
                String userId = userIdArr[i];
                list.add(userId);
            }
            map.put("list",list);
            map.put("employeeStatus",employeeStatus);
            int i = baseEmployeeMapper.updateBatchUserNoStatusById(map);
            return i>0 ?true:false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public PageInfo<BaseEmployee> selectUserInfoList(String keyword, String employeeStatus, Date startEntryTime,
                                                     Date endEntryTime, String[] locationArr, int pageNum, int pageSize) {

        Map<String, Object> map = new HashMap<>();
        List<String> list = new ArrayList<>();

        map.put("keyword", keyword);
        map.put("employeeStatus", employeeStatus);
        map.put("startEntryTime", startEntryTime);
        map.put("endEntryTime", endEntryTime);

        if (!StringUtil.isEmpty(locationArr.toString())) {
            for (int i = 0; i < locationArr.length; i++) {
                String location = locationArr[i];
                list.add(location);
            }
        }

        map.put("list", list);
        PageHelper.startPage(pageNum, pageSize);
        List<BaseEmployee> listData = baseEmployeeMapper.getUserInfoList(map);
        PageInfo<BaseEmployee> pageInfo = new PageInfo<>(listData);
        return pageInfo;
    }

}
