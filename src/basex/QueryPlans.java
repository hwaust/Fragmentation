package basex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import utils.common;

public class QueryPlans {
	static HashMap<String, QueryPlan> xqueries;

	public static void main(String[] arge) throws Exception {
		initialize();
		System.out.println(xqueries.size());
		xqueries.get("xm1.org").show();
	}

	/**
	 * Get the query plan with the specified key.
	 * 
	 * @param key
	 *            key to the query plan.
	 * @return
	 */
	public static QueryPlan getQueryPlan(String key) throws Exception {
		if (xqueries == null)
			initialize();
		return xqueries.get(key);
	}

	public static void initialize() throws Exception {
		BufferedReader br = new BufferedReader(
				new FileReader(common.getCurrentFolder() + File.separator + "bxQueries.txt"));
		xqueries = new HashMap<String, QueryPlan>();
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;

			// remove empty or commentary lines.
			line = line.trim();
			if (line.length() == 0 || line.startsWith("--") || line.startsWith("//"))
				continue;

			// get key and value.
			int p = line.indexOf(':');
			if (p < 0)
				continue;

			String key = line.substring(0, p).trim();
			String value = line.substring(p + 1).trim();

			QueryPlan qp = xqueries.get(key);
			if (qp == null) {
				qp = new QueryPlan(key);
				xqueries.put(key, qp);
			}
			qp.add(value);
		}
		
		br.close();

		// set optimization.
		QueryPlan optimization = xqueries.get("optimization");
		if (optimization != null) {
			for (String key : optimization.first().split(";")) {
				QueryPlan q = xqueries.get(key.trim());
				if (q != null)
					q.optimized = true;
			}
		}

	}

}
