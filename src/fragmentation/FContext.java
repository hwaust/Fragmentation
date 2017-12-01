package fragmentation;

import java.io.File;

import basex.DBInfo;
import basex.common;

public class FContext {
	public String ip;
	public String db;
	public int Ns;
	public int seed;
	public int maxsize;
	public String datafolder;
	public boolean needExport;

	public String querykey;
	public String[] ips; // used for query
	public String[] dbs; // used for query

	public FContext() {
		ip = "localhost";
		db = "xmark0.01";
		Ns = 16;
		maxsize = 1000;
		datafolder = "D:\\data\\fragments";
		needExport = true;
		seed = 20171126;

	}

	public static FContext parse(String[] args) throws Exception {
		FContext fc = new FContext();

		for (int i = 0; i < args.length / 2; i++) {
			String op = args[i * 2].substring(1);
			String v = args[i * 2 + 1];

			switch (op) {
			case "sv":
				fc.ip = v;
				break;
			case "db":
				fc.db = v;
				break;
			case "n":
				fc.Ns = Integer.parseInt(v);
				break;
			case "ms":
				fc.maxsize = common.FormatedStringToInt(v);
				break;
			case "f":
				fc.datafolder = v;
				break;
			case "export":
				fc.needExport = v.equals("on");
				break;
			case "seed":
				fc.seed = Integer.parseInt(v);
				break;

			case "iplist":
				fc.ips = parseIPs(v);
				break;

			case "dblist":
				fc.dbs = parseDBs(v);
				break;

			case "key":
				fc.querykey = v;
				break;
			}

		}

		fc.check();

		return fc;
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
				ips[i] = start + i + "";
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

	void check() throws Exception {
		if (ip == null)
			throw new Exception(String.format("violating Context.server != null."));
		if (db == null)
			throw new Exception("violating Context.table != null.");
		if (Ns < 2)
			throw new Exception("violating Context.N > 2.");
	}

	public String toString() {
		return String.format("%s:%s, Ns=%d, maxsize=%s, seed=%d, output directory=%s\n", ip, db, Ns,
				common.IntToFormatedString(maxsize), seed, getFullPath(""));
	}

	public String toString1() {
		StringBuilder sb = new StringBuilder();

		sb.append("Server list: ");
		for (int i = 0; i < ips.length; i++)
			sb.append(ips[i] + "." + dbs[i]+"; ");
		sb.append("\n");

		sb.append("Query Key: " + querykey + "\n");
		sb.append("Input folder: " + datafolder + "\n");

		return sb.toString();
	}

	/**
	 * Returns the name in format of outputfolder/db_algo_N_maxsize.txt It will make
	 * the directory if the folder doex not exist.
	 * 
	 * @return
	 */
	public String getFullPath(String filename) {
		// fragmentindex_xmark600_16_4M_256_20171126

		String dir = datafolder + File.separator
				+ String.format("%s_%d_%s_%d", db, Ns, common.IntToFormatedString(maxsize), seed);
		File file = new File(dir);
		file.mkdir();

		if (filename != null && filename.length() > 0)
			dir = dir + File.separator + filename;

		return dir;
	}

	public DBInfo[] getDBInfo() {
		String dir = this.getFullPath(null);
		DBInfo[] dbs = new DBInfo[Ns];
		for (int i = 0; i < dbs.length; i++)
			dbs[i] = new DBInfo(ip, "frag_" + i, dir);

		return dbs;
	}
}
