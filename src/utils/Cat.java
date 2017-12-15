package utils;

import java.io.RandomAccessFile;

public class Cat {

	public static void main(String[] args) throws Exception {
		String filepath = args[0];
		int startpos = Integer.parseInt(args[1]);
		int length = Integer.parseInt(args[2]);

		RandomAccessFile fr = new RandomAccessFile(filepath, "rw");

		fr.seek(startpos);

		byte[] bts = new byte[length];

		fr.read(bts);

		String str = new String(bts);

		fr.close();

		System.out.println(str);
	}
}
