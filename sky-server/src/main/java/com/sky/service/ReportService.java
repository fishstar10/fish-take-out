package com.sky.service;

import java.time.LocalDate;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

public interface ReportService {
  /*
   * 营业额统计
   * 
   * @param begin 统计的开始日期
   * 
   * @param end 统计的结束日期
   * 
   * @return
   */
  TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

  /*
   * 用户统计
   * 
   * @param begin 统计的开始日期
   * 
   * @param end 统计的结束日期
   * 
   * @return
   */
  UserReportVO getUserStatistics(LocalDate begin, LocalDate end);
 /*
   * 订单统计
   * 
   * @param begin 统计的开始日期
   * 
   * @param end 统计的结束日期
   * 
   * @return
   */
  OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);


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
  SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);

}
