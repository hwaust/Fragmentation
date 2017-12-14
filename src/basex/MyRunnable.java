package basex;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyRunnable implements Runnable {
	public CountDownLatch latch;

	@Override
	public void run() {

	}

	public static long parallelRun(MyRunnable[] tasks) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(tasks.length);
		CountDownLatch latch = new CountDownLatch(tasks.length);
		for (int i = 0; i < tasks.length; i++)
			tasks[i].latch = latch;

		for (int i = 0; i < tasks.length; i++)
			executor.submit(tasks[i]);
		
		long time = System.currentTimeMillis();
		latch.await();
		time = System.currentTimeMillis() - time;
		
		executor.shutdown();

		return time;
	}

	public static long parallelRun(PExecutor[][] pess) throws Exception {
		List<PExecutor> executorList = new ArrayList<PExecutor>();

		for (int i = 0; i < pess.length; i++)
			for (int j = 0; j < pess[i].length; j++)
				executorList.add(pess[i][j]);

		PExecutor[] executors = new PExecutor[executorList.size()];
		for (int i = 0; i < executors.length; i++)
			executors[i] = executorList.get(i);

		return parallelRun(executors);
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
