package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;//依赖注入接口

    /**
     * 菜品及套餐分类分页查询
     * @param categoryPageQueryDTO 对多个Query参数进行自动装箱
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> categoryPage(CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("菜品及套餐分类分页查询:{}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.categoryPage(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 分类启用禁用
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    public Result startOrStopCategory(@PathVariable Integer status,Long id) {
        log.info("分类启用禁用状态:{},分类id:{}",status,id);
        categoryService.startOrStopCategory(status,id);
        return Result.success();
    }


    /**
     * 修改分类
     * @param categoryDTO
     * @return
     */
    @PutMapping
    public Result updateCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("修改分类:{}",categoryDTO);
        categoryService.updateCategory(categoryDTO);
        return Result.success();
    }

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    public Result addCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增分类:{}",categoryDTO);
        categoryService.addCategory(categoryDTO);
        return Result.success();
    }

    /**
     * 根据类型查询
     * @param type
     * @return
     */
    @GetMapping("/list")
    public Result<List<Category>> selectByType(Integer type) {
        log.info("根据类型查询:{}",type);
        List<Category> list=categoryService.selectByType(type);
        return Result.success(list);
    }

    /**
     * 根据id删除分类
     * 如果该分类中已经关联了菜品或套餐则无法删除
     * @param id
     * @return
     */
    @DeleteMapping
    public Result deleteCategoryById(Long id) {
        log.info("根据分类id删除分类:{}",id);
        categoryService.deleteCategoryById(id);
        return Result.success();
    }
}
