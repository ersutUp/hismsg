package xyz.ersut.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.baomidou.dynamic.datasource.annotation.DS;
import cn.hutool.core.util.StrUtil;
import xyz.ersut.message.entity.SysDictType;
import xyz.ersut.message.mapper.SysDictTypeMapper;
import xyz.ersut.message.service.SysDictTypeService;

/**
 * 字典类型服务实现类
 * 
 * @author ersut
 */
@Slf4j
@Service
@DS("mysql")
@RequiredArgsConstructor
public class SysDictTypeServiceImpl implements SysDictTypeService {
    
    private final SysDictTypeMapper dictTypeMapper;
    
    @Override
    public Page<SysDictType> selectPage(int pageNum, int pageSize, String dictName, String dictType) {
        // 构建分页对象
        Page<SysDictType> page = new Page<>(pageNum, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<SysDictType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(dictName), SysDictType::getDictName, dictName)
                   .like(StrUtil.isNotBlank(dictType), SysDictType::getDictType, dictType)
                   .orderByDesc(SysDictType::getCreateTime);
        
        return dictTypeMapper.selectPage(page, queryWrapper);
    }
    
    @Override
    public SysDictType getById(Long id) {
        if (id == null) {
            return null;
        }
        return dictTypeMapper.selectById(id);
    }
    
    @Override
    public SysDictType getByDictType(String dictType) {
        if (StrUtil.isBlank(dictType)) {
            return null;
        }
        return dictTypeMapper.selectByDictType(dictType);
    }
    
    @Override
    public boolean save(SysDictType dictType) {
        if (dictType == null) {
            return false;
        }
        
        // 检查字典类型是否已存在
        if (getByDictType(dictType.getDictType()) != null) {
            throw new RuntimeException("字典类型已存在");
        }
        
        // 设置默认值
        if (dictType.getStatus() == null) {
            dictType.setStatus(1);
        }
        
        return dictTypeMapper.insert(dictType) > 0;
    }
    
    @Override
    public boolean update(SysDictType dictType) {
        if (dictType == null || dictType.getId() == null) {
            return false;
        }
        
        // 检查字典类型是否重复（排除当前记录）
        SysDictType existDict = getByDictType(dictType.getDictType());
        if (existDict != null && !existDict.getId().equals(dictType.getId())) {
            throw new RuntimeException("字典类型已存在");
        }
        
        return dictTypeMapper.updateById(dictType) > 0;
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        
        // 这里使用逻辑删除，由MyBatis Plus自动处理
        return dictTypeMapper.deleteById(id) > 0;
    }
}