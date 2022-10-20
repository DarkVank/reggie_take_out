package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.BaseContext;
import com.itheima.common.R;
import com.itheima.dto.OrdersDto;
import com.itheima.entity.Orders;
import com.itheima.entity.ShoppingCart;
import com.itheima.service.OrdersService;
import com.itheima.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.callback.LanguageCallback;
import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/order")
@EnableTransactionManagement
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    /**
     * 后台订单信息
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> employeePage(int page, int pageSize, Long number, Date beginTime,Date endTime){

        String numbers = String.valueOf(number);
        //分页构造器
        Page<Orders> ordersPage = new Page<>(page,pageSize);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(numbers .length() == 19, Orders::getNumber,number);
        queryWrapper.between(beginTime != null && endTime !=null,Orders::getCheckoutTime,beginTime,endTime);
        queryWrapper.orderByDesc(Orders::getCheckoutTime);

        ordersService.page(ordersPage,queryWrapper);

        return R.success(ordersPage);
    }
    /**
     * 移动端订单信息
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单信息");

        ordersService.submit(orders);
        return R.success("下单成功");
    }
    /**
     * 历史订单展示
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> page(int page ,int pageSize){
        log.info("订单信息");

       Page<OrdersDto> ordersDtoPage = ordersService.getWithDetail(page,pageSize);

        return R.success(ordersDtoPage);

    }
}
