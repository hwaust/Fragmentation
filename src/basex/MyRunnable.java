package basex;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyRunnable implements Runnable {
	public CountDownLatch latch;

	@Override
	public void run() {

	}

	public static void parallelRun(MyRunnable[] tasks) throws Exception {
		int P = tasks.length;
		ExecutorService executor = Executors.newFixedThreadPool(P);
		CountDownLatch latch = new CountDownLatch(P);
		for (int i = 0; i < P; i++)
			tasks[i].latch = latch;

		for (int i = 0; i < P; i++)
			executor.submit(tasks[i]);

		latch.await();

		executor.shutdown();
	}

	public static void serialRun(MyRunnable[] tasks) throws Exception {
		int P = tasks.length;
		ExecutorService executor = Executors.newFixedThreadPool(P);
		CountDownLatch latch = new CountDownLatch(P);
		for (int i = 0; i < P; i++)
			tasks[i].latch = latch;

		for (int i = 0; i < P; i++)
			executor.submit(tasks[i]);

		latch.await();

		executor.shutdown();
	}

	public static void serialRun(MyRunnable task) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(1); 
		executor.submit(task); 
		executor.shutdown();
	}

}
