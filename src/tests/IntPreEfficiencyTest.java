package tests;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import basex.BXClient;
import basex.QueryResultPre;
import basex.common;

public class IntPreEfficiencyTest {

	static char separator = '[';
	static String path =   "d:\\data\\xmark10_keyword.txt";
	
	public static void main(String[] args) throws IOException { 
		byte[] bts = Files.readAllBytes(Paths.get(path)); 
		ByteArrayInputStream bs = new ByteArrayInputStream(bts); 
		
		long total = 0;
		long time = 0;
		int runningTimes = 20;
		QueryResultPre qrl = process(bs);
		
		for (int i = 0; i < 3; i++) { 
			bs = new ByteArrayInputStream(bts); 
			qrl = process(bs); 
		}
		
		for (int i = 0; i < runningTimes; i++) {
			System.out.print(".");
			bs = new ByteArrayInputStream(bts);
			time = System.currentTimeMillis();
			qrl = process(bs);
			total += System.currentTimeMillis() - time;
		}
		
		System.out.printf("Time taken: %d ms. QR.PRES.Size=%d, Results.length=%d KiB.\n", 
				total / runningTimes, qrl.pres.size(), bts.length / 1024);
	}
	
	
	public static void makeFile(String path) throws IOException {
		BXClient bx = BXClient.open("localhost");
		
		String query ="xquery declare option output:method 'basex';\n" 
				+ "declare option output:item-separator '['; \n"
				+ "for $node in db:open('xmark10')//keyword \n"
				+ "  return (db:node-pre($node), $node)";
		String results = bx.execute(query);
		
		common.saveStringtoFile(results, path);
	}
	
	public static QueryResultPre process(final InputStream input) throws IOException {
		QueryResultPre rd = new QueryResultPre();
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

 
		  