package com.leyou.item.api;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/spec")
public interface SpecificationsApi {

    /**
     * 根据多条件查询规格参数
     * */
    @GetMapping("/params")
    @ResponseBody
    public List<SpecParam> queryParams(
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "generic",required = false)Boolean generic,
            @RequestParam(value = "searching",required = false)Boolean searching
    );

    /**
     *根据cid查询group和param
     * */
    @GetMapping("/group/param/{cid}")
    @ResponseBody
    public List<SpecGroup> queryGroupsWithParam(@PathVariable("cid")Long cid);
}
