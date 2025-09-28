package xyz.ersut.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.baomidou.dynamic.datasource.annotation.DS;
import cn.hutool.core.util.StrUtil;
import xyz.ersut.message.entity.SysDictData;
import xyz.ersut.message.mapper.SysDictDataMapper;
import xyz.ersut.message.service.SysDictDataService;

import java.util.List;

/**
 * 字典数据服务实现类
 * 
 * @author ersut
 */
@Slf4j
@Service
@DS("mysql")
@RequiredArgsConstructor
public class SysDictDataServiceImpl implements SysDictDataService {
    
    private final SysDictDataMapper dictDataMapper;
    
    @Override
    public Page<SysDictData> selectPage(int pageNum, int pageSize, String dictType, String dictLabel) {
        // 构建分页对象
        Page<SysDictData> page = new Page<>(pageNum, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<SysDictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotBlank(dictType), SysDictData::getDictType, dictType)
                   .like(StrUtil.isNotBlank(dictLabel), SysDictData::getDictLabel, dictLabel)
                   .orderByAsc(SysDictData::getDictSort)
                   .orderByDesc(SysDictData::getCreateTime);
        
        return dictDataMapper.selectPage(page, queryWrapper);
    }
    
    @Override
    public List<SysDictData> getByDictType(String dictType) {
        if (StrUtil.isBlank(dictType)) {
            return null;
        }
        return dictDataMapper.selectByDictType(dictType);
    }
    
    @Override
    public SysDictData getById(Long id) {
        if (id == null) {
            return null;
        }
        return dictDataMapper.selectById(id);
    }
    
    @Override
    public SysDictData getByDictTypeAndValue(String dictType, String dictValue) {
        if (StrUtil.isBlank(dictType) || StrUtil.isBlank(dictValue)) {
            return null;
        }
        return dictDataMapper.selectByDictTypeAndValue(dictType, dictValue);
    }
    
    @Override
    public boolean save(SysDictData dictData) {
        if (dictData == null) {
            return false;
        }
        
        // 检查字典值是否已存在
        if (getByDictTypeAndValue(dictData.getDictType(), dictData.getDictValue()) != null) {
            throw new RuntimeException("字典值已存在");
        }
        
        // 设置默认值
        if (dictData.getStatus() == null) {
            dictData.setStatus(1);
        }
        if (dictData.getDictSort() == null) {
            dictData.setDictSort(0);
        }
        if (dictData.getIsDefault() == null) {
            dictData.setIsDefault(0);
        }
        
        return dictDataMapper.insert(dictData) > 0;
    }
    
    @Override
    public boolean update(SysDictData dictData) {
        if (dictData == null || dictData.getId() == null) {
            return false;
        }
        
        // 检查字典值是否重复（排除当前记录）
        SysDictData existDict = getByDictTypeAndValue(dictData.getDictType(), dictData.getDictValue());
        if (existDict != null && !existDict.getId().equals(dictData.getId())) {
            throw new RuntimeException("字典值已存在");
        }
        
        return dictDataMapper.updateById(dictData) > 0;
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        
        // 使用逻辑删除
        return dictDataMapper.deleteById(id) > 0;
    }
    
    @Override
    public String getDictValue(String dictType, String dictLabel) {
        if (StrUtil.isBlank(dictType) || StrUtil.isBlank(dictLabel)) {
            return null;
        }
        
        LambdaQueryWrapper<SysDictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDictData::getDictType, dictType)
                   .eq(SysDictData::getDictLabel, dictLabel)
                   .eq(SysDictData::getStatus, 1);
        
        SysDictData dictData = dictDataMapper.selectOne(queryWrapper);
        return dictData != null ? dictData.getDictValue() : null;
    }
    
    @Override
    public String getDictLabel(String dictType, String dictValue) {
        if (StrUtil.isBlank(dictType) || StrUtil.isBlank(dictValue)) {
            return null;
        }
        
        SysDictData dictData = getByDictTypeAndValue(dictType, dictValue);
        return dictData != null && dictData.getStatus() == 1 ? dictData.getDictLabel() : null;
    }
}