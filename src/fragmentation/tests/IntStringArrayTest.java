package fragmentation.tests;

import java.util.ArrayList;

public class IntStringArrayTest {

	public static void main(String[] args) throws Exception {

		String swin = "\r\n3138\r\n<name>recover meat</name>\r\n\r\n3167\r\n<name>valour</name>\r\n\r\n3201\r\n<name>back</name>";
		// String slinux = "\n3138\n<name>recover meat</name>\n\n3167\n<name>valour</name>\n\n3201\n<name>back</name>";

		swin = "\r\n" + "3138\r\n" + "<name>recover meat</name>\r\n" + "\r\n" + "3140\r\n"
				+ "<payment>Money order, Cash</payment>\r\n" + "\r\n" + "3142\r\n" + "<description>\r\n"
				+ "  <parlist>\r\n" + "    <listitem>\r\n"
				+ "      <text>eros hard crowned empty grant<bold>grant kneaded<emph>trade pleas milk</emph>remorse pow paul gone</bold>mad above dried scope brand year water enlarge sending chastity healthful sciaticas tents</text>\r\n"
				+ "    </listitem>\r\n" + "    <listitem>\r\n" + "      <text>knavery insatiate buckingham</text>\r\n"
				+ "    </listitem>\r\n" + "  </parlist>\r\n" + "</description>\r\n" + "\r\n" + "3143\r\n"
				+ "<parlist>\r\n" + "  <listitem>\r\n"
				+ "    <text>eros hard crowned empty grant<bold>grant kneaded<emph>trade pleas milk</emph>remorse pow paul gone</bold>mad above dried scope brand year water enlarge sending chastity healthful sciaticas tents</text>\r\n"
				+ "  </listitem>\r\n" + "  <listitem>\r\n" + "    <text>knavery insatiate buckingham</text>\r\n"
				+ "  </listitem>\r\n" + "</parlist>";

		System.out.println("Original String ------------\n" + swin);

		System.out.println("\non Windows ---------------");
		testStringWin(swin.getBytes());

		// System.out.println("\non Linux ---------------");
		// convertLinux(slinux.getBytes());
	}

	static void convertLinux(byte[] bts) {
		ArrayList<Integer> its = new ArrayList<Integer>();
		ArrayList<String> strs = new ArrayList<String>();
		boolean entered = true;
		StringBuilder sb = new StringBuilder();
		int value = 0;

		for (int i = 0; i < bts.length; i++) {
			if (bts[i] == '\n') {
				if (entered) {
					strs.add(sb.toString());
					sb = new StringBuilder();
					entered = false;

					while (bts[++i] != '\n') {
						value *= 10;
						value += bts[i] - '0';
					}
					its.add(value);
					value = 0;
				} else
					entered = true;
			} else {
				entered = false;
				sb.append((char) bts[i]);
			}
		}

		if (sb.length() > 0)
			strs.add(sb.toString());

		for (int i : its)
			System.out.println(i);

		for (String s : strs)
			System.out.println(s);
	}

	static void testStringWin(byte[] bts) {
		ArrayList<Integer> its = new ArrayList<Integer>();
		ArrayList<String> strs = new ArrayList<String>();
		boolean entered = true;
		StringBuilder sb = new StringBuilder();
		int value = 0;

		for (int i = 0; i < bts.length; i++) {
			if (bts[i] == '\r') {
				i++;

				if (entered) {
					strs.add(sb.toString());
					sb = new StringBuilder();
					entered = false;

					while (bts[++i] != '\r') {
						value *= 10;
						value += bts[i] - '0';
					}
					its.add(value);
					value = 0;
					i++;
				} else
					entered = true;

			} else {
				entered = false;
				sb.append((char) bts[i]);
			}
		}

		if (sb.length() > 0)
			strs.add(sb.toString());

		for (int i : its)
			System.out.println(i);

		for (String s : strs)
			System.out.println(s);
	}

}
