package basex;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class PreValueReceiver {

	public static void main(String[] args) throws Exception {

		String str = "\n1\nabc\n\n2\nbcd\n\n3\nefg";

		final InputStream instr = new ByteArrayInputStream(str.getBytes());

		PreValueReceiver rec = new PreValueReceiver();

		QueryResult_IntStringList rd = rec.process_linux(instr);

		for (Integer item : rd.pres)
			System.out.println(item);

		for (String item : rd.values)
			System.out.println(item);
	}

	QueryResult_IntStringList process(final InputStream input) throws IOException {
		return new File("c:").exists() ? process_win(input) : process_linux(input);
	}

	QueryResult_IntStringList process_win(final InputStream input) throws IOException {
		ArrayList<Integer> its = new ArrayList<Integer>();
		ArrayList<String> strs = new ArrayList<String>();
		boolean isPreEnter = true;
		StringBuilder sb = new StringBuilder();
		int value = 0;
		int b = 0;

		while ((b = input.read()) > 0) {
			if (b == '\r') {
				input.read(); // skip \n

				if (isPreEnter) {
					if (sb.length() > 0)
						strs.add(sb.toString());
					sb = new StringBuilder();
					isPreEnter = false;

					while (true) {
						b = input.read();
						if (b == '\r')
							break;
						value *= 10;
						value += b - '0';
					}
					its.add(value);
					value = 0;
					input.read();
				} else
					isPreEnter = true;
			} else {
				isPreEnter = false;
				sb.append((char) b);
			}
		}

		if (sb.length() > 0)
			strs.add(sb.toString());

		QueryResult_IntStringList rd = new QueryResult_IntStringList();
		rd.pres = its;
		rd.values = strs;
		return rd;
	}

	QueryResult_IntStringList process_linux(final InputStream input) throws IOException {
		ArrayList<Integer> its = new ArrayList<Integer>();
		ArrayList<String> strs = new ArrayList<String>();
		boolean isPreEnter = true;
		StringBuilder sb = new StringBuilder();
		int value = 0;
		int b = 0;

		while ((b = input.read()) > 0) {
			if (b == '\n') {
				if (isPreEnter) {
					if (sb.length() > 0)
						strs.add(sb.toString());
					sb = new StringBuilder();
					isPreEnter = false;
					while (true) {
						b = input.read();
						if (b == '\n')
							break;
						value *= 10;
						value += b - '0';
					}
					its.add(value);
					value = 0;
				} else
					isPreEnter = true;
			} else {
				isPreEnter = false;
				sb.append((char) b);
			}
		}

		if (sb.length() > 0)
			strs.add(sb.toString());

		QueryResult_IntStringList rd = new QueryResult_IntStringList();
		rd.pres = its;
		rd.values = strs;
		return rd;

	}

}
