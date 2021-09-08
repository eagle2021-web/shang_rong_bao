package com.eagle.srb.core.mapper;

import com.eagle.srb.core.pojo.dto.ExcelDictDTO;
import com.eagle.srb.core.pojo.entity.Dict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 数据字典 Mapper 接口
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
public interface DictMapper extends BaseMapper<Dict> {

    void insertBatch(List<ExcelDictDTO> list);
}
