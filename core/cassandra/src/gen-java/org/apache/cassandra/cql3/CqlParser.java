// $ANTLR 3.5.2 /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g 2016-12-25 20:10:14

    package org.apache.cassandra.cql3;

    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.Collections;
    import java.util.EnumSet;
    import java.util.HashSet;
    import java.util.HashMap;
    import java.util.LinkedHashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.Set;

    import org.apache.cassandra.auth.*;
    import org.apache.cassandra.cql3.*;
    import org.apache.cassandra.cql3.restrictions.CustomIndexExpression;
    import org.apache.cassandra.cql3.statements.*;
    import org.apache.cassandra.cql3.selection.*;
    import org.apache.cassandra.cql3.functions.*;
    import org.apache.cassandra.db.marshal.CollectionType;
    import org.apache.cassandra.exceptions.ConfigurationException;
    import org.apache.cassandra.exceptions.InvalidRequestException;
    import org.apache.cassandra.exceptions.SyntaxException;
    import org.apache.cassandra.utils.Pair;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class CqlParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "A", "B", "BOOLEAN", "C", "COMMENT", 
		"D", "DIGIT", "E", "EXPONENT", "F", "FLOAT", "G", "H", "HEX", "HEXNUMBER", 
		"I", "IDENT", "INTEGER", "J", "K", "K_ADD", "K_AGGREGATE", "K_ALL", "K_ALLOW", 
		"K_ALTER", "K_AND", "K_APPLY", "K_AS", "K_ASC", "K_ASCII", "K_AUTHORIZE", 
		"K_BATCH", "K_BEGIN", "K_BIGINT", "K_BLOB", "K_BOOLEAN", "K_BY", "K_CALLED", 
		"K_CLUSTERING", "K_COLUMNFAMILY", "K_COMPACT", "K_CONTAINS", "K_COUNT", 
		"K_COUNTER", "K_CREATE", "K_CUSTOM", "K_DATE", "K_DECIMAL", "K_DELETE", 
		"K_DESC", "K_DESCRIBE", "K_DISTINCT", "K_DOUBLE", "K_DROP", "K_ENTRIES", 
		"K_EXECUTE", "K_EXISTS", "K_FILTERING", "K_FINALFUNC", "K_FLOAT", "K_FROM", 
		"K_FROZEN", "K_FULL", "K_FUNCTION", "K_FUNCTIONS", "K_GRANT", "K_IF", 
		"K_IN", "K_INDEX", "K_INET", "K_INFINITY", "K_INITCOND", "K_INPUT", "K_INSERT", 
		"K_INT", "K_INTO", "K_IS", "K_JSON", "K_KEY", "K_KEYS", "K_KEYSPACE", 
		"K_KEYSPACES", "K_LANGUAGE", "K_LIMIT", "K_LIST", "K_LOGIN", "K_MAP", 
		"K_MATERIALIZED", "K_MODIFY", "K_NAN", "K_NOLOGIN", "K_NORECURSIVE", "K_NOSUPERUSER", 
		"K_NOT", "K_NULL", "K_OF", "K_ON", "K_OPTIONS", "K_OR", "K_ORDER", "K_PASSWORD", 
		"K_PERMISSION", "K_PERMISSIONS", "K_PRIMARY", "K_RENAME", "K_REPLACE", 
		"K_RETURNS", "K_REVOKE", "K_ROLE", "K_ROLES", "K_SELECT", "K_SET", "K_SFUNC", 
		"K_SMALLINT", "K_STATIC", "K_STORAGE", "K_STYPE", "K_SUPERUSER", "K_TEXT", 
		"K_TIME", "K_TIMESTAMP", "K_TIMEUUID", "K_TINYINT", "K_TO", "K_TOKEN", 
		"K_TRIGGER", "K_TRUNCATE", "K_TTL", "K_TUPLE", "K_TYPE", "K_UNLOGGED", 
		"K_UPDATE", "K_USE", "K_USER", "K_USERS", "K_USING", "K_UUID", "K_VALUES", 
		"K_VARCHAR", "K_VARINT", "K_VIEW", "K_WHERE", "K_WITH", "K_WRITETIME", 
		"L", "LETTER", "M", "MULTILINE_COMMENT", "N", "O", "P", "Q", "QMARK", 
		"QUOTED_NAME", "R", "S", "STRING_LITERAL", "T", "U", "UUID", "V", "W", 
		"WS", "X", "Y", "Z", "'!='", "'('", "')'", "'+'", "','", "'-'", "'.'", 
		"':'", "';'", "'<'", "'<='", "'='", "'>'", "'>='", "'['", "'\\*'", "']'", 
		"'expr('", "'{'", "'}'"
	};
	public static final int EOF=-1;
	public static final int T__170=170;
	public static final int T__171=171;
	public static final int T__172=172;
	public static final int T__173=173;
	public static final int T__174=174;
	public static final int T__175=175;
	public static final int T__176=176;
	public static final int T__177=177;
	public static final int T__178=178;
	public static final int T__179=179;
	public static final int T__180=180;
	public static final int T__181=181;
	public static final int T__182=182;
	public static final int T__183=183;
	public static final int T__184=184;
	public static final int T__185=185;
	public static final int T__186=186;
	public static final int T__187=187;
	public static final int T__188=188;
	public static final int T__189=189;
	public static final int A=4;
	public static final int B=5;
	public static final int BOOLEAN=6;
	public static final int C=7;
	public static final int COMMENT=8;
	public static final int D=9;
	public static final int DIGIT=10;
	public static final int E=11;
	public static final int EXPONENT=12;
	public static final int F=13;
	public static final int FLOAT=14;
	public static final int G=15;
	public static final int H=16;
	public static final int HEX=17;
	public static final int HEXNUMBER=18;
	public static final int I=19;
	public static final int IDENT=20;
	public static final int INTEGER=21;
	public static final int J=22;
	public static final int K=23;
	public static final int K_ADD=24;
	public static final int K_AGGREGATE=25;
	public static final int K_ALL=26;
	public static final int K_ALLOW=27;
	public static final int K_ALTER=28;
	public static final int K_AND=29;
	public static final int K_APPLY=30;
	public static final int K_AS=31;
	public static final int K_ASC=32;
	public static final int K_ASCII=33;
	public static final int K_AUTHORIZE=34;
	public static final int K_BATCH=35;
	public static final int K_BEGIN=36;
	public static final int K_BIGINT=37;
	public static final int K_BLOB=38;
	public static final int K_BOOLEAN=39;
	public static final int K_BY=40;
	public static final int K_CALLED=41;
	public static final int K_CLUSTERING=42;
	public static final int K_COLUMNFAMILY=43;
	public static final int K_COMPACT=44;
	public static final int K_CONTAINS=45;
	public static final int K_COUNT=46;
	public static final int K_COUNTER=47;
	public static final int K_CREATE=48;
	public static final int K_CUSTOM=49;
	public static final int K_DATE=50;
	public static final int K_DECIMAL=51;
	public static final int K_DELETE=52;
	public static final int K_DESC=53;
	public static final int K_DESCRIBE=54;
	public static final int K_DISTINCT=55;
	public static final int K_DOUBLE=56;
	public static final int K_DROP=57;
	public static final int K_ENTRIES=58;
	public static final int K_EXECUTE=59;
	public static final int K_EXISTS=60;
	public static final int K_FILTERING=61;
	public static final int K_FINALFUNC=62;
	public static final int K_FLOAT=63;
	public static final int K_FROM=64;
	public static final int K_FROZEN=65;
	public static final int K_FULL=66;
	public static final int K_FUNCTION=67;
	public static final int K_FUNCTIONS=68;
	public static final int K_GRANT=69;
	public static final int K_IF=70;
	public static final int K_IN=71;
	public static final int K_INDEX=72;
	public static final int K_INET=73;
	public static final int K_INFINITY=74;
	public static final int K_INITCOND=75;
	public static final int K_INPUT=76;
	public static final int K_INSERT=77;
	public static final int K_INT=78;
	public static final int K_INTO=79;
	public static final int K_IS=80;
	public static final int K_JSON=81;
	public static final int K_KEY=82;
	public static final int K_KEYS=83;
	public static final int K_KEYSPACE=84;
	public static final int K_KEYSPACES=85;
	public static final int K_LANGUAGE=86;
	public static final int K_LIMIT=87;
	public static final int K_LIST=88;
	public static final int K_LOGIN=89;
	public static final int K_MAP=90;
	public static final int K_MATERIALIZED=91;
	public static final int K_MODIFY=92;
	public static final int K_NAN=93;
	public static final int K_NOLOGIN=94;
	public static final int K_NORECURSIVE=95;
	public static final int K_NOSUPERUSER=96;
	public static final int K_NOT=97;
	public static final int K_NULL=98;
	public static final int K_OF=99;
	public static final int K_ON=100;
	public static final int K_OPTIONS=101;
	public static final int K_OR=102;
	public static final int K_ORDER=103;
	public static final int K_PASSWORD=104;
	public static final int K_PERMISSION=105;
	public static final int K_PERMISSIONS=106;
	public static final int K_PRIMARY=107;
	public static final int K_RENAME=108;
	public static final int K_REPLACE=109;
	public static final int K_RETURNS=110;
	public static final int K_REVOKE=111;
	public static final int K_ROLE=112;
	public static final int K_ROLES=113;
	public static final int K_SELECT=114;
	public static final int K_SET=115;
	public static final int K_SFUNC=116;
	public static final int K_SMALLINT=117;
	public static final int K_STATIC=118;
	public static final int K_STORAGE=119;
	public static final int K_STYPE=120;
	public static final int K_SUPERUSER=121;
	public static final int K_TEXT=122;
	public static final int K_TIME=123;
	public static final int K_TIMESTAMP=124;
	public static final int K_TIMEUUID=125;
	public static final int K_TINYINT=126;
	public static final int K_TO=127;
	public static final int K_TOKEN=128;
	public static final int K_TRIGGER=129;
	public static final int K_TRUNCATE=130;
	public static final int K_TTL=131;
	public static final int K_TUPLE=132;
	public static final int K_TYPE=133;
	public static final int K_UNLOGGED=134;
	public static final int K_UPDATE=135;
	public static final int K_USE=136;
	public static final int K_USER=137;
	public static final int K_USERS=138;
	public static final int K_USING=139;
	public static final int K_UUID=140;
	public static final int K_VALUES=141;
	public static final int K_VARCHAR=142;
	public static final int K_VARINT=143;
	public static final int K_VIEW=144;
	public static final int K_WHERE=145;
	public static final int K_WITH=146;
	public static final int K_WRITETIME=147;
	public static final int L=148;
	public static final int LETTER=149;
	public static final int M=150;
	public static final int MULTILINE_COMMENT=151;
	public static final int N=152;
	public static final int O=153;
	public static final int P=154;
	public static final int Q=155;
	public static final int QMARK=156;
	public static final int QUOTED_NAME=157;
	public static final int R=158;
	public static final int S=159;
	public static final int STRING_LITERAL=160;
	public static final int T=161;
	public static final int U=162;
	public static final int UUID=163;
	public static final int V=164;
	public static final int W=165;
	public static final int WS=166;
	public static final int X=167;
	public static final int Y=168;
	public static final int Z=169;

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public CqlParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public CqlParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	@Override public String[] getTokenNames() { return CqlParser.tokenNames; }
	@Override public String getGrammarFileName() { return "/Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g"; }


	    private final List<ErrorListener> listeners = new ArrayList<ErrorListener>();
	    private final List<ColumnIdentifier> bindVariables = new ArrayList<ColumnIdentifier>();

	    public static final Set<String> reservedTypeNames = new HashSet<String>()
	    {{
	        add("byte");
	        add("complex");
	        add("enum");
	        add("date");
	        add("interval");
	        add("macaddr");
	        add("bitstring");
	    }};

	    public AbstractMarker.Raw newBindVariables(ColumnIdentifier name)
	    {
	        AbstractMarker.Raw marker = new AbstractMarker.Raw(bindVariables.size());
	        bindVariables.add(name);
	        return marker;
	    }

	    public AbstractMarker.INRaw newINBindVariables(ColumnIdentifier name)
	    {
	        AbstractMarker.INRaw marker = new AbstractMarker.INRaw(bindVariables.size());
	        bindVariables.add(name);
	        return marker;
	    }

	    public Tuples.Raw newTupleBindVariables(ColumnIdentifier name)
	    {
	        Tuples.Raw marker = new Tuples.Raw(bindVariables.size());
	        bindVariables.add(name);
	        return marker;
	    }

	    public Tuples.INRaw newTupleINBindVariables(ColumnIdentifier name)
	    {
	        Tuples.INRaw marker = new Tuples.INRaw(bindVariables.size());
	        bindVariables.add(name);
	        return marker;
	    }

	    public Json.Marker newJsonBindVariables(ColumnIdentifier name)
	    {
	        Json.Marker marker = new Json.Marker(bindVariables.size());
	        bindVariables.add(name);
	        return marker;
	    }

	    public void addErrorListener(ErrorListener listener)
	    {
	        this.listeners.add(listener);
	    }

	    public void removeErrorListener(ErrorListener listener)
	    {
	        this.listeners.remove(listener);
	    }

	    public void displayRecognitionError(String[] tokenNames, RecognitionException e)
	    {
	        for (int i = 0, m = listeners.size(); i < m; i++)
	            listeners.get(i).syntaxError(this, tokenNames, e);
	    }

	    private void addRecognitionError(String msg)
	    {
	        for (int i = 0, m = listeners.size(); i < m; i++)
	            listeners.get(i).syntaxError(this, msg);
	    }

	    public Map<String, String> convertPropertyMap(Maps.Literal map)
	    {
	        if (map == null || map.entries == null || map.entries.isEmpty())
	            return Collections.<String, String>emptyMap();

	        Map<String, String> res = new HashMap<String, String>(map.entries.size());

	        for (Pair<Term.Raw, Term.Raw> entry : map.entries)
	        {
	            // Because the parser tries to be smart and recover on error (to
	            // allow displaying more than one error I suppose), we have null
	            // entries in there. Just skip those, a proper error will be thrown in the end.
	            if (entry.left == null || entry.right == null)
	                break;

	            if (!(entry.left instanceof Constants.Literal))
	            {
	                String msg = "Invalid property name: " + entry.left;
	                if (entry.left instanceof AbstractMarker.Raw)
	                    msg += " (bind variables are not supported in DDL queries)";
	                addRecognitionError(msg);
	                break;
	            }
	            if (!(entry.right instanceof Constants.Literal))
	            {
	                String msg = "Invalid property value: " + entry.right + " for property: " + entry.left;
	                if (entry.right instanceof AbstractMarker.Raw)
	                    msg += " (bind variables are not supported in DDL queries)";
	                addRecognitionError(msg);
	                break;
	            }

	            res.put(((Constants.Literal)entry.left).getRawText(), ((Constants.Literal)entry.right).getRawText());
	        }

	        return res;
	    }

	    public void addRawUpdate(List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations, ColumnIdentifier.Raw key, Operation.RawUpdate update)
	    {
	        for (Pair<ColumnIdentifier.Raw, Operation.RawUpdate> p : operations)
	        {
	            if (p.left.equals(key) && !p.right.isCompatibleWith(update))
	                addRecognitionError("Multiple incompatible setting of column " + key);
	        }
	        operations.add(Pair.create(key, update));
	    }

	    public Set<Permission> filterPermissions(Set<Permission> permissions, IResource resource)
	    {
	        if (resource == null)
	            return Collections.emptySet();
	        Set<Permission> filtered = new HashSet<>(permissions);
	        filtered.retainAll(resource.applicablePermissions());
	        if (filtered.isEmpty())
	            addRecognitionError("Resource type " + resource.getClass().getSimpleName() +
	                                    " does not support any of the requested permissions");

	        return filtered;
	    }



	// $ANTLR start "query"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:231:1: query returns [ParsedStatement stmnt] : st= cqlStatement ( ';' )* EOF ;
	public final ParsedStatement query() throws RecognitionException {
		ParsedStatement stmnt = null;


		ParsedStatement st =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:232:5: (st= cqlStatement ( ';' )* EOF )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:232:7: st= cqlStatement ( ';' )* EOF
			{
			pushFollow(FOLLOW_cqlStatement_in_query72);
			st=cqlStatement();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:232:23: ( ';' )*
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==178) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:232:24: ';'
					{
					match(input,178,FOLLOW_178_in_query75); 
					}
					break;

				default :
					break loop1;
				}
			}

			match(input,EOF,FOLLOW_EOF_in_query79); 
			 stmnt = st; 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return stmnt;
	}
	// $ANTLR end "query"



	// $ANTLR start "cqlStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:235:1: cqlStatement returns [ParsedStatement stmt] : (st1= selectStatement |st2= insertStatement |st3= updateStatement |st4= batchStatement |st5= deleteStatement |st6= useStatement |st7= truncateStatement |st8= createKeyspaceStatement |st9= createTableStatement |st10= createIndexStatement |st11= dropKeyspaceStatement |st12= dropTableStatement |st13= dropIndexStatement |st14= alterTableStatement |st15= alterKeyspaceStatement |st16= grantPermissionsStatement |st17= revokePermissionsStatement |st18= listPermissionsStatement |st19= createUserStatement |st20= alterUserStatement |st21= dropUserStatement |st22= listUsersStatement |st23= createTriggerStatement |st24= dropTriggerStatement |st25= createTypeStatement |st26= alterTypeStatement |st27= dropTypeStatement |st28= createFunctionStatement |st29= dropFunctionStatement |st30= createAggregateStatement |st31= dropAggregateStatement |st32= createRoleStatement |st33= alterRoleStatement |st34= dropRoleStatement |st35= listRolesStatement |st36= grantRoleStatement |st37= revokeRoleStatement |st38= createMaterializedViewStatement |st39= dropMaterializedViewStatement |st40= alterMaterializedViewStatement );
	public final ParsedStatement cqlStatement() throws RecognitionException {
		ParsedStatement stmt = null;


		SelectStatement.RawStatement st1 =null;
		ModificationStatement.Parsed st2 =null;
		UpdateStatement.ParsedUpdate st3 =null;
		BatchStatement.Parsed st4 =null;
		DeleteStatement.Parsed st5 =null;
		UseStatement st6 =null;
		TruncateStatement st7 =null;
		CreateKeyspaceStatement st8 =null;
		CreateTableStatement.RawStatement st9 =null;
		CreateIndexStatement st10 =null;
		DropKeyspaceStatement st11 =null;
		DropTableStatement st12 =null;
		DropIndexStatement st13 =null;
		AlterTableStatement st14 =null;
		AlterKeyspaceStatement st15 =null;
		GrantPermissionsStatement st16 =null;
		RevokePermissionsStatement st17 =null;
		ListPermissionsStatement st18 =null;
		CreateRoleStatement st19 =null;
		AlterRoleStatement st20 =null;
		DropRoleStatement st21 =null;
		ListRolesStatement st22 =null;
		CreateTriggerStatement st23 =null;
		DropTriggerStatement st24 =null;
		CreateTypeStatement st25 =null;
		AlterTypeStatement st26 =null;
		DropTypeStatement st27 =null;
		CreateFunctionStatement st28 =null;
		DropFunctionStatement st29 =null;
		CreateAggregateStatement st30 =null;
		DropAggregateStatement st31 =null;
		CreateRoleStatement st32 =null;
		AlterRoleStatement st33 =null;
		DropRoleStatement st34 =null;
		ListRolesStatement st35 =null;
		GrantRoleStatement st36 =null;
		RevokeRoleStatement st37 =null;
		CreateViewStatement st38 =null;
		DropViewStatement st39 =null;
		AlterViewStatement st40 =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:237:5: (st1= selectStatement |st2= insertStatement |st3= updateStatement |st4= batchStatement |st5= deleteStatement |st6= useStatement |st7= truncateStatement |st8= createKeyspaceStatement |st9= createTableStatement |st10= createIndexStatement |st11= dropKeyspaceStatement |st12= dropTableStatement |st13= dropIndexStatement |st14= alterTableStatement |st15= alterKeyspaceStatement |st16= grantPermissionsStatement |st17= revokePermissionsStatement |st18= listPermissionsStatement |st19= createUserStatement |st20= alterUserStatement |st21= dropUserStatement |st22= listUsersStatement |st23= createTriggerStatement |st24= dropTriggerStatement |st25= createTypeStatement |st26= alterTypeStatement |st27= dropTypeStatement |st28= createFunctionStatement |st29= dropFunctionStatement |st30= createAggregateStatement |st31= dropAggregateStatement |st32= createRoleStatement |st33= alterRoleStatement |st34= dropRoleStatement |st35= listRolesStatement |st36= grantRoleStatement |st37= revokeRoleStatement |st38= createMaterializedViewStatement |st39= dropMaterializedViewStatement |st40= alterMaterializedViewStatement )
			int alt2=40;
			alt2 = dfa2.predict(input);
			switch (alt2) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:237:7: st1= selectStatement
					{
					pushFollow(FOLLOW_selectStatement_in_cqlStatement113);
					st1=selectStatement();
					state._fsp--;

					 stmt = st1; 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:238:7: st2= insertStatement
					{
					pushFollow(FOLLOW_insertStatement_in_cqlStatement142);
					st2=insertStatement();
					state._fsp--;

					 stmt = st2; 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:239:7: st3= updateStatement
					{
					pushFollow(FOLLOW_updateStatement_in_cqlStatement171);
					st3=updateStatement();
					state._fsp--;

					 stmt = st3; 
					}
					break;
				case 4 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:240:7: st4= batchStatement
					{
					pushFollow(FOLLOW_batchStatement_in_cqlStatement200);
					st4=batchStatement();
					state._fsp--;

					 stmt = st4; 
					}
					break;
				case 5 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:241:7: st5= deleteStatement
					{
					pushFollow(FOLLOW_deleteStatement_in_cqlStatement230);
					st5=deleteStatement();
					state._fsp--;

					 stmt = st5; 
					}
					break;
				case 6 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:242:7: st6= useStatement
					{
					pushFollow(FOLLOW_useStatement_in_cqlStatement259);
					st6=useStatement();
					state._fsp--;

					 stmt = st6; 
					}
					break;
				case 7 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:243:7: st7= truncateStatement
					{
					pushFollow(FOLLOW_truncateStatement_in_cqlStatement291);
					st7=truncateStatement();
					state._fsp--;

					 stmt = st7; 
					}
					break;
				case 8 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:244:7: st8= createKeyspaceStatement
					{
					pushFollow(FOLLOW_createKeyspaceStatement_in_cqlStatement318);
					st8=createKeyspaceStatement();
					state._fsp--;

					 stmt = st8; 
					}
					break;
				case 9 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:245:7: st9= createTableStatement
					{
					pushFollow(FOLLOW_createTableStatement_in_cqlStatement339);
					st9=createTableStatement();
					state._fsp--;

					 stmt = st9; 
					}
					break;
				case 10 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:246:7: st10= createIndexStatement
					{
					pushFollow(FOLLOW_createIndexStatement_in_cqlStatement362);
					st10=createIndexStatement();
					state._fsp--;

					 stmt = st10; 
					}
					break;
				case 11 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:247:7: st11= dropKeyspaceStatement
					{
					pushFollow(FOLLOW_dropKeyspaceStatement_in_cqlStatement385);
					st11=dropKeyspaceStatement();
					state._fsp--;

					 stmt = st11; 
					}
					break;
				case 12 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:248:7: st12= dropTableStatement
					{
					pushFollow(FOLLOW_dropTableStatement_in_cqlStatement407);
					st12=dropTableStatement();
					state._fsp--;

					 stmt = st12; 
					}
					break;
				case 13 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:249:7: st13= dropIndexStatement
					{
					pushFollow(FOLLOW_dropIndexStatement_in_cqlStatement432);
					st13=dropIndexStatement();
					state._fsp--;

					 stmt = st13; 
					}
					break;
				case 14 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:250:7: st14= alterTableStatement
					{
					pushFollow(FOLLOW_alterTableStatement_in_cqlStatement457);
					st14=alterTableStatement();
					state._fsp--;

					 stmt = st14; 
					}
					break;
				case 15 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:251:7: st15= alterKeyspaceStatement
					{
					pushFollow(FOLLOW_alterKeyspaceStatement_in_cqlStatement481);
					st15=alterKeyspaceStatement();
					state._fsp--;

					 stmt = st15; 
					}
					break;
				case 16 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:252:7: st16= grantPermissionsStatement
					{
					pushFollow(FOLLOW_grantPermissionsStatement_in_cqlStatement502);
					st16=grantPermissionsStatement();
					state._fsp--;

					 stmt = st16; 
					}
					break;
				case 17 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:253:7: st17= revokePermissionsStatement
					{
					pushFollow(FOLLOW_revokePermissionsStatement_in_cqlStatement520);
					st17=revokePermissionsStatement();
					state._fsp--;

					 stmt = st17; 
					}
					break;
				case 18 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:254:7: st18= listPermissionsStatement
					{
					pushFollow(FOLLOW_listPermissionsStatement_in_cqlStatement537);
					st18=listPermissionsStatement();
					state._fsp--;

					 stmt = st18; 
					}
					break;
				case 19 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:255:7: st19= createUserStatement
					{
					pushFollow(FOLLOW_createUserStatement_in_cqlStatement556);
					st19=createUserStatement();
					state._fsp--;

					 stmt = st19; 
					}
					break;
				case 20 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:256:7: st20= alterUserStatement
					{
					pushFollow(FOLLOW_alterUserStatement_in_cqlStatement580);
					st20=alterUserStatement();
					state._fsp--;

					 stmt = st20; 
					}
					break;
				case 21 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:257:7: st21= dropUserStatement
					{
					pushFollow(FOLLOW_dropUserStatement_in_cqlStatement605);
					st21=dropUserStatement();
					state._fsp--;

					 stmt = st21; 
					}
					break;
				case 22 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:258:7: st22= listUsersStatement
					{
					pushFollow(FOLLOW_listUsersStatement_in_cqlStatement631);
					st22=listUsersStatement();
					state._fsp--;

					 stmt = st22; 
					}
					break;
				case 23 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:259:7: st23= createTriggerStatement
					{
					pushFollow(FOLLOW_createTriggerStatement_in_cqlStatement656);
					st23=createTriggerStatement();
					state._fsp--;

					 stmt = st23; 
					}
					break;
				case 24 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:260:7: st24= dropTriggerStatement
					{
					pushFollow(FOLLOW_dropTriggerStatement_in_cqlStatement677);
					st24=dropTriggerStatement();
					state._fsp--;

					 stmt = st24; 
					}
					break;
				case 25 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:261:7: st25= createTypeStatement
					{
					pushFollow(FOLLOW_createTypeStatement_in_cqlStatement700);
					st25=createTypeStatement();
					state._fsp--;

					 stmt = st25; 
					}
					break;
				case 26 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:262:7: st26= alterTypeStatement
					{
					pushFollow(FOLLOW_alterTypeStatement_in_cqlStatement724);
					st26=alterTypeStatement();
					state._fsp--;

					 stmt = st26; 
					}
					break;
				case 27 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:263:7: st27= dropTypeStatement
					{
					pushFollow(FOLLOW_dropTypeStatement_in_cqlStatement749);
					st27=dropTypeStatement();
					state._fsp--;

					 stmt = st27; 
					}
					break;
				case 28 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:264:7: st28= createFunctionStatement
					{
					pushFollow(FOLLOW_createFunctionStatement_in_cqlStatement775);
					st28=createFunctionStatement();
					state._fsp--;

					 stmt = st28; 
					}
					break;
				case 29 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:265:7: st29= dropFunctionStatement
					{
					pushFollow(FOLLOW_dropFunctionStatement_in_cqlStatement795);
					st29=dropFunctionStatement();
					state._fsp--;

					 stmt = st29; 
					}
					break;
				case 30 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:266:7: st30= createAggregateStatement
					{
					pushFollow(FOLLOW_createAggregateStatement_in_cqlStatement817);
					st30=createAggregateStatement();
					state._fsp--;

					 stmt = st30; 
					}
					break;
				case 31 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:267:7: st31= dropAggregateStatement
					{
					pushFollow(FOLLOW_dropAggregateStatement_in_cqlStatement836);
					st31=dropAggregateStatement();
					state._fsp--;

					 stmt = st31; 
					}
					break;
				case 32 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:268:7: st32= createRoleStatement
					{
					pushFollow(FOLLOW_createRoleStatement_in_cqlStatement857);
					st32=createRoleStatement();
					state._fsp--;

					 stmt = st32; 
					}
					break;
				case 33 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:269:7: st33= alterRoleStatement
					{
					pushFollow(FOLLOW_alterRoleStatement_in_cqlStatement881);
					st33=alterRoleStatement();
					state._fsp--;

					 stmt = st33; 
					}
					break;
				case 34 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:270:7: st34= dropRoleStatement
					{
					pushFollow(FOLLOW_dropRoleStatement_in_cqlStatement906);
					st34=dropRoleStatement();
					state._fsp--;

					 stmt = st34; 
					}
					break;
				case 35 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:271:7: st35= listRolesStatement
					{
					pushFollow(FOLLOW_listRolesStatement_in_cqlStatement932);
					st35=listRolesStatement();
					state._fsp--;

					 stmt = st35; 
					}
					break;
				case 36 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:272:7: st36= grantRoleStatement
					{
					pushFollow(FOLLOW_grantRoleStatement_in_cqlStatement957);
					st36=grantRoleStatement();
					state._fsp--;

					 stmt = st36; 
					}
					break;
				case 37 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:273:7: st37= revokeRoleStatement
					{
					pushFollow(FOLLOW_revokeRoleStatement_in_cqlStatement982);
					st37=revokeRoleStatement();
					state._fsp--;

					 stmt = st37; 
					}
					break;
				case 38 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:274:7: st38= createMaterializedViewStatement
					{
					pushFollow(FOLLOW_createMaterializedViewStatement_in_cqlStatement1006);
					st38=createMaterializedViewStatement();
					state._fsp--;

					 stmt = st38; 
					}
					break;
				case 39 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:275:7: st39= dropMaterializedViewStatement
					{
					pushFollow(FOLLOW_dropMaterializedViewStatement_in_cqlStatement1018);
					st39=dropMaterializedViewStatement();
					state._fsp--;

					 stmt = st39; 
					}
					break;
				case 40 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:276:7: st40= alterMaterializedViewStatement
					{
					pushFollow(FOLLOW_alterMaterializedViewStatement_in_cqlStatement1032);
					st40=alterMaterializedViewStatement();
					state._fsp--;

					 stmt = st40; 
					}
					break;

			}
			 if (stmt != null) stmt.setBoundVariables(bindVariables); 
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return stmt;
	}
	// $ANTLR end "cqlStatement"



	// $ANTLR start "useStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:282:1: useStatement returns [UseStatement stmt] : K_USE ks= keyspaceName ;
	public final UseStatement useStatement() throws RecognitionException {
		UseStatement stmt = null;


		String ks =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:283:5: ( K_USE ks= keyspaceName )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:283:7: K_USE ks= keyspaceName
			{
			match(input,K_USE,FOLLOW_K_USE_in_useStatement1058); 
			pushFollow(FOLLOW_keyspaceName_in_useStatement1062);
			ks=keyspaceName();
			state._fsp--;

			 stmt = new UseStatement(ks); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return stmt;
	}
	// $ANTLR end "useStatement"



	// $ANTLR start "selectStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:292:1: selectStatement returns [SelectStatement.RawStatement expr] : K_SELECT ( K_JSON )? ( ( K_DISTINCT )? sclause= selectClause ) K_FROM cf= columnFamilyName ( K_WHERE wclause= whereClause )? ( K_ORDER K_BY orderByClause[orderings] ( ',' orderByClause[orderings] )* )? ( K_LIMIT rows= intValue )? ( K_ALLOW K_FILTERING )? ;
	public final SelectStatement.RawStatement selectStatement() throws RecognitionException {
		SelectStatement.RawStatement expr = null;


		List<RawSelector> sclause =null;
		CFName cf =null;
		WhereClause.Builder wclause =null;
		Term.Raw rows =null;


		        boolean isDistinct = false;
		        Term.Raw limit = null;
		        Map<ColumnIdentifier.Raw, Boolean> orderings = new LinkedHashMap<ColumnIdentifier.Raw, Boolean>();
		        boolean allowFiltering = false;
		        boolean isJson = false;
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:300:5: ( K_SELECT ( K_JSON )? ( ( K_DISTINCT )? sclause= selectClause ) K_FROM cf= columnFamilyName ( K_WHERE wclause= whereClause )? ( K_ORDER K_BY orderByClause[orderings] ( ',' orderByClause[orderings] )* )? ( K_LIMIT rows= intValue )? ( K_ALLOW K_FILTERING )? )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:300:7: K_SELECT ( K_JSON )? ( ( K_DISTINCT )? sclause= selectClause ) K_FROM cf= columnFamilyName ( K_WHERE wclause= whereClause )? ( K_ORDER K_BY orderByClause[orderings] ( ',' orderByClause[orderings] )* )? ( K_LIMIT rows= intValue )? ( K_ALLOW K_FILTERING )?
			{
			match(input,K_SELECT,FOLLOW_K_SELECT_in_selectStatement1096); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:301:7: ( K_JSON )?
			int alt3=2;
			int LA3_0 = input.LA(1);
			if ( (LA3_0==K_JSON) ) {
				int LA3_1 = input.LA(2);
				if ( (LA3_1==IDENT||(LA3_1 >= K_AGGREGATE && LA3_1 <= K_ALL)||LA3_1==K_ASCII||(LA3_1 >= K_BIGINT && LA3_1 <= K_BOOLEAN)||(LA3_1 >= K_CALLED && LA3_1 <= K_CLUSTERING)||(LA3_1 >= K_COMPACT && LA3_1 <= K_COUNTER)||(LA3_1 >= K_CUSTOM && LA3_1 <= K_DECIMAL)||(LA3_1 >= K_DISTINCT && LA3_1 <= K_DOUBLE)||(LA3_1 >= K_EXISTS && LA3_1 <= K_FLOAT)||LA3_1==K_FROZEN||(LA3_1 >= K_FUNCTION && LA3_1 <= K_FUNCTIONS)||LA3_1==K_INET||(LA3_1 >= K_INITCOND && LA3_1 <= K_INPUT)||LA3_1==K_INT||(LA3_1 >= K_JSON && LA3_1 <= K_KEYS)||(LA3_1 >= K_KEYSPACES && LA3_1 <= K_LANGUAGE)||(LA3_1 >= K_LIST && LA3_1 <= K_MAP)||LA3_1==K_NOLOGIN||LA3_1==K_NOSUPERUSER||LA3_1==K_OPTIONS||(LA3_1 >= K_PASSWORD && LA3_1 <= K_PERMISSIONS)||LA3_1==K_RETURNS||(LA3_1 >= K_ROLE && LA3_1 <= K_ROLES)||(LA3_1 >= K_SFUNC && LA3_1 <= K_TINYINT)||(LA3_1 >= K_TOKEN && LA3_1 <= K_TRIGGER)||(LA3_1 >= K_TTL && LA3_1 <= K_TYPE)||(LA3_1 >= K_USER && LA3_1 <= K_USERS)||(LA3_1 >= K_UUID && LA3_1 <= K_VARINT)||LA3_1==K_WRITETIME||(LA3_1 >= QMARK && LA3_1 <= QUOTED_NAME)||LA3_1==185) ) {
					alt3=1;
				}
				else if ( (LA3_1==K_AS) ) {
					int LA3_4 = input.LA(3);
					if ( (LA3_4==K_FROM||LA3_4==171||LA3_4==174||LA3_4==176) ) {
						alt3=1;
					}
					else if ( (LA3_4==K_AS) ) {
						int LA3_5 = input.LA(4);
						if ( (LA3_5==IDENT||(LA3_5 >= K_AGGREGATE && LA3_5 <= K_ALL)||LA3_5==K_AS||LA3_5==K_ASCII||(LA3_5 >= K_BIGINT && LA3_5 <= K_BOOLEAN)||(LA3_5 >= K_CALLED && LA3_5 <= K_CLUSTERING)||(LA3_5 >= K_COMPACT && LA3_5 <= K_COUNTER)||(LA3_5 >= K_CUSTOM && LA3_5 <= K_DECIMAL)||(LA3_5 >= K_DISTINCT && LA3_5 <= K_DOUBLE)||(LA3_5 >= K_EXISTS && LA3_5 <= K_FLOAT)||LA3_5==K_FROZEN||(LA3_5 >= K_FUNCTION && LA3_5 <= K_FUNCTIONS)||LA3_5==K_INET||(LA3_5 >= K_INITCOND && LA3_5 <= K_INPUT)||LA3_5==K_INT||(LA3_5 >= K_JSON && LA3_5 <= K_KEYS)||(LA3_5 >= K_KEYSPACES && LA3_5 <= K_LANGUAGE)||(LA3_5 >= K_LIST && LA3_5 <= K_MAP)||LA3_5==K_NOLOGIN||LA3_5==K_NOSUPERUSER||LA3_5==K_OPTIONS||(LA3_5 >= K_PASSWORD && LA3_5 <= K_PERMISSIONS)||LA3_5==K_RETURNS||(LA3_5 >= K_ROLE && LA3_5 <= K_ROLES)||(LA3_5 >= K_SFUNC && LA3_5 <= K_TINYINT)||LA3_5==K_TRIGGER||(LA3_5 >= K_TTL && LA3_5 <= K_TYPE)||(LA3_5 >= K_USER && LA3_5 <= K_USERS)||(LA3_5 >= K_UUID && LA3_5 <= K_VARINT)||LA3_5==K_WRITETIME||LA3_5==QUOTED_NAME) ) {
							alt3=1;
						}
					}
				}
			}
			switch (alt3) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:301:9: K_JSON
					{
					match(input,K_JSON,FOLLOW_K_JSON_in_selectStatement1107); 
					 isJson = true; 
					}
					break;

			}

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:302:7: ( ( K_DISTINCT )? sclause= selectClause )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:302:9: ( K_DISTINCT )? sclause= selectClause
			{
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:302:9: ( K_DISTINCT )?
			int alt4=2;
			int LA4_0 = input.LA(1);
			if ( (LA4_0==K_DISTINCT) ) {
				int LA4_1 = input.LA(2);
				if ( (LA4_1==IDENT||(LA4_1 >= K_AGGREGATE && LA4_1 <= K_ALL)||LA4_1==K_ASCII||(LA4_1 >= K_BIGINT && LA4_1 <= K_BOOLEAN)||(LA4_1 >= K_CALLED && LA4_1 <= K_CLUSTERING)||(LA4_1 >= K_COMPACT && LA4_1 <= K_COUNTER)||(LA4_1 >= K_CUSTOM && LA4_1 <= K_DECIMAL)||(LA4_1 >= K_DISTINCT && LA4_1 <= K_DOUBLE)||(LA4_1 >= K_EXISTS && LA4_1 <= K_FLOAT)||LA4_1==K_FROZEN||(LA4_1 >= K_FUNCTION && LA4_1 <= K_FUNCTIONS)||LA4_1==K_INET||(LA4_1 >= K_INITCOND && LA4_1 <= K_INPUT)||LA4_1==K_INT||(LA4_1 >= K_JSON && LA4_1 <= K_KEYS)||(LA4_1 >= K_KEYSPACES && LA4_1 <= K_LANGUAGE)||(LA4_1 >= K_LIST && LA4_1 <= K_MAP)||LA4_1==K_NOLOGIN||LA4_1==K_NOSUPERUSER||LA4_1==K_OPTIONS||(LA4_1 >= K_PASSWORD && LA4_1 <= K_PERMISSIONS)||LA4_1==K_RETURNS||(LA4_1 >= K_ROLE && LA4_1 <= K_ROLES)||(LA4_1 >= K_SFUNC && LA4_1 <= K_TINYINT)||(LA4_1 >= K_TOKEN && LA4_1 <= K_TRIGGER)||(LA4_1 >= K_TTL && LA4_1 <= K_TYPE)||(LA4_1 >= K_USER && LA4_1 <= K_USERS)||(LA4_1 >= K_UUID && LA4_1 <= K_VARINT)||LA4_1==K_WRITETIME||(LA4_1 >= QMARK && LA4_1 <= QUOTED_NAME)||LA4_1==185) ) {
					alt4=1;
				}
				else if ( (LA4_1==K_AS) ) {
					int LA4_4 = input.LA(3);
					if ( (LA4_4==K_FROM||LA4_4==171||LA4_4==174||LA4_4==176) ) {
						alt4=1;
					}
					else if ( (LA4_4==K_AS) ) {
						int LA4_5 = input.LA(4);
						if ( (LA4_5==IDENT||(LA4_5 >= K_AGGREGATE && LA4_5 <= K_ALL)||LA4_5==K_AS||LA4_5==K_ASCII||(LA4_5 >= K_BIGINT && LA4_5 <= K_BOOLEAN)||(LA4_5 >= K_CALLED && LA4_5 <= K_CLUSTERING)||(LA4_5 >= K_COMPACT && LA4_5 <= K_COUNTER)||(LA4_5 >= K_CUSTOM && LA4_5 <= K_DECIMAL)||(LA4_5 >= K_DISTINCT && LA4_5 <= K_DOUBLE)||(LA4_5 >= K_EXISTS && LA4_5 <= K_FLOAT)||LA4_5==K_FROZEN||(LA4_5 >= K_FUNCTION && LA4_5 <= K_FUNCTIONS)||LA4_5==K_INET||(LA4_5 >= K_INITCOND && LA4_5 <= K_INPUT)||LA4_5==K_INT||(LA4_5 >= K_JSON && LA4_5 <= K_KEYS)||(LA4_5 >= K_KEYSPACES && LA4_5 <= K_LANGUAGE)||(LA4_5 >= K_LIST && LA4_5 <= K_MAP)||LA4_5==K_NOLOGIN||LA4_5==K_NOSUPERUSER||LA4_5==K_OPTIONS||(LA4_5 >= K_PASSWORD && LA4_5 <= K_PERMISSIONS)||LA4_5==K_RETURNS||(LA4_5 >= K_ROLE && LA4_5 <= K_ROLES)||(LA4_5 >= K_SFUNC && LA4_5 <= K_TINYINT)||LA4_5==K_TRIGGER||(LA4_5 >= K_TTL && LA4_5 <= K_TYPE)||(LA4_5 >= K_USER && LA4_5 <= K_USERS)||(LA4_5 >= K_UUID && LA4_5 <= K_VARINT)||LA4_5==K_WRITETIME||LA4_5==QUOTED_NAME) ) {
							alt4=1;
						}
					}
				}
			}
			switch (alt4) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:302:11: K_DISTINCT
					{
					match(input,K_DISTINCT,FOLLOW_K_DISTINCT_in_selectStatement1124); 
					 isDistinct = true; 
					}
					break;

			}

			pushFollow(FOLLOW_selectClause_in_selectStatement1133);
			sclause=selectClause();
			state._fsp--;

			}

			match(input,K_FROM,FOLLOW_K_FROM_in_selectStatement1143); 
			pushFollow(FOLLOW_columnFamilyName_in_selectStatement1147);
			cf=columnFamilyName();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:304:7: ( K_WHERE wclause= whereClause )?
			int alt5=2;
			int LA5_0 = input.LA(1);
			if ( (LA5_0==K_WHERE) ) {
				alt5=1;
			}
			switch (alt5) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:304:9: K_WHERE wclause= whereClause
					{
					match(input,K_WHERE,FOLLOW_K_WHERE_in_selectStatement1157); 
					pushFollow(FOLLOW_whereClause_in_selectStatement1161);
					wclause=whereClause();
					state._fsp--;

					}
					break;

			}

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:305:7: ( K_ORDER K_BY orderByClause[orderings] ( ',' orderByClause[orderings] )* )?
			int alt7=2;
			int LA7_0 = input.LA(1);
			if ( (LA7_0==K_ORDER) ) {
				alt7=1;
			}
			switch (alt7) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:305:9: K_ORDER K_BY orderByClause[orderings] ( ',' orderByClause[orderings] )*
					{
					match(input,K_ORDER,FOLLOW_K_ORDER_in_selectStatement1174); 
					match(input,K_BY,FOLLOW_K_BY_in_selectStatement1176); 
					pushFollow(FOLLOW_orderByClause_in_selectStatement1178);
					orderByClause(orderings);
					state._fsp--;

					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:305:47: ( ',' orderByClause[orderings] )*
					loop6:
					while (true) {
						int alt6=2;
						int LA6_0 = input.LA(1);
						if ( (LA6_0==174) ) {
							alt6=1;
						}

						switch (alt6) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:305:49: ',' orderByClause[orderings]
							{
							match(input,174,FOLLOW_174_in_selectStatement1183); 
							pushFollow(FOLLOW_orderByClause_in_selectStatement1185);
							orderByClause(orderings);
							state._fsp--;

							}
							break;

						default :
							break loop6;
						}
					}

					}
					break;

			}

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:306:7: ( K_LIMIT rows= intValue )?
			int alt8=2;
			int LA8_0 = input.LA(1);
			if ( (LA8_0==K_LIMIT) ) {
				alt8=1;
			}
			switch (alt8) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:306:9: K_LIMIT rows= intValue
					{
					match(input,K_LIMIT,FOLLOW_K_LIMIT_in_selectStatement1202); 
					pushFollow(FOLLOW_intValue_in_selectStatement1206);
					rows=intValue();
					state._fsp--;

					 limit = rows; 
					}
					break;

			}

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:307:7: ( K_ALLOW K_FILTERING )?
			int alt9=2;
			int LA9_0 = input.LA(1);
			if ( (LA9_0==K_ALLOW) ) {
				alt9=1;
			}
			switch (alt9) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:307:9: K_ALLOW K_FILTERING
					{
					match(input,K_ALLOW,FOLLOW_K_ALLOW_in_selectStatement1221); 
					match(input,K_FILTERING,FOLLOW_K_FILTERING_in_selectStatement1223); 
					 allowFiltering = true; 
					}
					break;

			}


			          SelectStatement.Parameters params = new SelectStatement.Parameters(orderings,
			                                                                             isDistinct,
			                                                                             allowFiltering,
			                                                                             isJson);
			          WhereClause where = wclause == null ? WhereClause.empty() : wclause.build();
			          expr = new SelectStatement.RawStatement(cf, params, sclause, where, limit);
			      
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "selectStatement"



	// $ANTLR start "selectClause"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:318:1: selectClause returns [List<RawSelector> expr] : (t1= selector ( ',' tN= selector )* | '\\*' );
	public final List<RawSelector> selectClause() throws RecognitionException {
		List<RawSelector> expr = null;


		RawSelector t1 =null;
		RawSelector tN =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:319:5: (t1= selector ( ',' tN= selector )* | '\\*' )
			int alt11=2;
			int LA11_0 = input.LA(1);
			if ( (LA11_0==IDENT||(LA11_0 >= K_AGGREGATE && LA11_0 <= K_ALL)||LA11_0==K_AS||LA11_0==K_ASCII||(LA11_0 >= K_BIGINT && LA11_0 <= K_BOOLEAN)||(LA11_0 >= K_CALLED && LA11_0 <= K_CLUSTERING)||(LA11_0 >= K_COMPACT && LA11_0 <= K_COUNTER)||(LA11_0 >= K_CUSTOM && LA11_0 <= K_DECIMAL)||(LA11_0 >= K_DISTINCT && LA11_0 <= K_DOUBLE)||(LA11_0 >= K_EXISTS && LA11_0 <= K_FLOAT)||LA11_0==K_FROZEN||(LA11_0 >= K_FUNCTION && LA11_0 <= K_FUNCTIONS)||LA11_0==K_INET||(LA11_0 >= K_INITCOND && LA11_0 <= K_INPUT)||LA11_0==K_INT||(LA11_0 >= K_JSON && LA11_0 <= K_KEYS)||(LA11_0 >= K_KEYSPACES && LA11_0 <= K_LANGUAGE)||(LA11_0 >= K_LIST && LA11_0 <= K_MAP)||LA11_0==K_NOLOGIN||LA11_0==K_NOSUPERUSER||LA11_0==K_OPTIONS||(LA11_0 >= K_PASSWORD && LA11_0 <= K_PERMISSIONS)||LA11_0==K_RETURNS||(LA11_0 >= K_ROLE && LA11_0 <= K_ROLES)||(LA11_0 >= K_SFUNC && LA11_0 <= K_TINYINT)||(LA11_0 >= K_TOKEN && LA11_0 <= K_TRIGGER)||(LA11_0 >= K_TTL && LA11_0 <= K_TYPE)||(LA11_0 >= K_USER && LA11_0 <= K_USERS)||(LA11_0 >= K_UUID && LA11_0 <= K_VARINT)||LA11_0==K_WRITETIME||(LA11_0 >= QMARK && LA11_0 <= QUOTED_NAME)) ) {
				alt11=1;
			}
			else if ( (LA11_0==185) ) {
				alt11=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 11, 0, input);
				throw nvae;
			}

			switch (alt11) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:319:7: t1= selector ( ',' tN= selector )*
					{
					pushFollow(FOLLOW_selector_in_selectClause1260);
					t1=selector();
					state._fsp--;

					 expr = new ArrayList<RawSelector>(); expr.add(t1); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:319:76: ( ',' tN= selector )*
					loop10:
					while (true) {
						int alt10=2;
						int LA10_0 = input.LA(1);
						if ( (LA10_0==174) ) {
							alt10=1;
						}

						switch (alt10) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:319:77: ',' tN= selector
							{
							match(input,174,FOLLOW_174_in_selectClause1265); 
							pushFollow(FOLLOW_selector_in_selectClause1269);
							tN=selector();
							state._fsp--;

							 expr.add(tN); 
							}
							break;

						default :
							break loop10;
						}
					}

					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:320:7: '\\*'
					{
					match(input,185,FOLLOW_185_in_selectClause1281); 
					 expr = Collections.<RawSelector>emptyList();
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "selectClause"



	// $ANTLR start "selector"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:323:1: selector returns [RawSelector s] : us= unaliasedSelector ( K_AS c= noncol_ident )? ;
	public final RawSelector selector() throws RecognitionException {
		RawSelector s = null;


		Selectable.Raw us =null;
		ColumnIdentifier c =null;

		 ColumnIdentifier alias = null; 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:325:5: (us= unaliasedSelector ( K_AS c= noncol_ident )? )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:325:7: us= unaliasedSelector ( K_AS c= noncol_ident )?
			{
			pushFollow(FOLLOW_unaliasedSelector_in_selector1314);
			us=unaliasedSelector();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:325:28: ( K_AS c= noncol_ident )?
			int alt12=2;
			int LA12_0 = input.LA(1);
			if ( (LA12_0==K_AS) ) {
				alt12=1;
			}
			switch (alt12) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:325:29: K_AS c= noncol_ident
					{
					match(input,K_AS,FOLLOW_K_AS_in_selector1317); 
					pushFollow(FOLLOW_noncol_ident_in_selector1321);
					c=noncol_ident();
					state._fsp--;

					 alias = c; 
					}
					break;

			}

			 s = new RawSelector(us, alias); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return s;
	}
	// $ANTLR end "selector"



	// $ANTLR start "unaliasedSelector"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:328:1: unaliasedSelector returns [Selectable.Raw s] : (c= cident | K_COUNT '(' countArgument ')' | K_WRITETIME '(' c= cident ')' | K_TTL '(' c= cident ')' |f= functionName args= selectionFunctionArgs ) ( '.' fi= cident )* ;
	public final Selectable.Raw unaliasedSelector() throws RecognitionException {
		Selectable.Raw s = null;


		ColumnIdentifier.Raw c =null;
		FunctionName f =null;
		List<Selectable.Raw> args =null;
		ColumnIdentifier.Raw fi =null;

		 Selectable.Raw tmp = null; 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:330:5: ( (c= cident | K_COUNT '(' countArgument ')' | K_WRITETIME '(' c= cident ')' | K_TTL '(' c= cident ')' |f= functionName args= selectionFunctionArgs ) ( '.' fi= cident )* )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:330:8: (c= cident | K_COUNT '(' countArgument ')' | K_WRITETIME '(' c= cident ')' | K_TTL '(' c= cident ')' |f= functionName args= selectionFunctionArgs ) ( '.' fi= cident )*
			{
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:330:8: (c= cident | K_COUNT '(' countArgument ')' | K_WRITETIME '(' c= cident ')' | K_TTL '(' c= cident ')' |f= functionName args= selectionFunctionArgs )
			int alt13=5;
			alt13 = dfa13.predict(input);
			switch (alt13) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:330:10: c= cident
					{
					pushFollow(FOLLOW_cident_in_unaliasedSelector1362);
					c=cident();
					state._fsp--;

					 tmp = c; 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:331:10: K_COUNT '(' countArgument ')'
					{
					match(input,K_COUNT,FOLLOW_K_COUNT_in_unaliasedSelector1408); 
					match(input,171,FOLLOW_171_in_unaliasedSelector1410); 
					pushFollow(FOLLOW_countArgument_in_unaliasedSelector1412);
					countArgument();
					state._fsp--;

					match(input,172,FOLLOW_172_in_unaliasedSelector1414); 
					 tmp = new Selectable.WithFunction.Raw(FunctionName.nativeFunction("countRows"), Collections.<Selectable.Raw>emptyList());
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:332:10: K_WRITETIME '(' c= cident ')'
					{
					match(input,K_WRITETIME,FOLLOW_K_WRITETIME_in_unaliasedSelector1439); 
					match(input,171,FOLLOW_171_in_unaliasedSelector1441); 
					pushFollow(FOLLOW_cident_in_unaliasedSelector1445);
					c=cident();
					state._fsp--;

					match(input,172,FOLLOW_172_in_unaliasedSelector1447); 
					 tmp = new Selectable.WritetimeOrTTL.Raw(c, true); 
					}
					break;
				case 4 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:333:10: K_TTL '(' c= cident ')'
					{
					match(input,K_TTL,FOLLOW_K_TTL_in_unaliasedSelector1473); 
					match(input,171,FOLLOW_171_in_unaliasedSelector1481); 
					pushFollow(FOLLOW_cident_in_unaliasedSelector1485);
					c=cident();
					state._fsp--;

					match(input,172,FOLLOW_172_in_unaliasedSelector1487); 
					 tmp = new Selectable.WritetimeOrTTL.Raw(c, false); 
					}
					break;
				case 5 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:334:10: f= functionName args= selectionFunctionArgs
					{
					pushFollow(FOLLOW_functionName_in_unaliasedSelector1515);
					f=functionName();
					state._fsp--;

					pushFollow(FOLLOW_selectionFunctionArgs_in_unaliasedSelector1519);
					args=selectionFunctionArgs();
					state._fsp--;

					 tmp = new Selectable.WithFunction.Raw(f, args); 
					}
					break;

			}

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:335:10: ( '.' fi= cident )*
			loop14:
			while (true) {
				int alt14=2;
				int LA14_0 = input.LA(1);
				if ( (LA14_0==176) ) {
					alt14=1;
				}

				switch (alt14) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:335:12: '.' fi= cident
					{
					match(input,176,FOLLOW_176_in_unaliasedSelector1534); 
					pushFollow(FOLLOW_cident_in_unaliasedSelector1538);
					fi=cident();
					state._fsp--;

					 tmp = new Selectable.WithFieldSelection.Raw(tmp, fi); 
					}
					break;

				default :
					break loop14;
				}
			}

			 s = tmp; 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return s;
	}
	// $ANTLR end "unaliasedSelector"



	// $ANTLR start "selectionFunctionArgs"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:338:1: selectionFunctionArgs returns [List<Selectable.Raw> a] : ( '(' ')' | '(' s1= unaliasedSelector ( ',' sn= unaliasedSelector )* ')' );
	public final List<Selectable.Raw> selectionFunctionArgs() throws RecognitionException {
		List<Selectable.Raw> a = null;


		Selectable.Raw s1 =null;
		Selectable.Raw sn =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:339:5: ( '(' ')' | '(' s1= unaliasedSelector ( ',' sn= unaliasedSelector )* ')' )
			int alt16=2;
			int LA16_0 = input.LA(1);
			if ( (LA16_0==171) ) {
				int LA16_1 = input.LA(2);
				if ( (LA16_1==172) ) {
					alt16=1;
				}
				else if ( (LA16_1==IDENT||(LA16_1 >= K_AGGREGATE && LA16_1 <= K_ALL)||LA16_1==K_AS||LA16_1==K_ASCII||(LA16_1 >= K_BIGINT && LA16_1 <= K_BOOLEAN)||(LA16_1 >= K_CALLED && LA16_1 <= K_CLUSTERING)||(LA16_1 >= K_COMPACT && LA16_1 <= K_COUNTER)||(LA16_1 >= K_CUSTOM && LA16_1 <= K_DECIMAL)||(LA16_1 >= K_DISTINCT && LA16_1 <= K_DOUBLE)||(LA16_1 >= K_EXISTS && LA16_1 <= K_FLOAT)||LA16_1==K_FROZEN||(LA16_1 >= K_FUNCTION && LA16_1 <= K_FUNCTIONS)||LA16_1==K_INET||(LA16_1 >= K_INITCOND && LA16_1 <= K_INPUT)||LA16_1==K_INT||(LA16_1 >= K_JSON && LA16_1 <= K_KEYS)||(LA16_1 >= K_KEYSPACES && LA16_1 <= K_LANGUAGE)||(LA16_1 >= K_LIST && LA16_1 <= K_MAP)||LA16_1==K_NOLOGIN||LA16_1==K_NOSUPERUSER||LA16_1==K_OPTIONS||(LA16_1 >= K_PASSWORD && LA16_1 <= K_PERMISSIONS)||LA16_1==K_RETURNS||(LA16_1 >= K_ROLE && LA16_1 <= K_ROLES)||(LA16_1 >= K_SFUNC && LA16_1 <= K_TINYINT)||(LA16_1 >= K_TOKEN && LA16_1 <= K_TRIGGER)||(LA16_1 >= K_TTL && LA16_1 <= K_TYPE)||(LA16_1 >= K_USER && LA16_1 <= K_USERS)||(LA16_1 >= K_UUID && LA16_1 <= K_VARINT)||LA16_1==K_WRITETIME||(LA16_1 >= QMARK && LA16_1 <= QUOTED_NAME)) ) {
					alt16=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 16, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 16, 0, input);
				throw nvae;
			}

			switch (alt16) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:339:7: '(' ')'
					{
					match(input,171,FOLLOW_171_in_selectionFunctionArgs1566); 
					match(input,172,FOLLOW_172_in_selectionFunctionArgs1568); 
					 a = Collections.emptyList(); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:340:7: '(' s1= unaliasedSelector ( ',' sn= unaliasedSelector )* ')'
					{
					match(input,171,FOLLOW_171_in_selectionFunctionArgs1578); 
					pushFollow(FOLLOW_unaliasedSelector_in_selectionFunctionArgs1582);
					s1=unaliasedSelector();
					state._fsp--;

					 List<Selectable.Raw> args = new ArrayList<Selectable.Raw>(); args.add(s1); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:341:11: ( ',' sn= unaliasedSelector )*
					loop15:
					while (true) {
						int alt15=2;
						int LA15_0 = input.LA(1);
						if ( (LA15_0==174) ) {
							alt15=1;
						}

						switch (alt15) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:341:13: ',' sn= unaliasedSelector
							{
							match(input,174,FOLLOW_174_in_selectionFunctionArgs1598); 
							pushFollow(FOLLOW_unaliasedSelector_in_selectionFunctionArgs1602);
							sn=unaliasedSelector();
							state._fsp--;

							 args.add(sn); 
							}
							break;

						default :
							break loop15;
						}
					}

					match(input,172,FOLLOW_172_in_selectionFunctionArgs1615); 
					 a = args; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return a;
	}
	// $ANTLR end "selectionFunctionArgs"



	// $ANTLR start "countArgument"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:345:1: countArgument : ( '\\*' |i= INTEGER );
	public final void countArgument() throws RecognitionException {
		Token i=null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:346:5: ( '\\*' |i= INTEGER )
			int alt17=2;
			int LA17_0 = input.LA(1);
			if ( (LA17_0==185) ) {
				alt17=1;
			}
			else if ( (LA17_0==INTEGER) ) {
				alt17=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 17, 0, input);
				throw nvae;
			}

			switch (alt17) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:346:7: '\\*'
					{
					match(input,185,FOLLOW_185_in_countArgument1634); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:347:7: i= INTEGER
					{
					i=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_countArgument1644); 
					 if (!i.getText().equals("1")) addRecognitionError("Only COUNT(1) is supported, got COUNT(" + i.getText() + ")");
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "countArgument"



	// $ANTLR start "whereClause"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:350:1: whereClause returns [WhereClause.Builder clause] : relationOrExpression[$clause] ( K_AND relationOrExpression[$clause] )* ;
	public final WhereClause.Builder whereClause() throws RecognitionException {
		WhereClause.Builder clause = null;


		 clause = new WhereClause.Builder(); 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:352:5: ( relationOrExpression[$clause] ( K_AND relationOrExpression[$clause] )* )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:352:7: relationOrExpression[$clause] ( K_AND relationOrExpression[$clause] )*
			{
			pushFollow(FOLLOW_relationOrExpression_in_whereClause1675);
			relationOrExpression(clause);
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:352:37: ( K_AND relationOrExpression[$clause] )*
			loop18:
			while (true) {
				int alt18=2;
				int LA18_0 = input.LA(1);
				if ( (LA18_0==K_AND) ) {
					alt18=1;
				}

				switch (alt18) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:352:38: K_AND relationOrExpression[$clause]
					{
					match(input,K_AND,FOLLOW_K_AND_in_whereClause1679); 
					pushFollow(FOLLOW_relationOrExpression_in_whereClause1681);
					relationOrExpression(clause);
					state._fsp--;

					}
					break;

				default :
					break loop18;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return clause;
	}
	// $ANTLR end "whereClause"



	// $ANTLR start "relationOrExpression"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:355:1: relationOrExpression[WhereClause.Builder clause] : ( relation[$clause] | customIndexExpression[$clause] );
	public final void relationOrExpression(WhereClause.Builder clause) throws RecognitionException {
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:356:5: ( relation[$clause] | customIndexExpression[$clause] )
			int alt19=2;
			int LA19_0 = input.LA(1);
			if ( (LA19_0==IDENT||(LA19_0 >= K_AGGREGATE && LA19_0 <= K_ALL)||LA19_0==K_AS||LA19_0==K_ASCII||(LA19_0 >= K_BIGINT && LA19_0 <= K_BOOLEAN)||(LA19_0 >= K_CALLED && LA19_0 <= K_CLUSTERING)||(LA19_0 >= K_COMPACT && LA19_0 <= K_COUNTER)||(LA19_0 >= K_CUSTOM && LA19_0 <= K_DECIMAL)||(LA19_0 >= K_DISTINCT && LA19_0 <= K_DOUBLE)||(LA19_0 >= K_EXISTS && LA19_0 <= K_FLOAT)||LA19_0==K_FROZEN||(LA19_0 >= K_FUNCTION && LA19_0 <= K_FUNCTIONS)||LA19_0==K_INET||(LA19_0 >= K_INITCOND && LA19_0 <= K_INPUT)||LA19_0==K_INT||(LA19_0 >= K_JSON && LA19_0 <= K_KEYS)||(LA19_0 >= K_KEYSPACES && LA19_0 <= K_LANGUAGE)||(LA19_0 >= K_LIST && LA19_0 <= K_MAP)||LA19_0==K_NOLOGIN||LA19_0==K_NOSUPERUSER||LA19_0==K_OPTIONS||(LA19_0 >= K_PASSWORD && LA19_0 <= K_PERMISSIONS)||LA19_0==K_RETURNS||(LA19_0 >= K_ROLE && LA19_0 <= K_ROLES)||(LA19_0 >= K_SFUNC && LA19_0 <= K_TINYINT)||(LA19_0 >= K_TOKEN && LA19_0 <= K_TRIGGER)||(LA19_0 >= K_TTL && LA19_0 <= K_TYPE)||(LA19_0 >= K_USER && LA19_0 <= K_USERS)||(LA19_0 >= K_UUID && LA19_0 <= K_VARINT)||LA19_0==K_WRITETIME||LA19_0==QUOTED_NAME||LA19_0==171) ) {
				alt19=1;
			}
			else if ( (LA19_0==187) ) {
				alt19=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 19, 0, input);
				throw nvae;
			}

			switch (alt19) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:356:7: relation[$clause]
					{
					pushFollow(FOLLOW_relation_in_relationOrExpression1703);
					relation(clause);
					state._fsp--;

					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:357:7: customIndexExpression[$clause]
					{
					pushFollow(FOLLOW_customIndexExpression_in_relationOrExpression1712);
					customIndexExpression(clause);
					state._fsp--;

					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "relationOrExpression"



	// $ANTLR start "customIndexExpression"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:360:1: customIndexExpression[WhereClause.Builder clause] : 'expr(' idxName[name] ',' t= term ')' ;
	public final void customIndexExpression(WhereClause.Builder clause) throws RecognitionException {
		Term.Raw t =null;

		IndexName name = new IndexName();
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:362:5: ( 'expr(' idxName[name] ',' t= term ')' )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:362:7: 'expr(' idxName[name] ',' t= term ')'
			{
			match(input,187,FOLLOW_187_in_customIndexExpression1740); 
			pushFollow(FOLLOW_idxName_in_customIndexExpression1742);
			idxName(name);
			state._fsp--;

			match(input,174,FOLLOW_174_in_customIndexExpression1745); 
			pushFollow(FOLLOW_term_in_customIndexExpression1749);
			t=term();
			state._fsp--;

			match(input,172,FOLLOW_172_in_customIndexExpression1751); 
			 clause.add(new CustomIndexExpression(name, t));
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "customIndexExpression"



	// $ANTLR start "orderByClause"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:365:1: orderByClause[Map<ColumnIdentifier.Raw, Boolean> orderings] : c= cident ( K_ASC | K_DESC )? ;
	public final void orderByClause(Map<ColumnIdentifier.Raw, Boolean> orderings) throws RecognitionException {
		ColumnIdentifier.Raw c =null;


		        boolean reversed = false;
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:369:5: (c= cident ( K_ASC | K_DESC )? )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:369:7: c= cident ( K_ASC | K_DESC )?
			{
			pushFollow(FOLLOW_cident_in_orderByClause1781);
			c=cident();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:369:16: ( K_ASC | K_DESC )?
			int alt20=3;
			int LA20_0 = input.LA(1);
			if ( (LA20_0==K_ASC) ) {
				alt20=1;
			}
			else if ( (LA20_0==K_DESC) ) {
				alt20=2;
			}
			switch (alt20) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:369:17: K_ASC
					{
					match(input,K_ASC,FOLLOW_K_ASC_in_orderByClause1784); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:369:25: K_DESC
					{
					match(input,K_DESC,FOLLOW_K_DESC_in_orderByClause1788); 
					 reversed = true; 
					}
					break;

			}

			 orderings.put(c, reversed); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "orderByClause"



	// $ANTLR start "insertStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:378:1: insertStatement returns [ModificationStatement.Parsed expr] : K_INSERT K_INTO cf= columnFamilyName (st1= normalInsertStatement[cf] | K_JSON st2= jsonInsertStatement[cf] ) ;
	public final ModificationStatement.Parsed insertStatement() throws RecognitionException {
		ModificationStatement.Parsed expr = null;


		CFName cf =null;
		UpdateStatement.ParsedInsert st1 =null;
		UpdateStatement.ParsedInsertJson st2 =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:379:5: ( K_INSERT K_INTO cf= columnFamilyName (st1= normalInsertStatement[cf] | K_JSON st2= jsonInsertStatement[cf] ) )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:379:7: K_INSERT K_INTO cf= columnFamilyName (st1= normalInsertStatement[cf] | K_JSON st2= jsonInsertStatement[cf] )
			{
			match(input,K_INSERT,FOLLOW_K_INSERT_in_insertStatement1817); 
			match(input,K_INTO,FOLLOW_K_INTO_in_insertStatement1819); 
			pushFollow(FOLLOW_columnFamilyName_in_insertStatement1823);
			cf=columnFamilyName();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:380:9: (st1= normalInsertStatement[cf] | K_JSON st2= jsonInsertStatement[cf] )
			int alt21=2;
			int LA21_0 = input.LA(1);
			if ( (LA21_0==171) ) {
				alt21=1;
			}
			else if ( (LA21_0==K_JSON) ) {
				alt21=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 21, 0, input);
				throw nvae;
			}

			switch (alt21) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:380:11: st1= normalInsertStatement[cf]
					{
					pushFollow(FOLLOW_normalInsertStatement_in_insertStatement1837);
					st1=normalInsertStatement(cf);
					state._fsp--;

					 expr = st1; 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:381:11: K_JSON st2= jsonInsertStatement[cf]
					{
					match(input,K_JSON,FOLLOW_K_JSON_in_insertStatement1852); 
					pushFollow(FOLLOW_jsonInsertStatement_in_insertStatement1856);
					st2=jsonInsertStatement(cf);
					state._fsp--;

					 expr = st2; 
					}
					break;

			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "insertStatement"



	// $ANTLR start "normalInsertStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:384:1: normalInsertStatement[CFName cf] returns [UpdateStatement.ParsedInsert expr] : '(' c1= cident ( ',' cn= cident )* ')' K_VALUES '(' v1= term ( ',' vn= term )* ')' ( K_IF K_NOT K_EXISTS )? ( usingClause[attrs] )? ;
	public final UpdateStatement.ParsedInsert normalInsertStatement(CFName cf) throws RecognitionException {
		UpdateStatement.ParsedInsert expr = null;


		ColumnIdentifier.Raw c1 =null;
		ColumnIdentifier.Raw cn =null;
		Term.Raw v1 =null;
		Term.Raw vn =null;


		        Attributes.Raw attrs = new Attributes.Raw();
		        List<ColumnIdentifier.Raw> columnNames  = new ArrayList<ColumnIdentifier.Raw>();
		        List<Term.Raw> values = new ArrayList<Term.Raw>();
		        boolean ifNotExists = false;
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:391:5: ( '(' c1= cident ( ',' cn= cident )* ')' K_VALUES '(' v1= term ( ',' vn= term )* ')' ( K_IF K_NOT K_EXISTS )? ( usingClause[attrs] )? )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:391:7: '(' c1= cident ( ',' cn= cident )* ')' K_VALUES '(' v1= term ( ',' vn= term )* ')' ( K_IF K_NOT K_EXISTS )? ( usingClause[attrs] )?
			{
			match(input,171,FOLLOW_171_in_normalInsertStatement1892); 
			pushFollow(FOLLOW_cident_in_normalInsertStatement1896);
			c1=cident();
			state._fsp--;

			 columnNames.add(c1); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:391:47: ( ',' cn= cident )*
			loop22:
			while (true) {
				int alt22=2;
				int LA22_0 = input.LA(1);
				if ( (LA22_0==174) ) {
					alt22=1;
				}

				switch (alt22) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:391:49: ',' cn= cident
					{
					match(input,174,FOLLOW_174_in_normalInsertStatement1903); 
					pushFollow(FOLLOW_cident_in_normalInsertStatement1907);
					cn=cident();
					state._fsp--;

					 columnNames.add(cn); 
					}
					break;

				default :
					break loop22;
				}
			}

			match(input,172,FOLLOW_172_in_normalInsertStatement1914); 
			match(input,K_VALUES,FOLLOW_K_VALUES_in_normalInsertStatement1922); 
			match(input,171,FOLLOW_171_in_normalInsertStatement1930); 
			pushFollow(FOLLOW_term_in_normalInsertStatement1934);
			v1=term();
			state._fsp--;

			 values.add(v1); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:393:39: ( ',' vn= term )*
			loop23:
			while (true) {
				int alt23=2;
				int LA23_0 = input.LA(1);
				if ( (LA23_0==174) ) {
					alt23=1;
				}

				switch (alt23) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:393:41: ',' vn= term
					{
					match(input,174,FOLLOW_174_in_normalInsertStatement1940); 
					pushFollow(FOLLOW_term_in_normalInsertStatement1944);
					vn=term();
					state._fsp--;

					 values.add(vn); 
					}
					break;

				default :
					break loop23;
				}
			}

			match(input,172,FOLLOW_172_in_normalInsertStatement1951); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:394:7: ( K_IF K_NOT K_EXISTS )?
			int alt24=2;
			int LA24_0 = input.LA(1);
			if ( (LA24_0==K_IF) ) {
				alt24=1;
			}
			switch (alt24) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:394:9: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_normalInsertStatement1961); 
					match(input,K_NOT,FOLLOW_K_NOT_in_normalInsertStatement1963); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_normalInsertStatement1965); 
					 ifNotExists = true; 
					}
					break;

			}

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:395:7: ( usingClause[attrs] )?
			int alt25=2;
			int LA25_0 = input.LA(1);
			if ( (LA25_0==K_USING) ) {
				alt25=1;
			}
			switch (alt25) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:395:9: usingClause[attrs]
					{
					pushFollow(FOLLOW_usingClause_in_normalInsertStatement1980);
					usingClause(attrs);
					state._fsp--;

					}
					break;

			}


			          expr = new UpdateStatement.ParsedInsert(cf, attrs, columnNames, values, ifNotExists);
			      
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "normalInsertStatement"



	// $ANTLR start "jsonInsertStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:401:1: jsonInsertStatement[CFName cf] returns [UpdateStatement.ParsedInsertJson expr] : val= jsonValue ( K_IF K_NOT K_EXISTS )? ( usingClause[attrs] )? ;
	public final UpdateStatement.ParsedInsertJson jsonInsertStatement(CFName cf) throws RecognitionException {
		UpdateStatement.ParsedInsertJson expr = null;


		Json.Raw val =null;


		        Attributes.Raw attrs = new Attributes.Raw();
		        boolean ifNotExists = false;
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:406:5: (val= jsonValue ( K_IF K_NOT K_EXISTS )? ( usingClause[attrs] )? )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:406:7: val= jsonValue ( K_IF K_NOT K_EXISTS )? ( usingClause[attrs] )?
			{
			pushFollow(FOLLOW_jsonValue_in_jsonInsertStatement2026);
			val=jsonValue();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:407:7: ( K_IF K_NOT K_EXISTS )?
			int alt26=2;
			int LA26_0 = input.LA(1);
			if ( (LA26_0==K_IF) ) {
				alt26=1;
			}
			switch (alt26) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:407:9: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_jsonInsertStatement2036); 
					match(input,K_NOT,FOLLOW_K_NOT_in_jsonInsertStatement2038); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_jsonInsertStatement2040); 
					 ifNotExists = true; 
					}
					break;

			}

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:408:7: ( usingClause[attrs] )?
			int alt27=2;
			int LA27_0 = input.LA(1);
			if ( (LA27_0==K_USING) ) {
				alt27=1;
			}
			switch (alt27) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:408:9: usingClause[attrs]
					{
					pushFollow(FOLLOW_usingClause_in_jsonInsertStatement2055);
					usingClause(attrs);
					state._fsp--;

					}
					break;

			}


			          expr = new UpdateStatement.ParsedInsertJson(cf, attrs, val, ifNotExists);
			      
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "jsonInsertStatement"



	// $ANTLR start "jsonValue"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:414:1: jsonValue returns [Json.Raw value] : (|s= STRING_LITERAL | ':' id= noncol_ident | QMARK );
	public final Json.Raw jsonValue() throws RecognitionException {
		Json.Raw value = null;


		Token s=null;
		ColumnIdentifier id =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:415:5: (|s= STRING_LITERAL | ':' id= noncol_ident | QMARK )
			int alt28=4;
			switch ( input.LA(1) ) {
			case EOF:
			case K_APPLY:
			case K_DELETE:
			case K_IF:
			case K_INSERT:
			case K_UPDATE:
			case K_USING:
			case 178:
				{
				alt28=1;
				}
				break;
			case STRING_LITERAL:
				{
				alt28=2;
				}
				break;
			case 177:
				{
				alt28=3;
				}
				break;
			case QMARK:
				{
				alt28=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 28, 0, input);
				throw nvae;
			}
			switch (alt28) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:416:5: 
					{
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:416:7: s= STRING_LITERAL
					{
					s=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_jsonValue2096); 
					 value = new Json.Literal((s!=null?s.getText():null)); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:417:7: ':' id= noncol_ident
					{
					match(input,177,FOLLOW_177_in_jsonValue2106); 
					pushFollow(FOLLOW_noncol_ident_in_jsonValue2110);
					id=noncol_ident();
					state._fsp--;

					 value = newJsonBindVariables(id); 
					}
					break;
				case 4 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:418:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_jsonValue2124); 
					 value = newJsonBindVariables(null); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "jsonValue"



	// $ANTLR start "usingClause"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:421:1: usingClause[Attributes.Raw attrs] : K_USING usingClauseObjective[attrs] ( K_AND usingClauseObjective[attrs] )* ;
	public final void usingClause(Attributes.Raw attrs) throws RecognitionException {
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:422:5: ( K_USING usingClauseObjective[attrs] ( K_AND usingClauseObjective[attrs] )* )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:422:7: K_USING usingClauseObjective[attrs] ( K_AND usingClauseObjective[attrs] )*
			{
			match(input,K_USING,FOLLOW_K_USING_in_usingClause2155); 
			pushFollow(FOLLOW_usingClauseObjective_in_usingClause2157);
			usingClauseObjective(attrs);
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:422:43: ( K_AND usingClauseObjective[attrs] )*
			loop29:
			while (true) {
				int alt29=2;
				int LA29_0 = input.LA(1);
				if ( (LA29_0==K_AND) ) {
					alt29=1;
				}

				switch (alt29) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:422:45: K_AND usingClauseObjective[attrs]
					{
					match(input,K_AND,FOLLOW_K_AND_in_usingClause2162); 
					pushFollow(FOLLOW_usingClauseObjective_in_usingClause2164);
					usingClauseObjective(attrs);
					state._fsp--;

					}
					break;

				default :
					break loop29;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "usingClause"



	// $ANTLR start "usingClauseObjective"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:425:1: usingClauseObjective[Attributes.Raw attrs] : ( K_TIMESTAMP ts= intValue | K_TTL t= intValue );
	public final void usingClauseObjective(Attributes.Raw attrs) throws RecognitionException {
		Term.Raw ts =null;
		Term.Raw t =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:426:5: ( K_TIMESTAMP ts= intValue | K_TTL t= intValue )
			int alt30=2;
			int LA30_0 = input.LA(1);
			if ( (LA30_0==K_TIMESTAMP) ) {
				alt30=1;
			}
			else if ( (LA30_0==K_TTL) ) {
				alt30=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 30, 0, input);
				throw nvae;
			}

			switch (alt30) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:426:7: K_TIMESTAMP ts= intValue
					{
					match(input,K_TIMESTAMP,FOLLOW_K_TIMESTAMP_in_usingClauseObjective2186); 
					pushFollow(FOLLOW_intValue_in_usingClauseObjective2190);
					ts=intValue();
					state._fsp--;

					 attrs.timestamp = ts; 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:427:7: K_TTL t= intValue
					{
					match(input,K_TTL,FOLLOW_K_TTL_in_usingClauseObjective2200); 
					pushFollow(FOLLOW_intValue_in_usingClauseObjective2204);
					t=intValue();
					state._fsp--;

					 attrs.timeToLive = t; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "usingClauseObjective"



	// $ANTLR start "updateStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:437:1: updateStatement returns [UpdateStatement.ParsedUpdate expr] : K_UPDATE cf= columnFamilyName ( usingClause[attrs] )? K_SET columnOperation[operations] ( ',' columnOperation[operations] )* K_WHERE wclause= whereClause ( K_IF ( K_EXISTS |conditions= updateConditions ) )? ;
	public final UpdateStatement.ParsedUpdate updateStatement() throws RecognitionException {
		UpdateStatement.ParsedUpdate expr = null;


		CFName cf =null;
		WhereClause.Builder wclause =null;
		List<Pair<ColumnIdentifier.Raw, ColumnCondition.Raw>> conditions =null;


		        Attributes.Raw attrs = new Attributes.Raw();
		        List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations = new ArrayList<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>>();
		        boolean ifExists = false;
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:443:5: ( K_UPDATE cf= columnFamilyName ( usingClause[attrs] )? K_SET columnOperation[operations] ( ',' columnOperation[operations] )* K_WHERE wclause= whereClause ( K_IF ( K_EXISTS |conditions= updateConditions ) )? )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:443:7: K_UPDATE cf= columnFamilyName ( usingClause[attrs] )? K_SET columnOperation[operations] ( ',' columnOperation[operations] )* K_WHERE wclause= whereClause ( K_IF ( K_EXISTS |conditions= updateConditions ) )?
			{
			match(input,K_UPDATE,FOLLOW_K_UPDATE_in_updateStatement2238); 
			pushFollow(FOLLOW_columnFamilyName_in_updateStatement2242);
			cf=columnFamilyName();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:444:7: ( usingClause[attrs] )?
			int alt31=2;
			int LA31_0 = input.LA(1);
			if ( (LA31_0==K_USING) ) {
				alt31=1;
			}
			switch (alt31) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:444:9: usingClause[attrs]
					{
					pushFollow(FOLLOW_usingClause_in_updateStatement2252);
					usingClause(attrs);
					state._fsp--;

					}
					break;

			}

			match(input,K_SET,FOLLOW_K_SET_in_updateStatement2264); 
			pushFollow(FOLLOW_columnOperation_in_updateStatement2266);
			columnOperation(operations);
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:445:41: ( ',' columnOperation[operations] )*
			loop32:
			while (true) {
				int alt32=2;
				int LA32_0 = input.LA(1);
				if ( (LA32_0==174) ) {
					alt32=1;
				}

				switch (alt32) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:445:42: ',' columnOperation[operations]
					{
					match(input,174,FOLLOW_174_in_updateStatement2270); 
					pushFollow(FOLLOW_columnOperation_in_updateStatement2272);
					columnOperation(operations);
					state._fsp--;

					}
					break;

				default :
					break loop32;
				}
			}

			match(input,K_WHERE,FOLLOW_K_WHERE_in_updateStatement2283); 
			pushFollow(FOLLOW_whereClause_in_updateStatement2287);
			wclause=whereClause();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:447:7: ( K_IF ( K_EXISTS |conditions= updateConditions ) )?
			int alt34=2;
			int LA34_0 = input.LA(1);
			if ( (LA34_0==K_IF) ) {
				alt34=1;
			}
			switch (alt34) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:447:9: K_IF ( K_EXISTS |conditions= updateConditions )
					{
					match(input,K_IF,FOLLOW_K_IF_in_updateStatement2297); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:447:14: ( K_EXISTS |conditions= updateConditions )
					int alt33=2;
					int LA33_0 = input.LA(1);
					if ( (LA33_0==K_EXISTS) ) {
						int LA33_1 = input.LA(2);
						if ( (LA33_1==EOF||LA33_1==K_APPLY||LA33_1==K_DELETE||LA33_1==K_INSERT||LA33_1==K_UPDATE||LA33_1==178) ) {
							alt33=1;
						}
						else if ( (LA33_1==K_IN||LA33_1==170||(LA33_1 >= 179 && LA33_1 <= 184)) ) {
							alt33=2;
						}

						else {
							int nvaeMark = input.mark();
							try {
								input.consume();
								NoViableAltException nvae =
									new NoViableAltException("", 33, 1, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}
					else if ( (LA33_0==IDENT||(LA33_0 >= K_AGGREGATE && LA33_0 <= K_ALL)||LA33_0==K_AS||LA33_0==K_ASCII||(LA33_0 >= K_BIGINT && LA33_0 <= K_BOOLEAN)||(LA33_0 >= K_CALLED && LA33_0 <= K_CLUSTERING)||(LA33_0 >= K_COMPACT && LA33_0 <= K_COUNTER)||(LA33_0 >= K_CUSTOM && LA33_0 <= K_DECIMAL)||(LA33_0 >= K_DISTINCT && LA33_0 <= K_DOUBLE)||(LA33_0 >= K_FILTERING && LA33_0 <= K_FLOAT)||LA33_0==K_FROZEN||(LA33_0 >= K_FUNCTION && LA33_0 <= K_FUNCTIONS)||LA33_0==K_INET||(LA33_0 >= K_INITCOND && LA33_0 <= K_INPUT)||LA33_0==K_INT||(LA33_0 >= K_JSON && LA33_0 <= K_KEYS)||(LA33_0 >= K_KEYSPACES && LA33_0 <= K_LANGUAGE)||(LA33_0 >= K_LIST && LA33_0 <= K_MAP)||LA33_0==K_NOLOGIN||LA33_0==K_NOSUPERUSER||LA33_0==K_OPTIONS||(LA33_0 >= K_PASSWORD && LA33_0 <= K_PERMISSIONS)||LA33_0==K_RETURNS||(LA33_0 >= K_ROLE && LA33_0 <= K_ROLES)||(LA33_0 >= K_SFUNC && LA33_0 <= K_TINYINT)||LA33_0==K_TRIGGER||(LA33_0 >= K_TTL && LA33_0 <= K_TYPE)||(LA33_0 >= K_USER && LA33_0 <= K_USERS)||(LA33_0 >= K_UUID && LA33_0 <= K_VARINT)||LA33_0==K_WRITETIME||LA33_0==QUOTED_NAME) ) {
						alt33=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 33, 0, input);
						throw nvae;
					}

					switch (alt33) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:447:16: K_EXISTS
							{
							match(input,K_EXISTS,FOLLOW_K_EXISTS_in_updateStatement2301); 
							 ifExists = true; 
							}
							break;
						case 2 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:447:48: conditions= updateConditions
							{
							pushFollow(FOLLOW_updateConditions_in_updateStatement2309);
							conditions=updateConditions();
							state._fsp--;

							}
							break;

					}

					}
					break;

			}


			          return new UpdateStatement.ParsedUpdate(cf,
			                                                  attrs,
			                                                  operations,
			                                                  wclause.build(),
			                                                  conditions == null ? Collections.<Pair<ColumnIdentifier.Raw, ColumnCondition.Raw>>emptyList() : conditions,
			                                                  ifExists);
			     
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "updateStatement"



	// $ANTLR start "updateConditions"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:458:1: updateConditions returns [List<Pair<ColumnIdentifier.Raw, ColumnCondition.Raw>> conditions] : columnCondition[conditions] ( K_AND columnCondition[conditions] )* ;
	public final List<Pair<ColumnIdentifier.Raw, ColumnCondition.Raw>> updateConditions() throws RecognitionException {
		List<Pair<ColumnIdentifier.Raw, ColumnCondition.Raw>> conditions = null;


		 conditions = new ArrayList<Pair<ColumnIdentifier.Raw, ColumnCondition.Raw>>(); 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:460:5: ( columnCondition[conditions] ( K_AND columnCondition[conditions] )* )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:460:7: columnCondition[conditions] ( K_AND columnCondition[conditions] )*
			{
			pushFollow(FOLLOW_columnCondition_in_updateConditions2351);
			columnCondition(conditions);
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:460:35: ( K_AND columnCondition[conditions] )*
			loop35:
			while (true) {
				int alt35=2;
				int LA35_0 = input.LA(1);
				if ( (LA35_0==K_AND) ) {
					alt35=1;
				}

				switch (alt35) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:460:37: K_AND columnCondition[conditions]
					{
					match(input,K_AND,FOLLOW_K_AND_in_updateConditions2356); 
					pushFollow(FOLLOW_columnCondition_in_updateConditions2358);
					columnCondition(conditions);
					state._fsp--;

					}
					break;

				default :
					break loop35;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return conditions;
	}
	// $ANTLR end "updateConditions"



	// $ANTLR start "deleteStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:471:1: deleteStatement returns [DeleteStatement.Parsed expr] : K_DELETE (dels= deleteSelection )? K_FROM cf= columnFamilyName ( usingClauseDelete[attrs] )? K_WHERE wclause= whereClause ( K_IF ( K_EXISTS |conditions= updateConditions ) )? ;
	public final DeleteStatement.Parsed deleteStatement() throws RecognitionException {
		DeleteStatement.Parsed expr = null;


		List<Operation.RawDeletion> dels =null;
		CFName cf =null;
		WhereClause.Builder wclause =null;
		List<Pair<ColumnIdentifier.Raw, ColumnCondition.Raw>> conditions =null;


		        Attributes.Raw attrs = new Attributes.Raw();
		        List<Operation.RawDeletion> columnDeletions = Collections.emptyList();
		        boolean ifExists = false;
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:477:5: ( K_DELETE (dels= deleteSelection )? K_FROM cf= columnFamilyName ( usingClauseDelete[attrs] )? K_WHERE wclause= whereClause ( K_IF ( K_EXISTS |conditions= updateConditions ) )? )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:477:7: K_DELETE (dels= deleteSelection )? K_FROM cf= columnFamilyName ( usingClauseDelete[attrs] )? K_WHERE wclause= whereClause ( K_IF ( K_EXISTS |conditions= updateConditions ) )?
			{
			match(input,K_DELETE,FOLLOW_K_DELETE_in_deleteStatement2395); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:477:16: (dels= deleteSelection )?
			int alt36=2;
			int LA36_0 = input.LA(1);
			if ( (LA36_0==IDENT||(LA36_0 >= K_AGGREGATE && LA36_0 <= K_ALL)||LA36_0==K_AS||LA36_0==K_ASCII||(LA36_0 >= K_BIGINT && LA36_0 <= K_BOOLEAN)||(LA36_0 >= K_CALLED && LA36_0 <= K_CLUSTERING)||(LA36_0 >= K_COMPACT && LA36_0 <= K_COUNTER)||(LA36_0 >= K_CUSTOM && LA36_0 <= K_DECIMAL)||(LA36_0 >= K_DISTINCT && LA36_0 <= K_DOUBLE)||(LA36_0 >= K_EXISTS && LA36_0 <= K_FLOAT)||LA36_0==K_FROZEN||(LA36_0 >= K_FUNCTION && LA36_0 <= K_FUNCTIONS)||LA36_0==K_INET||(LA36_0 >= K_INITCOND && LA36_0 <= K_INPUT)||LA36_0==K_INT||(LA36_0 >= K_JSON && LA36_0 <= K_KEYS)||(LA36_0 >= K_KEYSPACES && LA36_0 <= K_LANGUAGE)||(LA36_0 >= K_LIST && LA36_0 <= K_MAP)||LA36_0==K_NOLOGIN||LA36_0==K_NOSUPERUSER||LA36_0==K_OPTIONS||(LA36_0 >= K_PASSWORD && LA36_0 <= K_PERMISSIONS)||LA36_0==K_RETURNS||(LA36_0 >= K_ROLE && LA36_0 <= K_ROLES)||(LA36_0 >= K_SFUNC && LA36_0 <= K_TINYINT)||LA36_0==K_TRIGGER||(LA36_0 >= K_TTL && LA36_0 <= K_TYPE)||(LA36_0 >= K_USER && LA36_0 <= K_USERS)||(LA36_0 >= K_UUID && LA36_0 <= K_VARINT)||LA36_0==K_WRITETIME||LA36_0==QUOTED_NAME) ) {
				alt36=1;
			}
			switch (alt36) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:477:18: dels= deleteSelection
					{
					pushFollow(FOLLOW_deleteSelection_in_deleteStatement2401);
					dels=deleteSelection();
					state._fsp--;

					 columnDeletions = dels; 
					}
					break;

			}

			match(input,K_FROM,FOLLOW_K_FROM_in_deleteStatement2414); 
			pushFollow(FOLLOW_columnFamilyName_in_deleteStatement2418);
			cf=columnFamilyName();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:479:7: ( usingClauseDelete[attrs] )?
			int alt37=2;
			int LA37_0 = input.LA(1);
			if ( (LA37_0==K_USING) ) {
				alt37=1;
			}
			switch (alt37) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:479:9: usingClauseDelete[attrs]
					{
					pushFollow(FOLLOW_usingClauseDelete_in_deleteStatement2428);
					usingClauseDelete(attrs);
					state._fsp--;

					}
					break;

			}

			match(input,K_WHERE,FOLLOW_K_WHERE_in_deleteStatement2440); 
			pushFollow(FOLLOW_whereClause_in_deleteStatement2444);
			wclause=whereClause();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:481:7: ( K_IF ( K_EXISTS |conditions= updateConditions ) )?
			int alt39=2;
			int LA39_0 = input.LA(1);
			if ( (LA39_0==K_IF) ) {
				alt39=1;
			}
			switch (alt39) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:481:9: K_IF ( K_EXISTS |conditions= updateConditions )
					{
					match(input,K_IF,FOLLOW_K_IF_in_deleteStatement2454); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:481:14: ( K_EXISTS |conditions= updateConditions )
					int alt38=2;
					int LA38_0 = input.LA(1);
					if ( (LA38_0==K_EXISTS) ) {
						int LA38_1 = input.LA(2);
						if ( (LA38_1==EOF||LA38_1==K_APPLY||LA38_1==K_DELETE||LA38_1==K_INSERT||LA38_1==K_UPDATE||LA38_1==178) ) {
							alt38=1;
						}
						else if ( (LA38_1==K_IN||LA38_1==170||(LA38_1 >= 179 && LA38_1 <= 184)) ) {
							alt38=2;
						}

						else {
							int nvaeMark = input.mark();
							try {
								input.consume();
								NoViableAltException nvae =
									new NoViableAltException("", 38, 1, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}
					else if ( (LA38_0==IDENT||(LA38_0 >= K_AGGREGATE && LA38_0 <= K_ALL)||LA38_0==K_AS||LA38_0==K_ASCII||(LA38_0 >= K_BIGINT && LA38_0 <= K_BOOLEAN)||(LA38_0 >= K_CALLED && LA38_0 <= K_CLUSTERING)||(LA38_0 >= K_COMPACT && LA38_0 <= K_COUNTER)||(LA38_0 >= K_CUSTOM && LA38_0 <= K_DECIMAL)||(LA38_0 >= K_DISTINCT && LA38_0 <= K_DOUBLE)||(LA38_0 >= K_FILTERING && LA38_0 <= K_FLOAT)||LA38_0==K_FROZEN||(LA38_0 >= K_FUNCTION && LA38_0 <= K_FUNCTIONS)||LA38_0==K_INET||(LA38_0 >= K_INITCOND && LA38_0 <= K_INPUT)||LA38_0==K_INT||(LA38_0 >= K_JSON && LA38_0 <= K_KEYS)||(LA38_0 >= K_KEYSPACES && LA38_0 <= K_LANGUAGE)||(LA38_0 >= K_LIST && LA38_0 <= K_MAP)||LA38_0==K_NOLOGIN||LA38_0==K_NOSUPERUSER||LA38_0==K_OPTIONS||(LA38_0 >= K_PASSWORD && LA38_0 <= K_PERMISSIONS)||LA38_0==K_RETURNS||(LA38_0 >= K_ROLE && LA38_0 <= K_ROLES)||(LA38_0 >= K_SFUNC && LA38_0 <= K_TINYINT)||LA38_0==K_TRIGGER||(LA38_0 >= K_TTL && LA38_0 <= K_TYPE)||(LA38_0 >= K_USER && LA38_0 <= K_USERS)||(LA38_0 >= K_UUID && LA38_0 <= K_VARINT)||LA38_0==K_WRITETIME||LA38_0==QUOTED_NAME) ) {
						alt38=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 38, 0, input);
						throw nvae;
					}

					switch (alt38) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:481:16: K_EXISTS
							{
							match(input,K_EXISTS,FOLLOW_K_EXISTS_in_deleteStatement2458); 
							 ifExists = true; 
							}
							break;
						case 2 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:481:48: conditions= updateConditions
							{
							pushFollow(FOLLOW_updateConditions_in_deleteStatement2466);
							conditions=updateConditions();
							state._fsp--;

							}
							break;

					}

					}
					break;

			}


			          return new DeleteStatement.Parsed(cf,
			                                            attrs,
			                                            columnDeletions,
			                                            wclause.build(),
			                                            conditions == null ? Collections.<Pair<ColumnIdentifier.Raw, ColumnCondition.Raw>>emptyList() : conditions,
			                                            ifExists);
			      
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "deleteStatement"



	// $ANTLR start "deleteSelection"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:492:1: deleteSelection returns [List<Operation.RawDeletion> operations] :t1= deleteOp ( ',' tN= deleteOp )* ;
	public final List<Operation.RawDeletion> deleteSelection() throws RecognitionException {
		List<Operation.RawDeletion> operations = null;


		Operation.RawDeletion t1 =null;
		Operation.RawDeletion tN =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:493:5: (t1= deleteOp ( ',' tN= deleteOp )* )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:493:7: t1= deleteOp ( ',' tN= deleteOp )*
			{
			 operations = new ArrayList<Operation.RawDeletion>(); 
			pushFollow(FOLLOW_deleteOp_in_deleteSelection2513);
			t1=deleteOp();
			state._fsp--;

			 operations.add(t1); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:495:11: ( ',' tN= deleteOp )*
			loop40:
			while (true) {
				int alt40=2;
				int LA40_0 = input.LA(1);
				if ( (LA40_0==174) ) {
					alt40=1;
				}

				switch (alt40) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:495:12: ',' tN= deleteOp
					{
					match(input,174,FOLLOW_174_in_deleteSelection2528); 
					pushFollow(FOLLOW_deleteOp_in_deleteSelection2532);
					tN=deleteOp();
					state._fsp--;

					 operations.add(tN); 
					}
					break;

				default :
					break loop40;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return operations;
	}
	// $ANTLR end "deleteSelection"



	// $ANTLR start "deleteOp"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:498:1: deleteOp returns [Operation.RawDeletion op] : (c= cident |c= cident '[' t= term ']' );
	public final Operation.RawDeletion deleteOp() throws RecognitionException {
		Operation.RawDeletion op = null;


		ColumnIdentifier.Raw c =null;
		Term.Raw t =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:499:5: (c= cident |c= cident '[' t= term ']' )
			int alt41=2;
			alt41 = dfa41.predict(input);
			switch (alt41) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:499:7: c= cident
					{
					pushFollow(FOLLOW_cident_in_deleteOp2559);
					c=cident();
					state._fsp--;

					 op = new Operation.ColumnDeletion(c); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:500:7: c= cident '[' t= term ']'
					{
					pushFollow(FOLLOW_cident_in_deleteOp2586);
					c=cident();
					state._fsp--;

					match(input,184,FOLLOW_184_in_deleteOp2588); 
					pushFollow(FOLLOW_term_in_deleteOp2592);
					t=term();
					state._fsp--;

					match(input,186,FOLLOW_186_in_deleteOp2594); 
					 op = new Operation.ElementDeletion(c, t); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return op;
	}
	// $ANTLR end "deleteOp"



	// $ANTLR start "usingClauseDelete"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:503:1: usingClauseDelete[Attributes.Raw attrs] : K_USING K_TIMESTAMP ts= intValue ;
	public final void usingClauseDelete(Attributes.Raw attrs) throws RecognitionException {
		Term.Raw ts =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:504:5: ( K_USING K_TIMESTAMP ts= intValue )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:504:7: K_USING K_TIMESTAMP ts= intValue
			{
			match(input,K_USING,FOLLOW_K_USING_in_usingClauseDelete2614); 
			match(input,K_TIMESTAMP,FOLLOW_K_TIMESTAMP_in_usingClauseDelete2616); 
			pushFollow(FOLLOW_intValue_in_usingClauseDelete2620);
			ts=intValue();
			state._fsp--;

			 attrs.timestamp = ts; 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "usingClauseDelete"



	// $ANTLR start "batchStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:531:1: batchStatement returns [BatchStatement.Parsed expr] : K_BEGIN ( K_UNLOGGED | K_COUNTER )? K_BATCH ( usingClause[attrs] )? (s= batchStatementObjective ( ';' )? )* K_APPLY K_BATCH ;
	public final BatchStatement.Parsed batchStatement() throws RecognitionException {
		BatchStatement.Parsed expr = null;


		ModificationStatement.Parsed s =null;


		        BatchStatement.Type type = BatchStatement.Type.LOGGED;
		        List<ModificationStatement.Parsed> statements = new ArrayList<ModificationStatement.Parsed>();
		        Attributes.Raw attrs = new Attributes.Raw();
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:537:5: ( K_BEGIN ( K_UNLOGGED | K_COUNTER )? K_BATCH ( usingClause[attrs] )? (s= batchStatementObjective ( ';' )? )* K_APPLY K_BATCH )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:537:7: K_BEGIN ( K_UNLOGGED | K_COUNTER )? K_BATCH ( usingClause[attrs] )? (s= batchStatementObjective ( ';' )? )* K_APPLY K_BATCH
			{
			match(input,K_BEGIN,FOLLOW_K_BEGIN_in_batchStatement2654); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:538:7: ( K_UNLOGGED | K_COUNTER )?
			int alt42=3;
			int LA42_0 = input.LA(1);
			if ( (LA42_0==K_UNLOGGED) ) {
				alt42=1;
			}
			else if ( (LA42_0==K_COUNTER) ) {
				alt42=2;
			}
			switch (alt42) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:538:9: K_UNLOGGED
					{
					match(input,K_UNLOGGED,FOLLOW_K_UNLOGGED_in_batchStatement2664); 
					 type = BatchStatement.Type.UNLOGGED; 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:538:63: K_COUNTER
					{
					match(input,K_COUNTER,FOLLOW_K_COUNTER_in_batchStatement2670); 
					 type = BatchStatement.Type.COUNTER; 
					}
					break;

			}

			match(input,K_BATCH,FOLLOW_K_BATCH_in_batchStatement2683); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:539:15: ( usingClause[attrs] )?
			int alt43=2;
			int LA43_0 = input.LA(1);
			if ( (LA43_0==K_USING) ) {
				alt43=1;
			}
			switch (alt43) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:539:17: usingClause[attrs]
					{
					pushFollow(FOLLOW_usingClause_in_batchStatement2687);
					usingClause(attrs);
					state._fsp--;

					}
					break;

			}

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:540:11: (s= batchStatementObjective ( ';' )? )*
			loop45:
			while (true) {
				int alt45=2;
				int LA45_0 = input.LA(1);
				if ( (LA45_0==K_DELETE||LA45_0==K_INSERT||LA45_0==K_UPDATE) ) {
					alt45=1;
				}

				switch (alt45) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:540:13: s= batchStatementObjective ( ';' )?
					{
					pushFollow(FOLLOW_batchStatementObjective_in_batchStatement2707);
					s=batchStatementObjective();
					state._fsp--;

					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:540:39: ( ';' )?
					int alt44=2;
					int LA44_0 = input.LA(1);
					if ( (LA44_0==178) ) {
						alt44=1;
					}
					switch (alt44) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:540:39: ';'
							{
							match(input,178,FOLLOW_178_in_batchStatement2709); 
							}
							break;

					}

					 statements.add(s); 
					}
					break;

				default :
					break loop45;
				}
			}

			match(input,K_APPLY,FOLLOW_K_APPLY_in_batchStatement2723); 
			match(input,K_BATCH,FOLLOW_K_BATCH_in_batchStatement2725); 

			          return new BatchStatement.Parsed(type, attrs, statements);
			      
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "batchStatement"



	// $ANTLR start "batchStatementObjective"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:547:1: batchStatementObjective returns [ModificationStatement.Parsed statement] : (i= insertStatement |u= updateStatement |d= deleteStatement );
	public final ModificationStatement.Parsed batchStatementObjective() throws RecognitionException {
		ModificationStatement.Parsed statement = null;


		ModificationStatement.Parsed i =null;
		UpdateStatement.ParsedUpdate u =null;
		DeleteStatement.Parsed d =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:548:5: (i= insertStatement |u= updateStatement |d= deleteStatement )
			int alt46=3;
			switch ( input.LA(1) ) {
			case K_INSERT:
				{
				alt46=1;
				}
				break;
			case K_UPDATE:
				{
				alt46=2;
				}
				break;
			case K_DELETE:
				{
				alt46=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 46, 0, input);
				throw nvae;
			}
			switch (alt46) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:548:7: i= insertStatement
					{
					pushFollow(FOLLOW_insertStatement_in_batchStatementObjective2756);
					i=insertStatement();
					state._fsp--;

					 statement = i; 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:549:7: u= updateStatement
					{
					pushFollow(FOLLOW_updateStatement_in_batchStatementObjective2769);
					u=updateStatement();
					state._fsp--;

					 statement = u; 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:550:7: d= deleteStatement
					{
					pushFollow(FOLLOW_deleteStatement_in_batchStatementObjective2782);
					d=deleteStatement();
					state._fsp--;

					 statement = d; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return statement;
	}
	// $ANTLR end "batchStatementObjective"



	// $ANTLR start "createAggregateStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:553:1: createAggregateStatement returns [CreateAggregateStatement expr] : K_CREATE ( K_OR K_REPLACE )? K_AGGREGATE ( K_IF K_NOT K_EXISTS )? fn= functionName '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' K_SFUNC sfunc= allowedFunctionName K_STYPE stype= comparatorType ( K_FINALFUNC ffunc= allowedFunctionName )? ( K_INITCOND ival= term )? ;
	public final CreateAggregateStatement createAggregateStatement() throws RecognitionException {
		CreateAggregateStatement expr = null;


		FunctionName fn =null;
		CQL3Type.Raw v =null;
		String sfunc =null;
		CQL3Type.Raw stype =null;
		String ffunc =null;
		Term.Raw ival =null;


		        boolean orReplace = false;
		        boolean ifNotExists = false;

		        List<CQL3Type.Raw> argsTypes = new ArrayList<>();
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:560:5: ( K_CREATE ( K_OR K_REPLACE )? K_AGGREGATE ( K_IF K_NOT K_EXISTS )? fn= functionName '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' K_SFUNC sfunc= allowedFunctionName K_STYPE stype= comparatorType ( K_FINALFUNC ffunc= allowedFunctionName )? ( K_INITCOND ival= term )? )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:560:7: K_CREATE ( K_OR K_REPLACE )? K_AGGREGATE ( K_IF K_NOT K_EXISTS )? fn= functionName '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' K_SFUNC sfunc= allowedFunctionName K_STYPE stype= comparatorType ( K_FINALFUNC ffunc= allowedFunctionName )? ( K_INITCOND ival= term )?
			{
			match(input,K_CREATE,FOLLOW_K_CREATE_in_createAggregateStatement2815); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:560:16: ( K_OR K_REPLACE )?
			int alt47=2;
			int LA47_0 = input.LA(1);
			if ( (LA47_0==K_OR) ) {
				alt47=1;
			}
			switch (alt47) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:560:17: K_OR K_REPLACE
					{
					match(input,K_OR,FOLLOW_K_OR_in_createAggregateStatement2818); 
					match(input,K_REPLACE,FOLLOW_K_REPLACE_in_createAggregateStatement2820); 
					 orReplace = true; 
					}
					break;

			}

			match(input,K_AGGREGATE,FOLLOW_K_AGGREGATE_in_createAggregateStatement2832); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:562:7: ( K_IF K_NOT K_EXISTS )?
			int alt48=2;
			int LA48_0 = input.LA(1);
			if ( (LA48_0==K_IF) ) {
				alt48=1;
			}
			switch (alt48) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:562:8: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_createAggregateStatement2841); 
					match(input,K_NOT,FOLLOW_K_NOT_in_createAggregateStatement2843); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createAggregateStatement2845); 
					 ifNotExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_functionName_in_createAggregateStatement2859);
			fn=functionName();
			state._fsp--;

			match(input,171,FOLLOW_171_in_createAggregateStatement2867); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:565:9: (v= comparatorType ( ',' v= comparatorType )* )?
			int alt50=2;
			int LA50_0 = input.LA(1);
			if ( (LA50_0==IDENT||(LA50_0 >= K_AGGREGATE && LA50_0 <= K_ALL)||LA50_0==K_AS||LA50_0==K_ASCII||(LA50_0 >= K_BIGINT && LA50_0 <= K_BOOLEAN)||(LA50_0 >= K_CALLED && LA50_0 <= K_CLUSTERING)||(LA50_0 >= K_COMPACT && LA50_0 <= K_COUNTER)||(LA50_0 >= K_CUSTOM && LA50_0 <= K_DECIMAL)||(LA50_0 >= K_DISTINCT && LA50_0 <= K_DOUBLE)||(LA50_0 >= K_EXISTS && LA50_0 <= K_FLOAT)||LA50_0==K_FROZEN||(LA50_0 >= K_FUNCTION && LA50_0 <= K_FUNCTIONS)||LA50_0==K_INET||(LA50_0 >= K_INITCOND && LA50_0 <= K_INPUT)||LA50_0==K_INT||(LA50_0 >= K_JSON && LA50_0 <= K_KEYS)||(LA50_0 >= K_KEYSPACES && LA50_0 <= K_LANGUAGE)||(LA50_0 >= K_LIST && LA50_0 <= K_MAP)||LA50_0==K_NOLOGIN||LA50_0==K_NOSUPERUSER||LA50_0==K_OPTIONS||(LA50_0 >= K_PASSWORD && LA50_0 <= K_PERMISSIONS)||LA50_0==K_RETURNS||(LA50_0 >= K_ROLE && LA50_0 <= K_ROLES)||(LA50_0 >= K_SET && LA50_0 <= K_TINYINT)||LA50_0==K_TRIGGER||(LA50_0 >= K_TTL && LA50_0 <= K_TYPE)||(LA50_0 >= K_USER && LA50_0 <= K_USERS)||(LA50_0 >= K_UUID && LA50_0 <= K_VARINT)||LA50_0==K_WRITETIME||LA50_0==QUOTED_NAME||LA50_0==STRING_LITERAL) ) {
				alt50=1;
			}
			switch (alt50) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:566:11: v= comparatorType ( ',' v= comparatorType )*
					{
					pushFollow(FOLLOW_comparatorType_in_createAggregateStatement2891);
					v=comparatorType();
					state._fsp--;

					 argsTypes.add(v); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:567:11: ( ',' v= comparatorType )*
					loop49:
					while (true) {
						int alt49=2;
						int LA49_0 = input.LA(1);
						if ( (LA49_0==174) ) {
							alt49=1;
						}

						switch (alt49) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:567:13: ',' v= comparatorType
							{
							match(input,174,FOLLOW_174_in_createAggregateStatement2907); 
							pushFollow(FOLLOW_comparatorType_in_createAggregateStatement2911);
							v=comparatorType();
							state._fsp--;

							 argsTypes.add(v); 
							}
							break;

						default :
							break loop49;
						}
					}

					}
					break;

			}

			match(input,172,FOLLOW_172_in_createAggregateStatement2935); 
			match(input,K_SFUNC,FOLLOW_K_SFUNC_in_createAggregateStatement2943); 
			pushFollow(FOLLOW_allowedFunctionName_in_createAggregateStatement2949);
			sfunc=allowedFunctionName();
			state._fsp--;

			match(input,K_STYPE,FOLLOW_K_STYPE_in_createAggregateStatement2957); 
			pushFollow(FOLLOW_comparatorType_in_createAggregateStatement2963);
			stype=comparatorType();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:572:7: ( K_FINALFUNC ffunc= allowedFunctionName )?
			int alt51=2;
			int LA51_0 = input.LA(1);
			if ( (LA51_0==K_FINALFUNC) ) {
				alt51=1;
			}
			switch (alt51) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:573:9: K_FINALFUNC ffunc= allowedFunctionName
					{
					match(input,K_FINALFUNC,FOLLOW_K_FINALFUNC_in_createAggregateStatement2981); 
					pushFollow(FOLLOW_allowedFunctionName_in_createAggregateStatement2987);
					ffunc=allowedFunctionName();
					state._fsp--;

					}
					break;

			}

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:575:7: ( K_INITCOND ival= term )?
			int alt52=2;
			int LA52_0 = input.LA(1);
			if ( (LA52_0==K_INITCOND) ) {
				alt52=1;
			}
			switch (alt52) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:576:9: K_INITCOND ival= term
					{
					match(input,K_INITCOND,FOLLOW_K_INITCOND_in_createAggregateStatement3014); 
					pushFollow(FOLLOW_term_in_createAggregateStatement3020);
					ival=term();
					state._fsp--;

					}
					break;

			}

			 expr = new CreateAggregateStatement(fn, argsTypes, sfunc, stype, ffunc, ival, orReplace, ifNotExists); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "createAggregateStatement"



	// $ANTLR start "dropAggregateStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:581:1: dropAggregateStatement returns [DropAggregateStatement expr] : K_DROP K_AGGREGATE ( K_IF K_EXISTS )? fn= functionName ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )? ;
	public final DropAggregateStatement dropAggregateStatement() throws RecognitionException {
		DropAggregateStatement expr = null;


		FunctionName fn =null;
		CQL3Type.Raw v =null;


		        boolean ifExists = false;
		        List<CQL3Type.Raw> argsTypes = new ArrayList<>();
		        boolean argsPresent = false;
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:587:5: ( K_DROP K_AGGREGATE ( K_IF K_EXISTS )? fn= functionName ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )? )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:587:7: K_DROP K_AGGREGATE ( K_IF K_EXISTS )? fn= functionName ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )?
			{
			match(input,K_DROP,FOLLOW_K_DROP_in_dropAggregateStatement3067); 
			match(input,K_AGGREGATE,FOLLOW_K_AGGREGATE_in_dropAggregateStatement3069); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:588:7: ( K_IF K_EXISTS )?
			int alt53=2;
			int LA53_0 = input.LA(1);
			if ( (LA53_0==K_IF) ) {
				alt53=1;
			}
			switch (alt53) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:588:8: K_IF K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_dropAggregateStatement3078); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropAggregateStatement3080); 
					 ifExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_functionName_in_dropAggregateStatement3095);
			fn=functionName();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:590:7: ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )?
			int alt56=2;
			int LA56_0 = input.LA(1);
			if ( (LA56_0==171) ) {
				alt56=1;
			}
			switch (alt56) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:591:9: '(' (v= comparatorType ( ',' v= comparatorType )* )? ')'
					{
					match(input,171,FOLLOW_171_in_dropAggregateStatement3113); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:592:11: (v= comparatorType ( ',' v= comparatorType )* )?
					int alt55=2;
					int LA55_0 = input.LA(1);
					if ( (LA55_0==IDENT||(LA55_0 >= K_AGGREGATE && LA55_0 <= K_ALL)||LA55_0==K_AS||LA55_0==K_ASCII||(LA55_0 >= K_BIGINT && LA55_0 <= K_BOOLEAN)||(LA55_0 >= K_CALLED && LA55_0 <= K_CLUSTERING)||(LA55_0 >= K_COMPACT && LA55_0 <= K_COUNTER)||(LA55_0 >= K_CUSTOM && LA55_0 <= K_DECIMAL)||(LA55_0 >= K_DISTINCT && LA55_0 <= K_DOUBLE)||(LA55_0 >= K_EXISTS && LA55_0 <= K_FLOAT)||LA55_0==K_FROZEN||(LA55_0 >= K_FUNCTION && LA55_0 <= K_FUNCTIONS)||LA55_0==K_INET||(LA55_0 >= K_INITCOND && LA55_0 <= K_INPUT)||LA55_0==K_INT||(LA55_0 >= K_JSON && LA55_0 <= K_KEYS)||(LA55_0 >= K_KEYSPACES && LA55_0 <= K_LANGUAGE)||(LA55_0 >= K_LIST && LA55_0 <= K_MAP)||LA55_0==K_NOLOGIN||LA55_0==K_NOSUPERUSER||LA55_0==K_OPTIONS||(LA55_0 >= K_PASSWORD && LA55_0 <= K_PERMISSIONS)||LA55_0==K_RETURNS||(LA55_0 >= K_ROLE && LA55_0 <= K_ROLES)||(LA55_0 >= K_SET && LA55_0 <= K_TINYINT)||LA55_0==K_TRIGGER||(LA55_0 >= K_TTL && LA55_0 <= K_TYPE)||(LA55_0 >= K_USER && LA55_0 <= K_USERS)||(LA55_0 >= K_UUID && LA55_0 <= K_VARINT)||LA55_0==K_WRITETIME||LA55_0==QUOTED_NAME||LA55_0==STRING_LITERAL) ) {
						alt55=1;
					}
					switch (alt55) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:593:13: v= comparatorType ( ',' v= comparatorType )*
							{
							pushFollow(FOLLOW_comparatorType_in_dropAggregateStatement3141);
							v=comparatorType();
							state._fsp--;

							 argsTypes.add(v); 
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:594:13: ( ',' v= comparatorType )*
							loop54:
							while (true) {
								int alt54=2;
								int LA54_0 = input.LA(1);
								if ( (LA54_0==174) ) {
									alt54=1;
								}

								switch (alt54) {
								case 1 :
									// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:594:15: ',' v= comparatorType
									{
									match(input,174,FOLLOW_174_in_dropAggregateStatement3159); 
									pushFollow(FOLLOW_comparatorType_in_dropAggregateStatement3163);
									v=comparatorType();
									state._fsp--;

									 argsTypes.add(v); 
									}
									break;

								default :
									break loop54;
								}
							}

							}
							break;

					}

					match(input,172,FOLLOW_172_in_dropAggregateStatement3191); 
					 argsPresent = true; 
					}
					break;

			}

			 expr = new DropAggregateStatement(fn, argsTypes, argsPresent, ifExists); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "dropAggregateStatement"



	// $ANTLR start "createFunctionStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:602:1: createFunctionStatement returns [CreateFunctionStatement expr] : K_CREATE ( K_OR K_REPLACE )? K_FUNCTION ( K_IF K_NOT K_EXISTS )? fn= functionName '(' (k= noncol_ident v= comparatorType ( ',' k= noncol_ident v= comparatorType )* )? ')' ( ( K_RETURNS K_NULL ) | ( K_CALLED ) ) K_ON K_NULL K_INPUT K_RETURNS rt= comparatorType K_LANGUAGE language= IDENT K_AS body= STRING_LITERAL ;
	public final CreateFunctionStatement createFunctionStatement() throws RecognitionException {
		CreateFunctionStatement expr = null;


		Token language=null;
		Token body=null;
		FunctionName fn =null;
		ColumnIdentifier k =null;
		CQL3Type.Raw v =null;
		CQL3Type.Raw rt =null;


		        boolean orReplace = false;
		        boolean ifNotExists = false;

		        List<ColumnIdentifier> argsNames = new ArrayList<>();
		        List<CQL3Type.Raw> argsTypes = new ArrayList<>();
		        boolean calledOnNullInput = false;
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:611:5: ( K_CREATE ( K_OR K_REPLACE )? K_FUNCTION ( K_IF K_NOT K_EXISTS )? fn= functionName '(' (k= noncol_ident v= comparatorType ( ',' k= noncol_ident v= comparatorType )* )? ')' ( ( K_RETURNS K_NULL ) | ( K_CALLED ) ) K_ON K_NULL K_INPUT K_RETURNS rt= comparatorType K_LANGUAGE language= IDENT K_AS body= STRING_LITERAL )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:611:7: K_CREATE ( K_OR K_REPLACE )? K_FUNCTION ( K_IF K_NOT K_EXISTS )? fn= functionName '(' (k= noncol_ident v= comparatorType ( ',' k= noncol_ident v= comparatorType )* )? ')' ( ( K_RETURNS K_NULL ) | ( K_CALLED ) ) K_ON K_NULL K_INPUT K_RETURNS rt= comparatorType K_LANGUAGE language= IDENT K_AS body= STRING_LITERAL
			{
			match(input,K_CREATE,FOLLOW_K_CREATE_in_createFunctionStatement3248); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:611:16: ( K_OR K_REPLACE )?
			int alt57=2;
			int LA57_0 = input.LA(1);
			if ( (LA57_0==K_OR) ) {
				alt57=1;
			}
			switch (alt57) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:611:17: K_OR K_REPLACE
					{
					match(input,K_OR,FOLLOW_K_OR_in_createFunctionStatement3251); 
					match(input,K_REPLACE,FOLLOW_K_REPLACE_in_createFunctionStatement3253); 
					 orReplace = true; 
					}
					break;

			}

			match(input,K_FUNCTION,FOLLOW_K_FUNCTION_in_createFunctionStatement3265); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:613:7: ( K_IF K_NOT K_EXISTS )?
			int alt58=2;
			int LA58_0 = input.LA(1);
			if ( (LA58_0==K_IF) ) {
				alt58=1;
			}
			switch (alt58) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:613:8: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_createFunctionStatement3274); 
					match(input,K_NOT,FOLLOW_K_NOT_in_createFunctionStatement3276); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createFunctionStatement3278); 
					 ifNotExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_functionName_in_createFunctionStatement3292);
			fn=functionName();
			state._fsp--;

			match(input,171,FOLLOW_171_in_createFunctionStatement3300); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:616:9: (k= noncol_ident v= comparatorType ( ',' k= noncol_ident v= comparatorType )* )?
			int alt60=2;
			int LA60_0 = input.LA(1);
			if ( (LA60_0==IDENT||(LA60_0 >= K_AGGREGATE && LA60_0 <= K_ALL)||LA60_0==K_AS||LA60_0==K_ASCII||(LA60_0 >= K_BIGINT && LA60_0 <= K_BOOLEAN)||(LA60_0 >= K_CALLED && LA60_0 <= K_CLUSTERING)||(LA60_0 >= K_COMPACT && LA60_0 <= K_COUNTER)||(LA60_0 >= K_CUSTOM && LA60_0 <= K_DECIMAL)||(LA60_0 >= K_DISTINCT && LA60_0 <= K_DOUBLE)||(LA60_0 >= K_EXISTS && LA60_0 <= K_FLOAT)||LA60_0==K_FROZEN||(LA60_0 >= K_FUNCTION && LA60_0 <= K_FUNCTIONS)||LA60_0==K_INET||(LA60_0 >= K_INITCOND && LA60_0 <= K_INPUT)||LA60_0==K_INT||(LA60_0 >= K_JSON && LA60_0 <= K_KEYS)||(LA60_0 >= K_KEYSPACES && LA60_0 <= K_LANGUAGE)||(LA60_0 >= K_LIST && LA60_0 <= K_MAP)||LA60_0==K_NOLOGIN||LA60_0==K_NOSUPERUSER||LA60_0==K_OPTIONS||(LA60_0 >= K_PASSWORD && LA60_0 <= K_PERMISSIONS)||LA60_0==K_RETURNS||(LA60_0 >= K_ROLE && LA60_0 <= K_ROLES)||(LA60_0 >= K_SFUNC && LA60_0 <= K_TINYINT)||LA60_0==K_TRIGGER||(LA60_0 >= K_TTL && LA60_0 <= K_TYPE)||(LA60_0 >= K_USER && LA60_0 <= K_USERS)||(LA60_0 >= K_UUID && LA60_0 <= K_VARINT)||LA60_0==K_WRITETIME||LA60_0==QUOTED_NAME) ) {
				alt60=1;
			}
			switch (alt60) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:617:11: k= noncol_ident v= comparatorType ( ',' k= noncol_ident v= comparatorType )*
					{
					pushFollow(FOLLOW_noncol_ident_in_createFunctionStatement3324);
					k=noncol_ident();
					state._fsp--;

					pushFollow(FOLLOW_comparatorType_in_createFunctionStatement3328);
					v=comparatorType();
					state._fsp--;

					 argsNames.add(k); argsTypes.add(v); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:618:11: ( ',' k= noncol_ident v= comparatorType )*
					loop59:
					while (true) {
						int alt59=2;
						int LA59_0 = input.LA(1);
						if ( (LA59_0==174) ) {
							alt59=1;
						}

						switch (alt59) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:618:13: ',' k= noncol_ident v= comparatorType
							{
							match(input,174,FOLLOW_174_in_createFunctionStatement3344); 
							pushFollow(FOLLOW_noncol_ident_in_createFunctionStatement3348);
							k=noncol_ident();
							state._fsp--;

							pushFollow(FOLLOW_comparatorType_in_createFunctionStatement3352);
							v=comparatorType();
							state._fsp--;

							 argsNames.add(k); argsTypes.add(v); 
							}
							break;

						default :
							break loop59;
						}
					}

					}
					break;

			}

			match(input,172,FOLLOW_172_in_createFunctionStatement3376); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:621:7: ( ( K_RETURNS K_NULL ) | ( K_CALLED ) )
			int alt61=2;
			int LA61_0 = input.LA(1);
			if ( (LA61_0==K_RETURNS) ) {
				alt61=1;
			}
			else if ( (LA61_0==K_CALLED) ) {
				alt61=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 61, 0, input);
				throw nvae;
			}

			switch (alt61) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:621:9: ( K_RETURNS K_NULL )
					{
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:621:9: ( K_RETURNS K_NULL )
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:621:10: K_RETURNS K_NULL
					{
					match(input,K_RETURNS,FOLLOW_K_RETURNS_in_createFunctionStatement3387); 
					match(input,K_NULL,FOLLOW_K_NULL_in_createFunctionStatement3389); 
					}

					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:621:30: ( K_CALLED )
					{
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:621:30: ( K_CALLED )
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:621:31: K_CALLED
					{
					match(input,K_CALLED,FOLLOW_K_CALLED_in_createFunctionStatement3395); 
					 calledOnNullInput=true; 
					}

					}
					break;

			}

			match(input,K_ON,FOLLOW_K_ON_in_createFunctionStatement3401); 
			match(input,K_NULL,FOLLOW_K_NULL_in_createFunctionStatement3403); 
			match(input,K_INPUT,FOLLOW_K_INPUT_in_createFunctionStatement3405); 
			match(input,K_RETURNS,FOLLOW_K_RETURNS_in_createFunctionStatement3413); 
			pushFollow(FOLLOW_comparatorType_in_createFunctionStatement3419);
			rt=comparatorType();
			state._fsp--;

			match(input,K_LANGUAGE,FOLLOW_K_LANGUAGE_in_createFunctionStatement3427); 
			language=(Token)match(input,IDENT,FOLLOW_IDENT_in_createFunctionStatement3433); 
			match(input,K_AS,FOLLOW_K_AS_in_createFunctionStatement3441); 
			body=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_createFunctionStatement3447); 
			 expr = new CreateFunctionStatement(fn, (language!=null?language.getText():null).toLowerCase(), (body!=null?body.getText():null),
			                                            argsNames, argsTypes, rt, calledOnNullInput, orReplace, ifNotExists); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "createFunctionStatement"



	// $ANTLR start "dropFunctionStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:629:1: dropFunctionStatement returns [DropFunctionStatement expr] : K_DROP K_FUNCTION ( K_IF K_EXISTS )? fn= functionName ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )? ;
	public final DropFunctionStatement dropFunctionStatement() throws RecognitionException {
		DropFunctionStatement expr = null;


		FunctionName fn =null;
		CQL3Type.Raw v =null;


		        boolean ifExists = false;
		        List<CQL3Type.Raw> argsTypes = new ArrayList<>();
		        boolean argsPresent = false;
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:635:5: ( K_DROP K_FUNCTION ( K_IF K_EXISTS )? fn= functionName ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )? )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:635:7: K_DROP K_FUNCTION ( K_IF K_EXISTS )? fn= functionName ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )?
			{
			match(input,K_DROP,FOLLOW_K_DROP_in_dropFunctionStatement3485); 
			match(input,K_FUNCTION,FOLLOW_K_FUNCTION_in_dropFunctionStatement3487); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:636:7: ( K_IF K_EXISTS )?
			int alt62=2;
			int LA62_0 = input.LA(1);
			if ( (LA62_0==K_IF) ) {
				alt62=1;
			}
			switch (alt62) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:636:8: K_IF K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_dropFunctionStatement3496); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropFunctionStatement3498); 
					 ifExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_functionName_in_dropFunctionStatement3513);
			fn=functionName();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:638:7: ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )?
			int alt65=2;
			int LA65_0 = input.LA(1);
			if ( (LA65_0==171) ) {
				alt65=1;
			}
			switch (alt65) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:639:9: '(' (v= comparatorType ( ',' v= comparatorType )* )? ')'
					{
					match(input,171,FOLLOW_171_in_dropFunctionStatement3531); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:640:11: (v= comparatorType ( ',' v= comparatorType )* )?
					int alt64=2;
					int LA64_0 = input.LA(1);
					if ( (LA64_0==IDENT||(LA64_0 >= K_AGGREGATE && LA64_0 <= K_ALL)||LA64_0==K_AS||LA64_0==K_ASCII||(LA64_0 >= K_BIGINT && LA64_0 <= K_BOOLEAN)||(LA64_0 >= K_CALLED && LA64_0 <= K_CLUSTERING)||(LA64_0 >= K_COMPACT && LA64_0 <= K_COUNTER)||(LA64_0 >= K_CUSTOM && LA64_0 <= K_DECIMAL)||(LA64_0 >= K_DISTINCT && LA64_0 <= K_DOUBLE)||(LA64_0 >= K_EXISTS && LA64_0 <= K_FLOAT)||LA64_0==K_FROZEN||(LA64_0 >= K_FUNCTION && LA64_0 <= K_FUNCTIONS)||LA64_0==K_INET||(LA64_0 >= K_INITCOND && LA64_0 <= K_INPUT)||LA64_0==K_INT||(LA64_0 >= K_JSON && LA64_0 <= K_KEYS)||(LA64_0 >= K_KEYSPACES && LA64_0 <= K_LANGUAGE)||(LA64_0 >= K_LIST && LA64_0 <= K_MAP)||LA64_0==K_NOLOGIN||LA64_0==K_NOSUPERUSER||LA64_0==K_OPTIONS||(LA64_0 >= K_PASSWORD && LA64_0 <= K_PERMISSIONS)||LA64_0==K_RETURNS||(LA64_0 >= K_ROLE && LA64_0 <= K_ROLES)||(LA64_0 >= K_SET && LA64_0 <= K_TINYINT)||LA64_0==K_TRIGGER||(LA64_0 >= K_TTL && LA64_0 <= K_TYPE)||(LA64_0 >= K_USER && LA64_0 <= K_USERS)||(LA64_0 >= K_UUID && LA64_0 <= K_VARINT)||LA64_0==K_WRITETIME||LA64_0==QUOTED_NAME||LA64_0==STRING_LITERAL) ) {
						alt64=1;
					}
					switch (alt64) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:641:13: v= comparatorType ( ',' v= comparatorType )*
							{
							pushFollow(FOLLOW_comparatorType_in_dropFunctionStatement3559);
							v=comparatorType();
							state._fsp--;

							 argsTypes.add(v); 
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:642:13: ( ',' v= comparatorType )*
							loop63:
							while (true) {
								int alt63=2;
								int LA63_0 = input.LA(1);
								if ( (LA63_0==174) ) {
									alt63=1;
								}

								switch (alt63) {
								case 1 :
									// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:642:15: ',' v= comparatorType
									{
									match(input,174,FOLLOW_174_in_dropFunctionStatement3577); 
									pushFollow(FOLLOW_comparatorType_in_dropFunctionStatement3581);
									v=comparatorType();
									state._fsp--;

									 argsTypes.add(v); 
									}
									break;

								default :
									break loop63;
								}
							}

							}
							break;

					}

					match(input,172,FOLLOW_172_in_dropFunctionStatement3609); 
					 argsPresent = true; 
					}
					break;

			}

			 expr = new DropFunctionStatement(fn, argsTypes, argsPresent, ifExists); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "dropFunctionStatement"



	// $ANTLR start "createKeyspaceStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:653:1: createKeyspaceStatement returns [CreateKeyspaceStatement expr] : K_CREATE K_KEYSPACE ( K_IF K_NOT K_EXISTS )? ks= keyspaceName K_WITH properties[attrs] ;
	public final CreateKeyspaceStatement createKeyspaceStatement() throws RecognitionException {
		CreateKeyspaceStatement expr = null;


		String ks =null;


		        KeyspaceAttributes attrs = new KeyspaceAttributes();
		        boolean ifNotExists = false;
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:658:5: ( K_CREATE K_KEYSPACE ( K_IF K_NOT K_EXISTS )? ks= keyspaceName K_WITH properties[attrs] )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:658:7: K_CREATE K_KEYSPACE ( K_IF K_NOT K_EXISTS )? ks= keyspaceName K_WITH properties[attrs]
			{
			match(input,K_CREATE,FOLLOW_K_CREATE_in_createKeyspaceStatement3668); 
			match(input,K_KEYSPACE,FOLLOW_K_KEYSPACE_in_createKeyspaceStatement3670); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:658:27: ( K_IF K_NOT K_EXISTS )?
			int alt66=2;
			int LA66_0 = input.LA(1);
			if ( (LA66_0==K_IF) ) {
				alt66=1;
			}
			switch (alt66) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:658:28: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_createKeyspaceStatement3673); 
					match(input,K_NOT,FOLLOW_K_NOT_in_createKeyspaceStatement3675); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createKeyspaceStatement3677); 
					 ifNotExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_keyspaceName_in_createKeyspaceStatement3686);
			ks=keyspaceName();
			state._fsp--;

			match(input,K_WITH,FOLLOW_K_WITH_in_createKeyspaceStatement3694); 
			pushFollow(FOLLOW_properties_in_createKeyspaceStatement3696);
			properties(attrs);
			state._fsp--;

			 expr = new CreateKeyspaceStatement(ks, attrs, ifNotExists); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "createKeyspaceStatement"



	// $ANTLR start "createTableStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:669:1: createTableStatement returns [CreateTableStatement.RawStatement expr] : K_CREATE K_COLUMNFAMILY ( K_IF K_NOT K_EXISTS )? cf= columnFamilyName cfamDefinition[expr] ;
	public final CreateTableStatement.RawStatement createTableStatement() throws RecognitionException {
		CreateTableStatement.RawStatement expr = null;


		CFName cf =null;

		 boolean ifNotExists = false; 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:671:5: ( K_CREATE K_COLUMNFAMILY ( K_IF K_NOT K_EXISTS )? cf= columnFamilyName cfamDefinition[expr] )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:671:7: K_CREATE K_COLUMNFAMILY ( K_IF K_NOT K_EXISTS )? cf= columnFamilyName cfamDefinition[expr]
			{
			match(input,K_CREATE,FOLLOW_K_CREATE_in_createTableStatement3731); 
			match(input,K_COLUMNFAMILY,FOLLOW_K_COLUMNFAMILY_in_createTableStatement3733); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:671:31: ( K_IF K_NOT K_EXISTS )?
			int alt67=2;
			int LA67_0 = input.LA(1);
			if ( (LA67_0==K_IF) ) {
				alt67=1;
			}
			switch (alt67) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:671:32: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_createTableStatement3736); 
					match(input,K_NOT,FOLLOW_K_NOT_in_createTableStatement3738); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createTableStatement3740); 
					 ifNotExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_columnFamilyName_in_createTableStatement3755);
			cf=columnFamilyName();
			state._fsp--;

			 expr = new CreateTableStatement.RawStatement(cf, ifNotExists); 
			pushFollow(FOLLOW_cfamDefinition_in_createTableStatement3765);
			cfamDefinition(expr);
			state._fsp--;

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "createTableStatement"



	// $ANTLR start "cfamDefinition"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:676:1: cfamDefinition[CreateTableStatement.RawStatement expr] : '(' cfamColumns[expr] ( ',' ( cfamColumns[expr] )? )* ')' ( K_WITH cfamProperty[expr.properties] ( K_AND cfamProperty[expr.properties] )* )? ;
	public final void cfamDefinition(CreateTableStatement.RawStatement expr) throws RecognitionException {
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:677:5: ( '(' cfamColumns[expr] ( ',' ( cfamColumns[expr] )? )* ')' ( K_WITH cfamProperty[expr.properties] ( K_AND cfamProperty[expr.properties] )* )? )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:677:7: '(' cfamColumns[expr] ( ',' ( cfamColumns[expr] )? )* ')' ( K_WITH cfamProperty[expr.properties] ( K_AND cfamProperty[expr.properties] )* )?
			{
			match(input,171,FOLLOW_171_in_cfamDefinition3784); 
			pushFollow(FOLLOW_cfamColumns_in_cfamDefinition3786);
			cfamColumns(expr);
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:677:29: ( ',' ( cfamColumns[expr] )? )*
			loop69:
			while (true) {
				int alt69=2;
				int LA69_0 = input.LA(1);
				if ( (LA69_0==174) ) {
					alt69=1;
				}

				switch (alt69) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:677:31: ',' ( cfamColumns[expr] )?
					{
					match(input,174,FOLLOW_174_in_cfamDefinition3791); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:677:35: ( cfamColumns[expr] )?
					int alt68=2;
					int LA68_0 = input.LA(1);
					if ( (LA68_0==IDENT||(LA68_0 >= K_AGGREGATE && LA68_0 <= K_ALL)||LA68_0==K_AS||LA68_0==K_ASCII||(LA68_0 >= K_BIGINT && LA68_0 <= K_BOOLEAN)||(LA68_0 >= K_CALLED && LA68_0 <= K_CLUSTERING)||(LA68_0 >= K_COMPACT && LA68_0 <= K_COUNTER)||(LA68_0 >= K_CUSTOM && LA68_0 <= K_DECIMAL)||(LA68_0 >= K_DISTINCT && LA68_0 <= K_DOUBLE)||(LA68_0 >= K_EXISTS && LA68_0 <= K_FLOAT)||LA68_0==K_FROZEN||(LA68_0 >= K_FUNCTION && LA68_0 <= K_FUNCTIONS)||LA68_0==K_INET||(LA68_0 >= K_INITCOND && LA68_0 <= K_INPUT)||LA68_0==K_INT||(LA68_0 >= K_JSON && LA68_0 <= K_KEYS)||(LA68_0 >= K_KEYSPACES && LA68_0 <= K_LANGUAGE)||(LA68_0 >= K_LIST && LA68_0 <= K_MAP)||LA68_0==K_NOLOGIN||LA68_0==K_NOSUPERUSER||LA68_0==K_OPTIONS||(LA68_0 >= K_PASSWORD && LA68_0 <= K_PRIMARY)||LA68_0==K_RETURNS||(LA68_0 >= K_ROLE && LA68_0 <= K_ROLES)||(LA68_0 >= K_SFUNC && LA68_0 <= K_TINYINT)||LA68_0==K_TRIGGER||(LA68_0 >= K_TTL && LA68_0 <= K_TYPE)||(LA68_0 >= K_USER && LA68_0 <= K_USERS)||(LA68_0 >= K_UUID && LA68_0 <= K_VARINT)||LA68_0==K_WRITETIME||LA68_0==QUOTED_NAME) ) {
						alt68=1;
					}
					switch (alt68) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:677:35: cfamColumns[expr]
							{
							pushFollow(FOLLOW_cfamColumns_in_cfamDefinition3793);
							cfamColumns(expr);
							state._fsp--;

							}
							break;

					}

					}
					break;

				default :
					break loop69;
				}
			}

			match(input,172,FOLLOW_172_in_cfamDefinition3800); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:678:7: ( K_WITH cfamProperty[expr.properties] ( K_AND cfamProperty[expr.properties] )* )?
			int alt71=2;
			int LA71_0 = input.LA(1);
			if ( (LA71_0==K_WITH) ) {
				alt71=1;
			}
			switch (alt71) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:678:9: K_WITH cfamProperty[expr.properties] ( K_AND cfamProperty[expr.properties] )*
					{
					match(input,K_WITH,FOLLOW_K_WITH_in_cfamDefinition3810); 
					pushFollow(FOLLOW_cfamProperty_in_cfamDefinition3812);
					cfamProperty(expr.properties);
					state._fsp--;

					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:678:46: ( K_AND cfamProperty[expr.properties] )*
					loop70:
					while (true) {
						int alt70=2;
						int LA70_0 = input.LA(1);
						if ( (LA70_0==K_AND) ) {
							alt70=1;
						}

						switch (alt70) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:678:48: K_AND cfamProperty[expr.properties]
							{
							match(input,K_AND,FOLLOW_K_AND_in_cfamDefinition3817); 
							pushFollow(FOLLOW_cfamProperty_in_cfamDefinition3819);
							cfamProperty(expr.properties);
							state._fsp--;

							}
							break;

						default :
							break loop70;
						}
					}

					}
					break;

			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "cfamDefinition"



	// $ANTLR start "cfamColumns"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:681:1: cfamColumns[CreateTableStatement.RawStatement expr] : (k= ident v= comparatorType ( K_STATIC )? ( K_PRIMARY K_KEY )? | K_PRIMARY K_KEY '(' pkDef[expr] ( ',' c= ident )* ')' );
	public final void cfamColumns(CreateTableStatement.RawStatement expr) throws RecognitionException {
		ColumnIdentifier k =null;
		CQL3Type.Raw v =null;
		ColumnIdentifier c =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:682:5: (k= ident v= comparatorType ( K_STATIC )? ( K_PRIMARY K_KEY )? | K_PRIMARY K_KEY '(' pkDef[expr] ( ',' c= ident )* ')' )
			int alt75=2;
			int LA75_0 = input.LA(1);
			if ( (LA75_0==IDENT||(LA75_0 >= K_AGGREGATE && LA75_0 <= K_ALL)||LA75_0==K_AS||LA75_0==K_ASCII||(LA75_0 >= K_BIGINT && LA75_0 <= K_BOOLEAN)||(LA75_0 >= K_CALLED && LA75_0 <= K_CLUSTERING)||(LA75_0 >= K_COMPACT && LA75_0 <= K_COUNTER)||(LA75_0 >= K_CUSTOM && LA75_0 <= K_DECIMAL)||(LA75_0 >= K_DISTINCT && LA75_0 <= K_DOUBLE)||(LA75_0 >= K_EXISTS && LA75_0 <= K_FLOAT)||LA75_0==K_FROZEN||(LA75_0 >= K_FUNCTION && LA75_0 <= K_FUNCTIONS)||LA75_0==K_INET||(LA75_0 >= K_INITCOND && LA75_0 <= K_INPUT)||LA75_0==K_INT||(LA75_0 >= K_JSON && LA75_0 <= K_KEYS)||(LA75_0 >= K_KEYSPACES && LA75_0 <= K_LANGUAGE)||(LA75_0 >= K_LIST && LA75_0 <= K_MAP)||LA75_0==K_NOLOGIN||LA75_0==K_NOSUPERUSER||LA75_0==K_OPTIONS||(LA75_0 >= K_PASSWORD && LA75_0 <= K_PERMISSIONS)||LA75_0==K_RETURNS||(LA75_0 >= K_ROLE && LA75_0 <= K_ROLES)||(LA75_0 >= K_SFUNC && LA75_0 <= K_TINYINT)||LA75_0==K_TRIGGER||(LA75_0 >= K_TTL && LA75_0 <= K_TYPE)||(LA75_0 >= K_USER && LA75_0 <= K_USERS)||(LA75_0 >= K_UUID && LA75_0 <= K_VARINT)||LA75_0==K_WRITETIME||LA75_0==QUOTED_NAME) ) {
				alt75=1;
			}
			else if ( (LA75_0==K_PRIMARY) ) {
				alt75=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 75, 0, input);
				throw nvae;
			}

			switch (alt75) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:682:7: k= ident v= comparatorType ( K_STATIC )? ( K_PRIMARY K_KEY )?
					{
					pushFollow(FOLLOW_ident_in_cfamColumns3845);
					k=ident();
					state._fsp--;

					pushFollow(FOLLOW_comparatorType_in_cfamColumns3849);
					v=comparatorType();
					state._fsp--;

					 boolean isStatic=false; 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:682:60: ( K_STATIC )?
					int alt72=2;
					int LA72_0 = input.LA(1);
					if ( (LA72_0==K_STATIC) ) {
						alt72=1;
					}
					switch (alt72) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:682:61: K_STATIC
							{
							match(input,K_STATIC,FOLLOW_K_STATIC_in_cfamColumns3854); 
							isStatic = true;
							}
							break;

					}

					 expr.addDefinition(k, v, isStatic); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:683:9: ( K_PRIMARY K_KEY )?
					int alt73=2;
					int LA73_0 = input.LA(1);
					if ( (LA73_0==K_PRIMARY) ) {
						alt73=1;
					}
					switch (alt73) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:683:10: K_PRIMARY K_KEY
							{
							match(input,K_PRIMARY,FOLLOW_K_PRIMARY_in_cfamColumns3871); 
							match(input,K_KEY,FOLLOW_K_KEY_in_cfamColumns3873); 
							 expr.addKeyAliases(Collections.singletonList(k)); 
							}
							break;

					}

					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:684:7: K_PRIMARY K_KEY '(' pkDef[expr] ( ',' c= ident )* ')'
					{
					match(input,K_PRIMARY,FOLLOW_K_PRIMARY_in_cfamColumns3885); 
					match(input,K_KEY,FOLLOW_K_KEY_in_cfamColumns3887); 
					match(input,171,FOLLOW_171_in_cfamColumns3889); 
					pushFollow(FOLLOW_pkDef_in_cfamColumns3891);
					pkDef(expr);
					state._fsp--;

					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:684:39: ( ',' c= ident )*
					loop74:
					while (true) {
						int alt74=2;
						int LA74_0 = input.LA(1);
						if ( (LA74_0==174) ) {
							alt74=1;
						}

						switch (alt74) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:684:40: ',' c= ident
							{
							match(input,174,FOLLOW_174_in_cfamColumns3895); 
							pushFollow(FOLLOW_ident_in_cfamColumns3899);
							c=ident();
							state._fsp--;

							 expr.addColumnAlias(c); 
							}
							break;

						default :
							break loop74;
						}
					}

					match(input,172,FOLLOW_172_in_cfamColumns3906); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "cfamColumns"



	// $ANTLR start "pkDef"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:687:1: pkDef[CreateTableStatement.RawStatement expr] : (k= ident | '(' k1= ident ( ',' kn= ident )* ')' );
	public final void pkDef(CreateTableStatement.RawStatement expr) throws RecognitionException {
		ColumnIdentifier k =null;
		ColumnIdentifier k1 =null;
		ColumnIdentifier kn =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:688:5: (k= ident | '(' k1= ident ( ',' kn= ident )* ')' )
			int alt77=2;
			int LA77_0 = input.LA(1);
			if ( (LA77_0==IDENT||(LA77_0 >= K_AGGREGATE && LA77_0 <= K_ALL)||LA77_0==K_AS||LA77_0==K_ASCII||(LA77_0 >= K_BIGINT && LA77_0 <= K_BOOLEAN)||(LA77_0 >= K_CALLED && LA77_0 <= K_CLUSTERING)||(LA77_0 >= K_COMPACT && LA77_0 <= K_COUNTER)||(LA77_0 >= K_CUSTOM && LA77_0 <= K_DECIMAL)||(LA77_0 >= K_DISTINCT && LA77_0 <= K_DOUBLE)||(LA77_0 >= K_EXISTS && LA77_0 <= K_FLOAT)||LA77_0==K_FROZEN||(LA77_0 >= K_FUNCTION && LA77_0 <= K_FUNCTIONS)||LA77_0==K_INET||(LA77_0 >= K_INITCOND && LA77_0 <= K_INPUT)||LA77_0==K_INT||(LA77_0 >= K_JSON && LA77_0 <= K_KEYS)||(LA77_0 >= K_KEYSPACES && LA77_0 <= K_LANGUAGE)||(LA77_0 >= K_LIST && LA77_0 <= K_MAP)||LA77_0==K_NOLOGIN||LA77_0==K_NOSUPERUSER||LA77_0==K_OPTIONS||(LA77_0 >= K_PASSWORD && LA77_0 <= K_PERMISSIONS)||LA77_0==K_RETURNS||(LA77_0 >= K_ROLE && LA77_0 <= K_ROLES)||(LA77_0 >= K_SFUNC && LA77_0 <= K_TINYINT)||LA77_0==K_TRIGGER||(LA77_0 >= K_TTL && LA77_0 <= K_TYPE)||(LA77_0 >= K_USER && LA77_0 <= K_USERS)||(LA77_0 >= K_UUID && LA77_0 <= K_VARINT)||LA77_0==K_WRITETIME||LA77_0==QUOTED_NAME) ) {
				alt77=1;
			}
			else if ( (LA77_0==171) ) {
				alt77=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 77, 0, input);
				throw nvae;
			}

			switch (alt77) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:688:7: k= ident
					{
					pushFollow(FOLLOW_ident_in_pkDef3926);
					k=ident();
					state._fsp--;

					 expr.addKeyAliases(Collections.singletonList(k)); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:689:7: '(' k1= ident ( ',' kn= ident )* ')'
					{
					match(input,171,FOLLOW_171_in_pkDef3936); 
					 List<ColumnIdentifier> l = new ArrayList<ColumnIdentifier>(); 
					pushFollow(FOLLOW_ident_in_pkDef3942);
					k1=ident();
					state._fsp--;

					 l.add(k1); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:689:101: ( ',' kn= ident )*
					loop76:
					while (true) {
						int alt76=2;
						int LA76_0 = input.LA(1);
						if ( (LA76_0==174) ) {
							alt76=1;
						}

						switch (alt76) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:689:103: ',' kn= ident
							{
							match(input,174,FOLLOW_174_in_pkDef3948); 
							pushFollow(FOLLOW_ident_in_pkDef3952);
							kn=ident();
							state._fsp--;

							 l.add(kn); 
							}
							break;

						default :
							break loop76;
						}
					}

					match(input,172,FOLLOW_172_in_pkDef3959); 
					 expr.addKeyAliases(l); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "pkDef"



	// $ANTLR start "cfamProperty"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:692:1: cfamProperty[CFProperties props] : ( property[props.properties] | K_COMPACT K_STORAGE | K_CLUSTERING K_ORDER K_BY '(' cfamOrdering[props] ( ',' cfamOrdering[props] )* ')' );
	public final void cfamProperty(CFProperties props) throws RecognitionException {
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:693:5: ( property[props.properties] | K_COMPACT K_STORAGE | K_CLUSTERING K_ORDER K_BY '(' cfamOrdering[props] ( ',' cfamOrdering[props] )* ')' )
			int alt79=3;
			switch ( input.LA(1) ) {
			case IDENT:
			case K_AGGREGATE:
			case K_ALL:
			case K_AS:
			case K_ASCII:
			case K_BIGINT:
			case K_BLOB:
			case K_BOOLEAN:
			case K_CALLED:
			case K_CONTAINS:
			case K_COUNT:
			case K_COUNTER:
			case K_CUSTOM:
			case K_DATE:
			case K_DECIMAL:
			case K_DISTINCT:
			case K_DOUBLE:
			case K_EXISTS:
			case K_FILTERING:
			case K_FINALFUNC:
			case K_FLOAT:
			case K_FROZEN:
			case K_FUNCTION:
			case K_FUNCTIONS:
			case K_INET:
			case K_INITCOND:
			case K_INPUT:
			case K_INT:
			case K_JSON:
			case K_KEY:
			case K_KEYS:
			case K_KEYSPACES:
			case K_LANGUAGE:
			case K_LIST:
			case K_LOGIN:
			case K_MAP:
			case K_NOLOGIN:
			case K_NOSUPERUSER:
			case K_OPTIONS:
			case K_PASSWORD:
			case K_PERMISSION:
			case K_PERMISSIONS:
			case K_RETURNS:
			case K_ROLE:
			case K_ROLES:
			case K_SFUNC:
			case K_SMALLINT:
			case K_STATIC:
			case K_STORAGE:
			case K_STYPE:
			case K_SUPERUSER:
			case K_TEXT:
			case K_TIME:
			case K_TIMESTAMP:
			case K_TIMEUUID:
			case K_TINYINT:
			case K_TRIGGER:
			case K_TTL:
			case K_TUPLE:
			case K_TYPE:
			case K_USER:
			case K_USERS:
			case K_UUID:
			case K_VALUES:
			case K_VARCHAR:
			case K_VARINT:
			case K_WRITETIME:
			case QUOTED_NAME:
				{
				alt79=1;
				}
				break;
			case K_COMPACT:
				{
				int LA79_2 = input.LA(2);
				if ( (LA79_2==K_STORAGE) ) {
					alt79=2;
				}
				else if ( (LA79_2==181) ) {
					alt79=1;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 79, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case K_CLUSTERING:
				{
				int LA79_3 = input.LA(2);
				if ( (LA79_3==K_ORDER) ) {
					alt79=3;
				}
				else if ( (LA79_3==181) ) {
					alt79=1;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 79, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 79, 0, input);
				throw nvae;
			}
			switch (alt79) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:693:7: property[props.properties]
					{
					pushFollow(FOLLOW_property_in_cfamProperty3979);
					property(props.properties);
					state._fsp--;

					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:694:7: K_COMPACT K_STORAGE
					{
					match(input,K_COMPACT,FOLLOW_K_COMPACT_in_cfamProperty3988); 
					match(input,K_STORAGE,FOLLOW_K_STORAGE_in_cfamProperty3990); 
					 props.setCompactStorage(); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:695:7: K_CLUSTERING K_ORDER K_BY '(' cfamOrdering[props] ( ',' cfamOrdering[props] )* ')'
					{
					match(input,K_CLUSTERING,FOLLOW_K_CLUSTERING_in_cfamProperty4000); 
					match(input,K_ORDER,FOLLOW_K_ORDER_in_cfamProperty4002); 
					match(input,K_BY,FOLLOW_K_BY_in_cfamProperty4004); 
					match(input,171,FOLLOW_171_in_cfamProperty4006); 
					pushFollow(FOLLOW_cfamOrdering_in_cfamProperty4008);
					cfamOrdering(props);
					state._fsp--;

					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:695:57: ( ',' cfamOrdering[props] )*
					loop78:
					while (true) {
						int alt78=2;
						int LA78_0 = input.LA(1);
						if ( (LA78_0==174) ) {
							alt78=1;
						}

						switch (alt78) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:695:58: ',' cfamOrdering[props]
							{
							match(input,174,FOLLOW_174_in_cfamProperty4012); 
							pushFollow(FOLLOW_cfamOrdering_in_cfamProperty4014);
							cfamOrdering(props);
							state._fsp--;

							}
							break;

						default :
							break loop78;
						}
					}

					match(input,172,FOLLOW_172_in_cfamProperty4019); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "cfamProperty"



	// $ANTLR start "cfamOrdering"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:698:1: cfamOrdering[CFProperties props] : k= ident ( K_ASC | K_DESC ) ;
	public final void cfamOrdering(CFProperties props) throws RecognitionException {
		ColumnIdentifier k =null;

		 boolean reversed=false; 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:700:5: (k= ident ( K_ASC | K_DESC ) )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:700:7: k= ident ( K_ASC | K_DESC )
			{
			pushFollow(FOLLOW_ident_in_cfamOrdering4047);
			k=ident();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:700:15: ( K_ASC | K_DESC )
			int alt80=2;
			int LA80_0 = input.LA(1);
			if ( (LA80_0==K_ASC) ) {
				alt80=1;
			}
			else if ( (LA80_0==K_DESC) ) {
				alt80=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 80, 0, input);
				throw nvae;
			}

			switch (alt80) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:700:16: K_ASC
					{
					match(input,K_ASC,FOLLOW_K_ASC_in_cfamOrdering4050); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:700:24: K_DESC
					{
					match(input,K_DESC,FOLLOW_K_DESC_in_cfamOrdering4054); 
					 reversed=true;
					}
					break;

			}

			 props.setOrdering(k, reversed); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "cfamOrdering"



	// $ANTLR start "createTypeStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:711:1: createTypeStatement returns [CreateTypeStatement expr] : K_CREATE K_TYPE ( K_IF K_NOT K_EXISTS )? tn= userTypeName '(' typeColumns[expr] ( ',' ( typeColumns[expr] )? )* ')' ;
	public final CreateTypeStatement createTypeStatement() throws RecognitionException {
		CreateTypeStatement expr = null;


		UTName tn =null;

		 boolean ifNotExists = false; 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:713:5: ( K_CREATE K_TYPE ( K_IF K_NOT K_EXISTS )? tn= userTypeName '(' typeColumns[expr] ( ',' ( typeColumns[expr] )? )* ')' )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:713:7: K_CREATE K_TYPE ( K_IF K_NOT K_EXISTS )? tn= userTypeName '(' typeColumns[expr] ( ',' ( typeColumns[expr] )? )* ')'
			{
			match(input,K_CREATE,FOLLOW_K_CREATE_in_createTypeStatement4093); 
			match(input,K_TYPE,FOLLOW_K_TYPE_in_createTypeStatement4095); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:713:23: ( K_IF K_NOT K_EXISTS )?
			int alt81=2;
			int LA81_0 = input.LA(1);
			if ( (LA81_0==K_IF) ) {
				alt81=1;
			}
			switch (alt81) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:713:24: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_createTypeStatement4098); 
					match(input,K_NOT,FOLLOW_K_NOT_in_createTypeStatement4100); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createTypeStatement4102); 
					 ifNotExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_userTypeName_in_createTypeStatement4120);
			tn=userTypeName();
			state._fsp--;

			 expr = new CreateTypeStatement(tn, ifNotExists); 
			match(input,171,FOLLOW_171_in_createTypeStatement4133); 
			pushFollow(FOLLOW_typeColumns_in_createTypeStatement4135);
			typeColumns(expr);
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:715:32: ( ',' ( typeColumns[expr] )? )*
			loop83:
			while (true) {
				int alt83=2;
				int LA83_0 = input.LA(1);
				if ( (LA83_0==174) ) {
					alt83=1;
				}

				switch (alt83) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:715:34: ',' ( typeColumns[expr] )?
					{
					match(input,174,FOLLOW_174_in_createTypeStatement4140); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:715:38: ( typeColumns[expr] )?
					int alt82=2;
					int LA82_0 = input.LA(1);
					if ( (LA82_0==IDENT||(LA82_0 >= K_AGGREGATE && LA82_0 <= K_ALL)||LA82_0==K_AS||LA82_0==K_ASCII||(LA82_0 >= K_BIGINT && LA82_0 <= K_BOOLEAN)||(LA82_0 >= K_CALLED && LA82_0 <= K_CLUSTERING)||(LA82_0 >= K_COMPACT && LA82_0 <= K_COUNTER)||(LA82_0 >= K_CUSTOM && LA82_0 <= K_DECIMAL)||(LA82_0 >= K_DISTINCT && LA82_0 <= K_DOUBLE)||(LA82_0 >= K_EXISTS && LA82_0 <= K_FLOAT)||LA82_0==K_FROZEN||(LA82_0 >= K_FUNCTION && LA82_0 <= K_FUNCTIONS)||LA82_0==K_INET||(LA82_0 >= K_INITCOND && LA82_0 <= K_INPUT)||LA82_0==K_INT||(LA82_0 >= K_JSON && LA82_0 <= K_KEYS)||(LA82_0 >= K_KEYSPACES && LA82_0 <= K_LANGUAGE)||(LA82_0 >= K_LIST && LA82_0 <= K_MAP)||LA82_0==K_NOLOGIN||LA82_0==K_NOSUPERUSER||LA82_0==K_OPTIONS||(LA82_0 >= K_PASSWORD && LA82_0 <= K_PERMISSIONS)||LA82_0==K_RETURNS||(LA82_0 >= K_ROLE && LA82_0 <= K_ROLES)||(LA82_0 >= K_SFUNC && LA82_0 <= K_TINYINT)||LA82_0==K_TRIGGER||(LA82_0 >= K_TTL && LA82_0 <= K_TYPE)||(LA82_0 >= K_USER && LA82_0 <= K_USERS)||(LA82_0 >= K_UUID && LA82_0 <= K_VARINT)||LA82_0==K_WRITETIME||LA82_0==QUOTED_NAME) ) {
						alt82=1;
					}
					switch (alt82) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:715:38: typeColumns[expr]
							{
							pushFollow(FOLLOW_typeColumns_in_createTypeStatement4142);
							typeColumns(expr);
							state._fsp--;

							}
							break;

					}

					}
					break;

				default :
					break loop83;
				}
			}

			match(input,172,FOLLOW_172_in_createTypeStatement4149); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "createTypeStatement"



	// $ANTLR start "typeColumns"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:718:1: typeColumns[CreateTypeStatement expr] : k= noncol_ident v= comparatorType ;
	public final void typeColumns(CreateTypeStatement expr) throws RecognitionException {
		ColumnIdentifier k =null;
		CQL3Type.Raw v =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:719:5: (k= noncol_ident v= comparatorType )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:719:7: k= noncol_ident v= comparatorType
			{
			pushFollow(FOLLOW_noncol_ident_in_typeColumns4169);
			k=noncol_ident();
			state._fsp--;

			pushFollow(FOLLOW_comparatorType_in_typeColumns4173);
			v=comparatorType();
			state._fsp--;

			 expr.addDefinition(k, v); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "typeColumns"



	// $ANTLR start "createIndexStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:727:1: createIndexStatement returns [CreateIndexStatement expr] : K_CREATE ( K_CUSTOM )? K_INDEX ( K_IF K_NOT K_EXISTS )? ( idxName[name] )? K_ON cf= columnFamilyName '(' ( indexIdent[targets] ( ',' indexIdent[targets] )* )? ')' ( K_USING cls= STRING_LITERAL )? ( K_WITH properties[props] )? ;
	public final CreateIndexStatement createIndexStatement() throws RecognitionException {
		CreateIndexStatement expr = null;


		Token cls=null;
		CFName cf =null;


		        IndexPropDefs props = new IndexPropDefs();
		        boolean ifNotExists = false;
		        IndexName name = new IndexName();
		        List<IndexTarget.Raw> targets = new ArrayList<>();
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:734:5: ( K_CREATE ( K_CUSTOM )? K_INDEX ( K_IF K_NOT K_EXISTS )? ( idxName[name] )? K_ON cf= columnFamilyName '(' ( indexIdent[targets] ( ',' indexIdent[targets] )* )? ')' ( K_USING cls= STRING_LITERAL )? ( K_WITH properties[props] )? )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:734:7: K_CREATE ( K_CUSTOM )? K_INDEX ( K_IF K_NOT K_EXISTS )? ( idxName[name] )? K_ON cf= columnFamilyName '(' ( indexIdent[targets] ( ',' indexIdent[targets] )* )? ')' ( K_USING cls= STRING_LITERAL )? ( K_WITH properties[props] )?
			{
			match(input,K_CREATE,FOLLOW_K_CREATE_in_createIndexStatement4208); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:734:16: ( K_CUSTOM )?
			int alt84=2;
			int LA84_0 = input.LA(1);
			if ( (LA84_0==K_CUSTOM) ) {
				alt84=1;
			}
			switch (alt84) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:734:17: K_CUSTOM
					{
					match(input,K_CUSTOM,FOLLOW_K_CUSTOM_in_createIndexStatement4211); 
					 props.isCustom = true; 
					}
					break;

			}

			match(input,K_INDEX,FOLLOW_K_INDEX_in_createIndexStatement4217); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:734:63: ( K_IF K_NOT K_EXISTS )?
			int alt85=2;
			int LA85_0 = input.LA(1);
			if ( (LA85_0==K_IF) ) {
				alt85=1;
			}
			switch (alt85) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:734:64: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_createIndexStatement4220); 
					match(input,K_NOT,FOLLOW_K_NOT_in_createIndexStatement4222); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createIndexStatement4224); 
					 ifNotExists = true; 
					}
					break;

			}

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:735:9: ( idxName[name] )?
			int alt86=2;
			int LA86_0 = input.LA(1);
			if ( (LA86_0==IDENT||(LA86_0 >= K_AGGREGATE && LA86_0 <= K_ALL)||LA86_0==K_AS||LA86_0==K_ASCII||(LA86_0 >= K_BIGINT && LA86_0 <= K_BOOLEAN)||(LA86_0 >= K_CALLED && LA86_0 <= K_CLUSTERING)||(LA86_0 >= K_COMPACT && LA86_0 <= K_COUNTER)||(LA86_0 >= K_CUSTOM && LA86_0 <= K_DECIMAL)||(LA86_0 >= K_DISTINCT && LA86_0 <= K_DOUBLE)||(LA86_0 >= K_EXISTS && LA86_0 <= K_FLOAT)||LA86_0==K_FROZEN||(LA86_0 >= K_FUNCTION && LA86_0 <= K_FUNCTIONS)||LA86_0==K_INET||(LA86_0 >= K_INITCOND && LA86_0 <= K_INPUT)||LA86_0==K_INT||(LA86_0 >= K_JSON && LA86_0 <= K_KEYS)||(LA86_0 >= K_KEYSPACES && LA86_0 <= K_LANGUAGE)||(LA86_0 >= K_LIST && LA86_0 <= K_MAP)||LA86_0==K_NOLOGIN||LA86_0==K_NOSUPERUSER||LA86_0==K_OPTIONS||(LA86_0 >= K_PASSWORD && LA86_0 <= K_PERMISSIONS)||LA86_0==K_RETURNS||(LA86_0 >= K_ROLE && LA86_0 <= K_ROLES)||(LA86_0 >= K_SFUNC && LA86_0 <= K_TINYINT)||LA86_0==K_TRIGGER||(LA86_0 >= K_TTL && LA86_0 <= K_TYPE)||(LA86_0 >= K_USER && LA86_0 <= K_USERS)||(LA86_0 >= K_UUID && LA86_0 <= K_VARINT)||LA86_0==K_WRITETIME||(LA86_0 >= QMARK && LA86_0 <= QUOTED_NAME)) ) {
				alt86=1;
			}
			switch (alt86) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:735:10: idxName[name]
					{
					pushFollow(FOLLOW_idxName_in_createIndexStatement4240);
					idxName(name);
					state._fsp--;

					}
					break;

			}

			match(input,K_ON,FOLLOW_K_ON_in_createIndexStatement4245); 
			pushFollow(FOLLOW_columnFamilyName_in_createIndexStatement4249);
			cf=columnFamilyName();
			state._fsp--;

			match(input,171,FOLLOW_171_in_createIndexStatement4251); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:735:55: ( indexIdent[targets] ( ',' indexIdent[targets] )* )?
			int alt88=2;
			int LA88_0 = input.LA(1);
			if ( (LA88_0==IDENT||(LA88_0 >= K_AGGREGATE && LA88_0 <= K_ALL)||LA88_0==K_AS||LA88_0==K_ASCII||(LA88_0 >= K_BIGINT && LA88_0 <= K_BOOLEAN)||(LA88_0 >= K_CALLED && LA88_0 <= K_CLUSTERING)||(LA88_0 >= K_COMPACT && LA88_0 <= K_COUNTER)||(LA88_0 >= K_CUSTOM && LA88_0 <= K_DECIMAL)||(LA88_0 >= K_DISTINCT && LA88_0 <= K_DOUBLE)||LA88_0==K_ENTRIES||(LA88_0 >= K_EXISTS && LA88_0 <= K_FLOAT)||(LA88_0 >= K_FROZEN && LA88_0 <= K_FUNCTIONS)||LA88_0==K_INET||(LA88_0 >= K_INITCOND && LA88_0 <= K_INPUT)||LA88_0==K_INT||(LA88_0 >= K_JSON && LA88_0 <= K_KEYS)||(LA88_0 >= K_KEYSPACES && LA88_0 <= K_LANGUAGE)||(LA88_0 >= K_LIST && LA88_0 <= K_MAP)||LA88_0==K_NOLOGIN||LA88_0==K_NOSUPERUSER||LA88_0==K_OPTIONS||(LA88_0 >= K_PASSWORD && LA88_0 <= K_PERMISSIONS)||LA88_0==K_RETURNS||(LA88_0 >= K_ROLE && LA88_0 <= K_ROLES)||(LA88_0 >= K_SFUNC && LA88_0 <= K_TINYINT)||LA88_0==K_TRIGGER||(LA88_0 >= K_TTL && LA88_0 <= K_TYPE)||(LA88_0 >= K_USER && LA88_0 <= K_USERS)||(LA88_0 >= K_UUID && LA88_0 <= K_VARINT)||LA88_0==K_WRITETIME||LA88_0==QUOTED_NAME) ) {
				alt88=1;
			}
			switch (alt88) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:735:56: indexIdent[targets] ( ',' indexIdent[targets] )*
					{
					pushFollow(FOLLOW_indexIdent_in_createIndexStatement4254);
					indexIdent(targets);
					state._fsp--;

					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:735:76: ( ',' indexIdent[targets] )*
					loop87:
					while (true) {
						int alt87=2;
						int LA87_0 = input.LA(1);
						if ( (LA87_0==174) ) {
							alt87=1;
						}

						switch (alt87) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:735:77: ',' indexIdent[targets]
							{
							match(input,174,FOLLOW_174_in_createIndexStatement4258); 
							pushFollow(FOLLOW_indexIdent_in_createIndexStatement4260);
							indexIdent(targets);
							state._fsp--;

							}
							break;

						default :
							break loop87;
						}
					}

					}
					break;

			}

			match(input,172,FOLLOW_172_in_createIndexStatement4267); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:736:9: ( K_USING cls= STRING_LITERAL )?
			int alt89=2;
			int LA89_0 = input.LA(1);
			if ( (LA89_0==K_USING) ) {
				alt89=1;
			}
			switch (alt89) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:736:10: K_USING cls= STRING_LITERAL
					{
					match(input,K_USING,FOLLOW_K_USING_in_createIndexStatement4278); 
					cls=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_createIndexStatement4282); 
					 props.customClass = (cls!=null?cls.getText():null); 
					}
					break;

			}

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:737:9: ( K_WITH properties[props] )?
			int alt90=2;
			int LA90_0 = input.LA(1);
			if ( (LA90_0==K_WITH) ) {
				alt90=1;
			}
			switch (alt90) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:737:10: K_WITH properties[props]
					{
					match(input,K_WITH,FOLLOW_K_WITH_in_createIndexStatement4297); 
					pushFollow(FOLLOW_properties_in_createIndexStatement4299);
					properties(props);
					state._fsp--;

					}
					break;

			}

			 expr = new CreateIndexStatement(cf, name, targets, props, ifNotExists); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "createIndexStatement"



	// $ANTLR start "indexIdent"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:741:1: indexIdent[List<IndexTarget.Raw> targets] : (c= cident | K_VALUES '(' c= cident ')' | K_KEYS '(' c= cident ')' | K_ENTRIES '(' c= cident ')' | K_FULL '(' c= cident ')' );
	public final void indexIdent(List<IndexTarget.Raw> targets) throws RecognitionException {
		ColumnIdentifier.Raw c =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:742:5: (c= cident | K_VALUES '(' c= cident ')' | K_KEYS '(' c= cident ')' | K_ENTRIES '(' c= cident ')' | K_FULL '(' c= cident ')' )
			int alt91=5;
			switch ( input.LA(1) ) {
			case IDENT:
			case K_AGGREGATE:
			case K_ALL:
			case K_AS:
			case K_ASCII:
			case K_BIGINT:
			case K_BLOB:
			case K_BOOLEAN:
			case K_CALLED:
			case K_CLUSTERING:
			case K_COMPACT:
			case K_CONTAINS:
			case K_COUNT:
			case K_COUNTER:
			case K_CUSTOM:
			case K_DATE:
			case K_DECIMAL:
			case K_DISTINCT:
			case K_DOUBLE:
			case K_EXISTS:
			case K_FILTERING:
			case K_FINALFUNC:
			case K_FLOAT:
			case K_FROZEN:
			case K_FUNCTION:
			case K_FUNCTIONS:
			case K_INET:
			case K_INITCOND:
			case K_INPUT:
			case K_INT:
			case K_JSON:
			case K_KEY:
			case K_KEYSPACES:
			case K_LANGUAGE:
			case K_LIST:
			case K_LOGIN:
			case K_MAP:
			case K_NOLOGIN:
			case K_NOSUPERUSER:
			case K_OPTIONS:
			case K_PASSWORD:
			case K_PERMISSION:
			case K_PERMISSIONS:
			case K_RETURNS:
			case K_ROLE:
			case K_ROLES:
			case K_SFUNC:
			case K_SMALLINT:
			case K_STATIC:
			case K_STORAGE:
			case K_STYPE:
			case K_SUPERUSER:
			case K_TEXT:
			case K_TIME:
			case K_TIMESTAMP:
			case K_TIMEUUID:
			case K_TINYINT:
			case K_TRIGGER:
			case K_TTL:
			case K_TUPLE:
			case K_TYPE:
			case K_USER:
			case K_USERS:
			case K_UUID:
			case K_VARCHAR:
			case K_VARINT:
			case K_WRITETIME:
			case QUOTED_NAME:
				{
				alt91=1;
				}
				break;
			case K_VALUES:
				{
				int LA91_2 = input.LA(2);
				if ( (LA91_2==171) ) {
					alt91=2;
				}
				else if ( (LA91_2==172||LA91_2==174) ) {
					alt91=1;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 91, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case K_KEYS:
				{
				int LA91_3 = input.LA(2);
				if ( (LA91_3==171) ) {
					alt91=3;
				}
				else if ( (LA91_3==172||LA91_3==174) ) {
					alt91=1;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 91, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case K_ENTRIES:
				{
				alt91=4;
				}
				break;
			case K_FULL:
				{
				alt91=5;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 91, 0, input);
				throw nvae;
			}
			switch (alt91) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:742:7: c= cident
					{
					pushFollow(FOLLOW_cident_in_indexIdent4331);
					c=cident();
					state._fsp--;

					 targets.add(IndexTarget.Raw.simpleIndexOn(c)); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:743:7: K_VALUES '(' c= cident ')'
					{
					match(input,K_VALUES,FOLLOW_K_VALUES_in_indexIdent4359); 
					match(input,171,FOLLOW_171_in_indexIdent4361); 
					pushFollow(FOLLOW_cident_in_indexIdent4365);
					c=cident();
					state._fsp--;

					match(input,172,FOLLOW_172_in_indexIdent4367); 
					 targets.add(IndexTarget.Raw.valuesOf(c)); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:744:7: K_KEYS '(' c= cident ')'
					{
					match(input,K_KEYS,FOLLOW_K_KEYS_in_indexIdent4378); 
					match(input,171,FOLLOW_171_in_indexIdent4380); 
					pushFollow(FOLLOW_cident_in_indexIdent4384);
					c=cident();
					state._fsp--;

					match(input,172,FOLLOW_172_in_indexIdent4386); 
					 targets.add(IndexTarget.Raw.keysOf(c)); 
					}
					break;
				case 4 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:745:7: K_ENTRIES '(' c= cident ')'
					{
					match(input,K_ENTRIES,FOLLOW_K_ENTRIES_in_indexIdent4399); 
					match(input,171,FOLLOW_171_in_indexIdent4401); 
					pushFollow(FOLLOW_cident_in_indexIdent4405);
					c=cident();
					state._fsp--;

					match(input,172,FOLLOW_172_in_indexIdent4407); 
					 targets.add(IndexTarget.Raw.keysAndValuesOf(c)); 
					}
					break;
				case 5 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:746:7: K_FULL '(' c= cident ')'
					{
					match(input,K_FULL,FOLLOW_K_FULL_in_indexIdent4417); 
					match(input,171,FOLLOW_171_in_indexIdent4419); 
					pushFollow(FOLLOW_cident_in_indexIdent4423);
					c=cident();
					state._fsp--;

					match(input,172,FOLLOW_172_in_indexIdent4425); 
					 targets.add(IndexTarget.Raw.fullCollection(c)); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "indexIdent"



	// $ANTLR start "createMaterializedViewStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:757:1: createMaterializedViewStatement returns [CreateViewStatement expr] : K_CREATE K_MATERIALIZED K_VIEW ( K_IF K_NOT K_EXISTS )? cf= columnFamilyName K_AS K_SELECT sclause= selectClause K_FROM basecf= columnFamilyName ( K_WHERE wclause= whereClause )? K_PRIMARY K_KEY ( '(' '(' k1= cident ( ',' kn= cident )* ')' ( ',' c1= cident )* ')' | '(' k1= cident ( ',' cn= cident )* ')' ) ( K_WITH cfamProperty[expr.properties] ( K_AND cfamProperty[expr.properties] )* )? ;
	public final CreateViewStatement createMaterializedViewStatement() throws RecognitionException {
		CreateViewStatement expr = null;


		CFName cf =null;
		List<RawSelector> sclause =null;
		CFName basecf =null;
		WhereClause.Builder wclause =null;
		ColumnIdentifier.Raw k1 =null;
		ColumnIdentifier.Raw kn =null;
		ColumnIdentifier.Raw c1 =null;
		ColumnIdentifier.Raw cn =null;


		        boolean ifNotExists = false;
		        List<ColumnIdentifier.Raw> partitionKeys = new ArrayList<>();
		        List<ColumnIdentifier.Raw> compositeKeys = new ArrayList<>();
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:763:5: ( K_CREATE K_MATERIALIZED K_VIEW ( K_IF K_NOT K_EXISTS )? cf= columnFamilyName K_AS K_SELECT sclause= selectClause K_FROM basecf= columnFamilyName ( K_WHERE wclause= whereClause )? K_PRIMARY K_KEY ( '(' '(' k1= cident ( ',' kn= cident )* ')' ( ',' c1= cident )* ')' | '(' k1= cident ( ',' cn= cident )* ')' ) ( K_WITH cfamProperty[expr.properties] ( K_AND cfamProperty[expr.properties] )* )? )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:763:7: K_CREATE K_MATERIALIZED K_VIEW ( K_IF K_NOT K_EXISTS )? cf= columnFamilyName K_AS K_SELECT sclause= selectClause K_FROM basecf= columnFamilyName ( K_WHERE wclause= whereClause )? K_PRIMARY K_KEY ( '(' '(' k1= cident ( ',' kn= cident )* ')' ( ',' c1= cident )* ')' | '(' k1= cident ( ',' cn= cident )* ')' ) ( K_WITH cfamProperty[expr.properties] ( K_AND cfamProperty[expr.properties] )* )?
			{
			match(input,K_CREATE,FOLLOW_K_CREATE_in_createMaterializedViewStatement4462); 
			match(input,K_MATERIALIZED,FOLLOW_K_MATERIALIZED_in_createMaterializedViewStatement4464); 
			match(input,K_VIEW,FOLLOW_K_VIEW_in_createMaterializedViewStatement4466); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:763:38: ( K_IF K_NOT K_EXISTS )?
			int alt92=2;
			int LA92_0 = input.LA(1);
			if ( (LA92_0==K_IF) ) {
				alt92=1;
			}
			switch (alt92) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:763:39: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_createMaterializedViewStatement4469); 
					match(input,K_NOT,FOLLOW_K_NOT_in_createMaterializedViewStatement4471); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createMaterializedViewStatement4473); 
					 ifNotExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_columnFamilyName_in_createMaterializedViewStatement4481);
			cf=columnFamilyName();
			state._fsp--;

			match(input,K_AS,FOLLOW_K_AS_in_createMaterializedViewStatement4483); 
			match(input,K_SELECT,FOLLOW_K_SELECT_in_createMaterializedViewStatement4493); 
			pushFollow(FOLLOW_selectClause_in_createMaterializedViewStatement4497);
			sclause=selectClause();
			state._fsp--;

			match(input,K_FROM,FOLLOW_K_FROM_in_createMaterializedViewStatement4499); 
			pushFollow(FOLLOW_columnFamilyName_in_createMaterializedViewStatement4503);
			basecf=columnFamilyName();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:765:9: ( K_WHERE wclause= whereClause )?
			int alt93=2;
			int LA93_0 = input.LA(1);
			if ( (LA93_0==K_WHERE) ) {
				alt93=1;
			}
			switch (alt93) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:765:10: K_WHERE wclause= whereClause
					{
					match(input,K_WHERE,FOLLOW_K_WHERE_in_createMaterializedViewStatement4514); 
					pushFollow(FOLLOW_whereClause_in_createMaterializedViewStatement4518);
					wclause=whereClause();
					state._fsp--;

					}
					break;

			}

			match(input,K_PRIMARY,FOLLOW_K_PRIMARY_in_createMaterializedViewStatement4530); 
			match(input,K_KEY,FOLLOW_K_KEY_in_createMaterializedViewStatement4532); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:766:25: ( '(' '(' k1= cident ( ',' kn= cident )* ')' ( ',' c1= cident )* ')' | '(' k1= cident ( ',' cn= cident )* ')' )
			int alt97=2;
			int LA97_0 = input.LA(1);
			if ( (LA97_0==171) ) {
				int LA97_1 = input.LA(2);
				if ( (LA97_1==171) ) {
					alt97=1;
				}
				else if ( (LA97_1==IDENT||(LA97_1 >= K_AGGREGATE && LA97_1 <= K_ALL)||LA97_1==K_AS||LA97_1==K_ASCII||(LA97_1 >= K_BIGINT && LA97_1 <= K_BOOLEAN)||(LA97_1 >= K_CALLED && LA97_1 <= K_CLUSTERING)||(LA97_1 >= K_COMPACT && LA97_1 <= K_COUNTER)||(LA97_1 >= K_CUSTOM && LA97_1 <= K_DECIMAL)||(LA97_1 >= K_DISTINCT && LA97_1 <= K_DOUBLE)||(LA97_1 >= K_EXISTS && LA97_1 <= K_FLOAT)||LA97_1==K_FROZEN||(LA97_1 >= K_FUNCTION && LA97_1 <= K_FUNCTIONS)||LA97_1==K_INET||(LA97_1 >= K_INITCOND && LA97_1 <= K_INPUT)||LA97_1==K_INT||(LA97_1 >= K_JSON && LA97_1 <= K_KEYS)||(LA97_1 >= K_KEYSPACES && LA97_1 <= K_LANGUAGE)||(LA97_1 >= K_LIST && LA97_1 <= K_MAP)||LA97_1==K_NOLOGIN||LA97_1==K_NOSUPERUSER||LA97_1==K_OPTIONS||(LA97_1 >= K_PASSWORD && LA97_1 <= K_PERMISSIONS)||LA97_1==K_RETURNS||(LA97_1 >= K_ROLE && LA97_1 <= K_ROLES)||(LA97_1 >= K_SFUNC && LA97_1 <= K_TINYINT)||LA97_1==K_TRIGGER||(LA97_1 >= K_TTL && LA97_1 <= K_TYPE)||(LA97_1 >= K_USER && LA97_1 <= K_USERS)||(LA97_1 >= K_UUID && LA97_1 <= K_VARINT)||LA97_1==K_WRITETIME||LA97_1==QUOTED_NAME) ) {
					alt97=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 97, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 97, 0, input);
				throw nvae;
			}

			switch (alt97) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:767:9: '(' '(' k1= cident ( ',' kn= cident )* ')' ( ',' c1= cident )* ')'
					{
					match(input,171,FOLLOW_171_in_createMaterializedViewStatement4544); 
					match(input,171,FOLLOW_171_in_createMaterializedViewStatement4546); 
					pushFollow(FOLLOW_cident_in_createMaterializedViewStatement4550);
					k1=cident();
					state._fsp--;

					 partitionKeys.add(k1); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:767:54: ( ',' kn= cident )*
					loop94:
					while (true) {
						int alt94=2;
						int LA94_0 = input.LA(1);
						if ( (LA94_0==174) ) {
							alt94=1;
						}

						switch (alt94) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:767:56: ',' kn= cident
							{
							match(input,174,FOLLOW_174_in_createMaterializedViewStatement4556); 
							pushFollow(FOLLOW_cident_in_createMaterializedViewStatement4560);
							kn=cident();
							state._fsp--;

							 partitionKeys.add(kn); 
							}
							break;

						default :
							break loop94;
						}
					}

					match(input,172,FOLLOW_172_in_createMaterializedViewStatement4567); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:767:104: ( ',' c1= cident )*
					loop95:
					while (true) {
						int alt95=2;
						int LA95_0 = input.LA(1);
						if ( (LA95_0==174) ) {
							alt95=1;
						}

						switch (alt95) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:767:106: ',' c1= cident
							{
							match(input,174,FOLLOW_174_in_createMaterializedViewStatement4571); 
							pushFollow(FOLLOW_cident_in_createMaterializedViewStatement4575);
							c1=cident();
							state._fsp--;

							 compositeKeys.add(c1); 
							}
							break;

						default :
							break loop95;
						}
					}

					match(input,172,FOLLOW_172_in_createMaterializedViewStatement4582); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:768:9: '(' k1= cident ( ',' cn= cident )* ')'
					{
					match(input,171,FOLLOW_171_in_createMaterializedViewStatement4592); 
					pushFollow(FOLLOW_cident_in_createMaterializedViewStatement4596);
					k1=cident();
					state._fsp--;

					 partitionKeys.add(k1); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:768:50: ( ',' cn= cident )*
					loop96:
					while (true) {
						int alt96=2;
						int LA96_0 = input.LA(1);
						if ( (LA96_0==174) ) {
							alt96=1;
						}

						switch (alt96) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:768:52: ',' cn= cident
							{
							match(input,174,FOLLOW_174_in_createMaterializedViewStatement4602); 
							pushFollow(FOLLOW_cident_in_createMaterializedViewStatement4606);
							cn=cident();
							state._fsp--;

							 compositeKeys.add(cn); 
							}
							break;

						default :
							break loop96;
						}
					}

					match(input,172,FOLLOW_172_in_createMaterializedViewStatement4613); 
					}
					break;

			}


			             WhereClause where = wclause == null ? WhereClause.empty() : wclause.build();
			             expr = new CreateViewStatement(cf, basecf, sclause, where, partitionKeys, compositeKeys, ifNotExists);
			        
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:774:9: ( K_WITH cfamProperty[expr.properties] ( K_AND cfamProperty[expr.properties] )* )?
			int alt99=2;
			int LA99_0 = input.LA(1);
			if ( (LA99_0==K_WITH) ) {
				alt99=1;
			}
			switch (alt99) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:774:11: K_WITH cfamProperty[expr.properties] ( K_AND cfamProperty[expr.properties] )*
					{
					match(input,K_WITH,FOLLOW_K_WITH_in_createMaterializedViewStatement4645); 
					pushFollow(FOLLOW_cfamProperty_in_createMaterializedViewStatement4647);
					cfamProperty(expr.properties);
					state._fsp--;

					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:774:48: ( K_AND cfamProperty[expr.properties] )*
					loop98:
					while (true) {
						int alt98=2;
						int LA98_0 = input.LA(1);
						if ( (LA98_0==K_AND) ) {
							alt98=1;
						}

						switch (alt98) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:774:50: K_AND cfamProperty[expr.properties]
							{
							match(input,K_AND,FOLLOW_K_AND_in_createMaterializedViewStatement4652); 
							pushFollow(FOLLOW_cfamProperty_in_createMaterializedViewStatement4654);
							cfamProperty(expr.properties);
							state._fsp--;

							}
							break;

						default :
							break loop98;
						}
					}

					}
					break;

			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "createMaterializedViewStatement"



	// $ANTLR start "createTriggerStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:780:1: createTriggerStatement returns [CreateTriggerStatement expr] : K_CREATE K_TRIGGER ( K_IF K_NOT K_EXISTS )? (name= cident ) K_ON cf= columnFamilyName K_USING cls= STRING_LITERAL ;
	public final CreateTriggerStatement createTriggerStatement() throws RecognitionException {
		CreateTriggerStatement expr = null;


		Token cls=null;
		ColumnIdentifier.Raw name =null;
		CFName cf =null;


		        boolean ifNotExists = false;
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:784:5: ( K_CREATE K_TRIGGER ( K_IF K_NOT K_EXISTS )? (name= cident ) K_ON cf= columnFamilyName K_USING cls= STRING_LITERAL )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:784:7: K_CREATE K_TRIGGER ( K_IF K_NOT K_EXISTS )? (name= cident ) K_ON cf= columnFamilyName K_USING cls= STRING_LITERAL
			{
			match(input,K_CREATE,FOLLOW_K_CREATE_in_createTriggerStatement4692); 
			match(input,K_TRIGGER,FOLLOW_K_TRIGGER_in_createTriggerStatement4694); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:784:26: ( K_IF K_NOT K_EXISTS )?
			int alt100=2;
			int LA100_0 = input.LA(1);
			if ( (LA100_0==K_IF) ) {
				alt100=1;
			}
			switch (alt100) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:784:27: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_createTriggerStatement4697); 
					match(input,K_NOT,FOLLOW_K_NOT_in_createTriggerStatement4699); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createTriggerStatement4701); 
					 ifNotExists = true; 
					}
					break;

			}

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:784:74: (name= cident )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:784:75: name= cident
			{
			pushFollow(FOLLOW_cident_in_createTriggerStatement4711);
			name=cident();
			state._fsp--;

			}

			match(input,K_ON,FOLLOW_K_ON_in_createTriggerStatement4722); 
			pushFollow(FOLLOW_columnFamilyName_in_createTriggerStatement4726);
			cf=columnFamilyName();
			state._fsp--;

			match(input,K_USING,FOLLOW_K_USING_in_createTriggerStatement4728); 
			cls=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_createTriggerStatement4732); 
			 expr = new CreateTriggerStatement(cf, name.toString(), (cls!=null?cls.getText():null), ifNotExists); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "createTriggerStatement"



	// $ANTLR start "dropTriggerStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:792:1: dropTriggerStatement returns [DropTriggerStatement expr] : K_DROP K_TRIGGER ( K_IF K_EXISTS )? (name= cident ) K_ON cf= columnFamilyName ;
	public final DropTriggerStatement dropTriggerStatement() throws RecognitionException {
		DropTriggerStatement expr = null;


		ColumnIdentifier.Raw name =null;
		CFName cf =null;

		 boolean ifExists = false; 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:794:5: ( K_DROP K_TRIGGER ( K_IF K_EXISTS )? (name= cident ) K_ON cf= columnFamilyName )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:794:7: K_DROP K_TRIGGER ( K_IF K_EXISTS )? (name= cident ) K_ON cf= columnFamilyName
			{
			match(input,K_DROP,FOLLOW_K_DROP_in_dropTriggerStatement4773); 
			match(input,K_TRIGGER,FOLLOW_K_TRIGGER_in_dropTriggerStatement4775); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:794:24: ( K_IF K_EXISTS )?
			int alt101=2;
			int LA101_0 = input.LA(1);
			if ( (LA101_0==K_IF) ) {
				alt101=1;
			}
			switch (alt101) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:794:25: K_IF K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_dropTriggerStatement4778); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropTriggerStatement4780); 
					 ifExists = true; 
					}
					break;

			}

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:794:63: (name= cident )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:794:64: name= cident
			{
			pushFollow(FOLLOW_cident_in_dropTriggerStatement4790);
			name=cident();
			state._fsp--;

			}

			match(input,K_ON,FOLLOW_K_ON_in_dropTriggerStatement4793); 
			pushFollow(FOLLOW_columnFamilyName_in_dropTriggerStatement4797);
			cf=columnFamilyName();
			state._fsp--;

			 expr = new DropTriggerStatement(cf, name.toString(), ifExists); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "dropTriggerStatement"



	// $ANTLR start "alterKeyspaceStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:801:1: alterKeyspaceStatement returns [AlterKeyspaceStatement expr] : K_ALTER K_KEYSPACE ks= keyspaceName K_WITH properties[attrs] ;
	public final AlterKeyspaceStatement alterKeyspaceStatement() throws RecognitionException {
		AlterKeyspaceStatement expr = null;


		String ks =null;

		 KeyspaceAttributes attrs = new KeyspaceAttributes(); 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:803:5: ( K_ALTER K_KEYSPACE ks= keyspaceName K_WITH properties[attrs] )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:803:7: K_ALTER K_KEYSPACE ks= keyspaceName K_WITH properties[attrs]
			{
			match(input,K_ALTER,FOLLOW_K_ALTER_in_alterKeyspaceStatement4837); 
			match(input,K_KEYSPACE,FOLLOW_K_KEYSPACE_in_alterKeyspaceStatement4839); 
			pushFollow(FOLLOW_keyspaceName_in_alterKeyspaceStatement4843);
			ks=keyspaceName();
			state._fsp--;

			match(input,K_WITH,FOLLOW_K_WITH_in_alterKeyspaceStatement4853); 
			pushFollow(FOLLOW_properties_in_alterKeyspaceStatement4855);
			properties(attrs);
			state._fsp--;

			 expr = new AlterKeyspaceStatement(ks, attrs); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "alterKeyspaceStatement"



	// $ANTLR start "alterTableStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:815:1: alterTableStatement returns [AlterTableStatement expr] : K_ALTER K_COLUMNFAMILY cf= columnFamilyName ( K_ALTER id= cident K_TYPE v= comparatorType | K_ADD id= cident v= comparatorType ( K_STATIC )? | K_DROP id= cident | K_DROP id= cident K_USING K_TIMESTAMP t= INTEGER | K_WITH properties[attrs] | K_RENAME id1= cident K_TO toId1= cident ( K_AND idn= cident K_TO toIdn= cident )* ) ;
	public final AlterTableStatement alterTableStatement() throws RecognitionException {
		AlterTableStatement expr = null;


		Token t=null;
		CFName cf =null;
		ColumnIdentifier.Raw id =null;
		CQL3Type.Raw v =null;
		ColumnIdentifier.Raw id1 =null;
		ColumnIdentifier.Raw toId1 =null;
		ColumnIdentifier.Raw idn =null;
		ColumnIdentifier.Raw toIdn =null;


		        AlterTableStatement.Type type = null;
		        TableAttributes attrs = new TableAttributes();
		        Map<ColumnIdentifier.Raw, ColumnIdentifier.Raw> renames = new HashMap<ColumnIdentifier.Raw, ColumnIdentifier.Raw>();
		        boolean isStatic = false;
		        Long dropTimestamp = null;
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:823:5: ( K_ALTER K_COLUMNFAMILY cf= columnFamilyName ( K_ALTER id= cident K_TYPE v= comparatorType | K_ADD id= cident v= comparatorType ( K_STATIC )? | K_DROP id= cident | K_DROP id= cident K_USING K_TIMESTAMP t= INTEGER | K_WITH properties[attrs] | K_RENAME id1= cident K_TO toId1= cident ( K_AND idn= cident K_TO toIdn= cident )* ) )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:823:7: K_ALTER K_COLUMNFAMILY cf= columnFamilyName ( K_ALTER id= cident K_TYPE v= comparatorType | K_ADD id= cident v= comparatorType ( K_STATIC )? | K_DROP id= cident | K_DROP id= cident K_USING K_TIMESTAMP t= INTEGER | K_WITH properties[attrs] | K_RENAME id1= cident K_TO toId1= cident ( K_AND idn= cident K_TO toIdn= cident )* )
			{
			match(input,K_ALTER,FOLLOW_K_ALTER_in_alterTableStatement4891); 
			match(input,K_COLUMNFAMILY,FOLLOW_K_COLUMNFAMILY_in_alterTableStatement4893); 
			pushFollow(FOLLOW_columnFamilyName_in_alterTableStatement4897);
			cf=columnFamilyName();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:824:11: ( K_ALTER id= cident K_TYPE v= comparatorType | K_ADD id= cident v= comparatorType ( K_STATIC )? | K_DROP id= cident | K_DROP id= cident K_USING K_TIMESTAMP t= INTEGER | K_WITH properties[attrs] | K_RENAME id1= cident K_TO toId1= cident ( K_AND idn= cident K_TO toIdn= cident )* )
			int alt104=6;
			alt104 = dfa104.predict(input);
			switch (alt104) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:824:13: K_ALTER id= cident K_TYPE v= comparatorType
					{
					match(input,K_ALTER,FOLLOW_K_ALTER_in_alterTableStatement4911); 
					pushFollow(FOLLOW_cident_in_alterTableStatement4915);
					id=cident();
					state._fsp--;

					match(input,K_TYPE,FOLLOW_K_TYPE_in_alterTableStatement4917); 
					pushFollow(FOLLOW_comparatorType_in_alterTableStatement4921);
					v=comparatorType();
					state._fsp--;

					 type = AlterTableStatement.Type.ALTER; 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:825:13: K_ADD id= cident v= comparatorType ( K_STATIC )?
					{
					match(input,K_ADD,FOLLOW_K_ADD_in_alterTableStatement4937); 
					pushFollow(FOLLOW_cident_in_alterTableStatement4943);
					id=cident();
					state._fsp--;

					pushFollow(FOLLOW_comparatorType_in_alterTableStatement4947);
					v=comparatorType();
					state._fsp--;

					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:825:48: ( K_STATIC )?
					int alt102=2;
					int LA102_0 = input.LA(1);
					if ( (LA102_0==K_STATIC) ) {
						alt102=1;
					}
					switch (alt102) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:825:49: K_STATIC
							{
							 isStatic=true; 
							match(input,K_STATIC,FOLLOW_K_STATIC_in_alterTableStatement4952); 
							}
							break;

					}

					 type = AlterTableStatement.Type.ADD; 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:826:13: K_DROP id= cident
					{
					match(input,K_DROP,FOLLOW_K_DROP_in_alterTableStatement4970); 
					pushFollow(FOLLOW_cident_in_alterTableStatement4975);
					id=cident();
					state._fsp--;

					 type = AlterTableStatement.Type.DROP; 
					}
					break;
				case 4 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:827:13: K_DROP id= cident K_USING K_TIMESTAMP t= INTEGER
					{
					match(input,K_DROP,FOLLOW_K_DROP_in_alterTableStatement5021); 
					pushFollow(FOLLOW_cident_in_alterTableStatement5026);
					id=cident();
					state._fsp--;

					match(input,K_USING,FOLLOW_K_USING_in_alterTableStatement5028); 
					match(input,K_TIMESTAMP,FOLLOW_K_TIMESTAMP_in_alterTableStatement5030); 
					t=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_alterTableStatement5034); 
					 type = AlterTableStatement.Type.DROP;
					                                                              dropTimestamp = Long.parseLong(Constants.Literal.integer((t!=null?t.getText():null)).getText()); 
					}
					break;
				case 5 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:829:13: K_WITH properties[attrs]
					{
					match(input,K_WITH,FOLLOW_K_WITH_in_alterTableStatement5050); 
					pushFollow(FOLLOW_properties_in_alterTableStatement5053);
					properties(attrs);
					state._fsp--;

					 type = AlterTableStatement.Type.OPTS; 
					}
					break;
				case 6 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:830:13: K_RENAME id1= cident K_TO toId1= cident ( K_AND idn= cident K_TO toIdn= cident )*
					{
					match(input,K_RENAME,FOLLOW_K_RENAME_in_alterTableStatement5092); 
					 type = AlterTableStatement.Type.RENAME; 
					pushFollow(FOLLOW_cident_in_alterTableStatement5152);
					id1=cident();
					state._fsp--;

					match(input,K_TO,FOLLOW_K_TO_in_alterTableStatement5154); 
					pushFollow(FOLLOW_cident_in_alterTableStatement5158);
					toId1=cident();
					state._fsp--;

					 renames.put(id1, toId1); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:832:16: ( K_AND idn= cident K_TO toIdn= cident )*
					loop103:
					while (true) {
						int alt103=2;
						int LA103_0 = input.LA(1);
						if ( (LA103_0==K_AND) ) {
							alt103=1;
						}

						switch (alt103) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:832:18: K_AND idn= cident K_TO toIdn= cident
							{
							match(input,K_AND,FOLLOW_K_AND_in_alterTableStatement5179); 
							pushFollow(FOLLOW_cident_in_alterTableStatement5183);
							idn=cident();
							state._fsp--;

							match(input,K_TO,FOLLOW_K_TO_in_alterTableStatement5185); 
							pushFollow(FOLLOW_cident_in_alterTableStatement5189);
							toIdn=cident();
							state._fsp--;

							 renames.put(idn, toIdn); 
							}
							break;

						default :
							break loop103;
						}
					}

					}
					break;

			}


			        expr = new AlterTableStatement(cf, type, id, v, attrs, renames, isStatic, dropTimestamp);
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "alterTableStatement"



	// $ANTLR start "alterMaterializedViewStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:839:1: alterMaterializedViewStatement returns [AlterViewStatement expr] : K_ALTER K_MATERIALIZED K_VIEW name= columnFamilyName K_WITH properties[attrs] ;
	public final AlterViewStatement alterMaterializedViewStatement() throws RecognitionException {
		AlterViewStatement expr = null;


		CFName name =null;


		        TableAttributes attrs = new TableAttributes();
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:843:5: ( K_ALTER K_MATERIALIZED K_VIEW name= columnFamilyName K_WITH properties[attrs] )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:843:7: K_ALTER K_MATERIALIZED K_VIEW name= columnFamilyName K_WITH properties[attrs]
			{
			match(input,K_ALTER,FOLLOW_K_ALTER_in_alterMaterializedViewStatement5242); 
			match(input,K_MATERIALIZED,FOLLOW_K_MATERIALIZED_in_alterMaterializedViewStatement5244); 
			match(input,K_VIEW,FOLLOW_K_VIEW_in_alterMaterializedViewStatement5246); 
			pushFollow(FOLLOW_columnFamilyName_in_alterMaterializedViewStatement5250);
			name=columnFamilyName();
			state._fsp--;

			match(input,K_WITH,FOLLOW_K_WITH_in_alterMaterializedViewStatement5262); 
			pushFollow(FOLLOW_properties_in_alterMaterializedViewStatement5264);
			properties(attrs);
			state._fsp--;


			        expr = new AlterViewStatement(name, attrs);
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "alterMaterializedViewStatement"



	// $ANTLR start "alterTypeStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:856:1: alterTypeStatement returns [AlterTypeStatement expr] : K_ALTER K_TYPE name= userTypeName ( K_ALTER f= noncol_ident K_TYPE v= comparatorType | K_ADD f= noncol_ident v= comparatorType | K_RENAME id1= noncol_ident K_TO toId1= noncol_ident ( K_AND idn= noncol_ident K_TO toIdn= noncol_ident )* ) ;
	public final AlterTypeStatement alterTypeStatement() throws RecognitionException {
		AlterTypeStatement expr = null;


		UTName name =null;
		ColumnIdentifier f =null;
		CQL3Type.Raw v =null;
		ColumnIdentifier id1 =null;
		ColumnIdentifier toId1 =null;
		ColumnIdentifier idn =null;
		ColumnIdentifier toIdn =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:857:5: ( K_ALTER K_TYPE name= userTypeName ( K_ALTER f= noncol_ident K_TYPE v= comparatorType | K_ADD f= noncol_ident v= comparatorType | K_RENAME id1= noncol_ident K_TO toId1= noncol_ident ( K_AND idn= noncol_ident K_TO toIdn= noncol_ident )* ) )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:857:7: K_ALTER K_TYPE name= userTypeName ( K_ALTER f= noncol_ident K_TYPE v= comparatorType | K_ADD f= noncol_ident v= comparatorType | K_RENAME id1= noncol_ident K_TO toId1= noncol_ident ( K_AND idn= noncol_ident K_TO toIdn= noncol_ident )* )
			{
			match(input,K_ALTER,FOLLOW_K_ALTER_in_alterTypeStatement5299); 
			match(input,K_TYPE,FOLLOW_K_TYPE_in_alterTypeStatement5301); 
			pushFollow(FOLLOW_userTypeName_in_alterTypeStatement5305);
			name=userTypeName();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:858:11: ( K_ALTER f= noncol_ident K_TYPE v= comparatorType | K_ADD f= noncol_ident v= comparatorType | K_RENAME id1= noncol_ident K_TO toId1= noncol_ident ( K_AND idn= noncol_ident K_TO toIdn= noncol_ident )* )
			int alt106=3;
			switch ( input.LA(1) ) {
			case K_ALTER:
				{
				alt106=1;
				}
				break;
			case K_ADD:
				{
				alt106=2;
				}
				break;
			case K_RENAME:
				{
				alt106=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 106, 0, input);
				throw nvae;
			}
			switch (alt106) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:858:13: K_ALTER f= noncol_ident K_TYPE v= comparatorType
					{
					match(input,K_ALTER,FOLLOW_K_ALTER_in_alterTypeStatement5319); 
					pushFollow(FOLLOW_noncol_ident_in_alterTypeStatement5323);
					f=noncol_ident();
					state._fsp--;

					match(input,K_TYPE,FOLLOW_K_TYPE_in_alterTypeStatement5325); 
					pushFollow(FOLLOW_comparatorType_in_alterTypeStatement5329);
					v=comparatorType();
					state._fsp--;

					 expr = AlterTypeStatement.alter(name, f, v); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:859:13: K_ADD f= noncol_ident v= comparatorType
					{
					match(input,K_ADD,FOLLOW_K_ADD_in_alterTypeStatement5345); 
					pushFollow(FOLLOW_noncol_ident_in_alterTypeStatement5351);
					f=noncol_ident();
					state._fsp--;

					pushFollow(FOLLOW_comparatorType_in_alterTypeStatement5355);
					v=comparatorType();
					state._fsp--;

					 expr = AlterTypeStatement.addition(name, f, v); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:860:13: K_RENAME id1= noncol_ident K_TO toId1= noncol_ident ( K_AND idn= noncol_ident K_TO toIdn= noncol_ident )*
					{
					match(input,K_RENAME,FOLLOW_K_RENAME_in_alterTypeStatement5378); 
					 Map<ColumnIdentifier, ColumnIdentifier> renames = new HashMap<ColumnIdentifier, ColumnIdentifier>(); 
					pushFollow(FOLLOW_noncol_ident_in_alterTypeStatement5416);
					id1=noncol_ident();
					state._fsp--;

					match(input,K_TO,FOLLOW_K_TO_in_alterTypeStatement5418); 
					pushFollow(FOLLOW_noncol_ident_in_alterTypeStatement5422);
					toId1=noncol_ident();
					state._fsp--;

					 renames.put(id1, toId1); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:863:18: ( K_AND idn= noncol_ident K_TO toIdn= noncol_ident )*
					loop105:
					while (true) {
						int alt105=2;
						int LA105_0 = input.LA(1);
						if ( (LA105_0==K_AND) ) {
							alt105=1;
						}

						switch (alt105) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:863:20: K_AND idn= noncol_ident K_TO toIdn= noncol_ident
							{
							match(input,K_AND,FOLLOW_K_AND_in_alterTypeStatement5445); 
							pushFollow(FOLLOW_noncol_ident_in_alterTypeStatement5449);
							idn=noncol_ident();
							state._fsp--;

							match(input,K_TO,FOLLOW_K_TO_in_alterTypeStatement5451); 
							pushFollow(FOLLOW_noncol_ident_in_alterTypeStatement5455);
							toIdn=noncol_ident();
							state._fsp--;

							 renames.put(idn, toIdn); 
							}
							break;

						default :
							break loop105;
						}
					}

					 expr = AlterTypeStatement.renames(name, renames); 
					}
					break;

			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "alterTypeStatement"



	// $ANTLR start "dropKeyspaceStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:872:1: dropKeyspaceStatement returns [DropKeyspaceStatement ksp] : K_DROP K_KEYSPACE ( K_IF K_EXISTS )? ks= keyspaceName ;
	public final DropKeyspaceStatement dropKeyspaceStatement() throws RecognitionException {
		DropKeyspaceStatement ksp = null;


		String ks =null;

		 boolean ifExists = false; 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:874:5: ( K_DROP K_KEYSPACE ( K_IF K_EXISTS )? ks= keyspaceName )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:874:7: K_DROP K_KEYSPACE ( K_IF K_EXISTS )? ks= keyspaceName
			{
			match(input,K_DROP,FOLLOW_K_DROP_in_dropKeyspaceStatement5522); 
			match(input,K_KEYSPACE,FOLLOW_K_KEYSPACE_in_dropKeyspaceStatement5524); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:874:25: ( K_IF K_EXISTS )?
			int alt107=2;
			int LA107_0 = input.LA(1);
			if ( (LA107_0==K_IF) ) {
				alt107=1;
			}
			switch (alt107) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:874:26: K_IF K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_dropKeyspaceStatement5527); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropKeyspaceStatement5529); 
					 ifExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_keyspaceName_in_dropKeyspaceStatement5538);
			ks=keyspaceName();
			state._fsp--;

			 ksp = new DropKeyspaceStatement(ks, ifExists); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return ksp;
	}
	// $ANTLR end "dropKeyspaceStatement"



	// $ANTLR start "dropTableStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:880:1: dropTableStatement returns [DropTableStatement stmt] : K_DROP K_COLUMNFAMILY ( K_IF K_EXISTS )? cf= columnFamilyName ;
	public final DropTableStatement dropTableStatement() throws RecognitionException {
		DropTableStatement stmt = null;


		CFName cf =null;

		 boolean ifExists = false; 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:882:5: ( K_DROP K_COLUMNFAMILY ( K_IF K_EXISTS )? cf= columnFamilyName )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:882:7: K_DROP K_COLUMNFAMILY ( K_IF K_EXISTS )? cf= columnFamilyName
			{
			match(input,K_DROP,FOLLOW_K_DROP_in_dropTableStatement5572); 
			match(input,K_COLUMNFAMILY,FOLLOW_K_COLUMNFAMILY_in_dropTableStatement5574); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:882:29: ( K_IF K_EXISTS )?
			int alt108=2;
			int LA108_0 = input.LA(1);
			if ( (LA108_0==K_IF) ) {
				alt108=1;
			}
			switch (alt108) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:882:30: K_IF K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_dropTableStatement5577); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropTableStatement5579); 
					 ifExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_columnFamilyName_in_dropTableStatement5588);
			cf=columnFamilyName();
			state._fsp--;

			 stmt = new DropTableStatement(cf, ifExists); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return stmt;
	}
	// $ANTLR end "dropTableStatement"



	// $ANTLR start "dropTypeStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:888:1: dropTypeStatement returns [DropTypeStatement stmt] : K_DROP K_TYPE ( K_IF K_EXISTS )? name= userTypeName ;
	public final DropTypeStatement dropTypeStatement() throws RecognitionException {
		DropTypeStatement stmt = null;


		UTName name =null;

		 boolean ifExists = false; 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:890:5: ( K_DROP K_TYPE ( K_IF K_EXISTS )? name= userTypeName )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:890:7: K_DROP K_TYPE ( K_IF K_EXISTS )? name= userTypeName
			{
			match(input,K_DROP,FOLLOW_K_DROP_in_dropTypeStatement5622); 
			match(input,K_TYPE,FOLLOW_K_TYPE_in_dropTypeStatement5624); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:890:21: ( K_IF K_EXISTS )?
			int alt109=2;
			int LA109_0 = input.LA(1);
			if ( (LA109_0==K_IF) ) {
				alt109=1;
			}
			switch (alt109) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:890:22: K_IF K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_dropTypeStatement5627); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropTypeStatement5629); 
					 ifExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_userTypeName_in_dropTypeStatement5638);
			name=userTypeName();
			state._fsp--;

			 stmt = new DropTypeStatement(name, ifExists); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return stmt;
	}
	// $ANTLR end "dropTypeStatement"



	// $ANTLR start "dropIndexStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:896:1: dropIndexStatement returns [DropIndexStatement expr] : K_DROP K_INDEX ( K_IF K_EXISTS )? index= indexName ;
	public final DropIndexStatement dropIndexStatement() throws RecognitionException {
		DropIndexStatement expr = null;


		IndexName index =null;

		 boolean ifExists = false; 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:898:5: ( K_DROP K_INDEX ( K_IF K_EXISTS )? index= indexName )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:898:7: K_DROP K_INDEX ( K_IF K_EXISTS )? index= indexName
			{
			match(input,K_DROP,FOLLOW_K_DROP_in_dropIndexStatement5672); 
			match(input,K_INDEX,FOLLOW_K_INDEX_in_dropIndexStatement5674); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:898:22: ( K_IF K_EXISTS )?
			int alt110=2;
			int LA110_0 = input.LA(1);
			if ( (LA110_0==K_IF) ) {
				alt110=1;
			}
			switch (alt110) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:898:23: K_IF K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_dropIndexStatement5677); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropIndexStatement5679); 
					 ifExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_indexName_in_dropIndexStatement5688);
			index=indexName();
			state._fsp--;

			 expr = new DropIndexStatement(index, ifExists); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "dropIndexStatement"



	// $ANTLR start "dropMaterializedViewStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:905:1: dropMaterializedViewStatement returns [DropViewStatement expr] : K_DROP K_MATERIALIZED K_VIEW ( K_IF K_EXISTS )? cf= columnFamilyName ;
	public final DropViewStatement dropMaterializedViewStatement() throws RecognitionException {
		DropViewStatement expr = null;


		CFName cf =null;

		 boolean ifExists = false; 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:907:5: ( K_DROP K_MATERIALIZED K_VIEW ( K_IF K_EXISTS )? cf= columnFamilyName )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:907:7: K_DROP K_MATERIALIZED K_VIEW ( K_IF K_EXISTS )? cf= columnFamilyName
			{
			match(input,K_DROP,FOLLOW_K_DROP_in_dropMaterializedViewStatement5728); 
			match(input,K_MATERIALIZED,FOLLOW_K_MATERIALIZED_in_dropMaterializedViewStatement5730); 
			match(input,K_VIEW,FOLLOW_K_VIEW_in_dropMaterializedViewStatement5732); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:907:36: ( K_IF K_EXISTS )?
			int alt111=2;
			int LA111_0 = input.LA(1);
			if ( (LA111_0==K_IF) ) {
				alt111=1;
			}
			switch (alt111) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:907:37: K_IF K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_dropMaterializedViewStatement5735); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropMaterializedViewStatement5737); 
					 ifExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_columnFamilyName_in_dropMaterializedViewStatement5746);
			cf=columnFamilyName();
			state._fsp--;

			 expr = new DropViewStatement(cf, ifExists); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return expr;
	}
	// $ANTLR end "dropMaterializedViewStatement"



	// $ANTLR start "truncateStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:914:1: truncateStatement returns [TruncateStatement stmt] : K_TRUNCATE ( K_COLUMNFAMILY )? cf= columnFamilyName ;
	public final TruncateStatement truncateStatement() throws RecognitionException {
		TruncateStatement stmt = null;


		CFName cf =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:915:5: ( K_TRUNCATE ( K_COLUMNFAMILY )? cf= columnFamilyName )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:915:7: K_TRUNCATE ( K_COLUMNFAMILY )? cf= columnFamilyName
			{
			match(input,K_TRUNCATE,FOLLOW_K_TRUNCATE_in_truncateStatement5777); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:915:18: ( K_COLUMNFAMILY )?
			int alt112=2;
			int LA112_0 = input.LA(1);
			if ( (LA112_0==K_COLUMNFAMILY) ) {
				alt112=1;
			}
			switch (alt112) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:915:19: K_COLUMNFAMILY
					{
					match(input,K_COLUMNFAMILY,FOLLOW_K_COLUMNFAMILY_in_truncateStatement5780); 
					}
					break;

			}

			pushFollow(FOLLOW_columnFamilyName_in_truncateStatement5786);
			cf=columnFamilyName();
			state._fsp--;

			 stmt = new TruncateStatement(cf); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return stmt;
	}
	// $ANTLR end "truncateStatement"



	// $ANTLR start "grantPermissionsStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:921:1: grantPermissionsStatement returns [GrantPermissionsStatement stmt] : K_GRANT permissionOrAll K_ON resource K_TO grantee= userOrRoleName ;
	public final GrantPermissionsStatement grantPermissionsStatement() throws RecognitionException {
		GrantPermissionsStatement stmt = null;


		RoleName grantee =null;
		Set<Permission> permissionOrAll1 =null;
		IResource resource2 =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:922:5: ( K_GRANT permissionOrAll K_ON resource K_TO grantee= userOrRoleName )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:922:7: K_GRANT permissionOrAll K_ON resource K_TO grantee= userOrRoleName
			{
			match(input,K_GRANT,FOLLOW_K_GRANT_in_grantPermissionsStatement5811); 
			pushFollow(FOLLOW_permissionOrAll_in_grantPermissionsStatement5823);
			permissionOrAll1=permissionOrAll();
			state._fsp--;

			match(input,K_ON,FOLLOW_K_ON_in_grantPermissionsStatement5831); 
			pushFollow(FOLLOW_resource_in_grantPermissionsStatement5843);
			resource2=resource();
			state._fsp--;

			match(input,K_TO,FOLLOW_K_TO_in_grantPermissionsStatement5851); 
			pushFollow(FOLLOW_userOrRoleName_in_grantPermissionsStatement5865);
			grantee=userOrRoleName();
			state._fsp--;

			 stmt = new GrantPermissionsStatement(filterPermissions(permissionOrAll1, resource2), resource2, grantee); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return stmt;
	}
	// $ANTLR end "grantPermissionsStatement"



	// $ANTLR start "revokePermissionsStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:934:1: revokePermissionsStatement returns [RevokePermissionsStatement stmt] : K_REVOKE permissionOrAll K_ON resource K_FROM revokee= userOrRoleName ;
	public final RevokePermissionsStatement revokePermissionsStatement() throws RecognitionException {
		RevokePermissionsStatement stmt = null;


		RoleName revokee =null;
		Set<Permission> permissionOrAll3 =null;
		IResource resource4 =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:935:5: ( K_REVOKE permissionOrAll K_ON resource K_FROM revokee= userOrRoleName )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:935:7: K_REVOKE permissionOrAll K_ON resource K_FROM revokee= userOrRoleName
			{
			match(input,K_REVOKE,FOLLOW_K_REVOKE_in_revokePermissionsStatement5896); 
			pushFollow(FOLLOW_permissionOrAll_in_revokePermissionsStatement5908);
			permissionOrAll3=permissionOrAll();
			state._fsp--;

			match(input,K_ON,FOLLOW_K_ON_in_revokePermissionsStatement5916); 
			pushFollow(FOLLOW_resource_in_revokePermissionsStatement5928);
			resource4=resource();
			state._fsp--;

			match(input,K_FROM,FOLLOW_K_FROM_in_revokePermissionsStatement5936); 
			pushFollow(FOLLOW_userOrRoleName_in_revokePermissionsStatement5950);
			revokee=userOrRoleName();
			state._fsp--;

			 stmt = new RevokePermissionsStatement(filterPermissions(permissionOrAll3, resource4), resource4, revokee); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return stmt;
	}
	// $ANTLR end "revokePermissionsStatement"



	// $ANTLR start "grantRoleStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:947:1: grantRoleStatement returns [GrantRoleStatement stmt] : K_GRANT role= userOrRoleName K_TO grantee= userOrRoleName ;
	public final GrantRoleStatement grantRoleStatement() throws RecognitionException {
		GrantRoleStatement stmt = null;


		RoleName role =null;
		RoleName grantee =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:948:5: ( K_GRANT role= userOrRoleName K_TO grantee= userOrRoleName )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:948:7: K_GRANT role= userOrRoleName K_TO grantee= userOrRoleName
			{
			match(input,K_GRANT,FOLLOW_K_GRANT_in_grantRoleStatement5981); 
			pushFollow(FOLLOW_userOrRoleName_in_grantRoleStatement5995);
			role=userOrRoleName();
			state._fsp--;

			match(input,K_TO,FOLLOW_K_TO_in_grantRoleStatement6003); 
			pushFollow(FOLLOW_userOrRoleName_in_grantRoleStatement6017);
			grantee=userOrRoleName();
			state._fsp--;

			 stmt = new GrantRoleStatement(role, grantee); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return stmt;
	}
	// $ANTLR end "grantRoleStatement"



	// $ANTLR start "revokeRoleStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:958:1: revokeRoleStatement returns [RevokeRoleStatement stmt] : K_REVOKE role= userOrRoleName K_FROM revokee= userOrRoleName ;
	public final RevokeRoleStatement revokeRoleStatement() throws RecognitionException {
		RevokeRoleStatement stmt = null;


		RoleName role =null;
		RoleName revokee =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:959:5: ( K_REVOKE role= userOrRoleName K_FROM revokee= userOrRoleName )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:959:7: K_REVOKE role= userOrRoleName K_FROM revokee= userOrRoleName
			{
			match(input,K_REVOKE,FOLLOW_K_REVOKE_in_revokeRoleStatement6048); 
			pushFollow(FOLLOW_userOrRoleName_in_revokeRoleStatement6062);
			role=userOrRoleName();
			state._fsp--;

			match(input,K_FROM,FOLLOW_K_FROM_in_revokeRoleStatement6070); 
			pushFollow(FOLLOW_userOrRoleName_in_revokeRoleStatement6084);
			revokee=userOrRoleName();
			state._fsp--;

			 stmt = new RevokeRoleStatement(role, revokee); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return stmt;
	}
	// $ANTLR end "revokeRoleStatement"



	// $ANTLR start "listPermissionsStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:966:1: listPermissionsStatement returns [ListPermissionsStatement stmt] : K_LIST permissionOrAll ( K_ON resource )? ( K_OF roleName[grantee] )? ( K_NORECURSIVE )? ;
	public final ListPermissionsStatement listPermissionsStatement() throws RecognitionException {
		ListPermissionsStatement stmt = null;


		IResource resource5 =null;
		Set<Permission> permissionOrAll6 =null;


		        IResource resource = null;
		        boolean recursive = true;
		        RoleName grantee = new RoleName();
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:972:5: ( K_LIST permissionOrAll ( K_ON resource )? ( K_OF roleName[grantee] )? ( K_NORECURSIVE )? )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:972:7: K_LIST permissionOrAll ( K_ON resource )? ( K_OF roleName[grantee] )? ( K_NORECURSIVE )?
			{
			match(input,K_LIST,FOLLOW_K_LIST_in_listPermissionsStatement6122); 
			pushFollow(FOLLOW_permissionOrAll_in_listPermissionsStatement6134);
			permissionOrAll6=permissionOrAll();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:974:7: ( K_ON resource )?
			int alt113=2;
			int LA113_0 = input.LA(1);
			if ( (LA113_0==K_ON) ) {
				alt113=1;
			}
			switch (alt113) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:974:9: K_ON resource
					{
					match(input,K_ON,FOLLOW_K_ON_in_listPermissionsStatement6144); 
					pushFollow(FOLLOW_resource_in_listPermissionsStatement6146);
					resource5=resource();
					state._fsp--;

					 resource = resource5; 
					}
					break;

			}

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:975:7: ( K_OF roleName[grantee] )?
			int alt114=2;
			int LA114_0 = input.LA(1);
			if ( (LA114_0==K_OF) ) {
				alt114=1;
			}
			switch (alt114) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:975:9: K_OF roleName[grantee]
					{
					match(input,K_OF,FOLLOW_K_OF_in_listPermissionsStatement6161); 
					pushFollow(FOLLOW_roleName_in_listPermissionsStatement6163);
					roleName(grantee);
					state._fsp--;

					}
					break;

			}

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:976:7: ( K_NORECURSIVE )?
			int alt115=2;
			int LA115_0 = input.LA(1);
			if ( (LA115_0==K_NORECURSIVE) ) {
				alt115=1;
			}
			switch (alt115) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:976:9: K_NORECURSIVE
					{
					match(input,K_NORECURSIVE,FOLLOW_K_NORECURSIVE_in_listPermissionsStatement6177); 
					 recursive = false; 
					}
					break;

			}

			 stmt = new ListPermissionsStatement(permissionOrAll6, resource, grantee, recursive); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return stmt;
	}
	// $ANTLR end "listPermissionsStatement"



	// $ANTLR start "permission"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:980:1: permission returns [Permission perm] : p= ( K_CREATE | K_ALTER | K_DROP | K_SELECT | K_MODIFY | K_AUTHORIZE | K_DESCRIBE | K_EXECUTE ) ;
	public final Permission permission() throws RecognitionException {
		Permission perm = null;


		Token p=null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:981:5: (p= ( K_CREATE | K_ALTER | K_DROP | K_SELECT | K_MODIFY | K_AUTHORIZE | K_DESCRIBE | K_EXECUTE ) )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:981:7: p= ( K_CREATE | K_ALTER | K_DROP | K_SELECT | K_MODIFY | K_AUTHORIZE | K_DESCRIBE | K_EXECUTE )
			{
			p=input.LT(1);
			if ( input.LA(1)==K_ALTER||input.LA(1)==K_AUTHORIZE||input.LA(1)==K_CREATE||input.LA(1)==K_DESCRIBE||input.LA(1)==K_DROP||input.LA(1)==K_EXECUTE||input.LA(1)==K_MODIFY||input.LA(1)==K_SELECT ) {
				input.consume();
				state.errorRecovery=false;
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			 perm = Permission.valueOf((p!=null?p.getText():null).toUpperCase()); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return perm;
	}
	// $ANTLR end "permission"



	// $ANTLR start "permissionOrAll"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:985:1: permissionOrAll returns [Set<Permission> perms] : ( K_ALL ( K_PERMISSIONS )? |p= permission ( K_PERMISSION )? );
	public final Set<Permission> permissionOrAll() throws RecognitionException {
		Set<Permission> perms = null;


		Permission p =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:986:5: ( K_ALL ( K_PERMISSIONS )? |p= permission ( K_PERMISSION )? )
			int alt118=2;
			int LA118_0 = input.LA(1);
			if ( (LA118_0==K_ALL) ) {
				alt118=1;
			}
			else if ( (LA118_0==K_ALTER||LA118_0==K_AUTHORIZE||LA118_0==K_CREATE||LA118_0==K_DESCRIBE||LA118_0==K_DROP||LA118_0==K_EXECUTE||LA118_0==K_MODIFY||LA118_0==K_SELECT) ) {
				alt118=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 118, 0, input);
				throw nvae;
			}

			switch (alt118) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:986:7: K_ALL ( K_PERMISSIONS )?
					{
					match(input,K_ALL,FOLLOW_K_ALL_in_permissionOrAll6270); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:986:13: ( K_PERMISSIONS )?
					int alt116=2;
					int LA116_0 = input.LA(1);
					if ( (LA116_0==K_PERMISSIONS) ) {
						alt116=1;
					}
					switch (alt116) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:986:15: K_PERMISSIONS
							{
							match(input,K_PERMISSIONS,FOLLOW_K_PERMISSIONS_in_permissionOrAll6274); 
							}
							break;

					}

					 perms = Permission.ALL; 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:987:7: p= permission ( K_PERMISSION )?
					{
					pushFollow(FOLLOW_permission_in_permissionOrAll6295);
					p=permission();
					state._fsp--;

					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:987:20: ( K_PERMISSION )?
					int alt117=2;
					int LA117_0 = input.LA(1);
					if ( (LA117_0==K_PERMISSION) ) {
						alt117=1;
					}
					switch (alt117) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:987:22: K_PERMISSION
							{
							match(input,K_PERMISSION,FOLLOW_K_PERMISSION_in_permissionOrAll6299); 
							}
							break;

					}

					 perms = EnumSet.of(p); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return perms;
	}
	// $ANTLR end "permissionOrAll"



	// $ANTLR start "resource"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:990:1: resource returns [IResource res] : (d= dataResource |r= roleResource |f= functionResource );
	public final IResource resource() throws RecognitionException {
		IResource res = null;


		DataResource d =null;
		RoleResource r =null;
		FunctionResource f =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:991:5: (d= dataResource |r= roleResource |f= functionResource )
			int alt119=3;
			switch ( input.LA(1) ) {
			case K_ALL:
				{
				switch ( input.LA(2) ) {
				case EOF:
				case K_FROM:
				case K_KEYSPACES:
				case K_NORECURSIVE:
				case K_OF:
				case K_TO:
				case 176:
				case 178:
					{
					alt119=1;
					}
					break;
				case K_ROLES:
					{
					alt119=2;
					}
					break;
				case K_FUNCTIONS:
					{
					alt119=3;
					}
					break;
				default:
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 119, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
				}
				break;
			case IDENT:
			case K_AGGREGATE:
			case K_AS:
			case K_ASCII:
			case K_BIGINT:
			case K_BLOB:
			case K_BOOLEAN:
			case K_CALLED:
			case K_CLUSTERING:
			case K_COLUMNFAMILY:
			case K_COMPACT:
			case K_CONTAINS:
			case K_COUNT:
			case K_COUNTER:
			case K_CUSTOM:
			case K_DATE:
			case K_DECIMAL:
			case K_DISTINCT:
			case K_DOUBLE:
			case K_EXISTS:
			case K_FILTERING:
			case K_FINALFUNC:
			case K_FLOAT:
			case K_FROZEN:
			case K_FUNCTIONS:
			case K_INET:
			case K_INITCOND:
			case K_INPUT:
			case K_INT:
			case K_JSON:
			case K_KEY:
			case K_KEYS:
			case K_KEYSPACE:
			case K_KEYSPACES:
			case K_LANGUAGE:
			case K_LIST:
			case K_LOGIN:
			case K_MAP:
			case K_NOLOGIN:
			case K_NOSUPERUSER:
			case K_OPTIONS:
			case K_PASSWORD:
			case K_PERMISSION:
			case K_PERMISSIONS:
			case K_RETURNS:
			case K_ROLES:
			case K_SFUNC:
			case K_SMALLINT:
			case K_STATIC:
			case K_STORAGE:
			case K_STYPE:
			case K_SUPERUSER:
			case K_TEXT:
			case K_TIME:
			case K_TIMESTAMP:
			case K_TIMEUUID:
			case K_TINYINT:
			case K_TRIGGER:
			case K_TTL:
			case K_TUPLE:
			case K_TYPE:
			case K_USER:
			case K_USERS:
			case K_UUID:
			case K_VALUES:
			case K_VARCHAR:
			case K_VARINT:
			case K_WRITETIME:
			case QMARK:
			case QUOTED_NAME:
				{
				alt119=1;
				}
				break;
			case K_ROLE:
				{
				int LA119_3 = input.LA(2);
				if ( (LA119_3==EOF||LA119_3==K_FROM||LA119_3==K_NORECURSIVE||LA119_3==K_OF||LA119_3==K_TO||LA119_3==176||LA119_3==178) ) {
					alt119=1;
				}
				else if ( (LA119_3==IDENT||(LA119_3 >= K_AGGREGATE && LA119_3 <= K_ALL)||LA119_3==K_AS||LA119_3==K_ASCII||(LA119_3 >= K_BIGINT && LA119_3 <= K_BOOLEAN)||(LA119_3 >= K_CALLED && LA119_3 <= K_CLUSTERING)||(LA119_3 >= K_COMPACT && LA119_3 <= K_COUNTER)||(LA119_3 >= K_CUSTOM && LA119_3 <= K_DECIMAL)||(LA119_3 >= K_DISTINCT && LA119_3 <= K_DOUBLE)||(LA119_3 >= K_EXISTS && LA119_3 <= K_FLOAT)||LA119_3==K_FROZEN||(LA119_3 >= K_FUNCTION && LA119_3 <= K_FUNCTIONS)||LA119_3==K_INET||(LA119_3 >= K_INITCOND && LA119_3 <= K_INPUT)||LA119_3==K_INT||(LA119_3 >= K_JSON && LA119_3 <= K_KEYS)||(LA119_3 >= K_KEYSPACES && LA119_3 <= K_LANGUAGE)||(LA119_3 >= K_LIST && LA119_3 <= K_MAP)||LA119_3==K_NOLOGIN||LA119_3==K_NOSUPERUSER||LA119_3==K_OPTIONS||(LA119_3 >= K_PASSWORD && LA119_3 <= K_PERMISSIONS)||LA119_3==K_RETURNS||(LA119_3 >= K_ROLE && LA119_3 <= K_ROLES)||(LA119_3 >= K_SFUNC && LA119_3 <= K_TINYINT)||LA119_3==K_TRIGGER||(LA119_3 >= K_TTL && LA119_3 <= K_TYPE)||(LA119_3 >= K_USER && LA119_3 <= K_USERS)||(LA119_3 >= K_UUID && LA119_3 <= K_VARINT)||LA119_3==K_WRITETIME||(LA119_3 >= QMARK && LA119_3 <= QUOTED_NAME)||LA119_3==STRING_LITERAL) ) {
					alt119=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 119, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case K_FUNCTION:
				{
				int LA119_4 = input.LA(2);
				if ( (LA119_4==EOF||LA119_4==K_FROM||LA119_4==K_NORECURSIVE||LA119_4==K_OF||LA119_4==K_TO||LA119_4==176||LA119_4==178) ) {
					alt119=1;
				}
				else if ( (LA119_4==IDENT||(LA119_4 >= K_AGGREGATE && LA119_4 <= K_ALL)||LA119_4==K_AS||LA119_4==K_ASCII||(LA119_4 >= K_BIGINT && LA119_4 <= K_BOOLEAN)||(LA119_4 >= K_CALLED && LA119_4 <= K_CLUSTERING)||(LA119_4 >= K_COMPACT && LA119_4 <= K_COUNTER)||(LA119_4 >= K_CUSTOM && LA119_4 <= K_DECIMAL)||(LA119_4 >= K_DISTINCT && LA119_4 <= K_DOUBLE)||(LA119_4 >= K_EXISTS && LA119_4 <= K_FLOAT)||LA119_4==K_FROZEN||(LA119_4 >= K_FUNCTION && LA119_4 <= K_FUNCTIONS)||LA119_4==K_INET||(LA119_4 >= K_INITCOND && LA119_4 <= K_INPUT)||LA119_4==K_INT||(LA119_4 >= K_JSON && LA119_4 <= K_KEYS)||(LA119_4 >= K_KEYSPACES && LA119_4 <= K_LANGUAGE)||(LA119_4 >= K_LIST && LA119_4 <= K_MAP)||LA119_4==K_NOLOGIN||LA119_4==K_NOSUPERUSER||LA119_4==K_OPTIONS||(LA119_4 >= K_PASSWORD && LA119_4 <= K_PERMISSIONS)||LA119_4==K_RETURNS||(LA119_4 >= K_ROLE && LA119_4 <= K_ROLES)||(LA119_4 >= K_SFUNC && LA119_4 <= K_TINYINT)||(LA119_4 >= K_TOKEN && LA119_4 <= K_TRIGGER)||(LA119_4 >= K_TTL && LA119_4 <= K_TYPE)||(LA119_4 >= K_USER && LA119_4 <= K_USERS)||(LA119_4 >= K_UUID && LA119_4 <= K_VARINT)||LA119_4==K_WRITETIME||(LA119_4 >= QMARK && LA119_4 <= QUOTED_NAME)) ) {
					alt119=3;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 119, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 119, 0, input);
				throw nvae;
			}
			switch (alt119) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:991:7: d= dataResource
					{
					pushFollow(FOLLOW_dataResource_in_resource6327);
					d=dataResource();
					state._fsp--;

					 res = d; 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:992:7: r= roleResource
					{
					pushFollow(FOLLOW_roleResource_in_resource6339);
					r=roleResource();
					state._fsp--;

					 res = r; 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:993:7: f= functionResource
					{
					pushFollow(FOLLOW_functionResource_in_resource6351);
					f=functionResource();
					state._fsp--;

					 res = f; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return res;
	}
	// $ANTLR end "resource"



	// $ANTLR start "dataResource"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:996:1: dataResource returns [DataResource res] : ( K_ALL K_KEYSPACES | K_KEYSPACE ks= keyspaceName | ( K_COLUMNFAMILY )? cf= columnFamilyName );
	public final DataResource dataResource() throws RecognitionException {
		DataResource res = null;


		String ks =null;
		CFName cf =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:997:5: ( K_ALL K_KEYSPACES | K_KEYSPACE ks= keyspaceName | ( K_COLUMNFAMILY )? cf= columnFamilyName )
			int alt121=3;
			switch ( input.LA(1) ) {
			case K_ALL:
				{
				int LA121_1 = input.LA(2);
				if ( (LA121_1==K_KEYSPACES) ) {
					alt121=1;
				}
				else if ( (LA121_1==EOF||LA121_1==K_FROM||LA121_1==K_NORECURSIVE||LA121_1==K_OF||LA121_1==K_TO||LA121_1==176||LA121_1==178) ) {
					alt121=3;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 121, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case K_KEYSPACE:
				{
				alt121=2;
				}
				break;
			case IDENT:
			case K_AGGREGATE:
			case K_AS:
			case K_ASCII:
			case K_BIGINT:
			case K_BLOB:
			case K_BOOLEAN:
			case K_CALLED:
			case K_CLUSTERING:
			case K_COLUMNFAMILY:
			case K_COMPACT:
			case K_CONTAINS:
			case K_COUNT:
			case K_COUNTER:
			case K_CUSTOM:
			case K_DATE:
			case K_DECIMAL:
			case K_DISTINCT:
			case K_DOUBLE:
			case K_EXISTS:
			case K_FILTERING:
			case K_FINALFUNC:
			case K_FLOAT:
			case K_FROZEN:
			case K_FUNCTION:
			case K_FUNCTIONS:
			case K_INET:
			case K_INITCOND:
			case K_INPUT:
			case K_INT:
			case K_JSON:
			case K_KEY:
			case K_KEYS:
			case K_KEYSPACES:
			case K_LANGUAGE:
			case K_LIST:
			case K_LOGIN:
			case K_MAP:
			case K_NOLOGIN:
			case K_NOSUPERUSER:
			case K_OPTIONS:
			case K_PASSWORD:
			case K_PERMISSION:
			case K_PERMISSIONS:
			case K_RETURNS:
			case K_ROLE:
			case K_ROLES:
			case K_SFUNC:
			case K_SMALLINT:
			case K_STATIC:
			case K_STORAGE:
			case K_STYPE:
			case K_SUPERUSER:
			case K_TEXT:
			case K_TIME:
			case K_TIMESTAMP:
			case K_TIMEUUID:
			case K_TINYINT:
			case K_TRIGGER:
			case K_TTL:
			case K_TUPLE:
			case K_TYPE:
			case K_USER:
			case K_USERS:
			case K_UUID:
			case K_VALUES:
			case K_VARCHAR:
			case K_VARINT:
			case K_WRITETIME:
			case QMARK:
			case QUOTED_NAME:
				{
				alt121=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 121, 0, input);
				throw nvae;
			}
			switch (alt121) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:997:7: K_ALL K_KEYSPACES
					{
					match(input,K_ALL,FOLLOW_K_ALL_in_dataResource6374); 
					match(input,K_KEYSPACES,FOLLOW_K_KEYSPACES_in_dataResource6376); 
					 res = DataResource.root(); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:998:7: K_KEYSPACE ks= keyspaceName
					{
					match(input,K_KEYSPACE,FOLLOW_K_KEYSPACE_in_dataResource6386); 
					pushFollow(FOLLOW_keyspaceName_in_dataResource6392);
					ks=keyspaceName();
					state._fsp--;

					 res = DataResource.keyspace(ks); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:999:7: ( K_COLUMNFAMILY )? cf= columnFamilyName
					{
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:999:7: ( K_COLUMNFAMILY )?
					int alt120=2;
					int LA120_0 = input.LA(1);
					if ( (LA120_0==K_COLUMNFAMILY) ) {
						alt120=1;
					}
					switch (alt120) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:999:9: K_COLUMNFAMILY
							{
							match(input,K_COLUMNFAMILY,FOLLOW_K_COLUMNFAMILY_in_dataResource6404); 
							}
							break;

					}

					pushFollow(FOLLOW_columnFamilyName_in_dataResource6413);
					cf=columnFamilyName();
					state._fsp--;

					 res = DataResource.table(cf.getKeyspace(), cf.getColumnFamily()); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return res;
	}
	// $ANTLR end "dataResource"



	// $ANTLR start "roleResource"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1003:1: roleResource returns [RoleResource res] : ( K_ALL K_ROLES | K_ROLE role= userOrRoleName );
	public final RoleResource roleResource() throws RecognitionException {
		RoleResource res = null;


		RoleName role =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1004:5: ( K_ALL K_ROLES | K_ROLE role= userOrRoleName )
			int alt122=2;
			int LA122_0 = input.LA(1);
			if ( (LA122_0==K_ALL) ) {
				alt122=1;
			}
			else if ( (LA122_0==K_ROLE) ) {
				alt122=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 122, 0, input);
				throw nvae;
			}

			switch (alt122) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1004:7: K_ALL K_ROLES
					{
					match(input,K_ALL,FOLLOW_K_ALL_in_roleResource6442); 
					match(input,K_ROLES,FOLLOW_K_ROLES_in_roleResource6444); 
					 res = RoleResource.root(); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1005:7: K_ROLE role= userOrRoleName
					{
					match(input,K_ROLE,FOLLOW_K_ROLE_in_roleResource6454); 
					pushFollow(FOLLOW_userOrRoleName_in_roleResource6460);
					role=userOrRoleName();
					state._fsp--;

					 res = RoleResource.role(role.getName()); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return res;
	}
	// $ANTLR end "roleResource"



	// $ANTLR start "functionResource"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1008:1: functionResource returns [FunctionResource res] : ( K_ALL K_FUNCTIONS | K_ALL K_FUNCTIONS K_IN K_KEYSPACE ks= keyspaceName | K_FUNCTION fn= functionName ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' ) );
	public final FunctionResource functionResource() throws RecognitionException {
		FunctionResource res = null;


		String ks =null;
		FunctionName fn =null;
		CQL3Type.Raw v =null;


		        List<CQL3Type.Raw> argsTypes = new ArrayList<>();
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1012:5: ( K_ALL K_FUNCTIONS | K_ALL K_FUNCTIONS K_IN K_KEYSPACE ks= keyspaceName | K_FUNCTION fn= functionName ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' ) )
			int alt125=3;
			int LA125_0 = input.LA(1);
			if ( (LA125_0==K_ALL) ) {
				int LA125_1 = input.LA(2);
				if ( (LA125_1==K_FUNCTIONS) ) {
					int LA125_3 = input.LA(3);
					if ( (LA125_3==K_IN) ) {
						alt125=2;
					}
					else if ( (LA125_3==EOF||LA125_3==K_FROM||LA125_3==K_NORECURSIVE||LA125_3==K_OF||LA125_3==K_TO||LA125_3==178) ) {
						alt125=1;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 125, 3, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 125, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA125_0==K_FUNCTION) ) {
				alt125=3;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 125, 0, input);
				throw nvae;
			}

			switch (alt125) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1012:7: K_ALL K_FUNCTIONS
					{
					match(input,K_ALL,FOLLOW_K_ALL_in_functionResource6492); 
					match(input,K_FUNCTIONS,FOLLOW_K_FUNCTIONS_in_functionResource6494); 
					 res = FunctionResource.root(); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1013:7: K_ALL K_FUNCTIONS K_IN K_KEYSPACE ks= keyspaceName
					{
					match(input,K_ALL,FOLLOW_K_ALL_in_functionResource6504); 
					match(input,K_FUNCTIONS,FOLLOW_K_FUNCTIONS_in_functionResource6506); 
					match(input,K_IN,FOLLOW_K_IN_in_functionResource6508); 
					match(input,K_KEYSPACE,FOLLOW_K_KEYSPACE_in_functionResource6510); 
					pushFollow(FOLLOW_keyspaceName_in_functionResource6516);
					ks=keyspaceName();
					state._fsp--;

					 res = FunctionResource.keyspace(ks); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1015:7: K_FUNCTION fn= functionName ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )
					{
					match(input,K_FUNCTION,FOLLOW_K_FUNCTION_in_functionResource6531); 
					pushFollow(FOLLOW_functionName_in_functionResource6535);
					fn=functionName();
					state._fsp--;

					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1016:7: ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1017:9: '(' (v= comparatorType ( ',' v= comparatorType )* )? ')'
					{
					match(input,171,FOLLOW_171_in_functionResource6553); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1018:11: (v= comparatorType ( ',' v= comparatorType )* )?
					int alt124=2;
					int LA124_0 = input.LA(1);
					if ( (LA124_0==IDENT||(LA124_0 >= K_AGGREGATE && LA124_0 <= K_ALL)||LA124_0==K_AS||LA124_0==K_ASCII||(LA124_0 >= K_BIGINT && LA124_0 <= K_BOOLEAN)||(LA124_0 >= K_CALLED && LA124_0 <= K_CLUSTERING)||(LA124_0 >= K_COMPACT && LA124_0 <= K_COUNTER)||(LA124_0 >= K_CUSTOM && LA124_0 <= K_DECIMAL)||(LA124_0 >= K_DISTINCT && LA124_0 <= K_DOUBLE)||(LA124_0 >= K_EXISTS && LA124_0 <= K_FLOAT)||LA124_0==K_FROZEN||(LA124_0 >= K_FUNCTION && LA124_0 <= K_FUNCTIONS)||LA124_0==K_INET||(LA124_0 >= K_INITCOND && LA124_0 <= K_INPUT)||LA124_0==K_INT||(LA124_0 >= K_JSON && LA124_0 <= K_KEYS)||(LA124_0 >= K_KEYSPACES && LA124_0 <= K_LANGUAGE)||(LA124_0 >= K_LIST && LA124_0 <= K_MAP)||LA124_0==K_NOLOGIN||LA124_0==K_NOSUPERUSER||LA124_0==K_OPTIONS||(LA124_0 >= K_PASSWORD && LA124_0 <= K_PERMISSIONS)||LA124_0==K_RETURNS||(LA124_0 >= K_ROLE && LA124_0 <= K_ROLES)||(LA124_0 >= K_SET && LA124_0 <= K_TINYINT)||LA124_0==K_TRIGGER||(LA124_0 >= K_TTL && LA124_0 <= K_TYPE)||(LA124_0 >= K_USER && LA124_0 <= K_USERS)||(LA124_0 >= K_UUID && LA124_0 <= K_VARINT)||LA124_0==K_WRITETIME||LA124_0==QUOTED_NAME||LA124_0==STRING_LITERAL) ) {
						alt124=1;
					}
					switch (alt124) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1019:13: v= comparatorType ( ',' v= comparatorType )*
							{
							pushFollow(FOLLOW_comparatorType_in_functionResource6581);
							v=comparatorType();
							state._fsp--;

							 argsTypes.add(v); 
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1020:13: ( ',' v= comparatorType )*
							loop123:
							while (true) {
								int alt123=2;
								int LA123_0 = input.LA(1);
								if ( (LA123_0==174) ) {
									alt123=1;
								}

								switch (alt123) {
								case 1 :
									// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1020:15: ',' v= comparatorType
									{
									match(input,174,FOLLOW_174_in_functionResource6599); 
									pushFollow(FOLLOW_comparatorType_in_functionResource6603);
									v=comparatorType();
									state._fsp--;

									 argsTypes.add(v); 
									}
									break;

								default :
									break loop123;
								}
							}

							}
							break;

					}

					match(input,172,FOLLOW_172_in_functionResource6631); 
					}

					 res = FunctionResource.functionFromCql(fn.keyspace, fn.name, argsTypes); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return res;
	}
	// $ANTLR end "functionResource"



	// $ANTLR start "createUserStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1030:1: createUserStatement returns [CreateRoleStatement stmt] : K_CREATE K_USER ( K_IF K_NOT K_EXISTS )? u= username ( K_WITH userPassword[opts] )? ( K_SUPERUSER | K_NOSUPERUSER )? ;
	public final CreateRoleStatement createUserStatement() throws RecognitionException {
		CreateRoleStatement stmt = null;


		ParserRuleReturnScope u =null;


		        RoleOptions opts = new RoleOptions();
		        opts.setOption(IRoleManager.Option.LOGIN, true);
		        boolean superuser = false;
		        boolean ifNotExists = false;
		        RoleName name = new RoleName();
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1038:5: ( K_CREATE K_USER ( K_IF K_NOT K_EXISTS )? u= username ( K_WITH userPassword[opts] )? ( K_SUPERUSER | K_NOSUPERUSER )? )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1038:7: K_CREATE K_USER ( K_IF K_NOT K_EXISTS )? u= username ( K_WITH userPassword[opts] )? ( K_SUPERUSER | K_NOSUPERUSER )?
			{
			match(input,K_CREATE,FOLLOW_K_CREATE_in_createUserStatement6679); 
			match(input,K_USER,FOLLOW_K_USER_in_createUserStatement6681); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1038:23: ( K_IF K_NOT K_EXISTS )?
			int alt126=2;
			int LA126_0 = input.LA(1);
			if ( (LA126_0==K_IF) ) {
				alt126=1;
			}
			switch (alt126) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1038:24: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_createUserStatement6684); 
					match(input,K_NOT,FOLLOW_K_NOT_in_createUserStatement6686); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createUserStatement6688); 
					 ifNotExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_username_in_createUserStatement6696);
			u=username();
			state._fsp--;

			 name.setName((u!=null?input.toString(u.start,u.stop):null), true); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1039:7: ( K_WITH userPassword[opts] )?
			int alt127=2;
			int LA127_0 = input.LA(1);
			if ( (LA127_0==K_WITH) ) {
				alt127=1;
			}
			switch (alt127) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1039:9: K_WITH userPassword[opts]
					{
					match(input,K_WITH,FOLLOW_K_WITH_in_createUserStatement6708); 
					pushFollow(FOLLOW_userPassword_in_createUserStatement6710);
					userPassword(opts);
					state._fsp--;

					}
					break;

			}

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1040:7: ( K_SUPERUSER | K_NOSUPERUSER )?
			int alt128=3;
			int LA128_0 = input.LA(1);
			if ( (LA128_0==K_SUPERUSER) ) {
				alt128=1;
			}
			else if ( (LA128_0==K_NOSUPERUSER) ) {
				alt128=2;
			}
			switch (alt128) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1040:9: K_SUPERUSER
					{
					match(input,K_SUPERUSER,FOLLOW_K_SUPERUSER_in_createUserStatement6724); 
					 superuser = true; 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1040:45: K_NOSUPERUSER
					{
					match(input,K_NOSUPERUSER,FOLLOW_K_NOSUPERUSER_in_createUserStatement6730); 
					 superuser = false; 
					}
					break;

			}

			 opts.setOption(IRoleManager.Option.SUPERUSER, superuser);
			        stmt = new CreateRoleStatement(name, opts, ifNotExists); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return stmt;
	}
	// $ANTLR end "createUserStatement"



	// $ANTLR start "alterUserStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1048:1: alterUserStatement returns [AlterRoleStatement stmt] : K_ALTER K_USER u= username ( K_WITH userPassword[opts] )? ( K_SUPERUSER | K_NOSUPERUSER )? ;
	public final AlterRoleStatement alterUserStatement() throws RecognitionException {
		AlterRoleStatement stmt = null;


		ParserRuleReturnScope u =null;


		        RoleOptions opts = new RoleOptions();
		        RoleName name = new RoleName();
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1053:5: ( K_ALTER K_USER u= username ( K_WITH userPassword[opts] )? ( K_SUPERUSER | K_NOSUPERUSER )? )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1053:7: K_ALTER K_USER u= username ( K_WITH userPassword[opts] )? ( K_SUPERUSER | K_NOSUPERUSER )?
			{
			match(input,K_ALTER,FOLLOW_K_ALTER_in_alterUserStatement6775); 
			match(input,K_USER,FOLLOW_K_USER_in_alterUserStatement6777); 
			pushFollow(FOLLOW_username_in_alterUserStatement6781);
			u=username();
			state._fsp--;

			 name.setName((u!=null?input.toString(u.start,u.stop):null), true); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1054:7: ( K_WITH userPassword[opts] )?
			int alt129=2;
			int LA129_0 = input.LA(1);
			if ( (LA129_0==K_WITH) ) {
				alt129=1;
			}
			switch (alt129) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1054:9: K_WITH userPassword[opts]
					{
					match(input,K_WITH,FOLLOW_K_WITH_in_alterUserStatement6793); 
					pushFollow(FOLLOW_userPassword_in_alterUserStatement6795);
					userPassword(opts);
					state._fsp--;

					}
					break;

			}

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1055:7: ( K_SUPERUSER | K_NOSUPERUSER )?
			int alt130=3;
			int LA130_0 = input.LA(1);
			if ( (LA130_0==K_SUPERUSER) ) {
				alt130=1;
			}
			else if ( (LA130_0==K_NOSUPERUSER) ) {
				alt130=2;
			}
			switch (alt130) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1055:9: K_SUPERUSER
					{
					match(input,K_SUPERUSER,FOLLOW_K_SUPERUSER_in_alterUserStatement6809); 
					 opts.setOption(IRoleManager.Option.SUPERUSER, true); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1056:11: K_NOSUPERUSER
					{
					match(input,K_NOSUPERUSER,FOLLOW_K_NOSUPERUSER_in_alterUserStatement6823); 
					 opts.setOption(IRoleManager.Option.SUPERUSER, false); 
					}
					break;

			}

			  stmt = new AlterRoleStatement(name, opts); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return stmt;
	}
	// $ANTLR end "alterUserStatement"



	// $ANTLR start "dropUserStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1063:1: dropUserStatement returns [DropRoleStatement stmt] : K_DROP K_USER ( K_IF K_EXISTS )? u= username ;
	public final DropRoleStatement dropUserStatement() throws RecognitionException {
		DropRoleStatement stmt = null;


		ParserRuleReturnScope u =null;


		        boolean ifExists = false;
		        RoleName name = new RoleName();
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1068:5: ( K_DROP K_USER ( K_IF K_EXISTS )? u= username )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1068:7: K_DROP K_USER ( K_IF K_EXISTS )? u= username
			{
			match(input,K_DROP,FOLLOW_K_DROP_in_dropUserStatement6869); 
			match(input,K_USER,FOLLOW_K_USER_in_dropUserStatement6871); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1068:21: ( K_IF K_EXISTS )?
			int alt131=2;
			int LA131_0 = input.LA(1);
			if ( (LA131_0==K_IF) ) {
				alt131=1;
			}
			switch (alt131) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1068:22: K_IF K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_dropUserStatement6874); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropUserStatement6876); 
					 ifExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_username_in_dropUserStatement6884);
			u=username();
			state._fsp--;

			 name.setName((u!=null?input.toString(u.start,u.stop):null), true); stmt = new DropRoleStatement(name, ifExists); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return stmt;
	}
	// $ANTLR end "dropUserStatement"



	// $ANTLR start "listUsersStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1074:1: listUsersStatement returns [ListRolesStatement stmt] : K_LIST K_USERS ;
	public final ListRolesStatement listUsersStatement() throws RecognitionException {
		ListRolesStatement stmt = null;


		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1075:5: ( K_LIST K_USERS )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1075:7: K_LIST K_USERS
			{
			match(input,K_LIST,FOLLOW_K_LIST_in_listUsersStatement6909); 
			match(input,K_USERS,FOLLOW_K_USERS_in_listUsersStatement6911); 
			 stmt = new ListUsersStatement(); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return stmt;
	}
	// $ANTLR end "listUsersStatement"



	// $ANTLR start "createRoleStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1087:1: createRoleStatement returns [CreateRoleStatement stmt] : K_CREATE K_ROLE ( K_IF K_NOT K_EXISTS )? name= userOrRoleName ( K_WITH roleOptions[opts] )? ;
	public final CreateRoleStatement createRoleStatement() throws RecognitionException {
		CreateRoleStatement stmt = null;


		RoleName name =null;


		        RoleOptions opts = new RoleOptions();
		        boolean ifNotExists = false;
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1092:5: ( K_CREATE K_ROLE ( K_IF K_NOT K_EXISTS )? name= userOrRoleName ( K_WITH roleOptions[opts] )? )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1092:7: K_CREATE K_ROLE ( K_IF K_NOT K_EXISTS )? name= userOrRoleName ( K_WITH roleOptions[opts] )?
			{
			match(input,K_CREATE,FOLLOW_K_CREATE_in_createRoleStatement6945); 
			match(input,K_ROLE,FOLLOW_K_ROLE_in_createRoleStatement6947); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1092:23: ( K_IF K_NOT K_EXISTS )?
			int alt132=2;
			int LA132_0 = input.LA(1);
			if ( (LA132_0==K_IF) ) {
				alt132=1;
			}
			switch (alt132) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1092:24: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_createRoleStatement6950); 
					match(input,K_NOT,FOLLOW_K_NOT_in_createRoleStatement6952); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createRoleStatement6954); 
					 ifNotExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_userOrRoleName_in_createRoleStatement6962);
			name=userOrRoleName();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1093:7: ( K_WITH roleOptions[opts] )?
			int alt133=2;
			int LA133_0 = input.LA(1);
			if ( (LA133_0==K_WITH) ) {
				alt133=1;
			}
			switch (alt133) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1093:9: K_WITH roleOptions[opts]
					{
					match(input,K_WITH,FOLLOW_K_WITH_in_createRoleStatement6972); 
					pushFollow(FOLLOW_roleOptions_in_createRoleStatement6974);
					roleOptions(opts);
					state._fsp--;

					}
					break;

			}


			        // set defaults if they weren't explictly supplied
			        if (!opts.getLogin().isPresent())
			        {
			            opts.setOption(IRoleManager.Option.LOGIN, false);
			        }
			        if (!opts.getSuperuser().isPresent())
			        {
			            opts.setOption(IRoleManager.Option.SUPERUSER, false);
			        }
			        stmt = new CreateRoleStatement(name, opts, ifNotExists);
			      
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return stmt;
	}
	// $ANTLR end "createRoleStatement"



	// $ANTLR start "alterRoleStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1117:1: alterRoleStatement returns [AlterRoleStatement stmt] : K_ALTER K_ROLE name= userOrRoleName ( K_WITH roleOptions[opts] )? ;
	public final AlterRoleStatement alterRoleStatement() throws RecognitionException {
		AlterRoleStatement stmt = null;


		RoleName name =null;


		        RoleOptions opts = new RoleOptions();
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1121:5: ( K_ALTER K_ROLE name= userOrRoleName ( K_WITH roleOptions[opts] )? )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1121:7: K_ALTER K_ROLE name= userOrRoleName ( K_WITH roleOptions[opts] )?
			{
			match(input,K_ALTER,FOLLOW_K_ALTER_in_alterRoleStatement7018); 
			match(input,K_ROLE,FOLLOW_K_ROLE_in_alterRoleStatement7020); 
			pushFollow(FOLLOW_userOrRoleName_in_alterRoleStatement7024);
			name=userOrRoleName();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1122:7: ( K_WITH roleOptions[opts] )?
			int alt134=2;
			int LA134_0 = input.LA(1);
			if ( (LA134_0==K_WITH) ) {
				alt134=1;
			}
			switch (alt134) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1122:9: K_WITH roleOptions[opts]
					{
					match(input,K_WITH,FOLLOW_K_WITH_in_alterRoleStatement7034); 
					pushFollow(FOLLOW_roleOptions_in_alterRoleStatement7036);
					roleOptions(opts);
					state._fsp--;

					}
					break;

			}

			  stmt = new AlterRoleStatement(name, opts); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return stmt;
	}
	// $ANTLR end "alterRoleStatement"



	// $ANTLR start "dropRoleStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1129:1: dropRoleStatement returns [DropRoleStatement stmt] : K_DROP K_ROLE ( K_IF K_EXISTS )? name= userOrRoleName ;
	public final DropRoleStatement dropRoleStatement() throws RecognitionException {
		DropRoleStatement stmt = null;


		RoleName name =null;


		        boolean ifExists = false;
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1133:5: ( K_DROP K_ROLE ( K_IF K_EXISTS )? name= userOrRoleName )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1133:7: K_DROP K_ROLE ( K_IF K_EXISTS )? name= userOrRoleName
			{
			match(input,K_DROP,FOLLOW_K_DROP_in_dropRoleStatement7080); 
			match(input,K_ROLE,FOLLOW_K_ROLE_in_dropRoleStatement7082); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1133:21: ( K_IF K_EXISTS )?
			int alt135=2;
			int LA135_0 = input.LA(1);
			if ( (LA135_0==K_IF) ) {
				alt135=1;
			}
			switch (alt135) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1133:22: K_IF K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_dropRoleStatement7085); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropRoleStatement7087); 
					 ifExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_userOrRoleName_in_dropRoleStatement7095);
			name=userOrRoleName();
			state._fsp--;

			 stmt = new DropRoleStatement(name, ifExists); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return stmt;
	}
	// $ANTLR end "dropRoleStatement"



	// $ANTLR start "listRolesStatement"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1140:1: listRolesStatement returns [ListRolesStatement stmt] : K_LIST K_ROLES ( K_OF roleName[grantee] )? ( K_NORECURSIVE )? ;
	public final ListRolesStatement listRolesStatement() throws RecognitionException {
		ListRolesStatement stmt = null;



		        boolean recursive = true;
		        RoleName grantee = new RoleName();
		    
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1145:5: ( K_LIST K_ROLES ( K_OF roleName[grantee] )? ( K_NORECURSIVE )? )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1145:7: K_LIST K_ROLES ( K_OF roleName[grantee] )? ( K_NORECURSIVE )?
			{
			match(input,K_LIST,FOLLOW_K_LIST_in_listRolesStatement7135); 
			match(input,K_ROLES,FOLLOW_K_ROLES_in_listRolesStatement7137); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1146:7: ( K_OF roleName[grantee] )?
			int alt136=2;
			int LA136_0 = input.LA(1);
			if ( (LA136_0==K_OF) ) {
				alt136=1;
			}
			switch (alt136) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1146:9: K_OF roleName[grantee]
					{
					match(input,K_OF,FOLLOW_K_OF_in_listRolesStatement7147); 
					pushFollow(FOLLOW_roleName_in_listRolesStatement7149);
					roleName(grantee);
					state._fsp--;

					}
					break;

			}

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1147:7: ( K_NORECURSIVE )?
			int alt137=2;
			int LA137_0 = input.LA(1);
			if ( (LA137_0==K_NORECURSIVE) ) {
				alt137=1;
			}
			switch (alt137) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1147:9: K_NORECURSIVE
					{
					match(input,K_NORECURSIVE,FOLLOW_K_NORECURSIVE_in_listRolesStatement7162); 
					 recursive = false; 
					}
					break;

			}

			 stmt = new ListRolesStatement(grantee, recursive); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return stmt;
	}
	// $ANTLR end "listRolesStatement"



	// $ANTLR start "roleOptions"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1151:1: roleOptions[RoleOptions opts] : roleOption[opts] ( K_AND roleOption[opts] )* ;
	public final void roleOptions(RoleOptions opts) throws RecognitionException {
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1152:5: ( roleOption[opts] ( K_AND roleOption[opts] )* )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1152:7: roleOption[opts] ( K_AND roleOption[opts] )*
			{
			pushFollow(FOLLOW_roleOption_in_roleOptions7193);
			roleOption(opts);
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1152:24: ( K_AND roleOption[opts] )*
			loop138:
			while (true) {
				int alt138=2;
				int LA138_0 = input.LA(1);
				if ( (LA138_0==K_AND) ) {
					alt138=1;
				}

				switch (alt138) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1152:25: K_AND roleOption[opts]
					{
					match(input,K_AND,FOLLOW_K_AND_in_roleOptions7197); 
					pushFollow(FOLLOW_roleOption_in_roleOptions7199);
					roleOption(opts);
					state._fsp--;

					}
					break;

				default :
					break loop138;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "roleOptions"



	// $ANTLR start "roleOption"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1155:1: roleOption[RoleOptions opts] : ( K_PASSWORD '=' v= STRING_LITERAL | K_OPTIONS '=' m= mapLiteral | K_SUPERUSER '=' b= BOOLEAN | K_LOGIN '=' b= BOOLEAN );
	public final void roleOption(RoleOptions opts) throws RecognitionException {
		Token v=null;
		Token b=null;
		Maps.Literal m =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1156:5: ( K_PASSWORD '=' v= STRING_LITERAL | K_OPTIONS '=' m= mapLiteral | K_SUPERUSER '=' b= BOOLEAN | K_LOGIN '=' b= BOOLEAN )
			int alt139=4;
			switch ( input.LA(1) ) {
			case K_PASSWORD:
				{
				alt139=1;
				}
				break;
			case K_OPTIONS:
				{
				alt139=2;
				}
				break;
			case K_SUPERUSER:
				{
				alt139=3;
				}
				break;
			case K_LOGIN:
				{
				alt139=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 139, 0, input);
				throw nvae;
			}
			switch (alt139) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1156:8: K_PASSWORD '=' v= STRING_LITERAL
					{
					match(input,K_PASSWORD,FOLLOW_K_PASSWORD_in_roleOption7221); 
					match(input,181,FOLLOW_181_in_roleOption7223); 
					v=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_roleOption7227); 
					 opts.setOption(IRoleManager.Option.PASSWORD, (v!=null?v.getText():null)); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1157:8: K_OPTIONS '=' m= mapLiteral
					{
					match(input,K_OPTIONS,FOLLOW_K_OPTIONS_in_roleOption7238); 
					match(input,181,FOLLOW_181_in_roleOption7240); 
					pushFollow(FOLLOW_mapLiteral_in_roleOption7244);
					m=mapLiteral();
					state._fsp--;

					 opts.setOption(IRoleManager.Option.OPTIONS, convertPropertyMap(m)); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1158:8: K_SUPERUSER '=' b= BOOLEAN
					{
					match(input,K_SUPERUSER,FOLLOW_K_SUPERUSER_in_roleOption7255); 
					match(input,181,FOLLOW_181_in_roleOption7257); 
					b=(Token)match(input,BOOLEAN,FOLLOW_BOOLEAN_in_roleOption7261); 
					 opts.setOption(IRoleManager.Option.SUPERUSER, Boolean.valueOf((b!=null?b.getText():null))); 
					}
					break;
				case 4 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1159:8: K_LOGIN '=' b= BOOLEAN
					{
					match(input,K_LOGIN,FOLLOW_K_LOGIN_in_roleOption7272); 
					match(input,181,FOLLOW_181_in_roleOption7274); 
					b=(Token)match(input,BOOLEAN,FOLLOW_BOOLEAN_in_roleOption7278); 
					 opts.setOption(IRoleManager.Option.LOGIN, Boolean.valueOf((b!=null?b.getText():null))); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "roleOption"



	// $ANTLR start "userPassword"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1163:1: userPassword[RoleOptions opts] : K_PASSWORD v= STRING_LITERAL ;
	public final void userPassword(RoleOptions opts) throws RecognitionException {
		Token v=null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1164:5: ( K_PASSWORD v= STRING_LITERAL )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1164:8: K_PASSWORD v= STRING_LITERAL
			{
			match(input,K_PASSWORD,FOLLOW_K_PASSWORD_in_userPassword7300); 
			v=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_userPassword7304); 
			 opts.setOption(IRoleManager.Option.PASSWORD, (v!=null?v.getText():null)); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "userPassword"



	// $ANTLR start "cident"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1172:1: cident returns [ColumnIdentifier.Raw id] : (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword );
	public final ColumnIdentifier.Raw cident() throws RecognitionException {
		ColumnIdentifier.Raw id = null;


		Token t=null;
		String k =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1173:5: (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword )
			int alt140=3;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				alt140=1;
				}
				break;
			case QUOTED_NAME:
				{
				alt140=2;
				}
				break;
			case K_AGGREGATE:
			case K_ALL:
			case K_AS:
			case K_ASCII:
			case K_BIGINT:
			case K_BLOB:
			case K_BOOLEAN:
			case K_CALLED:
			case K_CLUSTERING:
			case K_COMPACT:
			case K_CONTAINS:
			case K_COUNT:
			case K_COUNTER:
			case K_CUSTOM:
			case K_DATE:
			case K_DECIMAL:
			case K_DISTINCT:
			case K_DOUBLE:
			case K_EXISTS:
			case K_FILTERING:
			case K_FINALFUNC:
			case K_FLOAT:
			case K_FROZEN:
			case K_FUNCTION:
			case K_FUNCTIONS:
			case K_INET:
			case K_INITCOND:
			case K_INPUT:
			case K_INT:
			case K_JSON:
			case K_KEY:
			case K_KEYS:
			case K_KEYSPACES:
			case K_LANGUAGE:
			case K_LIST:
			case K_LOGIN:
			case K_MAP:
			case K_NOLOGIN:
			case K_NOSUPERUSER:
			case K_OPTIONS:
			case K_PASSWORD:
			case K_PERMISSION:
			case K_PERMISSIONS:
			case K_RETURNS:
			case K_ROLE:
			case K_ROLES:
			case K_SFUNC:
			case K_SMALLINT:
			case K_STATIC:
			case K_STORAGE:
			case K_STYPE:
			case K_SUPERUSER:
			case K_TEXT:
			case K_TIME:
			case K_TIMESTAMP:
			case K_TIMEUUID:
			case K_TINYINT:
			case K_TRIGGER:
			case K_TTL:
			case K_TUPLE:
			case K_TYPE:
			case K_USER:
			case K_USERS:
			case K_UUID:
			case K_VALUES:
			case K_VARCHAR:
			case K_VARINT:
			case K_WRITETIME:
				{
				alt140=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 140, 0, input);
				throw nvae;
			}
			switch (alt140) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1173:7: t= IDENT
					{
					t=(Token)match(input,IDENT,FOLLOW_IDENT_in_cident7335); 
					 id = new ColumnIdentifier.Literal((t!=null?t.getText():null), false); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1174:7: t= QUOTED_NAME
					{
					t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_cident7360); 
					 id = new ColumnIdentifier.Literal((t!=null?t.getText():null), true); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1175:7: k= unreserved_keyword
					{
					pushFollow(FOLLOW_unreserved_keyword_in_cident7379);
					k=unreserved_keyword();
					state._fsp--;

					 id = new ColumnIdentifier.Literal(k, false); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return id;
	}
	// $ANTLR end "cident"



	// $ANTLR start "ident"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1179:1: ident returns [ColumnIdentifier id] : (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword );
	public final ColumnIdentifier ident() throws RecognitionException {
		ColumnIdentifier id = null;


		Token t=null;
		String k =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1180:5: (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword )
			int alt141=3;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				alt141=1;
				}
				break;
			case QUOTED_NAME:
				{
				alt141=2;
				}
				break;
			case K_AGGREGATE:
			case K_ALL:
			case K_AS:
			case K_ASCII:
			case K_BIGINT:
			case K_BLOB:
			case K_BOOLEAN:
			case K_CALLED:
			case K_CLUSTERING:
			case K_COMPACT:
			case K_CONTAINS:
			case K_COUNT:
			case K_COUNTER:
			case K_CUSTOM:
			case K_DATE:
			case K_DECIMAL:
			case K_DISTINCT:
			case K_DOUBLE:
			case K_EXISTS:
			case K_FILTERING:
			case K_FINALFUNC:
			case K_FLOAT:
			case K_FROZEN:
			case K_FUNCTION:
			case K_FUNCTIONS:
			case K_INET:
			case K_INITCOND:
			case K_INPUT:
			case K_INT:
			case K_JSON:
			case K_KEY:
			case K_KEYS:
			case K_KEYSPACES:
			case K_LANGUAGE:
			case K_LIST:
			case K_LOGIN:
			case K_MAP:
			case K_NOLOGIN:
			case K_NOSUPERUSER:
			case K_OPTIONS:
			case K_PASSWORD:
			case K_PERMISSION:
			case K_PERMISSIONS:
			case K_RETURNS:
			case K_ROLE:
			case K_ROLES:
			case K_SFUNC:
			case K_SMALLINT:
			case K_STATIC:
			case K_STORAGE:
			case K_STYPE:
			case K_SUPERUSER:
			case K_TEXT:
			case K_TIME:
			case K_TIMESTAMP:
			case K_TIMEUUID:
			case K_TINYINT:
			case K_TRIGGER:
			case K_TTL:
			case K_TUPLE:
			case K_TYPE:
			case K_USER:
			case K_USERS:
			case K_UUID:
			case K_VALUES:
			case K_VARCHAR:
			case K_VARINT:
			case K_WRITETIME:
				{
				alt141=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 141, 0, input);
				throw nvae;
			}
			switch (alt141) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1180:7: t= IDENT
					{
					t=(Token)match(input,IDENT,FOLLOW_IDENT_in_ident7405); 
					 id = ColumnIdentifier.getInterned((t!=null?t.getText():null), false); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1181:7: t= QUOTED_NAME
					{
					t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_ident7430); 
					 id = ColumnIdentifier.getInterned((t!=null?t.getText():null), true); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1182:7: k= unreserved_keyword
					{
					pushFollow(FOLLOW_unreserved_keyword_in_ident7449);
					k=unreserved_keyword();
					state._fsp--;

					 id = ColumnIdentifier.getInterned(k, false); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return id;
	}
	// $ANTLR end "ident"



	// $ANTLR start "noncol_ident"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1186:1: noncol_ident returns [ColumnIdentifier id] : (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword );
	public final ColumnIdentifier noncol_ident() throws RecognitionException {
		ColumnIdentifier id = null;


		Token t=null;
		String k =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1187:5: (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword )
			int alt142=3;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				alt142=1;
				}
				break;
			case QUOTED_NAME:
				{
				alt142=2;
				}
				break;
			case K_AGGREGATE:
			case K_ALL:
			case K_AS:
			case K_ASCII:
			case K_BIGINT:
			case K_BLOB:
			case K_BOOLEAN:
			case K_CALLED:
			case K_CLUSTERING:
			case K_COMPACT:
			case K_CONTAINS:
			case K_COUNT:
			case K_COUNTER:
			case K_CUSTOM:
			case K_DATE:
			case K_DECIMAL:
			case K_DISTINCT:
			case K_DOUBLE:
			case K_EXISTS:
			case K_FILTERING:
			case K_FINALFUNC:
			case K_FLOAT:
			case K_FROZEN:
			case K_FUNCTION:
			case K_FUNCTIONS:
			case K_INET:
			case K_INITCOND:
			case K_INPUT:
			case K_INT:
			case K_JSON:
			case K_KEY:
			case K_KEYS:
			case K_KEYSPACES:
			case K_LANGUAGE:
			case K_LIST:
			case K_LOGIN:
			case K_MAP:
			case K_NOLOGIN:
			case K_NOSUPERUSER:
			case K_OPTIONS:
			case K_PASSWORD:
			case K_PERMISSION:
			case K_PERMISSIONS:
			case K_RETURNS:
			case K_ROLE:
			case K_ROLES:
			case K_SFUNC:
			case K_SMALLINT:
			case K_STATIC:
			case K_STORAGE:
			case K_STYPE:
			case K_SUPERUSER:
			case K_TEXT:
			case K_TIME:
			case K_TIMESTAMP:
			case K_TIMEUUID:
			case K_TINYINT:
			case K_TRIGGER:
			case K_TTL:
			case K_TUPLE:
			case K_TYPE:
			case K_USER:
			case K_USERS:
			case K_UUID:
			case K_VALUES:
			case K_VARCHAR:
			case K_VARINT:
			case K_WRITETIME:
				{
				alt142=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 142, 0, input);
				throw nvae;
			}
			switch (alt142) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1187:7: t= IDENT
					{
					t=(Token)match(input,IDENT,FOLLOW_IDENT_in_noncol_ident7475); 
					 id = new ColumnIdentifier((t!=null?t.getText():null), false); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1188:7: t= QUOTED_NAME
					{
					t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_noncol_ident7500); 
					 id = new ColumnIdentifier((t!=null?t.getText():null), true); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1189:7: k= unreserved_keyword
					{
					pushFollow(FOLLOW_unreserved_keyword_in_noncol_ident7519);
					k=unreserved_keyword();
					state._fsp--;

					 id = new ColumnIdentifier(k, false); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return id;
	}
	// $ANTLR end "noncol_ident"



	// $ANTLR start "keyspaceName"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1193:1: keyspaceName returns [String id] : ksName[name] ;
	public final String keyspaceName() throws RecognitionException {
		String id = null;


		 CFName name = new CFName(); 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1195:5: ( ksName[name] )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1195:7: ksName[name]
			{
			pushFollow(FOLLOW_ksName_in_keyspaceName7552);
			ksName(name);
			state._fsp--;

			 id = name.getKeyspace(); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return id;
	}
	// $ANTLR end "keyspaceName"



	// $ANTLR start "indexName"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1198:1: indexName returns [IndexName name] : ( ksName[name] '.' )? idxName[name] ;
	public final IndexName indexName() throws RecognitionException {
		IndexName name = null;


		 name = new IndexName(); 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1200:5: ( ( ksName[name] '.' )? idxName[name] )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1200:7: ( ksName[name] '.' )? idxName[name]
			{
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1200:7: ( ksName[name] '.' )?
			int alt143=2;
			alt143 = dfa143.predict(input);
			switch (alt143) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1200:8: ksName[name] '.'
					{
					pushFollow(FOLLOW_ksName_in_indexName7586);
					ksName(name);
					state._fsp--;

					match(input,176,FOLLOW_176_in_indexName7589); 
					}
					break;

			}

			pushFollow(FOLLOW_idxName_in_indexName7593);
			idxName(name);
			state._fsp--;

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return name;
	}
	// $ANTLR end "indexName"



	// $ANTLR start "columnFamilyName"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1203:1: columnFamilyName returns [CFName name] : ( ksName[name] '.' )? cfName[name] ;
	public final CFName columnFamilyName() throws RecognitionException {
		CFName name = null;


		 name = new CFName(); 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1205:5: ( ( ksName[name] '.' )? cfName[name] )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1205:7: ( ksName[name] '.' )? cfName[name]
			{
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1205:7: ( ksName[name] '.' )?
			int alt144=2;
			alt144 = dfa144.predict(input);
			switch (alt144) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1205:8: ksName[name] '.'
					{
					pushFollow(FOLLOW_ksName_in_columnFamilyName7625);
					ksName(name);
					state._fsp--;

					match(input,176,FOLLOW_176_in_columnFamilyName7628); 
					}
					break;

			}

			pushFollow(FOLLOW_cfName_in_columnFamilyName7632);
			cfName(name);
			state._fsp--;

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return name;
	}
	// $ANTLR end "columnFamilyName"



	// $ANTLR start "userTypeName"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1208:1: userTypeName returns [UTName name] : (ks= noncol_ident '.' )? ut= non_type_ident ;
	public final UTName userTypeName() throws RecognitionException {
		UTName name = null;


		ColumnIdentifier ks =null;
		ColumnIdentifier ut =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1209:5: ( (ks= noncol_ident '.' )? ut= non_type_ident )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1209:7: (ks= noncol_ident '.' )? ut= non_type_ident
			{
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1209:7: (ks= noncol_ident '.' )?
			int alt145=2;
			switch ( input.LA(1) ) {
				case IDENT:
					{
					int LA145_1 = input.LA(2);
					if ( (LA145_1==176) ) {
						alt145=1;
					}
					}
					break;
				case QUOTED_NAME:
					{
					int LA145_2 = input.LA(2);
					if ( (LA145_2==176) ) {
						alt145=1;
					}
					}
					break;
				case K_AGGREGATE:
				case K_ALL:
				case K_AS:
				case K_CALLED:
				case K_CLUSTERING:
				case K_COMPACT:
				case K_CONTAINS:
				case K_CUSTOM:
				case K_DISTINCT:
				case K_EXISTS:
				case K_FILTERING:
				case K_FINALFUNC:
				case K_FROZEN:
				case K_FUNCTION:
				case K_FUNCTIONS:
				case K_INITCOND:
				case K_INPUT:
				case K_JSON:
				case K_KEYS:
				case K_KEYSPACES:
				case K_LANGUAGE:
				case K_LIST:
				case K_LOGIN:
				case K_MAP:
				case K_NOLOGIN:
				case K_NOSUPERUSER:
				case K_OPTIONS:
				case K_PASSWORD:
				case K_PERMISSION:
				case K_PERMISSIONS:
				case K_RETURNS:
				case K_ROLE:
				case K_ROLES:
				case K_SFUNC:
				case K_STATIC:
				case K_STORAGE:
				case K_STYPE:
				case K_SUPERUSER:
				case K_TRIGGER:
				case K_TUPLE:
				case K_TYPE:
				case K_USER:
				case K_USERS:
				case K_VALUES:
					{
					int LA145_3 = input.LA(2);
					if ( (LA145_3==176) ) {
						alt145=1;
					}
					}
					break;
				case K_ASCII:
				case K_BIGINT:
				case K_BLOB:
				case K_BOOLEAN:
				case K_COUNT:
				case K_COUNTER:
				case K_DATE:
				case K_DECIMAL:
				case K_DOUBLE:
				case K_FLOAT:
				case K_INET:
				case K_INT:
				case K_SMALLINT:
				case K_TEXT:
				case K_TIME:
				case K_TIMESTAMP:
				case K_TIMEUUID:
				case K_TINYINT:
				case K_TTL:
				case K_UUID:
				case K_VARCHAR:
				case K_VARINT:
				case K_WRITETIME:
					{
					alt145=1;
					}
					break;
				case K_KEY:
					{
					int LA145_5 = input.LA(2);
					if ( (LA145_5==176) ) {
						alt145=1;
					}
					}
					break;
			}
			switch (alt145) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1209:8: ks= noncol_ident '.'
					{
					pushFollow(FOLLOW_noncol_ident_in_userTypeName7657);
					ks=noncol_ident();
					state._fsp--;

					match(input,176,FOLLOW_176_in_userTypeName7659); 
					}
					break;

			}

			pushFollow(FOLLOW_non_type_ident_in_userTypeName7665);
			ut=non_type_ident();
			state._fsp--;

			 return new UTName(ks, ut); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return name;
	}
	// $ANTLR end "userTypeName"



	// $ANTLR start "userOrRoleName"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1212:1: userOrRoleName returns [RoleName name] : roleName[name] ;
	public final RoleName userOrRoleName() throws RecognitionException {
		RoleName name = null;


		 name = new RoleName(); 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1214:5: ( roleName[name] )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1214:7: roleName[name]
			{
			pushFollow(FOLLOW_roleName_in_userOrRoleName7697);
			roleName(name);
			state._fsp--;

			return name;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return name;
	}
	// $ANTLR end "userOrRoleName"



	// $ANTLR start "ksName"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1217:1: ksName[KeyspaceElementName name] : (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword | QMARK );
	public final void ksName(KeyspaceElementName name) throws RecognitionException {
		Token t=null;
		String k =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1218:5: (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword | QMARK )
			int alt146=4;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				alt146=1;
				}
				break;
			case QUOTED_NAME:
				{
				alt146=2;
				}
				break;
			case K_AGGREGATE:
			case K_ALL:
			case K_AS:
			case K_ASCII:
			case K_BIGINT:
			case K_BLOB:
			case K_BOOLEAN:
			case K_CALLED:
			case K_CLUSTERING:
			case K_COMPACT:
			case K_CONTAINS:
			case K_COUNT:
			case K_COUNTER:
			case K_CUSTOM:
			case K_DATE:
			case K_DECIMAL:
			case K_DISTINCT:
			case K_DOUBLE:
			case K_EXISTS:
			case K_FILTERING:
			case K_FINALFUNC:
			case K_FLOAT:
			case K_FROZEN:
			case K_FUNCTION:
			case K_FUNCTIONS:
			case K_INET:
			case K_INITCOND:
			case K_INPUT:
			case K_INT:
			case K_JSON:
			case K_KEY:
			case K_KEYS:
			case K_KEYSPACES:
			case K_LANGUAGE:
			case K_LIST:
			case K_LOGIN:
			case K_MAP:
			case K_NOLOGIN:
			case K_NOSUPERUSER:
			case K_OPTIONS:
			case K_PASSWORD:
			case K_PERMISSION:
			case K_PERMISSIONS:
			case K_RETURNS:
			case K_ROLE:
			case K_ROLES:
			case K_SFUNC:
			case K_SMALLINT:
			case K_STATIC:
			case K_STORAGE:
			case K_STYPE:
			case K_SUPERUSER:
			case K_TEXT:
			case K_TIME:
			case K_TIMESTAMP:
			case K_TIMEUUID:
			case K_TINYINT:
			case K_TRIGGER:
			case K_TTL:
			case K_TUPLE:
			case K_TYPE:
			case K_USER:
			case K_USERS:
			case K_UUID:
			case K_VALUES:
			case K_VARCHAR:
			case K_VARINT:
			case K_WRITETIME:
				{
				alt146=3;
				}
				break;
			case QMARK:
				{
				alt146=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 146, 0, input);
				throw nvae;
			}
			switch (alt146) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1218:7: t= IDENT
					{
					t=(Token)match(input,IDENT,FOLLOW_IDENT_in_ksName7720); 
					 name.setKeyspace((t!=null?t.getText():null), false);
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1219:7: t= QUOTED_NAME
					{
					t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_ksName7745); 
					 name.setKeyspace((t!=null?t.getText():null), true);
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1220:7: k= unreserved_keyword
					{
					pushFollow(FOLLOW_unreserved_keyword_in_ksName7764);
					k=unreserved_keyword();
					state._fsp--;

					 name.setKeyspace(k, false);
					}
					break;
				case 4 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1221:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_ksName7774); 
					addRecognitionError("Bind variables cannot be used for keyspace names");
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ksName"



	// $ANTLR start "cfName"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1224:1: cfName[CFName name] : (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword | QMARK );
	public final void cfName(CFName name) throws RecognitionException {
		Token t=null;
		String k =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1225:5: (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword | QMARK )
			int alt147=4;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				alt147=1;
				}
				break;
			case QUOTED_NAME:
				{
				alt147=2;
				}
				break;
			case K_AGGREGATE:
			case K_ALL:
			case K_AS:
			case K_ASCII:
			case K_BIGINT:
			case K_BLOB:
			case K_BOOLEAN:
			case K_CALLED:
			case K_CLUSTERING:
			case K_COMPACT:
			case K_CONTAINS:
			case K_COUNT:
			case K_COUNTER:
			case K_CUSTOM:
			case K_DATE:
			case K_DECIMAL:
			case K_DISTINCT:
			case K_DOUBLE:
			case K_EXISTS:
			case K_FILTERING:
			case K_FINALFUNC:
			case K_FLOAT:
			case K_FROZEN:
			case K_FUNCTION:
			case K_FUNCTIONS:
			case K_INET:
			case K_INITCOND:
			case K_INPUT:
			case K_INT:
			case K_JSON:
			case K_KEY:
			case K_KEYS:
			case K_KEYSPACES:
			case K_LANGUAGE:
			case K_LIST:
			case K_LOGIN:
			case K_MAP:
			case K_NOLOGIN:
			case K_NOSUPERUSER:
			case K_OPTIONS:
			case K_PASSWORD:
			case K_PERMISSION:
			case K_PERMISSIONS:
			case K_RETURNS:
			case K_ROLE:
			case K_ROLES:
			case K_SFUNC:
			case K_SMALLINT:
			case K_STATIC:
			case K_STORAGE:
			case K_STYPE:
			case K_SUPERUSER:
			case K_TEXT:
			case K_TIME:
			case K_TIMESTAMP:
			case K_TIMEUUID:
			case K_TINYINT:
			case K_TRIGGER:
			case K_TTL:
			case K_TUPLE:
			case K_TYPE:
			case K_USER:
			case K_USERS:
			case K_UUID:
			case K_VALUES:
			case K_VARCHAR:
			case K_VARINT:
			case K_WRITETIME:
				{
				alt147=3;
				}
				break;
			case QMARK:
				{
				alt147=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 147, 0, input);
				throw nvae;
			}
			switch (alt147) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1225:7: t= IDENT
					{
					t=(Token)match(input,IDENT,FOLLOW_IDENT_in_cfName7796); 
					 name.setColumnFamily((t!=null?t.getText():null), false); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1226:7: t= QUOTED_NAME
					{
					t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_cfName7821); 
					 name.setColumnFamily((t!=null?t.getText():null), true); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1227:7: k= unreserved_keyword
					{
					pushFollow(FOLLOW_unreserved_keyword_in_cfName7840);
					k=unreserved_keyword();
					state._fsp--;

					 name.setColumnFamily(k, false); 
					}
					break;
				case 4 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1228:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_cfName7850); 
					addRecognitionError("Bind variables cannot be used for table names");
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "cfName"



	// $ANTLR start "idxName"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1231:1: idxName[IndexName name] : (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword | QMARK );
	public final void idxName(IndexName name) throws RecognitionException {
		Token t=null;
		String k =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1232:5: (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword | QMARK )
			int alt148=4;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				alt148=1;
				}
				break;
			case QUOTED_NAME:
				{
				alt148=2;
				}
				break;
			case K_AGGREGATE:
			case K_ALL:
			case K_AS:
			case K_ASCII:
			case K_BIGINT:
			case K_BLOB:
			case K_BOOLEAN:
			case K_CALLED:
			case K_CLUSTERING:
			case K_COMPACT:
			case K_CONTAINS:
			case K_COUNT:
			case K_COUNTER:
			case K_CUSTOM:
			case K_DATE:
			case K_DECIMAL:
			case K_DISTINCT:
			case K_DOUBLE:
			case K_EXISTS:
			case K_FILTERING:
			case K_FINALFUNC:
			case K_FLOAT:
			case K_FROZEN:
			case K_FUNCTION:
			case K_FUNCTIONS:
			case K_INET:
			case K_INITCOND:
			case K_INPUT:
			case K_INT:
			case K_JSON:
			case K_KEY:
			case K_KEYS:
			case K_KEYSPACES:
			case K_LANGUAGE:
			case K_LIST:
			case K_LOGIN:
			case K_MAP:
			case K_NOLOGIN:
			case K_NOSUPERUSER:
			case K_OPTIONS:
			case K_PASSWORD:
			case K_PERMISSION:
			case K_PERMISSIONS:
			case K_RETURNS:
			case K_ROLE:
			case K_ROLES:
			case K_SFUNC:
			case K_SMALLINT:
			case K_STATIC:
			case K_STORAGE:
			case K_STYPE:
			case K_SUPERUSER:
			case K_TEXT:
			case K_TIME:
			case K_TIMESTAMP:
			case K_TIMEUUID:
			case K_TINYINT:
			case K_TRIGGER:
			case K_TTL:
			case K_TUPLE:
			case K_TYPE:
			case K_USER:
			case K_USERS:
			case K_UUID:
			case K_VALUES:
			case K_VARCHAR:
			case K_VARINT:
			case K_WRITETIME:
				{
				alt148=3;
				}
				break;
			case QMARK:
				{
				alt148=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 148, 0, input);
				throw nvae;
			}
			switch (alt148) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1232:7: t= IDENT
					{
					t=(Token)match(input,IDENT,FOLLOW_IDENT_in_idxName7872); 
					 name.setIndex((t!=null?t.getText():null), false); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1233:7: t= QUOTED_NAME
					{
					t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_idxName7897); 
					 name.setIndex((t!=null?t.getText():null), true);
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1234:7: k= unreserved_keyword
					{
					pushFollow(FOLLOW_unreserved_keyword_in_idxName7916);
					k=unreserved_keyword();
					state._fsp--;

					 name.setIndex(k, false); 
					}
					break;
				case 4 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1235:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_idxName7926); 
					addRecognitionError("Bind variables cannot be used for index names");
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "idxName"



	// $ANTLR start "roleName"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1238:1: roleName[RoleName name] : (t= IDENT |s= STRING_LITERAL |t= QUOTED_NAME |k= unreserved_keyword | QMARK );
	public final void roleName(RoleName name) throws RecognitionException {
		Token t=null;
		Token s=null;
		String k =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1239:5: (t= IDENT |s= STRING_LITERAL |t= QUOTED_NAME |k= unreserved_keyword | QMARK )
			int alt149=5;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				alt149=1;
				}
				break;
			case STRING_LITERAL:
				{
				alt149=2;
				}
				break;
			case QUOTED_NAME:
				{
				alt149=3;
				}
				break;
			case K_AGGREGATE:
			case K_ALL:
			case K_AS:
			case K_ASCII:
			case K_BIGINT:
			case K_BLOB:
			case K_BOOLEAN:
			case K_CALLED:
			case K_CLUSTERING:
			case K_COMPACT:
			case K_CONTAINS:
			case K_COUNT:
			case K_COUNTER:
			case K_CUSTOM:
			case K_DATE:
			case K_DECIMAL:
			case K_DISTINCT:
			case K_DOUBLE:
			case K_EXISTS:
			case K_FILTERING:
			case K_FINALFUNC:
			case K_FLOAT:
			case K_FROZEN:
			case K_FUNCTION:
			case K_FUNCTIONS:
			case K_INET:
			case K_INITCOND:
			case K_INPUT:
			case K_INT:
			case K_JSON:
			case K_KEY:
			case K_KEYS:
			case K_KEYSPACES:
			case K_LANGUAGE:
			case K_LIST:
			case K_LOGIN:
			case K_MAP:
			case K_NOLOGIN:
			case K_NOSUPERUSER:
			case K_OPTIONS:
			case K_PASSWORD:
			case K_PERMISSION:
			case K_PERMISSIONS:
			case K_RETURNS:
			case K_ROLE:
			case K_ROLES:
			case K_SFUNC:
			case K_SMALLINT:
			case K_STATIC:
			case K_STORAGE:
			case K_STYPE:
			case K_SUPERUSER:
			case K_TEXT:
			case K_TIME:
			case K_TIMESTAMP:
			case K_TIMEUUID:
			case K_TINYINT:
			case K_TRIGGER:
			case K_TTL:
			case K_TUPLE:
			case K_TYPE:
			case K_USER:
			case K_USERS:
			case K_UUID:
			case K_VALUES:
			case K_VARCHAR:
			case K_VARINT:
			case K_WRITETIME:
				{
				alt149=4;
				}
				break;
			case QMARK:
				{
				alt149=5;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 149, 0, input);
				throw nvae;
			}
			switch (alt149) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1239:7: t= IDENT
					{
					t=(Token)match(input,IDENT,FOLLOW_IDENT_in_roleName7948); 
					 name.setName((t!=null?t.getText():null), false); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1240:7: s= STRING_LITERAL
					{
					s=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_roleName7973); 
					 name.setName((s!=null?s.getText():null), true); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1241:7: t= QUOTED_NAME
					{
					t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_roleName7989); 
					 name.setName((t!=null?t.getText():null), true); 
					}
					break;
				case 4 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1242:7: k= unreserved_keyword
					{
					pushFollow(FOLLOW_unreserved_keyword_in_roleName8008);
					k=unreserved_keyword();
					state._fsp--;

					 name.setName(k, false); 
					}
					break;
				case 5 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1243:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_roleName8018); 
					addRecognitionError("Bind variables cannot be used for role names");
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "roleName"



	// $ANTLR start "constant"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1246:1: constant returns [Constants.Literal constant] : (t= STRING_LITERAL |t= INTEGER |t= FLOAT |t= BOOLEAN |t= UUID |t= HEXNUMBER | ( '-' )? t= ( K_NAN | K_INFINITY ) );
	public final Constants.Literal constant() throws RecognitionException {
		Constants.Literal constant = null;


		Token t=null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1247:5: (t= STRING_LITERAL |t= INTEGER |t= FLOAT |t= BOOLEAN |t= UUID |t= HEXNUMBER | ( '-' )? t= ( K_NAN | K_INFINITY ) )
			int alt151=7;
			switch ( input.LA(1) ) {
			case STRING_LITERAL:
				{
				alt151=1;
				}
				break;
			case INTEGER:
				{
				alt151=2;
				}
				break;
			case FLOAT:
				{
				alt151=3;
				}
				break;
			case BOOLEAN:
				{
				alt151=4;
				}
				break;
			case UUID:
				{
				alt151=5;
				}
				break;
			case HEXNUMBER:
				{
				alt151=6;
				}
				break;
			case K_INFINITY:
			case K_NAN:
			case 175:
				{
				alt151=7;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 151, 0, input);
				throw nvae;
			}
			switch (alt151) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1247:7: t= STRING_LITERAL
					{
					t=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_constant8043); 
					 constant = Constants.Literal.string((t!=null?t.getText():null)); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1248:7: t= INTEGER
					{
					t=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_constant8055); 
					 constant = Constants.Literal.integer((t!=null?t.getText():null)); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1249:7: t= FLOAT
					{
					t=(Token)match(input,FLOAT,FOLLOW_FLOAT_in_constant8074); 
					 constant = Constants.Literal.floatingPoint((t!=null?t.getText():null)); 
					}
					break;
				case 4 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1250:7: t= BOOLEAN
					{
					t=(Token)match(input,BOOLEAN,FOLLOW_BOOLEAN_in_constant8095); 
					 constant = Constants.Literal.bool((t!=null?t.getText():null)); 
					}
					break;
				case 5 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1251:7: t= UUID
					{
					t=(Token)match(input,UUID,FOLLOW_UUID_in_constant8114); 
					 constant = Constants.Literal.uuid((t!=null?t.getText():null)); 
					}
					break;
				case 6 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1252:7: t= HEXNUMBER
					{
					t=(Token)match(input,HEXNUMBER,FOLLOW_HEXNUMBER_in_constant8136); 
					 constant = Constants.Literal.hex((t!=null?t.getText():null)); 
					}
					break;
				case 7 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1253:7: ( '-' )? t= ( K_NAN | K_INFINITY )
					{
					 String sign=""; 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1253:27: ( '-' )?
					int alt150=2;
					int LA150_0 = input.LA(1);
					if ( (LA150_0==175) ) {
						alt150=1;
					}
					switch (alt150) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1253:28: '-'
							{
							match(input,175,FOLLOW_175_in_constant8154); 
							sign = "-"; 
							}
							break;

					}

					t=input.LT(1);
					if ( input.LA(1)==K_INFINITY||input.LA(1)==K_NAN ) {
						input.consume();
						state.errorRecovery=false;
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					 constant = Constants.Literal.floatingPoint(sign + (t!=null?t.getText():null)); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return constant;
	}
	// $ANTLR end "constant"



	// $ANTLR start "mapLiteral"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1256:1: mapLiteral returns [Maps.Literal map] : '{' (k1= term ':' v1= term ( ',' kn= term ':' vn= term )* )? '}' ;
	public final Maps.Literal mapLiteral() throws RecognitionException {
		Maps.Literal map = null;


		Term.Raw k1 =null;
		Term.Raw v1 =null;
		Term.Raw kn =null;
		Term.Raw vn =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1257:5: ( '{' (k1= term ':' v1= term ( ',' kn= term ':' vn= term )* )? '}' )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1257:7: '{' (k1= term ':' v1= term ( ',' kn= term ':' vn= term )* )? '}'
			{
			match(input,188,FOLLOW_188_in_mapLiteral8192); 
			 List<Pair<Term.Raw, Term.Raw>> m = new ArrayList<Pair<Term.Raw, Term.Raw>>(); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1258:11: (k1= term ':' v1= term ( ',' kn= term ':' vn= term )* )?
			int alt153=2;
			int LA153_0 = input.LA(1);
			if ( (LA153_0==BOOLEAN||LA153_0==FLOAT||LA153_0==HEXNUMBER||(LA153_0 >= IDENT && LA153_0 <= INTEGER)||(LA153_0 >= K_AGGREGATE && LA153_0 <= K_ALL)||LA153_0==K_AS||LA153_0==K_ASCII||(LA153_0 >= K_BIGINT && LA153_0 <= K_BOOLEAN)||(LA153_0 >= K_CALLED && LA153_0 <= K_CLUSTERING)||(LA153_0 >= K_COMPACT && LA153_0 <= K_COUNTER)||(LA153_0 >= K_CUSTOM && LA153_0 <= K_DECIMAL)||(LA153_0 >= K_DISTINCT && LA153_0 <= K_DOUBLE)||(LA153_0 >= K_EXISTS && LA153_0 <= K_FLOAT)||LA153_0==K_FROZEN||(LA153_0 >= K_FUNCTION && LA153_0 <= K_FUNCTIONS)||(LA153_0 >= K_INET && LA153_0 <= K_INPUT)||LA153_0==K_INT||(LA153_0 >= K_JSON && LA153_0 <= K_KEYS)||(LA153_0 >= K_KEYSPACES && LA153_0 <= K_LANGUAGE)||(LA153_0 >= K_LIST && LA153_0 <= K_MAP)||(LA153_0 >= K_NAN && LA153_0 <= K_NOLOGIN)||LA153_0==K_NOSUPERUSER||LA153_0==K_NULL||LA153_0==K_OPTIONS||(LA153_0 >= K_PASSWORD && LA153_0 <= K_PERMISSIONS)||LA153_0==K_RETURNS||(LA153_0 >= K_ROLE && LA153_0 <= K_ROLES)||(LA153_0 >= K_SFUNC && LA153_0 <= K_TINYINT)||(LA153_0 >= K_TOKEN && LA153_0 <= K_TRIGGER)||(LA153_0 >= K_TTL && LA153_0 <= K_TYPE)||(LA153_0 >= K_USER && LA153_0 <= K_USERS)||(LA153_0 >= K_UUID && LA153_0 <= K_VARINT)||LA153_0==K_WRITETIME||(LA153_0 >= QMARK && LA153_0 <= QUOTED_NAME)||LA153_0==STRING_LITERAL||LA153_0==UUID||LA153_0==171||LA153_0==175||LA153_0==177||LA153_0==184||LA153_0==188) ) {
				alt153=1;
			}
			switch (alt153) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1258:13: k1= term ':' v1= term ( ',' kn= term ':' vn= term )*
					{
					pushFollow(FOLLOW_term_in_mapLiteral8210);
					k1=term();
					state._fsp--;

					match(input,177,FOLLOW_177_in_mapLiteral8212); 
					pushFollow(FOLLOW_term_in_mapLiteral8216);
					v1=term();
					state._fsp--;

					 m.add(Pair.create(k1, v1)); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1258:65: ( ',' kn= term ':' vn= term )*
					loop152:
					while (true) {
						int alt152=2;
						int LA152_0 = input.LA(1);
						if ( (LA152_0==174) ) {
							alt152=1;
						}

						switch (alt152) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1258:67: ',' kn= term ':' vn= term
							{
							match(input,174,FOLLOW_174_in_mapLiteral8222); 
							pushFollow(FOLLOW_term_in_mapLiteral8226);
							kn=term();
							state._fsp--;

							match(input,177,FOLLOW_177_in_mapLiteral8228); 
							pushFollow(FOLLOW_term_in_mapLiteral8232);
							vn=term();
							state._fsp--;

							 m.add(Pair.create(kn, vn)); 
							}
							break;

						default :
							break loop152;
						}
					}

					}
					break;

			}

			match(input,189,FOLLOW_189_in_mapLiteral8248); 
			 map = new Maps.Literal(m); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return map;
	}
	// $ANTLR end "mapLiteral"



	// $ANTLR start "setOrMapLiteral"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1262:1: setOrMapLiteral[Term.Raw t] returns [Term.Raw value] : ( ':' v= term ( ',' kn= term ':' vn= term )* | ( ',' tn= term )* );
	public final Term.Raw setOrMapLiteral(Term.Raw t) throws RecognitionException {
		Term.Raw value = null;


		Term.Raw v =null;
		Term.Raw kn =null;
		Term.Raw vn =null;
		Term.Raw tn =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1263:5: ( ':' v= term ( ',' kn= term ':' vn= term )* | ( ',' tn= term )* )
			int alt156=2;
			int LA156_0 = input.LA(1);
			if ( (LA156_0==177) ) {
				alt156=1;
			}
			else if ( (LA156_0==174||LA156_0==189) ) {
				alt156=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 156, 0, input);
				throw nvae;
			}

			switch (alt156) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1263:7: ':' v= term ( ',' kn= term ':' vn= term )*
					{
					match(input,177,FOLLOW_177_in_setOrMapLiteral8272); 
					pushFollow(FOLLOW_term_in_setOrMapLiteral8276);
					v=term();
					state._fsp--;

					 List<Pair<Term.Raw, Term.Raw>> m = new ArrayList<Pair<Term.Raw, Term.Raw>>(); m.add(Pair.create(t, v)); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1264:11: ( ',' kn= term ':' vn= term )*
					loop154:
					while (true) {
						int alt154=2;
						int LA154_0 = input.LA(1);
						if ( (LA154_0==174) ) {
							alt154=1;
						}

						switch (alt154) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1264:13: ',' kn= term ':' vn= term
							{
							match(input,174,FOLLOW_174_in_setOrMapLiteral8292); 
							pushFollow(FOLLOW_term_in_setOrMapLiteral8296);
							kn=term();
							state._fsp--;

							match(input,177,FOLLOW_177_in_setOrMapLiteral8298); 
							pushFollow(FOLLOW_term_in_setOrMapLiteral8302);
							vn=term();
							state._fsp--;

							 m.add(Pair.create(kn, vn)); 
							}
							break;

						default :
							break loop154;
						}
					}

					 value = new Maps.Literal(m); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1266:7: ( ',' tn= term )*
					{
					 List<Term.Raw> s = new ArrayList<Term.Raw>(); s.add(t); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1267:11: ( ',' tn= term )*
					loop155:
					while (true) {
						int alt155=2;
						int LA155_0 = input.LA(1);
						if ( (LA155_0==174) ) {
							alt155=1;
						}

						switch (alt155) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1267:13: ',' tn= term
							{
							match(input,174,FOLLOW_174_in_setOrMapLiteral8337); 
							pushFollow(FOLLOW_term_in_setOrMapLiteral8341);
							tn=term();
							state._fsp--;

							 s.add(tn); 
							}
							break;

						default :
							break loop155;
						}
					}

					 value = new Sets.Literal(s); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "setOrMapLiteral"



	// $ANTLR start "collectionLiteral"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1271:1: collectionLiteral returns [Term.Raw value] : ( '[' (t1= term ( ',' tn= term )* )? ']' | '{' t= term v= setOrMapLiteral[t] '}' | '{' '}' );
	public final Term.Raw collectionLiteral() throws RecognitionException {
		Term.Raw value = null;


		Term.Raw t1 =null;
		Term.Raw tn =null;
		Term.Raw t =null;
		Term.Raw v =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1272:5: ( '[' (t1= term ( ',' tn= term )* )? ']' | '{' t= term v= setOrMapLiteral[t] '}' | '{' '}' )
			int alt159=3;
			int LA159_0 = input.LA(1);
			if ( (LA159_0==184) ) {
				alt159=1;
			}
			else if ( (LA159_0==188) ) {
				int LA159_2 = input.LA(2);
				if ( (LA159_2==189) ) {
					alt159=3;
				}
				else if ( (LA159_2==BOOLEAN||LA159_2==FLOAT||LA159_2==HEXNUMBER||(LA159_2 >= IDENT && LA159_2 <= INTEGER)||(LA159_2 >= K_AGGREGATE && LA159_2 <= K_ALL)||LA159_2==K_AS||LA159_2==K_ASCII||(LA159_2 >= K_BIGINT && LA159_2 <= K_BOOLEAN)||(LA159_2 >= K_CALLED && LA159_2 <= K_CLUSTERING)||(LA159_2 >= K_COMPACT && LA159_2 <= K_COUNTER)||(LA159_2 >= K_CUSTOM && LA159_2 <= K_DECIMAL)||(LA159_2 >= K_DISTINCT && LA159_2 <= K_DOUBLE)||(LA159_2 >= K_EXISTS && LA159_2 <= K_FLOAT)||LA159_2==K_FROZEN||(LA159_2 >= K_FUNCTION && LA159_2 <= K_FUNCTIONS)||(LA159_2 >= K_INET && LA159_2 <= K_INPUT)||LA159_2==K_INT||(LA159_2 >= K_JSON && LA159_2 <= K_KEYS)||(LA159_2 >= K_KEYSPACES && LA159_2 <= K_LANGUAGE)||(LA159_2 >= K_LIST && LA159_2 <= K_MAP)||(LA159_2 >= K_NAN && LA159_2 <= K_NOLOGIN)||LA159_2==K_NOSUPERUSER||LA159_2==K_NULL||LA159_2==K_OPTIONS||(LA159_2 >= K_PASSWORD && LA159_2 <= K_PERMISSIONS)||LA159_2==K_RETURNS||(LA159_2 >= K_ROLE && LA159_2 <= K_ROLES)||(LA159_2 >= K_SFUNC && LA159_2 <= K_TINYINT)||(LA159_2 >= K_TOKEN && LA159_2 <= K_TRIGGER)||(LA159_2 >= K_TTL && LA159_2 <= K_TYPE)||(LA159_2 >= K_USER && LA159_2 <= K_USERS)||(LA159_2 >= K_UUID && LA159_2 <= K_VARINT)||LA159_2==K_WRITETIME||(LA159_2 >= QMARK && LA159_2 <= QUOTED_NAME)||LA159_2==STRING_LITERAL||LA159_2==UUID||LA159_2==171||LA159_2==175||LA159_2==177||LA159_2==184||LA159_2==188) ) {
					alt159=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 159, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 159, 0, input);
				throw nvae;
			}

			switch (alt159) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1272:7: '[' (t1= term ( ',' tn= term )* )? ']'
					{
					match(input,184,FOLLOW_184_in_collectionLiteral8375); 
					 List<Term.Raw> l = new ArrayList<Term.Raw>(); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1273:11: (t1= term ( ',' tn= term )* )?
					int alt158=2;
					int LA158_0 = input.LA(1);
					if ( (LA158_0==BOOLEAN||LA158_0==FLOAT||LA158_0==HEXNUMBER||(LA158_0 >= IDENT && LA158_0 <= INTEGER)||(LA158_0 >= K_AGGREGATE && LA158_0 <= K_ALL)||LA158_0==K_AS||LA158_0==K_ASCII||(LA158_0 >= K_BIGINT && LA158_0 <= K_BOOLEAN)||(LA158_0 >= K_CALLED && LA158_0 <= K_CLUSTERING)||(LA158_0 >= K_COMPACT && LA158_0 <= K_COUNTER)||(LA158_0 >= K_CUSTOM && LA158_0 <= K_DECIMAL)||(LA158_0 >= K_DISTINCT && LA158_0 <= K_DOUBLE)||(LA158_0 >= K_EXISTS && LA158_0 <= K_FLOAT)||LA158_0==K_FROZEN||(LA158_0 >= K_FUNCTION && LA158_0 <= K_FUNCTIONS)||(LA158_0 >= K_INET && LA158_0 <= K_INPUT)||LA158_0==K_INT||(LA158_0 >= K_JSON && LA158_0 <= K_KEYS)||(LA158_0 >= K_KEYSPACES && LA158_0 <= K_LANGUAGE)||(LA158_0 >= K_LIST && LA158_0 <= K_MAP)||(LA158_0 >= K_NAN && LA158_0 <= K_NOLOGIN)||LA158_0==K_NOSUPERUSER||LA158_0==K_NULL||LA158_0==K_OPTIONS||(LA158_0 >= K_PASSWORD && LA158_0 <= K_PERMISSIONS)||LA158_0==K_RETURNS||(LA158_0 >= K_ROLE && LA158_0 <= K_ROLES)||(LA158_0 >= K_SFUNC && LA158_0 <= K_TINYINT)||(LA158_0 >= K_TOKEN && LA158_0 <= K_TRIGGER)||(LA158_0 >= K_TTL && LA158_0 <= K_TYPE)||(LA158_0 >= K_USER && LA158_0 <= K_USERS)||(LA158_0 >= K_UUID && LA158_0 <= K_VARINT)||LA158_0==K_WRITETIME||(LA158_0 >= QMARK && LA158_0 <= QUOTED_NAME)||LA158_0==STRING_LITERAL||LA158_0==UUID||LA158_0==171||LA158_0==175||LA158_0==177||LA158_0==184||LA158_0==188) ) {
						alt158=1;
					}
					switch (alt158) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1273:13: t1= term ( ',' tn= term )*
							{
							pushFollow(FOLLOW_term_in_collectionLiteral8393);
							t1=term();
							state._fsp--;

							 l.add(t1); 
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1273:36: ( ',' tn= term )*
							loop157:
							while (true) {
								int alt157=2;
								int LA157_0 = input.LA(1);
								if ( (LA157_0==174) ) {
									alt157=1;
								}

								switch (alt157) {
								case 1 :
									// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1273:38: ',' tn= term
									{
									match(input,174,FOLLOW_174_in_collectionLiteral8399); 
									pushFollow(FOLLOW_term_in_collectionLiteral8403);
									tn=term();
									state._fsp--;

									 l.add(tn); 
									}
									break;

								default :
									break loop157;
								}
							}

							}
							break;

					}

					match(input,186,FOLLOW_186_in_collectionLiteral8419); 
					 value = new Lists.Literal(l); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1275:7: '{' t= term v= setOrMapLiteral[t] '}'
					{
					match(input,188,FOLLOW_188_in_collectionLiteral8429); 
					pushFollow(FOLLOW_term_in_collectionLiteral8433);
					t=term();
					state._fsp--;

					pushFollow(FOLLOW_setOrMapLiteral_in_collectionLiteral8437);
					v=setOrMapLiteral(t);
					state._fsp--;

					 value = v; 
					match(input,189,FOLLOW_189_in_collectionLiteral8442); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1278:7: '{' '}'
					{
					match(input,188,FOLLOW_188_in_collectionLiteral8460); 
					match(input,189,FOLLOW_189_in_collectionLiteral8462); 
					 value = new Sets.Literal(Collections.<Term.Raw>emptyList()); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "collectionLiteral"



	// $ANTLR start "usertypeLiteral"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1281:1: usertypeLiteral returns [UserTypes.Literal ut] : '{' k1= noncol_ident ':' v1= term ( ',' kn= noncol_ident ':' vn= term )* '}' ;
	public final UserTypes.Literal usertypeLiteral() throws RecognitionException {
		UserTypes.Literal ut = null;


		ColumnIdentifier k1 =null;
		Term.Raw v1 =null;
		ColumnIdentifier kn =null;
		Term.Raw vn =null;

		 Map<ColumnIdentifier, Term.Raw> m = new HashMap<ColumnIdentifier, Term.Raw>(); 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1285:5: ( '{' k1= noncol_ident ':' v1= term ( ',' kn= noncol_ident ':' vn= term )* '}' )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1285:7: '{' k1= noncol_ident ':' v1= term ( ',' kn= noncol_ident ':' vn= term )* '}'
			{
			match(input,188,FOLLOW_188_in_usertypeLiteral8506); 
			pushFollow(FOLLOW_noncol_ident_in_usertypeLiteral8510);
			k1=noncol_ident();
			state._fsp--;

			match(input,177,FOLLOW_177_in_usertypeLiteral8512); 
			pushFollow(FOLLOW_term_in_usertypeLiteral8516);
			v1=term();
			state._fsp--;

			 m.put(k1, v1); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1285:58: ( ',' kn= noncol_ident ':' vn= term )*
			loop160:
			while (true) {
				int alt160=2;
				int LA160_0 = input.LA(1);
				if ( (LA160_0==174) ) {
					alt160=1;
				}

				switch (alt160) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1285:60: ',' kn= noncol_ident ':' vn= term
					{
					match(input,174,FOLLOW_174_in_usertypeLiteral8522); 
					pushFollow(FOLLOW_noncol_ident_in_usertypeLiteral8526);
					kn=noncol_ident();
					state._fsp--;

					match(input,177,FOLLOW_177_in_usertypeLiteral8528); 
					pushFollow(FOLLOW_term_in_usertypeLiteral8532);
					vn=term();
					state._fsp--;

					 m.put(kn, vn); 
					}
					break;

				default :
					break loop160;
				}
			}

			match(input,189,FOLLOW_189_in_usertypeLiteral8539); 
			}

			 ut = new UserTypes.Literal(m); 
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return ut;
	}
	// $ANTLR end "usertypeLiteral"



	// $ANTLR start "tupleLiteral"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1288:1: tupleLiteral returns [Tuples.Literal tt] : '(' t1= term ( ',' tn= term )* ')' ;
	public final Tuples.Literal tupleLiteral() throws RecognitionException {
		Tuples.Literal tt = null;


		Term.Raw t1 =null;
		Term.Raw tn =null;

		 List<Term.Raw> l = new ArrayList<Term.Raw>(); 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1291:5: ( '(' t1= term ( ',' tn= term )* ')' )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1291:7: '(' t1= term ( ',' tn= term )* ')'
			{
			match(input,171,FOLLOW_171_in_tupleLiteral8576); 
			pushFollow(FOLLOW_term_in_tupleLiteral8580);
			t1=term();
			state._fsp--;

			 l.add(t1); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1291:34: ( ',' tn= term )*
			loop161:
			while (true) {
				int alt161=2;
				int LA161_0 = input.LA(1);
				if ( (LA161_0==174) ) {
					alt161=1;
				}

				switch (alt161) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1291:36: ',' tn= term
					{
					match(input,174,FOLLOW_174_in_tupleLiteral8586); 
					pushFollow(FOLLOW_term_in_tupleLiteral8590);
					tn=term();
					state._fsp--;

					 l.add(tn); 
					}
					break;

				default :
					break loop161;
				}
			}

			match(input,172,FOLLOW_172_in_tupleLiteral8597); 
			}

			 tt = new Tuples.Literal(l); 
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return tt;
	}
	// $ANTLR end "tupleLiteral"



	// $ANTLR start "value"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1294:1: value returns [Term.Raw value] : (c= constant |l= collectionLiteral |u= usertypeLiteral |t= tupleLiteral | K_NULL | ':' id= noncol_ident | QMARK );
	public final Term.Raw value() throws RecognitionException {
		Term.Raw value = null;


		Constants.Literal c =null;
		Term.Raw l =null;
		UserTypes.Literal u =null;
		Tuples.Literal t =null;
		ColumnIdentifier id =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1295:5: (c= constant |l= collectionLiteral |u= usertypeLiteral |t= tupleLiteral | K_NULL | ':' id= noncol_ident | QMARK )
			int alt162=7;
			alt162 = dfa162.predict(input);
			switch (alt162) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1295:7: c= constant
					{
					pushFollow(FOLLOW_constant_in_value8620);
					c=constant();
					state._fsp--;

					 value = c; 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1296:7: l= collectionLiteral
					{
					pushFollow(FOLLOW_collectionLiteral_in_value8642);
					l=collectionLiteral();
					state._fsp--;

					 value = l; 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1297:7: u= usertypeLiteral
					{
					pushFollow(FOLLOW_usertypeLiteral_in_value8655);
					u=usertypeLiteral();
					state._fsp--;

					 value = u; 
					}
					break;
				case 4 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1298:7: t= tupleLiteral
					{
					pushFollow(FOLLOW_tupleLiteral_in_value8670);
					t=tupleLiteral();
					state._fsp--;

					 value = t; 
					}
					break;
				case 5 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1299:7: K_NULL
					{
					match(input,K_NULL,FOLLOW_K_NULL_in_value8686); 
					 value = Constants.NULL_LITERAL; 
					}
					break;
				case 6 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1300:7: ':' id= noncol_ident
					{
					match(input,177,FOLLOW_177_in_value8710); 
					pushFollow(FOLLOW_noncol_ident_in_value8714);
					id=noncol_ident();
					state._fsp--;

					 value = newBindVariables(id); 
					}
					break;
				case 7 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1301:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_value8725); 
					 value = newBindVariables(null); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "value"



	// $ANTLR start "intValue"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1304:1: intValue returns [Term.Raw value] : (|t= INTEGER | ':' id= noncol_ident | QMARK );
	public final Term.Raw intValue() throws RecognitionException {
		Term.Raw value = null;


		Token t=null;
		ColumnIdentifier id =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1305:5: (|t= INTEGER | ':' id= noncol_ident | QMARK )
			int alt163=4;
			switch ( input.LA(1) ) {
			case EOF:
			case K_ALLOW:
			case K_AND:
			case K_APPLY:
			case K_DELETE:
			case K_INSERT:
			case K_SET:
			case K_UPDATE:
			case K_WHERE:
			case 178:
				{
				alt163=1;
				}
				break;
			case INTEGER:
				{
				alt163=2;
				}
				break;
			case 177:
				{
				alt163=3;
				}
				break;
			case QMARK:
				{
				alt163=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 163, 0, input);
				throw nvae;
			}
			switch (alt163) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1306:5: 
					{
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1306:7: t= INTEGER
					{
					t=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_intValue8771); 
					 value = Constants.Literal.integer((t!=null?t.getText():null)); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1307:7: ':' id= noncol_ident
					{
					match(input,177,FOLLOW_177_in_intValue8785); 
					pushFollow(FOLLOW_noncol_ident_in_intValue8789);
					id=noncol_ident();
					state._fsp--;

					 value = newBindVariables(id); 
					}
					break;
				case 4 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1308:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_intValue8800); 
					 value = newBindVariables(null); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "intValue"



	// $ANTLR start "functionName"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1311:1: functionName returns [FunctionName s] : (ks= keyspaceName '.' )? f= allowedFunctionName ;
	public final FunctionName functionName() throws RecognitionException {
		FunctionName s = null;


		String ks =null;
		String f =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1312:5: ( (ks= keyspaceName '.' )? f= allowedFunctionName )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1312:7: (ks= keyspaceName '.' )? f= allowedFunctionName
			{
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1312:7: (ks= keyspaceName '.' )?
			int alt164=2;
			alt164 = dfa164.predict(input);
			switch (alt164) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1312:8: ks= keyspaceName '.'
					{
					pushFollow(FOLLOW_keyspaceName_in_functionName8834);
					ks=keyspaceName();
					state._fsp--;

					match(input,176,FOLLOW_176_in_functionName8836); 
					}
					break;

			}

			pushFollow(FOLLOW_allowedFunctionName_in_functionName8842);
			f=allowedFunctionName();
			state._fsp--;

			 s = new FunctionName(ks, f); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return s;
	}
	// $ANTLR end "functionName"



	// $ANTLR start "allowedFunctionName"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1315:1: allowedFunctionName returns [String s] : (f= IDENT |f= QUOTED_NAME |u= unreserved_function_keyword | K_TOKEN | K_COUNT );
	public final String allowedFunctionName() throws RecognitionException {
		String s = null;


		Token f=null;
		String u =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1316:5: (f= IDENT |f= QUOTED_NAME |u= unreserved_function_keyword | K_TOKEN | K_COUNT )
			int alt165=5;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				alt165=1;
				}
				break;
			case QUOTED_NAME:
				{
				alt165=2;
				}
				break;
			case K_AGGREGATE:
			case K_ALL:
			case K_AS:
			case K_ASCII:
			case K_BIGINT:
			case K_BLOB:
			case K_BOOLEAN:
			case K_CALLED:
			case K_CLUSTERING:
			case K_COMPACT:
			case K_CONTAINS:
			case K_COUNTER:
			case K_CUSTOM:
			case K_DATE:
			case K_DECIMAL:
			case K_DISTINCT:
			case K_DOUBLE:
			case K_EXISTS:
			case K_FILTERING:
			case K_FINALFUNC:
			case K_FLOAT:
			case K_FROZEN:
			case K_FUNCTION:
			case K_FUNCTIONS:
			case K_INET:
			case K_INITCOND:
			case K_INPUT:
			case K_INT:
			case K_JSON:
			case K_KEYS:
			case K_KEYSPACES:
			case K_LANGUAGE:
			case K_LIST:
			case K_LOGIN:
			case K_MAP:
			case K_NOLOGIN:
			case K_NOSUPERUSER:
			case K_OPTIONS:
			case K_PASSWORD:
			case K_PERMISSION:
			case K_PERMISSIONS:
			case K_RETURNS:
			case K_ROLE:
			case K_ROLES:
			case K_SFUNC:
			case K_SMALLINT:
			case K_STATIC:
			case K_STORAGE:
			case K_STYPE:
			case K_SUPERUSER:
			case K_TEXT:
			case K_TIME:
			case K_TIMESTAMP:
			case K_TIMEUUID:
			case K_TINYINT:
			case K_TRIGGER:
			case K_TUPLE:
			case K_TYPE:
			case K_USER:
			case K_USERS:
			case K_UUID:
			case K_VALUES:
			case K_VARCHAR:
			case K_VARINT:
				{
				alt165=3;
				}
				break;
			case K_TOKEN:
				{
				alt165=4;
				}
				break;
			case K_COUNT:
				{
				alt165=5;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 165, 0, input);
				throw nvae;
			}
			switch (alt165) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1316:7: f= IDENT
					{
					f=(Token)match(input,IDENT,FOLLOW_IDENT_in_allowedFunctionName8869); 
					 s = (f!=null?f.getText():null).toLowerCase(); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1317:7: f= QUOTED_NAME
					{
					f=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_allowedFunctionName8903); 
					 s = (f!=null?f.getText():null); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1318:7: u= unreserved_function_keyword
					{
					pushFollow(FOLLOW_unreserved_function_keyword_in_allowedFunctionName8931);
					u=unreserved_function_keyword();
					state._fsp--;

					 s = u; 
					}
					break;
				case 4 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1319:7: K_TOKEN
					{
					match(input,K_TOKEN,FOLLOW_K_TOKEN_in_allowedFunctionName8941); 
					 s = "token"; 
					}
					break;
				case 5 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1320:7: K_COUNT
					{
					match(input,K_COUNT,FOLLOW_K_COUNT_in_allowedFunctionName8973); 
					 s = "count"; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return s;
	}
	// $ANTLR end "allowedFunctionName"



	// $ANTLR start "function"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1323:1: function returns [Term.Raw t] : (f= functionName '(' ')' |f= functionName '(' args= functionArgs ')' );
	public final Term.Raw function() throws RecognitionException {
		Term.Raw t = null;


		FunctionName f =null;
		List<Term.Raw> args =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1324:5: (f= functionName '(' ')' |f= functionName '(' args= functionArgs ')' )
			int alt166=2;
			alt166 = dfa166.predict(input);
			switch (alt166) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1324:7: f= functionName '(' ')'
					{
					pushFollow(FOLLOW_functionName_in_function9020);
					f=functionName();
					state._fsp--;

					match(input,171,FOLLOW_171_in_function9022); 
					match(input,172,FOLLOW_172_in_function9024); 
					 t = new FunctionCall.Raw(f, Collections.<Term.Raw>emptyList()); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1325:7: f= functionName '(' args= functionArgs ')'
					{
					pushFollow(FOLLOW_functionName_in_function9054);
					f=functionName();
					state._fsp--;

					match(input,171,FOLLOW_171_in_function9056); 
					pushFollow(FOLLOW_functionArgs_in_function9060);
					args=functionArgs();
					state._fsp--;

					match(input,172,FOLLOW_172_in_function9062); 
					 t = new FunctionCall.Raw(f, args); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return t;
	}
	// $ANTLR end "function"



	// $ANTLR start "functionArgs"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1328:1: functionArgs returns [List<Term.Raw> args] : t1= term ( ',' tn= term )* ;
	public final List<Term.Raw> functionArgs() throws RecognitionException {
		List<Term.Raw> args = null;


		Term.Raw t1 =null;
		Term.Raw tn =null;

		 args = new ArrayList<Term.Raw>(); 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1330:5: (t1= term ( ',' tn= term )* )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1330:7: t1= term ( ',' tn= term )*
			{
			pushFollow(FOLLOW_term_in_functionArgs9095);
			t1=term();
			state._fsp--;

			args.add(t1); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1330:32: ( ',' tn= term )*
			loop167:
			while (true) {
				int alt167=2;
				int LA167_0 = input.LA(1);
				if ( (LA167_0==174) ) {
					alt167=1;
				}

				switch (alt167) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1330:34: ',' tn= term
					{
					match(input,174,FOLLOW_174_in_functionArgs9101); 
					pushFollow(FOLLOW_term_in_functionArgs9105);
					tn=term();
					state._fsp--;

					 args.add(tn); 
					}
					break;

				default :
					break loop167;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return args;
	}
	// $ANTLR end "functionArgs"



	// $ANTLR start "term"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1333:1: term returns [Term.Raw term] : (v= value |f= function | '(' c= comparatorType ')' t= term );
	public final Term.Raw term() throws RecognitionException {
		Term.Raw term = null;


		Term.Raw v =null;
		Term.Raw f =null;
		CQL3Type.Raw c =null;
		Term.Raw t =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1334:5: (v= value |f= function | '(' c= comparatorType ')' t= term )
			int alt168=3;
			alt168 = dfa168.predict(input);
			switch (alt168) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1334:7: v= value
					{
					pushFollow(FOLLOW_value_in_term9133);
					v=value();
					state._fsp--;

					 term = v; 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1335:7: f= function
					{
					pushFollow(FOLLOW_function_in_term9170);
					f=function();
					state._fsp--;

					 term = f; 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1336:7: '(' c= comparatorType ')' t= term
					{
					match(input,171,FOLLOW_171_in_term9202); 
					pushFollow(FOLLOW_comparatorType_in_term9206);
					c=comparatorType();
					state._fsp--;

					match(input,172,FOLLOW_172_in_term9208); 
					pushFollow(FOLLOW_term_in_term9212);
					t=term();
					state._fsp--;

					 term = new TypeCast(c, t); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return term;
	}
	// $ANTLR end "term"



	// $ANTLR start "columnOperation"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1339:1: columnOperation[List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations] : key= cident columnOperationDifferentiator[operations, key] ;
	public final void columnOperation(List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations) throws RecognitionException {
		ColumnIdentifier.Raw key =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1340:5: (key= cident columnOperationDifferentiator[operations, key] )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1340:7: key= cident columnOperationDifferentiator[operations, key]
			{
			pushFollow(FOLLOW_cident_in_columnOperation9235);
			key=cident();
			state._fsp--;

			pushFollow(FOLLOW_columnOperationDifferentiator_in_columnOperation9237);
			columnOperationDifferentiator(operations, key);
			state._fsp--;

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "columnOperation"



	// $ANTLR start "columnOperationDifferentiator"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1343:1: columnOperationDifferentiator[List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations, ColumnIdentifier.Raw key] : ( '=' normalColumnOperation[operations, key] | '[' k= term ']' specializedColumnOperation[operations, key, k] );
	public final void columnOperationDifferentiator(List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations, ColumnIdentifier.Raw key) throws RecognitionException {
		Term.Raw k =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1344:5: ( '=' normalColumnOperation[operations, key] | '[' k= term ']' specializedColumnOperation[operations, key, k] )
			int alt169=2;
			int LA169_0 = input.LA(1);
			if ( (LA169_0==181) ) {
				alt169=1;
			}
			else if ( (LA169_0==184) ) {
				alt169=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 169, 0, input);
				throw nvae;
			}

			switch (alt169) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1344:7: '=' normalColumnOperation[operations, key]
					{
					match(input,181,FOLLOW_181_in_columnOperationDifferentiator9256); 
					pushFollow(FOLLOW_normalColumnOperation_in_columnOperationDifferentiator9258);
					normalColumnOperation(operations, key);
					state._fsp--;

					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1345:7: '[' k= term ']' specializedColumnOperation[operations, key, k]
					{
					match(input,184,FOLLOW_184_in_columnOperationDifferentiator9267); 
					pushFollow(FOLLOW_term_in_columnOperationDifferentiator9271);
					k=term();
					state._fsp--;

					match(input,186,FOLLOW_186_in_columnOperationDifferentiator9273); 
					pushFollow(FOLLOW_specializedColumnOperation_in_columnOperationDifferentiator9275);
					specializedColumnOperation(operations, key, k);
					state._fsp--;

					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "columnOperationDifferentiator"



	// $ANTLR start "normalColumnOperation"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1348:1: normalColumnOperation[List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations, ColumnIdentifier.Raw key] : (t= term ( '+' c= cident )? |c= cident sig= ( '+' | '-' ) t= term |c= cident i= INTEGER );
	public final void normalColumnOperation(List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations, ColumnIdentifier.Raw key) throws RecognitionException {
		Token sig=null;
		Token i=null;
		Term.Raw t =null;
		ColumnIdentifier.Raw c =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1349:5: (t= term ( '+' c= cident )? |c= cident sig= ( '+' | '-' ) t= term |c= cident i= INTEGER )
			int alt171=3;
			alt171 = dfa171.predict(input);
			switch (alt171) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1349:7: t= term ( '+' c= cident )?
					{
					pushFollow(FOLLOW_term_in_normalColumnOperation9296);
					t=term();
					state._fsp--;

					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1349:14: ( '+' c= cident )?
					int alt170=2;
					int LA170_0 = input.LA(1);
					if ( (LA170_0==173) ) {
						alt170=1;
					}
					switch (alt170) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1349:15: '+' c= cident
							{
							match(input,173,FOLLOW_173_in_normalColumnOperation9299); 
							pushFollow(FOLLOW_cident_in_normalColumnOperation9303);
							c=cident();
							state._fsp--;

							}
							break;

					}


					          if (c == null)
					          {
					              addRawUpdate(operations, key, new Operation.SetValue(t));
					          }
					          else
					          {
					              if (!key.equals(c))
					                  addRecognitionError("Only expressions of the form X = <value> + X are supported.");
					              addRawUpdate(operations, key, new Operation.Prepend(t));
					          }
					      
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1362:7: c= cident sig= ( '+' | '-' ) t= term
					{
					pushFollow(FOLLOW_cident_in_normalColumnOperation9324);
					c=cident();
					state._fsp--;

					sig=input.LT(1);
					if ( input.LA(1)==173||input.LA(1)==175 ) {
						input.consume();
						state.errorRecovery=false;
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_term_in_normalColumnOperation9338);
					t=term();
					state._fsp--;


					          if (!key.equals(c))
					              addRecognitionError("Only expressions of the form X = X " + (sig!=null?sig.getText():null) + "<value> are supported.");
					          addRawUpdate(operations, key, (sig!=null?sig.getText():null).equals("+") ? new Operation.Addition(t) : new Operation.Substraction(t));
					      
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1368:7: c= cident i= INTEGER
					{
					pushFollow(FOLLOW_cident_in_normalColumnOperation9356);
					c=cident();
					state._fsp--;

					i=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_normalColumnOperation9360); 

					          // Note that this production *is* necessary because X = X - 3 will in fact be lexed as [ X, '=', X, INTEGER].
					          if (!key.equals(c))
					              // We don't yet allow a '+' in front of an integer, but we could in the future really, so let's be future-proof in our error message
					              addRecognitionError("Only expressions of the form X = X " + ((i!=null?i.getText():null).charAt(0) == '-' ? '-' : '+') + " <value> are supported.");
					          addRawUpdate(operations, key, new Operation.Addition(Constants.Literal.integer((i!=null?i.getText():null))));
					      
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "normalColumnOperation"



	// $ANTLR start "specializedColumnOperation"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1378:1: specializedColumnOperation[List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations, ColumnIdentifier.Raw key, Term.Raw k] : '=' t= term ;
	public final void specializedColumnOperation(List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations, ColumnIdentifier.Raw key, Term.Raw k) throws RecognitionException {
		Term.Raw t =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1379:5: ( '=' t= term )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1379:7: '=' t= term
			{
			match(input,181,FOLLOW_181_in_specializedColumnOperation9386); 
			pushFollow(FOLLOW_term_in_specializedColumnOperation9390);
			t=term();
			state._fsp--;


			          addRawUpdate(operations, key, new Operation.SetElement(k, t));
			      
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "specializedColumnOperation"



	// $ANTLR start "columnCondition"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1385:1: columnCondition[List<Pair<ColumnIdentifier.Raw, ColumnCondition.Raw>> conditions] : key= cident (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) | '[' element= term ']' (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) ) ) ;
	public final void columnCondition(List<Pair<ColumnIdentifier.Raw, ColumnCondition.Raw>> conditions) throws RecognitionException {
		ColumnIdentifier.Raw key =null;
		Operator op =null;
		Term.Raw t =null;
		List<Term.Raw> values =null;
		AbstractMarker.INRaw marker =null;
		Term.Raw element =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1387:5: (key= cident (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) | '[' element= term ']' (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) ) ) )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1387:7: key= cident (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) | '[' element= term ']' (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) ) )
			{
			pushFollow(FOLLOW_cident_in_columnCondition9423);
			key=cident();
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1388:9: (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) | '[' element= term ']' (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) ) )
			int alt175=3;
			switch ( input.LA(1) ) {
			case 170:
			case 179:
			case 180:
			case 181:
			case 182:
			case 183:
				{
				alt175=1;
				}
				break;
			case K_IN:
				{
				alt175=2;
				}
				break;
			case 184:
				{
				alt175=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 175, 0, input);
				throw nvae;
			}
			switch (alt175) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1388:11: op= relationType t= term
					{
					pushFollow(FOLLOW_relationType_in_columnCondition9437);
					op=relationType();
					state._fsp--;

					pushFollow(FOLLOW_term_in_columnCondition9441);
					t=term();
					state._fsp--;

					 conditions.add(Pair.create(key, ColumnCondition.Raw.simpleCondition(t, op))); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1389:11: K_IN (values= singleColumnInValues |marker= inMarker )
					{
					match(input,K_IN,FOLLOW_K_IN_in_columnCondition9455); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1390:13: (values= singleColumnInValues |marker= inMarker )
					int alt172=2;
					int LA172_0 = input.LA(1);
					if ( (LA172_0==171) ) {
						alt172=1;
					}
					else if ( (LA172_0==QMARK||LA172_0==177) ) {
						alt172=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 172, 0, input);
						throw nvae;
					}

					switch (alt172) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1390:15: values= singleColumnInValues
							{
							pushFollow(FOLLOW_singleColumnInValues_in_columnCondition9473);
							values=singleColumnInValues();
							state._fsp--;

							 conditions.add(Pair.create(key, ColumnCondition.Raw.simpleInCondition(values))); 
							}
							break;
						case 2 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1391:15: marker= inMarker
							{
							pushFollow(FOLLOW_inMarker_in_columnCondition9493);
							marker=inMarker();
							state._fsp--;

							 conditions.add(Pair.create(key, ColumnCondition.Raw.simpleInCondition(marker))); 
							}
							break;

					}

					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1393:11: '[' element= term ']' (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) )
					{
					match(input,184,FOLLOW_184_in_columnCondition9521); 
					pushFollow(FOLLOW_term_in_columnCondition9525);
					element=term();
					state._fsp--;

					match(input,186,FOLLOW_186_in_columnCondition9527); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1394:13: (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) )
					int alt174=2;
					int LA174_0 = input.LA(1);
					if ( (LA174_0==170||(LA174_0 >= 179 && LA174_0 <= 183)) ) {
						alt174=1;
					}
					else if ( (LA174_0==K_IN) ) {
						alt174=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 174, 0, input);
						throw nvae;
					}

					switch (alt174) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1394:15: op= relationType t= term
							{
							pushFollow(FOLLOW_relationType_in_columnCondition9545);
							op=relationType();
							state._fsp--;

							pushFollow(FOLLOW_term_in_columnCondition9549);
							t=term();
							state._fsp--;

							 conditions.add(Pair.create(key, ColumnCondition.Raw.collectionCondition(t, element, op))); 
							}
							break;
						case 2 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1395:15: K_IN (values= singleColumnInValues |marker= inMarker )
							{
							match(input,K_IN,FOLLOW_K_IN_in_columnCondition9567); 
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1396:17: (values= singleColumnInValues |marker= inMarker )
							int alt173=2;
							int LA173_0 = input.LA(1);
							if ( (LA173_0==171) ) {
								alt173=1;
							}
							else if ( (LA173_0==QMARK||LA173_0==177) ) {
								alt173=2;
							}

							else {
								NoViableAltException nvae =
									new NoViableAltException("", 173, 0, input);
								throw nvae;
							}

							switch (alt173) {
								case 1 :
									// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1396:19: values= singleColumnInValues
									{
									pushFollow(FOLLOW_singleColumnInValues_in_columnCondition9589);
									values=singleColumnInValues();
									state._fsp--;

									 conditions.add(Pair.create(key, ColumnCondition.Raw.collectionInCondition(element, values))); 
									}
									break;
								case 2 :
									// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1397:19: marker= inMarker
									{
									pushFollow(FOLLOW_inMarker_in_columnCondition9613);
									marker=inMarker();
									state._fsp--;

									 conditions.add(Pair.create(key, ColumnCondition.Raw.collectionInCondition(element, marker))); 
									}
									break;

							}

							}
							break;

					}

					}
					break;

			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "columnCondition"



	// $ANTLR start "properties"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1403:1: properties[PropertyDefinitions props] : property[props] ( K_AND property[props] )* ;
	public final void properties(PropertyDefinitions props) throws RecognitionException {
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1404:5: ( property[props] ( K_AND property[props] )* )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1404:7: property[props] ( K_AND property[props] )*
			{
			pushFollow(FOLLOW_property_in_properties9675);
			property(props);
			state._fsp--;

			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1404:23: ( K_AND property[props] )*
			loop176:
			while (true) {
				int alt176=2;
				int LA176_0 = input.LA(1);
				if ( (LA176_0==K_AND) ) {
					alt176=1;
				}

				switch (alt176) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1404:24: K_AND property[props]
					{
					match(input,K_AND,FOLLOW_K_AND_in_properties9679); 
					pushFollow(FOLLOW_property_in_properties9681);
					property(props);
					state._fsp--;

					}
					break;

				default :
					break loop176;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "properties"



	// $ANTLR start "property"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1407:1: property[PropertyDefinitions props] : (k= noncol_ident '=' simple= propertyValue |k= noncol_ident '=' map= mapLiteral );
	public final void property(PropertyDefinitions props) throws RecognitionException {
		ColumnIdentifier k =null;
		String simple =null;
		Maps.Literal map =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1408:5: (k= noncol_ident '=' simple= propertyValue |k= noncol_ident '=' map= mapLiteral )
			int alt177=2;
			alt177 = dfa177.predict(input);
			switch (alt177) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1408:7: k= noncol_ident '=' simple= propertyValue
					{
					pushFollow(FOLLOW_noncol_ident_in_property9704);
					k=noncol_ident();
					state._fsp--;

					match(input,181,FOLLOW_181_in_property9706); 
					pushFollow(FOLLOW_propertyValue_in_property9710);
					simple=propertyValue();
					state._fsp--;

					 try { props.addProperty(k.toString(), simple); } catch (SyntaxException e) { addRecognitionError(e.getMessage()); } 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1409:7: k= noncol_ident '=' map= mapLiteral
					{
					pushFollow(FOLLOW_noncol_ident_in_property9722);
					k=noncol_ident();
					state._fsp--;

					match(input,181,FOLLOW_181_in_property9724); 
					pushFollow(FOLLOW_mapLiteral_in_property9728);
					map=mapLiteral();
					state._fsp--;

					 try { props.addProperty(k.toString(), convertPropertyMap(map)); } catch (SyntaxException e) { addRecognitionError(e.getMessage()); } 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "property"



	// $ANTLR start "propertyValue"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1412:1: propertyValue returns [String str] : (c= constant |u= unreserved_keyword );
	public final String propertyValue() throws RecognitionException {
		String str = null;


		Constants.Literal c =null;
		String u =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1413:5: (c= constant |u= unreserved_keyword )
			int alt178=2;
			int LA178_0 = input.LA(1);
			if ( (LA178_0==BOOLEAN||LA178_0==FLOAT||LA178_0==HEXNUMBER||LA178_0==INTEGER||LA178_0==K_INFINITY||LA178_0==K_NAN||LA178_0==STRING_LITERAL||LA178_0==UUID||LA178_0==175) ) {
				alt178=1;
			}
			else if ( ((LA178_0 >= K_AGGREGATE && LA178_0 <= K_ALL)||LA178_0==K_AS||LA178_0==K_ASCII||(LA178_0 >= K_BIGINT && LA178_0 <= K_BOOLEAN)||(LA178_0 >= K_CALLED && LA178_0 <= K_CLUSTERING)||(LA178_0 >= K_COMPACT && LA178_0 <= K_COUNTER)||(LA178_0 >= K_CUSTOM && LA178_0 <= K_DECIMAL)||(LA178_0 >= K_DISTINCT && LA178_0 <= K_DOUBLE)||(LA178_0 >= K_EXISTS && LA178_0 <= K_FLOAT)||LA178_0==K_FROZEN||(LA178_0 >= K_FUNCTION && LA178_0 <= K_FUNCTIONS)||LA178_0==K_INET||(LA178_0 >= K_INITCOND && LA178_0 <= K_INPUT)||LA178_0==K_INT||(LA178_0 >= K_JSON && LA178_0 <= K_KEYS)||(LA178_0 >= K_KEYSPACES && LA178_0 <= K_LANGUAGE)||(LA178_0 >= K_LIST && LA178_0 <= K_MAP)||LA178_0==K_NOLOGIN||LA178_0==K_NOSUPERUSER||LA178_0==K_OPTIONS||(LA178_0 >= K_PASSWORD && LA178_0 <= K_PERMISSIONS)||LA178_0==K_RETURNS||(LA178_0 >= K_ROLE && LA178_0 <= K_ROLES)||(LA178_0 >= K_SFUNC && LA178_0 <= K_TINYINT)||LA178_0==K_TRIGGER||(LA178_0 >= K_TTL && LA178_0 <= K_TYPE)||(LA178_0 >= K_USER && LA178_0 <= K_USERS)||(LA178_0 >= K_UUID && LA178_0 <= K_VARINT)||LA178_0==K_WRITETIME) ) {
				alt178=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 178, 0, input);
				throw nvae;
			}

			switch (alt178) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1413:7: c= constant
					{
					pushFollow(FOLLOW_constant_in_propertyValue9753);
					c=constant();
					state._fsp--;

					 str = c.getRawText(); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1414:7: u= unreserved_keyword
					{
					pushFollow(FOLLOW_unreserved_keyword_in_propertyValue9775);
					u=unreserved_keyword();
					state._fsp--;

					 str = u; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return str;
	}
	// $ANTLR end "propertyValue"



	// $ANTLR start "relationType"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1417:1: relationType returns [Operator op] : ( '=' | '<' | '<=' | '>' | '>=' | '!=' );
	public final Operator relationType() throws RecognitionException {
		Operator op = null;


		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1418:5: ( '=' | '<' | '<=' | '>' | '>=' | '!=' )
			int alt179=6;
			switch ( input.LA(1) ) {
			case 181:
				{
				alt179=1;
				}
				break;
			case 179:
				{
				alt179=2;
				}
				break;
			case 180:
				{
				alt179=3;
				}
				break;
			case 182:
				{
				alt179=4;
				}
				break;
			case 183:
				{
				alt179=5;
				}
				break;
			case 170:
				{
				alt179=6;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 179, 0, input);
				throw nvae;
			}
			switch (alt179) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1418:7: '='
					{
					match(input,181,FOLLOW_181_in_relationType9798); 
					 op = Operator.EQ; 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1419:7: '<'
					{
					match(input,179,FOLLOW_179_in_relationType9809); 
					 op = Operator.LT; 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1420:7: '<='
					{
					match(input,180,FOLLOW_180_in_relationType9820); 
					 op = Operator.LTE; 
					}
					break;
				case 4 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1421:7: '>'
					{
					match(input,182,FOLLOW_182_in_relationType9830); 
					 op = Operator.GT; 
					}
					break;
				case 5 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1422:7: '>='
					{
					match(input,183,FOLLOW_183_in_relationType9841); 
					 op = Operator.GTE; 
					}
					break;
				case 6 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1423:7: '!='
					{
					match(input,170,FOLLOW_170_in_relationType9851); 
					 op = Operator.NEQ; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return op;
	}
	// $ANTLR end "relationType"



	// $ANTLR start "relation"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1426:1: relation[WhereClause.Builder clauses] : (name= cident type= relationType t= term |name= cident K_IS K_NOT K_NULL | K_TOKEN l= tupleOfIdentifiers type= relationType t= term |name= cident K_IN marker= inMarker |name= cident K_IN inValues= singleColumnInValues |name= cident K_CONTAINS ( K_KEY )? t= term |name= cident '[' key= term ']' type= relationType t= term |ids= tupleOfIdentifiers ( K_IN ( '(' ')' |tupleInMarker= inMarkerForTuple |literals= tupleOfTupleLiterals |markers= tupleOfMarkersForTuples ) |type= relationType literal= tupleLiteral |type= relationType tupleMarker= markerForTuple ) | '(' relation[$clauses] ')' );
	public final void relation(WhereClause.Builder clauses) throws RecognitionException {
		ColumnIdentifier.Raw name =null;
		Operator type =null;
		Term.Raw t =null;
		List<ColumnIdentifier.Raw> l =null;
		AbstractMarker.INRaw marker =null;
		List<Term.Raw> inValues =null;
		Term.Raw key =null;
		List<ColumnIdentifier.Raw> ids =null;
		Tuples.INRaw tupleInMarker =null;
		List<Tuples.Literal> literals =null;
		List<Tuples.Raw> markers =null;
		Tuples.Literal literal =null;
		Tuples.Raw tupleMarker =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1427:5: (name= cident type= relationType t= term |name= cident K_IS K_NOT K_NULL | K_TOKEN l= tupleOfIdentifiers type= relationType t= term |name= cident K_IN marker= inMarker |name= cident K_IN inValues= singleColumnInValues |name= cident K_CONTAINS ( K_KEY )? t= term |name= cident '[' key= term ']' type= relationType t= term |ids= tupleOfIdentifiers ( K_IN ( '(' ')' |tupleInMarker= inMarkerForTuple |literals= tupleOfTupleLiterals |markers= tupleOfMarkersForTuples ) |type= relationType literal= tupleLiteral |type= relationType tupleMarker= markerForTuple ) | '(' relation[$clauses] ')' )
			int alt183=9;
			alt183 = dfa183.predict(input);
			switch (alt183) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1427:7: name= cident type= relationType t= term
					{
					pushFollow(FOLLOW_cident_in_relation9873);
					name=cident();
					state._fsp--;

					pushFollow(FOLLOW_relationType_in_relation9877);
					type=relationType();
					state._fsp--;

					pushFollow(FOLLOW_term_in_relation9881);
					t=term();
					state._fsp--;

					 clauses.add(new SingleColumnRelation(name, type, t)); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1428:7: name= cident K_IS K_NOT K_NULL
					{
					pushFollow(FOLLOW_cident_in_relation9893);
					name=cident();
					state._fsp--;

					match(input,K_IS,FOLLOW_K_IS_in_relation9895); 
					match(input,K_NOT,FOLLOW_K_NOT_in_relation9897); 
					match(input,K_NULL,FOLLOW_K_NULL_in_relation9899); 
					 clauses.add(new SingleColumnRelation(name, Operator.IS_NOT, Constants.NULL_LITERAL)); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1429:7: K_TOKEN l= tupleOfIdentifiers type= relationType t= term
					{
					match(input,K_TOKEN,FOLLOW_K_TOKEN_in_relation9909); 
					pushFollow(FOLLOW_tupleOfIdentifiers_in_relation9913);
					l=tupleOfIdentifiers();
					state._fsp--;

					pushFollow(FOLLOW_relationType_in_relation9917);
					type=relationType();
					state._fsp--;

					pushFollow(FOLLOW_term_in_relation9921);
					t=term();
					state._fsp--;

					 clauses.add(new TokenRelation(l, type, t)); 
					}
					break;
				case 4 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1431:7: name= cident K_IN marker= inMarker
					{
					pushFollow(FOLLOW_cident_in_relation9941);
					name=cident();
					state._fsp--;

					match(input,K_IN,FOLLOW_K_IN_in_relation9943); 
					pushFollow(FOLLOW_inMarker_in_relation9947);
					marker=inMarker();
					state._fsp--;

					 clauses.add(new SingleColumnRelation(name, Operator.IN, marker)); 
					}
					break;
				case 5 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1433:7: name= cident K_IN inValues= singleColumnInValues
					{
					pushFollow(FOLLOW_cident_in_relation9967);
					name=cident();
					state._fsp--;

					match(input,K_IN,FOLLOW_K_IN_in_relation9969); 
					pushFollow(FOLLOW_singleColumnInValues_in_relation9973);
					inValues=singleColumnInValues();
					state._fsp--;

					 clauses.add(SingleColumnRelation.createInRelation(name, inValues)); 
					}
					break;
				case 6 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1435:7: name= cident K_CONTAINS ( K_KEY )? t= term
					{
					pushFollow(FOLLOW_cident_in_relation9993);
					name=cident();
					state._fsp--;

					match(input,K_CONTAINS,FOLLOW_K_CONTAINS_in_relation9995); 
					 Operator rt = Operator.CONTAINS; 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1435:67: ( K_KEY )?
					int alt180=2;
					int LA180_0 = input.LA(1);
					if ( (LA180_0==K_KEY) ) {
						int LA180_1 = input.LA(2);
						if ( (LA180_1==BOOLEAN||LA180_1==FLOAT||LA180_1==HEXNUMBER||(LA180_1 >= IDENT && LA180_1 <= INTEGER)||(LA180_1 >= K_AGGREGATE && LA180_1 <= K_ALL)||LA180_1==K_AS||LA180_1==K_ASCII||(LA180_1 >= K_BIGINT && LA180_1 <= K_BOOLEAN)||(LA180_1 >= K_CALLED && LA180_1 <= K_CLUSTERING)||(LA180_1 >= K_COMPACT && LA180_1 <= K_COUNTER)||(LA180_1 >= K_CUSTOM && LA180_1 <= K_DECIMAL)||(LA180_1 >= K_DISTINCT && LA180_1 <= K_DOUBLE)||(LA180_1 >= K_EXISTS && LA180_1 <= K_FLOAT)||LA180_1==K_FROZEN||(LA180_1 >= K_FUNCTION && LA180_1 <= K_FUNCTIONS)||(LA180_1 >= K_INET && LA180_1 <= K_INPUT)||LA180_1==K_INT||(LA180_1 >= K_JSON && LA180_1 <= K_KEYS)||(LA180_1 >= K_KEYSPACES && LA180_1 <= K_LANGUAGE)||(LA180_1 >= K_LIST && LA180_1 <= K_MAP)||(LA180_1 >= K_NAN && LA180_1 <= K_NOLOGIN)||LA180_1==K_NOSUPERUSER||LA180_1==K_NULL||LA180_1==K_OPTIONS||(LA180_1 >= K_PASSWORD && LA180_1 <= K_PERMISSIONS)||LA180_1==K_RETURNS||(LA180_1 >= K_ROLE && LA180_1 <= K_ROLES)||(LA180_1 >= K_SFUNC && LA180_1 <= K_TINYINT)||(LA180_1 >= K_TOKEN && LA180_1 <= K_TRIGGER)||(LA180_1 >= K_TTL && LA180_1 <= K_TYPE)||(LA180_1 >= K_USER && LA180_1 <= K_USERS)||(LA180_1 >= K_UUID && LA180_1 <= K_VARINT)||LA180_1==K_WRITETIME||(LA180_1 >= QMARK && LA180_1 <= QUOTED_NAME)||LA180_1==STRING_LITERAL||LA180_1==UUID||LA180_1==171||LA180_1==175||LA180_1==177||LA180_1==184||LA180_1==188) ) {
							alt180=1;
						}
					}
					switch (alt180) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1435:68: K_KEY
							{
							match(input,K_KEY,FOLLOW_K_KEY_in_relation10000); 
							 rt = Operator.CONTAINS_KEY; 
							}
							break;

					}

					pushFollow(FOLLOW_term_in_relation10016);
					t=term();
					state._fsp--;

					 clauses.add(new SingleColumnRelation(name, rt, t)); 
					}
					break;
				case 7 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1437:7: name= cident '[' key= term ']' type= relationType t= term
					{
					pushFollow(FOLLOW_cident_in_relation10028);
					name=cident();
					state._fsp--;

					match(input,184,FOLLOW_184_in_relation10030); 
					pushFollow(FOLLOW_term_in_relation10034);
					key=term();
					state._fsp--;

					match(input,186,FOLLOW_186_in_relation10036); 
					pushFollow(FOLLOW_relationType_in_relation10040);
					type=relationType();
					state._fsp--;

					pushFollow(FOLLOW_term_in_relation10044);
					t=term();
					state._fsp--;

					 clauses.add(new SingleColumnRelation(name, key, type, t)); 
					}
					break;
				case 8 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1438:7: ids= tupleOfIdentifiers ( K_IN ( '(' ')' |tupleInMarker= inMarkerForTuple |literals= tupleOfTupleLiterals |markers= tupleOfMarkersForTuples ) |type= relationType literal= tupleLiteral |type= relationType tupleMarker= markerForTuple )
					{
					pushFollow(FOLLOW_tupleOfIdentifiers_in_relation10056);
					ids=tupleOfIdentifiers();
					state._fsp--;

					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1439:7: ( K_IN ( '(' ')' |tupleInMarker= inMarkerForTuple |literals= tupleOfTupleLiterals |markers= tupleOfMarkersForTuples ) |type= relationType literal= tupleLiteral |type= relationType tupleMarker= markerForTuple )
					int alt182=3;
					alt182 = dfa182.predict(input);
					switch (alt182) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1439:9: K_IN ( '(' ')' |tupleInMarker= inMarkerForTuple |literals= tupleOfTupleLiterals |markers= tupleOfMarkersForTuples )
							{
							match(input,K_IN,FOLLOW_K_IN_in_relation10066); 
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1440:11: ( '(' ')' |tupleInMarker= inMarkerForTuple |literals= tupleOfTupleLiterals |markers= tupleOfMarkersForTuples )
							int alt181=4;
							int LA181_0 = input.LA(1);
							if ( (LA181_0==171) ) {
								switch ( input.LA(2) ) {
								case 172:
									{
									alt181=1;
									}
									break;
								case 171:
									{
									alt181=3;
									}
									break;
								case QMARK:
								case 177:
									{
									alt181=4;
									}
									break;
								default:
									int nvaeMark = input.mark();
									try {
										input.consume();
										NoViableAltException nvae =
											new NoViableAltException("", 181, 1, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}
							}
							else if ( (LA181_0==QMARK||LA181_0==177) ) {
								alt181=2;
							}

							else {
								NoViableAltException nvae =
									new NoViableAltException("", 181, 0, input);
								throw nvae;
							}

							switch (alt181) {
								case 1 :
									// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1440:13: '(' ')'
									{
									match(input,171,FOLLOW_171_in_relation10080); 
									match(input,172,FOLLOW_172_in_relation10082); 
									 clauses.add(MultiColumnRelation.createInRelation(ids, new ArrayList<Tuples.Literal>())); 
									}
									break;
								case 2 :
									// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1442:13: tupleInMarker= inMarkerForTuple
									{
									pushFollow(FOLLOW_inMarkerForTuple_in_relation10114);
									tupleInMarker=inMarkerForTuple();
									state._fsp--;

									 clauses.add(MultiColumnRelation.createSingleMarkerInRelation(ids, tupleInMarker)); 
									}
									break;
								case 3 :
									// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1444:13: literals= tupleOfTupleLiterals
									{
									pushFollow(FOLLOW_tupleOfTupleLiterals_in_relation10148);
									literals=tupleOfTupleLiterals();
									state._fsp--;


									                  clauses.add(MultiColumnRelation.createInRelation(ids, literals));
									              
									}
									break;
								case 4 :
									// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1448:13: markers= tupleOfMarkersForTuples
									{
									pushFollow(FOLLOW_tupleOfMarkersForTuples_in_relation10182);
									markers=tupleOfMarkersForTuples();
									state._fsp--;

									 clauses.add(MultiColumnRelation.createInRelation(ids, markers)); 
									}
									break;

							}

							}
							break;
						case 2 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1451:9: type= relationType literal= tupleLiteral
							{
							pushFollow(FOLLOW_relationType_in_relation10224);
							type=relationType();
							state._fsp--;

							pushFollow(FOLLOW_tupleLiteral_in_relation10228);
							literal=tupleLiteral();
							state._fsp--;


							              clauses.add(MultiColumnRelation.createNonInRelation(ids, type, literal));
							          
							}
							break;
						case 3 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1455:9: type= relationType tupleMarker= markerForTuple
							{
							pushFollow(FOLLOW_relationType_in_relation10254);
							type=relationType();
							state._fsp--;

							pushFollow(FOLLOW_markerForTuple_in_relation10258);
							tupleMarker=markerForTuple();
							state._fsp--;

							 clauses.add(MultiColumnRelation.createNonInRelation(ids, type, tupleMarker)); 
							}
							break;

					}

					}
					break;
				case 9 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1458:7: '(' relation[$clauses] ')'
					{
					match(input,171,FOLLOW_171_in_relation10288); 
					pushFollow(FOLLOW_relation_in_relation10290);
					relation(clauses);
					state._fsp--;

					match(input,172,FOLLOW_172_in_relation10293); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "relation"



	// $ANTLR start "inMarker"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1461:1: inMarker returns [AbstractMarker.INRaw marker] : ( QMARK | ':' name= noncol_ident );
	public final AbstractMarker.INRaw inMarker() throws RecognitionException {
		AbstractMarker.INRaw marker = null;


		ColumnIdentifier name =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1462:5: ( QMARK | ':' name= noncol_ident )
			int alt184=2;
			int LA184_0 = input.LA(1);
			if ( (LA184_0==QMARK) ) {
				alt184=1;
			}
			else if ( (LA184_0==177) ) {
				alt184=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 184, 0, input);
				throw nvae;
			}

			switch (alt184) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1462:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_inMarker10314); 
					 marker = newINBindVariables(null); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1463:7: ':' name= noncol_ident
					{
					match(input,177,FOLLOW_177_in_inMarker10324); 
					pushFollow(FOLLOW_noncol_ident_in_inMarker10328);
					name=noncol_ident();
					state._fsp--;

					 marker = newINBindVariables(name); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return marker;
	}
	// $ANTLR end "inMarker"



	// $ANTLR start "tupleOfIdentifiers"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1466:1: tupleOfIdentifiers returns [List<ColumnIdentifier.Raw> ids] : '(' n1= cident ( ',' ni= cident )* ')' ;
	public final List<ColumnIdentifier.Raw> tupleOfIdentifiers() throws RecognitionException {
		List<ColumnIdentifier.Raw> ids = null;


		ColumnIdentifier.Raw n1 =null;
		ColumnIdentifier.Raw ni =null;

		 ids = new ArrayList<ColumnIdentifier.Raw>(); 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1468:5: ( '(' n1= cident ( ',' ni= cident )* ')' )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1468:7: '(' n1= cident ( ',' ni= cident )* ')'
			{
			match(input,171,FOLLOW_171_in_tupleOfIdentifiers10360); 
			pushFollow(FOLLOW_cident_in_tupleOfIdentifiers10364);
			n1=cident();
			state._fsp--;

			 ids.add(n1); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1468:39: ( ',' ni= cident )*
			loop185:
			while (true) {
				int alt185=2;
				int LA185_0 = input.LA(1);
				if ( (LA185_0==174) ) {
					alt185=1;
				}

				switch (alt185) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1468:40: ',' ni= cident
					{
					match(input,174,FOLLOW_174_in_tupleOfIdentifiers10369); 
					pushFollow(FOLLOW_cident_in_tupleOfIdentifiers10373);
					ni=cident();
					state._fsp--;

					 ids.add(ni); 
					}
					break;

				default :
					break loop185;
				}
			}

			match(input,172,FOLLOW_172_in_tupleOfIdentifiers10379); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return ids;
	}
	// $ANTLR end "tupleOfIdentifiers"



	// $ANTLR start "singleColumnInValues"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1471:1: singleColumnInValues returns [List<Term.Raw> terms] : '(' (t1= term ( ',' ti= term )* )? ')' ;
	public final List<Term.Raw> singleColumnInValues() throws RecognitionException {
		List<Term.Raw> terms = null;


		Term.Raw t1 =null;
		Term.Raw ti =null;

		 terms = new ArrayList<Term.Raw>(); 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1473:5: ( '(' (t1= term ( ',' ti= term )* )? ')' )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1473:7: '(' (t1= term ( ',' ti= term )* )? ')'
			{
			match(input,171,FOLLOW_171_in_singleColumnInValues10409); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1473:11: (t1= term ( ',' ti= term )* )?
			int alt187=2;
			int LA187_0 = input.LA(1);
			if ( (LA187_0==BOOLEAN||LA187_0==FLOAT||LA187_0==HEXNUMBER||(LA187_0 >= IDENT && LA187_0 <= INTEGER)||(LA187_0 >= K_AGGREGATE && LA187_0 <= K_ALL)||LA187_0==K_AS||LA187_0==K_ASCII||(LA187_0 >= K_BIGINT && LA187_0 <= K_BOOLEAN)||(LA187_0 >= K_CALLED && LA187_0 <= K_CLUSTERING)||(LA187_0 >= K_COMPACT && LA187_0 <= K_COUNTER)||(LA187_0 >= K_CUSTOM && LA187_0 <= K_DECIMAL)||(LA187_0 >= K_DISTINCT && LA187_0 <= K_DOUBLE)||(LA187_0 >= K_EXISTS && LA187_0 <= K_FLOAT)||LA187_0==K_FROZEN||(LA187_0 >= K_FUNCTION && LA187_0 <= K_FUNCTIONS)||(LA187_0 >= K_INET && LA187_0 <= K_INPUT)||LA187_0==K_INT||(LA187_0 >= K_JSON && LA187_0 <= K_KEYS)||(LA187_0 >= K_KEYSPACES && LA187_0 <= K_LANGUAGE)||(LA187_0 >= K_LIST && LA187_0 <= K_MAP)||(LA187_0 >= K_NAN && LA187_0 <= K_NOLOGIN)||LA187_0==K_NOSUPERUSER||LA187_0==K_NULL||LA187_0==K_OPTIONS||(LA187_0 >= K_PASSWORD && LA187_0 <= K_PERMISSIONS)||LA187_0==K_RETURNS||(LA187_0 >= K_ROLE && LA187_0 <= K_ROLES)||(LA187_0 >= K_SFUNC && LA187_0 <= K_TINYINT)||(LA187_0 >= K_TOKEN && LA187_0 <= K_TRIGGER)||(LA187_0 >= K_TTL && LA187_0 <= K_TYPE)||(LA187_0 >= K_USER && LA187_0 <= K_USERS)||(LA187_0 >= K_UUID && LA187_0 <= K_VARINT)||LA187_0==K_WRITETIME||(LA187_0 >= QMARK && LA187_0 <= QUOTED_NAME)||LA187_0==STRING_LITERAL||LA187_0==UUID||LA187_0==171||LA187_0==175||LA187_0==177||LA187_0==184||LA187_0==188) ) {
				alt187=1;
			}
			switch (alt187) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1473:13: t1= term ( ',' ti= term )*
					{
					pushFollow(FOLLOW_term_in_singleColumnInValues10417);
					t1=term();
					state._fsp--;

					 terms.add(t1); 
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1473:43: ( ',' ti= term )*
					loop186:
					while (true) {
						int alt186=2;
						int LA186_0 = input.LA(1);
						if ( (LA186_0==174) ) {
							alt186=1;
						}

						switch (alt186) {
						case 1 :
							// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1473:44: ',' ti= term
							{
							match(input,174,FOLLOW_174_in_singleColumnInValues10422); 
							pushFollow(FOLLOW_term_in_singleColumnInValues10426);
							ti=term();
							state._fsp--;

							 terms.add(ti); 
							}
							break;

						default :
							break loop186;
						}
					}

					}
					break;

			}

			match(input,172,FOLLOW_172_in_singleColumnInValues10435); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return terms;
	}
	// $ANTLR end "singleColumnInValues"



	// $ANTLR start "tupleOfTupleLiterals"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1476:1: tupleOfTupleLiterals returns [List<Tuples.Literal> literals] : '(' t1= tupleLiteral ( ',' ti= tupleLiteral )* ')' ;
	public final List<Tuples.Literal> tupleOfTupleLiterals() throws RecognitionException {
		List<Tuples.Literal> literals = null;


		Tuples.Literal t1 =null;
		Tuples.Literal ti =null;

		 literals = new ArrayList<>(); 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1478:5: ( '(' t1= tupleLiteral ( ',' ti= tupleLiteral )* ')' )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1478:7: '(' t1= tupleLiteral ( ',' ti= tupleLiteral )* ')'
			{
			match(input,171,FOLLOW_171_in_tupleOfTupleLiterals10465); 
			pushFollow(FOLLOW_tupleLiteral_in_tupleOfTupleLiterals10469);
			t1=tupleLiteral();
			state._fsp--;

			 literals.add(t1); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1478:50: ( ',' ti= tupleLiteral )*
			loop188:
			while (true) {
				int alt188=2;
				int LA188_0 = input.LA(1);
				if ( (LA188_0==174) ) {
					alt188=1;
				}

				switch (alt188) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1478:51: ',' ti= tupleLiteral
					{
					match(input,174,FOLLOW_174_in_tupleOfTupleLiterals10474); 
					pushFollow(FOLLOW_tupleLiteral_in_tupleOfTupleLiterals10478);
					ti=tupleLiteral();
					state._fsp--;

					 literals.add(ti); 
					}
					break;

				default :
					break loop188;
				}
			}

			match(input,172,FOLLOW_172_in_tupleOfTupleLiterals10484); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return literals;
	}
	// $ANTLR end "tupleOfTupleLiterals"



	// $ANTLR start "markerForTuple"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1481:1: markerForTuple returns [Tuples.Raw marker] : ( QMARK | ':' name= noncol_ident );
	public final Tuples.Raw markerForTuple() throws RecognitionException {
		Tuples.Raw marker = null;


		ColumnIdentifier name =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1482:5: ( QMARK | ':' name= noncol_ident )
			int alt189=2;
			int LA189_0 = input.LA(1);
			if ( (LA189_0==QMARK) ) {
				alt189=1;
			}
			else if ( (LA189_0==177) ) {
				alt189=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 189, 0, input);
				throw nvae;
			}

			switch (alt189) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1482:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_markerForTuple10505); 
					 marker = newTupleBindVariables(null); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1483:7: ':' name= noncol_ident
					{
					match(input,177,FOLLOW_177_in_markerForTuple10515); 
					pushFollow(FOLLOW_noncol_ident_in_markerForTuple10519);
					name=noncol_ident();
					state._fsp--;

					 marker = newTupleBindVariables(name); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return marker;
	}
	// $ANTLR end "markerForTuple"



	// $ANTLR start "tupleOfMarkersForTuples"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1486:1: tupleOfMarkersForTuples returns [List<Tuples.Raw> markers] : '(' m1= markerForTuple ( ',' mi= markerForTuple )* ')' ;
	public final List<Tuples.Raw> tupleOfMarkersForTuples() throws RecognitionException {
		List<Tuples.Raw> markers = null;


		Tuples.Raw m1 =null;
		Tuples.Raw mi =null;

		 markers = new ArrayList<Tuples.Raw>(); 
		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1488:5: ( '(' m1= markerForTuple ( ',' mi= markerForTuple )* ')' )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1488:7: '(' m1= markerForTuple ( ',' mi= markerForTuple )* ')'
			{
			match(input,171,FOLLOW_171_in_tupleOfMarkersForTuples10551); 
			pushFollow(FOLLOW_markerForTuple_in_tupleOfMarkersForTuples10555);
			m1=markerForTuple();
			state._fsp--;

			 markers.add(m1); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1488:51: ( ',' mi= markerForTuple )*
			loop190:
			while (true) {
				int alt190=2;
				int LA190_0 = input.LA(1);
				if ( (LA190_0==174) ) {
					alt190=1;
				}

				switch (alt190) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1488:52: ',' mi= markerForTuple
					{
					match(input,174,FOLLOW_174_in_tupleOfMarkersForTuples10560); 
					pushFollow(FOLLOW_markerForTuple_in_tupleOfMarkersForTuples10564);
					mi=markerForTuple();
					state._fsp--;

					 markers.add(mi); 
					}
					break;

				default :
					break loop190;
				}
			}

			match(input,172,FOLLOW_172_in_tupleOfMarkersForTuples10570); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return markers;
	}
	// $ANTLR end "tupleOfMarkersForTuples"



	// $ANTLR start "inMarkerForTuple"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1491:1: inMarkerForTuple returns [Tuples.INRaw marker] : ( QMARK | ':' name= noncol_ident );
	public final Tuples.INRaw inMarkerForTuple() throws RecognitionException {
		Tuples.INRaw marker = null;


		ColumnIdentifier name =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1492:5: ( QMARK | ':' name= noncol_ident )
			int alt191=2;
			int LA191_0 = input.LA(1);
			if ( (LA191_0==QMARK) ) {
				alt191=1;
			}
			else if ( (LA191_0==177) ) {
				alt191=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 191, 0, input);
				throw nvae;
			}

			switch (alt191) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1492:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_inMarkerForTuple10591); 
					 marker = newTupleINBindVariables(null); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1493:7: ':' name= noncol_ident
					{
					match(input,177,FOLLOW_177_in_inMarkerForTuple10601); 
					pushFollow(FOLLOW_noncol_ident_in_inMarkerForTuple10605);
					name=noncol_ident();
					state._fsp--;

					 marker = newTupleINBindVariables(name); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return marker;
	}
	// $ANTLR end "inMarkerForTuple"



	// $ANTLR start "comparatorType"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1496:1: comparatorType returns [CQL3Type.Raw t] : (n= native_type |c= collection_type |tt= tuple_type |id= userTypeName | K_FROZEN '<' f= comparatorType '>' |s= STRING_LITERAL );
	public final CQL3Type.Raw comparatorType() throws RecognitionException {
		CQL3Type.Raw t = null;


		Token s=null;
		CQL3Type n =null;
		CQL3Type.Raw c =null;
		CQL3Type.Raw tt =null;
		UTName id =null;
		CQL3Type.Raw f =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1497:5: (n= native_type |c= collection_type |tt= tuple_type |id= userTypeName | K_FROZEN '<' f= comparatorType '>' |s= STRING_LITERAL )
			int alt192=6;
			alt192 = dfa192.predict(input);
			switch (alt192) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1497:7: n= native_type
					{
					pushFollow(FOLLOW_native_type_in_comparatorType10630);
					n=native_type();
					state._fsp--;

					 t = CQL3Type.Raw.from(n); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1498:7: c= collection_type
					{
					pushFollow(FOLLOW_collection_type_in_comparatorType10646);
					c=collection_type();
					state._fsp--;

					 t = c; 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1499:7: tt= tuple_type
					{
					pushFollow(FOLLOW_tuple_type_in_comparatorType10658);
					tt=tuple_type();
					state._fsp--;

					 t = tt; 
					}
					break;
				case 4 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1500:7: id= userTypeName
					{
					pushFollow(FOLLOW_userTypeName_in_comparatorType10674);
					id=userTypeName();
					state._fsp--;

					 t = CQL3Type.Raw.userType(id); 
					}
					break;
				case 5 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1501:7: K_FROZEN '<' f= comparatorType '>'
					{
					match(input,K_FROZEN,FOLLOW_K_FROZEN_in_comparatorType10686); 
					match(input,179,FOLLOW_179_in_comparatorType10688); 
					pushFollow(FOLLOW_comparatorType_in_comparatorType10692);
					f=comparatorType();
					state._fsp--;

					match(input,182,FOLLOW_182_in_comparatorType10694); 

					        try {
					            t = CQL3Type.Raw.frozen(f);
					        } catch (InvalidRequestException e) {
					            addRecognitionError(e.getMessage());
					        }
					      
					}
					break;
				case 6 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1509:7: s= STRING_LITERAL
					{
					s=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_comparatorType10712); 

					        try {
					            t = CQL3Type.Raw.from(new CQL3Type.Custom((s!=null?s.getText():null)));
					        } catch (SyntaxException e) {
					            addRecognitionError("Cannot parse type " + (s!=null?s.getText():null) + ": " + e.getMessage());
					        } catch (ConfigurationException e) {
					            addRecognitionError("Error setting type " + (s!=null?s.getText():null) + ": " + e.getMessage());
					        }
					      
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return t;
	}
	// $ANTLR end "comparatorType"



	// $ANTLR start "native_type"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1521:1: native_type returns [CQL3Type t] : ( K_ASCII | K_BIGINT | K_BLOB | K_BOOLEAN | K_COUNTER | K_DECIMAL | K_DOUBLE | K_FLOAT | K_INET | K_INT | K_SMALLINT | K_TEXT | K_TIMESTAMP | K_TINYINT | K_UUID | K_VARCHAR | K_VARINT | K_TIMEUUID | K_DATE | K_TIME );
	public final CQL3Type native_type() throws RecognitionException {
		CQL3Type t = null;


		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1522:5: ( K_ASCII | K_BIGINT | K_BLOB | K_BOOLEAN | K_COUNTER | K_DECIMAL | K_DOUBLE | K_FLOAT | K_INET | K_INT | K_SMALLINT | K_TEXT | K_TIMESTAMP | K_TINYINT | K_UUID | K_VARCHAR | K_VARINT | K_TIMEUUID | K_DATE | K_TIME )
			int alt193=20;
			switch ( input.LA(1) ) {
			case K_ASCII:
				{
				alt193=1;
				}
				break;
			case K_BIGINT:
				{
				alt193=2;
				}
				break;
			case K_BLOB:
				{
				alt193=3;
				}
				break;
			case K_BOOLEAN:
				{
				alt193=4;
				}
				break;
			case K_COUNTER:
				{
				alt193=5;
				}
				break;
			case K_DECIMAL:
				{
				alt193=6;
				}
				break;
			case K_DOUBLE:
				{
				alt193=7;
				}
				break;
			case K_FLOAT:
				{
				alt193=8;
				}
				break;
			case K_INET:
				{
				alt193=9;
				}
				break;
			case K_INT:
				{
				alt193=10;
				}
				break;
			case K_SMALLINT:
				{
				alt193=11;
				}
				break;
			case K_TEXT:
				{
				alt193=12;
				}
				break;
			case K_TIMESTAMP:
				{
				alt193=13;
				}
				break;
			case K_TINYINT:
				{
				alt193=14;
				}
				break;
			case K_UUID:
				{
				alt193=15;
				}
				break;
			case K_VARCHAR:
				{
				alt193=16;
				}
				break;
			case K_VARINT:
				{
				alt193=17;
				}
				break;
			case K_TIMEUUID:
				{
				alt193=18;
				}
				break;
			case K_DATE:
				{
				alt193=19;
				}
				break;
			case K_TIME:
				{
				alt193=20;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 193, 0, input);
				throw nvae;
			}
			switch (alt193) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1522:7: K_ASCII
					{
					match(input,K_ASCII,FOLLOW_K_ASCII_in_native_type10741); 
					 t = CQL3Type.Native.ASCII; 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1523:7: K_BIGINT
					{
					match(input,K_BIGINT,FOLLOW_K_BIGINT_in_native_type10755); 
					 t = CQL3Type.Native.BIGINT; 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1524:7: K_BLOB
					{
					match(input,K_BLOB,FOLLOW_K_BLOB_in_native_type10768); 
					 t = CQL3Type.Native.BLOB; 
					}
					break;
				case 4 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1525:7: K_BOOLEAN
					{
					match(input,K_BOOLEAN,FOLLOW_K_BOOLEAN_in_native_type10783); 
					 t = CQL3Type.Native.BOOLEAN; 
					}
					break;
				case 5 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1526:7: K_COUNTER
					{
					match(input,K_COUNTER,FOLLOW_K_COUNTER_in_native_type10795); 
					 t = CQL3Type.Native.COUNTER; 
					}
					break;
				case 6 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1527:7: K_DECIMAL
					{
					match(input,K_DECIMAL,FOLLOW_K_DECIMAL_in_native_type10807); 
					 t = CQL3Type.Native.DECIMAL; 
					}
					break;
				case 7 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1528:7: K_DOUBLE
					{
					match(input,K_DOUBLE,FOLLOW_K_DOUBLE_in_native_type10819); 
					 t = CQL3Type.Native.DOUBLE; 
					}
					break;
				case 8 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1529:7: K_FLOAT
					{
					match(input,K_FLOAT,FOLLOW_K_FLOAT_in_native_type10832); 
					 t = CQL3Type.Native.FLOAT; 
					}
					break;
				case 9 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1530:7: K_INET
					{
					match(input,K_INET,FOLLOW_K_INET_in_native_type10846); 
					 t = CQL3Type.Native.INET;
					}
					break;
				case 10 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1531:7: K_INT
					{
					match(input,K_INT,FOLLOW_K_INT_in_native_type10861); 
					 t = CQL3Type.Native.INT; 
					}
					break;
				case 11 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1532:7: K_SMALLINT
					{
					match(input,K_SMALLINT,FOLLOW_K_SMALLINT_in_native_type10877); 
					 t = CQL3Type.Native.SMALLINT; 
					}
					break;
				case 12 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1533:7: K_TEXT
					{
					match(input,K_TEXT,FOLLOW_K_TEXT_in_native_type10888); 
					 t = CQL3Type.Native.TEXT; 
					}
					break;
				case 13 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1534:7: K_TIMESTAMP
					{
					match(input,K_TIMESTAMP,FOLLOW_K_TIMESTAMP_in_native_type10903); 
					 t = CQL3Type.Native.TIMESTAMP; 
					}
					break;
				case 14 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1535:7: K_TINYINT
					{
					match(input,K_TINYINT,FOLLOW_K_TINYINT_in_native_type10913); 
					 t = CQL3Type.Native.TINYINT; 
					}
					break;
				case 15 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1536:7: K_UUID
					{
					match(input,K_UUID,FOLLOW_K_UUID_in_native_type10925); 
					 t = CQL3Type.Native.UUID; 
					}
					break;
				case 16 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1537:7: K_VARCHAR
					{
					match(input,K_VARCHAR,FOLLOW_K_VARCHAR_in_native_type10940); 
					 t = CQL3Type.Native.VARCHAR; 
					}
					break;
				case 17 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1538:7: K_VARINT
					{
					match(input,K_VARINT,FOLLOW_K_VARINT_in_native_type10952); 
					 t = CQL3Type.Native.VARINT; 
					}
					break;
				case 18 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1539:7: K_TIMEUUID
					{
					match(input,K_TIMEUUID,FOLLOW_K_TIMEUUID_in_native_type10965); 
					 t = CQL3Type.Native.TIMEUUID; 
					}
					break;
				case 19 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1540:7: K_DATE
					{
					match(input,K_DATE,FOLLOW_K_DATE_in_native_type10976); 
					 t = CQL3Type.Native.DATE; 
					}
					break;
				case 20 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1541:7: K_TIME
					{
					match(input,K_TIME,FOLLOW_K_TIME_in_native_type10991); 
					 t = CQL3Type.Native.TIME; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return t;
	}
	// $ANTLR end "native_type"



	// $ANTLR start "collection_type"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1544:1: collection_type returns [CQL3Type.Raw pt] : ( K_MAP '<' t1= comparatorType ',' t2= comparatorType '>' | K_LIST '<' t= comparatorType '>' | K_SET '<' t= comparatorType '>' );
	public final CQL3Type.Raw collection_type() throws RecognitionException {
		CQL3Type.Raw pt = null;


		CQL3Type.Raw t1 =null;
		CQL3Type.Raw t2 =null;
		CQL3Type.Raw t =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1545:5: ( K_MAP '<' t1= comparatorType ',' t2= comparatorType '>' | K_LIST '<' t= comparatorType '>' | K_SET '<' t= comparatorType '>' )
			int alt194=3;
			switch ( input.LA(1) ) {
			case K_MAP:
				{
				alt194=1;
				}
				break;
			case K_LIST:
				{
				alt194=2;
				}
				break;
			case K_SET:
				{
				alt194=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 194, 0, input);
				throw nvae;
			}
			switch (alt194) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1545:7: K_MAP '<' t1= comparatorType ',' t2= comparatorType '>'
					{
					match(input,K_MAP,FOLLOW_K_MAP_in_collection_type11019); 
					match(input,179,FOLLOW_179_in_collection_type11022); 
					pushFollow(FOLLOW_comparatorType_in_collection_type11026);
					t1=comparatorType();
					state._fsp--;

					match(input,174,FOLLOW_174_in_collection_type11028); 
					pushFollow(FOLLOW_comparatorType_in_collection_type11032);
					t2=comparatorType();
					state._fsp--;

					match(input,182,FOLLOW_182_in_collection_type11034); 

					            // if we can't parse either t1 or t2, antlr will "recover" and we may have t1 or t2 null.
					            if (t1 != null && t2 != null)
					                pt = CQL3Type.Raw.map(t1, t2);
					        
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1551:7: K_LIST '<' t= comparatorType '>'
					{
					match(input,K_LIST,FOLLOW_K_LIST_in_collection_type11052); 
					match(input,179,FOLLOW_179_in_collection_type11054); 
					pushFollow(FOLLOW_comparatorType_in_collection_type11058);
					t=comparatorType();
					state._fsp--;

					match(input,182,FOLLOW_182_in_collection_type11060); 
					 if (t != null) pt = CQL3Type.Raw.list(t); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1553:7: K_SET '<' t= comparatorType '>'
					{
					match(input,K_SET,FOLLOW_K_SET_in_collection_type11078); 
					match(input,179,FOLLOW_179_in_collection_type11081); 
					pushFollow(FOLLOW_comparatorType_in_collection_type11085);
					t=comparatorType();
					state._fsp--;

					match(input,182,FOLLOW_182_in_collection_type11087); 
					 if (t != null) pt = CQL3Type.Raw.set(t); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return pt;
	}
	// $ANTLR end "collection_type"



	// $ANTLR start "tuple_type"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1557:1: tuple_type returns [CQL3Type.Raw t] : K_TUPLE '<' t1= comparatorType ( ',' tn= comparatorType )* '>' ;
	public final CQL3Type.Raw tuple_type() throws RecognitionException {
		CQL3Type.Raw t = null;


		CQL3Type.Raw t1 =null;
		CQL3Type.Raw tn =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1558:5: ( K_TUPLE '<' t1= comparatorType ( ',' tn= comparatorType )* '>' )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1558:7: K_TUPLE '<' t1= comparatorType ( ',' tn= comparatorType )* '>'
			{
			match(input,K_TUPLE,FOLLOW_K_TUPLE_in_tuple_type11118); 
			match(input,179,FOLLOW_179_in_tuple_type11120); 
			 List<CQL3Type.Raw> types = new ArrayList<>(); 
			pushFollow(FOLLOW_comparatorType_in_tuple_type11135);
			t1=comparatorType();
			state._fsp--;

			 types.add(t1); 
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1559:47: ( ',' tn= comparatorType )*
			loop195:
			while (true) {
				int alt195=2;
				int LA195_0 = input.LA(1);
				if ( (LA195_0==174) ) {
					alt195=1;
				}

				switch (alt195) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1559:48: ',' tn= comparatorType
					{
					match(input,174,FOLLOW_174_in_tuple_type11140); 
					pushFollow(FOLLOW_comparatorType_in_tuple_type11144);
					tn=comparatorType();
					state._fsp--;

					 types.add(tn); 
					}
					break;

				default :
					break loop195;
				}
			}

			match(input,182,FOLLOW_182_in_tuple_type11156); 
			 t = CQL3Type.Raw.tuple(types); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return t;
	}
	// $ANTLR end "tuple_type"


	public static class username_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "username"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1563:1: username : ( IDENT | STRING_LITERAL | QUOTED_NAME );
	public final CqlParser.username_return username() throws RecognitionException {
		CqlParser.username_return retval = new CqlParser.username_return();
		retval.start = input.LT(1);

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1564:5: ( IDENT | STRING_LITERAL | QUOTED_NAME )
			int alt196=3;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				alt196=1;
				}
				break;
			case STRING_LITERAL:
				{
				alt196=2;
				}
				break;
			case QUOTED_NAME:
				{
				alt196=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 196, 0, input);
				throw nvae;
			}
			switch (alt196) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1564:7: IDENT
					{
					match(input,IDENT,FOLLOW_IDENT_in_username11175); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1565:7: STRING_LITERAL
					{
					match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_username11183); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1566:7: QUOTED_NAME
					{
					match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_username11191); 
					 addRecognitionError("Quoted strings are are not supported for user names and USER is deprecated, please use ROLE");
					}
					break;

			}
			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "username"



	// $ANTLR start "non_type_ident"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1571:1: non_type_ident returns [ColumnIdentifier id] : (t= IDENT |t= QUOTED_NAME |k= basic_unreserved_keyword |kk= K_KEY );
	public final ColumnIdentifier non_type_ident() throws RecognitionException {
		ColumnIdentifier id = null;


		Token t=null;
		Token kk=null;
		String k =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1572:5: (t= IDENT |t= QUOTED_NAME |k= basic_unreserved_keyword |kk= K_KEY )
			int alt197=4;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				alt197=1;
				}
				break;
			case QUOTED_NAME:
				{
				alt197=2;
				}
				break;
			case K_AGGREGATE:
			case K_ALL:
			case K_AS:
			case K_CALLED:
			case K_CLUSTERING:
			case K_COMPACT:
			case K_CONTAINS:
			case K_CUSTOM:
			case K_DISTINCT:
			case K_EXISTS:
			case K_FILTERING:
			case K_FINALFUNC:
			case K_FROZEN:
			case K_FUNCTION:
			case K_FUNCTIONS:
			case K_INITCOND:
			case K_INPUT:
			case K_JSON:
			case K_KEYS:
			case K_KEYSPACES:
			case K_LANGUAGE:
			case K_LIST:
			case K_LOGIN:
			case K_MAP:
			case K_NOLOGIN:
			case K_NOSUPERUSER:
			case K_OPTIONS:
			case K_PASSWORD:
			case K_PERMISSION:
			case K_PERMISSIONS:
			case K_RETURNS:
			case K_ROLE:
			case K_ROLES:
			case K_SFUNC:
			case K_STATIC:
			case K_STORAGE:
			case K_STYPE:
			case K_SUPERUSER:
			case K_TRIGGER:
			case K_TUPLE:
			case K_TYPE:
			case K_USER:
			case K_USERS:
			case K_VALUES:
				{
				alt197=3;
				}
				break;
			case K_KEY:
				{
				alt197=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 197, 0, input);
				throw nvae;
			}
			switch (alt197) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1572:7: t= IDENT
					{
					t=(Token)match(input,IDENT,FOLLOW_IDENT_in_non_type_ident11218); 
					 if (reservedTypeNames.contains((t!=null?t.getText():null))) addRecognitionError("Invalid (reserved) user type name " + (t!=null?t.getText():null)); id = new ColumnIdentifier((t!=null?t.getText():null), false); 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1573:7: t= QUOTED_NAME
					{
					t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_non_type_ident11249); 
					 id = new ColumnIdentifier((t!=null?t.getText():null), true); 
					}
					break;
				case 3 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1574:7: k= basic_unreserved_keyword
					{
					pushFollow(FOLLOW_basic_unreserved_keyword_in_non_type_ident11274);
					k=basic_unreserved_keyword();
					state._fsp--;

					 id = new ColumnIdentifier(k, false); 
					}
					break;
				case 4 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1575:7: kk= K_KEY
					{
					kk=(Token)match(input,K_KEY,FOLLOW_K_KEY_in_non_type_ident11286); 
					 id = new ColumnIdentifier((kk!=null?kk.getText():null), false); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return id;
	}
	// $ANTLR end "non_type_ident"



	// $ANTLR start "unreserved_keyword"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1578:1: unreserved_keyword returns [String str] : (u= unreserved_function_keyword |k= ( K_TTL | K_COUNT | K_WRITETIME | K_KEY ) );
	public final String unreserved_keyword() throws RecognitionException {
		String str = null;


		Token k=null;
		String u =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1579:5: (u= unreserved_function_keyword |k= ( K_TTL | K_COUNT | K_WRITETIME | K_KEY ) )
			int alt198=2;
			int LA198_0 = input.LA(1);
			if ( ((LA198_0 >= K_AGGREGATE && LA198_0 <= K_ALL)||LA198_0==K_AS||LA198_0==K_ASCII||(LA198_0 >= K_BIGINT && LA198_0 <= K_BOOLEAN)||(LA198_0 >= K_CALLED && LA198_0 <= K_CLUSTERING)||(LA198_0 >= K_COMPACT && LA198_0 <= K_CONTAINS)||LA198_0==K_COUNTER||(LA198_0 >= K_CUSTOM && LA198_0 <= K_DECIMAL)||(LA198_0 >= K_DISTINCT && LA198_0 <= K_DOUBLE)||(LA198_0 >= K_EXISTS && LA198_0 <= K_FLOAT)||LA198_0==K_FROZEN||(LA198_0 >= K_FUNCTION && LA198_0 <= K_FUNCTIONS)||LA198_0==K_INET||(LA198_0 >= K_INITCOND && LA198_0 <= K_INPUT)||LA198_0==K_INT||LA198_0==K_JSON||LA198_0==K_KEYS||(LA198_0 >= K_KEYSPACES && LA198_0 <= K_LANGUAGE)||(LA198_0 >= K_LIST && LA198_0 <= K_MAP)||LA198_0==K_NOLOGIN||LA198_0==K_NOSUPERUSER||LA198_0==K_OPTIONS||(LA198_0 >= K_PASSWORD && LA198_0 <= K_PERMISSIONS)||LA198_0==K_RETURNS||(LA198_0 >= K_ROLE && LA198_0 <= K_ROLES)||(LA198_0 >= K_SFUNC && LA198_0 <= K_TINYINT)||LA198_0==K_TRIGGER||(LA198_0 >= K_TUPLE && LA198_0 <= K_TYPE)||(LA198_0 >= K_USER && LA198_0 <= K_USERS)||(LA198_0 >= K_UUID && LA198_0 <= K_VARINT)) ) {
				alt198=1;
			}
			else if ( (LA198_0==K_COUNT||LA198_0==K_KEY||LA198_0==K_TTL||LA198_0==K_WRITETIME) ) {
				alt198=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 198, 0, input);
				throw nvae;
			}

			switch (alt198) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1579:7: u= unreserved_function_keyword
					{
					pushFollow(FOLLOW_unreserved_function_keyword_in_unreserved_keyword11329);
					u=unreserved_function_keyword();
					state._fsp--;

					 str = u; 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1580:7: k= ( K_TTL | K_COUNT | K_WRITETIME | K_KEY )
					{
					k=input.LT(1);
					if ( input.LA(1)==K_COUNT||input.LA(1)==K_KEY||input.LA(1)==K_TTL||input.LA(1)==K_WRITETIME ) {
						input.consume();
						state.errorRecovery=false;
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					 str = (k!=null?k.getText():null); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return str;
	}
	// $ANTLR end "unreserved_keyword"



	// $ANTLR start "unreserved_function_keyword"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1583:1: unreserved_function_keyword returns [String str] : (u= basic_unreserved_keyword |t= native_type );
	public final String unreserved_function_keyword() throws RecognitionException {
		String str = null;


		String u =null;
		CQL3Type t =null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1584:5: (u= basic_unreserved_keyword |t= native_type )
			int alt199=2;
			int LA199_0 = input.LA(1);
			if ( ((LA199_0 >= K_AGGREGATE && LA199_0 <= K_ALL)||LA199_0==K_AS||(LA199_0 >= K_CALLED && LA199_0 <= K_CLUSTERING)||(LA199_0 >= K_COMPACT && LA199_0 <= K_CONTAINS)||LA199_0==K_CUSTOM||LA199_0==K_DISTINCT||(LA199_0 >= K_EXISTS && LA199_0 <= K_FINALFUNC)||LA199_0==K_FROZEN||(LA199_0 >= K_FUNCTION && LA199_0 <= K_FUNCTIONS)||(LA199_0 >= K_INITCOND && LA199_0 <= K_INPUT)||LA199_0==K_JSON||LA199_0==K_KEYS||(LA199_0 >= K_KEYSPACES && LA199_0 <= K_LANGUAGE)||(LA199_0 >= K_LIST && LA199_0 <= K_MAP)||LA199_0==K_NOLOGIN||LA199_0==K_NOSUPERUSER||LA199_0==K_OPTIONS||(LA199_0 >= K_PASSWORD && LA199_0 <= K_PERMISSIONS)||LA199_0==K_RETURNS||(LA199_0 >= K_ROLE && LA199_0 <= K_ROLES)||LA199_0==K_SFUNC||(LA199_0 >= K_STATIC && LA199_0 <= K_SUPERUSER)||LA199_0==K_TRIGGER||(LA199_0 >= K_TUPLE && LA199_0 <= K_TYPE)||(LA199_0 >= K_USER && LA199_0 <= K_USERS)||LA199_0==K_VALUES) ) {
				alt199=1;
			}
			else if ( (LA199_0==K_ASCII||(LA199_0 >= K_BIGINT && LA199_0 <= K_BOOLEAN)||LA199_0==K_COUNTER||(LA199_0 >= K_DATE && LA199_0 <= K_DECIMAL)||LA199_0==K_DOUBLE||LA199_0==K_FLOAT||LA199_0==K_INET||LA199_0==K_INT||LA199_0==K_SMALLINT||(LA199_0 >= K_TEXT && LA199_0 <= K_TINYINT)||LA199_0==K_UUID||(LA199_0 >= K_VARCHAR && LA199_0 <= K_VARINT)) ) {
				alt199=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 199, 0, input);
				throw nvae;
			}

			switch (alt199) {
				case 1 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1584:7: u= basic_unreserved_keyword
					{
					pushFollow(FOLLOW_basic_unreserved_keyword_in_unreserved_function_keyword11384);
					u=basic_unreserved_keyword();
					state._fsp--;

					 str = u; 
					}
					break;
				case 2 :
					// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1585:7: t= native_type
					{
					pushFollow(FOLLOW_native_type_in_unreserved_function_keyword11396);
					t=native_type();
					state._fsp--;

					 str = t.toString(); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return str;
	}
	// $ANTLR end "unreserved_function_keyword"



	// $ANTLR start "basic_unreserved_keyword"
	// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1588:1: basic_unreserved_keyword returns [String str] : k= ( K_KEYS | K_AS | K_CLUSTERING | K_COMPACT | K_STORAGE | K_TYPE | K_VALUES | K_MAP | K_LIST | K_FILTERING | K_PERMISSION | K_PERMISSIONS | K_KEYSPACES | K_ALL | K_USER | K_USERS | K_ROLE | K_ROLES | K_SUPERUSER | K_NOSUPERUSER | K_LOGIN | K_NOLOGIN | K_OPTIONS | K_PASSWORD | K_EXISTS | K_CUSTOM | K_TRIGGER | K_DISTINCT | K_CONTAINS | K_STATIC | K_FROZEN | K_TUPLE | K_FUNCTION | K_FUNCTIONS | K_AGGREGATE | K_SFUNC | K_STYPE | K_FINALFUNC | K_INITCOND | K_RETURNS | K_LANGUAGE | K_JSON | K_CALLED | K_INPUT ) ;
	public final String basic_unreserved_keyword() throws RecognitionException {
		String str = null;


		Token k=null;

		try {
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1589:5: (k= ( K_KEYS | K_AS | K_CLUSTERING | K_COMPACT | K_STORAGE | K_TYPE | K_VALUES | K_MAP | K_LIST | K_FILTERING | K_PERMISSION | K_PERMISSIONS | K_KEYSPACES | K_ALL | K_USER | K_USERS | K_ROLE | K_ROLES | K_SUPERUSER | K_NOSUPERUSER | K_LOGIN | K_NOLOGIN | K_OPTIONS | K_PASSWORD | K_EXISTS | K_CUSTOM | K_TRIGGER | K_DISTINCT | K_CONTAINS | K_STATIC | K_FROZEN | K_TUPLE | K_FUNCTION | K_FUNCTIONS | K_AGGREGATE | K_SFUNC | K_STYPE | K_FINALFUNC | K_INITCOND | K_RETURNS | K_LANGUAGE | K_JSON | K_CALLED | K_INPUT ) )
			// /Users/vroyer/Dev/git/work/elassandra3.0.10-2.4.2-2/core/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1589:7: k= ( K_KEYS | K_AS | K_CLUSTERING | K_COMPACT | K_STORAGE | K_TYPE | K_VALUES | K_MAP | K_LIST | K_FILTERING | K_PERMISSION | K_PERMISSIONS | K_KEYSPACES | K_ALL | K_USER | K_USERS | K_ROLE | K_ROLES | K_SUPERUSER | K_NOSUPERUSER | K_LOGIN | K_NOLOGIN | K_OPTIONS | K_PASSWORD | K_EXISTS | K_CUSTOM | K_TRIGGER | K_DISTINCT | K_CONTAINS | K_STATIC | K_FROZEN | K_TUPLE | K_FUNCTION | K_FUNCTIONS | K_AGGREGATE | K_SFUNC | K_STYPE | K_FINALFUNC | K_INITCOND | K_RETURNS | K_LANGUAGE | K_JSON | K_CALLED | K_INPUT )
			{
			k=input.LT(1);
			if ( (input.LA(1) >= K_AGGREGATE && input.LA(1) <= K_ALL)||input.LA(1)==K_AS||(input.LA(1) >= K_CALLED && input.LA(1) <= K_CLUSTERING)||(input.LA(1) >= K_COMPACT && input.LA(1) <= K_CONTAINS)||input.LA(1)==K_CUSTOM||input.LA(1)==K_DISTINCT||(input.LA(1) >= K_EXISTS && input.LA(1) <= K_FINALFUNC)||input.LA(1)==K_FROZEN||(input.LA(1) >= K_FUNCTION && input.LA(1) <= K_FUNCTIONS)||(input.LA(1) >= K_INITCOND && input.LA(1) <= K_INPUT)||input.LA(1)==K_JSON||input.LA(1)==K_KEYS||(input.LA(1) >= K_KEYSPACES && input.LA(1) <= K_LANGUAGE)||(input.LA(1) >= K_LIST && input.LA(1) <= K_MAP)||input.LA(1)==K_NOLOGIN||input.LA(1)==K_NOSUPERUSER||input.LA(1)==K_OPTIONS||(input.LA(1) >= K_PASSWORD && input.LA(1) <= K_PERMISSIONS)||input.LA(1)==K_RETURNS||(input.LA(1) >= K_ROLE && input.LA(1) <= K_ROLES)||input.LA(1)==K_SFUNC||(input.LA(1) >= K_STATIC && input.LA(1) <= K_SUPERUSER)||input.LA(1)==K_TRIGGER||(input.LA(1) >= K_TUPLE && input.LA(1) <= K_TYPE)||(input.LA(1) >= K_USER && input.LA(1) <= K_USERS)||input.LA(1)==K_VALUES ) {
				input.consume();
				state.errorRecovery=false;
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			 str = (k!=null?k.getText():null); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return str;
	}
	// $ANTLR end "basic_unreserved_keyword"

	// Delegated rules


	protected DFA2 dfa2 = new DFA2(this);
	protected DFA13 dfa13 = new DFA13(this);
	protected DFA41 dfa41 = new DFA41(this);
	protected DFA104 dfa104 = new DFA104(this);
	protected DFA143 dfa143 = new DFA143(this);
	protected DFA144 dfa144 = new DFA144(this);
	protected DFA162 dfa162 = new DFA162(this);
	protected DFA164 dfa164 = new DFA164(this);
	protected DFA166 dfa166 = new DFA166(this);
	protected DFA168 dfa168 = new DFA168(this);
	protected DFA171 dfa171 = new DFA171(this);
	protected DFA177 dfa177 = new DFA177(this);
	protected DFA183 dfa183 = new DFA183(this);
	protected DFA182 dfa182 = new DFA182(this);
	protected DFA192 dfa192 = new DFA192(this);
	static final String DFA2_eotS =
		"\63\uffff";
	static final String DFA2_eofS =
		"\63\uffff";
	static final String DFA2_minS =
		"\1\34\7\uffff\2\31\1\53\2\24\1\32\10\uffff\1\155\22\uffff\1\144\2\uffff"+
		"\1\100\5\uffff\1\31";
	static final String DFA2_maxS =
		"\1\u0088\7\uffff\3\u0089\2\u00a0\1\u008a\10\uffff\1\155\22\uffff\1\177"+
		"\2\uffff\1\152\5\uffff\1\103";
	static final String DFA2_acceptS =
		"\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\6\uffff\1\10\1\11\1\23\1\27\1\31"+
		"\1\40\1\46\1\12\1\uffff\1\34\1\36\1\13\1\14\1\15\1\25\1\30\1\33\1\35\1"+
		"\37\1\42\1\47\1\16\1\17\1\24\1\32\1\41\1\50\1\uffff\1\20\1\44\1\uffff"+
		"\1\21\1\45\1\26\1\43\1\22\1\uffff";
	static final String DFA2_specialS =
		"\63\uffff}>";
	static final String[] DFA2_transitionS = {
			"\1\12\7\uffff\1\4\13\uffff\1\10\3\uffff\1\5\4\uffff\1\11\13\uffff\1\13"+
			"\7\uffff\1\2\12\uffff\1\15\26\uffff\1\14\2\uffff\1\1\17\uffff\1\7\4\uffff"+
			"\1\3\1\6",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\30\21\uffff\1\17\5\uffff\1\25\21\uffff\1\27\4\uffff\1\25\13\uffff"+
			"\1\16\6\uffff\1\24\12\uffff\1\26\11\uffff\1\23\20\uffff\1\21\3\uffff"+
			"\1\22\3\uffff\1\20",
			"\1\40\21\uffff\1\32\27\uffff\1\37\4\uffff\1\33\13\uffff\1\31\6\uffff"+
			"\1\42\24\uffff\1\41\20\uffff\1\35\3\uffff\1\36\3\uffff\1\34",
			"\1\43\50\uffff\1\44\6\uffff\1\50\24\uffff\1\47\24\uffff\1\46\3\uffff"+
			"\1\45",
			"\1\53\4\uffff\1\53\1\51\1\uffff\1\52\2\uffff\1\53\1\uffff\1\53\1\52"+
			"\2\uffff\3\53\1\uffff\2\53\1\uffff\4\53\1\52\3\53\2\uffff\1\52\2\53\1"+
			"\52\1\uffff\1\52\4\53\1\uffff\1\53\1\uffff\2\53\4\uffff\1\53\1\uffff"+
			"\2\53\1\uffff\1\53\2\uffff\3\53\1\uffff\2\53\1\uffff\3\53\1\uffff\1\52"+
			"\1\uffff\1\53\1\uffff\1\53\4\uffff\1\53\2\uffff\3\53\3\uffff\1\53\1\uffff"+
			"\2\53\1\52\1\uffff\13\53\2\uffff\1\53\1\uffff\3\53\3\uffff\2\53\1\uffff"+
			"\4\53\3\uffff\1\53\10\uffff\2\53\2\uffff\1\53",
			"\1\56\4\uffff\1\56\1\54\1\uffff\1\55\2\uffff\1\56\1\uffff\1\56\1\55"+
			"\2\uffff\3\56\1\uffff\2\56\1\uffff\4\56\1\55\3\56\2\uffff\1\55\2\56\1"+
			"\55\1\uffff\1\55\4\56\1\uffff\1\56\1\uffff\2\56\4\uffff\1\56\1\uffff"+
			"\2\56\1\uffff\1\56\2\uffff\3\56\1\uffff\2\56\1\uffff\3\56\1\uffff\1\55"+
			"\1\uffff\1\56\1\uffff\1\56\4\uffff\1\56\2\uffff\3\56\3\uffff\1\56\1\uffff"+
			"\2\56\1\55\1\uffff\13\56\2\uffff\1\56\1\uffff\3\56\3\uffff\2\56\1\uffff"+
			"\4\56\3\uffff\1\56\10\uffff\2\56\2\uffff\1\56",
			"\1\61\1\uffff\1\61\5\uffff\1\61\15\uffff\1\61\5\uffff\1\61\2\uffff\1"+
			"\61\1\uffff\1\61\40\uffff\1\61\24\uffff\1\60\1\61\27\uffff\1\57",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\62",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\52\5\uffff\1\52\24\uffff\1\53",
			"",
			"",
			"\1\56\43\uffff\1\55\5\uffff\1\55",
			"",
			"",
			"",
			"",
			"",
			"\1\30\51\uffff\1\27"
	};

	static final short[] DFA2_eot = DFA.unpackEncodedString(DFA2_eotS);
	static final short[] DFA2_eof = DFA.unpackEncodedString(DFA2_eofS);
	static final char[] DFA2_min = DFA.unpackEncodedStringToUnsignedChars(DFA2_minS);
	static final char[] DFA2_max = DFA.unpackEncodedStringToUnsignedChars(DFA2_maxS);
	static final short[] DFA2_accept = DFA.unpackEncodedString(DFA2_acceptS);
	static final short[] DFA2_special = DFA.unpackEncodedString(DFA2_specialS);
	static final short[][] DFA2_transition;

	static {
		int numStates = DFA2_transitionS.length;
		DFA2_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA2_transition[i] = DFA.unpackEncodedString(DFA2_transitionS[i]);
		}
	}

	protected class DFA2 extends DFA {

		public DFA2(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 2;
			this.eot = DFA2_eot;
			this.eof = DFA2_eof;
			this.min = DFA2_min;
			this.max = DFA2_max;
			this.accept = DFA2_accept;
			this.special = DFA2_special;
			this.transition = DFA2_transition;
		}
		@Override
		public String getDescription() {
			return "235:1: cqlStatement returns [ParsedStatement stmt] : (st1= selectStatement |st2= insertStatement |st3= updateStatement |st4= batchStatement |st5= deleteStatement |st6= useStatement |st7= truncateStatement |st8= createKeyspaceStatement |st9= createTableStatement |st10= createIndexStatement |st11= dropKeyspaceStatement |st12= dropTableStatement |st13= dropIndexStatement |st14= alterTableStatement |st15= alterKeyspaceStatement |st16= grantPermissionsStatement |st17= revokePermissionsStatement |st18= listPermissionsStatement |st19= createUserStatement |st20= alterUserStatement |st21= dropUserStatement |st22= listUsersStatement |st23= createTriggerStatement |st24= dropTriggerStatement |st25= createTypeStatement |st26= alterTypeStatement |st27= dropTypeStatement |st28= createFunctionStatement |st29= dropFunctionStatement |st30= createAggregateStatement |st31= dropAggregateStatement |st32= createRoleStatement |st33= alterRoleStatement |st34= dropRoleStatement |st35= listRolesStatement |st36= grantRoleStatement |st37= revokeRoleStatement |st38= createMaterializedViewStatement |st39= dropMaterializedViewStatement |st40= alterMaterializedViewStatement );";
		}
	}

	static final String DFA13_eotS =
		"\73\uffff";
	static final String DFA13_eofS =
		"\73\uffff";
	static final String DFA13_minS =
		"\1\24\33\37\1\uffff\1\24\1\uffff\1\24\2\uffff\30\37\1\uffff";
	static final String DFA13_maxS =
		"\1\u009d\33\u00b0\1\uffff\1\u009d\1\uffff\1\u00b9\2\uffff\30\u00b0\1\uffff";
	static final String DFA13_acceptS =
		"\34\uffff\1\5\1\uffff\1\1\1\uffff\1\3\1\4\30\uffff\1\2";
	static final String DFA13_specialS =
		"\73\uffff}>";
	static final String[] DFA13_transitionS = {
			"\1\1\4\uffff\2\3\4\uffff\1\3\1\uffff\1\4\3\uffff\1\5\1\6\1\7\1\uffff"+
			"\2\3\1\uffff\2\3\1\30\1\10\1\uffff\1\3\1\26\1\11\3\uffff\1\3\1\12\3\uffff"+
			"\3\3\1\13\1\uffff\1\3\1\uffff\2\3\4\uffff\1\14\1\uffff\2\3\1\uffff\1"+
			"\15\2\uffff\1\3\1\33\1\3\1\uffff\2\3\1\uffff\3\3\3\uffff\1\3\1\uffff"+
			"\1\3\4\uffff\1\3\2\uffff\3\3\3\uffff\1\3\1\uffff\2\3\2\uffff\1\3\1\16"+
			"\4\3\1\17\1\27\1\20\1\25\1\21\1\uffff\1\34\1\3\1\uffff\1\32\2\3\3\uffff"+
			"\2\3\1\uffff\1\22\1\3\1\23\1\24\3\uffff\1\31\10\uffff\1\34\1\2",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\37\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\40\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\152\uffff\1\41\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\153\uffff\1\36\1\uffff\1\36\1\uffff\1\35",
			"",
			"\1\42\4\uffff\2\44\4\uffff\1\44\1\uffff\1\45\3\uffff\1\46\1\47\1\50"+
			"\1\uffff\2\44\1\uffff\2\44\1\71\1\51\1\uffff\1\44\1\67\1\52\3\uffff\1"+
			"\44\1\53\3\uffff\3\44\1\54\1\uffff\1\44\1\uffff\2\44\4\uffff\1\55\1\uffff"+
			"\2\44\1\uffff\1\56\2\uffff\1\44\1\36\1\44\1\uffff\2\44\1\uffff\3\44\3"+
			"\uffff\1\44\1\uffff\1\44\4\uffff\1\44\2\uffff\3\44\3\uffff\1\44\1\uffff"+
			"\2\44\2\uffff\1\44\1\57\4\44\1\60\1\70\1\61\1\66\1\62\1\uffff\1\34\1"+
			"\44\1\uffff\1\36\2\44\3\uffff\2\44\1\uffff\1\63\1\44\1\64\1\65\3\uffff"+
			"\1\36\11\uffff\1\43",
			"",
			"\1\34\1\72\3\uffff\2\34\4\uffff\1\34\1\uffff\1\34\3\uffff\3\34\1\uffff"+
			"\2\34\1\uffff\4\34\1\uffff\3\34\3\uffff\2\34\3\uffff\4\34\1\uffff\1\34"+
			"\1\uffff\2\34\4\uffff\1\34\1\uffff\2\34\1\uffff\1\34\2\uffff\3\34\1\uffff"+
			"\2\34\1\uffff\3\34\3\uffff\1\34\1\uffff\1\34\4\uffff\1\34\2\uffff\3\34"+
			"\3\uffff\1\34\1\uffff\2\34\2\uffff\13\34\1\uffff\2\34\1\uffff\3\34\3"+
			"\uffff\2\34\1\uffff\4\34\3\uffff\1\34\10\uffff\2\34\16\uffff\1\34\14"+
			"\uffff\1\72",
			"",
			"",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\152\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			""
	};

	static final short[] DFA13_eot = DFA.unpackEncodedString(DFA13_eotS);
	static final short[] DFA13_eof = DFA.unpackEncodedString(DFA13_eofS);
	static final char[] DFA13_min = DFA.unpackEncodedStringToUnsignedChars(DFA13_minS);
	static final char[] DFA13_max = DFA.unpackEncodedStringToUnsignedChars(DFA13_maxS);
	static final short[] DFA13_accept = DFA.unpackEncodedString(DFA13_acceptS);
	static final short[] DFA13_special = DFA.unpackEncodedString(DFA13_specialS);
	static final short[][] DFA13_transition;

	static {
		int numStates = DFA13_transitionS.length;
		DFA13_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA13_transition[i] = DFA.unpackEncodedString(DFA13_transitionS[i]);
		}
	}

	protected class DFA13 extends DFA {

		public DFA13(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 13;
			this.eot = DFA13_eot;
			this.eof = DFA13_eof;
			this.min = DFA13_min;
			this.max = DFA13_max;
			this.accept = DFA13_accept;
			this.special = DFA13_special;
			this.transition = DFA13_transition;
		}
		@Override
		public String getDescription() {
			return "330:8: (c= cident | K_COUNT '(' countArgument ')' | K_WRITETIME '(' c= cident ')' | K_TTL '(' c= cident ')' |f= functionName args= selectionFunctionArgs )";
		}
	}

	static final String DFA41_eotS =
		"\33\uffff";
	static final String DFA41_eofS =
		"\33\uffff";
	static final String DFA41_minS =
		"\1\24\30\100\2\uffff";
	static final String DFA41_maxS =
		"\1\u009d\30\u00b8\2\uffff";
	static final String DFA41_acceptS =
		"\31\uffff\1\1\1\2";
	static final String DFA41_specialS =
		"\33\uffff}>";
	static final String[] DFA41_transitionS = {
			"\1\1\4\uffff\2\3\4\uffff\1\3\1\uffff\1\4\3\uffff\1\5\1\6\1\7\1\uffff"+
			"\2\3\1\uffff\2\3\1\30\1\10\1\uffff\1\3\1\26\1\11\3\uffff\1\3\1\12\3\uffff"+
			"\3\3\1\13\1\uffff\1\3\1\uffff\2\3\4\uffff\1\14\1\uffff\2\3\1\uffff\1"+
			"\15\2\uffff\1\3\1\30\1\3\1\uffff\2\3\1\uffff\3\3\3\uffff\1\3\1\uffff"+
			"\1\3\4\uffff\1\3\2\uffff\3\3\3\uffff\1\3\1\uffff\2\3\2\uffff\1\3\1\16"+
			"\4\3\1\17\1\27\1\20\1\25\1\21\2\uffff\1\3\1\uffff\1\30\2\3\3\uffff\2"+
			"\3\1\uffff\1\22\1\3\1\23\1\24\3\uffff\1\30\11\uffff\1\2",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"\1\31\155\uffff\1\31\11\uffff\1\32",
			"",
			""
	};

	static final short[] DFA41_eot = DFA.unpackEncodedString(DFA41_eotS);
	static final short[] DFA41_eof = DFA.unpackEncodedString(DFA41_eofS);
	static final char[] DFA41_min = DFA.unpackEncodedStringToUnsignedChars(DFA41_minS);
	static final char[] DFA41_max = DFA.unpackEncodedStringToUnsignedChars(DFA41_maxS);
	static final short[] DFA41_accept = DFA.unpackEncodedString(DFA41_acceptS);
	static final short[] DFA41_special = DFA.unpackEncodedString(DFA41_specialS);
	static final short[][] DFA41_transition;

	static {
		int numStates = DFA41_transitionS.length;
		DFA41_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA41_transition[i] = DFA.unpackEncodedString(DFA41_transitionS[i]);
		}
	}

	protected class DFA41 extends DFA {

		public DFA41(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 41;
			this.eot = DFA41_eot;
			this.eof = DFA41_eof;
			this.min = DFA41_min;
			this.max = DFA41_max;
			this.accept = DFA41_accept;
			this.special = DFA41_special;
			this.transition = DFA41_transition;
		}
		@Override
		public String getDescription() {
			return "498:1: deleteOp returns [Operation.RawDeletion op] : (c= cident |c= cident '[' t= term ']' );";
		}
	}

	static final String DFA104_eotS =
		"\40\uffff";
	static final String DFA104_eofS =
		"\6\uffff\30\36\2\uffff";
	static final String DFA104_minS =
		"\1\30\2\uffff\1\24\2\uffff\30\u008b\2\uffff";
	static final String DFA104_maxS =
		"\1\u0092\2\uffff\1\u009d\2\uffff\30\u00b2\2\uffff";
	static final String DFA104_acceptS =
		"\1\uffff\1\1\1\2\1\uffff\1\5\1\6\30\uffff\1\3\1\4";
	static final String DFA104_specialS =
		"\40\uffff}>";
	static final String[] DFA104_transitionS = {
			"\1\2\3\uffff\1\1\34\uffff\1\3\62\uffff\1\5\45\uffff\1\4",
			"",
			"",
			"\1\6\4\uffff\2\10\4\uffff\1\10\1\uffff\1\11\3\uffff\1\12\1\13\1\14\1"+
			"\uffff\2\10\1\uffff\2\10\1\35\1\15\1\uffff\1\10\1\33\1\16\3\uffff\1\10"+
			"\1\17\3\uffff\3\10\1\20\1\uffff\1\10\1\uffff\2\10\4\uffff\1\21\1\uffff"+
			"\2\10\1\uffff\1\22\2\uffff\1\10\1\35\1\10\1\uffff\2\10\1\uffff\3\10\3"+
			"\uffff\1\10\1\uffff\1\10\4\uffff\1\10\2\uffff\3\10\3\uffff\1\10\1\uffff"+
			"\2\10\2\uffff\1\10\1\23\4\10\1\24\1\34\1\25\1\32\1\26\2\uffff\1\10\1"+
			"\uffff\1\35\2\10\3\uffff\2\10\1\uffff\1\27\1\10\1\30\1\31\3\uffff\1\35"+
			"\11\uffff\1\7",
			"",
			"",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"\1\37\46\uffff\1\36",
			"",
			""
	};

	static final short[] DFA104_eot = DFA.unpackEncodedString(DFA104_eotS);
	static final short[] DFA104_eof = DFA.unpackEncodedString(DFA104_eofS);
	static final char[] DFA104_min = DFA.unpackEncodedStringToUnsignedChars(DFA104_minS);
	static final char[] DFA104_max = DFA.unpackEncodedStringToUnsignedChars(DFA104_maxS);
	static final short[] DFA104_accept = DFA.unpackEncodedString(DFA104_acceptS);
	static final short[] DFA104_special = DFA.unpackEncodedString(DFA104_specialS);
	static final short[][] DFA104_transition;

	static {
		int numStates = DFA104_transitionS.length;
		DFA104_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA104_transition[i] = DFA.unpackEncodedString(DFA104_transitionS[i]);
		}
	}

	protected class DFA104 extends DFA {

		public DFA104(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 104;
			this.eot = DFA104_eot;
			this.eof = DFA104_eof;
			this.min = DFA104_min;
			this.max = DFA104_max;
			this.accept = DFA104_accept;
			this.special = DFA104_special;
			this.transition = DFA104_transition;
		}
		@Override
		public String getDescription() {
			return "824:11: ( K_ALTER id= cident K_TYPE v= comparatorType | K_ADD id= cident v= comparatorType ( K_STATIC )? | K_DROP id= cident | K_DROP id= cident K_USING K_TIMESTAMP t= INTEGER | K_WITH properties[attrs] | K_RENAME id1= cident K_TO toId1= cident ( K_AND idn= cident K_TO toIdn= cident )* )";
		}
	}

	static final String DFA143_eotS =
		"\34\uffff";
	static final String DFA143_eofS =
		"\1\uffff\31\33\2\uffff";
	static final String DFA143_minS =
		"\1\24\31\u00b0\2\uffff";
	static final String DFA143_maxS =
		"\1\u009d\31\u00b2\2\uffff";
	static final String DFA143_acceptS =
		"\32\uffff\1\1\1\2";
	static final String DFA143_specialS =
		"\34\uffff}>";
	static final String[] DFA143_transitionS = {
			"\1\1\4\uffff\2\3\4\uffff\1\3\1\uffff\1\4\3\uffff\1\5\1\6\1\7\1\uffff"+
			"\2\3\1\uffff\2\3\1\30\1\10\1\uffff\1\3\1\26\1\11\3\uffff\1\3\1\12\3\uffff"+
			"\3\3\1\13\1\uffff\1\3\1\uffff\2\3\4\uffff\1\14\1\uffff\2\3\1\uffff\1"+
			"\15\2\uffff\1\3\1\30\1\3\1\uffff\2\3\1\uffff\3\3\3\uffff\1\3\1\uffff"+
			"\1\3\4\uffff\1\3\2\uffff\3\3\3\uffff\1\3\1\uffff\2\3\2\uffff\1\3\1\16"+
			"\4\3\1\17\1\27\1\20\1\25\1\21\2\uffff\1\3\1\uffff\1\30\2\3\3\uffff\2"+
			"\3\1\uffff\1\22\1\3\1\23\1\24\3\uffff\1\30\10\uffff\1\31\1\2",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"\1\32\1\uffff\1\33",
			"",
			""
	};

	static final short[] DFA143_eot = DFA.unpackEncodedString(DFA143_eotS);
	static final short[] DFA143_eof = DFA.unpackEncodedString(DFA143_eofS);
	static final char[] DFA143_min = DFA.unpackEncodedStringToUnsignedChars(DFA143_minS);
	static final char[] DFA143_max = DFA.unpackEncodedStringToUnsignedChars(DFA143_maxS);
	static final short[] DFA143_accept = DFA.unpackEncodedString(DFA143_acceptS);
	static final short[] DFA143_special = DFA.unpackEncodedString(DFA143_specialS);
	static final short[][] DFA143_transition;

	static {
		int numStates = DFA143_transitionS.length;
		DFA143_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA143_transition[i] = DFA.unpackEncodedString(DFA143_transitionS[i]);
		}
	}

	protected class DFA143 extends DFA {

		public DFA143(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 143;
			this.eot = DFA143_eot;
			this.eof = DFA143_eof;
			this.min = DFA143_min;
			this.max = DFA143_max;
			this.accept = DFA143_accept;
			this.special = DFA143_special;
			this.transition = DFA143_transition;
		}
		@Override
		public String getDescription() {
			return "1200:7: ( ksName[name] '.' )?";
		}
	}

	static final String DFA144_eotS =
		"\34\uffff";
	static final String DFA144_eofS =
		"\1\uffff\31\33\2\uffff";
	static final String DFA144_minS =
		"\1\24\31\30\2\uffff";
	static final String DFA144_maxS =
		"\1\u009d\31\u00b2\2\uffff";
	static final String DFA144_acceptS =
		"\32\uffff\1\1\1\2";
	static final String DFA144_specialS =
		"\34\uffff}>";
	static final String[] DFA144_transitionS = {
			"\1\1\4\uffff\2\3\4\uffff\1\3\1\uffff\1\4\3\uffff\1\5\1\6\1\7\1\uffff"+
			"\2\3\1\uffff\2\3\1\30\1\10\1\uffff\1\3\1\26\1\11\3\uffff\1\3\1\12\3\uffff"+
			"\3\3\1\13\1\uffff\1\3\1\uffff\2\3\4\uffff\1\14\1\uffff\2\3\1\uffff\1"+
			"\15\2\uffff\1\3\1\30\1\3\1\uffff\2\3\1\uffff\3\3\3\uffff\1\3\1\uffff"+
			"\1\3\4\uffff\1\3\2\uffff\3\3\3\uffff\1\3\1\uffff\2\3\2\uffff\1\3\1\16"+
			"\4\3\1\17\1\27\1\20\1\25\1\21\2\uffff\1\3\1\uffff\1\30\2\3\3\uffff\2"+
			"\3\1\uffff\1\22\1\3\1\23\1\24\3\uffff\1\30\10\uffff\1\31\1\2",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"\1\33\2\uffff\2\33\2\uffff\1\33\31\uffff\1\33\6\uffff\1\33\20\uffff"+
			"\1\33\5\uffff\1\33\7\uffff\1\33\3\uffff\1\33\3\uffff\1\33\3\uffff\2\33"+
			"\6\uffff\1\33\13\uffff\1\33\13\uffff\1\33\5\uffff\2\33\30\uffff\1\33"+
			"\4\uffff\1\32\1\uffff\1\33",
			"",
			""
	};

	static final short[] DFA144_eot = DFA.unpackEncodedString(DFA144_eotS);
	static final short[] DFA144_eof = DFA.unpackEncodedString(DFA144_eofS);
	static final char[] DFA144_min = DFA.unpackEncodedStringToUnsignedChars(DFA144_minS);
	static final char[] DFA144_max = DFA.unpackEncodedStringToUnsignedChars(DFA144_maxS);
	static final short[] DFA144_accept = DFA.unpackEncodedString(DFA144_acceptS);
	static final short[] DFA144_special = DFA.unpackEncodedString(DFA144_specialS);
	static final short[][] DFA144_transition;

	static {
		int numStates = DFA144_transitionS.length;
		DFA144_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA144_transition[i] = DFA.unpackEncodedString(DFA144_transitionS[i]);
		}
	}

	protected class DFA144 extends DFA {

		public DFA144(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 144;
			this.eot = DFA144_eot;
			this.eof = DFA144_eof;
			this.min = DFA144_min;
			this.max = DFA144_max;
			this.accept = DFA144_accept;
			this.special = DFA144_special;
			this.transition = DFA144_transition;
		}
		@Override
		public String getDescription() {
			return "1205:7: ( ksName[name] '.' )?";
		}
	}

	static final String DFA162_eotS =
		"\42\uffff";
	static final String DFA162_eofS =
		"\42\uffff";
	static final String DFA162_minS =
		"\1\6\2\uffff\1\6\4\uffff\30\u00ab\1\u00b0\1\uffff";
	static final String DFA162_maxS =
		"\1\u00bc\2\uffff\1\u00bd\4\uffff\31\u00b1\1\uffff";
	static final String DFA162_acceptS =
		"\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\7\31\uffff\1\3";
	static final String DFA162_specialS =
		"\42\uffff}>";
	static final String[] DFA162_transitionS = {
			"\1\1\7\uffff\1\1\3\uffff\1\1\2\uffff\1\1\64\uffff\1\1\22\uffff\1\1\4"+
			"\uffff\1\5\71\uffff\1\7\3\uffff\1\1\2\uffff\1\1\7\uffff\1\4\3\uffff\1"+
			"\1\1\uffff\1\6\6\uffff\1\2\3\uffff\1\3",
			"",
			"",
			"\1\2\7\uffff\1\2\3\uffff\1\2\1\uffff\1\10\1\2\3\uffff\2\12\4\uffff\1"+
			"\12\1\uffff\1\13\3\uffff\1\14\1\15\1\16\1\uffff\2\12\1\uffff\2\12\1\37"+
			"\1\17\1\uffff\1\12\1\35\1\20\3\uffff\1\12\1\21\3\uffff\3\12\1\22\1\uffff"+
			"\1\12\1\uffff\2\12\4\uffff\1\23\1\2\2\12\1\uffff\1\24\2\uffff\1\12\1"+
			"\40\1\12\1\uffff\2\12\1\uffff\3\12\2\uffff\1\2\1\12\1\uffff\1\12\1\uffff"+
			"\1\2\2\uffff\1\12\2\uffff\3\12\3\uffff\1\12\1\uffff\2\12\2\uffff\1\12"+
			"\1\25\4\12\1\26\1\36\1\27\1\34\1\30\1\uffff\1\2\1\12\1\uffff\1\40\2\12"+
			"\3\uffff\2\12\1\uffff\1\31\1\12\1\32\1\33\3\uffff\1\40\10\uffff\1\2\1"+
			"\11\2\uffff\1\2\2\uffff\1\2\7\uffff\1\2\3\uffff\1\2\1\uffff\1\2\6\uffff"+
			"\1\2\3\uffff\2\2",
			"",
			"",
			"",
			"",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\4\uffff\1\2\1\41",
			"\1\2\1\41",
			""
	};

	static final short[] DFA162_eot = DFA.unpackEncodedString(DFA162_eotS);
	static final short[] DFA162_eof = DFA.unpackEncodedString(DFA162_eofS);
	static final char[] DFA162_min = DFA.unpackEncodedStringToUnsignedChars(DFA162_minS);
	static final char[] DFA162_max = DFA.unpackEncodedStringToUnsignedChars(DFA162_maxS);
	static final short[] DFA162_accept = DFA.unpackEncodedString(DFA162_acceptS);
	static final short[] DFA162_special = DFA.unpackEncodedString(DFA162_specialS);
	static final short[][] DFA162_transition;

	static {
		int numStates = DFA162_transitionS.length;
		DFA162_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA162_transition[i] = DFA.unpackEncodedString(DFA162_transitionS[i]);
		}
	}

	protected class DFA162 extends DFA {

		public DFA162(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 162;
			this.eot = DFA162_eot;
			this.eof = DFA162_eof;
			this.min = DFA162_min;
			this.max = DFA162_max;
			this.accept = DFA162_accept;
			this.special = DFA162_special;
			this.transition = DFA162_transition;
		}
		@Override
		public String getDescription() {
			return "1294:1: value returns [Term.Raw value] : (c= constant |l= collectionLiteral |u= usertypeLiteral |t= tupleLiteral | K_NULL | ':' id= noncol_ident | QMARK );";
		}
	}

	static final String DFA164_eotS =
		"\33\uffff";
	static final String DFA164_eofS =
		"\1\uffff\30\32\2\uffff";
	static final String DFA164_minS =
		"\1\24\30\u00ab\2\uffff";
	static final String DFA164_maxS =
		"\1\u009d\30\u00b2\2\uffff";
	static final String DFA164_acceptS =
		"\31\uffff\1\1\1\2";
	static final String DFA164_specialS =
		"\33\uffff}>";
	static final String[] DFA164_transitionS = {
			"\1\1\4\uffff\2\3\4\uffff\1\3\1\uffff\1\4\3\uffff\1\5\1\6\1\7\1\uffff"+
			"\2\3\1\uffff\2\3\1\30\1\10\1\uffff\1\3\1\26\1\11\3\uffff\1\3\1\12\3\uffff"+
			"\3\3\1\13\1\uffff\1\3\1\uffff\2\3\4\uffff\1\14\1\uffff\2\3\1\uffff\1"+
			"\15\2\uffff\1\3\1\31\1\3\1\uffff\2\3\1\uffff\3\3\3\uffff\1\3\1\uffff"+
			"\1\3\4\uffff\1\3\2\uffff\3\3\3\uffff\1\3\1\uffff\2\3\2\uffff\1\3\1\16"+
			"\4\3\1\17\1\27\1\20\1\25\1\21\1\uffff\1\32\1\3\1\uffff\1\31\2\3\3\uffff"+
			"\2\3\1\uffff\1\22\1\3\1\23\1\24\3\uffff\1\31\10\uffff\1\31\1\2",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"\1\32\4\uffff\1\31\1\uffff\1\32",
			"",
			""
	};

	static final short[] DFA164_eot = DFA.unpackEncodedString(DFA164_eotS);
	static final short[] DFA164_eof = DFA.unpackEncodedString(DFA164_eofS);
	static final char[] DFA164_min = DFA.unpackEncodedStringToUnsignedChars(DFA164_minS);
	static final char[] DFA164_max = DFA.unpackEncodedStringToUnsignedChars(DFA164_maxS);
	static final short[] DFA164_accept = DFA.unpackEncodedString(DFA164_acceptS);
	static final short[] DFA164_special = DFA.unpackEncodedString(DFA164_specialS);
	static final short[][] DFA164_transition;

	static {
		int numStates = DFA164_transitionS.length;
		DFA164_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA164_transition[i] = DFA.unpackEncodedString(DFA164_transitionS[i]);
		}
	}

	protected class DFA164 extends DFA {

		public DFA164(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 164;
			this.eot = DFA164_eot;
			this.eof = DFA164_eof;
			this.min = DFA164_min;
			this.max = DFA164_max;
			this.accept = DFA164_accept;
			this.special = DFA164_special;
			this.transition = DFA164_transition;
		}
		@Override
		public String getDescription() {
			return "1312:7: (ks= keyspaceName '.' )?";
		}
	}

	static final String DFA166_eotS =
		"\70\uffff";
	static final String DFA166_eofS =
		"\70\uffff";
	static final String DFA166_minS =
		"\1\24\30\u00ab\1\u00b0\1\u00ab\1\u00b0\1\24\1\6\30\u00ab\2\uffff";
	static final String DFA166_maxS =
		"\1\u009d\31\u00b0\1\u00ab\1\u00b0\1\u009d\1\u00bc\30\u00ab\2\uffff";
	static final String DFA166_acceptS =
		"\66\uffff\1\1\1\2";
	static final String DFA166_specialS =
		"\70\uffff}>";
	static final String[] DFA166_transitionS = {
			"\1\1\4\uffff\2\3\4\uffff\1\3\1\uffff\1\4\3\uffff\1\5\1\6\1\7\1\uffff"+
			"\2\3\1\uffff\2\3\1\30\1\10\1\uffff\1\3\1\26\1\11\3\uffff\1\3\1\12\3\uffff"+
			"\3\3\1\13\1\uffff\1\3\1\uffff\2\3\4\uffff\1\14\1\uffff\2\3\1\uffff\1"+
			"\15\2\uffff\1\3\1\33\1\3\1\uffff\2\3\1\uffff\3\3\3\uffff\1\3\1\uffff"+
			"\1\3\4\uffff\1\3\2\uffff\3\3\3\uffff\1\3\1\uffff\2\3\2\uffff\1\3\1\16"+
			"\4\3\1\17\1\27\1\20\1\25\1\21\1\uffff\1\32\1\3\1\uffff\1\33\2\3\3\uffff"+
			"\2\3\1\uffff\1\22\1\3\1\23\1\24\3\uffff\1\33\10\uffff\1\31\1\2",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\35\4\uffff\1\34",
			"\1\34",
			"\1\35",
			"\1\34",
			"\1\36\4\uffff\2\40\4\uffff\1\40\1\uffff\1\41\3\uffff\1\42\1\43\1\44"+
			"\1\uffff\2\40\1\uffff\2\40\1\65\1\45\1\uffff\1\40\1\63\1\46\3\uffff\1"+
			"\40\1\47\3\uffff\3\40\1\50\1\uffff\1\40\1\uffff\2\40\4\uffff\1\51\1\uffff"+
			"\2\40\1\uffff\1\52\2\uffff\1\40\1\uffff\1\40\1\uffff\2\40\1\uffff\3\40"+
			"\3\uffff\1\40\1\uffff\1\40\4\uffff\1\40\2\uffff\3\40\3\uffff\1\40\1\uffff"+
			"\2\40\2\uffff\1\40\1\53\4\40\1\54\1\64\1\55\1\62\1\56\1\uffff\1\32\1"+
			"\40\2\uffff\2\40\3\uffff\2\40\1\uffff\1\57\1\40\1\60\1\61\15\uffff\1"+
			"\37",
			"\1\67\7\uffff\1\67\3\uffff\1\67\1\uffff\2\67\3\uffff\2\67\4\uffff\1"+
			"\67\1\uffff\1\67\3\uffff\3\67\1\uffff\2\67\1\uffff\4\67\1\uffff\3\67"+
			"\3\uffff\2\67\3\uffff\4\67\1\uffff\1\67\1\uffff\2\67\4\uffff\4\67\1\uffff"+
			"\1\67\2\uffff\3\67\1\uffff\2\67\1\uffff\3\67\2\uffff\2\67\1\uffff\1\67"+
			"\1\uffff\1\67\2\uffff\1\67\2\uffff\3\67\3\uffff\1\67\1\uffff\2\67\2\uffff"+
			"\13\67\1\uffff\2\67\1\uffff\3\67\3\uffff\2\67\1\uffff\4\67\3\uffff\1"+
			"\67\10\uffff\2\67\2\uffff\1\67\2\uffff\1\67\7\uffff\1\67\1\66\2\uffff"+
			"\1\67\1\uffff\1\67\6\uffff\1\67\3\uffff\1\67",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"\1\35",
			"",
			""
	};

	static final short[] DFA166_eot = DFA.unpackEncodedString(DFA166_eotS);
	static final short[] DFA166_eof = DFA.unpackEncodedString(DFA166_eofS);
	static final char[] DFA166_min = DFA.unpackEncodedStringToUnsignedChars(DFA166_minS);
	static final char[] DFA166_max = DFA.unpackEncodedStringToUnsignedChars(DFA166_maxS);
	static final short[] DFA166_accept = DFA.unpackEncodedString(DFA166_acceptS);
	static final short[] DFA166_special = DFA.unpackEncodedString(DFA166_specialS);
	static final short[][] DFA166_transition;

	static {
		int numStates = DFA166_transitionS.length;
		DFA166_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA166_transition[i] = DFA.unpackEncodedString(DFA166_transitionS[i]);
		}
	}

	protected class DFA166 extends DFA {

		public DFA166(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 166;
			this.eot = DFA166_eot;
			this.eof = DFA166_eof;
			this.min = DFA166_min;
			this.max = DFA166_max;
			this.accept = DFA166_accept;
			this.special = DFA166_special;
			this.transition = DFA166_transition;
		}
		@Override
		public String getDescription() {
			return "1323:1: function returns [Term.Raw t] : (f= functionName '(' ')' |f= functionName '(' args= functionArgs ')' );";
		}
	}

	static final String DFA168_eotS =
		"\104\uffff";
	static final String DFA168_eofS =
		"\3\uffff\1\1\41\uffff\1\1\5\uffff\31\41";
	static final String DFA168_minS =
		"\1\6\1\uffff\1\6\1\33\1\uffff\1\u00ac\30\u00ab\1\u00ac\2\u00ab\1\uffff"+
		"\1\u00ab\1\u00b0\1\u00ab\1\6\1\24\1\6\3\u00ab\31\33";
	static final String DFA168_maxS =
		"\1\u00bc\1\uffff\1\u00bc\1\u00bd\1\uffff\1\u00ae\2\u00b0\1\u00b3\26\u00b0"+
		"\2\u00b3\1\uffff\1\u00b3\2\u00b0\1\u00bd\1\u009d\1\u00bc\3\u00ac\31\u00bd";
	static final String DFA168_acceptS =
		"\1\uffff\1\1\2\uffff\1\2\34\uffff\1\3\42\uffff";
	static final String DFA168_specialS =
		"\104\uffff}>";
	static final String[] DFA168_transitionS = {
			"\1\1\7\uffff\1\1\3\uffff\1\1\1\uffff\1\4\1\1\3\uffff\2\4\4\uffff\1\4"+
			"\1\uffff\1\4\3\uffff\3\4\1\uffff\2\4\1\uffff\4\4\1\uffff\3\4\3\uffff"+
			"\2\4\3\uffff\4\4\1\uffff\1\4\1\uffff\2\4\4\uffff\1\4\1\1\2\4\1\uffff"+
			"\1\4\2\uffff\3\4\1\uffff\2\4\1\uffff\3\4\2\uffff\1\1\1\4\1\uffff\1\4"+
			"\1\uffff\1\1\2\uffff\1\4\2\uffff\3\4\3\uffff\1\4\1\uffff\2\4\2\uffff"+
			"\13\4\1\uffff\2\4\1\uffff\3\4\3\uffff\2\4\1\uffff\4\4\3\uffff\1\4\10"+
			"\uffff\1\3\1\4\2\uffff\1\1\2\uffff\1\1\7\uffff\1\2\3\uffff\1\1\1\uffff"+
			"\1\1\6\uffff\1\1\3\uffff\1\1",
			"",
			"\1\1\7\uffff\1\1\3\uffff\1\1\1\uffff\1\6\1\1\3\uffff\2\44\4\uffff\1"+
			"\44\1\uffff\1\11\3\uffff\1\12\1\13\1\14\1\uffff\2\44\1\uffff\2\44\1\35"+
			"\1\15\1\uffff\1\44\1\33\1\16\3\uffff\1\44\1\17\3\uffff\3\44\1\20\1\uffff"+
			"\1\42\1\uffff\2\44\4\uffff\1\21\1\1\2\44\1\uffff\1\22\2\uffff\1\44\1"+
			"\36\1\44\1\uffff\2\44\1\uffff\1\37\1\44\1\10\2\uffff\1\1\1\44\1\uffff"+
			"\1\44\1\uffff\1\1\2\uffff\1\44\2\uffff\3\44\3\uffff\1\44\1\uffff\2\44"+
			"\1\uffff\1\41\1\44\1\23\4\44\1\24\1\34\1\25\1\32\1\26\1\uffff\1\1\1\44"+
			"\1\uffff\1\43\1\40\1\44\3\uffff\2\44\1\uffff\1\27\1\44\1\30\1\31\3\uffff"+
			"\1\43\10\uffff\1\1\1\7\2\uffff\1\5\2\uffff\1\1\7\uffff\1\1\3\uffff\1"+
			"\1\1\uffff\1\1\6\uffff\1\1\3\uffff\1\1",
			"\1\1\1\uffff\2\1\25\uffff\1\1\21\uffff\1\1\6\uffff\1\1\11\uffff\1\1"+
			"\17\uffff\1\1\3\uffff\1\1\33\uffff\1\1\11\uffff\1\1\32\uffff\3\1\1\uffff"+
			"\1\4\2\1\7\uffff\1\1\2\uffff\1\1",
			"",
			"\1\45\1\uffff\1\1",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46\2\uffff\1\41",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\1\4\uffff\1\46",
			"\1\41\3\uffff\1\46",
			"\1\1\1\41\3\uffff\1\46\2\uffff\1\41",
			"\1\1\1\41\3\uffff\1\46\2\uffff\1\41",
			"",
			"\1\1\1\41\3\uffff\1\46\2\uffff\1\41",
			"\1\46",
			"\1\1\1\41\3\uffff\1\46",
			"\1\41\7\uffff\1\41\3\uffff\1\41\1\uffff\2\41\3\uffff\2\41\1\1\1\uffff"+
			"\2\1\1\41\1\uffff\1\41\3\uffff\3\41\1\uffff\2\41\1\uffff\4\41\1\uffff"+
			"\3\41\1\1\2\uffff\2\41\3\uffff\4\41\1\uffff\1\41\1\uffff\2\41\1\uffff"+
			"\1\1\2\uffff\4\41\1\1\1\41\2\uffff\3\41\1\uffff\2\41\1\1\3\41\2\uffff"+
			"\2\41\1\uffff\1\41\1\uffff\1\41\2\uffff\1\41\1\uffff\1\1\3\41\1\1\2\uffff"+
			"\1\41\1\uffff\2\41\2\uffff\13\41\1\uffff\2\41\1\uffff\3\41\1\uffff\1"+
			"\1\1\uffff\2\41\1\uffff\4\41\1\uffff\1\1\1\uffff\1\41\10\uffff\2\41\2"+
			"\uffff\1\41\2\uffff\1\41\7\uffff\1\41\3\1\1\41\1\uffff\1\47\1\1\5\uffff"+
			"\1\41\1\uffff\1\1\1\uffff\1\41\1\1",
			"\1\50\4\uffff\2\52\4\uffff\1\52\1\uffff\1\1\3\uffff\3\1\1\uffff\2\52"+
			"\1\uffff\2\52\2\1\1\uffff\1\52\2\1\3\uffff\1\52\1\1\3\uffff\3\52\1\1"+
			"\1\uffff\1\52\1\uffff\2\52\4\uffff\1\1\1\uffff\2\52\1\uffff\1\1\2\uffff"+
			"\1\52\1\41\1\52\1\uffff\2\52\1\uffff\3\52\3\uffff\1\52\1\uffff\1\52\4"+
			"\uffff\1\52\2\uffff\3\52\3\uffff\1\52\1\uffff\2\52\2\uffff\1\52\1\1\4"+
			"\52\5\1\1\uffff\1\1\1\52\2\uffff\2\52\3\uffff\2\52\1\uffff\1\1\1\52\2"+
			"\1\15\uffff\1\51",
			"\1\1\7\uffff\1\1\3\uffff\1\1\1\uffff\1\53\1\1\3\uffff\2\55\4\uffff\1"+
			"\55\1\uffff\1\56\3\uffff\1\57\1\60\1\61\1\uffff\2\55\1\uffff\2\55\1\102"+
			"\1\62\1\uffff\1\55\1\100\1\63\3\uffff\1\55\1\64\3\uffff\3\55\1\65\1\uffff"+
			"\1\55\1\uffff\2\55\4\uffff\1\66\1\1\2\55\1\uffff\1\67\2\uffff\1\55\1"+
			"\103\1\55\1\uffff\2\55\1\uffff\3\55\2\uffff\1\1\1\55\1\uffff\1\55\1\uffff"+
			"\1\1\2\uffff\1\55\2\uffff\3\55\3\uffff\1\55\1\uffff\2\55\2\uffff\1\55"+
			"\1\70\4\55\1\71\1\101\1\72\1\77\1\73\1\uffff\1\1\1\55\1\uffff\1\103\2"+
			"\55\3\uffff\2\55\1\uffff\1\74\1\55\1\75\1\76\3\uffff\1\103\10\uffff\1"+
			"\1\1\54\2\uffff\1\1\2\uffff\1\1\7\uffff\1\1\3\uffff\1\1\1\uffff\1\1\6"+
			"\uffff\1\1\3\uffff\1\1",
			"\1\1\1\41",
			"\1\1\1\41",
			"\1\1\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\31\uffff"+
			"\1\1\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\11\uffff"+
			"\1\41\17\uffff\1\41\3\uffff\1\41\33\uffff\1\41\11\uffff\1\41\32\uffff"+
			"\3\41\1\uffff\1\1\2\41\7\uffff\1\41\2\uffff\1\41"
	};

	static final short[] DFA168_eot = DFA.unpackEncodedString(DFA168_eotS);
	static final short[] DFA168_eof = DFA.unpackEncodedString(DFA168_eofS);
	static final char[] DFA168_min = DFA.unpackEncodedStringToUnsignedChars(DFA168_minS);
	static final char[] DFA168_max = DFA.unpackEncodedStringToUnsignedChars(DFA168_maxS);
	static final short[] DFA168_accept = DFA.unpackEncodedString(DFA168_acceptS);
	static final short[] DFA168_special = DFA.unpackEncodedString(DFA168_specialS);
	static final short[][] DFA168_transition;

	static {
		int numStates = DFA168_transitionS.length;
		DFA168_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA168_transition[i] = DFA.unpackEncodedString(DFA168_transitionS[i]);
		}
	}

	protected class DFA168 extends DFA {

		public DFA168(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 168;
			this.eot = DFA168_eot;
			this.eof = DFA168_eof;
			this.min = DFA168_min;
			this.max = DFA168_max;
			this.accept = DFA168_accept;
			this.special = DFA168_special;
			this.transition = DFA168_transition;
		}
		@Override
		public String getDescription() {
			return "1333:1: term returns [Term.Raw term] : (v= value |f= function | '(' c= comparatorType ')' t= term );";
		}
	}

	static final String DFA171_eotS =
		"\35\uffff";
	static final String DFA171_eofS =
		"\35\uffff";
	static final String DFA171_minS =
		"\1\6\1\uffff\31\25\2\uffff";
	static final String DFA171_maxS =
		"\1\u00bc\1\uffff\31\u00b0\2\uffff";
	static final String DFA171_acceptS =
		"\1\uffff\1\1\31\uffff\1\2\1\3";
	static final String DFA171_specialS =
		"\35\uffff}>";
	static final String[] DFA171_transitionS = {
			"\1\1\7\uffff\1\1\3\uffff\1\1\1\uffff\1\2\1\1\3\uffff\2\4\4\uffff\1\4"+
			"\1\uffff\1\5\3\uffff\1\6\1\7\1\10\1\uffff\2\4\1\uffff\2\4\1\31\1\11\1"+
			"\uffff\1\4\1\27\1\12\3\uffff\1\4\1\13\3\uffff\3\4\1\14\1\uffff\1\4\1"+
			"\uffff\2\4\4\uffff\1\15\1\1\2\4\1\uffff\1\16\2\uffff\1\4\1\32\1\4\1\uffff"+
			"\2\4\1\uffff\3\4\2\uffff\1\1\1\4\1\uffff\1\4\1\uffff\1\1\2\uffff\1\4"+
			"\2\uffff\3\4\3\uffff\1\4\1\uffff\2\4\2\uffff\1\4\1\17\4\4\1\20\1\30\1"+
			"\21\1\26\1\22\1\uffff\1\1\1\4\1\uffff\1\32\2\4\3\uffff\2\4\1\uffff\1"+
			"\23\1\4\1\24\1\25\3\uffff\1\32\10\uffff\1\1\1\3\2\uffff\1\1\2\uffff\1"+
			"\1\7\uffff\1\1\3\uffff\1\1\1\uffff\1\1\6\uffff\1\1\3\uffff\1\1",
			"",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0095\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0097\uffff\1\33\1\uffff\1\33\1\1",
			"",
			""
	};

	static final short[] DFA171_eot = DFA.unpackEncodedString(DFA171_eotS);
	static final short[] DFA171_eof = DFA.unpackEncodedString(DFA171_eofS);
	static final char[] DFA171_min = DFA.unpackEncodedStringToUnsignedChars(DFA171_minS);
	static final char[] DFA171_max = DFA.unpackEncodedStringToUnsignedChars(DFA171_maxS);
	static final short[] DFA171_accept = DFA.unpackEncodedString(DFA171_acceptS);
	static final short[] DFA171_special = DFA.unpackEncodedString(DFA171_specialS);
	static final short[][] DFA171_transition;

	static {
		int numStates = DFA171_transitionS.length;
		DFA171_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA171_transition[i] = DFA.unpackEncodedString(DFA171_transitionS[i]);
		}
	}

	protected class DFA171 extends DFA {

		public DFA171(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 171;
			this.eot = DFA171_eot;
			this.eof = DFA171_eof;
			this.min = DFA171_min;
			this.max = DFA171_max;
			this.accept = DFA171_accept;
			this.special = DFA171_special;
			this.transition = DFA171_transition;
		}
		@Override
		public String getDescription() {
			return "1348:1: normalColumnOperation[List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations, ColumnIdentifier.Raw key] : (t= term ( '+' c= cident )? |c= cident sig= ( '+' | '-' ) t= term |c= cident i= INTEGER );";
		}
	}

	static final String DFA177_eotS =
		"\34\uffff";
	static final String DFA177_eofS =
		"\34\uffff";
	static final String DFA177_minS =
		"\1\24\30\u00b5\1\6\2\uffff";
	static final String DFA177_maxS =
		"\1\u009d\30\u00b5\1\u00bc\2\uffff";
	static final String DFA177_acceptS =
		"\32\uffff\1\1\1\2";
	static final String DFA177_specialS =
		"\34\uffff}>";
	static final String[] DFA177_transitionS = {
			"\1\1\4\uffff\2\3\4\uffff\1\3\1\uffff\1\4\3\uffff\1\5\1\6\1\7\1\uffff"+
			"\2\3\1\uffff\2\3\1\30\1\10\1\uffff\1\3\1\26\1\11\3\uffff\1\3\1\12\3\uffff"+
			"\3\3\1\13\1\uffff\1\3\1\uffff\2\3\4\uffff\1\14\1\uffff\2\3\1\uffff\1"+
			"\15\2\uffff\1\3\1\30\1\3\1\uffff\2\3\1\uffff\3\3\3\uffff\1\3\1\uffff"+
			"\1\3\4\uffff\1\3\2\uffff\3\3\3\uffff\1\3\1\uffff\2\3\2\uffff\1\3\1\16"+
			"\4\3\1\17\1\27\1\20\1\25\1\21\2\uffff\1\3\1\uffff\1\30\2\3\3\uffff\2"+
			"\3\1\uffff\1\22\1\3\1\23\1\24\3\uffff\1\30\11\uffff\1\2",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\31",
			"\1\32\7\uffff\1\32\3\uffff\1\32\2\uffff\1\32\3\uffff\2\32\4\uffff\1"+
			"\32\1\uffff\1\32\3\uffff\3\32\1\uffff\2\32\1\uffff\4\32\1\uffff\3\32"+
			"\3\uffff\2\32\3\uffff\4\32\1\uffff\1\32\1\uffff\2\32\4\uffff\4\32\1\uffff"+
			"\1\32\2\uffff\3\32\1\uffff\2\32\1\uffff\3\32\2\uffff\2\32\1\uffff\1\32"+
			"\4\uffff\1\32\2\uffff\3\32\3\uffff\1\32\1\uffff\2\32\2\uffff\13\32\2"+
			"\uffff\1\32\1\uffff\3\32\3\uffff\2\32\1\uffff\4\32\3\uffff\1\32\14\uffff"+
			"\1\32\2\uffff\1\32\13\uffff\1\32\14\uffff\1\33",
			"",
			""
	};

	static final short[] DFA177_eot = DFA.unpackEncodedString(DFA177_eotS);
	static final short[] DFA177_eof = DFA.unpackEncodedString(DFA177_eofS);
	static final char[] DFA177_min = DFA.unpackEncodedStringToUnsignedChars(DFA177_minS);
	static final char[] DFA177_max = DFA.unpackEncodedStringToUnsignedChars(DFA177_maxS);
	static final short[] DFA177_accept = DFA.unpackEncodedString(DFA177_acceptS);
	static final short[] DFA177_special = DFA.unpackEncodedString(DFA177_specialS);
	static final short[][] DFA177_transition;

	static {
		int numStates = DFA177_transitionS.length;
		DFA177_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA177_transition[i] = DFA.unpackEncodedString(DFA177_transitionS[i]);
		}
	}

	protected class DFA177 extends DFA {

		public DFA177(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 177;
			this.eot = DFA177_eot;
			this.eof = DFA177_eof;
			this.min = DFA177_min;
			this.max = DFA177_max;
			this.accept = DFA177_accept;
			this.special = DFA177_special;
			this.transition = DFA177_transition;
		}
		@Override
		public String getDescription() {
			return "1407:1: property[PropertyDefinitions props] : (k= noncol_ident '=' simple= propertyValue |k= noncol_ident '=' map= mapLiteral );";
		}
	}

	static final String DFA183_eotS =
		"\74\uffff";
	static final String DFA183_eofS =
		"\74\uffff";
	static final String DFA183_minS =
		"\1\24\30\55\1\uffff\1\24\2\uffff\1\u009c\2\uffff\30\55\4\uffff";
	static final String DFA183_maxS =
		"\1\u00ab\30\u00b8\1\uffff\1\u00ab\2\uffff\1\u00b1\2\uffff\30\u00b8\4\uffff";
	static final String DFA183_acceptS =
		"\31\uffff\1\3\1\uffff\1\1\1\2\1\uffff\1\6\1\7\30\uffff\1\11\1\4\1\5\1"+
		"\10";
	static final String DFA183_specialS =
		"\74\uffff}>";
	static final String[] DFA183_transitionS = {
			"\1\1\4\uffff\2\3\4\uffff\1\3\1\uffff\1\4\3\uffff\1\5\1\6\1\7\1\uffff"+
			"\2\3\1\uffff\2\3\1\30\1\10\1\uffff\1\3\1\26\1\11\3\uffff\1\3\1\12\3\uffff"+
			"\3\3\1\13\1\uffff\1\3\1\uffff\2\3\4\uffff\1\14\1\uffff\2\3\1\uffff\1"+
			"\15\2\uffff\1\3\1\30\1\3\1\uffff\2\3\1\uffff\3\3\3\uffff\1\3\1\uffff"+
			"\1\3\4\uffff\1\3\2\uffff\3\3\3\uffff\1\3\1\uffff\2\3\2\uffff\1\3\1\16"+
			"\4\3\1\17\1\27\1\20\1\25\1\21\1\uffff\1\31\1\3\1\uffff\1\30\2\3\3\uffff"+
			"\2\3\1\uffff\1\22\1\3\1\23\1\24\3\uffff\1\30\11\uffff\1\2\15\uffff\1"+
			"\32",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"\1\36\31\uffff\1\35\10\uffff\1\34\131\uffff\1\33\10\uffff\5\33\1\37",
			"",
			"\1\40\4\uffff\2\42\4\uffff\1\42\1\uffff\1\43\3\uffff\1\44\1\45\1\46"+
			"\1\uffff\2\42\1\uffff\2\42\1\67\1\47\1\uffff\1\42\1\65\1\50\3\uffff\1"+
			"\42\1\51\3\uffff\3\42\1\52\1\uffff\1\42\1\uffff\2\42\4\uffff\1\53\1\uffff"+
			"\2\42\1\uffff\1\54\2\uffff\1\42\1\67\1\42\1\uffff\2\42\1\uffff\3\42\3"+
			"\uffff\1\42\1\uffff\1\42\4\uffff\1\42\2\uffff\3\42\3\uffff\1\42\1\uffff"+
			"\2\42\2\uffff\1\42\1\55\4\42\1\56\1\66\1\57\1\64\1\60\1\uffff\1\70\1"+
			"\42\1\uffff\1\67\2\42\3\uffff\2\42\1\uffff\1\61\1\42\1\62\1\63\3\uffff"+
			"\1\67\11\uffff\1\41\15\uffff\1\70",
			"",
			"",
			"\1\71\16\uffff\1\72\5\uffff\1\71",
			"",
			"",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"\1\70\31\uffff\1\70\10\uffff\1\70\131\uffff\1\70\1\uffff\1\73\1\uffff"+
			"\1\73\4\uffff\6\70",
			"",
			"",
			"",
			""
	};

	static final short[] DFA183_eot = DFA.unpackEncodedString(DFA183_eotS);
	static final short[] DFA183_eof = DFA.unpackEncodedString(DFA183_eofS);
	static final char[] DFA183_min = DFA.unpackEncodedStringToUnsignedChars(DFA183_minS);
	static final char[] DFA183_max = DFA.unpackEncodedStringToUnsignedChars(DFA183_maxS);
	static final short[] DFA183_accept = DFA.unpackEncodedString(DFA183_acceptS);
	static final short[] DFA183_special = DFA.unpackEncodedString(DFA183_specialS);
	static final short[][] DFA183_transition;

	static {
		int numStates = DFA183_transitionS.length;
		DFA183_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA183_transition[i] = DFA.unpackEncodedString(DFA183_transitionS[i]);
		}
	}

	protected class DFA183 extends DFA {

		public DFA183(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 183;
			this.eot = DFA183_eot;
			this.eof = DFA183_eof;
			this.min = DFA183_min;
			this.max = DFA183_max;
			this.accept = DFA183_accept;
			this.special = DFA183_special;
			this.transition = DFA183_transition;
		}
		@Override
		public String getDescription() {
			return "1426:1: relation[WhereClause.Builder clauses] : (name= cident type= relationType t= term |name= cident K_IS K_NOT K_NULL | K_TOKEN l= tupleOfIdentifiers type= relationType t= term |name= cident K_IN marker= inMarker |name= cident K_IN inValues= singleColumnInValues |name= cident K_CONTAINS ( K_KEY )? t= term |name= cident '[' key= term ']' type= relationType t= term |ids= tupleOfIdentifiers ( K_IN ( '(' ')' |tupleInMarker= inMarkerForTuple |literals= tupleOfTupleLiterals |markers= tupleOfMarkersForTuples ) |type= relationType literal= tupleLiteral |type= relationType tupleMarker= markerForTuple ) | '(' relation[$clauses] ')' );";
		}
	}

	static final String DFA182_eotS =
		"\12\uffff";
	static final String DFA182_eofS =
		"\12\uffff";
	static final String DFA182_minS =
		"\1\107\1\uffff\6\u009c\2\uffff";
	static final String DFA182_maxS =
		"\1\u00b7\1\uffff\6\u00b1\2\uffff";
	static final String DFA182_acceptS =
		"\1\uffff\1\1\6\uffff\1\2\1\3";
	static final String DFA182_specialS =
		"\12\uffff}>";
	static final String[] DFA182_transitionS = {
			"\1\1\142\uffff\1\7\10\uffff\1\3\1\4\1\2\1\5\1\6",
			"",
			"\1\11\16\uffff\1\10\5\uffff\1\11",
			"\1\11\16\uffff\1\10\5\uffff\1\11",
			"\1\11\16\uffff\1\10\5\uffff\1\11",
			"\1\11\16\uffff\1\10\5\uffff\1\11",
			"\1\11\16\uffff\1\10\5\uffff\1\11",
			"\1\11\16\uffff\1\10\5\uffff\1\11",
			"",
			""
	};

	static final short[] DFA182_eot = DFA.unpackEncodedString(DFA182_eotS);
	static final short[] DFA182_eof = DFA.unpackEncodedString(DFA182_eofS);
	static final char[] DFA182_min = DFA.unpackEncodedStringToUnsignedChars(DFA182_minS);
	static final char[] DFA182_max = DFA.unpackEncodedStringToUnsignedChars(DFA182_maxS);
	static final short[] DFA182_accept = DFA.unpackEncodedString(DFA182_acceptS);
	static final short[] DFA182_special = DFA.unpackEncodedString(DFA182_specialS);
	static final short[][] DFA182_transition;

	static {
		int numStates = DFA182_transitionS.length;
		DFA182_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA182_transition[i] = DFA.unpackEncodedString(DFA182_transitionS[i]);
		}
	}

	protected class DFA182 extends DFA {

		public DFA182(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 182;
			this.eot = DFA182_eot;
			this.eof = DFA182_eof;
			this.min = DFA182_min;
			this.max = DFA182_max;
			this.accept = DFA182_accept;
			this.special = DFA182_special;
			this.transition = DFA182_transition;
		}
		@Override
		public String getDescription() {
			return "1439:7: ( K_IN ( '(' ')' |tupleInMarker= inMarkerForTuple |literals= tupleOfTupleLiterals |markers= tupleOfMarkersForTuples ) |type= relationType literal= tupleLiteral |type= relationType tupleMarker= markerForTuple )";
		}
	}

	static final String DFA192_eotS =
		"\37\uffff";
	static final String DFA192_eofS =
		"\1\uffff\24\34\2\31\1\uffff\1\31\1\uffff\1\31\4\uffff";
	static final String DFA192_minS =
		"\1\24\26\76\1\uffff\1\76\1\uffff\1\76\4\uffff";
	static final String DFA192_maxS =
		"\1\u00a0\26\u00b6\1\uffff\1\u00b6\1\uffff\1\u00b6\4\uffff";
	static final String DFA192_acceptS =
		"\27\uffff\1\2\1\uffff\1\4\1\uffff\1\6\1\1\1\3\1\5";
	static final String DFA192_specialS =
		"\37\uffff}>";
	static final String[] DFA192_transitionS = {
			"\1\31\4\uffff\2\31\4\uffff\1\31\1\uffff\1\1\3\uffff\1\2\1\3\1\4\1\uffff"+
			"\2\31\1\uffff\3\31\1\5\1\uffff\1\31\1\23\1\6\3\uffff\1\31\1\7\3\uffff"+
			"\3\31\1\10\1\uffff\1\32\1\uffff\2\31\4\uffff\1\11\1\uffff\2\31\1\uffff"+
			"\1\12\2\uffff\3\31\1\uffff\2\31\1\uffff\1\26\1\31\1\25\3\uffff\1\31\1"+
			"\uffff\1\31\4\uffff\1\31\2\uffff\3\31\3\uffff\1\31\1\uffff\2\31\1\uffff"+
			"\1\27\1\31\1\13\4\31\1\14\1\24\1\15\1\22\1\16\2\uffff\1\31\1\uffff\1"+
			"\31\1\30\1\31\3\uffff\2\31\1\uffff\1\17\1\31\1\20\1\21\3\uffff\1\31\11"+
			"\uffff\1\31\2\uffff\1\33",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\12\uffff\1\34\24\uffff\1\34\12\uffff\1\34\65\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\31\14\uffff\1\31\12\uffff\1\31\24\uffff\1\31\12\uffff\1\31\65\uffff"+
			"\1\31\1\uffff\1\31\1\uffff\1\31\1\uffff\1\31\1\27\2\uffff\1\31",
			"\1\31\14\uffff\1\31\12\uffff\1\31\24\uffff\1\31\12\uffff\1\31\65\uffff"+
			"\1\31\1\uffff\1\31\1\uffff\1\31\1\uffff\1\31\1\27\2\uffff\1\31",
			"",
			"\1\31\14\uffff\1\31\12\uffff\1\31\24\uffff\1\31\12\uffff\1\31\65\uffff"+
			"\1\31\1\uffff\1\31\1\uffff\1\31\1\uffff\1\31\1\35\2\uffff\1\31",
			"",
			"\1\31\14\uffff\1\31\12\uffff\1\31\24\uffff\1\31\12\uffff\1\31\65\uffff"+
			"\1\31\1\uffff\1\31\1\uffff\1\31\1\uffff\1\31\1\36\2\uffff\1\31",
			"",
			"",
			"",
			""
	};

	static final short[] DFA192_eot = DFA.unpackEncodedString(DFA192_eotS);
	static final short[] DFA192_eof = DFA.unpackEncodedString(DFA192_eofS);
	static final char[] DFA192_min = DFA.unpackEncodedStringToUnsignedChars(DFA192_minS);
	static final char[] DFA192_max = DFA.unpackEncodedStringToUnsignedChars(DFA192_maxS);
	static final short[] DFA192_accept = DFA.unpackEncodedString(DFA192_acceptS);
	static final short[] DFA192_special = DFA.unpackEncodedString(DFA192_specialS);
	static final short[][] DFA192_transition;

	static {
		int numStates = DFA192_transitionS.length;
		DFA192_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA192_transition[i] = DFA.unpackEncodedString(DFA192_transitionS[i]);
		}
	}

	protected class DFA192 extends DFA {

		public DFA192(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 192;
			this.eot = DFA192_eot;
			this.eof = DFA192_eof;
			this.min = DFA192_min;
			this.max = DFA192_max;
			this.accept = DFA192_accept;
			this.special = DFA192_special;
			this.transition = DFA192_transition;
		}
		@Override
		public String getDescription() {
			return "1496:1: comparatorType returns [CQL3Type.Raw t] : (n= native_type |c= collection_type |tt= tuple_type |id= userTypeName | K_FROZEN '<' f= comparatorType '>' |s= STRING_LITERAL );";
		}
	}

	public static final BitSet FOLLOW_cqlStatement_in_query72 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0004000000000000L});
	public static final BitSet FOLLOW_178_in_query75 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0004000000000000L});
	public static final BitSet FOLLOW_EOF_in_query79 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_selectStatement_in_cqlStatement113 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insertStatement_in_cqlStatement142 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_updateStatement_in_cqlStatement171 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_batchStatement_in_cqlStatement200 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_deleteStatement_in_cqlStatement230 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_useStatement_in_cqlStatement259 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_truncateStatement_in_cqlStatement291 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_createKeyspaceStatement_in_cqlStatement318 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_createTableStatement_in_cqlStatement339 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_createIndexStatement_in_cqlStatement362 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dropKeyspaceStatement_in_cqlStatement385 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dropTableStatement_in_cqlStatement407 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dropIndexStatement_in_cqlStatement432 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_alterTableStatement_in_cqlStatement457 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_alterKeyspaceStatement_in_cqlStatement481 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_grantPermissionsStatement_in_cqlStatement502 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_revokePermissionsStatement_in_cqlStatement520 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_listPermissionsStatement_in_cqlStatement537 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_createUserStatement_in_cqlStatement556 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_alterUserStatement_in_cqlStatement580 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dropUserStatement_in_cqlStatement605 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_listUsersStatement_in_cqlStatement631 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_createTriggerStatement_in_cqlStatement656 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dropTriggerStatement_in_cqlStatement677 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_createTypeStatement_in_cqlStatement700 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_alterTypeStatement_in_cqlStatement724 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dropTypeStatement_in_cqlStatement749 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_createFunctionStatement_in_cqlStatement775 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dropFunctionStatement_in_cqlStatement795 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_createAggregateStatement_in_cqlStatement817 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dropAggregateStatement_in_cqlStatement836 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_createRoleStatement_in_cqlStatement857 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_alterRoleStatement_in_cqlStatement881 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dropRoleStatement_in_cqlStatement906 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_listRolesStatement_in_cqlStatement932 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_grantRoleStatement_in_cqlStatement957 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_revokeRoleStatement_in_cqlStatement982 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_createMaterializedViewStatement_in_cqlStatement1006 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dropMaterializedViewStatement_in_cqlStatement1018 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_alterMaterializedViewStatement_in_cqlStatement1032 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_USE_in_useStatement1058 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_keyspaceName_in_useStatement1062 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_SELECT_in_selectStatement1096 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x020000003008F63BL});
	public static final BitSet FOLLOW_K_JSON_in_selectStatement1107 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x020000003008F63BL});
	public static final BitSet FOLLOW_K_DISTINCT_in_selectStatement1124 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x020000003008F63BL});
	public static final BitSet FOLLOW_selectClause_in_selectStatement1133 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
	public static final BitSet FOLLOW_K_FROM_in_selectStatement1143 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_columnFamilyName_in_selectStatement1147 = new BitSet(new long[]{0x0000000008000002L,0x0000008000800000L,0x0000000000020000L});
	public static final BitSet FOLLOW_K_WHERE_in_selectStatement1157 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x080008002008F63BL});
	public static final BitSet FOLLOW_whereClause_in_selectStatement1161 = new BitSet(new long[]{0x0000000008000002L,0x0000008000800000L});
	public static final BitSet FOLLOW_K_ORDER_in_selectStatement1174 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_K_BY_in_selectStatement1176 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_orderByClause_in_selectStatement1178 = new BitSet(new long[]{0x0000000008000002L,0x0000000000800000L,0x0000400000000000L});
	public static final BitSet FOLLOW_174_in_selectStatement1183 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_orderByClause_in_selectStatement1185 = new BitSet(new long[]{0x0000000008000002L,0x0000000000800000L,0x0000400000000000L});
	public static final BitSet FOLLOW_K_LIMIT_in_selectStatement1202 = new BitSet(new long[]{0x0000000008200000L,0x0000000000000000L,0x0002000010000000L});
	public static final BitSet FOLLOW_intValue_in_selectStatement1206 = new BitSet(new long[]{0x0000000008000002L});
	public static final BitSet FOLLOW_K_ALLOW_in_selectStatement1221 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_K_FILTERING_in_selectStatement1223 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_selector_in_selectClause1260 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_174_in_selectClause1265 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63BL});
	public static final BitSet FOLLOW_selector_in_selectClause1269 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_185_in_selectClause1281 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unaliasedSelector_in_selector1314 = new BitSet(new long[]{0x0000000080000002L});
	public static final BitSet FOLLOW_K_AS_in_selector1317 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_noncol_ident_in_selector1321 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_unaliasedSelector1362 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_K_COUNT_in_unaliasedSelector1408 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_unaliasedSelector1410 = new BitSet(new long[]{0x0000000000200000L,0x0000000000000000L,0x0200000000000000L});
	public static final BitSet FOLLOW_countArgument_in_unaliasedSelector1412 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_172_in_unaliasedSelector1414 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_K_WRITETIME_in_unaliasedSelector1439 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_unaliasedSelector1441 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_unaliasedSelector1445 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_172_in_unaliasedSelector1447 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_K_TTL_in_unaliasedSelector1473 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_unaliasedSelector1481 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_unaliasedSelector1485 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_172_in_unaliasedSelector1487 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_functionName_in_unaliasedSelector1515 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_selectionFunctionArgs_in_unaliasedSelector1519 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_176_in_unaliasedSelector1534 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_unaliasedSelector1538 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_171_in_selectionFunctionArgs1566 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_172_in_selectionFunctionArgs1568 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_171_in_selectionFunctionArgs1578 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63BL});
	public static final BitSet FOLLOW_unaliasedSelector_in_selectionFunctionArgs1582 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_selectionFunctionArgs1598 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63BL});
	public static final BitSet FOLLOW_unaliasedSelector_in_selectionFunctionArgs1602 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_selectionFunctionArgs1615 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_185_in_countArgument1634 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGER_in_countArgument1644 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_relationOrExpression_in_whereClause1675 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_AND_in_whereClause1679 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x080008002008F63BL});
	public static final BitSet FOLLOW_relationOrExpression_in_whereClause1681 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_relation_in_relationOrExpression1703 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_customIndexExpression_in_relationOrExpression1712 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_187_in_customIndexExpression1740 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_idxName_in_customIndexExpression1742 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_174_in_customIndexExpression1745 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_customIndexExpression1749 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_172_in_customIndexExpression1751 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_orderByClause1781 = new BitSet(new long[]{0x0020000100000002L});
	public static final BitSet FOLLOW_K_ASC_in_orderByClause1784 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DESC_in_orderByClause1788 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_INSERT_in_insertStatement1817 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
	public static final BitSet FOLLOW_K_INTO_in_insertStatement1819 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_columnFamilyName_in_insertStatement1823 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L,0x0000080000000000L});
	public static final BitSet FOLLOW_normalInsertStatement_in_insertStatement1837 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_JSON_in_insertStatement1852 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L,0x0002000110000800L});
	public static final BitSet FOLLOW_jsonInsertStatement_in_insertStatement1856 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_171_in_normalInsertStatement1892 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_normalInsertStatement1896 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_normalInsertStatement1903 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_normalInsertStatement1907 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_normalInsertStatement1914 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_K_VALUES_in_normalInsertStatement1922 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_normalInsertStatement1930 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_normalInsertStatement1934 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_normalInsertStatement1940 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_normalInsertStatement1944 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_normalInsertStatement1951 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L,0x0000000000000800L});
	public static final BitSet FOLLOW_K_IF_in_normalInsertStatement1961 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
	public static final BitSet FOLLOW_K_NOT_in_normalInsertStatement1963 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_normalInsertStatement1965 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000800L});
	public static final BitSet FOLLOW_usingClause_in_normalInsertStatement1980 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_jsonValue_in_jsonInsertStatement2026 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L,0x0000000000000800L});
	public static final BitSet FOLLOW_K_IF_in_jsonInsertStatement2036 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
	public static final BitSet FOLLOW_K_NOT_in_jsonInsertStatement2038 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_jsonInsertStatement2040 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000800L});
	public static final BitSet FOLLOW_usingClause_in_jsonInsertStatement2055 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_jsonValue2096 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_177_in_jsonValue2106 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_noncol_ident_in_jsonValue2110 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_jsonValue2124 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_USING_in_usingClause2155 = new BitSet(new long[]{0x0000000000000000L,0x1000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_usingClauseObjective_in_usingClause2157 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_AND_in_usingClause2162 = new BitSet(new long[]{0x0000000000000000L,0x1000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_usingClauseObjective_in_usingClause2164 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_TIMESTAMP_in_usingClauseObjective2186 = new BitSet(new long[]{0x0000000000200000L,0x0000000000000000L,0x0002000010000000L});
	public static final BitSet FOLLOW_intValue_in_usingClauseObjective2190 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TTL_in_usingClauseObjective2200 = new BitSet(new long[]{0x0000000000200000L,0x0000000000000000L,0x0002000010000000L});
	public static final BitSet FOLLOW_intValue_in_usingClauseObjective2204 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_UPDATE_in_updateStatement2238 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_columnFamilyName_in_updateStatement2242 = new BitSet(new long[]{0x0000000000000000L,0x0008000000000000L,0x0000000000000800L});
	public static final BitSet FOLLOW_usingClause_in_updateStatement2252 = new BitSet(new long[]{0x0000000000000000L,0x0008000000000000L});
	public static final BitSet FOLLOW_K_SET_in_updateStatement2264 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_columnOperation_in_updateStatement2266 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000400000020000L});
	public static final BitSet FOLLOW_174_in_updateStatement2270 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_columnOperation_in_updateStatement2272 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000400000020000L});
	public static final BitSet FOLLOW_K_WHERE_in_updateStatement2283 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x080008002008F63BL});
	public static final BitSet FOLLOW_whereClause_in_updateStatement2287 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
	public static final BitSet FOLLOW_K_IF_in_updateStatement2297 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_K_EXISTS_in_updateStatement2301 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_updateConditions_in_updateStatement2309 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_columnCondition_in_updateConditions2351 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_AND_in_updateConditions2356 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_columnCondition_in_updateConditions2358 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_DELETE_in_deleteStatement2395 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1BL,0x000000002008F63AL});
	public static final BitSet FOLLOW_deleteSelection_in_deleteStatement2401 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
	public static final BitSet FOLLOW_K_FROM_in_deleteStatement2414 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_columnFamilyName_in_deleteStatement2418 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000020800L});
	public static final BitSet FOLLOW_usingClauseDelete_in_deleteStatement2428 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
	public static final BitSet FOLLOW_K_WHERE_in_deleteStatement2440 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x080008002008F63BL});
	public static final BitSet FOLLOW_whereClause_in_deleteStatement2444 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
	public static final BitSet FOLLOW_K_IF_in_deleteStatement2454 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_K_EXISTS_in_deleteStatement2458 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_updateConditions_in_deleteStatement2466 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_deleteOp_in_deleteSelection2513 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_174_in_deleteSelection2528 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_deleteOp_in_deleteSelection2532 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_cident_in_deleteOp2559 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_deleteOp2586 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0100000000000000L});
	public static final BitSet FOLLOW_184_in_deleteOp2588 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_deleteOp2592 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0400000000000000L});
	public static final BitSet FOLLOW_186_in_deleteOp2594 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_USING_in_usingClauseDelete2614 = new BitSet(new long[]{0x0000000000000000L,0x1000000000000000L});
	public static final BitSet FOLLOW_K_TIMESTAMP_in_usingClauseDelete2616 = new BitSet(new long[]{0x0000000000200000L,0x0000000000000000L,0x0002000010000000L});
	public static final BitSet FOLLOW_intValue_in_usingClauseDelete2620 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_BEGIN_in_batchStatement2654 = new BitSet(new long[]{0x0000800800000000L,0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_K_UNLOGGED_in_batchStatement2664 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_K_COUNTER_in_batchStatement2670 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_K_BATCH_in_batchStatement2683 = new BitSet(new long[]{0x0010000040000000L,0x0000000000002000L,0x0000000000000880L});
	public static final BitSet FOLLOW_usingClause_in_batchStatement2687 = new BitSet(new long[]{0x0010000040000000L,0x0000000000002000L,0x0000000000000080L});
	public static final BitSet FOLLOW_batchStatementObjective_in_batchStatement2707 = new BitSet(new long[]{0x0010000040000000L,0x0000000000002000L,0x0004000000000080L});
	public static final BitSet FOLLOW_178_in_batchStatement2709 = new BitSet(new long[]{0x0010000040000000L,0x0000000000002000L,0x0000000000000080L});
	public static final BitSet FOLLOW_K_APPLY_in_batchStatement2723 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_K_BATCH_in_batchStatement2725 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insertStatement_in_batchStatementObjective2756 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_updateStatement_in_batchStatementObjective2769 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_deleteStatement_in_batchStatementObjective2782 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CREATE_in_createAggregateStatement2815 = new BitSet(new long[]{0x0000000002000000L,0x0000004000000000L});
	public static final BitSet FOLLOW_K_OR_in_createAggregateStatement2818 = new BitSet(new long[]{0x0000000000000000L,0x0000200000000000L});
	public static final BitSet FOLLOW_K_REPLACE_in_createAggregateStatement2820 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_K_AGGREGATE_in_createAggregateStatement2832 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A5AL,0x000000003008F63BL});
	public static final BitSet FOLLOW_K_IF_in_createAggregateStatement2841 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
	public static final BitSet FOLLOW_K_NOT_in_createAggregateStatement2843 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_createAggregateStatement2845 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63BL});
	public static final BitSet FOLLOW_functionName_in_createAggregateStatement2859 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_createAggregateStatement2867 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000010012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_createAggregateStatement2891 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_createAggregateStatement2907 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_createAggregateStatement2911 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_createAggregateStatement2935 = new BitSet(new long[]{0x0000000000000000L,0x0010000000000000L});
	public static final BitSet FOLLOW_K_SFUNC_in_createAggregateStatement2943 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476A5A1AL,0x000000002000F633L});
	public static final BitSet FOLLOW_allowedFunctionName_in_createAggregateStatement2949 = new BitSet(new long[]{0x0000000000000000L,0x0100000000000000L});
	public static final BitSet FOLLOW_K_STYPE_in_createAggregateStatement2957 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_createAggregateStatement2963 = new BitSet(new long[]{0x4000000000000002L,0x0000000000000800L});
	public static final BitSet FOLLOW_K_FINALFUNC_in_createAggregateStatement2981 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476A5A1AL,0x000000002000F633L});
	public static final BitSet FOLLOW_allowedFunctionName_in_createAggregateStatement2987 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000800L});
	public static final BitSet FOLLOW_K_INITCOND_in_createAggregateStatement3014 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_createAggregateStatement3020 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_dropAggregateStatement3067 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_K_AGGREGATE_in_dropAggregateStatement3069 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A5AL,0x000000003008F63BL});
	public static final BitSet FOLLOW_K_IF_in_dropAggregateStatement3078 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_dropAggregateStatement3080 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63BL});
	public static final BitSet FOLLOW_functionName_in_dropAggregateStatement3095 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_dropAggregateStatement3113 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000010012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_dropAggregateStatement3141 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_dropAggregateStatement3159 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_dropAggregateStatement3163 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_dropAggregateStatement3191 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CREATE_in_createFunctionStatement3248 = new BitSet(new long[]{0x0000000000000000L,0x0000004000000008L});
	public static final BitSet FOLLOW_K_OR_in_createFunctionStatement3251 = new BitSet(new long[]{0x0000000000000000L,0x0000200000000000L});
	public static final BitSet FOLLOW_K_REPLACE_in_createFunctionStatement3253 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_K_FUNCTION_in_createFunctionStatement3265 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A5AL,0x000000003008F63BL});
	public static final BitSet FOLLOW_K_IF_in_createFunctionStatement3274 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
	public static final BitSet FOLLOW_K_NOT_in_createFunctionStatement3276 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_createFunctionStatement3278 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63BL});
	public static final BitSet FOLLOW_functionName_in_createFunctionStatement3292 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_createFunctionStatement3300 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000010002008F63AL});
	public static final BitSet FOLLOW_noncol_ident_in_createFunctionStatement3324 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_createFunctionStatement3328 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_createFunctionStatement3344 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_noncol_ident_in_createFunctionStatement3348 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_createFunctionStatement3352 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_createFunctionStatement3376 = new BitSet(new long[]{0x0000020000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_K_RETURNS_in_createFunctionStatement3387 = new BitSet(new long[]{0x0000000000000000L,0x0000000400000000L});
	public static final BitSet FOLLOW_K_NULL_in_createFunctionStatement3389 = new BitSet(new long[]{0x0000000000000000L,0x0000001000000000L});
	public static final BitSet FOLLOW_K_CALLED_in_createFunctionStatement3395 = new BitSet(new long[]{0x0000000000000000L,0x0000001000000000L});
	public static final BitSet FOLLOW_K_ON_in_createFunctionStatement3401 = new BitSet(new long[]{0x0000000000000000L,0x0000000400000000L});
	public static final BitSet FOLLOW_K_NULL_in_createFunctionStatement3403 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
	public static final BitSet FOLLOW_K_INPUT_in_createFunctionStatement3405 = new BitSet(new long[]{0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_K_RETURNS_in_createFunctionStatement3413 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_createFunctionStatement3419 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
	public static final BitSet FOLLOW_K_LANGUAGE_in_createFunctionStatement3427 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_IDENT_in_createFunctionStatement3433 = new BitSet(new long[]{0x0000000080000000L});
	public static final BitSet FOLLOW_K_AS_in_createFunctionStatement3441 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000100000000L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_createFunctionStatement3447 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_dropFunctionStatement3485 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_K_FUNCTION_in_dropFunctionStatement3487 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A5AL,0x000000003008F63BL});
	public static final BitSet FOLLOW_K_IF_in_dropFunctionStatement3496 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_dropFunctionStatement3498 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63BL});
	public static final BitSet FOLLOW_functionName_in_dropFunctionStatement3513 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_dropFunctionStatement3531 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000010012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_dropFunctionStatement3559 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_dropFunctionStatement3577 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_dropFunctionStatement3581 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_dropFunctionStatement3609 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CREATE_in_createKeyspaceStatement3668 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
	public static final BitSet FOLLOW_K_KEYSPACE_in_createKeyspaceStatement3670 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A5AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_K_IF_in_createKeyspaceStatement3673 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
	public static final BitSet FOLLOW_K_NOT_in_createKeyspaceStatement3675 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_createKeyspaceStatement3677 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_keyspaceName_in_createKeyspaceStatement3686 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000040000L});
	public static final BitSet FOLLOW_K_WITH_in_createKeyspaceStatement3694 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_properties_in_createKeyspaceStatement3696 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CREATE_in_createTableStatement3731 = new BitSet(new long[]{0x0000080000000000L});
	public static final BitSet FOLLOW_K_COLUMNFAMILY_in_createTableStatement3733 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A5AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_K_IF_in_createTableStatement3736 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
	public static final BitSet FOLLOW_K_NOT_in_createTableStatement3738 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_createTableStatement3740 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_columnFamilyName_in_createTableStatement3755 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_cfamDefinition_in_createTableStatement3765 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_171_in_cfamDefinition3784 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34F21476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cfamColumns_in_cfamDefinition3786 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_cfamDefinition3791 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34F21476E5A1AL,0x000050002008F63AL});
	public static final BitSet FOLLOW_cfamColumns_in_cfamDefinition3793 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_cfamDefinition3800 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000040000L});
	public static final BitSet FOLLOW_K_WITH_in_cfamDefinition3810 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cfamProperty_in_cfamDefinition3812 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_AND_in_cfamDefinition3817 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cfamProperty_in_cfamDefinition3819 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_ident_in_cfamColumns3845 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_cfamColumns3849 = new BitSet(new long[]{0x0000000000000002L,0x0040080000000000L});
	public static final BitSet FOLLOW_K_STATIC_in_cfamColumns3854 = new BitSet(new long[]{0x0000000000000002L,0x0000080000000000L});
	public static final BitSet FOLLOW_K_PRIMARY_in_cfamColumns3871 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
	public static final BitSet FOLLOW_K_KEY_in_cfamColumns3873 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_PRIMARY_in_cfamColumns3885 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
	public static final BitSet FOLLOW_K_KEY_in_cfamColumns3887 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_cfamColumns3889 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000008002008F63AL});
	public static final BitSet FOLLOW_pkDef_in_cfamColumns3891 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_cfamColumns3895 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_ident_in_cfamColumns3899 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_cfamColumns3906 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ident_in_pkDef3926 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_171_in_pkDef3936 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_ident_in_pkDef3942 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_pkDef3948 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_ident_in_pkDef3952 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_pkDef3959 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_property_in_cfamProperty3979 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_COMPACT_in_cfamProperty3988 = new BitSet(new long[]{0x0000000000000000L,0x0080000000000000L});
	public static final BitSet FOLLOW_K_STORAGE_in_cfamProperty3990 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CLUSTERING_in_cfamProperty4000 = new BitSet(new long[]{0x0000000000000000L,0x0000008000000000L});
	public static final BitSet FOLLOW_K_ORDER_in_cfamProperty4002 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_K_BY_in_cfamProperty4004 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_cfamProperty4006 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cfamOrdering_in_cfamProperty4008 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_cfamProperty4012 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cfamOrdering_in_cfamProperty4014 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_cfamProperty4019 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ident_in_cfamOrdering4047 = new BitSet(new long[]{0x0020000100000000L});
	public static final BitSet FOLLOW_K_ASC_in_cfamOrdering4050 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DESC_in_cfamOrdering4054 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CREATE_in_createTypeStatement4093 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000020L});
	public static final BitSet FOLLOW_K_TYPE_in_createTypeStatement4095 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A5AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_K_IF_in_createTypeStatement4098 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
	public static final BitSet FOLLOW_K_NOT_in_createTypeStatement4100 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_createTypeStatement4102 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_userTypeName_in_createTypeStatement4120 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_createTypeStatement4133 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_typeColumns_in_createTypeStatement4135 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_createTypeStatement4140 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000050002008F63AL});
	public static final BitSet FOLLOW_typeColumns_in_createTypeStatement4142 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_createTypeStatement4149 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_noncol_ident_in_typeColumns4169 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_typeColumns4173 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CREATE_in_createIndexStatement4208 = new BitSet(new long[]{0x0002000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_K_CUSTOM_in_createIndexStatement4211 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_K_INDEX_in_createIndexStatement4217 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34731476E5A5AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_K_IF_in_createIndexStatement4220 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
	public static final BitSet FOLLOW_K_NOT_in_createIndexStatement4222 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_createIndexStatement4224 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34731476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_idxName_in_createIndexStatement4240 = new BitSet(new long[]{0x0000000000000000L,0x0000001000000000L});
	public static final BitSet FOLLOW_K_ON_in_createIndexStatement4245 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_columnFamilyName_in_createIndexStatement4249 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_createIndexStatement4251 = new BitSet(new long[]{0xF58EF6E286100000L,0x7FF34721476E5A1EL,0x000010002008F63AL});
	public static final BitSet FOLLOW_indexIdent_in_createIndexStatement4254 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_createIndexStatement4258 = new BitSet(new long[]{0xF58EF6E286100000L,0x7FF34721476E5A1EL,0x000000002008F63AL});
	public static final BitSet FOLLOW_indexIdent_in_createIndexStatement4260 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_createIndexStatement4267 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000040800L});
	public static final BitSet FOLLOW_K_USING_in_createIndexStatement4278 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000100000000L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_createIndexStatement4282 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000040000L});
	public static final BitSet FOLLOW_K_WITH_in_createIndexStatement4297 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_properties_in_createIndexStatement4299 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_indexIdent4331 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_VALUES_in_indexIdent4359 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_indexIdent4361 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_indexIdent4365 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_172_in_indexIdent4367 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_KEYS_in_indexIdent4378 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_indexIdent4380 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_indexIdent4384 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_172_in_indexIdent4386 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ENTRIES_in_indexIdent4399 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_indexIdent4401 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_indexIdent4405 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_172_in_indexIdent4407 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_FULL_in_indexIdent4417 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_indexIdent4419 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_indexIdent4423 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_172_in_indexIdent4425 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CREATE_in_createMaterializedViewStatement4462 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_K_MATERIALIZED_in_createMaterializedViewStatement4464 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000010000L});
	public static final BitSet FOLLOW_K_VIEW_in_createMaterializedViewStatement4466 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A5AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_K_IF_in_createMaterializedViewStatement4469 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
	public static final BitSet FOLLOW_K_NOT_in_createMaterializedViewStatement4471 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_createMaterializedViewStatement4473 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_columnFamilyName_in_createMaterializedViewStatement4481 = new BitSet(new long[]{0x0000000080000000L});
	public static final BitSet FOLLOW_K_AS_in_createMaterializedViewStatement4483 = new BitSet(new long[]{0x0000000000000000L,0x0004000000000000L});
	public static final BitSet FOLLOW_K_SELECT_in_createMaterializedViewStatement4493 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x020000003008F63BL});
	public static final BitSet FOLLOW_selectClause_in_createMaterializedViewStatement4497 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
	public static final BitSet FOLLOW_K_FROM_in_createMaterializedViewStatement4499 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_columnFamilyName_in_createMaterializedViewStatement4503 = new BitSet(new long[]{0x0000000000000000L,0x0000080000000000L,0x0000000000020000L});
	public static final BitSet FOLLOW_K_WHERE_in_createMaterializedViewStatement4514 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x080008002008F63BL});
	public static final BitSet FOLLOW_whereClause_in_createMaterializedViewStatement4518 = new BitSet(new long[]{0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_K_PRIMARY_in_createMaterializedViewStatement4530 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
	public static final BitSet FOLLOW_K_KEY_in_createMaterializedViewStatement4532 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_createMaterializedViewStatement4544 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_createMaterializedViewStatement4546 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_createMaterializedViewStatement4550 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_createMaterializedViewStatement4556 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_createMaterializedViewStatement4560 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_createMaterializedViewStatement4567 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_createMaterializedViewStatement4571 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_createMaterializedViewStatement4575 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_createMaterializedViewStatement4582 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000040000L});
	public static final BitSet FOLLOW_171_in_createMaterializedViewStatement4592 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_createMaterializedViewStatement4596 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_createMaterializedViewStatement4602 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_createMaterializedViewStatement4606 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_createMaterializedViewStatement4613 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000040000L});
	public static final BitSet FOLLOW_K_WITH_in_createMaterializedViewStatement4645 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cfamProperty_in_createMaterializedViewStatement4647 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_AND_in_createMaterializedViewStatement4652 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cfamProperty_in_createMaterializedViewStatement4654 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_CREATE_in_createTriggerStatement4692 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_K_TRIGGER_in_createTriggerStatement4694 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A5AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_K_IF_in_createTriggerStatement4697 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
	public static final BitSet FOLLOW_K_NOT_in_createTriggerStatement4699 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_createTriggerStatement4701 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_createTriggerStatement4711 = new BitSet(new long[]{0x0000000000000000L,0x0000001000000000L});
	public static final BitSet FOLLOW_K_ON_in_createTriggerStatement4722 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_columnFamilyName_in_createTriggerStatement4726 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000800L});
	public static final BitSet FOLLOW_K_USING_in_createTriggerStatement4728 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000100000000L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_createTriggerStatement4732 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_dropTriggerStatement4773 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_K_TRIGGER_in_dropTriggerStatement4775 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A5AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_K_IF_in_dropTriggerStatement4778 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_dropTriggerStatement4780 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_dropTriggerStatement4790 = new BitSet(new long[]{0x0000000000000000L,0x0000001000000000L});
	public static final BitSet FOLLOW_K_ON_in_dropTriggerStatement4793 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_columnFamilyName_in_dropTriggerStatement4797 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ALTER_in_alterKeyspaceStatement4837 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
	public static final BitSet FOLLOW_K_KEYSPACE_in_alterKeyspaceStatement4839 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_keyspaceName_in_alterKeyspaceStatement4843 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000040000L});
	public static final BitSet FOLLOW_K_WITH_in_alterKeyspaceStatement4853 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_properties_in_alterKeyspaceStatement4855 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ALTER_in_alterTableStatement4891 = new BitSet(new long[]{0x0000080000000000L});
	public static final BitSet FOLLOW_K_COLUMNFAMILY_in_alterTableStatement4893 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_columnFamilyName_in_alterTableStatement4897 = new BitSet(new long[]{0x0200000011000000L,0x0000100000000000L,0x0000000000040000L});
	public static final BitSet FOLLOW_K_ALTER_in_alterTableStatement4911 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_alterTableStatement4915 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000020L});
	public static final BitSet FOLLOW_K_TYPE_in_alterTableStatement4917 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_alterTableStatement4921 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ADD_in_alterTableStatement4937 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_alterTableStatement4943 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_alterTableStatement4947 = new BitSet(new long[]{0x0000000000000002L,0x0040000000000000L});
	public static final BitSet FOLLOW_K_STATIC_in_alterTableStatement4952 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_alterTableStatement4970 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_alterTableStatement4975 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_alterTableStatement5021 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_alterTableStatement5026 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000800L});
	public static final BitSet FOLLOW_K_USING_in_alterTableStatement5028 = new BitSet(new long[]{0x0000000000000000L,0x1000000000000000L});
	public static final BitSet FOLLOW_K_TIMESTAMP_in_alterTableStatement5030 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_INTEGER_in_alterTableStatement5034 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_WITH_in_alterTableStatement5050 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_properties_in_alterTableStatement5053 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_RENAME_in_alterTableStatement5092 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_alterTableStatement5152 = new BitSet(new long[]{0x0000000000000000L,0x8000000000000000L});
	public static final BitSet FOLLOW_K_TO_in_alterTableStatement5154 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_alterTableStatement5158 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_AND_in_alterTableStatement5179 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_alterTableStatement5183 = new BitSet(new long[]{0x0000000000000000L,0x8000000000000000L});
	public static final BitSet FOLLOW_K_TO_in_alterTableStatement5185 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_alterTableStatement5189 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_ALTER_in_alterMaterializedViewStatement5242 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_K_MATERIALIZED_in_alterMaterializedViewStatement5244 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000010000L});
	public static final BitSet FOLLOW_K_VIEW_in_alterMaterializedViewStatement5246 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_columnFamilyName_in_alterMaterializedViewStatement5250 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000040000L});
	public static final BitSet FOLLOW_K_WITH_in_alterMaterializedViewStatement5262 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_properties_in_alterMaterializedViewStatement5264 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ALTER_in_alterTypeStatement5299 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000020L});
	public static final BitSet FOLLOW_K_TYPE_in_alterTypeStatement5301 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_userTypeName_in_alterTypeStatement5305 = new BitSet(new long[]{0x0000000011000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_K_ALTER_in_alterTypeStatement5319 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_noncol_ident_in_alterTypeStatement5323 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000020L});
	public static final BitSet FOLLOW_K_TYPE_in_alterTypeStatement5325 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_alterTypeStatement5329 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ADD_in_alterTypeStatement5345 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_noncol_ident_in_alterTypeStatement5351 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_alterTypeStatement5355 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_RENAME_in_alterTypeStatement5378 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_noncol_ident_in_alterTypeStatement5416 = new BitSet(new long[]{0x0000000000000000L,0x8000000000000000L});
	public static final BitSet FOLLOW_K_TO_in_alterTypeStatement5418 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_noncol_ident_in_alterTypeStatement5422 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_AND_in_alterTypeStatement5445 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_noncol_ident_in_alterTypeStatement5449 = new BitSet(new long[]{0x0000000000000000L,0x8000000000000000L});
	public static final BitSet FOLLOW_K_TO_in_alterTypeStatement5451 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_noncol_ident_in_alterTypeStatement5455 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_DROP_in_dropKeyspaceStatement5522 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
	public static final BitSet FOLLOW_K_KEYSPACE_in_dropKeyspaceStatement5524 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A5AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_K_IF_in_dropKeyspaceStatement5527 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_dropKeyspaceStatement5529 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_keyspaceName_in_dropKeyspaceStatement5538 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_dropTableStatement5572 = new BitSet(new long[]{0x0000080000000000L});
	public static final BitSet FOLLOW_K_COLUMNFAMILY_in_dropTableStatement5574 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A5AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_K_IF_in_dropTableStatement5577 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_dropTableStatement5579 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_columnFamilyName_in_dropTableStatement5588 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_dropTypeStatement5622 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000020L});
	public static final BitSet FOLLOW_K_TYPE_in_dropTypeStatement5624 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A5AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_K_IF_in_dropTypeStatement5627 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_dropTypeStatement5629 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_userTypeName_in_dropTypeStatement5638 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_dropIndexStatement5672 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_K_INDEX_in_dropIndexStatement5674 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A5AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_K_IF_in_dropIndexStatement5677 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_dropIndexStatement5679 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_indexName_in_dropIndexStatement5688 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_dropMaterializedViewStatement5728 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_K_MATERIALIZED_in_dropMaterializedViewStatement5730 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000010000L});
	public static final BitSet FOLLOW_K_VIEW_in_dropMaterializedViewStatement5732 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A5AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_K_IF_in_dropMaterializedViewStatement5735 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_dropMaterializedViewStatement5737 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_columnFamilyName_in_dropMaterializedViewStatement5746 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TRUNCATE_in_truncateStatement5777 = new BitSet(new long[]{0xF18EFEE286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_K_COLUMNFAMILY_in_truncateStatement5780 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_columnFamilyName_in_truncateStatement5786 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_GRANT_in_grantPermissionsStatement5811 = new BitSet(new long[]{0x0A41000414000000L,0x0004000010000000L});
	public static final BitSet FOLLOW_permissionOrAll_in_grantPermissionsStatement5823 = new BitSet(new long[]{0x0000000000000000L,0x0000001000000000L});
	public static final BitSet FOLLOW_K_ON_in_grantPermissionsStatement5831 = new BitSet(new long[]{0xF18EFEE286100000L,0x7FF34721477E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_resource_in_grantPermissionsStatement5843 = new BitSet(new long[]{0x0000000000000000L,0x8000000000000000L});
	public static final BitSet FOLLOW_K_TO_in_grantPermissionsStatement5851 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000013008F63AL});
	public static final BitSet FOLLOW_userOrRoleName_in_grantPermissionsStatement5865 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_REVOKE_in_revokePermissionsStatement5896 = new BitSet(new long[]{0x0A41000414000000L,0x0004000010000000L});
	public static final BitSet FOLLOW_permissionOrAll_in_revokePermissionsStatement5908 = new BitSet(new long[]{0x0000000000000000L,0x0000001000000000L});
	public static final BitSet FOLLOW_K_ON_in_revokePermissionsStatement5916 = new BitSet(new long[]{0xF18EFEE286100000L,0x7FF34721477E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_resource_in_revokePermissionsStatement5928 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
	public static final BitSet FOLLOW_K_FROM_in_revokePermissionsStatement5936 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000013008F63AL});
	public static final BitSet FOLLOW_userOrRoleName_in_revokePermissionsStatement5950 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_GRANT_in_grantRoleStatement5981 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000013008F63AL});
	public static final BitSet FOLLOW_userOrRoleName_in_grantRoleStatement5995 = new BitSet(new long[]{0x0000000000000000L,0x8000000000000000L});
	public static final BitSet FOLLOW_K_TO_in_grantRoleStatement6003 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000013008F63AL});
	public static final BitSet FOLLOW_userOrRoleName_in_grantRoleStatement6017 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_REVOKE_in_revokeRoleStatement6048 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000013008F63AL});
	public static final BitSet FOLLOW_userOrRoleName_in_revokeRoleStatement6062 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
	public static final BitSet FOLLOW_K_FROM_in_revokeRoleStatement6070 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000013008F63AL});
	public static final BitSet FOLLOW_userOrRoleName_in_revokeRoleStatement6084 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_LIST_in_listPermissionsStatement6122 = new BitSet(new long[]{0x0A41000414000000L,0x0004000010000000L});
	public static final BitSet FOLLOW_permissionOrAll_in_listPermissionsStatement6134 = new BitSet(new long[]{0x0000000000000002L,0x0000001880000000L});
	public static final BitSet FOLLOW_K_ON_in_listPermissionsStatement6144 = new BitSet(new long[]{0xF18EFEE286100000L,0x7FF34721477E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_resource_in_listPermissionsStatement6146 = new BitSet(new long[]{0x0000000000000002L,0x0000000880000000L});
	public static final BitSet FOLLOW_K_OF_in_listPermissionsStatement6161 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000013008F63AL});
	public static final BitSet FOLLOW_roleName_in_listPermissionsStatement6163 = new BitSet(new long[]{0x0000000000000002L,0x0000000080000000L});
	public static final BitSet FOLLOW_K_NORECURSIVE_in_listPermissionsStatement6177 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_permission6213 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ALL_in_permissionOrAll6270 = new BitSet(new long[]{0x0000000000000002L,0x0000040000000000L});
	public static final BitSet FOLLOW_K_PERMISSIONS_in_permissionOrAll6274 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_permission_in_permissionOrAll6295 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000000L});
	public static final BitSet FOLLOW_K_PERMISSION_in_permissionOrAll6299 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dataResource_in_resource6327 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_roleResource_in_resource6339 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_functionResource_in_resource6351 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ALL_in_dataResource6374 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
	public static final BitSet FOLLOW_K_KEYSPACES_in_dataResource6376 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_KEYSPACE_in_dataResource6386 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_keyspaceName_in_dataResource6392 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_COLUMNFAMILY_in_dataResource6404 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_columnFamilyName_in_dataResource6413 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ALL_in_roleResource6442 = new BitSet(new long[]{0x0000000000000000L,0x0002000000000000L});
	public static final BitSet FOLLOW_K_ROLES_in_roleResource6444 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ROLE_in_roleResource6454 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000013008F63AL});
	public static final BitSet FOLLOW_userOrRoleName_in_roleResource6460 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ALL_in_functionResource6492 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_K_FUNCTIONS_in_functionResource6494 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ALL_in_functionResource6504 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_K_FUNCTIONS_in_functionResource6506 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_K_IN_in_functionResource6508 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
	public static final BitSet FOLLOW_K_KEYSPACE_in_functionResource6510 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_keyspaceName_in_functionResource6516 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_FUNCTION_in_functionResource6531 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63BL});
	public static final BitSet FOLLOW_functionName_in_functionResource6535 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_functionResource6553 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000010012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_functionResource6581 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_functionResource6599 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_functionResource6603 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_functionResource6631 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CREATE_in_createUserStatement6679 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000200L});
	public static final BitSet FOLLOW_K_USER_in_createUserStatement6681 = new BitSet(new long[]{0x0000000000100000L,0x0000000000000040L,0x0000000120000000L});
	public static final BitSet FOLLOW_K_IF_in_createUserStatement6684 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
	public static final BitSet FOLLOW_K_NOT_in_createUserStatement6686 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_createUserStatement6688 = new BitSet(new long[]{0x0000000000100000L,0x0000000000000000L,0x0000000120000000L});
	public static final BitSet FOLLOW_username_in_createUserStatement6696 = new BitSet(new long[]{0x0000000000000002L,0x0200000100000000L,0x0000000000040000L});
	public static final BitSet FOLLOW_K_WITH_in_createUserStatement6708 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_userPassword_in_createUserStatement6710 = new BitSet(new long[]{0x0000000000000002L,0x0200000100000000L});
	public static final BitSet FOLLOW_K_SUPERUSER_in_createUserStatement6724 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_NOSUPERUSER_in_createUserStatement6730 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ALTER_in_alterUserStatement6775 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000200L});
	public static final BitSet FOLLOW_K_USER_in_alterUserStatement6777 = new BitSet(new long[]{0x0000000000100000L,0x0000000000000000L,0x0000000120000000L});
	public static final BitSet FOLLOW_username_in_alterUserStatement6781 = new BitSet(new long[]{0x0000000000000002L,0x0200000100000000L,0x0000000000040000L});
	public static final BitSet FOLLOW_K_WITH_in_alterUserStatement6793 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_userPassword_in_alterUserStatement6795 = new BitSet(new long[]{0x0000000000000002L,0x0200000100000000L});
	public static final BitSet FOLLOW_K_SUPERUSER_in_alterUserStatement6809 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_NOSUPERUSER_in_alterUserStatement6823 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_dropUserStatement6869 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000200L});
	public static final BitSet FOLLOW_K_USER_in_dropUserStatement6871 = new BitSet(new long[]{0x0000000000100000L,0x0000000000000040L,0x0000000120000000L});
	public static final BitSet FOLLOW_K_IF_in_dropUserStatement6874 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_dropUserStatement6876 = new BitSet(new long[]{0x0000000000100000L,0x0000000000000000L,0x0000000120000000L});
	public static final BitSet FOLLOW_username_in_dropUserStatement6884 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_LIST_in_listUsersStatement6909 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_K_USERS_in_listUsersStatement6911 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CREATE_in_createRoleStatement6945 = new BitSet(new long[]{0x0000000000000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_K_ROLE_in_createRoleStatement6947 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A5AL,0x000000013008F63AL});
	public static final BitSet FOLLOW_K_IF_in_createRoleStatement6950 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
	public static final BitSet FOLLOW_K_NOT_in_createRoleStatement6952 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_createRoleStatement6954 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000013008F63AL});
	public static final BitSet FOLLOW_userOrRoleName_in_createRoleStatement6962 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000040000L});
	public static final BitSet FOLLOW_K_WITH_in_createRoleStatement6972 = new BitSet(new long[]{0x0000000000000000L,0x0200012002000000L});
	public static final BitSet FOLLOW_roleOptions_in_createRoleStatement6974 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ALTER_in_alterRoleStatement7018 = new BitSet(new long[]{0x0000000000000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_K_ROLE_in_alterRoleStatement7020 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000013008F63AL});
	public static final BitSet FOLLOW_userOrRoleName_in_alterRoleStatement7024 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000040000L});
	public static final BitSet FOLLOW_K_WITH_in_alterRoleStatement7034 = new BitSet(new long[]{0x0000000000000000L,0x0200012002000000L});
	public static final BitSet FOLLOW_roleOptions_in_alterRoleStatement7036 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_dropRoleStatement7080 = new BitSet(new long[]{0x0000000000000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_K_ROLE_in_dropRoleStatement7082 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A5AL,0x000000013008F63AL});
	public static final BitSet FOLLOW_K_IF_in_dropRoleStatement7085 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_dropRoleStatement7087 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000013008F63AL});
	public static final BitSet FOLLOW_userOrRoleName_in_dropRoleStatement7095 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_LIST_in_listRolesStatement7135 = new BitSet(new long[]{0x0000000000000000L,0x0002000000000000L});
	public static final BitSet FOLLOW_K_ROLES_in_listRolesStatement7137 = new BitSet(new long[]{0x0000000000000002L,0x0000000880000000L});
	public static final BitSet FOLLOW_K_OF_in_listRolesStatement7147 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000013008F63AL});
	public static final BitSet FOLLOW_roleName_in_listRolesStatement7149 = new BitSet(new long[]{0x0000000000000002L,0x0000000080000000L});
	public static final BitSet FOLLOW_K_NORECURSIVE_in_listRolesStatement7162 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_roleOption_in_roleOptions7193 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_AND_in_roleOptions7197 = new BitSet(new long[]{0x0000000000000000L,0x0200012002000000L});
	public static final BitSet FOLLOW_roleOption_in_roleOptions7199 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_PASSWORD_in_roleOption7221 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0020000000000000L});
	public static final BitSet FOLLOW_181_in_roleOption7223 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000100000000L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_roleOption7227 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_OPTIONS_in_roleOption7238 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0020000000000000L});
	public static final BitSet FOLLOW_181_in_roleOption7240 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x1000000000000000L});
	public static final BitSet FOLLOW_mapLiteral_in_roleOption7244 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_SUPERUSER_in_roleOption7255 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0020000000000000L});
	public static final BitSet FOLLOW_181_in_roleOption7257 = new BitSet(new long[]{0x0000000000000040L});
	public static final BitSet FOLLOW_BOOLEAN_in_roleOption7261 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_LOGIN_in_roleOption7272 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0020000000000000L});
	public static final BitSet FOLLOW_181_in_roleOption7274 = new BitSet(new long[]{0x0000000000000040L});
	public static final BitSet FOLLOW_BOOLEAN_in_roleOption7278 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_PASSWORD_in_userPassword7300 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000100000000L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_userPassword7304 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_cident7335 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUOTED_NAME_in_cident7360 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unreserved_keyword_in_cident7379 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_ident7405 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUOTED_NAME_in_ident7430 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unreserved_keyword_in_ident7449 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_noncol_ident7475 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUOTED_NAME_in_noncol_ident7500 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unreserved_keyword_in_noncol_ident7519 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ksName_in_keyspaceName7552 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ksName_in_indexName7586 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_176_in_indexName7589 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_idxName_in_indexName7593 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ksName_in_columnFamilyName7625 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_176_in_columnFamilyName7628 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000003008F63AL});
	public static final BitSet FOLLOW_cfName_in_columnFamilyName7632 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_noncol_ident_in_userTypeName7657 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_176_in_userTypeName7659 = new BitSet(new long[]{0x7082360086100000L,0x03D34721476E181AL,0x0000000020002632L});
	public static final BitSet FOLLOW_non_type_ident_in_userTypeName7665 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_roleName_in_userOrRoleName7697 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_ksName7720 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUOTED_NAME_in_ksName7745 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unreserved_keyword_in_ksName7764 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_ksName7774 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_cfName7796 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUOTED_NAME_in_cfName7821 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unreserved_keyword_in_cfName7840 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_cfName7850 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_idxName7872 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUOTED_NAME_in_idxName7897 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unreserved_keyword_in_idxName7916 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_idxName7926 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_roleName7948 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_roleName7973 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUOTED_NAME_in_roleName7989 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unreserved_keyword_in_roleName8008 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_roleName8018 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_constant8043 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGER_in_constant8055 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FLOAT_in_constant8074 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BOOLEAN_in_constant8095 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_UUID_in_constant8114 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_HEXNUMBER_in_constant8136 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_175_in_constant8154 = new BitSet(new long[]{0x0000000000000000L,0x0000000020000400L});
	public static final BitSet FOLLOW_set_in_constant8163 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_188_in_mapLiteral8192 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x310288093008F63BL});
	public static final BitSet FOLLOW_term_in_mapLiteral8210 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0002000000000000L});
	public static final BitSet FOLLOW_177_in_mapLiteral8212 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_mapLiteral8216 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x2000400000000000L});
	public static final BitSet FOLLOW_174_in_mapLiteral8222 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_mapLiteral8226 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0002000000000000L});
	public static final BitSet FOLLOW_177_in_mapLiteral8228 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_mapLiteral8232 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x2000400000000000L});
	public static final BitSet FOLLOW_189_in_mapLiteral8248 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_177_in_setOrMapLiteral8272 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_setOrMapLiteral8276 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_174_in_setOrMapLiteral8292 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_setOrMapLiteral8296 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0002000000000000L});
	public static final BitSet FOLLOW_177_in_setOrMapLiteral8298 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_setOrMapLiteral8302 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_174_in_setOrMapLiteral8337 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_setOrMapLiteral8341 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_184_in_collectionLiteral8375 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x150288093008F63BL});
	public static final BitSet FOLLOW_term_in_collectionLiteral8393 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0400400000000000L});
	public static final BitSet FOLLOW_174_in_collectionLiteral8399 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_collectionLiteral8403 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0400400000000000L});
	public static final BitSet FOLLOW_186_in_collectionLiteral8419 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_188_in_collectionLiteral8429 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_collectionLiteral8433 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x2002400000000000L});
	public static final BitSet FOLLOW_setOrMapLiteral_in_collectionLiteral8437 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x2000000000000000L});
	public static final BitSet FOLLOW_189_in_collectionLiteral8442 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_188_in_collectionLiteral8460 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x2000000000000000L});
	public static final BitSet FOLLOW_189_in_collectionLiteral8462 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_188_in_usertypeLiteral8506 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_noncol_ident_in_usertypeLiteral8510 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0002000000000000L});
	public static final BitSet FOLLOW_177_in_usertypeLiteral8512 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_usertypeLiteral8516 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x2000400000000000L});
	public static final BitSet FOLLOW_174_in_usertypeLiteral8522 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_noncol_ident_in_usertypeLiteral8526 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0002000000000000L});
	public static final BitSet FOLLOW_177_in_usertypeLiteral8528 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_usertypeLiteral8532 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x2000400000000000L});
	public static final BitSet FOLLOW_189_in_usertypeLiteral8539 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_171_in_tupleLiteral8576 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_tupleLiteral8580 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_tupleLiteral8586 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_tupleLiteral8590 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_tupleLiteral8597 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_constant_in_value8620 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_collectionLiteral_in_value8642 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_usertypeLiteral_in_value8655 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_tupleLiteral_in_value8670 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_NULL_in_value8686 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_177_in_value8710 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_noncol_ident_in_value8714 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_value8725 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGER_in_intValue8771 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_177_in_intValue8785 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_noncol_ident_in_intValue8789 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_intValue8800 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_keyspaceName_in_functionName8834 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_176_in_functionName8836 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476A5A1AL,0x000000002000F633L});
	public static final BitSet FOLLOW_allowedFunctionName_in_functionName8842 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_allowedFunctionName8869 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUOTED_NAME_in_allowedFunctionName8903 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unreserved_function_keyword_in_allowedFunctionName8931 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TOKEN_in_allowedFunctionName8941 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_COUNT_in_allowedFunctionName8973 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_functionName_in_function9020 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_function9022 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_172_in_function9024 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_functionName_in_function9054 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_function9056 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_functionArgs_in_function9060 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_172_in_function9062 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_term_in_functionArgs9095 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_174_in_functionArgs9101 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_functionArgs9105 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_value_in_term9133 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_function_in_term9170 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_171_in_term9202 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_term9206 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_172_in_term9208 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_term9212 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_columnOperation9235 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0120000000000000L});
	public static final BitSet FOLLOW_columnOperationDifferentiator_in_columnOperation9237 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_181_in_columnOperationDifferentiator9256 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_normalColumnOperation_in_columnOperationDifferentiator9258 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_184_in_columnOperationDifferentiator9267 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_columnOperationDifferentiator9271 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0400000000000000L});
	public static final BitSet FOLLOW_186_in_columnOperationDifferentiator9273 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0020000000000000L});
	public static final BitSet FOLLOW_specializedColumnOperation_in_columnOperationDifferentiator9275 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_term_in_normalColumnOperation9296 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000200000000000L});
	public static final BitSet FOLLOW_173_in_normalColumnOperation9299 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_normalColumnOperation9303 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_normalColumnOperation9324 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000A00000000000L});
	public static final BitSet FOLLOW_set_in_normalColumnOperation9328 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_normalColumnOperation9338 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_normalColumnOperation9356 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_INTEGER_in_normalColumnOperation9360 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_181_in_specializedColumnOperation9386 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_specializedColumnOperation9390 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_columnCondition9423 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L,0x01F8040000000000L});
	public static final BitSet FOLLOW_relationType_in_columnCondition9437 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_columnCondition9441 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_IN_in_columnCondition9455 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0002080010000000L});
	public static final BitSet FOLLOW_singleColumnInValues_in_columnCondition9473 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_inMarker_in_columnCondition9493 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_184_in_columnCondition9521 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_columnCondition9525 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0400000000000000L});
	public static final BitSet FOLLOW_186_in_columnCondition9527 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L,0x00F8040000000000L});
	public static final BitSet FOLLOW_relationType_in_columnCondition9545 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_columnCondition9549 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_IN_in_columnCondition9567 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0002080010000000L});
	public static final BitSet FOLLOW_singleColumnInValues_in_columnCondition9589 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_inMarker_in_columnCondition9613 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_property_in_properties9675 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_AND_in_properties9679 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_property_in_properties9681 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_noncol_ident_in_property9704 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0020000000000000L});
	public static final BitSet FOLLOW_181_in_property9706 = new BitSet(new long[]{0xF18EF6E286244040L,0x7FF34721676E5E1AL,0x000080090008F63AL});
	public static final BitSet FOLLOW_propertyValue_in_property9710 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_noncol_ident_in_property9722 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0020000000000000L});
	public static final BitSet FOLLOW_181_in_property9724 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x1000000000000000L});
	public static final BitSet FOLLOW_mapLiteral_in_property9728 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_constant_in_propertyValue9753 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unreserved_keyword_in_propertyValue9775 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_181_in_relationType9798 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_179_in_relationType9809 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_180_in_relationType9820 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_182_in_relationType9830 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_183_in_relationType9841 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_170_in_relationType9851 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_relation9873 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00F8040000000000L});
	public static final BitSet FOLLOW_relationType_in_relation9877 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_relation9881 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_relation9893 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
	public static final BitSet FOLLOW_K_IS_in_relation9895 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
	public static final BitSet FOLLOW_K_NOT_in_relation9897 = new BitSet(new long[]{0x0000000000000000L,0x0000000400000000L});
	public static final BitSet FOLLOW_K_NULL_in_relation9899 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TOKEN_in_relation9909 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_tupleOfIdentifiers_in_relation9913 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00F8040000000000L});
	public static final BitSet FOLLOW_relationType_in_relation9917 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_relation9921 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_relation9941 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_K_IN_in_relation9943 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0002000010000000L});
	public static final BitSet FOLLOW_inMarker_in_relation9947 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_relation9967 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_K_IN_in_relation9969 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_singleColumnInValues_in_relation9973 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_relation9993 = new BitSet(new long[]{0x0000200000000000L});
	public static final BitSet FOLLOW_K_CONTAINS_in_relation9995 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_K_KEY_in_relation10000 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_relation10016 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_relation10028 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0100000000000000L});
	public static final BitSet FOLLOW_184_in_relation10030 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_relation10034 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0400000000000000L});
	public static final BitSet FOLLOW_186_in_relation10036 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00F8040000000000L});
	public static final BitSet FOLLOW_relationType_in_relation10040 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_relation10044 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_tupleOfIdentifiers_in_relation10056 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L,0x00F8040000000000L});
	public static final BitSet FOLLOW_K_IN_in_relation10066 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0002080010000000L});
	public static final BitSet FOLLOW_171_in_relation10080 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_172_in_relation10082 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_inMarkerForTuple_in_relation10114 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_tupleOfTupleLiterals_in_relation10148 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_tupleOfMarkersForTuples_in_relation10182 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_relationType_in_relation10224 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_tupleLiteral_in_relation10228 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_relationType_in_relation10254 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0002000010000000L});
	public static final BitSet FOLLOW_markerForTuple_in_relation10258 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_171_in_relation10288 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000008002008F63BL});
	public static final BitSet FOLLOW_relation_in_relation10290 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_172_in_relation10293 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_inMarker10314 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_177_in_inMarker10324 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_noncol_ident_in_inMarker10328 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_171_in_tupleOfIdentifiers10360 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_tupleOfIdentifiers10364 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_tupleOfIdentifiers10369 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_cident_in_tupleOfIdentifiers10373 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_tupleOfIdentifiers10379 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_171_in_singleColumnInValues10409 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110298093008F63BL});
	public static final BitSet FOLLOW_term_in_singleColumnInValues10417 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_singleColumnInValues10422 = new BitSet(new long[]{0xF18EF6E286344040L,0x7FF34725676E5E1AL,0x110288093008F63BL});
	public static final BitSet FOLLOW_term_in_singleColumnInValues10426 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_singleColumnInValues10435 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_171_in_tupleOfTupleLiterals10465 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_tupleLiteral_in_tupleOfTupleLiterals10469 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_tupleOfTupleLiterals10474 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_tupleLiteral_in_tupleOfTupleLiterals10478 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_tupleOfTupleLiterals10484 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_markerForTuple10505 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_177_in_markerForTuple10515 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_noncol_ident_in_markerForTuple10519 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_171_in_tupleOfMarkersForTuples10551 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0002000010000000L});
	public static final BitSet FOLLOW_markerForTuple_in_tupleOfMarkersForTuples10555 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_174_in_tupleOfMarkersForTuples10560 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0002000010000000L});
	public static final BitSet FOLLOW_markerForTuple_in_tupleOfMarkersForTuples10564 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000500000000000L});
	public static final BitSet FOLLOW_172_in_tupleOfMarkersForTuples10570 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_inMarkerForTuple10591 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_177_in_inMarkerForTuple10601 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FF34721476E5A1AL,0x000000002008F63AL});
	public static final BitSet FOLLOW_noncol_ident_in_inMarkerForTuple10605 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_native_type_in_comparatorType10630 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_collection_type_in_comparatorType10646 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_tuple_type_in_comparatorType10658 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_userTypeName_in_comparatorType10674 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_FROZEN_in_comparatorType10686 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0008000000000000L});
	public static final BitSet FOLLOW_179_in_comparatorType10688 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_comparatorType10692 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0040000000000000L});
	public static final BitSet FOLLOW_182_in_comparatorType10694 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_comparatorType10712 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ASCII_in_native_type10741 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_BIGINT_in_native_type10755 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_BLOB_in_native_type10768 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_BOOLEAN_in_native_type10783 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_COUNTER_in_native_type10795 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DECIMAL_in_native_type10807 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DOUBLE_in_native_type10819 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_FLOAT_in_native_type10832 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_INET_in_native_type10846 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_INT_in_native_type10861 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_SMALLINT_in_native_type10877 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TEXT_in_native_type10888 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TIMESTAMP_in_native_type10903 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TINYINT_in_native_type10913 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_UUID_in_native_type10925 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_VARCHAR_in_native_type10940 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_VARINT_in_native_type10952 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TIMEUUID_in_native_type10965 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DATE_in_native_type10976 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TIME_in_native_type10991 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_MAP_in_collection_type11019 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0008000000000000L});
	public static final BitSet FOLLOW_179_in_collection_type11022 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_collection_type11026 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_174_in_collection_type11028 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_collection_type11032 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0040000000000000L});
	public static final BitSet FOLLOW_182_in_collection_type11034 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_LIST_in_collection_type11052 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0008000000000000L});
	public static final BitSet FOLLOW_179_in_collection_type11054 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_collection_type11058 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0040000000000000L});
	public static final BitSet FOLLOW_182_in_collection_type11060 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_SET_in_collection_type11078 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0008000000000000L});
	public static final BitSet FOLLOW_179_in_collection_type11081 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_collection_type11085 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0040000000000000L});
	public static final BitSet FOLLOW_182_in_collection_type11087 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TUPLE_in_tuple_type11118 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0008000000000000L});
	public static final BitSet FOLLOW_179_in_tuple_type11120 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_tuple_type11135 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0040400000000000L});
	public static final BitSet FOLLOW_174_in_tuple_type11140 = new BitSet(new long[]{0xF18EF6E286100000L,0x7FFB4721476E5A1AL,0x000000012008F63AL});
	public static final BitSet FOLLOW_comparatorType_in_tuple_type11144 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0040400000000000L});
	public static final BitSet FOLLOW_182_in_tuple_type11156 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_username11175 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_username11183 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUOTED_NAME_in_username11191 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_non_type_ident11218 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUOTED_NAME_in_non_type_ident11249 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_basic_unreserved_keyword_in_non_type_ident11274 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_KEY_in_non_type_ident11286 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unreserved_function_keyword_in_unreserved_keyword11329 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_unreserved_keyword11345 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_basic_unreserved_keyword_in_unreserved_function_keyword11384 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_native_type_in_unreserved_function_keyword11396 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_basic_unreserved_keyword11434 = new BitSet(new long[]{0x0000000000000002L});
}
