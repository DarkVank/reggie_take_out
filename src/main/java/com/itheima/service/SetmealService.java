package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.dto.DishDto;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Setmeal;

public interface SetmealService extends IService<Setmeal> {
    //保存套餐信息
    void saveWithDish(SetmealDto setmealDto);
    //修改套餐信息回显
    SetmealDto getWithFlavor(Long id);
    //修改套餐信息
    void updateWithFlavor(SetmealDto setmealDto);


    void removeWithDish(Long[] ids);
}
