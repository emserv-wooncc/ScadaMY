package com.serotonin.db.spring;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import com.serotonin.db.spring.GenericRowMapper;

public class ExtendedJdbcTemplate extends JdbcTemplate {

    public ExtendedJdbcTemplate() {
        super();
    }

    public ExtendedJdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }

    public ExtendedJdbcTemplate(DataSource dataSource, boolean lazyInit) {
        super(dataSource, lazyInit);
    }

    public int queryForInt(String sql) throws DataAccessException {
        Integer result = queryForObject(sql, Integer.class);
        return (result != null ? result.intValue() : 0);
    }

    public int queryForInt(String sql, Object[] args) throws DataAccessException {
        Integer result = queryForObject(sql, args, Integer.class);
        return (result != null ? result.intValue() : 0);
    }

    public int queryForInt(String sql, Object[] args, int[] argTypes) throws DataAccessException {
        Integer result = queryForObject(sql, args, argTypes, Integer.class);
        return (result != null ? result.intValue() : 0);
    }

    public int queryForInt(String sql, int defaultVal) {
        try {
            Integer result = queryForObject(sql, Integer.class);
            return (result != null ? result.intValue() : defaultVal);
        } catch (DataAccessException e) {
            return defaultVal;
        }
    }

    public int queryForInt(String sql, Object[] args, int defaultVal) {
        try {
            Integer result = queryForObject(sql, args, Integer.class);
            return (result != null ? result.intValue() : defaultVal);
        } catch (DataAccessException e) {
            return defaultVal;
        }
    }

    public int queryForInt(String sql, Object[] args, int[] argTypes, int defaultVal) {
        try {
            Integer result = queryForObject(sql, args, argTypes, Integer.class);
            return (result != null ? result.intValue() : defaultVal);
        } catch (DataAccessException e) {
            return defaultVal;
        }
    }

    public long queryForLong(String sql) throws DataAccessException {
        Long result = queryForObject(sql, Long.class);
        return (result != null ? result.longValue() : 0L);
    }

    public long queryForLong(String sql, Object[] args) throws DataAccessException {
        Long result = queryForObject(sql, args, Long.class);
        return (result != null ? result.longValue() : 0L);
    }

    public long queryForLong(String sql, Object[] args, int[] argTypes) throws DataAccessException {
        Long result = queryForObject(sql, args, argTypes, Long.class);
        return (result != null ? result.longValue() : 0L);
    }

    public long queryForLong(String sql, long defaultVal) {
        try {
            Long result = queryForObject(sql, Long.class);
            return (result != null ? result.longValue() : defaultVal);
        } catch (DataAccessException e) {
            return defaultVal;
        }
    }

    public long queryForLong(String sql, Object[] args, long defaultVal) {
        try {
            Long result = queryForObject(sql, args, Long.class);
            return (result != null ? result.longValue() : defaultVal);
        } catch (DataAccessException e) {
            return defaultVal;
        }
    }

    public long queryForLong(String sql, Object[] args, int[] argTypes, long defaultVal) {
        try {
            Long result = queryForObject(sql, args, argTypes, Long.class);
            return (result != null ? result.longValue() : defaultVal);
        } catch (DataAccessException e) {
            return defaultVal;
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rm, T defaultVal) {
        try {
            return queryForObject(sql, rm);
        } catch (DataAccessException e) {
            return defaultVal;
        }
    }

    public <T> T queryForObject(String sql, Object[] args, RowMapper<T> rm, T defaultVal) {
        try {
            return queryForObject(sql, args, rm);
        } catch (DataAccessException e) {
            return defaultVal;
        }
    }

    public <T> T queryForObject(String sql, Object[] args, int[] argTypes, RowMapper<T> rm, T defaultVal) {
        try {
            return queryForObject(sql, args, argTypes, rm);
        } catch (DataAccessException e) {
            return defaultVal;
        }
    }

    public <T> T queryForObject(String sql, Class<T> requiredType, T defaultVal) {
        try {
            return queryForObject(sql, requiredType);
        } catch (DataAccessException e) {
            return defaultVal;
        }
    }

    public <T> T queryForObject(String sql, Object[] args, Class<T> requiredType, T defaultVal) {
        try {
            return queryForObject(sql, args, requiredType);
        } catch (DataAccessException e) {
            return defaultVal;
        }
    }

    public <T> T queryForObject(String sql, Object[] args, int[] argTypes, Class<T> requiredType, T defaultVal) {
        try {
            return queryForObject(sql, args, argTypes, requiredType);
        } catch (DataAccessException e) {
            return defaultVal;
        }
    }

    public <T> T optionalUniqueResult(List<T> results, T defaultVal) {
        if (results == null || results.isEmpty()) {
            return defaultVal;
        }
        return results.get(0);
    }

    public <T> List<T> query(String sql, final RowMapper<T> rm, final int limit) throws DataAccessException {
        return query(sql, new ResultSetExtractor<List<T>>() {
            @Override
            public List<T> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<T> results = new ArrayList<T>();
                int rowNum = 0;
                while (rs.next() && (limit <= 0 || rowNum < limit)) {
                    results.add(rm.mapRow(rs, rowNum++));
                }
                return results;
            }
        });
    }

    public <T> List<T> query(String sql, Object[] args, final RowMapper<T> rm, final int limit) throws DataAccessException {
        return query(sql, args, new ResultSetExtractor<List<T>>() {
            @Override
            public List<T> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<T> results = new ArrayList<T>();
                int rowNum = 0;
                while (rs.next() && (limit <= 0 || rowNum < limit)) {
                    results.add(rm.mapRow(rs, rowNum++));
                }
                return results;
            }
        });
    }

    public <T> List<T> query(String sql, GenericRowMapper<T> rm) throws DataAccessException {
        return super.query(sql, rm);
    }

    public <T> List<T> query(String sql, Object[] args, GenericRowMapper<T> rm) throws DataAccessException {
        return super.query(sql, args, rm);
    }

    public <T> List<T> query(String sql, GenericRowMapper<T> rm, int limit) throws DataAccessException {
        return query(sql, (RowMapper<T>) rm, limit);
    }

    public <T> List<T> query(String sql, Object[] args, GenericRowMapper<T> rm, int limit) throws DataAccessException {
        return query(sql, args, (RowMapper<T>) rm, limit);
    }

    public <T> T queryForObject(String sql, GenericRowMapper<T> rm, T defaultVal) {
        try {
            return (T) super.queryForObject(sql, (RowMapper<T>) rm);
        } catch (DataAccessException e) {
            return defaultVal;
        }
    }

    public <T> T queryForObject(String sql, Object[] args, GenericRowMapper<T> rm, T defaultVal) {
        try {
            return (T) super.queryForObject(sql, args, (RowMapper<T>) rm);
        } catch (DataAccessException e) {
            return defaultVal;
        }
    }

    public <T> T queryForObject(String sql, Object[] args, int[] argTypes, GenericRowMapper<T> rm, T defaultVal) {
        try {
            return (T) super.queryForObject(sql, args, argTypes, (RowMapper<T>) rm);
        } catch (DataAccessException e) {
            return defaultVal;
        }
    }

    public <T> List<T> query(String sql, Class<T> clazz) {
        return super.queryForList(sql, clazz);
    }

    public <T> List<T> query(String sql, Object[] args, Class<T> clazz) {
        return super.queryForList(sql, args, clazz);
    }

    public int update(String sql, Object[] args, org.springframework.jdbc.support.KeyHolder keyHolder) throws DataAccessException {
        return update(sql, args, null, keyHolder);
    }

    public int update(final String sql, final Object[] args, final int[] argTypes, final org.springframework.jdbc.support.KeyHolder keyHolder) throws DataAccessException {
        return update(new org.springframework.jdbc.core.PreparedStatementCreator() {
            @Override
            public java.sql.PreparedStatement createPreparedStatement(java.sql.Connection con) throws SQLException {
                java.sql.PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
                if (args != null) {
                    org.springframework.jdbc.core.PreparedStatementSetter setter = argTypes != null
                            ? new org.springframework.jdbc.core.ArgumentTypePreparedStatementSetter(args, argTypes)
                            : new org.springframework.jdbc.core.ArgumentPreparedStatementSetter(args);
                    setter.setValues(ps);
                }
                return ps;
            }
        }, keyHolder);
    }

    public int update(final String sql, final org.springframework.jdbc.core.PreparedStatementSetter pss, final org.springframework.jdbc.support.KeyHolder keyHolder) throws DataAccessException {
        return update(new org.springframework.jdbc.core.PreparedStatementCreator() {
            @Override
            public java.sql.PreparedStatement createPreparedStatement(java.sql.Connection con) throws SQLException {
                java.sql.PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
                if (pss != null) {
                    pss.setValues(ps);
                }
                return ps;
            }
        }, keyHolder);
    }
}
