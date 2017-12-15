package fragmentation;

import basex.QueryResultPre;

public class InterResult {

	int[] sizes;
	int size = 0;
	QueryResultPre[] results;

	public InterResult(QueryResultPre[] results) {
		setResults(results);
	}

	public int size() {
		return sizes[sizes.length - 1];
	}

	public void setResults(QueryResultPre[] results) {
		this.results = results;

		size = 0;
		for (QueryResultPre re : results) {
			size += re.pres.size();
		}

		sizes = new int[results.length + 1];

		for (int i = 0; i < results.length; i++) {
			sizes[i + 1] = sizes[i] + results[i].size();
		}

		for (int s : sizes)
			System.out.print(s + ", ");
	}

	public void compute(int[] bounds) {
		int pos = 0;
		for (int i = 0; i < bounds.length; i++)
			System.out.println(bounds[i]);

		for (int i = 0; i < results.length; i++) {
			for (int j = 0; j < results[i].size(); j++) {
				int pre = results[i].pres.get(j);
				while (pos < bounds.length - 1 && pre > bounds[pos + 1]) {
					System.out.printf("(%d, %d)\n", i, j);
					pos++;
				}
				System.out.println(pre);
			}

		}
	}

}
