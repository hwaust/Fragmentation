package fragmentation;

import java.io.File;

import basex.DBInfo;

public class FContext {
	public String ip;
	public String db;
	public String algo;
	public int N;
	public int maxsize;
	public String outputFolder;
	public boolean needExport;

	public FContext() {
		ip = "localhost";
		db = "xmark0.01";
		algo = "hw";
		N = 4;
		maxsize = 1000;
		outputFolder = "D:\\data\\fragments";
		needExport = true;
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
			case "algo":
				fc.algo = v;
				break;
			case "n":
				fc.N = Integer.parseInt(v);
				break;
			case "ms":
				fc.maxsize = Integer.parseInt(v);
				break;
			case "f":
				fc.outputFolder = v;
				break;
			case "export":
				fc.needExport = v.equals("on");
				break;
			}

		}

		fc.check();

		return fc;
	}

	void check() throws Exception {
		if (ip == null)
			throw new Exception(String.format("violating Context.server != null."));
		if (db == null)
			throw new Exception("violating Context.table != null.");
		if (N < 2)
			throw new Exception("violating Context.N > 2.");
	}

	public String toString() {
		return String.format("%s:%s by %s, N=%d, maxsize=%d, output directory=%s%s\n", ip, db, algo, N, maxsize,
				outputFolder, needExport ? "" : ", no export.");
	}

	/**
	 * Format: output folder + / + filename + .xml
	 * 
	 * @param filename
	 * @return
	 */
	public String getOutputPath(String filename) {
		return String.format("%s%s.xml", outputFolder + File.separator, filename);
	}

	/**
	 * Returns the name in format of outputfolder/db_algo_N_maxsize.txt
	 * 
	 * @return
	 */
	public String getFillFilename() {
		return String.format("%s%s_%s_%d_%d.txt", outputFolder + File.separator, db, algo, N, maxsize);
	}

	/**
	 * Returns the name in format of outputfolder/db_algo_N_maxsize.txt It will make
	 * the directory if the folder doex not exist.
	 * 
	 * @return
	 */
	public String getFullPath(String filename) {
		String dir = outputFolder + File.separator + String.format("%s_%s_%d_%d", db, algo, N, maxsize);
		File file = new File(dir);
		file.mkdir();

		if (filename != null && filename.length() > 0)
			dir = dir + File.separator + filename;

		return dir;
	}

	public DBInfo[] getDBInfo() {
		String dir = this.getFullPath(null);
		DBInfo[] dbs = new DBInfo[N];
		for (int i = 0; i < dbs.length; i++)
			dbs[i] = new DBInfo(ip, "frag_" + i, dir);

		return dbs;
	}
}
