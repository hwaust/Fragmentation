package basex;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * A socket based client in charge of processing queries with a remote (or
 * local) BaseX server.
 * 
 * Two possible settings: set mainmem on | set queryinfo on
 * 
 * @author Administrator
 *
 */
public class BXClient extends BaseXClient {
	public int tagid;

	Charset encoding = Charset.forName("UTF-8");

	public static BXClient open(String ip) throws IOException {
		BXClient bx = new BXClient(ip, 1984, "admin", "admin");
		bx.send("set queryinfo on");
		return bx;
	}

	public BXClient(String host, int port, String username, String password) throws IOException {
		super(host, port, username, password);
	}

	public QueryResult exeQuery(final String command) throws Exception {
		QueryResult qr = new QueryResult();
		final ByteArrayOutputStream os = new ByteArrayOutputStream();

		long start = System.currentTimeMillis();
		execute(command, os);
		qr.exetime = System.currentTimeMillis() - start;
		qr.info = this.info;

		start = System.currentTimeMillis();
		qr.result = new String(os.toByteArray(), encoding);
		qr.receiving = System.currentTimeMillis() - start;

		return qr;
	}

	public QueryResult executeForIntStringArray(final String command) throws IOException {
		long start = System.currentTimeMillis();
		send(command);
		QueryResultPre qr = PreValueReceiver.process(in);

		info = receive();
		if (!ok())
			throw new IOException(info);
		qr.exetime = System.currentTimeMillis() - start;
		qr.info = this.info;

		start = System.currentTimeMillis();
		qr.receiving = System.currentTimeMillis() - start;

		return qr;
	}

	public void execute(String command, FileWriter fw) throws Exception {
		send(command);
		int b = 0;

		BufferedWriter bw = new BufferedWriter(fw, 1024 * 1024);
		while ((b = in.read()) > 0) {
			bw.write(b);
		}
		bw.flush();
		info = receive();
		if (!ok())
			throw new IOException(info);
	}
}
