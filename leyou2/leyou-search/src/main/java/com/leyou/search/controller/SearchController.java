package com.leyou.search.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 根据查询条件查询Goods分页结果集
     * */
    @PostMapping("/page")
    @ResponseBody
    public ResponseEntity<SearchResult> search(@RequestBody SearchRequest request){
        SearchResult result=this.searchService.search(request);
        if (request==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}
