package basex;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PExecutor implements Runnable {
	public String tag;
	public QueryResult sr;
	public String xquery;
	public BXClient bx;
	public int resultType;
	public static String outputfolder;
	public CountDownLatch latch;

	public static int counter;

	public PExecutor(BXClient bxclient) {
		this.bx = bxclient;
		resultType = 0;
	}

	public PExecutor(BXClient processor, int resultType, String query) {
		this.resultType = resultType;
		this.bx = processor;
		this.xquery = query;
	}

	public PExecutor(String server, String database) throws Exception {
		bx = BXClient.open(server);
	}

	@Override
	public void run() {

		try {
			long time = System.currentTimeMillis();
			switch (resultType) {
			case 0:
				sr = bx.exeQuery(xquery);
				break;
			case 1:
				sr = bx.executeForIntStringArray(xquery);
				break;
			}
			counter++;
			System.out.printf("%d_%s: Execution time = %d ms\n", counter, tag, System.currentTimeMillis() - time);
			common.saveStringtoFile(bx.info, outputfolder + File.separator + tag + ".txt");

		} catch (Exception e) {
			System.out.println("PExecutor.run: result type = " + resultType + ", sr = " + sr);
			e.printStackTrace();
		}

		if (latch != null)
			latch.countDown();
	}

	public void close() throws Exception {
		bx.close();
	}

	public void setQuery(String xquery, int resultType) {
		this.xquery = xquery;
		this.resultType = resultType;
	}

	public static PExecutor[] toArray(PExecutor[][] pess) {
		List<PExecutor> executorList = new ArrayList<PExecutor>();

		for (int i = 0; i < pess.length; i++)
			for (int j = 0; j < pess[i].length; j++)
				executorList.add(pess[i][j]);

		PExecutor[] executors = new PExecutor[executorList.size()];
		for (int i = 0; i < executors.length; i++)
			executors[i] = executorList.get(i);
		return executors;
	}

	public static long parallelRun(PExecutor[] tasks) throws Exception {
		counter = 0;
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

	public static long serialRun(PExecutor[] tasks) throws Exception {
		counter = 0;
		long time = System.currentTimeMillis();
		for (PExecutor task : tasks) {
			task.run();
		}

		return System.currentTimeMillis() - time;

	}
}
