package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.common.BaseContext;
import com.itheima.common.R;
import com.itheima.entity.ShoppingCart;
import com.itheima.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @DeleteMapping("/clean")
    public R<String> clean(){
        log.info("清空购物车");

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);
        return R.success("清空");
    }
    /**
     * 减少数量
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        log.info("减少订单");

        //获取当前用户
        Long currentId = BaseContext.getCurrentId();

        //获取菜品id
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);

        if( dishId != null) {
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //判断当前菜品or套餐是否为最后一个
        ShoppingCart cart = shoppingCartService.getOne(queryWrapper);
        Integer number = cart.getNumber();

        if(number == 1){
            shoppingCartService.remove(queryWrapper);
            cart.setNumber(0);
        }else{
            cart.setNumber(number-1);
            shoppingCartService.updateById(cart);
        }

        return  R.success(cart);
    }
    /**
     * 添加菜品or套餐
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("添加到购物车{}",shoppingCart.toString());

        //获取当前用户
        Long currentId = BaseContext.getCurrentId();
        //获取菜品id
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);

        //添加到购物车表
        if( dishId != null) {
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //判断当前菜品or套餐是否存在
        ShoppingCart cart = shoppingCartService.getOne(queryWrapper);

        //如果不存在,新增
        if(cart == null){
            shoppingCart.setNumber(1);
            shoppingCart.setUserId(currentId);

            shoppingCartService.save(shoppingCart);

            cart = shoppingCart;
        }else {

            Integer number = cart.getNumber();
            cart.setNumber(number+1);

            shoppingCartService.updateById(cart);
        }
        return R.success(cart);

    }

    /**
     * 获取购物车数据
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("购物车数据");
        List<ShoppingCart> list = shoppingCartService.list();
        return R.success(list);
    }
}
