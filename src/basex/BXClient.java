package basex;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class BXClient extends BaseXClient {
	Charset encoding = Charset.forName("UTF-8");

	public static BXClient open(String ip) throws IOException {
		return new BXClient(ip, 1984, "admin", "admin");
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
		QueryResult_IntStringList qr = recieve(in);
		info = receive();
		if (!ok())
			throw new IOException(info);

		qr.exetime = System.currentTimeMillis() - start;
		qr.info = this.info;

		start = System.currentTimeMillis();
		qr.receiving = System.currentTimeMillis() - start;

		return qr;
	}

	public void executeToFile(final String command, final FileWriter fw) throws IOException {
		send(command);
		int b = 0;
		int buffsize = 1024 * 1024;
		int count = 0;
		while ((b = in.read()) > 0) {
			fw.write(b);
			if (count++ > buffsize) {
				fw.flush();
				count = 0;
			}
		}

		info = receive();
		if (!ok())
			throw new IOException(info);
	}

	// removed private
	private static QueryResult_IntStringList recieve(final InputStream input) throws IOException {
		return new PreValueReceiver().process(input);
	}

}
