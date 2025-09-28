package xyz.ersut.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import xyz.ersut.message.entity.SysDictType;

/**
 * 字典类型服务接口
 * 
 * @author ersut
 */
public interface SysDictTypeService {
    
    /**
     * 分页查询字典类型
     * 
     * @param pageNum 页码
     * @param pageSize 页大小
     * @param dictName 字典名称（模糊查询）
     * @param dictType 字典类型（模糊查询）
     * @return 分页结果
     */
    Page<SysDictType> selectPage(int pageNum, int pageSize, String dictName, String dictType);
    
    /**
     * 根据ID查询字典类型
     * 
     * @param id 字典类型ID
     * @return 字典类型信息
     */
    SysDictType getById(Long id);
    
    /**
     * 根据字典类型查询
     * 
     * @param dictType 字典类型
     * @return 字典类型信息
     */
    SysDictType getByDictType(String dictType);
    
    /**
     * 新增字典类型
     * 
     * @param dictType 字典类型信息
     * @return 操作结果
     */
    boolean save(SysDictType dictType);
    
    /**
     * 修改字典类型
     * 
     * @param dictType 字典类型信息
     * @return 操作结果
     */
    boolean update(SysDictType dictType);
    
    /**
     * 删除字典类型
     * 
     * @param id 字典类型ID
     * @return 操作结果
     */
    boolean deleteById(Long id);
}