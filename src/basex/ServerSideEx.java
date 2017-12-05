package basex;

public class ServerSideEx {

	String prefix_format = "xquery let $tmp := db:open('{tmpdb}') let $d := array { db:open('{db}'){prefix} ! db:node-pre(.) } return let $P := {p} return for $i in 0 to $P - 1 return let $q := array:size($d) idiv $P return let $r := array:size($d) mod $P return let $part_length := if ($i < $r) then $q + 1 else $q return  let $part_begin  := if ($i <= $r) then ($q + 1)*$i else $q*$i + $r return  insert node element part { array:subarray($d, $part_begin + 1, $part_length) } as last into $tmp/root";
	String suffix_format = "xquery let $i := {p} return let $part_pre := $i*2 + 1 return for $x in ft:tokenize(db:open-pre('{tmpdb}', $part_pre)) return let $node := db:open-pre('{db}', xs:integer($x)){suffix} return (('', db:node-pre($node)), $node)";

	public String database;
	public String tmpdb;

	/**
	 * 
	 * @param database
	 *            the name of database.
	 * @param optimized
	 */
	public ServerSideEx(String database) {
		this.database = database;

		if (tmpdb == null)
			tmpdb = database + "_tmp";
	}

	public String getPrefix(QueryPlan query, int P) {
		String prefix = prefix_format;
		prefix = prefix.replace("{prefix}", query.first());
		prefix = prefix.replace("{db}", database);
		prefix = prefix.replace("{p}", P + "");
		prefix = prefix.replace("{tmpdb}", tmpdb);

		return prefix;
	}

	public String getPrefix(QueryPlan query, int pos, int P) {
		String prefix = prefix_format;
		prefix = prefix.replace("{prefix}", query.first());
		prefix = prefix.replaceAll("{pos}", pos + "");
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
			suffix_i = suffix_i.replace("{suffix}", query.last()); // "/name()");//
			suffix_i = suffix_i.replace("{tmpdb}", tmpdb);
			suffixes[i] = suffix_i;
		}

		return suffixes;
	}

	public String createTempdb() {
		return "set mainmem false;drop db " + tmpdb + " ;create db " + tmpdb + " <root></root>";
	}
}
