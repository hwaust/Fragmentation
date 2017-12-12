package utils;

import java.io.File;
import java.io.FileInputStream;

/**
 * Examine the occurrence of all 256 ASICC characters of an input file.
 */
public class CharacterOccurence {

	/**
	 * Returns occurrence of 256 character of an input file.
	 * 
	 * @param args
	 *            Only one argument, an input file.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		args = args.length == 0 ? "D:\\data\\xmark\\xmark1.xml".split(" ") : args;

		if (!new File(args[0]).exists())
			throw new Exception("File " + args[0] + " does not exist.");

		long[] charcount = new long[256];
		long processedBytes = 0;
		FileInputStream fis = new FileInputStream(args[0]);
		byte[] buffer = new byte[10485760];
		int len = buffer.length;
		while (len == buffer.length) {
			len = fis.read(buffer);
			processedBytes += len;
			for (int i = 0; i < len; i++) {
				charcount[buffer[i]]++;
			}
			System.out.println("processed " + (processedBytes / 1048576) + " MB.");
		}
		fis.close();

		System.out.println("\nchar\toccurence");
		for (int i = 0; i < charcount.length; i++) {
			System.out.printf("%d\t%d\n", i, charcount[i]);
		}
	}
}
