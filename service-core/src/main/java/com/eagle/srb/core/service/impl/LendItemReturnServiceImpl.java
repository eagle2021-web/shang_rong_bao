package com.eagle.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eagle.srb.core.mapper.LendItemMapper;
import com.eagle.srb.core.mapper.LendItemReturnMapper;
import com.eagle.srb.core.mapper.LendMapper;
import com.eagle.srb.core.mapper.LendReturnMapper;
import com.eagle.srb.core.pojo.entity.LendItemReturn;
import com.eagle.srb.core.service.LendItemReturnService;
import com.eagle.srb.core.service.UserBindService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 标的出借回款记录表 服务实现类
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
@Service
public class LendItemReturnServiceImpl extends ServiceImpl<LendItemReturnMapper, LendItemReturn> implements LendItemReturnService {

    @Resource
    private LendMapper lendMapper;

    @Resource
    private LendReturnMapper lendReturnMapper;

    @Resource
    private LendItemMapper lendItemMapper;

    @Resource
    private UserBindService userBindService;

    @Override
    public List<LendItemReturn> selectByLendId(Long lendId, Long userId) {
        QueryWrapper<LendItemReturn> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("lend_id", lendId)
                .eq("invest_user_id", userId)
                .orderByAsc("current_period");
        return baseMapper.selectList(queryWrapper);
    }
}
