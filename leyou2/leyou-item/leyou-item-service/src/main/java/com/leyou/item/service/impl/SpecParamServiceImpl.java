package com.leyou.item.service.impl;

import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecParamServiceImpl implements SpecParamService {
    @Autowired
    private SpecParamMapper specParamMapper;

    /**
     * 根据参数组id查询规格参数
     *
     * */
    @Override
    public List<SpecParam> queryParams(Long cid, Long gid, Boolean generic, Boolean searching) {
        SpecParam param=new SpecParam();
        param.setGroupId(gid);
        param.setCid(cid);
        param.setGeneric(generic);
        param.setSearching(searching);
        List<SpecParam> params = specParamMapper.select(param);
        return params;
    }
}
