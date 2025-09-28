package xyz.ersut.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import xyz.ersut.message.entity.SysDictData;

import java.util.List;

/**
 * 字典数据服务接口
 * 
 * @author ersut
 */
public interface SysDictDataService {
    
    /**
     * 分页查询字典数据
     * 
     * @param pageNum 页码
     * @param pageSize 页大小
     * @param dictType 字典类型
     * @param dictLabel 字典标签（模糊查询）
     * @return 分页结果
     */
    Page<SysDictData> selectPage(int pageNum, int pageSize, String dictType, String dictLabel);
    
    /**
     * 根据字典类型查询字典数据
     * 
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    List<SysDictData> getByDictType(String dictType);
    
    /**
     * 根据ID查询字典数据
     * 
     * @param id 字典数据ID
     * @return 字典数据信息
     */
    SysDictData getById(Long id);
    
    /**
     * 根据字典类型和字典值查询
     * 
     * @param dictType 字典类型
     * @param dictValue 字典值
     * @return 字典数据
     */
    SysDictData getByDictTypeAndValue(String dictType, String dictValue);
    
    /**
     * 新增字典数据
     * 
     * @param dictData 字典数据信息
     * @return 操作结果
     */
    boolean save(SysDictData dictData);
    
    /**
     * 修改字典数据
     * 
     * @param dictData 字典数据信息
     * @return 操作结果
     */
    boolean update(SysDictData dictData);
    
    /**
     * 删除字典数据
     * 
     * @param id 字典数据ID
     * @return 操作结果
     */
    boolean deleteById(Long id);
    
    /**
     * 根据字典类型和标签获取字典值
     * 
     * @param dictType 字典类型
     * @param dictLabel 字典标签
     * @return 字典值
     */
    String getDictValue(String dictType, String dictLabel);
    
    /**
     * 根据字典类型和值获取字典标签
     * 
     * @param dictType 字典类型
     * @param dictValue 字典值
     * @return 字典标签
     */
    String getDictLabel(String dictType, String dictValue);
}