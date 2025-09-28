package xyz.ersut.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import xyz.ersut.message.entity.SysDictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 字典类型Mapper接口
 * 
 * @author ersut
 */
@Mapper
public interface SysDictTypeMapper extends BaseMapper<SysDictType> {
    
    /**
     * 根据字典类型查询
     * 
     * @param dictType 字典类型
     * @return 字典类型信息
     */
    @Select("SELECT * FROM sys_dict_type WHERE dict_type = #{dictType} AND deleted = 0")
    SysDictType selectByDictType(String dictType);
}