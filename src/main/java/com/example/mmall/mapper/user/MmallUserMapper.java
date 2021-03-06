package com.example.mmall.mapper.user;
import com.example.mmall.model.user.MmallUser;
//import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MmallUserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(MmallUser record);

    int insertSelective(MmallUser record);

    MmallUser selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MmallUser record);

    int updateByPrimaryKey(MmallUser record);

    List<MmallUser> getUserInfoList();

    MmallUser checkUsername(String username);

    MmallUser getUserInfo(int userId);

    MmallUser selectLogin(String username, String password);
}