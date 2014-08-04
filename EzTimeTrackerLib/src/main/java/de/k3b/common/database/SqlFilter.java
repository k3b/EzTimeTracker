package de.k3b.common.database;

/**
 * helper class because java function cannot return 2 values
 */
public class SqlFilter {
    /**
     * genereted sql-where
     */
    public final String sql;
    /**
     * "?" placeholder values needed for prepared sql statements
     */
    public final String[] args;

    public SqlFilter(final String sql, final String... args) {
        this.sql = sql;
        this.args = args;
    }

    /**
     * formats sql for debugging purposes
     */
    public String getDebugMessage(final String debugContext) {
        final StringBuilder result = new StringBuilder().append(debugContext)
                .append(": ").append(this.sql);
        if (this.args != null) {
            result.append(" [");
            for (final String argument : this.args) {
                result.append("'").append(argument).append("', ");
            }
            result.append("]");
        }
        return result.toString();
    }

    @Override
    public String toString() {
        return this.getDebugMessage("TimeSliceSql.SqlFilter");
    }
}
