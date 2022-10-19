package com.itheima.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomException;
import com.itheima.dto.DishDto;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Setmeal;
import com.itheima.entity.SetmealDish;
import com.itheima.mapper.SetmealMapper;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    public void saveWithDish(SetmealDto setmealDto) {

        //保存套餐信息
        this.save(setmealDto);

        //保存套餐菜品信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);

    }

    @Override
    public SetmealDto getWithFlavor(Long id) {
        //获取套餐信息
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();

        //拷贝数据
        BeanUtils.copyProperties(setmeal,setmealDto);
        //获取套餐菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper= new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);

        //合并信息并返回
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(setmealDishes);

        return setmealDto;
    }

    @Override
    public void updateWithFlavor(SetmealDto setmealDto) {
        //修改套餐信息
        this.updateById(setmealDto);

        //删除先前套餐菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());

        setmealDishService.remove(queryWrapper);

        //添加新的信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public void removeWithDish(Long[] ids) {

        LambdaQueryWrapper<Setmeal> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Setmeal::getStatus,1);
        queryWrapper1.in(Setmeal::getId,ids);

        //判断当前套餐是否售卖
        int count = this.count(queryWrapper1);
        if(count>0){
            throw new CustomException("当前套餐正在售卖……");
        }

        //删除套餐信息
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);

        this.remove(queryWrapper);

        //删除套餐菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(queryWrapper2);
    }
}
