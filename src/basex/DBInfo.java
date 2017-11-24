package basex;

import java.io.File;

public class DBInfo {
	public String ip;
	public String dbname;
	public String directory;

	public DBInfo(String ip, String dbname, String directory) {
		this.ip = ip;
		this.dbname = dbname;
		this.directory = directory;
	}

	public String getFullpath() {
		return directory + File.separator + dbname + ".xml";
	}

	public String toString() {
		return String.format("IP=%s; database=%s.", ip, dbname);
	}

	public static DBInfo[] parse(String[] args) throws Exception {
		if (args == null || args.length < 2)
			throw new Exception("The length of arguments do equal to 2.");

		// use lab to represent "172.21.52"
		String ip = args[0].replace("lab", "172.21.52.");
		String dir = args[1];
		DBInfo[] dbs = null;

		// Syntax sugar: #4 means localhost;localhost;localhost;localhost
		if (ip.startsWith("#")) {
			int num = Integer.parseInt(ip.substring(1));
			dbs = new DBInfo[num];
			for (int i = 0; i < dbs.length; i++)
				dbs[i] = new DBInfo("localhost", "frag_" + i, dir);
		}
		// Format 2: range:172.21.52.50:3 equals to
		// 172.21.52.50;172.21.52.51;...;172.21.52.63
		else if (ip.startsWith("range")) {
			String[] addresses = ip.split(":")[1].split("\\.");
			int start = Integer.parseInt(addresses[3]);
			dbs = new DBInfo[Integer.parseInt(ip.split(":")[2])];
			for (int i = 0; i < dbs.length; i++) {
				addresses[3] = start + i + "";
				dbs[i] = new DBInfo(String.join(".", addresses), "frag_" + i, dir);
			}
		}
		// Format 1: ip;ip;...;ip
		else {
			String[] ips = ip.split(";");
			dbs = new DBInfo[ips.length];
			for (int i = 0; i < dbs.length; i++)
				dbs[i] = new DBInfo(ips[i], "frag_" + i, dir);
		}

		return dbs;
	}

	public void check() throws Exception {

	}

	/**
	 * Return create db command.
	 * @return
	 */
	public String getCreateDbCmd() {
		return 	String.format("create db %s %s", dbname, getFullpath());
	}
}
