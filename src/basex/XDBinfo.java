package basex;

public class XDBinfo {

	public static void main(String[] args) throws Exception {

		for (int i = 0; i < 16; i++) {
			BXClient bx = BXClient.open("172.21.52." + (50 + i));
			String query = "xquery for $node in db:info('xmark600_16_4M') return $node//size/text()  || ',' ||   $node//inputsize/text()";
			String r = bx.execute(query);
			System.out.printf("172.21.52.%d: %s, query=%s\n", i + 50, r, query);
		}
	}

}
