package fragmentation;

import basex.QueryPlan;
import basex.QueryPlans;

public class QContext {

	public String datafolder;
	public String[] ips; // used for query
	public String[] dbs; // used for query
	public int p; // number of thread used.
	public static boolean isWin = true;
	public QueryPlan query;

	public QContext() {
		datafolder = "D:\\data\\fragments";
	}

	public static QContext parse(String[] args) throws Exception {
		QContext fc = new QContext();

		for (int i = 0; i < args.length / 2; i++) {
			String op = args[i * 2].substring(1);
			String v = args[i * 2 + 1];

			switch (op) {

			case "f":
				fc.datafolder = v;
				break;

			case "iplist":
				fc.ips = parseIPs(v);
				break;

			case "dblist":
				fc.dbs = parseDBs(v);
				break;

			case "key":
				fc.query = QueryPlans.getQueryPlan(v);
				break;

			case "sys":
				QContext.isWin = v.equals("win");
				break;
				
			case "p":
				fc.p = Integer.parseInt(v);
				break;
			}

		}

		fc.check();

		return fc;
	}

	void check() throws Exception {
		if (this.ips == null)
			throw new Exception("No avaiable IP address.");
		if (this.dbs == null)
			throw new Exception(String.format("No available database."));
		if (query == null)
			throw new Exception("Query not found");
		if (ips.length != dbs.length)
			throw new Exception(String.format("IP address and DB names does not match in length."));
	}

	/**
	 * Parse a string to create a list of IP addresses.
	 * 
	 * @param ipstr
	 * @return
	 */
	public static String[] parseIPs(String ipstr) {
		// use lab to represent "172.21.52"
		String ip = ipstr.replace("lab", "172.21.52.");
		ip = ip.replace("home", "192.168.1.");

		String[] ips;
		// Syntax sugar: #4 means localhost;localhost;localhost;localhost
		if (ip.startsWith("#")) {
			int num = Integer.parseInt(ip.substring(1));
			ips = new String[num];
			for (int i = 0; i < ips.length; i++)
				ips[i] = "localhost";
		}
		// Format 2: range:172.21.52.50:3 equals to
		// 172.21.52.50;172.21.52.51;...;172.21.52.63
		else if (ip.startsWith("range")) {
			String[] addresses = ip.split(":")[1].split("\\.");
			int start = Integer.parseInt(addresses[3]);
			ips = new String[Integer.parseInt(ip.split(":")[2])];
			for (int i = 0; i < ips.length; i++)
				ips[i] = addresses[0] + "." + addresses[1] + "." + addresses[2] + "." + (start + i);
		}
		// Format 1: ip;ip;...;ip
		else {
			ips = ip.split(";");
		}
		return ips;
	}

	public static String[] parseDBs(String dbstr) {
		String[] dbs;
		//
		// range:dbname:start-end, such as range:mfrag:0-3.
		if (dbstr.startsWith("range")) {
			String[] strs = dbstr.split(":");
			int start = Integer.parseInt(strs[2].split("-")[0]);
			int end = Integer.parseInt(strs[2].split("-")[1]);

			dbs = new String[end - start + 1];
			for (int i = 0; i < dbs.length; i++)
				dbs[i] = strs[1] + (start + i);
		}
		// db list db1;db2;db3...;dbn
		else if (dbstr.contains(";")) {
			dbs = dbstr.split(";");
		}
		// -dblist xmark1_1_20k:5
		else {
			dbs = new String[Integer.parseInt(dbstr.split((":"))[1])];
			for (int i = 0; i < dbs.length; i++)
				dbs[i] = dbstr.split(":")[0];
		}

		return dbs;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Server list: ");
		for (int i = 0; i < ips.length; i++)
			sb.append(ips[i] + "." + dbs[i] + "; ");
		sb.append("\n");

		sb.append("Query Key: " + query.key + "\n");
		sb.append("Input folder: " + datafolder + "\n");

		return sb.toString();
	}

}
