package de.k3b.database;

/**
 * helper class because java function cannot return 2 values
 */
public class SqlFilter {
	public SqlFilter(final String sql, final String... args) {
		this.sql = sql;
		this.args = args;
	}

	/**
	 * genereted sql-where
	 */
	public final String sql;

	/**
	 * "?" placeholder values needed for prepared sql statements
	 */
	public final String[] args;

	/**
	 * formats sql for debugging purposes
	 */
	public String getDebugMessage(final String debugContext) {
		final StringBuffer result = new StringBuffer().append(debugContext)
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
};
