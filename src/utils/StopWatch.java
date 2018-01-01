package utils;

import java.util.ArrayList;
import java.util.List;

public class StopWatch { 

	
	public static void main(String[] args) {
		StopWatch sw = new StopWatch();
		sw.start("init");
		System.out.println("hello");
		sw.stop("init");
		
		System.out.println(sw);
	}
	
	List<TimePeriod> times;

	public StopWatch() {
		times = new ArrayList<>();
	}

	public void start(String testName) {
		for (TimePeriod time : times) {
			if (time.name.equals(testName)) {
				time.start(testName);
				return;
			}
		}

		times.add(new TimePeriod(testName));
	}

	public void stop(String testName) {
		for (TimePeriod time : times) {
			if (time.name.equals(testName)) {
				time.stop();
				return;
			}
		}

		System.out.println("Error: " + testName + " does not exist.");
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (TimePeriod time : times)
			sb.append(time.toString() + "\n");
		return sb.toString();
	}

	class TimePeriod {
		public String name;
		public long start;
		public long end;

		public TimePeriod(String name) {
			start(name);
		}

		public void start(String name) {
			this.name = name;
			this.start = System.currentTimeMillis();
		}

		public void stop() {
			end = System.currentTimeMillis();
		}

		public String toString() {
			return String.format("%s: %d ms.", name, end - start);
		}
	}
}
