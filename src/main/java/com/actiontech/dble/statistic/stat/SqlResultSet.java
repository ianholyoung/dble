package com.actiontech.dble.statistic.stat;

/**
 * SqlResultSet
 */
public class SqlResultSet {
    private String sql;
    private int resultSetSize = 0;
    private int count;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public int getResultSetSize() {
        return resultSetSize;
    }

    public void setResultSetSize(int resultSetSize) {
        this.resultSetSize = resultSetSize;
    }

    public int getCount() {
        return count;
    }

    public void count() {
        this.count++;
    }


}
