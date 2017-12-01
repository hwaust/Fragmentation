package basex;

public class QueryResult {
	
	public static void main(String[] args) throws Exception {
		String server = "172.21.52.50";
		String database = "xmark40";
		int position = 0;
		int partition = 16;
		QueryPlan query_org = QueryPlans.getQueryPlan("xm4.org");
		QueryPlan query_dps = QueryPlans.getQueryPlan("xm4b.dps");
		
//		BXClient bx = BXClient.open(server);
//		String q1 = "xquery count(db:open('xmark40')/vn/site/regions/*/item)";
//		System.out.println(bx.execute(q1));
		/************************** Original *****************************/
		
		String xquery = Context.getOriginal(query_org, database, position); 
		BXClient bx = BXClient.open(server);
		QueryResult qr = bx.exeQuery(xquery);
		qr.show();
		
		/************************** DPS *****************************/
		DPS dps = new DPS(server, database, position, partition);
		dps.open(); 
		dps.query = query_dps; 
		 (new Thread(dps)).start();
	}
	
	public long exetime;
	public long receiving;
	public String result;
	public String info;

	public QueryResult(long exetime, long receiving, String result) {
		this.exetime = exetime;
		this.receiving = receiving;
		this.result = result;
	}

	public QueryResult() {
	}

	public long totalTime() {
		return exetime + receiving;
	}

	public int resultSize() {
		return result == null ? 0 : result.length();
	}

	public void show() {
		System.out.printf("time: %d ms, size: %d.\n", this.totalTime(), this.resultSize());		
	}
}
