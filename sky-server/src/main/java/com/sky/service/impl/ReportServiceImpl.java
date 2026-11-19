package com.sky.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang.StringUtils;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
  @Autowired
  private OrderMapper orderMapper;
  @Autowired
  private UserMapper userMapper;

  /*
   * 营业额统计
   *
   * @param begin 开始日期
   *
   * @param end 结束日期
   *
   * @return
   */
  public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
    // 当前集合用于存放从begin到End范围内的每天的日期
    List<LocalDate> dateList = new ArrayList<>();
    dateList.add(begin);
    while (!begin.equals(end)) {
      // 日期计算,计算指定日期的后一天对应的日期
      begin = begin.plusDays(1);
      dateList.add(begin);
    }
    // 存放每天的营业额
    List<Double> turnoverList = new ArrayList<>();
    for (LocalDate date : dateList) {
      // 从数据库中查询该日期的营业额(订单状态显示为已完成的订单金额合计)
      LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
      LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
      // select sum(amount) from orders where order_status = 5 and order_time >
      // beginTime and order_time < endTime
      Map<String, Object> map = new HashMap<>();
      map.put("begin", beginTime);
      map.put("end", endTime);
      map.put("status", Orders.COMPLETED);
      Double turnover = orderMapper.sumByMap(map);
      turnover = turnover == null ? 0.0 : turnover;
      turnoverList.add(turnover);
    }

    return TurnoverReportVO
        .builder()
        .dateList(StringUtils.join(dateList, ","))
        .turnoverList(StringUtils.join(turnoverList, ","))
        .build();
  }

  /*
   * 用户统计
   *
   * @param begin 统计的开始日期
   *
   * @param end 统计的结束日期
   *
   * @return
   */
  public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
    // 当前集合用于存放从begin到End范围内的每天的日期
    List<LocalDate> dateList = new ArrayList<>();
    dateList.add(begin);
    while (!begin.equals(end)) {
      // 日期计算,计算指定日期的后一天对应的日期
      begin = begin.plusDays(1);
      dateList.add(begin);

    }
    // 存放新老用户数量
    List<Integer> newUserList = new ArrayList<>();
    List<Integer> totalUserList = new ArrayList<>();
    for (LocalDate date : dateList) {
      LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
      LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
      // 新增用户数量 select count(id) from user where create_time > beginTime and
      // create_time < endTime
      Integer newUser = getUserCount(beginTime, endTime);
      // 总用户数量 select count(id) from user where create_time < beginTime
      Integer totalUser = getUserCount(null, endTime);
      newUserList.add(newUser);
      totalUserList.add(totalUser);
    }
    return UserReportVO.builder()
        .dateList(StringUtils.join(dateList, ","))
        .newUserList(StringUtils.join(newUserList, ","))
        .totalUserList(StringUtils.join(totalUserList, ","))
        .build();
  }

  private Integer getUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
    Map<String, Object> map = new HashMap<>();
    map.put("begin", beginTime);
    map.put("end", endTime);
    return userMapper.countByMap(map);
  }

  /*
   * 订单统计
   *
   * @param begin 统计的开始日期
   *
   * @param end 统计的结束日期
   *
   * @return
   */
  public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
    List<LocalDate> dateList = new ArrayList<>();
    dateList.add(begin);
    while (!begin.equals(end)) {
      begin = begin.plusDays(1);
      dateList.add(begin);
    }
    // 存放每天的订单数量
    List<Integer> orderCountList = new ArrayList<>();
    // 每天有效订单数量
    List<Integer> validOrderCountList = new ArrayList<>();
    for (LocalDate date : dateList) {
      // 从数据库中查询该日期的订单数量(订单状态显示为已完成的订单数量)
      // select count(id) from orders where order_status = 5 and order_time >
      // beginTime and order_time < endTime
      LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
      LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
      // select count(id) from orders where order_status = 5 and order_time >
      // beginTime and order_time < endTime
      Integer orderCount = getOrderCount(beginTime, endTime, null);
      // 从数据库中查询该日期的有效订单数量(订单状态显示为已完成的订单数量)
      // select count(id) from orders where order_status = 5 and order_time >
      // beginTime and order_time < endTime and pay_status = 1
      Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
      orderCountList.add(orderCount);
      validOrderCountList.add(validOrderCount);

    }
    // 时间区间内的总订单数
    Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
    // 时间区间内的有效订单数
    Integer totalValidOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
    // 订单完成率
    Double orderCompletionRate = 0.0;
    if (totalOrderCount != 0) {
      orderCompletionRate = totalValidOrderCount.doubleValue() / totalOrderCount;
    }
    return OrderReportVO.builder()
        .dateList(StringUtils.join(dateList, ","))
        .orderCountList(StringUtils.join(orderCountList, ","))
        .validOrderCountList(StringUtils.join(validOrderCountList, ","))
        .totalOrderCount(totalOrderCount)
        .validOrderCount(totalValidOrderCount)
        .orderCompletionRate(orderCompletionRate)
        .build();
  }

  /**
   * 根据时间区间统计指定状态的订单数量
   * 
   * @param beginTime
   * @param endTime
   * @param status
   * @return
   */
  private Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
    Map<String, Object> map = new HashMap<>();
    map.put("begin", beginTime);
    map.put("end", endTime);
    map.put("status", status);
    return orderMapper.countByMap(map);

  }

  /*
   * 销量排名
   *
   * @param begin 统计的开始日期
   *
   * @param end 统计的结束日期
   *
   * @param number 显示的条数
   *
   * @return
   */
  public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
    LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
    List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
    String nameList = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.joining(","));
    List<Integer> numberList = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
    return SalesTop10ReportVO.builder()
        .nameList(nameList)
        .numberList(StringUtils.join(numberList, ","))
        .build();
  }
}
