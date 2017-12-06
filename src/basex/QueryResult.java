package basex;

public class QueryResult {

	public long exetime;
	public long receiving;
	public String result;
	public String info;

	public QueryResult(long exetime, long receiving, String result) {
		this.exetime = exetime;
		this.receiving = receiving;
		this.result = result;
	}

	public QueryResult() {
	}

	public long totalTime() {
		return exetime + receiving;
	}

	public int resultSize() {
		return result == null ? 0 : result.length();
	}

	public void show() {
		System.out.printf("time: %d ms, size: %d.\n", this.totalTime(), this.resultSize());
	}
}
