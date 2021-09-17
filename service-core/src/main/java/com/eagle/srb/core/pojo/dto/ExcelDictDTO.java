package com.eagle.srb.core.pojo.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 数据字典
 * </p>
 *
 * @author eagle
 * @since 2021-09-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "Dict对象", description = "数据字典")
public class ExcelDictDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ExcelProperty("上级id")
    private Long parentId;

    @ExcelProperty("名称")
    private String name;

    @ExcelProperty("值")
    private Integer value;

    @ExcelProperty("编码")
    private String dictCode;

}
