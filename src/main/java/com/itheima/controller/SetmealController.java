package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.dto.DishDto;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.Setmeal;
import com.itheima.service.CategoryService;
import com.itheima.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 修改套餐状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping ("/status/{status}")
    public R<String> status(@PathVariable int status,Long[] ids){
        log.info("更改套餐状态{}",ids);
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Setmeal::getStatus,status);
        updateWrapper.in(Setmeal::getId,ids);

        setmealService.update(updateWrapper);
        return R.success("状态更改成功");
    }

    /**
     * 删除当前套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long[] ids){
        log.info("删除的套餐id{}",ids);

        setmealService.removeWithDish(ids);

        return R.success("删除成功");
    }
    /**
     * 修改套餐信息
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info("修改菜品的数据{}");
        setmealService.updateWithFlavor(setmealDto);

        return R.success("修改成功");
    }

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("新增套餐:{}",setmealDto.getName());

        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 分页信息
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("菜品分页信息{}",name);
        //分页构造器
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        //查询套餐分页信息
        setmealService.page(pageInfo,queryWrapper);
        //拷贝分页信息
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");


        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> setmealDtoList = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();

            BeanUtils.copyProperties(item, setmealDto);
            //获取分类对象
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            setmealDto.setCategoryName(category.getName());

            return setmealDto;
        }).collect(Collectors.toList());

        //分页数据赋值
        setmealDtoPage.setRecords(setmealDtoList);


        return R.success(setmealDtoPage);
    }

    /**
     * 修改菜品回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> dishData( @PathVariable Long id){
        log.info("修改的菜品id：{}",id);
        SetmealDto withFlavor = setmealService.getWithFlavor(id);

        return R.success(withFlavor);
    }

}
