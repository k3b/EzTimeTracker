package de.k3b.timetracker.database;

import java.util.ArrayList;
import java.util.List;

public class SqlFilterBuilder {
	private static final String[] STRING_ARRAY = new String[0];

	private final StringBuilder sql = new StringBuilder();
	private final List<String> filterArgs = new ArrayList<String>();

	public SqlFilterBuilder() {

	}

	public SqlFilterBuilder addConst(final String field,
			final String sqlExpression) {
		this.addAND();
		this.sql.append(field);
		this.sql.append(" ");
		this.sql.append(sqlExpression);
		return this;
	}

	public SqlFilterBuilder add(final String sqlExpressiont,
			final String value, final String emptyValue) {
		if (emptyValue.compareTo(value) != 0) {
			this.addAND();
			this.sql.append(sqlExpressiont);
			this.filterArgs.add(value);
		}
		return this;
	}

	public SqlFilterBuilder addAND() {
		if (this.sql.length() > 0) {
			this.sql.append(" AND ");
		}
		return this;
	}

	public SqlFilter toFilter() {
		if (this.sql.length() == 0) {
			return new SqlFilter(null, (String[]) null);
		} else {
			return new SqlFilter(this.sql.toString(),
					(this.filterArgs.size() == 0) ? null : this.filterArgs
							.toArray(SqlFilterBuilder.STRING_ARRAY));
		}
	}

}
