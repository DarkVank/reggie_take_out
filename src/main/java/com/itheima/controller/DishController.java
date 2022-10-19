package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.dto.DishDto;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.service.CategoryService;
import com.itheima.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 修改菜品信息
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info("修改菜品的数据{}");
        dishService.updateWithFlavor(dishDto);

        return R.success("修改成功");
    }
    /**
     * 修改菜品回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> dishData( @PathVariable Long id){
        log.info("修改的菜品id：{}",id);
        DishDto withFlavor = dishService.getWithFlavor(id);

        return R.success(withFlavor);
    }

    /**
     * 新增套餐回显
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){
        log.info("新增套餐回显菜品的分类id{}",dish.getCategoryId());

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);

        List<Dish> dishList = dishService.list(queryWrapper);
        return R.success(dishList);
    }

    /**
     * 新增菜品信息
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info("保存的菜品{}",dishDto.getName());

        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 菜品分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("菜品分页信息{}",name);
        //分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //获取菜品信息
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name),Dish::getName,name);

        dishService.page(pageInfo,queryWrapper);
        //拷贝分页数据到dishDtoPage
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        //根据分类id获取分类名称
        List<Dish> records = pageInfo.getRecords();

        List<DishDto> dishDtoList = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            //获取分类名称
            Long categoryId = dishDto.getCategoryId();
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();

            dishDto.setCategoryName(categoryName);
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(dishDtoList);

        return R.success(dishDtoPage);
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long[] ids){
        log.info("删除的菜品id{}",ids);

        dishService.removeWithFlavor(ids);

        return R.success("删除成功");
    }

    @PostMapping ("/status/{status}")
    public R<String> status(@PathVariable int status,Long[] ids){
        log.info("更改菜品状态{}",ids);
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Dish::getStatus,status);
        updateWrapper.in(Dish::getId,ids);

        dishService.update(updateWrapper);
        return R.success("状态更改成功");
    }

}
