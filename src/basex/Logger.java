package basex;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Logger {

	HashMap<String, ArrayList<Double>> data = new HashMap<String, ArrayList<Double>>();
	ArrayList<String> keys = new ArrayList<String>();

	public String logname;

	public Logger(String name) {
		logname = name;
	}

	public void add(String key, double value) {
		if (!data.keySet().contains(key)) {
			data.put(key, new ArrayList<Double>());
			keys.add(key);
		}
		data.get(key).add(value);
	}

	public void save() throws Exception {
		String folder = common.getFolder("logs");
		new File(folder).mkdirs();
		common.saveStringtoFile(toString(), folder + logname);
		System.out.printf("\nLog data has been save to: %s\n", folder + logname);
	}

	public void save(String folder) throws Exception {
		new File(folder).mkdirs();
		common.saveStringtoFile(toString(), folder + File.separator + logname);
		System.out.printf("\nLog data has been save to: %s\n", folder + File.separator + logname);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String key : keys) {
			sb.append(key + ":");
			ArrayList<Double> vs = data.get(key);
			for (int i = 0; i < vs.size(); i++) {
				sb.append(String.format("%8.0f", vs.get(i)));
				sb.append(i < vs.size() - 1 ? "\t" : "\n");
			}
		}
		return sb.toString();
	}

	// hao: I keep this for testing.
	public void show() {
		System.out.println("====================================");
		for (String key : keys) {
			double d = 0;
			ArrayList<Double> vs = data.get(key);
			for (int i = 0; i < vs.size(); i++)
				d += vs.get(i);
			if (vs.size() > 0)
				d /= vs.size();
			System.out.printf("%s: %8.2f\n", key, d);
		}
		System.out.println("====================================");
	}

}
