package basex;

public class ServerSide {

	String prefix_format = "xquery let $tmp := db:open('{tmpdb}') let $d := array { db:open('{db}'){prefix} ! db:node-pre(.) } return let $P := {p} return for $i in 0 to $P - 1 return let $q := array:size($d) idiv $P return let $r := array:size($d) mod $P return let $part_length := if ($i < $r) then $q + 1 else $q return  let $part_begin  := if ($i <= $r) then ($q + 1)*$i else $q*$i + $r return  insert node element part { array:subarray($d, $part_begin + 1, $part_length) } as last into $tmp/root";
	String suffix_format = "xquery let $i := {p} return let $part_pre := $i*2 + 1 return for $x in ft:tokenize(db:open-pre('{tmpdb}', $part_pre)) return db:open-pre('{db}', xs:integer($x)){suffix}";

	public String database;
	public String tmpdb;

	/**
	 * 
	 * @param database
	 *            the name of database.
	 * @param optimized
	 */
	public ServerSide(String database) {
		this.database = database;
	}

	public String getPrefix(QueryPlan query, int position, int P) {
		String prefix = prefix_format;
		String xpath = query.first().replace("/site", "/vn/site[" + (position + 1) + "]");
		prefix = prefix.replace("{prefix}", xpath);
		prefix = prefix.replace("{db}", database);
		prefix = prefix.replace("{p}", P + "");
		prefix = prefix.replace("{tmpdb}", tmpdb);

		return prefix;
	}

	public String[] getSuffix(QueryPlan query, int P) {
		String[] suffixes = new String[P];
		for (int i = 0; i < P; i++) {
			String suffix_i = suffix_format.replace("{db}", database);
			suffix_i = suffix_i.replace("{p}", (i + 1) + "");
			suffix_i = suffix_i.replace("{suffix}", query.last()); //"/name()");// 
			suffix_i = suffix_i.replace("{tmpdb}", tmpdb);
			suffixes[i] = suffix_i;
		}

		return suffixes;
	}

	public String createTempdb() {
		return "set mainmem on;drop db " + tmpdb + " ;create db " + tmpdb + " <root></root>";
	}
}
