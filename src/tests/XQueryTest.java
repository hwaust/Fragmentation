package tests;

import basex.BXClient;

public class XQueryTest {

	public static void main(String[] args) throws Exception {

			BXClient bx = BXClient.open("localhost"); 
			String r = bx.execute("xquery for $node in db:info('xmark1') return $node//size/text()  || ',' ||   $node//inputsize/text()");
			System.out.println("r = " + r);
 
	}
}
