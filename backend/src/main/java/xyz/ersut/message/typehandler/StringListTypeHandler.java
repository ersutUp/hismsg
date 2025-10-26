package xyz.ersut.message.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ClickHouse 数组类型处理器
 * 用于将 Java List<String> 与 ClickHouse 的 Array(String) 类型进行转换
 *
 * @author ersut
 */
@MappedTypes(List.class)
public class StringListTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        // ClickHouse 使用数组对象
        if (parameter == null || parameter.isEmpty()) {
            ps.setObject(i, new String[0]);
        } else {
            ps.setObject(i, parameter.toArray(new String[0]));
        }
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getArrayAsList(rs.getObject(columnName));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getArrayAsList(rs.getObject(columnIndex));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getArrayAsList(cs.getObject(columnIndex));
    }

    /**
     * 将数组对象转换为 List
     *
     * @param obj 数组对象
     * @return List<String>
     */
    private List<String> getArrayAsList(Object obj) throws SQLException {
        if (obj == null) {
            return Collections.emptyList();
        }

        // 处理 SQL Array 类型
        if (obj instanceof Array) {
            Array array = (Array) obj;
            Object[] elements = (Object[]) array.getArray();
            if (elements == null || elements.length == 0) {
                return Collections.emptyList();
            }
            List<String> result = new ArrayList<>(elements.length);
            for (Object element : elements) {
                result.add(element != null ? element.toString() : "");
            }
            return result;
        }

        // 处理普通数组类型
        if (obj instanceof Object[]) {
            Object[] array = (Object[]) obj;
            if (array.length == 0) {
                return Collections.emptyList();
            }
            List<String> result = new ArrayList<>(array.length);
            for (Object element : array) {
                result.add(element != null ? element.toString() : "");
            }
            return result;
        }

        // 处理字符串数组
        if (obj instanceof String[]) {
            String[] array = (String[]) obj;
            return array.length == 0 ? Collections.emptyList() : Arrays.asList(array);
        }

        return Collections.emptyList();
    }
}