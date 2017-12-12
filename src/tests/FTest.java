package tests;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import basex.common;
import fragmentation.Node;

public class FTest {

	public static void main(String[] args) {
		int[] arr = common.splitArray(12000, 1024);
		for (int i = 1; i < arr.length; i++)
			System.out.printf("/site/country[%d-%d]\n", arr[i - 1] + 1, arr[i]);
		
		int size = 255000;
		List<Node> nodes = new ArrayList<Node>();
		Random rnd = new Random();
		
		for(int i = 0; i < size; i++) {
			Node n = new Node();
			n.mpre = rnd.nextInt();
			n.size = rnd.nextInt();
			nodes.add(n);
		}
		
		double t = 0;
		for(Node n: nodes)
			t += n.mpre + n.size;
		
		
		System.out.println(t);
		
		
		long totalMem = Runtime.getRuntime().totalMemory();
		long  freeMem = Runtime.getRuntime().freeMemory();
		long  maxMem = Runtime.getRuntime().maxMemory();
		
		DecimalFormat df = new DecimalFormat("0.00") ;
		
		common.showMemory();
	}
	

	

}
