package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.BaseContext;
import com.itheima.common.R;
import com.itheima.dto.OrdersDto;
import com.itheima.entity.AddressBook;
import com.itheima.entity.OrderDetail;
import com.itheima.entity.Orders;
import com.itheima.entity.ShoppingCart;
import com.itheima.mapper.OrdersMapper;
import com.itheima.service.AddressBookService;
import com.itheima.service.OrderDetailService;
import com.itheima.service.OrdersService;
import com.itheima.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Override
    @Transactional
    public void submit(Orders orders) {
        //获取用户id
        Long currentId = BaseContext.getCurrentId();
        //获取购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        //获取当前地址
        LambdaQueryWrapper<AddressBook> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(AddressBook::getId,orders.getAddressBookId());

        AddressBook addressBook = addressBookService.getOne(queryWrapper1);

        //判断购物车中是否有数据
        //有，封装订单表
        if(shoppingCarts != null){
           //生成订单号
            long orderId = IdWorker.getId();
            //订单明细
            AtomicInteger amount = new AtomicInteger(0);

            List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
                OrderDetail orderDetail = new OrderDetail();

                orderDetail.setOrderId(orderId);
                orderDetail.setNumber(item.getNumber());
                orderDetail.setDishFlavor(item.getDishFlavor());
                orderDetail.setDishId(item.getDishId());
                orderDetail.setSetmealId(item.getSetmealId());
                orderDetail.setName(item.getName());
                orderDetail.setImage(item.getImage());
                orderDetail.setAmount(item.getAmount());
                amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());

                return orderDetail;
            }).collect(Collectors.toList());

            //订单表插入数据
            orders.setId(orderId);
            orders.setOrderTime(LocalDateTime.now());
            orders.setCheckoutTime(LocalDateTime.now());
            orders.setStatus(2);
            orders.setAmount(new BigDecimal(amount.get()));
            orders.setUserId(currentId);
            orders.setNumber(String.valueOf(orderId));
            orders.setConsignee(addressBook.getConsignee());
            orders.setPhone(addressBook.getPhone());
            orders.setAddress((addressBook.getProvinceName() == null ? "" :addressBook.getProvinceName())
                    + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                    + (addressBook.getDistrictCode() ==null ? "" : addressBook.getDistrictCode())
                    + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

            this.save(orders);

            orderDetailService.saveBatch(orderDetails);

            //清空购物车
            LambdaQueryWrapper<ShoppingCart> queryWrapper2 = new LambdaQueryWrapper<>();
            queryWrapper2.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

            shoppingCartService.remove(queryWrapper2);
        }
    }

    /**
     * 历史订单
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    @Transactional
    public Page<OrdersDto> getWithDetail(int page, int pageSize) {
        //分页构造器
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();

        //查询订单
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,BaseContext.getCurrentId());

        this.page(ordersPage,queryWrapper);

        //拷贝查询的订单数据，到订单传输类
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");

        //获取订单记录
        List<Orders> orders = ordersPage.getRecords();
        //遍历添加订单明细
        List<OrdersDto> ordersDtoList = orders.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            //拷贝数据
            BeanUtils.copyProperties(item,ordersDto);
            //获取订单明细数据
            Long orderId = item.getId();
            LambdaQueryWrapper<OrderDetail> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(OrderDetail::getOrderId, orderId);

            List<OrderDetail> list = orderDetailService.list(queryWrapper1);

            ordersDto.setOrderDetails(list);

            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(ordersDtoList);

        return ordersDtoPage;
    }
}
