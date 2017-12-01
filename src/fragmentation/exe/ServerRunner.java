package fragmentation.exe;

import basex.BXClient;
import basex.DBInfo;

public class ServerRunner {

	public static void main(String[] args) throws Exception {
		// for test
		if (args.length == 0)
			args = new String[] { "#4", "d:\\data\\fragments\\xmark0.1_hw_4_1000" };

		DBInfo[] dbs = DBInfo.parse(args);

		System.out.println("data folder: " + args[1]);
		for (int i = 0; i < dbs.length; i++) {
			BXClient bx = BXClient.open(dbs[i].ip);
			bx.execute("set mainmem on");
			bx.execute(dbs[i].getCreateDbCmd());
			System.out.printf("Running server: %s\n", dbs[i].toString());
		}

		System.out.println("All databases have been created. \nPress any key to stop all servers and exit...");
		System.in.read();
		System.out.println("Exited.");
	}

}
