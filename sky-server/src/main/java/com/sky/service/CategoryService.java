package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {
    PageResult categoryPage(CategoryPageQueryDTO categoryPageQueryDTO);

    void startOrStopCategory(Integer status, Long id);

    void updateCategory(CategoryDTO categoryDTO);

    void addCategory(CategoryDTO categoryDTO);

    List<Category> selectByType(Integer type);

    void deleteCategoryById(Long id);
}
