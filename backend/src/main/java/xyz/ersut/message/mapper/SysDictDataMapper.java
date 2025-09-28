package xyz.ersut.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import xyz.ersut.message.entity.SysDictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 字典数据Mapper接口
 * 
 * @author ersut
 */
@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {
    
    /**
     * 根据字典类型查询字典数据
     * 
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    @Select("SELECT * FROM sys_dict_data WHERE dict_type = #{dictType} AND deleted = 0 AND status = 1 ORDER BY dict_sort ASC")
    List<SysDictData> selectByDictType(String dictType);
    
    /**
     * 根据字典类型和字典值查询
     * 
     * @param dictType 字典类型
     * @param dictValue 字典值
     * @return 字典数据
     */
    @Select("SELECT * FROM sys_dict_data WHERE dict_type = #{dictType} AND dict_value = #{dictValue} AND deleted = 0")
    SysDictData selectByDictTypeAndValue(String dictType, String dictValue);
}