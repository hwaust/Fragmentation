package basex;

import java.util.ArrayList;

public class QueryPlan {

	// The key of the query, such as xm1.org, xm2.dps etc.
	public String key;

	// Sub queries.
	ArrayList<String> subqueries;

	// indicate whether a query is optimized.
	public boolean optimized;


	public QueryPlan(String key) {
		this.key = key;
		this.subqueries = new ArrayList<String>(2);
		this.optimized = false;
	}

	public void show() {
		for (int i = 0; i < subqueries.size(); i++)
			System.out.println(key + ": " + subqueries.get(i));
	}

	public void add(String query) {
		this.subqueries.add(query);
	}

	public String first() {
		return this.subqueries.get(0);
	}

	public String last() {
		return this.subqueries.get(subqueries.size() - 1);
	}

}
