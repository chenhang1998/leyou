package com.leyou.item.service;

import com.leyou.item.pojo.SpecGroup;

import java.util.List;

public interface SpecGroupService {
    List<SpecGroup> queryGroupByCid(Long cid);

    /**
     *根据cid查询group和param
     * */
    List<SpecGroup> queryGroupsWithParam(Long cid);
}
