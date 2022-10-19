package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomException;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.Setmeal;
import com.itheima.mapper.CategoryMapper;
import com.itheima.service.CategoryService;
import com.itheima.service.DishService;
import com.itheima.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService{

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;
    /**
     * 判断分类是否关联菜品
     * @param ids
     */
    @Override
    public void delete(Long ids) {
        //查询菜品，根据分类id
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId,ids);
        int count = dishService.count(queryWrapper);
        //判断当前分类是否有菜品
        if(count!=0){
            //有，抛出业务异常
             throw new CustomException("当前分类含有菜品");

        }

        LambdaQueryWrapper<Setmeal> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Setmeal::getCategoryId,ids);
        int count1 = setmealService.count();
        //判断当前分类是否有套餐
        if(count!=0){
            //有，抛出业务异常
            throw new CustomException("当前分类含有套餐");
        }
        //正常移除
        this.removeById(ids);
    }
}
