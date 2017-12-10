package utils;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Find a specified string from a specified file.
 * 
 * args[0]: a specifed plain text file.
 * 
 * args[1]: a target string for searching.
 * 
 * @author Hao
 * 
 *         Date: 2017-12-10
 *
 *
 */

public class StringFinder {

	public static void main(String[] args) throws Exception {
		args = args.length == 0 ? "d:\\data\\xmark\\xmark1.xml \"category52\"".split(" ") : args;
		System.out.printf("Fining %s from %s \n", args[1], args[0]);
		FileReader reader = new FileReader(args[0]);
		BufferedReader br = new BufferedReader(reader);
		String str = null;
		int lineno = 0;
		while ((str = br.readLine()) != null) {
			lineno++;
			if (str.contains(args[1])) {
				System.out.println("Found in line " + lineno + ": " + str);
			}
		}
		br.close();
		reader.close();
	}
}
