package com.sky.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.sky.entity.User;

@Mapper 
public interface UserMapper {

  /**
   * 根据openid查询用户信息
   * 
   * @param openid 微信用户openid
   * @return 用户信息
   */
  @Select("select * from user where openid = #{openid}")
  User getByOpenid(String openid);

  /**
   * 插入数据
   * 
   * @param user 用户信息
   */
   void insert(User user);
   	/**
     * 根据动态条件统计用户数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
