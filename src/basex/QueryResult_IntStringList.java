package basex;

import java.util.ArrayList;

public class QueryResult_IntStringList extends QueryResult {

	public ArrayList<Integer> pres = new ArrayList<Integer>();
	public ArrayList<String> values = new ArrayList<String>();
	public int[] newpres;

	/**
	 * The length of this array is the same as the number of fragments.
	 */
	public ArrayList<ArrayList<String>> results;

	public void print() {
		for (int i = 0; i < pres.size(); i++) {
			System.out.println(pres.get(i) + "\n" + values.get(i));
		}
	}

	/**
	 * The length is the same as tree.fragments.size()
	 * 
	 * @param length
	 */
	public void initResults(int length) {
		results = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < length; i++)
			results.add(new ArrayList<String>());
	}

}
