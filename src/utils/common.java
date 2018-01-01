package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import fragmentation.FragmentIndex;

public class common {

	public static void main(String[] args) throws Exception {

	}

	/**
	 * Returns the current time in a formatted string: yyyyMMdd_HHmmss
	 * 
	 * @return
	 */
	public static String getDateString() {
		return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	}

	public static String ReadALLContent(String filepath) throws Exception {
		return String.join("\n", Files.readAllLines(Paths.get(filepath)));
	}

	public static void saveStringtoFile(String content, String path) {
		try {
			FileWriter fw = new FileWriter(path);
			fw.write(content, 0, content.length());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getFolder(String name) {
		String currentFolder = common.getCurrentFolder();
		if (name == null || name.length() == 0)
			return currentFolder;

		String subfolder = currentFolder + File.separator + name + File.separator;
		File f = new File(subfolder);
		if (!f.exists())
			f.mkdir();

		return subfolder;
	}

	public static String getCurrentFolder() {
		return System.getProperty("user.dir");
	}

	public static String getLinebreaker() {
		return File.separator.equals("\\") ? "\r\n" : "\n";
	}

	public static String[] split2lines(String text) {
		String[] lines = text.split("\r\n");
		if (lines.length == 1)
			lines = lines[0].split("\n");
		return lines;
	}

	public FragmentIndex[] getLinks() {
		return null;
	}

	public static long[] getSplitPosition(String file, int count) throws Exception {
		count = count < 1 ? 1 : count;
		long[] splits = new long[count + 1];
		splits[0] = 0;

		RandomAccessFile rf = new RandomAccessFile(file, "rw");
		System.out.println("filesize: " + rf.length());
		for (int i = 1; i < splits.length - 1; i++) {
			splits[i] = rf.length() * i / count;
			rf.seek(splits[i]);
			while (true) {
				byte d = rf.readByte();
				if (d == '<')
					break;
				splits[i]++;
			}
		}

		splits[count] = rf.length();
		rf.close();

		return splits;
	}

	public static String readAllText(String scr) throws Exception {
		StringBuilder sb = new StringBuilder();

		int buf_size = 1024 * 1024;
		byte[] bts = new byte[buf_size];
		long curpos = 0;
		RandomAccessFile source = new RandomAccessFile(scr, "r");
		long end = source.length();

		while (curpos < end) {
			int len = buf_size;
			if (curpos + len > end)
				len = (int) (end - curpos);
			source.read(bts, 0, len);
			// dest.write(bts, 0, len);
			curpos += len;
		}

		source.close();

		return sb.toString();
	}

	public static void filePartialCopy(String scr, long start, long end, String dst) throws Exception {
		int buf_size = 1024 * 1024;
		byte[] bts = new byte[buf_size];
		long curpos = start;
		RandomAccessFile source = new RandomAccessFile(scr, "r");
		RandomAccessFile dest = new RandomAccessFile(dst, "rw");

		source.seek(start);
		while (curpos < end) {
			int len = buf_size;
			if (curpos + len > end)
				len = (int) (end - curpos);
			source.read(bts, 0, len);
			dest.write(bts, 0, len);
			curpos += len;
		}

		source.close();
		dest.close();

	}

	public static String getLinebreak() {
		return new File("c:").exists() ? "\r\n" : "\n";
	}

	public static String[] getUniqueStrings(String[] strs) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (String s : strs)
			map.putIfAbsent(s, s);

		String[] values = new String[map.values().size()];
		int i = 0;
		for (String v : map.values())
			values[i++] = v;

		return values;
	}

	public static int[] splitArray(int total, int size) {
		int[] arr = new int[total / size + 2];

		for (int i = 1; i < arr.length; i++) {
			arr[i] = i == arr.length - 1 ? total : size * i;
		}

		return arr;
	}

	/**
	 * Format a integer to GB, MB or KB
	 * 
	 * @param filesize
	 * @return
	 */
	public static String formatFileSize(long filesize) {
		if (filesize < 0) {
			return "Error: Current filesize is less then zero. (Size = " + filesize + ")";
		}
		long quotion = filesize;
		long mode = 0;
		double result = quotion;
		int i = 0;
		String[] name = { "B", "KB", "MB", "GB", "TB", "PB", "ZB" };
		String str = "";

		while (true) {
			if (result < 1024) {
				str = String.format("%4.2f %s", result, name[i]);
				break;
			}

			i++;
			mode = quotion % 1024;
			quotion = quotion / 1024;
			result = mode * 1.0 / 1024 + quotion;

			if (i >= name.length) {
				str = "too large to express...";
				break;
			}
		}
		return str;
	}

	/**
	 * show memory info
	 */
	public static void showMemory() {
		Runtime r = Runtime.getRuntime();
		long mfree = r.freeMemory();
		long mtotal = r.totalMemory();
		long mused = mtotal - mfree;

		System.out.printf("used = %s, free = %s, total = %s.", common.formatFileSize(mused),
				common.formatFileSize(mfree), common.formatFileSize(mtotal));
	}

	public static int FormatedStringToInt(String s) {
		int value = 0;
		// to support abbreviations, such as 20M, 300K, etc.
		if (!Character.isDigit(s.charAt(s.length() - 1))) {
			switch (s.charAt(s.length() - 1)) {
			case 'm':
			case 'M':
				value = 1000 * 1000 * Integer.parseInt(s.substring(0, s.length() - 1));
				break;
			case 'k':
			case 'K':
				value = 1000 * Integer.parseInt(s.substring(0, s.length() - 1));
				break;
			}
		} else
			value = Integer.parseInt(s);
		return value;
	}

	public static String IntToFormatedString(int v) {
		return v > 1000000 ? v / 1000000 + "M" : v > 1000 ? v / 1000 + "K" : v + "";
	}

	public static void gc() {
		Runtime r = Runtime.getRuntime();
		long total = r.totalMemory();
		long used1 = r.totalMemory() - r.freeMemory();

		long t1 = System.currentTimeMillis();
		System.gc();
		long t2 = System.currentTimeMillis();

		r = Runtime.getRuntime();
		long used2 = r.totalMemory() - r.freeMemory();

		System.out.printf("Used memory: %d MB -> %d MB of %d MB. Time cost: %d ms.\n", used1 / 1048576, used2 / 1048576,
				total / 1048576, t2 - t1);
	}

}
