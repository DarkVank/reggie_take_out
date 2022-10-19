package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomException;
import com.itheima.dto.DishDto;
import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import com.itheima.mapper.DishMapper;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 保存菜品口味信息
     * @param dishDto
     */
    @Override
    public void saveWithFlavor(DishDto dishDto) {

        //保存菜品信息
        this.save(dishDto);
        Long dishId = dishDto.getId();
        //设置口味关联的菜品id
        List<DishFlavor> flavors = dishDto.getFlavors();
         flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //保存口味信息
         dishFlavorService.saveBatch(flavors);

    }

    @Override
    public void removeWithFlavor(Long[] ids) {

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.in(Dish::getId,ids);

        //判断菜品是否售卖中
        int count = this.count(queryWrapper);
        if(count > 0){
            throw new CustomException("菜品正在售卖中……");
        }

        LambdaQueryWrapper<Dish> queryWrapper0 = new LambdaQueryWrapper<>();
        queryWrapper0.in(Dish::getId,ids);

        this.remove(queryWrapper0);
        //删除口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(queryWrapper1);
    }

    @Override
    public DishDto getWithFlavor(Long id) {
        //获取菜品信息
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();

        //拷贝数据
        BeanUtils.copyProperties(dish,dishDto);

        //获取口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);

        List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);


        dishDto.setFlavors(dishFlavors);

        return dishDto;
    }

    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //修改菜品信息
        this.updateById(dishDto);
        Long dishId = dishDto.getId();
        //删除口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(DishFlavor::getDishId,dishId);
        dishFlavorService.remove(queryWrapper1);

        //设置口味关联的菜品id

        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //保存口味信息
        dishFlavorService.saveBatch(flavors);

    }
}
