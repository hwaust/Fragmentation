package fragmentation.exe;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import basex.common;

public class TreeInfoAnalyzer {

	public static void main(String[] args) {
		args = args.length == 0 ? new String[] { "D:\\data\\fragments\\xmark10_16_200K_20171126" } : args;

		File dir = new File(args[0]);
		System.out.println(dir);

		if (!dir.exists())
			System.out.println("No such directory.");

		ArrayList<File> files = new ArrayList<>();

		for (File fc : dir.listFiles())
			if (fc.getName().startsWith("mfrag"))
				files.add(fc);

		Collections.sort(files, (fa, fb) -> fa.length() < fb.length() ? 1 : -1);

		long max = files.stream().reduce((mx, len) -> mx.length() < len.length() ? len : mx).get().length();
		long min = files.stream().reduce((mx, len) -> mx.length() > len.length() ? len : mx).get().length();

		double avg = 0;
		for (File f : files)
			avg += f.length();
		avg /= files.size();

		double dev = 0;
		for (File f : files)
			dev += (f.length() - avg) * (f.length() - avg);
		dev = Math.sqrt(dev);

		System.out.printf("max=%s, min=%s, averg=%s, deviation=%5.0f, averge/deviation=%5.2f.\n", longToString(max),
				longToString(min), longToString((long)avg), dev, dev / avg);
		files.forEach(f -> System.out.println(toString(f)));

	}

	static String longToString(long l) {
		double v = l;
		String unit = "B";

		if (v > 1000000) {
			v /= 1000000;
			unit = "M";
		} else if (v > 1000) {
			v /= 1000;
			unit = "K";
		}

		return String.format("%6.2f%s", v, unit);

	}

	static String toString(File f) {
		return String.format("%s: %s", f.getName().split("\\.")[0], longToString(f.length()));
	}

}
