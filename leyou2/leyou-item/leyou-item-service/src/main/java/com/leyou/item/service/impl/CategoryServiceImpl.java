package com.leyou.item.service.impl;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

//根据父节点查询子节点
    @Override
    public List<Category> queryCategoriesByPid(Long pid) {
        Category record=new Category();
        record.setParentId(pid);
        return this.categoryMapper.select(record);
    }

    /**
     * 查询多个分类名称
     * */
    public List<String> queryNameByIds(List<Long> ids){
        List<Category> categories = categoryMapper.selectByIdList(ids);
        List<String> collect = categories.stream().map(category -> {
            return category.getName();
        }).collect(Collectors.toList());
        return collect;
    }
}
