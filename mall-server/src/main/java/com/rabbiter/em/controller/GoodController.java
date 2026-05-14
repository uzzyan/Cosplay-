package com.rabbiter.em.controller;

import com.rabbiter.em.annotation.Authority;
import com.rabbiter.em.constants.Constants;
import com.rabbiter.em.common.Result;
import com.rabbiter.em.entity.AuthorityType;
import com.rabbiter.em.entity.Good;
import com.rabbiter.em.entity.Standard;
import com.rabbiter.em.service.GoodService;
import com.rabbiter.em.service.StandardService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 商品控制器
 * @author uzzyan
 */

@RestController
@RequestMapping("/api/good")
@Authority(AuthorityType.noRequire)  // 商品浏览不需要登录
public class GoodController {
    @Resource
    private GoodService goodService;

    @Resource
    private StandardService standardService;

    @Resource
    private com.rabbiter.em.service.RecommendationService recommendationService;

    /**
     * 获取推荐商品接口 (Get Recommendations)
     * @param userId 用户ID
     * @return Result 推荐商品列表
     */
    @Authority(AuthorityType.noRequire)
    @GetMapping("/recommend/{userId}")
    public Result recommend(@PathVariable Long userId) {
        return Result.success(recommendationService.recommend(userId));
    }

    /**
     * 新增商品接口 (Add Good)
     * 需要管理员权限
     * @param good 商品实体
     * @return Result 保存成功的商品ID
     */
    @Authority(AuthorityType.requireAuthority)  // 需要管理员权限
    @PostMapping
    public Result save(@RequestBody Good good) {
        return Result.success(goodService.saveOrUpdateGood(good));
    }

    /**
     * 更新商品接口 (Update Good)
     * 需要管理员权限
     * @param good 商品实体
     * @return Result 操作结果
     */
    @Authority(AuthorityType.requireAuthority)  // 需要管理员权限
    @PutMapping
    public Result update(@RequestBody Good good) {
        goodService.update(good);
        return Result.success();
    }

    /**
     * 删除商品接口 (Delete Good)
     * 需要管理员权限
     * @param id 商品ID
     * @return Result 操作结果
     */
    @Authority(AuthorityType.requireAuthority)
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        goodService.deleteGood(id);
        return Result.success();
    }

    /**
     * 根据ID获取商品详情接口 (Get Good By ID)
     * @param id 商品ID
     * @return Result 商品详情
     */
    @Authority(AuthorityType.noRequire)
    @GetMapping("/detail/{id}")
    public Result findById(@PathVariable Long id) {
        return Result.success(goodService.getGoodById(id));
    }

    /**
     * 获取商品规格信息接口 (Get Good Standards)
     * @param id 商品ID
     * @return Result 规格列表
     */
    @Authority(AuthorityType.noRequire)
    @GetMapping("/standard/{id}")
    public Result getStandard(@PathVariable int id) {
        return Result.success(goodService.getStandard(id));
    }

    /**
     * 查询所有前台展示商品接口 (Find All Front Goods)
     * @return Result 推荐商品列表
     */
    @Authority(AuthorityType.noRequire)
    @GetMapping
    public Result findAll() {
        return Result.success(goodService.findFrontGoods());
    }

    /**
     * 查询商品销量排行接口 (Get Sales Rank)
     * @param num 排行榜数量
     * @return Result 销量排行列表
     */
    @Authority(AuthorityType.noRequire)
    @GetMapping("/rank")
    public Result getSaleRank(@RequestParam int num){
        return Result.success(goodService.getSaleRank(num));
    }

    /**
     * 保存/更新商品规格接口 (Save Standards)
     * @param standards 规格列表
     * @param goodId 商品ID
     * @return Result 操作结果
     */
    @Authority(AuthorityType.requireAuthority)  // 需要管理员权限
    @PostMapping("/standard")
    public Result saveStandard(@RequestBody List<Standard> standards, @RequestParam int goodId) {
        // 先删除全部旧记录
        standardService.deleteAll(goodId);
        // 然后插入新记录
        for (Standard standard : standards) {
            standard.setGoodId(goodId);
            if(!standardService.save(standard)){
                return Result.error(Constants.CODE_500,"保存失败");
            }
        }
        return Result.success();
    }

    /**
     * 删除商品规格接口 (Delete Standard)
     * 需要管理员权限
     * @param standard 规格对象
     * @return Result 操作结果
     */
    @Authority(AuthorityType.requireAuthority)
    @DeleteMapping("/standard")
    public Result delStandard(@RequestBody Standard standard) {
        boolean delete = standardService.delete(standard);
        if(delete) {
            return Result.success();
        }else {
            return Result.error(Constants.CODE_500,"删除失败");
        }
    }

    /**
     * 修改商品推荐状态接口 (Set Recommend Status)
     * 需要管理员权限
     * @param id 商品ID
     * @param isRecommend 是否推荐
     * @return Result 操作结果
     */
    @Authority(AuthorityType.requireAuthority)
    @GetMapping("/recommend")
    public Result setRecommend(@RequestParam Long id,@RequestParam Boolean isRecommend){
        return Result.success(goodService.setRecommend(id,isRecommend));
    }

    /**
     * 分页查询商品接口 (Page Query Goods)
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param searchText 搜索内容
     * @param categoryId 分类ID
     * @return Result 分页结果
     */
    @Authority(AuthorityType.noRequire)
    @GetMapping("/page")
    public Result findPage(
                            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                            @RequestParam(required = false, defaultValue = "") String searchText,
                            @RequestParam(required = false) Integer categoryId) {

        return Result.success(goodService.findPage(pageNum,pageSize,searchText,categoryId));
    }

    /**
     * 分页查询完整商品信息接口 (Full Page Query)
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param searchText 搜索内容
     * @param categoryId 分类ID
     * @return Result 分页结果
     */
    @Authority(AuthorityType.noRequire)
    @GetMapping("/fullPage")
    public Result findFullPage(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "") String searchText,
            @RequestParam(required = false) Integer categoryId) {

        return Result.success(goodService.findFullPage(pageNum,pageSize,searchText,categoryId));
    }
}
