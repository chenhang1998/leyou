package com.leyou.search.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private GoodsRepository goodsRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 把spu构建为Goods
     * */
    public Goods buildGoods(Spu spu) throws IOException {

            // 创建goods对象
            Goods goods = new Goods();

            // 查询品牌
            Brand brand = this.brandClient.queryBrandById(spu.getBrandId());

            // 查询分类名称
            List<String> names = this.categoryClient.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

            // 查询spu下的所有sku
            List<Sku> skus = this.goodsClient.querySkusBySpuId(spu.getId());
            List<Long> prices = new ArrayList<>();
            List<Map<String, Object>> skuMapList = new ArrayList<>();
            // 遍历skus，获取价格集合
            skus.forEach(sku ->{
                prices.add(sku.getPrice());
                Map<String, Object> skuMap = new HashMap<>();
                skuMap.put("id", sku.getId());
                skuMap.put("title", sku.getTitle());
                skuMap.put("price", sku.getPrice());
                skuMap.put("image", StringUtils.isNotBlank(sku.getImages()) ? StringUtils.split(sku.getImages(), ",")[0] : "");
                skuMapList.add(skuMap);
            });

            // 查询出所有的搜索规格参数
            List<SpecParam> params = this.specificationClient.queryParams(null, spu.getCid3(), null, true);
            // 查询spuDetail。获取规格参数值
            SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spu.getId());
            // 获取通用的规格参数
            Map<Long, Object> genericSpecMap = MAPPER.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<Long, Object>>() {});
            // 获取特殊的规格参数
            Map<Long, List<Object>> specialSpecMap = MAPPER.readValue(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<Object>>>() {});
            // 定义map接收{规格参数名，规格参数值}
            Map<String, Object> paramMap = new HashMap<>();
            params.forEach(param -> {
                // 判断是否通用规格参数
                if (param.getGeneric()) {
                    // 获取通用规格参数值
                    String value = genericSpecMap.get(param.getId()).toString();
                    // 判断是否是数值类型
                    if (param.getNumeric()){
                        // 如果是数值的话，判断该数值落在那个区间
                        value = chooseSegment(value, param);
                    }
                    // 把参数名和值放入结果集中
                    paramMap.put(param.getName(), value);
                } else {
                    paramMap.put(param.getName(), specialSpecMap.get(param.getId()));
                }
            });
            // 设置参数
            goods.setId(spu.getId());
            goods.setCid1(spu.getCid1());
            goods.setCid2(spu.getCid2());
            goods.setCid3(spu.getCid3());
            goods.setBrandId(spu.getBrandId());
            goods.setCreateTime(spu.getCreateTime());
            goods.setSubTitle(spu.getSubTitle());
            goods.setAll(spu.getTitle() + brand.getName() + StringUtils.join(names, " "));
            goods.setPrice(prices);
            goods.setSkus(MAPPER.writeValueAsString(skuMapList));
            goods.setSpecs(paramMap);
            return goods;
        }

    /**
     * 根据查询条件查询Goods分页结果集
     * */
    @Override
    public SearchResult search(SearchRequest request) {
        if (StringUtils.isBlank(request.getKey())){
            return null;
        }
        //自定义查询构建器
        NativeSearchQueryBuilder queryBuilder=new NativeSearchQueryBuilder();

        //添加查询条件
        BoolQueryBuilder baseQuery=filterquery(request);
        queryBuilder.withQuery(baseQuery);
        //添加分页,分页页码从零开始
        queryBuilder.withPageable(PageRequest.of(request.getPage()-1,request.getSize()));
        //添加结果集过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        //添加分类和品牌的聚合
        String categoryAggName="categories";
        String brandAggName ="brands";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        //执行查询
        AggregatedPage<Goods> goodsPage =(AggregatedPage<Goods>)this.goodsRepository.search(queryBuilder.build());

        //获取分类和品牌的聚合结果集并解析
        List<Map<String,Object>> categories=getCategoryAggResult(goodsPage.getAggregation(categoryAggName));
        List<Brand> brands=getBrandAggResult(goodsPage.getAggregation(brandAggName));

        //获取参数的聚合结果集并解析
        List<Map<String, Object>> specs=new ArrayList<>();
        if (!CollectionUtils.isEmpty(categories) && categories.size()==1) {
            specs= getParamAggResult((Long)categories.get(0).get("id"),baseQuery);
        }

        //将goodsPage放入分页结果集
        SearchResult pageResult = new SearchResult();
        pageResult.setTotal(goodsPage.getTotalElements());
        pageResult.setTotalPage(goodsPage.getTotalPages());
        pageResult.setItems(goodsPage.getContent());
        pageResult.setBrands(brands);
        pageResult.setCategories(categories);
        pageResult.setSpecs(specs);
        //返回分页结果集
        return pageResult;
    }

    /**
     * 增添，修改消息队列的监听
     * */
    @Override
    public void save(Long spuId) throws IOException {
        try {
            Thread.sleep(500);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            Spu spu = this.goodsClient.querySpuBySpuId(spuId);
            Goods goods = this.buildGoods(spu);
            this.goodsRepository.save(goods);
        }
    }

    /**
     * 删除消息队列的监听
     * */
    @Override
    public void delete(Long spuId) throws IOException {
        try {
            Thread.sleep(500);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            Spu spu = this.goodsClient.querySpuBySpuId(spuId);
            Goods goods = this.buildGoods(spu);
            this.goodsRepository.delete(goods);
        }
    }






    /**
     * 过滤查询
     * */
    private BoolQueryBuilder filterquery(SearchRequest request) {
        BoolQueryBuilder boolQueryBuilder=new BoolQueryBuilder();
        //添加基本查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND));
        //遍历filter,并且添加过滤查询
        Map<String, Object> filter = request.getFilter();
        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            String key=entry.getKey();
            //判断key
            if (StringUtils.equals("分类",key)){
                key="cid3";
            }else if (StringUtils.equals("品牌",key)){
                key="brandId";
            }else {
                key="specs."+key+".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }

        return boolQueryBuilder;
    }

    /**
     * 查询并解析规格参数的聚合结果集
     * */
    private List<Map<String, Object>> getParamAggResult(Long id, QueryBuilder baseQuery) {
        //创建返回结果
        List<Map<String,Object>> resultList=new ArrayList<>();
        //自定义查询构建器
        NativeSearchQueryBuilder queryBuilder=new NativeSearchQueryBuilder();
        //添加查询条件
        queryBuilder.withQuery(baseQuery);
        //根据分类id查询用于查询的规格参数
        List<SpecParam> params = this.specificationClient.queryParams(null, id, null, true);
        //添加规格参数的聚合
        params.forEach(param ->{
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs."+param.getName()+".keyword"));
        });
        //添加结果集过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{},null));
        //执行聚合查询,获取聚合结果集
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>)this.goodsRepository.search(queryBuilder.build());
        //遍历解析聚合结果集
        params.forEach(param ->{
            Map map=new HashMap();
            //根据param获取聚合结果集
            Aggregation aggregation = goodsPage.getAggregation(param.getName());
            StringTerms terms=(StringTerms) aggregation;
            //遍历terms
            List<Object> list=new ArrayList<>();
            terms.getBuckets().forEach(bucket ->{
                list.add(bucket.getKey());
            });
            map.put("param",param.getName());
            map.put("options",list);
            resultList.add(map);
        });
        return resultList;
    }

    /**
     * 解析分类的的聚合结果集
     * */
    private List<Map<String, Object>> getCategoryAggResult(Aggregation aggregation) {
        LongTerms terms=(LongTerms)aggregation;

        //遍历桶，并且得到所有的key封装成list
        List<Map<String, Object>> maps = terms.getBuckets().stream().map(bucket -> {
            long id = bucket.getKeyAsNumber().longValue();
            //查询cid对应的name
            List<String> names = this.categoryClient.queryNameByIds(Arrays.asList(id));
            //将id和name放入map中
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("name", names.get(0));
            return map;
        }).collect(Collectors.toList());
        return maps;
    }

    /**
     * 解析品牌的聚合结果集
     * */
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        LongTerms terms=(LongTerms) aggregation;

        //获取聚合中的桶并且转化为brands
        List<Brand> brands = terms.getBuckets().stream().map(bucket -> {
            return this.brandClient.queryBrandById(bucket.getKeyAsNumber().longValue());
        }).collect(Collectors.toList());
        return brands;
    }


    private String chooseSegment(String value, SpecParam p) {
            double val = NumberUtils.toDouble(value);
            String result = "其它";
            // 保存数值段
            for (String segment : p.getSegments().split(",")) {
                String[] segs = segment.split("-");
                // 获取数值范围
                double begin = NumberUtils.toDouble(segs[0]);
                double end = Double.MAX_VALUE;
                if(segs.length == 2){
                    end = NumberUtils.toDouble(segs[1]);
                }
                // 判断是否在范围内
                if(val >= begin && val < end){
                    if(segs.length == 1){
                        result = segs[0] + p.getUnit() + "以上";
                    }else if(begin == 0){
                        result = segs[1] + p.getUnit() + "以下";
                    }else{
                        result = segment + p.getUnit();
                    }
                    break;
                }
            }
            return result;
        }
}
