package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.dto.DishDto;
import com.itheima.entity.Dish;

public interface DishService extends IService<Dish> {
    //保存菜品及口味信息
    void saveWithFlavor(DishDto dishDto);
    //删除
    void removeWithFlavor(Long[] ids);
   //修改菜品信息回显
   DishDto getWithFlavor(Long id);
    //修改菜品信息
    void updateWithFlavor(DishDto dishDto);
}
