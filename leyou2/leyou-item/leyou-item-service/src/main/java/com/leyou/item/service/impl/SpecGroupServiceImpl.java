package com.leyou.item.service.impl;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecGroupService;
import com.leyou.item.service.SpecParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecGroupServiceImpl implements SpecGroupService {
    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpecParamService specParamService;

    /**
     * 根据分类id查询参数组
     *
     * */
    @Override
    public List<SpecGroup> queryGroupByCid(Long cid) {
        SpecGroup group=new SpecGroup();
        group.setCid(cid);
        List<SpecGroup> groups = specGroupMapper.select(group);
        return groups;
    }

    /**
     *根据cid查询group和param
     * */
    @Override
    public List<SpecGroup> queryGroupsWithParam(Long cid) {
        List<SpecGroup> groups = this.queryGroupByCid(cid);
        groups.forEach(specGroup -> {
            List<SpecParam> params = specParamService.queryParams(null,specGroup.getId(),null,null);
            specGroup.setParams(params);
        });
        return groups;
    }
}
