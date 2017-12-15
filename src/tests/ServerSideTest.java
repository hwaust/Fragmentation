package tests;

import basex.BXClient;
import basex.QueryPlan;
import basex.QueryPlans;
import basex.QueryResultPre;
import basex.ServerSide;

public class ServerSideTest {

	public static void main(String[] args) throws Exception {
		ServerSide ss = new ServerSide("xmark1");
		QueryPlan query = QueryPlans.getQueryPlan("xm3a.dps");
		int P = 4;

		BXClient bx = BXClient.open("localhost");
		String prefix = ss.getPrefix(query, P);

		// prepare
		ss.prepare(bx);

		// execute prefix query
		bx.execute(prefix);

		// execute suffix query
		for (String suffix : ss.getSuffix(query, P)) {
			QueryResultPre qr = (QueryResultPre) bx.executeForIntStringArray(suffix);
			System.out.printf("execution time: %d, result size: %d \n", qr.exetime, qr.pres.size());
		}

	}

}
