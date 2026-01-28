package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {
    Page<Category> categoryPage(CategoryPageQueryDTO categoryPageQueryDTO);

    void update(Category category);

    void insert(Category category);

    List<Category> selectByType(Integer type);

    @Delete("delete from category where id=#{id}")
    void deleteCategoryById(Long id);
}
