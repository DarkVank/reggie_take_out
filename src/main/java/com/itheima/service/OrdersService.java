package com.itheima.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.dto.OrdersDto;
import com.itheima.entity.Orders;

public interface OrdersService extends IService<Orders> {
    //保存订单以及明细表
    void submit(Orders orders);
    //历史订单查询
    Page<OrdersDto> getWithDetail(int page, int pageSize);
}
