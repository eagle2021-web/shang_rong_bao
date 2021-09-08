package com.eagle.srb.core.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.eagle.srb.core.mapper.DictMapper;
import com.eagle.srb.core.pojo.dto.ExcelDictDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ExcelEictDTOlistener extends AnalysisEventListener<ExcelDictDTO> {
    /**
     * 因为ExcelEictDTOlistener 没有被spring管理，所以不能通过Resource注入
     */
    private DictMapper dictMapper;
    List<ExcelDictDTO> list = new ArrayList();
    private static final int BATCH_COUNT = 5;

    public ExcelEictDTOlistener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    public ExcelEictDTOlistener() {
    }

    @Override
    public void invoke(ExcelDictDTO data, AnalysisContext analysisContext) {
        log.info("一条记录 = {}", data);
        //将数据存入数据列表
        list.add(data);
        //调用mapper层的save方法
        if(list.size() >= BATCH_COUNT){
            saveData();
            list.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        saveData();
        log.info("完成");
    }
    private void saveData(){
        log.info("{} 条数据被存储到数据库", list.size());
        dictMapper.insertBatch(list);
        log.info("{} 条数据被成功存储到数据库", list.size());
    }
}
