package com.leyou.item.service;

import com.leyou.item.pojo.Category;

import java.util.List;

public interface CategoryService {

    List<Category> queryCategoriesByPid(Long pid);

    List<String> queryNameByIds(List<Long> ids);
}
