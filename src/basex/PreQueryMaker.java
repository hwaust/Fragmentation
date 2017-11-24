package basex;

public class PreQueryMaker {
	String database;

	public static void main(String[] args) throws Exception {
		System.out.println(new PreQueryMaker("example").getQuery("1", "/*"));
	}

	public PreQueryMaker(String database) {
		this.database = database;
	}

	public String getQuery(String pre, String query) {
		return String.format("xquery db:open-pre('%s', %s)%s", database, pre, query);
	}

	public String getQueryIndexform(String pre, String query) {
		return String.format("xquery db:node-pre(db:open-pre('%s', %s)%s)", database, pre, query);
	}

	public String getCount(String pre, String query) {
		return String.format("xquery count(db:open-pre('%s', %s)%s)", database, pre, query);
	}

}
