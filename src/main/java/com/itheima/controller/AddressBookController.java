package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.BaseContext;
import com.itheima.common.R;
import com.itheima.entity.AddressBook;
import com.itheima.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 保存地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody AddressBook addressBook){
        log.info("保存地址的用户名称{}",addressBook.getConsignee());
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);

        addressBookService.save(addressBook);
        return R.success("保存地址成功");
    }

    /**
     * 展示地址
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(){
        log.info("展示地址信息");

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(AddressBook::getCreateTime);

        return R.success(addressBookService.list(queryWrapper));
    }

    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */

    @PutMapping("/default")
    public R<String> setDefault(@RequestBody AddressBook addressBook){
        log.info("地址id");

        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        updateWrapper.set(AddressBook::getIsDefault,0);

        addressBookService.update(updateWrapper);

        //设置默认地址
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);

        return R.success("设置成功");

    }

    @GetMapping("/default")
    public R<AddressBook> getDefault(){
        log.info("获取地址");

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getIsDefault,1);
        queryWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());

        AddressBook addressBook = addressBookService.getOne(queryWrapper);

        return R.success(addressBook);
    }
 }
