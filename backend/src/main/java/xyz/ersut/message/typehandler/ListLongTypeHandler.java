package xyz.ersut.message.typehandler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * List<Long> 类型处理器
 * 用于处理数据库JSON字段与Java List<Long>对象之间的转换
 * 
 * @author ersut
 */
@MappedTypes({List.class})
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.LONGVARCHAR})
public class ListLongTypeHandler extends BaseTypeHandler<List<Long>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Long> parameter, JdbcType jdbcType) throws SQLException {
        // 将List<Long>转换为JSON字符串存储到数据库
        ps.setString(i, JSON.toJSONString(parameter));
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 从数据库读取JSON字符串并转换为List<Long>
        String jsonString = rs.getString(columnName);
        return parseJsonToList(jsonString);
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // 从数据库读取JSON字符串并转换为List<Long>
        String jsonString = rs.getString(columnIndex);
        return parseJsonToList(jsonString);
    }

    @Override
    public List<Long> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // 从数据库读取JSON字符串并转换为List<Long>
        String jsonString = cs.getString(columnIndex);
        return parseJsonToList(jsonString);
    }

    /**
     * 解析JSON字符串为List<Long>
     * 
     * @param jsonString JSON字符串
     * @return List<Long>对象
     */
    private List<Long> parseJsonToList(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            List<Long> result = JSON.parseObject(jsonString, new TypeReference<List<Long>>() {});
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            // 如果解析失败，返回空列表
            System.err.println("Failed to parse JSON to List<Long>: " + jsonString + ", error: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}