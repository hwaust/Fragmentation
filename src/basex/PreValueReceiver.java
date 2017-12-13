package basex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PreValueReceiver {

	public static char separator = '[';

	public static void main(String[] args) throws Exception {
		InputStream fr = new ByteArrayInputStream("1[<root></root>[2[<agsfasdfa>[23423423[alsdfasdfasf".getBytes());
		QueryResult_IntStringList qi = process(fr);
		fr.close();
		for (int i = 0; i < qi.pres.size(); i++) {
			System.out.printf("%d: %s.\n", qi.pres.get(i), qi.values.get(i));
		}
	}

	public static QueryResult_IntStringList process(final InputStream input) throws IOException {
		QueryResult_IntStringList rd = new QueryResult_IntStringList();
		int b = 1;
		while (b > 0) {
			// read PRE value
			int value = 0;
			while ((b = input.read()) != separator) {
				value = value * 10 + (b - '0');
			}
			rd.pres.add(value);
			// read string content
			StringBuilder sb = new StringBuilder();
			while ((b = input.read()) > 0 && b != separator) {
				sb.append((char) b);
			}
			rd.values.add(sb.toString());
		}
		return rd;
	}
}
