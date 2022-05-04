package com.example.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie.dto.SetmealDto;
import com.example.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    //新增套餐
    public void saveWithDish(SetmealDto setmealDto);

    //回显套餐数据
    public SetmealDto getWithDish(Long id);

    //更新套餐数据
    public void updateWithDish(SetmealDto setmealDto);

    //删除套餐数据
    public void deleteWithDish(List<Long> ids);
}
