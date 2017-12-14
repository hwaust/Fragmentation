package basex;

public class PExecutor extends MyRunnable {
	public QueryResult sr;
	public String xquery;
	public BXClient bx;
	public int resultType;

	public PExecutor(BXClient bxclient) {
		this.bx = bxclient;
		resultType = 0;
	}

	public PExecutor(BXClient processor, int resultType, String query) {
		this.resultType = resultType;
		this.bx = processor;
		this.xquery = query;
	}

	public PExecutor(String server, String database) throws Exception {
		bx = BXClient.open(server);
	}

	@Override
	public void run() {

		try {
			switch (resultType) {
			case 0:
				sr = bx.exeQuery(xquery);
				break;
			case 1:
				sr = bx.executeForIntStringArray(xquery);
				break;
			}
		} catch (Exception e) {
			System.out.println("PExecutor.run: result type = " + resultType + ", sr = " + sr);
			e.printStackTrace();
		}

		if (latch != null)
			latch.countDown();
	}

	public void close() throws Exception {
		bx.close();
	}

	public void setQuery(String xquery, int resultType) {
		this.xquery = xquery;
		this.resultType = resultType;
	}

}
