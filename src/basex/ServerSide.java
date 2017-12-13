package basex;

public class ServerSide {

	String prefix_format;
	String suffix_format;
	public String database;
	public String tmpdb;

	/**
	 * 
	 * @param database
	 *            the name of database.
	 * @param optimized
	 */
	public ServerSide(String db) {
		database = db;
		tmpdb = database + "_tmp";

		prefix_format = "xquery let $d := array { db:open('{db}'){prefix} ! db:node-pre(.) } return\n"
				+ "for $i in 0 to {P} - 1 return\n" + "    let $q := array:size($d) idiv {P} return\n"
				+ "    let $r := array:size($d) mod {P} return\n"
				+ "    let $part_length := if ($i < $r) then $q + 1 else $q return\n"
				+ "    let $part_begin  := if ($i <= $r) then ($q + 1) * $i else $q * $i + $r return\n"
				+ "    insert node element part {\n" + "        array:subarray($d, $part_begin + 1, $part_length)  \n" + //
				"    } as last into db:open('{tmpdb}')/root";

		suffix_format = "xquery declare option output:method 'basex';\n" + "declare option output:item-separator '[';\n"
				+ "for $pre in ft:tokenize(db:open-pre('{tmpdb}', {p}*2 + 1)) return  \n"
				+ "  for $node in db:open-pre('{db}', xs:integer($pre)){suffix} \n"
				+ "    return (db:node-pre($node), $node)";
	}

	public String getPrefix(QueryPlan query, int P) {
		String prefix = prefix_format;
		prefix = prefix.replace("{prefix}", query.first());
		prefix = prefix.replace("{db}", database);
		prefix = prefix.replace("{P}", P + "");
		prefix = prefix.replace("{tmpdb}", tmpdb);
		return prefix;
	}

	public String[] getSuffix(QueryPlan query, int P) {
		String[] suffixes = new String[P];
		for (int i = 0; i < P; i++) {
			String suffix_i = suffix_format.replace("{db}", database);
			suffix_i = suffix_i.replace("{p}", (i + 1) + "");
			suffix_i = suffix_i.replace("{suffix}", query.last()); // "/name()");//
			suffix_i = suffix_i.replace("{tmpdb}", tmpdb);
			suffixes[i] = suffix_i;
		}

		return suffixes;
	}

	public String createTempdb() {
		return "set mainmem true; set queryinfo true; drop db " + tmpdb + " ;create db " + tmpdb + " <root></root>";
	}

	public void prepare(BXClient bx) throws Exception {
		bx.execute("drop db " + tmpdb);
		bx.execute("set mainmem true");
		bx.execute("set queryinfo true");
		bx.execute("create db " + tmpdb + " <root></root>"); 
	}
}
