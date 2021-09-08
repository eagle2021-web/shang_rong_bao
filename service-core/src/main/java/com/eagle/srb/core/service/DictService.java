package com.eagle.srb.core.service;

import com.eagle.srb.core.pojo.dto.ExcelDictDTO;
import com.eagle.srb.core.pojo.entity.Dict;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.InputStream;
import java.util.List;

/**
 * <p>
 * 数据字典 服务类
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
public interface DictService extends IService<Dict> {
    void importData(InputStream inputStream);
    List<ExcelDictDTO> listDictData();

    List<Dict> listByParentId(Long parentId);
}
