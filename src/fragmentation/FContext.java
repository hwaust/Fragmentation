package fragmentation;

import java.io.File;

import basex.common;

public class FContext {
	public String ip;
	public String db;
	public int Ns;
	public int seed;
	public int maxsize;
	public String datafolder;
	public boolean needExport;

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
		if (Ns < 2)
			throw new Exception("violating Context.N > 2.");
	}

	public String toString() {
		return String.format("%s:%s, Ns=%d, maxsize=%s, seed=%d, output directory=%s\n", ip, db, Ns,
				common.IntToFormatedString(maxsize), seed, getFullPath(""));
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

 
}
