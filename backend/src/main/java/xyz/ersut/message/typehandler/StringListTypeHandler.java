package xyz.ersut.message.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * String List 类型处理器，用于 ClickHouse 数组类型转换
 * 
 * @author ersut
 */
public class StringListTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null || parameter.isEmpty()) {
            ps.setObject(i, new String[0]);
        } else {
            ps.setObject(i, parameter.toArray(new String[0]));
        }
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object[] array = (Object[]) rs.getArray(columnName).getArray();
        if (array == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(Arrays.copyOf(array, array.length, String[].class));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object[] array = (Object[]) rs.getArray(columnIndex).getArray();
        if (array == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(Arrays.copyOf(array, array.length, String[].class));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object[] array = (Object[]) cs.getArray(columnIndex).getArray();
        if (array == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(Arrays.copyOf(array, array.length, String[].class));
    }
}