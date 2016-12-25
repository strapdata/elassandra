// $ANTLR 3.5.2 /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g 2016-11-20 23:16:40

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
		"K_INT", "K_INTO", "K_JSON", "K_KEY", "K_KEYS", "K_KEYSPACE", "K_KEYSPACES", 
		"K_LANGUAGE", "K_LIMIT", "K_LIST", "K_LOGIN", "K_MAP", "K_MODIFY", "K_NAN", 
		"K_NOLOGIN", "K_NORECURSIVE", "K_NOSUPERUSER", "K_NOT", "K_NULL", "K_OF", 
		"K_ON", "K_OPTIONS", "K_OR", "K_ORDER", "K_PASSWORD", "K_PERMISSION", 
		"K_PERMISSIONS", "K_PRIMARY", "K_RENAME", "K_REPLACE", "K_RETURNS", "K_REVOKE", 
		"K_ROLE", "K_ROLES", "K_SELECT", "K_SET", "K_SFUNC", "K_SMALLINT", "K_STATIC", 
		"K_STORAGE", "K_STYPE", "K_SUPERUSER", "K_TEXT", "K_TIME", "K_TIMESTAMP", 
		"K_TIMEUUID", "K_TINYINT", "K_TO", "K_TOKEN", "K_TRIGGER", "K_TRUNCATE", 
		"K_TTL", "K_TUPLE", "K_TYPE", "K_UNLOGGED", "K_UPDATE", "K_USE", "K_USER", 
		"K_USERS", "K_USING", "K_UUID", "K_VALUES", "K_VARCHAR", "K_VARINT", "K_WHERE", 
		"K_WITH", "K_WRITETIME", "L", "LETTER", "M", "MULTILINE_COMMENT", "N", 
		"O", "P", "Q", "QMARK", "QUOTED_NAME", "R", "S", "STRING_LITERAL", "T", 
		"U", "UUID", "V", "W", "WS", "X", "Y", "Z", "'!='", "'('", "')'", "'+'", 
		"','", "'-'", "'.'", "':'", "';'", "'<'", "'<='", "'='", "'>'", "'>='", 
		"'['", "'\\*'", "']'", "'{'", "'}'"
	};
	public static final int EOF=-1;
	public static final int T__167=167;
	public static final int T__168=168;
	public static final int T__169=169;
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
	public static final int K_JSON=80;
	public static final int K_KEY=81;
	public static final int K_KEYS=82;
	public static final int K_KEYSPACE=83;
	public static final int K_KEYSPACES=84;
	public static final int K_LANGUAGE=85;
	public static final int K_LIMIT=86;
	public static final int K_LIST=87;
	public static final int K_LOGIN=88;
	public static final int K_MAP=89;
	public static final int K_MODIFY=90;
	public static final int K_NAN=91;
	public static final int K_NOLOGIN=92;
	public static final int K_NORECURSIVE=93;
	public static final int K_NOSUPERUSER=94;
	public static final int K_NOT=95;
	public static final int K_NULL=96;
	public static final int K_OF=97;
	public static final int K_ON=98;
	public static final int K_OPTIONS=99;
	public static final int K_OR=100;
	public static final int K_ORDER=101;
	public static final int K_PASSWORD=102;
	public static final int K_PERMISSION=103;
	public static final int K_PERMISSIONS=104;
	public static final int K_PRIMARY=105;
	public static final int K_RENAME=106;
	public static final int K_REPLACE=107;
	public static final int K_RETURNS=108;
	public static final int K_REVOKE=109;
	public static final int K_ROLE=110;
	public static final int K_ROLES=111;
	public static final int K_SELECT=112;
	public static final int K_SET=113;
	public static final int K_SFUNC=114;
	public static final int K_SMALLINT=115;
	public static final int K_STATIC=116;
	public static final int K_STORAGE=117;
	public static final int K_STYPE=118;
	public static final int K_SUPERUSER=119;
	public static final int K_TEXT=120;
	public static final int K_TIME=121;
	public static final int K_TIMESTAMP=122;
	public static final int K_TIMEUUID=123;
	public static final int K_TINYINT=124;
	public static final int K_TO=125;
	public static final int K_TOKEN=126;
	public static final int K_TRIGGER=127;
	public static final int K_TRUNCATE=128;
	public static final int K_TTL=129;
	public static final int K_TUPLE=130;
	public static final int K_TYPE=131;
	public static final int K_UNLOGGED=132;
	public static final int K_UPDATE=133;
	public static final int K_USE=134;
	public static final int K_USER=135;
	public static final int K_USERS=136;
	public static final int K_USING=137;
	public static final int K_UUID=138;
	public static final int K_VALUES=139;
	public static final int K_VARCHAR=140;
	public static final int K_VARINT=141;
	public static final int K_WHERE=142;
	public static final int K_WITH=143;
	public static final int K_WRITETIME=144;
	public static final int L=145;
	public static final int LETTER=146;
	public static final int M=147;
	public static final int MULTILINE_COMMENT=148;
	public static final int N=149;
	public static final int O=150;
	public static final int P=151;
	public static final int Q=152;
	public static final int QMARK=153;
	public static final int QUOTED_NAME=154;
	public static final int R=155;
	public static final int S=156;
	public static final int STRING_LITERAL=157;
	public static final int T=158;
	public static final int U=159;
	public static final int UUID=160;
	public static final int V=161;
	public static final int W=162;
	public static final int WS=163;
	public static final int X=164;
	public static final int Y=165;
	public static final int Z=166;

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
	@Override public String getGrammarFileName() { return "/Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g"; }


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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:230:1: query returns [ParsedStatement stmnt] : st= cqlStatement ( ';' )* EOF ;
	public final ParsedStatement query() throws RecognitionException {
		ParsedStatement stmnt = null;


		ParsedStatement st =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:231:5: (st= cqlStatement ( ';' )* EOF )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:231:7: st= cqlStatement ( ';' )* EOF
			{
			pushFollow(FOLLOW_cqlStatement_in_query72);
			st=cqlStatement();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:231:23: ( ';' )*
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==175) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:231:24: ';'
					{
					match(input,175,FOLLOW_175_in_query75); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:234:1: cqlStatement returns [ParsedStatement stmt] : (st1= selectStatement |st2= insertStatement |st3= updateStatement |st4= batchStatement |st5= deleteStatement |st6= useStatement |st7= truncateStatement |st8= createKeyspaceStatement |st9= createTableStatement |st10= createIndexStatement |st11= dropKeyspaceStatement |st12= dropTableStatement |st13= dropIndexStatement |st14= alterTableStatement |st15= alterKeyspaceStatement |st16= grantPermissionsStatement |st17= revokePermissionsStatement |st18= listPermissionsStatement |st19= createUserStatement |st20= alterUserStatement |st21= dropUserStatement |st22= listUsersStatement |st23= createTriggerStatement |st24= dropTriggerStatement |st25= createTypeStatement |st26= alterTypeStatement |st27= dropTypeStatement |st28= createFunctionStatement |st29= dropFunctionStatement |st30= createAggregateStatement |st31= dropAggregateStatement |st32= createRoleStatement |st33= alterRoleStatement |st34= dropRoleStatement |st35= listRolesStatement |st36= grantRoleStatement |st37= revokeRoleStatement );
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

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:236:5: (st1= selectStatement |st2= insertStatement |st3= updateStatement |st4= batchStatement |st5= deleteStatement |st6= useStatement |st7= truncateStatement |st8= createKeyspaceStatement |st9= createTableStatement |st10= createIndexStatement |st11= dropKeyspaceStatement |st12= dropTableStatement |st13= dropIndexStatement |st14= alterTableStatement |st15= alterKeyspaceStatement |st16= grantPermissionsStatement |st17= revokePermissionsStatement |st18= listPermissionsStatement |st19= createUserStatement |st20= alterUserStatement |st21= dropUserStatement |st22= listUsersStatement |st23= createTriggerStatement |st24= dropTriggerStatement |st25= createTypeStatement |st26= alterTypeStatement |st27= dropTypeStatement |st28= createFunctionStatement |st29= dropFunctionStatement |st30= createAggregateStatement |st31= dropAggregateStatement |st32= createRoleStatement |st33= alterRoleStatement |st34= dropRoleStatement |st35= listRolesStatement |st36= grantRoleStatement |st37= revokeRoleStatement )
			int alt2=37;
			alt2 = dfa2.predict(input);
			switch (alt2) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:236:7: st1= selectStatement
					{
					pushFollow(FOLLOW_selectStatement_in_cqlStatement113);
					st1=selectStatement();
					state._fsp--;

					 stmt = st1; 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:237:7: st2= insertStatement
					{
					pushFollow(FOLLOW_insertStatement_in_cqlStatement138);
					st2=insertStatement();
					state._fsp--;

					 stmt = st2; 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:238:7: st3= updateStatement
					{
					pushFollow(FOLLOW_updateStatement_in_cqlStatement163);
					st3=updateStatement();
					state._fsp--;

					 stmt = st3; 
					}
					break;
				case 4 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:239:7: st4= batchStatement
					{
					pushFollow(FOLLOW_batchStatement_in_cqlStatement188);
					st4=batchStatement();
					state._fsp--;

					 stmt = st4; 
					}
					break;
				case 5 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:240:7: st5= deleteStatement
					{
					pushFollow(FOLLOW_deleteStatement_in_cqlStatement214);
					st5=deleteStatement();
					state._fsp--;

					 stmt = st5; 
					}
					break;
				case 6 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:241:7: st6= useStatement
					{
					pushFollow(FOLLOW_useStatement_in_cqlStatement239);
					st6=useStatement();
					state._fsp--;

					 stmt = st6; 
					}
					break;
				case 7 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:242:7: st7= truncateStatement
					{
					pushFollow(FOLLOW_truncateStatement_in_cqlStatement267);
					st7=truncateStatement();
					state._fsp--;

					 stmt = st7; 
					}
					break;
				case 8 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:243:7: st8= createKeyspaceStatement
					{
					pushFollow(FOLLOW_createKeyspaceStatement_in_cqlStatement290);
					st8=createKeyspaceStatement();
					state._fsp--;

					 stmt = st8; 
					}
					break;
				case 9 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:244:7: st9= createTableStatement
					{
					pushFollow(FOLLOW_createTableStatement_in_cqlStatement307);
					st9=createTableStatement();
					state._fsp--;

					 stmt = st9; 
					}
					break;
				case 10 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:245:7: st10= createIndexStatement
					{
					pushFollow(FOLLOW_createIndexStatement_in_cqlStatement326);
					st10=createIndexStatement();
					state._fsp--;

					 stmt = st10; 
					}
					break;
				case 11 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:246:7: st11= dropKeyspaceStatement
					{
					pushFollow(FOLLOW_dropKeyspaceStatement_in_cqlStatement345);
					st11=dropKeyspaceStatement();
					state._fsp--;

					 stmt = st11; 
					}
					break;
				case 12 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:247:7: st12= dropTableStatement
					{
					pushFollow(FOLLOW_dropTableStatement_in_cqlStatement363);
					st12=dropTableStatement();
					state._fsp--;

					 stmt = st12; 
					}
					break;
				case 13 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:248:7: st13= dropIndexStatement
					{
					pushFollow(FOLLOW_dropIndexStatement_in_cqlStatement384);
					st13=dropIndexStatement();
					state._fsp--;

					 stmt = st13; 
					}
					break;
				case 14 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:249:7: st14= alterTableStatement
					{
					pushFollow(FOLLOW_alterTableStatement_in_cqlStatement405);
					st14=alterTableStatement();
					state._fsp--;

					 stmt = st14; 
					}
					break;
				case 15 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:250:7: st15= alterKeyspaceStatement
					{
					pushFollow(FOLLOW_alterKeyspaceStatement_in_cqlStatement425);
					st15=alterKeyspaceStatement();
					state._fsp--;

					 stmt = st15; 
					}
					break;
				case 16 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:251:7: st16= grantPermissionsStatement
					{
					pushFollow(FOLLOW_grantPermissionsStatement_in_cqlStatement442);
					st16=grantPermissionsStatement();
					state._fsp--;

					 stmt = st16; 
					}
					break;
				case 17 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:252:7: st17= revokePermissionsStatement
					{
					pushFollow(FOLLOW_revokePermissionsStatement_in_cqlStatement456);
					st17=revokePermissionsStatement();
					state._fsp--;

					 stmt = st17; 
					}
					break;
				case 18 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:253:7: st18= listPermissionsStatement
					{
					pushFollow(FOLLOW_listPermissionsStatement_in_cqlStatement469);
					st18=listPermissionsStatement();
					state._fsp--;

					 stmt = st18; 
					}
					break;
				case 19 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:254:7: st19= createUserStatement
					{
					pushFollow(FOLLOW_createUserStatement_in_cqlStatement484);
					st19=createUserStatement();
					state._fsp--;

					 stmt = st19; 
					}
					break;
				case 20 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:255:7: st20= alterUserStatement
					{
					pushFollow(FOLLOW_alterUserStatement_in_cqlStatement504);
					st20=alterUserStatement();
					state._fsp--;

					 stmt = st20; 
					}
					break;
				case 21 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:256:7: st21= dropUserStatement
					{
					pushFollow(FOLLOW_dropUserStatement_in_cqlStatement525);
					st21=dropUserStatement();
					state._fsp--;

					 stmt = st21; 
					}
					break;
				case 22 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:257:7: st22= listUsersStatement
					{
					pushFollow(FOLLOW_listUsersStatement_in_cqlStatement547);
					st22=listUsersStatement();
					state._fsp--;

					 stmt = st22; 
					}
					break;
				case 23 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:258:7: st23= createTriggerStatement
					{
					pushFollow(FOLLOW_createTriggerStatement_in_cqlStatement568);
					st23=createTriggerStatement();
					state._fsp--;

					 stmt = st23; 
					}
					break;
				case 24 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:259:7: st24= dropTriggerStatement
					{
					pushFollow(FOLLOW_dropTriggerStatement_in_cqlStatement585);
					st24=dropTriggerStatement();
					state._fsp--;

					 stmt = st24; 
					}
					break;
				case 25 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:260:7: st25= createTypeStatement
					{
					pushFollow(FOLLOW_createTypeStatement_in_cqlStatement604);
					st25=createTypeStatement();
					state._fsp--;

					 stmt = st25; 
					}
					break;
				case 26 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:261:7: st26= alterTypeStatement
					{
					pushFollow(FOLLOW_alterTypeStatement_in_cqlStatement624);
					st26=alterTypeStatement();
					state._fsp--;

					 stmt = st26; 
					}
					break;
				case 27 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:262:7: st27= dropTypeStatement
					{
					pushFollow(FOLLOW_dropTypeStatement_in_cqlStatement645);
					st27=dropTypeStatement();
					state._fsp--;

					 stmt = st27; 
					}
					break;
				case 28 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:263:7: st28= createFunctionStatement
					{
					pushFollow(FOLLOW_createFunctionStatement_in_cqlStatement667);
					st28=createFunctionStatement();
					state._fsp--;

					 stmt = st28; 
					}
					break;
				case 29 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:264:7: st29= dropFunctionStatement
					{
					pushFollow(FOLLOW_dropFunctionStatement_in_cqlStatement683);
					st29=dropFunctionStatement();
					state._fsp--;

					 stmt = st29; 
					}
					break;
				case 30 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:265:7: st30= createAggregateStatement
					{
					pushFollow(FOLLOW_createAggregateStatement_in_cqlStatement701);
					st30=createAggregateStatement();
					state._fsp--;

					 stmt = st30; 
					}
					break;
				case 31 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:266:7: st31= dropAggregateStatement
					{
					pushFollow(FOLLOW_dropAggregateStatement_in_cqlStatement716);
					st31=dropAggregateStatement();
					state._fsp--;

					 stmt = st31; 
					}
					break;
				case 32 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:267:7: st32= createRoleStatement
					{
					pushFollow(FOLLOW_createRoleStatement_in_cqlStatement733);
					st32=createRoleStatement();
					state._fsp--;

					 stmt = st32; 
					}
					break;
				case 33 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:268:7: st33= alterRoleStatement
					{
					pushFollow(FOLLOW_alterRoleStatement_in_cqlStatement753);
					st33=alterRoleStatement();
					state._fsp--;

					 stmt = st33; 
					}
					break;
				case 34 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:269:7: st34= dropRoleStatement
					{
					pushFollow(FOLLOW_dropRoleStatement_in_cqlStatement774);
					st34=dropRoleStatement();
					state._fsp--;

					 stmt = st34; 
					}
					break;
				case 35 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:270:7: st35= listRolesStatement
					{
					pushFollow(FOLLOW_listRolesStatement_in_cqlStatement796);
					st35=listRolesStatement();
					state._fsp--;

					 stmt = st35; 
					}
					break;
				case 36 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:271:7: st36= grantRoleStatement
					{
					pushFollow(FOLLOW_grantRoleStatement_in_cqlStatement817);
					st36=grantRoleStatement();
					state._fsp--;

					 stmt = st36; 
					}
					break;
				case 37 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:272:7: st37= revokeRoleStatement
					{
					pushFollow(FOLLOW_revokeRoleStatement_in_cqlStatement838);
					st37=revokeRoleStatement();
					state._fsp--;

					 stmt = st37; 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:278:1: useStatement returns [UseStatement stmt] : K_USE ks= keyspaceName ;
	public final UseStatement useStatement() throws RecognitionException {
		UseStatement stmt = null;


		String ks =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:279:5: ( K_USE ks= keyspaceName )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:279:7: K_USE ks= keyspaceName
			{
			match(input,K_USE,FOLLOW_K_USE_in_useStatement871); 
			pushFollow(FOLLOW_keyspaceName_in_useStatement875);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:288:1: selectStatement returns [SelectStatement.RawStatement expr] : K_SELECT ( K_JSON )? ( ( K_DISTINCT )? sclause= selectClause ) K_FROM cf= columnFamilyName ( K_WHERE wclause= whereClause )? ( K_ORDER K_BY orderByClause[orderings] ( ',' orderByClause[orderings] )* )? ( K_LIMIT rows= intValue )? ( K_ALLOW K_FILTERING )? ;
	public final SelectStatement.RawStatement selectStatement() throws RecognitionException {
		SelectStatement.RawStatement expr = null;


		List<RawSelector> sclause =null;
		CFName cf =null;
		List<Relation> wclause =null;
		Term.Raw rows =null;


		        boolean isDistinct = false;
		        Term.Raw limit = null;
		        Map<ColumnIdentifier.Raw, Boolean> orderings = new LinkedHashMap<ColumnIdentifier.Raw, Boolean>();
		        boolean allowFiltering = false;
		        boolean isJson = false;
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:296:5: ( K_SELECT ( K_JSON )? ( ( K_DISTINCT )? sclause= selectClause ) K_FROM cf= columnFamilyName ( K_WHERE wclause= whereClause )? ( K_ORDER K_BY orderByClause[orderings] ( ',' orderByClause[orderings] )* )? ( K_LIMIT rows= intValue )? ( K_ALLOW K_FILTERING )? )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:296:7: K_SELECT ( K_JSON )? ( ( K_DISTINCT )? sclause= selectClause ) K_FROM cf= columnFamilyName ( K_WHERE wclause= whereClause )? ( K_ORDER K_BY orderByClause[orderings] ( ',' orderByClause[orderings] )* )? ( K_LIMIT rows= intValue )? ( K_ALLOW K_FILTERING )?
			{
			match(input,K_SELECT,FOLLOW_K_SELECT_in_selectStatement909); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:297:7: ( K_JSON )?
			int alt3=2;
			int LA3_0 = input.LA(1);
			if ( (LA3_0==K_JSON) ) {
				int LA3_1 = input.LA(2);
				if ( (LA3_1==IDENT||(LA3_1 >= K_AGGREGATE && LA3_1 <= K_ALL)||LA3_1==K_ASCII||(LA3_1 >= K_BIGINT && LA3_1 <= K_BOOLEAN)||(LA3_1 >= K_CALLED && LA3_1 <= K_CLUSTERING)||(LA3_1 >= K_COMPACT && LA3_1 <= K_COUNTER)||(LA3_1 >= K_CUSTOM && LA3_1 <= K_DECIMAL)||(LA3_1 >= K_DISTINCT && LA3_1 <= K_DOUBLE)||(LA3_1 >= K_EXISTS && LA3_1 <= K_FLOAT)||LA3_1==K_FROZEN||(LA3_1 >= K_FUNCTION && LA3_1 <= K_FUNCTIONS)||LA3_1==K_INET||(LA3_1 >= K_INITCOND && LA3_1 <= K_INPUT)||LA3_1==K_INT||(LA3_1 >= K_JSON && LA3_1 <= K_KEYS)||(LA3_1 >= K_KEYSPACES && LA3_1 <= K_LANGUAGE)||(LA3_1 >= K_LIST && LA3_1 <= K_MAP)||LA3_1==K_NOLOGIN||LA3_1==K_NOSUPERUSER||LA3_1==K_OPTIONS||(LA3_1 >= K_PASSWORD && LA3_1 <= K_PERMISSIONS)||LA3_1==K_RETURNS||(LA3_1 >= K_ROLE && LA3_1 <= K_ROLES)||(LA3_1 >= K_SFUNC && LA3_1 <= K_TINYINT)||(LA3_1 >= K_TOKEN && LA3_1 <= K_TRIGGER)||(LA3_1 >= K_TTL && LA3_1 <= K_TYPE)||(LA3_1 >= K_USER && LA3_1 <= K_USERS)||(LA3_1 >= K_UUID && LA3_1 <= K_VARINT)||LA3_1==K_WRITETIME||(LA3_1 >= QMARK && LA3_1 <= QUOTED_NAME)||LA3_1==182) ) {
					alt3=1;
				}
				else if ( (LA3_1==K_AS) ) {
					int LA3_4 = input.LA(3);
					if ( (LA3_4==K_FROM||LA3_4==168||LA3_4==171||LA3_4==173) ) {
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
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:297:9: K_JSON
					{
					match(input,K_JSON,FOLLOW_K_JSON_in_selectStatement920); 
					 isJson = true; 
					}
					break;

			}

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:298:7: ( ( K_DISTINCT )? sclause= selectClause )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:298:9: ( K_DISTINCT )? sclause= selectClause
			{
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:298:9: ( K_DISTINCT )?
			int alt4=2;
			int LA4_0 = input.LA(1);
			if ( (LA4_0==K_DISTINCT) ) {
				int LA4_1 = input.LA(2);
				if ( (LA4_1==IDENT||(LA4_1 >= K_AGGREGATE && LA4_1 <= K_ALL)||LA4_1==K_ASCII||(LA4_1 >= K_BIGINT && LA4_1 <= K_BOOLEAN)||(LA4_1 >= K_CALLED && LA4_1 <= K_CLUSTERING)||(LA4_1 >= K_COMPACT && LA4_1 <= K_COUNTER)||(LA4_1 >= K_CUSTOM && LA4_1 <= K_DECIMAL)||(LA4_1 >= K_DISTINCT && LA4_1 <= K_DOUBLE)||(LA4_1 >= K_EXISTS && LA4_1 <= K_FLOAT)||LA4_1==K_FROZEN||(LA4_1 >= K_FUNCTION && LA4_1 <= K_FUNCTIONS)||LA4_1==K_INET||(LA4_1 >= K_INITCOND && LA4_1 <= K_INPUT)||LA4_1==K_INT||(LA4_1 >= K_JSON && LA4_1 <= K_KEYS)||(LA4_1 >= K_KEYSPACES && LA4_1 <= K_LANGUAGE)||(LA4_1 >= K_LIST && LA4_1 <= K_MAP)||LA4_1==K_NOLOGIN||LA4_1==K_NOSUPERUSER||LA4_1==K_OPTIONS||(LA4_1 >= K_PASSWORD && LA4_1 <= K_PERMISSIONS)||LA4_1==K_RETURNS||(LA4_1 >= K_ROLE && LA4_1 <= K_ROLES)||(LA4_1 >= K_SFUNC && LA4_1 <= K_TINYINT)||(LA4_1 >= K_TOKEN && LA4_1 <= K_TRIGGER)||(LA4_1 >= K_TTL && LA4_1 <= K_TYPE)||(LA4_1 >= K_USER && LA4_1 <= K_USERS)||(LA4_1 >= K_UUID && LA4_1 <= K_VARINT)||LA4_1==K_WRITETIME||(LA4_1 >= QMARK && LA4_1 <= QUOTED_NAME)||LA4_1==182) ) {
					alt4=1;
				}
				else if ( (LA4_1==K_AS) ) {
					int LA4_4 = input.LA(3);
					if ( (LA4_4==K_FROM||LA4_4==168||LA4_4==171||LA4_4==173) ) {
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
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:298:11: K_DISTINCT
					{
					match(input,K_DISTINCT,FOLLOW_K_DISTINCT_in_selectStatement937); 
					 isDistinct = true; 
					}
					break;

			}

			pushFollow(FOLLOW_selectClause_in_selectStatement946);
			sclause=selectClause();
			state._fsp--;

			}

			match(input,K_FROM,FOLLOW_K_FROM_in_selectStatement956); 
			pushFollow(FOLLOW_columnFamilyName_in_selectStatement960);
			cf=columnFamilyName();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:300:7: ( K_WHERE wclause= whereClause )?
			int alt5=2;
			int LA5_0 = input.LA(1);
			if ( (LA5_0==K_WHERE) ) {
				alt5=1;
			}
			switch (alt5) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:300:9: K_WHERE wclause= whereClause
					{
					match(input,K_WHERE,FOLLOW_K_WHERE_in_selectStatement970); 
					pushFollow(FOLLOW_whereClause_in_selectStatement974);
					wclause=whereClause();
					state._fsp--;

					}
					break;

			}

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:301:7: ( K_ORDER K_BY orderByClause[orderings] ( ',' orderByClause[orderings] )* )?
			int alt7=2;
			int LA7_0 = input.LA(1);
			if ( (LA7_0==K_ORDER) ) {
				alt7=1;
			}
			switch (alt7) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:301:9: K_ORDER K_BY orderByClause[orderings] ( ',' orderByClause[orderings] )*
					{
					match(input,K_ORDER,FOLLOW_K_ORDER_in_selectStatement987); 
					match(input,K_BY,FOLLOW_K_BY_in_selectStatement989); 
					pushFollow(FOLLOW_orderByClause_in_selectStatement991);
					orderByClause(orderings);
					state._fsp--;

					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:301:47: ( ',' orderByClause[orderings] )*
					loop6:
					while (true) {
						int alt6=2;
						int LA6_0 = input.LA(1);
						if ( (LA6_0==171) ) {
							alt6=1;
						}

						switch (alt6) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:301:49: ',' orderByClause[orderings]
							{
							match(input,171,FOLLOW_171_in_selectStatement996); 
							pushFollow(FOLLOW_orderByClause_in_selectStatement998);
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

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:302:7: ( K_LIMIT rows= intValue )?
			int alt8=2;
			int LA8_0 = input.LA(1);
			if ( (LA8_0==K_LIMIT) ) {
				alt8=1;
			}
			switch (alt8) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:302:9: K_LIMIT rows= intValue
					{
					match(input,K_LIMIT,FOLLOW_K_LIMIT_in_selectStatement1015); 
					pushFollow(FOLLOW_intValue_in_selectStatement1019);
					rows=intValue();
					state._fsp--;

					 limit = rows; 
					}
					break;

			}

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:303:7: ( K_ALLOW K_FILTERING )?
			int alt9=2;
			int LA9_0 = input.LA(1);
			if ( (LA9_0==K_ALLOW) ) {
				alt9=1;
			}
			switch (alt9) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:303:9: K_ALLOW K_FILTERING
					{
					match(input,K_ALLOW,FOLLOW_K_ALLOW_in_selectStatement1034); 
					match(input,K_FILTERING,FOLLOW_K_FILTERING_in_selectStatement1036); 
					 allowFiltering = true; 
					}
					break;

			}


			          SelectStatement.Parameters params = new SelectStatement.Parameters(orderings,
			                                                                             isDistinct,
			                                                                             allowFiltering,
			                                                                             isJson);
			          expr = new SelectStatement.RawStatement(cf, params, sclause, wclause, limit);
			      
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:313:1: selectClause returns [List<RawSelector> expr] : (t1= selector ( ',' tN= selector )* | '\\*' );
	public final List<RawSelector> selectClause() throws RecognitionException {
		List<RawSelector> expr = null;


		RawSelector t1 =null;
		RawSelector tN =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:314:5: (t1= selector ( ',' tN= selector )* | '\\*' )
			int alt11=2;
			int LA11_0 = input.LA(1);
			if ( (LA11_0==IDENT||(LA11_0 >= K_AGGREGATE && LA11_0 <= K_ALL)||LA11_0==K_AS||LA11_0==K_ASCII||(LA11_0 >= K_BIGINT && LA11_0 <= K_BOOLEAN)||(LA11_0 >= K_CALLED && LA11_0 <= K_CLUSTERING)||(LA11_0 >= K_COMPACT && LA11_0 <= K_COUNTER)||(LA11_0 >= K_CUSTOM && LA11_0 <= K_DECIMAL)||(LA11_0 >= K_DISTINCT && LA11_0 <= K_DOUBLE)||(LA11_0 >= K_EXISTS && LA11_0 <= K_FLOAT)||LA11_0==K_FROZEN||(LA11_0 >= K_FUNCTION && LA11_0 <= K_FUNCTIONS)||LA11_0==K_INET||(LA11_0 >= K_INITCOND && LA11_0 <= K_INPUT)||LA11_0==K_INT||(LA11_0 >= K_JSON && LA11_0 <= K_KEYS)||(LA11_0 >= K_KEYSPACES && LA11_0 <= K_LANGUAGE)||(LA11_0 >= K_LIST && LA11_0 <= K_MAP)||LA11_0==K_NOLOGIN||LA11_0==K_NOSUPERUSER||LA11_0==K_OPTIONS||(LA11_0 >= K_PASSWORD && LA11_0 <= K_PERMISSIONS)||LA11_0==K_RETURNS||(LA11_0 >= K_ROLE && LA11_0 <= K_ROLES)||(LA11_0 >= K_SFUNC && LA11_0 <= K_TINYINT)||(LA11_0 >= K_TOKEN && LA11_0 <= K_TRIGGER)||(LA11_0 >= K_TTL && LA11_0 <= K_TYPE)||(LA11_0 >= K_USER && LA11_0 <= K_USERS)||(LA11_0 >= K_UUID && LA11_0 <= K_VARINT)||LA11_0==K_WRITETIME||(LA11_0 >= QMARK && LA11_0 <= QUOTED_NAME)) ) {
				alt11=1;
			}
			else if ( (LA11_0==182) ) {
				alt11=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 11, 0, input);
				throw nvae;
			}

			switch (alt11) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:314:7: t1= selector ( ',' tN= selector )*
					{
					pushFollow(FOLLOW_selector_in_selectClause1073);
					t1=selector();
					state._fsp--;

					 expr = new ArrayList<RawSelector>(); expr.add(t1); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:314:76: ( ',' tN= selector )*
					loop10:
					while (true) {
						int alt10=2;
						int LA10_0 = input.LA(1);
						if ( (LA10_0==171) ) {
							alt10=1;
						}

						switch (alt10) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:314:77: ',' tN= selector
							{
							match(input,171,FOLLOW_171_in_selectClause1078); 
							pushFollow(FOLLOW_selector_in_selectClause1082);
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
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:315:7: '\\*'
					{
					match(input,182,FOLLOW_182_in_selectClause1094); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:318:1: selector returns [RawSelector s] : us= unaliasedSelector ( K_AS c= ident )? ;
	public final RawSelector selector() throws RecognitionException {
		RawSelector s = null;


		Selectable.Raw us =null;
		ColumnIdentifier c =null;

		 ColumnIdentifier alias = null; 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:320:5: (us= unaliasedSelector ( K_AS c= ident )? )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:320:7: us= unaliasedSelector ( K_AS c= ident )?
			{
			pushFollow(FOLLOW_unaliasedSelector_in_selector1127);
			us=unaliasedSelector();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:320:28: ( K_AS c= ident )?
			int alt12=2;
			int LA12_0 = input.LA(1);
			if ( (LA12_0==K_AS) ) {
				alt12=1;
			}
			switch (alt12) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:320:29: K_AS c= ident
					{
					match(input,K_AS,FOLLOW_K_AS_in_selector1130); 
					pushFollow(FOLLOW_ident_in_selector1134);
					c=ident();
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:323:1: unaliasedSelector returns [Selectable.Raw s] : (c= cident | K_COUNT '(' countArgument ')' | K_WRITETIME '(' c= cident ')' | K_TTL '(' c= cident ')' |f= functionName args= selectionFunctionArgs ) ( '.' fi= cident )* ;
	public final Selectable.Raw unaliasedSelector() throws RecognitionException {
		Selectable.Raw s = null;


		ColumnIdentifier.Raw c =null;
		FunctionName f =null;
		List<Selectable.Raw> args =null;
		ColumnIdentifier.Raw fi =null;

		 Selectable.Raw tmp = null; 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:325:5: ( (c= cident | K_COUNT '(' countArgument ')' | K_WRITETIME '(' c= cident ')' | K_TTL '(' c= cident ')' |f= functionName args= selectionFunctionArgs ) ( '.' fi= cident )* )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:325:8: (c= cident | K_COUNT '(' countArgument ')' | K_WRITETIME '(' c= cident ')' | K_TTL '(' c= cident ')' |f= functionName args= selectionFunctionArgs ) ( '.' fi= cident )*
			{
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:325:8: (c= cident | K_COUNT '(' countArgument ')' | K_WRITETIME '(' c= cident ')' | K_TTL '(' c= cident ')' |f= functionName args= selectionFunctionArgs )
			int alt13=5;
			alt13 = dfa13.predict(input);
			switch (alt13) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:325:10: c= cident
					{
					pushFollow(FOLLOW_cident_in_unaliasedSelector1175);
					c=cident();
					state._fsp--;

					 tmp = c; 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:326:10: K_COUNT '(' countArgument ')'
					{
					match(input,K_COUNT,FOLLOW_K_COUNT_in_unaliasedSelector1221); 
					match(input,168,FOLLOW_168_in_unaliasedSelector1223); 
					pushFollow(FOLLOW_countArgument_in_unaliasedSelector1225);
					countArgument();
					state._fsp--;

					match(input,169,FOLLOW_169_in_unaliasedSelector1227); 
					 tmp = new Selectable.WithFunction.Raw(FunctionName.nativeFunction("countRows"), Collections.<Selectable.Raw>emptyList());
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:327:10: K_WRITETIME '(' c= cident ')'
					{
					match(input,K_WRITETIME,FOLLOW_K_WRITETIME_in_unaliasedSelector1252); 
					match(input,168,FOLLOW_168_in_unaliasedSelector1254); 
					pushFollow(FOLLOW_cident_in_unaliasedSelector1258);
					c=cident();
					state._fsp--;

					match(input,169,FOLLOW_169_in_unaliasedSelector1260); 
					 tmp = new Selectable.WritetimeOrTTL.Raw(c, true); 
					}
					break;
				case 4 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:328:10: K_TTL '(' c= cident ')'
					{
					match(input,K_TTL,FOLLOW_K_TTL_in_unaliasedSelector1286); 
					match(input,168,FOLLOW_168_in_unaliasedSelector1294); 
					pushFollow(FOLLOW_cident_in_unaliasedSelector1298);
					c=cident();
					state._fsp--;

					match(input,169,FOLLOW_169_in_unaliasedSelector1300); 
					 tmp = new Selectable.WritetimeOrTTL.Raw(c, false); 
					}
					break;
				case 5 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:329:10: f= functionName args= selectionFunctionArgs
					{
					pushFollow(FOLLOW_functionName_in_unaliasedSelector1328);
					f=functionName();
					state._fsp--;

					pushFollow(FOLLOW_selectionFunctionArgs_in_unaliasedSelector1332);
					args=selectionFunctionArgs();
					state._fsp--;

					 tmp = new Selectable.WithFunction.Raw(f, args); 
					}
					break;

			}

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:330:10: ( '.' fi= cident )*
			loop14:
			while (true) {
				int alt14=2;
				int LA14_0 = input.LA(1);
				if ( (LA14_0==173) ) {
					alt14=1;
				}

				switch (alt14) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:330:12: '.' fi= cident
					{
					match(input,173,FOLLOW_173_in_unaliasedSelector1347); 
					pushFollow(FOLLOW_cident_in_unaliasedSelector1351);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:333:1: selectionFunctionArgs returns [List<Selectable.Raw> a] : ( '(' ')' | '(' s1= unaliasedSelector ( ',' sn= unaliasedSelector )* ')' );
	public final List<Selectable.Raw> selectionFunctionArgs() throws RecognitionException {
		List<Selectable.Raw> a = null;


		Selectable.Raw s1 =null;
		Selectable.Raw sn =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:334:5: ( '(' ')' | '(' s1= unaliasedSelector ( ',' sn= unaliasedSelector )* ')' )
			int alt16=2;
			int LA16_0 = input.LA(1);
			if ( (LA16_0==168) ) {
				int LA16_1 = input.LA(2);
				if ( (LA16_1==169) ) {
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
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:334:7: '(' ')'
					{
					match(input,168,FOLLOW_168_in_selectionFunctionArgs1379); 
					match(input,169,FOLLOW_169_in_selectionFunctionArgs1381); 
					 a = Collections.emptyList(); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:335:7: '(' s1= unaliasedSelector ( ',' sn= unaliasedSelector )* ')'
					{
					match(input,168,FOLLOW_168_in_selectionFunctionArgs1391); 
					pushFollow(FOLLOW_unaliasedSelector_in_selectionFunctionArgs1395);
					s1=unaliasedSelector();
					state._fsp--;

					 List<Selectable.Raw> args = new ArrayList<Selectable.Raw>(); args.add(s1); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:336:11: ( ',' sn= unaliasedSelector )*
					loop15:
					while (true) {
						int alt15=2;
						int LA15_0 = input.LA(1);
						if ( (LA15_0==171) ) {
							alt15=1;
						}

						switch (alt15) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:336:13: ',' sn= unaliasedSelector
							{
							match(input,171,FOLLOW_171_in_selectionFunctionArgs1411); 
							pushFollow(FOLLOW_unaliasedSelector_in_selectionFunctionArgs1415);
							sn=unaliasedSelector();
							state._fsp--;

							 args.add(sn); 
							}
							break;

						default :
							break loop15;
						}
					}

					match(input,169,FOLLOW_169_in_selectionFunctionArgs1428); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:340:1: countArgument : ( '\\*' |i= INTEGER );
	public final void countArgument() throws RecognitionException {
		Token i=null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:341:5: ( '\\*' |i= INTEGER )
			int alt17=2;
			int LA17_0 = input.LA(1);
			if ( (LA17_0==182) ) {
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
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:341:7: '\\*'
					{
					match(input,182,FOLLOW_182_in_countArgument1447); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:342:7: i= INTEGER
					{
					i=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_countArgument1457); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:345:1: whereClause returns [List<Relation> clause] : relation[$clause] ( K_AND relation[$clause] )* ;
	public final List<Relation> whereClause() throws RecognitionException {
		List<Relation> clause = null;


		 clause = new ArrayList<Relation>(); 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:347:5: ( relation[$clause] ( K_AND relation[$clause] )* )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:347:7: relation[$clause] ( K_AND relation[$clause] )*
			{
			pushFollow(FOLLOW_relation_in_whereClause1488);
			relation(clause);
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:347:25: ( K_AND relation[$clause] )*
			loop18:
			while (true) {
				int alt18=2;
				int LA18_0 = input.LA(1);
				if ( (LA18_0==K_AND) ) {
					alt18=1;
				}

				switch (alt18) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:347:26: K_AND relation[$clause]
					{
					match(input,K_AND,FOLLOW_K_AND_in_whereClause1492); 
					pushFollow(FOLLOW_relation_in_whereClause1494);
					relation(clause);
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



	// $ANTLR start "orderByClause"
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:350:1: orderByClause[Map<ColumnIdentifier.Raw, Boolean> orderings] : c= cident ( K_ASC | K_DESC )? ;
	public final void orderByClause(Map<ColumnIdentifier.Raw, Boolean> orderings) throws RecognitionException {
		ColumnIdentifier.Raw c =null;


		        boolean reversed = false;
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:354:5: (c= cident ( K_ASC | K_DESC )? )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:354:7: c= cident ( K_ASC | K_DESC )?
			{
			pushFollow(FOLLOW_cident_in_orderByClause1525);
			c=cident();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:354:16: ( K_ASC | K_DESC )?
			int alt19=3;
			int LA19_0 = input.LA(1);
			if ( (LA19_0==K_ASC) ) {
				alt19=1;
			}
			else if ( (LA19_0==K_DESC) ) {
				alt19=2;
			}
			switch (alt19) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:354:17: K_ASC
					{
					match(input,K_ASC,FOLLOW_K_ASC_in_orderByClause1528); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:354:25: K_DESC
					{
					match(input,K_DESC,FOLLOW_K_DESC_in_orderByClause1532); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:363:1: insertStatement returns [ModificationStatement.Parsed expr] : K_INSERT K_INTO cf= columnFamilyName (st1= normalInsertStatement[cf] | K_JSON st2= jsonInsertStatement[cf] ) ;
	public final ModificationStatement.Parsed insertStatement() throws RecognitionException {
		ModificationStatement.Parsed expr = null;


		CFName cf =null;
		UpdateStatement.ParsedInsert st1 =null;
		UpdateStatement.ParsedInsertJson st2 =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:364:5: ( K_INSERT K_INTO cf= columnFamilyName (st1= normalInsertStatement[cf] | K_JSON st2= jsonInsertStatement[cf] ) )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:364:7: K_INSERT K_INTO cf= columnFamilyName (st1= normalInsertStatement[cf] | K_JSON st2= jsonInsertStatement[cf] )
			{
			match(input,K_INSERT,FOLLOW_K_INSERT_in_insertStatement1561); 
			match(input,K_INTO,FOLLOW_K_INTO_in_insertStatement1563); 
			pushFollow(FOLLOW_columnFamilyName_in_insertStatement1567);
			cf=columnFamilyName();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:365:9: (st1= normalInsertStatement[cf] | K_JSON st2= jsonInsertStatement[cf] )
			int alt20=2;
			int LA20_0 = input.LA(1);
			if ( (LA20_0==168) ) {
				alt20=1;
			}
			else if ( (LA20_0==K_JSON) ) {
				alt20=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 20, 0, input);
				throw nvae;
			}

			switch (alt20) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:365:11: st1= normalInsertStatement[cf]
					{
					pushFollow(FOLLOW_normalInsertStatement_in_insertStatement1581);
					st1=normalInsertStatement(cf);
					state._fsp--;

					 expr = st1; 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:366:11: K_JSON st2= jsonInsertStatement[cf]
					{
					match(input,K_JSON,FOLLOW_K_JSON_in_insertStatement1596); 
					pushFollow(FOLLOW_jsonInsertStatement_in_insertStatement1600);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:369:1: normalInsertStatement[CFName cf] returns [UpdateStatement.ParsedInsert expr] : '(' c1= cident ( ',' cn= cident )* ')' K_VALUES '(' v1= term ( ',' vn= term )* ')' ( K_IF K_NOT K_EXISTS )? ( usingClause[attrs] )? ;
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
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:376:5: ( '(' c1= cident ( ',' cn= cident )* ')' K_VALUES '(' v1= term ( ',' vn= term )* ')' ( K_IF K_NOT K_EXISTS )? ( usingClause[attrs] )? )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:376:7: '(' c1= cident ( ',' cn= cident )* ')' K_VALUES '(' v1= term ( ',' vn= term )* ')' ( K_IF K_NOT K_EXISTS )? ( usingClause[attrs] )?
			{
			match(input,168,FOLLOW_168_in_normalInsertStatement1636); 
			pushFollow(FOLLOW_cident_in_normalInsertStatement1640);
			c1=cident();
			state._fsp--;

			 columnNames.add(c1); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:376:47: ( ',' cn= cident )*
			loop21:
			while (true) {
				int alt21=2;
				int LA21_0 = input.LA(1);
				if ( (LA21_0==171) ) {
					alt21=1;
				}

				switch (alt21) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:376:49: ',' cn= cident
					{
					match(input,171,FOLLOW_171_in_normalInsertStatement1647); 
					pushFollow(FOLLOW_cident_in_normalInsertStatement1651);
					cn=cident();
					state._fsp--;

					 columnNames.add(cn); 
					}
					break;

				default :
					break loop21;
				}
			}

			match(input,169,FOLLOW_169_in_normalInsertStatement1658); 
			match(input,K_VALUES,FOLLOW_K_VALUES_in_normalInsertStatement1666); 
			match(input,168,FOLLOW_168_in_normalInsertStatement1674); 
			pushFollow(FOLLOW_term_in_normalInsertStatement1678);
			v1=term();
			state._fsp--;

			 values.add(v1); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:378:39: ( ',' vn= term )*
			loop22:
			while (true) {
				int alt22=2;
				int LA22_0 = input.LA(1);
				if ( (LA22_0==171) ) {
					alt22=1;
				}

				switch (alt22) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:378:41: ',' vn= term
					{
					match(input,171,FOLLOW_171_in_normalInsertStatement1684); 
					pushFollow(FOLLOW_term_in_normalInsertStatement1688);
					vn=term();
					state._fsp--;

					 values.add(vn); 
					}
					break;

				default :
					break loop22;
				}
			}

			match(input,169,FOLLOW_169_in_normalInsertStatement1695); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:379:7: ( K_IF K_NOT K_EXISTS )?
			int alt23=2;
			int LA23_0 = input.LA(1);
			if ( (LA23_0==K_IF) ) {
				alt23=1;
			}
			switch (alt23) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:379:9: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_normalInsertStatement1705); 
					match(input,K_NOT,FOLLOW_K_NOT_in_normalInsertStatement1707); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_normalInsertStatement1709); 
					 ifNotExists = true; 
					}
					break;

			}

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:380:7: ( usingClause[attrs] )?
			int alt24=2;
			int LA24_0 = input.LA(1);
			if ( (LA24_0==K_USING) ) {
				alt24=1;
			}
			switch (alt24) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:380:9: usingClause[attrs]
					{
					pushFollow(FOLLOW_usingClause_in_normalInsertStatement1724);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:386:1: jsonInsertStatement[CFName cf] returns [UpdateStatement.ParsedInsertJson expr] : val= jsonValue ( K_IF K_NOT K_EXISTS )? ( usingClause[attrs] )? ;
	public final UpdateStatement.ParsedInsertJson jsonInsertStatement(CFName cf) throws RecognitionException {
		UpdateStatement.ParsedInsertJson expr = null;


		Json.Raw val =null;


		        Attributes.Raw attrs = new Attributes.Raw();
		        boolean ifNotExists = false;
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:391:5: (val= jsonValue ( K_IF K_NOT K_EXISTS )? ( usingClause[attrs] )? )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:391:7: val= jsonValue ( K_IF K_NOT K_EXISTS )? ( usingClause[attrs] )?
			{
			pushFollow(FOLLOW_jsonValue_in_jsonInsertStatement1770);
			val=jsonValue();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:392:7: ( K_IF K_NOT K_EXISTS )?
			int alt25=2;
			int LA25_0 = input.LA(1);
			if ( (LA25_0==K_IF) ) {
				alt25=1;
			}
			switch (alt25) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:392:9: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_jsonInsertStatement1780); 
					match(input,K_NOT,FOLLOW_K_NOT_in_jsonInsertStatement1782); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_jsonInsertStatement1784); 
					 ifNotExists = true; 
					}
					break;

			}

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:393:7: ( usingClause[attrs] )?
			int alt26=2;
			int LA26_0 = input.LA(1);
			if ( (LA26_0==K_USING) ) {
				alt26=1;
			}
			switch (alt26) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:393:9: usingClause[attrs]
					{
					pushFollow(FOLLOW_usingClause_in_jsonInsertStatement1799);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:399:1: jsonValue returns [Json.Raw value] : (|s= STRING_LITERAL | ':' id= ident | QMARK );
	public final Json.Raw jsonValue() throws RecognitionException {
		Json.Raw value = null;


		Token s=null;
		ColumnIdentifier id =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:400:5: (|s= STRING_LITERAL | ':' id= ident | QMARK )
			int alt27=4;
			switch ( input.LA(1) ) {
			case EOF:
			case K_APPLY:
			case K_DELETE:
			case K_IF:
			case K_INSERT:
			case K_UPDATE:
			case K_USING:
			case 175:
				{
				alt27=1;
				}
				break;
			case STRING_LITERAL:
				{
				alt27=2;
				}
				break;
			case 174:
				{
				alt27=3;
				}
				break;
			case QMARK:
				{
				alt27=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 27, 0, input);
				throw nvae;
			}
			switch (alt27) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:401:5: 
					{
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:401:7: s= STRING_LITERAL
					{
					s=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_jsonValue1840); 
					 value = new Json.Literal((s!=null?s.getText():null)); 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:402:7: ':' id= ident
					{
					match(input,174,FOLLOW_174_in_jsonValue1850); 
					pushFollow(FOLLOW_ident_in_jsonValue1854);
					id=ident();
					state._fsp--;

					 value = newJsonBindVariables(id); 
					}
					break;
				case 4 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:403:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_jsonValue1868); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:406:1: usingClause[Attributes.Raw attrs] : K_USING usingClauseObjective[attrs] ( K_AND usingClauseObjective[attrs] )* ;
	public final void usingClause(Attributes.Raw attrs) throws RecognitionException {
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:407:5: ( K_USING usingClauseObjective[attrs] ( K_AND usingClauseObjective[attrs] )* )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:407:7: K_USING usingClauseObjective[attrs] ( K_AND usingClauseObjective[attrs] )*
			{
			match(input,K_USING,FOLLOW_K_USING_in_usingClause1899); 
			pushFollow(FOLLOW_usingClauseObjective_in_usingClause1901);
			usingClauseObjective(attrs);
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:407:43: ( K_AND usingClauseObjective[attrs] )*
			loop28:
			while (true) {
				int alt28=2;
				int LA28_0 = input.LA(1);
				if ( (LA28_0==K_AND) ) {
					alt28=1;
				}

				switch (alt28) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:407:45: K_AND usingClauseObjective[attrs]
					{
					match(input,K_AND,FOLLOW_K_AND_in_usingClause1906); 
					pushFollow(FOLLOW_usingClauseObjective_in_usingClause1908);
					usingClauseObjective(attrs);
					state._fsp--;

					}
					break;

				default :
					break loop28;
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:410:1: usingClauseObjective[Attributes.Raw attrs] : ( K_TIMESTAMP ts= intValue | K_TTL t= intValue );
	public final void usingClauseObjective(Attributes.Raw attrs) throws RecognitionException {
		Term.Raw ts =null;
		Term.Raw t =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:411:5: ( K_TIMESTAMP ts= intValue | K_TTL t= intValue )
			int alt29=2;
			int LA29_0 = input.LA(1);
			if ( (LA29_0==K_TIMESTAMP) ) {
				alt29=1;
			}
			else if ( (LA29_0==K_TTL) ) {
				alt29=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 29, 0, input);
				throw nvae;
			}

			switch (alt29) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:411:7: K_TIMESTAMP ts= intValue
					{
					match(input,K_TIMESTAMP,FOLLOW_K_TIMESTAMP_in_usingClauseObjective1930); 
					pushFollow(FOLLOW_intValue_in_usingClauseObjective1934);
					ts=intValue();
					state._fsp--;

					 attrs.timestamp = ts; 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:412:7: K_TTL t= intValue
					{
					match(input,K_TTL,FOLLOW_K_TTL_in_usingClauseObjective1944); 
					pushFollow(FOLLOW_intValue_in_usingClauseObjective1948);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:422:1: updateStatement returns [UpdateStatement.ParsedUpdate expr] : K_UPDATE cf= columnFamilyName ( usingClause[attrs] )? K_SET columnOperation[operations] ( ',' columnOperation[operations] )* K_WHERE wclause= whereClause ( K_IF ( K_EXISTS |conditions= updateConditions ) )? ;
	public final UpdateStatement.ParsedUpdate updateStatement() throws RecognitionException {
		UpdateStatement.ParsedUpdate expr = null;


		CFName cf =null;
		List<Relation> wclause =null;
		List<Pair<ColumnIdentifier.Raw, ColumnCondition.Raw>> conditions =null;


		        Attributes.Raw attrs = new Attributes.Raw();
		        List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations = new ArrayList<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>>();
		        boolean ifExists = false;
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:428:5: ( K_UPDATE cf= columnFamilyName ( usingClause[attrs] )? K_SET columnOperation[operations] ( ',' columnOperation[operations] )* K_WHERE wclause= whereClause ( K_IF ( K_EXISTS |conditions= updateConditions ) )? )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:428:7: K_UPDATE cf= columnFamilyName ( usingClause[attrs] )? K_SET columnOperation[operations] ( ',' columnOperation[operations] )* K_WHERE wclause= whereClause ( K_IF ( K_EXISTS |conditions= updateConditions ) )?
			{
			match(input,K_UPDATE,FOLLOW_K_UPDATE_in_updateStatement1982); 
			pushFollow(FOLLOW_columnFamilyName_in_updateStatement1986);
			cf=columnFamilyName();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:429:7: ( usingClause[attrs] )?
			int alt30=2;
			int LA30_0 = input.LA(1);
			if ( (LA30_0==K_USING) ) {
				alt30=1;
			}
			switch (alt30) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:429:9: usingClause[attrs]
					{
					pushFollow(FOLLOW_usingClause_in_updateStatement1996);
					usingClause(attrs);
					state._fsp--;

					}
					break;

			}

			match(input,K_SET,FOLLOW_K_SET_in_updateStatement2008); 
			pushFollow(FOLLOW_columnOperation_in_updateStatement2010);
			columnOperation(operations);
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:430:41: ( ',' columnOperation[operations] )*
			loop31:
			while (true) {
				int alt31=2;
				int LA31_0 = input.LA(1);
				if ( (LA31_0==171) ) {
					alt31=1;
				}

				switch (alt31) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:430:42: ',' columnOperation[operations]
					{
					match(input,171,FOLLOW_171_in_updateStatement2014); 
					pushFollow(FOLLOW_columnOperation_in_updateStatement2016);
					columnOperation(operations);
					state._fsp--;

					}
					break;

				default :
					break loop31;
				}
			}

			match(input,K_WHERE,FOLLOW_K_WHERE_in_updateStatement2027); 
			pushFollow(FOLLOW_whereClause_in_updateStatement2031);
			wclause=whereClause();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:432:7: ( K_IF ( K_EXISTS |conditions= updateConditions ) )?
			int alt33=2;
			int LA33_0 = input.LA(1);
			if ( (LA33_0==K_IF) ) {
				alt33=1;
			}
			switch (alt33) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:432:9: K_IF ( K_EXISTS |conditions= updateConditions )
					{
					match(input,K_IF,FOLLOW_K_IF_in_updateStatement2041); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:432:14: ( K_EXISTS |conditions= updateConditions )
					int alt32=2;
					int LA32_0 = input.LA(1);
					if ( (LA32_0==K_EXISTS) ) {
						int LA32_1 = input.LA(2);
						if ( (LA32_1==EOF||LA32_1==K_APPLY||LA32_1==K_DELETE||LA32_1==K_INSERT||LA32_1==K_UPDATE||LA32_1==175) ) {
							alt32=1;
						}
						else if ( (LA32_1==K_IN||LA32_1==167||(LA32_1 >= 176 && LA32_1 <= 181)) ) {
							alt32=2;
						}

						else {
							int nvaeMark = input.mark();
							try {
								input.consume();
								NoViableAltException nvae =
									new NoViableAltException("", 32, 1, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}
					else if ( (LA32_0==IDENT||(LA32_0 >= K_AGGREGATE && LA32_0 <= K_ALL)||LA32_0==K_AS||LA32_0==K_ASCII||(LA32_0 >= K_BIGINT && LA32_0 <= K_BOOLEAN)||(LA32_0 >= K_CALLED && LA32_0 <= K_CLUSTERING)||(LA32_0 >= K_COMPACT && LA32_0 <= K_COUNTER)||(LA32_0 >= K_CUSTOM && LA32_0 <= K_DECIMAL)||(LA32_0 >= K_DISTINCT && LA32_0 <= K_DOUBLE)||(LA32_0 >= K_FILTERING && LA32_0 <= K_FLOAT)||LA32_0==K_FROZEN||(LA32_0 >= K_FUNCTION && LA32_0 <= K_FUNCTIONS)||LA32_0==K_INET||(LA32_0 >= K_INITCOND && LA32_0 <= K_INPUT)||LA32_0==K_INT||(LA32_0 >= K_JSON && LA32_0 <= K_KEYS)||(LA32_0 >= K_KEYSPACES && LA32_0 <= K_LANGUAGE)||(LA32_0 >= K_LIST && LA32_0 <= K_MAP)||LA32_0==K_NOLOGIN||LA32_0==K_NOSUPERUSER||LA32_0==K_OPTIONS||(LA32_0 >= K_PASSWORD && LA32_0 <= K_PERMISSIONS)||LA32_0==K_RETURNS||(LA32_0 >= K_ROLE && LA32_0 <= K_ROLES)||(LA32_0 >= K_SFUNC && LA32_0 <= K_TINYINT)||LA32_0==K_TRIGGER||(LA32_0 >= K_TTL && LA32_0 <= K_TYPE)||(LA32_0 >= K_USER && LA32_0 <= K_USERS)||(LA32_0 >= K_UUID && LA32_0 <= K_VARINT)||LA32_0==K_WRITETIME||LA32_0==QUOTED_NAME) ) {
						alt32=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 32, 0, input);
						throw nvae;
					}

					switch (alt32) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:432:16: K_EXISTS
							{
							match(input,K_EXISTS,FOLLOW_K_EXISTS_in_updateStatement2045); 
							 ifExists = true; 
							}
							break;
						case 2 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:432:48: conditions= updateConditions
							{
							pushFollow(FOLLOW_updateConditions_in_updateStatement2053);
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
			                                                  wclause,
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:443:1: updateConditions returns [List<Pair<ColumnIdentifier.Raw, ColumnCondition.Raw>> conditions] : columnCondition[conditions] ( K_AND columnCondition[conditions] )* ;
	public final List<Pair<ColumnIdentifier.Raw, ColumnCondition.Raw>> updateConditions() throws RecognitionException {
		List<Pair<ColumnIdentifier.Raw, ColumnCondition.Raw>> conditions = null;


		 conditions = new ArrayList<Pair<ColumnIdentifier.Raw, ColumnCondition.Raw>>(); 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:445:5: ( columnCondition[conditions] ( K_AND columnCondition[conditions] )* )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:445:7: columnCondition[conditions] ( K_AND columnCondition[conditions] )*
			{
			pushFollow(FOLLOW_columnCondition_in_updateConditions2095);
			columnCondition(conditions);
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:445:35: ( K_AND columnCondition[conditions] )*
			loop34:
			while (true) {
				int alt34=2;
				int LA34_0 = input.LA(1);
				if ( (LA34_0==K_AND) ) {
					alt34=1;
				}

				switch (alt34) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:445:37: K_AND columnCondition[conditions]
					{
					match(input,K_AND,FOLLOW_K_AND_in_updateConditions2100); 
					pushFollow(FOLLOW_columnCondition_in_updateConditions2102);
					columnCondition(conditions);
					state._fsp--;

					}
					break;

				default :
					break loop34;
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:456:1: deleteStatement returns [DeleteStatement.Parsed expr] : K_DELETE (dels= deleteSelection )? K_FROM cf= columnFamilyName ( usingClauseDelete[attrs] )? K_WHERE wclause= whereClause ( K_IF ( K_EXISTS |conditions= updateConditions ) )? ;
	public final DeleteStatement.Parsed deleteStatement() throws RecognitionException {
		DeleteStatement.Parsed expr = null;


		List<Operation.RawDeletion> dels =null;
		CFName cf =null;
		List<Relation> wclause =null;
		List<Pair<ColumnIdentifier.Raw, ColumnCondition.Raw>> conditions =null;


		        Attributes.Raw attrs = new Attributes.Raw();
		        List<Operation.RawDeletion> columnDeletions = Collections.emptyList();
		        boolean ifExists = false;
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:462:5: ( K_DELETE (dels= deleteSelection )? K_FROM cf= columnFamilyName ( usingClauseDelete[attrs] )? K_WHERE wclause= whereClause ( K_IF ( K_EXISTS |conditions= updateConditions ) )? )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:462:7: K_DELETE (dels= deleteSelection )? K_FROM cf= columnFamilyName ( usingClauseDelete[attrs] )? K_WHERE wclause= whereClause ( K_IF ( K_EXISTS |conditions= updateConditions ) )?
			{
			match(input,K_DELETE,FOLLOW_K_DELETE_in_deleteStatement2139); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:462:16: (dels= deleteSelection )?
			int alt35=2;
			int LA35_0 = input.LA(1);
			if ( (LA35_0==IDENT||(LA35_0 >= K_AGGREGATE && LA35_0 <= K_ALL)||LA35_0==K_AS||LA35_0==K_ASCII||(LA35_0 >= K_BIGINT && LA35_0 <= K_BOOLEAN)||(LA35_0 >= K_CALLED && LA35_0 <= K_CLUSTERING)||(LA35_0 >= K_COMPACT && LA35_0 <= K_COUNTER)||(LA35_0 >= K_CUSTOM && LA35_0 <= K_DECIMAL)||(LA35_0 >= K_DISTINCT && LA35_0 <= K_DOUBLE)||(LA35_0 >= K_EXISTS && LA35_0 <= K_FLOAT)||LA35_0==K_FROZEN||(LA35_0 >= K_FUNCTION && LA35_0 <= K_FUNCTIONS)||LA35_0==K_INET||(LA35_0 >= K_INITCOND && LA35_0 <= K_INPUT)||LA35_0==K_INT||(LA35_0 >= K_JSON && LA35_0 <= K_KEYS)||(LA35_0 >= K_KEYSPACES && LA35_0 <= K_LANGUAGE)||(LA35_0 >= K_LIST && LA35_0 <= K_MAP)||LA35_0==K_NOLOGIN||LA35_0==K_NOSUPERUSER||LA35_0==K_OPTIONS||(LA35_0 >= K_PASSWORD && LA35_0 <= K_PERMISSIONS)||LA35_0==K_RETURNS||(LA35_0 >= K_ROLE && LA35_0 <= K_ROLES)||(LA35_0 >= K_SFUNC && LA35_0 <= K_TINYINT)||LA35_0==K_TRIGGER||(LA35_0 >= K_TTL && LA35_0 <= K_TYPE)||(LA35_0 >= K_USER && LA35_0 <= K_USERS)||(LA35_0 >= K_UUID && LA35_0 <= K_VARINT)||LA35_0==K_WRITETIME||LA35_0==QUOTED_NAME) ) {
				alt35=1;
			}
			switch (alt35) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:462:18: dels= deleteSelection
					{
					pushFollow(FOLLOW_deleteSelection_in_deleteStatement2145);
					dels=deleteSelection();
					state._fsp--;

					 columnDeletions = dels; 
					}
					break;

			}

			match(input,K_FROM,FOLLOW_K_FROM_in_deleteStatement2158); 
			pushFollow(FOLLOW_columnFamilyName_in_deleteStatement2162);
			cf=columnFamilyName();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:464:7: ( usingClauseDelete[attrs] )?
			int alt36=2;
			int LA36_0 = input.LA(1);
			if ( (LA36_0==K_USING) ) {
				alt36=1;
			}
			switch (alt36) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:464:9: usingClauseDelete[attrs]
					{
					pushFollow(FOLLOW_usingClauseDelete_in_deleteStatement2172);
					usingClauseDelete(attrs);
					state._fsp--;

					}
					break;

			}

			match(input,K_WHERE,FOLLOW_K_WHERE_in_deleteStatement2184); 
			pushFollow(FOLLOW_whereClause_in_deleteStatement2188);
			wclause=whereClause();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:466:7: ( K_IF ( K_EXISTS |conditions= updateConditions ) )?
			int alt38=2;
			int LA38_0 = input.LA(1);
			if ( (LA38_0==K_IF) ) {
				alt38=1;
			}
			switch (alt38) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:466:9: K_IF ( K_EXISTS |conditions= updateConditions )
					{
					match(input,K_IF,FOLLOW_K_IF_in_deleteStatement2198); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:466:14: ( K_EXISTS |conditions= updateConditions )
					int alt37=2;
					int LA37_0 = input.LA(1);
					if ( (LA37_0==K_EXISTS) ) {
						int LA37_1 = input.LA(2);
						if ( (LA37_1==EOF||LA37_1==K_APPLY||LA37_1==K_DELETE||LA37_1==K_INSERT||LA37_1==K_UPDATE||LA37_1==175) ) {
							alt37=1;
						}
						else if ( (LA37_1==K_IN||LA37_1==167||(LA37_1 >= 176 && LA37_1 <= 181)) ) {
							alt37=2;
						}

						else {
							int nvaeMark = input.mark();
							try {
								input.consume();
								NoViableAltException nvae =
									new NoViableAltException("", 37, 1, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}
					else if ( (LA37_0==IDENT||(LA37_0 >= K_AGGREGATE && LA37_0 <= K_ALL)||LA37_0==K_AS||LA37_0==K_ASCII||(LA37_0 >= K_BIGINT && LA37_0 <= K_BOOLEAN)||(LA37_0 >= K_CALLED && LA37_0 <= K_CLUSTERING)||(LA37_0 >= K_COMPACT && LA37_0 <= K_COUNTER)||(LA37_0 >= K_CUSTOM && LA37_0 <= K_DECIMAL)||(LA37_0 >= K_DISTINCT && LA37_0 <= K_DOUBLE)||(LA37_0 >= K_FILTERING && LA37_0 <= K_FLOAT)||LA37_0==K_FROZEN||(LA37_0 >= K_FUNCTION && LA37_0 <= K_FUNCTIONS)||LA37_0==K_INET||(LA37_0 >= K_INITCOND && LA37_0 <= K_INPUT)||LA37_0==K_INT||(LA37_0 >= K_JSON && LA37_0 <= K_KEYS)||(LA37_0 >= K_KEYSPACES && LA37_0 <= K_LANGUAGE)||(LA37_0 >= K_LIST && LA37_0 <= K_MAP)||LA37_0==K_NOLOGIN||LA37_0==K_NOSUPERUSER||LA37_0==K_OPTIONS||(LA37_0 >= K_PASSWORD && LA37_0 <= K_PERMISSIONS)||LA37_0==K_RETURNS||(LA37_0 >= K_ROLE && LA37_0 <= K_ROLES)||(LA37_0 >= K_SFUNC && LA37_0 <= K_TINYINT)||LA37_0==K_TRIGGER||(LA37_0 >= K_TTL && LA37_0 <= K_TYPE)||(LA37_0 >= K_USER && LA37_0 <= K_USERS)||(LA37_0 >= K_UUID && LA37_0 <= K_VARINT)||LA37_0==K_WRITETIME||LA37_0==QUOTED_NAME) ) {
						alt37=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 37, 0, input);
						throw nvae;
					}

					switch (alt37) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:466:16: K_EXISTS
							{
							match(input,K_EXISTS,FOLLOW_K_EXISTS_in_deleteStatement2202); 
							 ifExists = true; 
							}
							break;
						case 2 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:466:48: conditions= updateConditions
							{
							pushFollow(FOLLOW_updateConditions_in_deleteStatement2210);
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
			                                            wclause,
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:477:1: deleteSelection returns [List<Operation.RawDeletion> operations] :t1= deleteOp ( ',' tN= deleteOp )* ;
	public final List<Operation.RawDeletion> deleteSelection() throws RecognitionException {
		List<Operation.RawDeletion> operations = null;


		Operation.RawDeletion t1 =null;
		Operation.RawDeletion tN =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:478:5: (t1= deleteOp ( ',' tN= deleteOp )* )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:478:7: t1= deleteOp ( ',' tN= deleteOp )*
			{
			 operations = new ArrayList<Operation.RawDeletion>(); 
			pushFollow(FOLLOW_deleteOp_in_deleteSelection2257);
			t1=deleteOp();
			state._fsp--;

			 operations.add(t1); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:480:11: ( ',' tN= deleteOp )*
			loop39:
			while (true) {
				int alt39=2;
				int LA39_0 = input.LA(1);
				if ( (LA39_0==171) ) {
					alt39=1;
				}

				switch (alt39) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:480:12: ',' tN= deleteOp
					{
					match(input,171,FOLLOW_171_in_deleteSelection2272); 
					pushFollow(FOLLOW_deleteOp_in_deleteSelection2276);
					tN=deleteOp();
					state._fsp--;

					 operations.add(tN); 
					}
					break;

				default :
					break loop39;
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:483:1: deleteOp returns [Operation.RawDeletion op] : (c= cident |c= cident '[' t= term ']' );
	public final Operation.RawDeletion deleteOp() throws RecognitionException {
		Operation.RawDeletion op = null;


		ColumnIdentifier.Raw c =null;
		Term.Raw t =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:484:5: (c= cident |c= cident '[' t= term ']' )
			int alt40=2;
			alt40 = dfa40.predict(input);
			switch (alt40) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:484:7: c= cident
					{
					pushFollow(FOLLOW_cident_in_deleteOp2303);
					c=cident();
					state._fsp--;

					 op = new Operation.ColumnDeletion(c); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:485:7: c= cident '[' t= term ']'
					{
					pushFollow(FOLLOW_cident_in_deleteOp2330);
					c=cident();
					state._fsp--;

					match(input,181,FOLLOW_181_in_deleteOp2332); 
					pushFollow(FOLLOW_term_in_deleteOp2336);
					t=term();
					state._fsp--;

					match(input,183,FOLLOW_183_in_deleteOp2338); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:488:1: usingClauseDelete[Attributes.Raw attrs] : K_USING K_TIMESTAMP ts= intValue ;
	public final void usingClauseDelete(Attributes.Raw attrs) throws RecognitionException {
		Term.Raw ts =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:489:5: ( K_USING K_TIMESTAMP ts= intValue )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:489:7: K_USING K_TIMESTAMP ts= intValue
			{
			match(input,K_USING,FOLLOW_K_USING_in_usingClauseDelete2358); 
			match(input,K_TIMESTAMP,FOLLOW_K_TIMESTAMP_in_usingClauseDelete2360); 
			pushFollow(FOLLOW_intValue_in_usingClauseDelete2364);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:516:1: batchStatement returns [BatchStatement.Parsed expr] : K_BEGIN ( K_UNLOGGED | K_COUNTER )? K_BATCH ( usingClause[attrs] )? (s= batchStatementObjective ( ';' )? )* K_APPLY K_BATCH ;
	public final BatchStatement.Parsed batchStatement() throws RecognitionException {
		BatchStatement.Parsed expr = null;


		ModificationStatement.Parsed s =null;


		        BatchStatement.Type type = BatchStatement.Type.LOGGED;
		        List<ModificationStatement.Parsed> statements = new ArrayList<ModificationStatement.Parsed>();
		        Attributes.Raw attrs = new Attributes.Raw();
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:522:5: ( K_BEGIN ( K_UNLOGGED | K_COUNTER )? K_BATCH ( usingClause[attrs] )? (s= batchStatementObjective ( ';' )? )* K_APPLY K_BATCH )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:522:7: K_BEGIN ( K_UNLOGGED | K_COUNTER )? K_BATCH ( usingClause[attrs] )? (s= batchStatementObjective ( ';' )? )* K_APPLY K_BATCH
			{
			match(input,K_BEGIN,FOLLOW_K_BEGIN_in_batchStatement2398); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:523:7: ( K_UNLOGGED | K_COUNTER )?
			int alt41=3;
			int LA41_0 = input.LA(1);
			if ( (LA41_0==K_UNLOGGED) ) {
				alt41=1;
			}
			else if ( (LA41_0==K_COUNTER) ) {
				alt41=2;
			}
			switch (alt41) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:523:9: K_UNLOGGED
					{
					match(input,K_UNLOGGED,FOLLOW_K_UNLOGGED_in_batchStatement2408); 
					 type = BatchStatement.Type.UNLOGGED; 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:523:63: K_COUNTER
					{
					match(input,K_COUNTER,FOLLOW_K_COUNTER_in_batchStatement2414); 
					 type = BatchStatement.Type.COUNTER; 
					}
					break;

			}

			match(input,K_BATCH,FOLLOW_K_BATCH_in_batchStatement2427); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:524:15: ( usingClause[attrs] )?
			int alt42=2;
			int LA42_0 = input.LA(1);
			if ( (LA42_0==K_USING) ) {
				alt42=1;
			}
			switch (alt42) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:524:17: usingClause[attrs]
					{
					pushFollow(FOLLOW_usingClause_in_batchStatement2431);
					usingClause(attrs);
					state._fsp--;

					}
					break;

			}

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:525:11: (s= batchStatementObjective ( ';' )? )*
			loop44:
			while (true) {
				int alt44=2;
				int LA44_0 = input.LA(1);
				if ( (LA44_0==K_DELETE||LA44_0==K_INSERT||LA44_0==K_UPDATE) ) {
					alt44=1;
				}

				switch (alt44) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:525:13: s= batchStatementObjective ( ';' )?
					{
					pushFollow(FOLLOW_batchStatementObjective_in_batchStatement2451);
					s=batchStatementObjective();
					state._fsp--;

					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:525:39: ( ';' )?
					int alt43=2;
					int LA43_0 = input.LA(1);
					if ( (LA43_0==175) ) {
						alt43=1;
					}
					switch (alt43) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:525:39: ';'
							{
							match(input,175,FOLLOW_175_in_batchStatement2453); 
							}
							break;

					}

					 statements.add(s); 
					}
					break;

				default :
					break loop44;
				}
			}

			match(input,K_APPLY,FOLLOW_K_APPLY_in_batchStatement2467); 
			match(input,K_BATCH,FOLLOW_K_BATCH_in_batchStatement2469); 

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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:532:1: batchStatementObjective returns [ModificationStatement.Parsed statement] : (i= insertStatement |u= updateStatement |d= deleteStatement );
	public final ModificationStatement.Parsed batchStatementObjective() throws RecognitionException {
		ModificationStatement.Parsed statement = null;


		ModificationStatement.Parsed i =null;
		UpdateStatement.ParsedUpdate u =null;
		DeleteStatement.Parsed d =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:533:5: (i= insertStatement |u= updateStatement |d= deleteStatement )
			int alt45=3;
			switch ( input.LA(1) ) {
			case K_INSERT:
				{
				alt45=1;
				}
				break;
			case K_UPDATE:
				{
				alt45=2;
				}
				break;
			case K_DELETE:
				{
				alt45=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 45, 0, input);
				throw nvae;
			}
			switch (alt45) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:533:7: i= insertStatement
					{
					pushFollow(FOLLOW_insertStatement_in_batchStatementObjective2500);
					i=insertStatement();
					state._fsp--;

					 statement = i; 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:534:7: u= updateStatement
					{
					pushFollow(FOLLOW_updateStatement_in_batchStatementObjective2513);
					u=updateStatement();
					state._fsp--;

					 statement = u; 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:535:7: d= deleteStatement
					{
					pushFollow(FOLLOW_deleteStatement_in_batchStatementObjective2526);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:538:1: createAggregateStatement returns [CreateAggregateStatement expr] : K_CREATE ( K_OR K_REPLACE )? K_AGGREGATE ( K_IF K_NOT K_EXISTS )? fn= functionName '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' K_SFUNC sfunc= allowedFunctionName K_STYPE stype= comparatorType ( K_FINALFUNC ffunc= allowedFunctionName )? ( K_INITCOND ival= term )? ;
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
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:545:5: ( K_CREATE ( K_OR K_REPLACE )? K_AGGREGATE ( K_IF K_NOT K_EXISTS )? fn= functionName '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' K_SFUNC sfunc= allowedFunctionName K_STYPE stype= comparatorType ( K_FINALFUNC ffunc= allowedFunctionName )? ( K_INITCOND ival= term )? )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:545:7: K_CREATE ( K_OR K_REPLACE )? K_AGGREGATE ( K_IF K_NOT K_EXISTS )? fn= functionName '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' K_SFUNC sfunc= allowedFunctionName K_STYPE stype= comparatorType ( K_FINALFUNC ffunc= allowedFunctionName )? ( K_INITCOND ival= term )?
			{
			match(input,K_CREATE,FOLLOW_K_CREATE_in_createAggregateStatement2559); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:545:16: ( K_OR K_REPLACE )?
			int alt46=2;
			int LA46_0 = input.LA(1);
			if ( (LA46_0==K_OR) ) {
				alt46=1;
			}
			switch (alt46) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:545:17: K_OR K_REPLACE
					{
					match(input,K_OR,FOLLOW_K_OR_in_createAggregateStatement2562); 
					match(input,K_REPLACE,FOLLOW_K_REPLACE_in_createAggregateStatement2564); 
					 orReplace = true; 
					}
					break;

			}

			match(input,K_AGGREGATE,FOLLOW_K_AGGREGATE_in_createAggregateStatement2576); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:547:7: ( K_IF K_NOT K_EXISTS )?
			int alt47=2;
			int LA47_0 = input.LA(1);
			if ( (LA47_0==K_IF) ) {
				alt47=1;
			}
			switch (alt47) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:547:8: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_createAggregateStatement2585); 
					match(input,K_NOT,FOLLOW_K_NOT_in_createAggregateStatement2587); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createAggregateStatement2589); 
					 ifNotExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_functionName_in_createAggregateStatement2603);
			fn=functionName();
			state._fsp--;

			match(input,168,FOLLOW_168_in_createAggregateStatement2611); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:550:9: (v= comparatorType ( ',' v= comparatorType )* )?
			int alt49=2;
			int LA49_0 = input.LA(1);
			if ( (LA49_0==IDENT||(LA49_0 >= K_AGGREGATE && LA49_0 <= K_ALL)||LA49_0==K_AS||LA49_0==K_ASCII||(LA49_0 >= K_BIGINT && LA49_0 <= K_BOOLEAN)||(LA49_0 >= K_CALLED && LA49_0 <= K_CLUSTERING)||(LA49_0 >= K_COMPACT && LA49_0 <= K_COUNTER)||(LA49_0 >= K_CUSTOM && LA49_0 <= K_DECIMAL)||(LA49_0 >= K_DISTINCT && LA49_0 <= K_DOUBLE)||(LA49_0 >= K_EXISTS && LA49_0 <= K_FLOAT)||LA49_0==K_FROZEN||(LA49_0 >= K_FUNCTION && LA49_0 <= K_FUNCTIONS)||LA49_0==K_INET||(LA49_0 >= K_INITCOND && LA49_0 <= K_INPUT)||LA49_0==K_INT||(LA49_0 >= K_JSON && LA49_0 <= K_KEYS)||(LA49_0 >= K_KEYSPACES && LA49_0 <= K_LANGUAGE)||(LA49_0 >= K_LIST && LA49_0 <= K_MAP)||LA49_0==K_NOLOGIN||LA49_0==K_NOSUPERUSER||LA49_0==K_OPTIONS||(LA49_0 >= K_PASSWORD && LA49_0 <= K_PERMISSIONS)||LA49_0==K_RETURNS||(LA49_0 >= K_ROLE && LA49_0 <= K_ROLES)||(LA49_0 >= K_SET && LA49_0 <= K_TINYINT)||LA49_0==K_TRIGGER||(LA49_0 >= K_TTL && LA49_0 <= K_TYPE)||(LA49_0 >= K_USER && LA49_0 <= K_USERS)||(LA49_0 >= K_UUID && LA49_0 <= K_VARINT)||LA49_0==K_WRITETIME||LA49_0==QUOTED_NAME||LA49_0==STRING_LITERAL) ) {
				alt49=1;
			}
			switch (alt49) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:551:11: v= comparatorType ( ',' v= comparatorType )*
					{
					pushFollow(FOLLOW_comparatorType_in_createAggregateStatement2635);
					v=comparatorType();
					state._fsp--;

					 argsTypes.add(v); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:552:11: ( ',' v= comparatorType )*
					loop48:
					while (true) {
						int alt48=2;
						int LA48_0 = input.LA(1);
						if ( (LA48_0==171) ) {
							alt48=1;
						}

						switch (alt48) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:552:13: ',' v= comparatorType
							{
							match(input,171,FOLLOW_171_in_createAggregateStatement2651); 
							pushFollow(FOLLOW_comparatorType_in_createAggregateStatement2655);
							v=comparatorType();
							state._fsp--;

							 argsTypes.add(v); 
							}
							break;

						default :
							break loop48;
						}
					}

					}
					break;

			}

			match(input,169,FOLLOW_169_in_createAggregateStatement2679); 
			match(input,K_SFUNC,FOLLOW_K_SFUNC_in_createAggregateStatement2687); 
			pushFollow(FOLLOW_allowedFunctionName_in_createAggregateStatement2693);
			sfunc=allowedFunctionName();
			state._fsp--;

			match(input,K_STYPE,FOLLOW_K_STYPE_in_createAggregateStatement2701); 
			pushFollow(FOLLOW_comparatorType_in_createAggregateStatement2707);
			stype=comparatorType();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:557:7: ( K_FINALFUNC ffunc= allowedFunctionName )?
			int alt50=2;
			int LA50_0 = input.LA(1);
			if ( (LA50_0==K_FINALFUNC) ) {
				alt50=1;
			}
			switch (alt50) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:558:9: K_FINALFUNC ffunc= allowedFunctionName
					{
					match(input,K_FINALFUNC,FOLLOW_K_FINALFUNC_in_createAggregateStatement2725); 
					pushFollow(FOLLOW_allowedFunctionName_in_createAggregateStatement2731);
					ffunc=allowedFunctionName();
					state._fsp--;

					}
					break;

			}

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:560:7: ( K_INITCOND ival= term )?
			int alt51=2;
			int LA51_0 = input.LA(1);
			if ( (LA51_0==K_INITCOND) ) {
				alt51=1;
			}
			switch (alt51) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:561:9: K_INITCOND ival= term
					{
					match(input,K_INITCOND,FOLLOW_K_INITCOND_in_createAggregateStatement2758); 
					pushFollow(FOLLOW_term_in_createAggregateStatement2764);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:566:1: dropAggregateStatement returns [DropAggregateStatement expr] : K_DROP K_AGGREGATE ( K_IF K_EXISTS )? fn= functionName ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )? ;
	public final DropAggregateStatement dropAggregateStatement() throws RecognitionException {
		DropAggregateStatement expr = null;


		FunctionName fn =null;
		CQL3Type.Raw v =null;


		        boolean ifExists = false;
		        List<CQL3Type.Raw> argsTypes = new ArrayList<>();
		        boolean argsPresent = false;
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:572:5: ( K_DROP K_AGGREGATE ( K_IF K_EXISTS )? fn= functionName ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )? )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:572:7: K_DROP K_AGGREGATE ( K_IF K_EXISTS )? fn= functionName ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )?
			{
			match(input,K_DROP,FOLLOW_K_DROP_in_dropAggregateStatement2811); 
			match(input,K_AGGREGATE,FOLLOW_K_AGGREGATE_in_dropAggregateStatement2813); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:573:7: ( K_IF K_EXISTS )?
			int alt52=2;
			int LA52_0 = input.LA(1);
			if ( (LA52_0==K_IF) ) {
				alt52=1;
			}
			switch (alt52) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:573:8: K_IF K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_dropAggregateStatement2822); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropAggregateStatement2824); 
					 ifExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_functionName_in_dropAggregateStatement2839);
			fn=functionName();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:575:7: ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )?
			int alt55=2;
			int LA55_0 = input.LA(1);
			if ( (LA55_0==168) ) {
				alt55=1;
			}
			switch (alt55) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:576:9: '(' (v= comparatorType ( ',' v= comparatorType )* )? ')'
					{
					match(input,168,FOLLOW_168_in_dropAggregateStatement2857); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:577:11: (v= comparatorType ( ',' v= comparatorType )* )?
					int alt54=2;
					int LA54_0 = input.LA(1);
					if ( (LA54_0==IDENT||(LA54_0 >= K_AGGREGATE && LA54_0 <= K_ALL)||LA54_0==K_AS||LA54_0==K_ASCII||(LA54_0 >= K_BIGINT && LA54_0 <= K_BOOLEAN)||(LA54_0 >= K_CALLED && LA54_0 <= K_CLUSTERING)||(LA54_0 >= K_COMPACT && LA54_0 <= K_COUNTER)||(LA54_0 >= K_CUSTOM && LA54_0 <= K_DECIMAL)||(LA54_0 >= K_DISTINCT && LA54_0 <= K_DOUBLE)||(LA54_0 >= K_EXISTS && LA54_0 <= K_FLOAT)||LA54_0==K_FROZEN||(LA54_0 >= K_FUNCTION && LA54_0 <= K_FUNCTIONS)||LA54_0==K_INET||(LA54_0 >= K_INITCOND && LA54_0 <= K_INPUT)||LA54_0==K_INT||(LA54_0 >= K_JSON && LA54_0 <= K_KEYS)||(LA54_0 >= K_KEYSPACES && LA54_0 <= K_LANGUAGE)||(LA54_0 >= K_LIST && LA54_0 <= K_MAP)||LA54_0==K_NOLOGIN||LA54_0==K_NOSUPERUSER||LA54_0==K_OPTIONS||(LA54_0 >= K_PASSWORD && LA54_0 <= K_PERMISSIONS)||LA54_0==K_RETURNS||(LA54_0 >= K_ROLE && LA54_0 <= K_ROLES)||(LA54_0 >= K_SET && LA54_0 <= K_TINYINT)||LA54_0==K_TRIGGER||(LA54_0 >= K_TTL && LA54_0 <= K_TYPE)||(LA54_0 >= K_USER && LA54_0 <= K_USERS)||(LA54_0 >= K_UUID && LA54_0 <= K_VARINT)||LA54_0==K_WRITETIME||LA54_0==QUOTED_NAME||LA54_0==STRING_LITERAL) ) {
						alt54=1;
					}
					switch (alt54) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:578:13: v= comparatorType ( ',' v= comparatorType )*
							{
							pushFollow(FOLLOW_comparatorType_in_dropAggregateStatement2885);
							v=comparatorType();
							state._fsp--;

							 argsTypes.add(v); 
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:579:13: ( ',' v= comparatorType )*
							loop53:
							while (true) {
								int alt53=2;
								int LA53_0 = input.LA(1);
								if ( (LA53_0==171) ) {
									alt53=1;
								}

								switch (alt53) {
								case 1 :
									// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:579:15: ',' v= comparatorType
									{
									match(input,171,FOLLOW_171_in_dropAggregateStatement2903); 
									pushFollow(FOLLOW_comparatorType_in_dropAggregateStatement2907);
									v=comparatorType();
									state._fsp--;

									 argsTypes.add(v); 
									}
									break;

								default :
									break loop53;
								}
							}

							}
							break;

					}

					match(input,169,FOLLOW_169_in_dropAggregateStatement2935); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:587:1: createFunctionStatement returns [CreateFunctionStatement expr] : K_CREATE ( K_OR K_REPLACE )? K_FUNCTION ( K_IF K_NOT K_EXISTS )? fn= functionName '(' (k= ident v= comparatorType ( ',' k= ident v= comparatorType )* )? ')' ( ( K_RETURNS K_NULL ) | ( K_CALLED ) ) K_ON K_NULL K_INPUT K_RETURNS rt= comparatorType K_LANGUAGE language= IDENT K_AS body= STRING_LITERAL ;
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
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:596:5: ( K_CREATE ( K_OR K_REPLACE )? K_FUNCTION ( K_IF K_NOT K_EXISTS )? fn= functionName '(' (k= ident v= comparatorType ( ',' k= ident v= comparatorType )* )? ')' ( ( K_RETURNS K_NULL ) | ( K_CALLED ) ) K_ON K_NULL K_INPUT K_RETURNS rt= comparatorType K_LANGUAGE language= IDENT K_AS body= STRING_LITERAL )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:596:7: K_CREATE ( K_OR K_REPLACE )? K_FUNCTION ( K_IF K_NOT K_EXISTS )? fn= functionName '(' (k= ident v= comparatorType ( ',' k= ident v= comparatorType )* )? ')' ( ( K_RETURNS K_NULL ) | ( K_CALLED ) ) K_ON K_NULL K_INPUT K_RETURNS rt= comparatorType K_LANGUAGE language= IDENT K_AS body= STRING_LITERAL
			{
			match(input,K_CREATE,FOLLOW_K_CREATE_in_createFunctionStatement2992); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:596:16: ( K_OR K_REPLACE )?
			int alt56=2;
			int LA56_0 = input.LA(1);
			if ( (LA56_0==K_OR) ) {
				alt56=1;
			}
			switch (alt56) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:596:17: K_OR K_REPLACE
					{
					match(input,K_OR,FOLLOW_K_OR_in_createFunctionStatement2995); 
					match(input,K_REPLACE,FOLLOW_K_REPLACE_in_createFunctionStatement2997); 
					 orReplace = true; 
					}
					break;

			}

			match(input,K_FUNCTION,FOLLOW_K_FUNCTION_in_createFunctionStatement3009); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:598:7: ( K_IF K_NOT K_EXISTS )?
			int alt57=2;
			int LA57_0 = input.LA(1);
			if ( (LA57_0==K_IF) ) {
				alt57=1;
			}
			switch (alt57) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:598:8: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_createFunctionStatement3018); 
					match(input,K_NOT,FOLLOW_K_NOT_in_createFunctionStatement3020); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createFunctionStatement3022); 
					 ifNotExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_functionName_in_createFunctionStatement3036);
			fn=functionName();
			state._fsp--;

			match(input,168,FOLLOW_168_in_createFunctionStatement3044); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:601:9: (k= ident v= comparatorType ( ',' k= ident v= comparatorType )* )?
			int alt59=2;
			int LA59_0 = input.LA(1);
			if ( (LA59_0==IDENT||(LA59_0 >= K_AGGREGATE && LA59_0 <= K_ALL)||LA59_0==K_AS||LA59_0==K_ASCII||(LA59_0 >= K_BIGINT && LA59_0 <= K_BOOLEAN)||(LA59_0 >= K_CALLED && LA59_0 <= K_CLUSTERING)||(LA59_0 >= K_COMPACT && LA59_0 <= K_COUNTER)||(LA59_0 >= K_CUSTOM && LA59_0 <= K_DECIMAL)||(LA59_0 >= K_DISTINCT && LA59_0 <= K_DOUBLE)||(LA59_0 >= K_EXISTS && LA59_0 <= K_FLOAT)||LA59_0==K_FROZEN||(LA59_0 >= K_FUNCTION && LA59_0 <= K_FUNCTIONS)||LA59_0==K_INET||(LA59_0 >= K_INITCOND && LA59_0 <= K_INPUT)||LA59_0==K_INT||(LA59_0 >= K_JSON && LA59_0 <= K_KEYS)||(LA59_0 >= K_KEYSPACES && LA59_0 <= K_LANGUAGE)||(LA59_0 >= K_LIST && LA59_0 <= K_MAP)||LA59_0==K_NOLOGIN||LA59_0==K_NOSUPERUSER||LA59_0==K_OPTIONS||(LA59_0 >= K_PASSWORD && LA59_0 <= K_PERMISSIONS)||LA59_0==K_RETURNS||(LA59_0 >= K_ROLE && LA59_0 <= K_ROLES)||(LA59_0 >= K_SFUNC && LA59_0 <= K_TINYINT)||LA59_0==K_TRIGGER||(LA59_0 >= K_TTL && LA59_0 <= K_TYPE)||(LA59_0 >= K_USER && LA59_0 <= K_USERS)||(LA59_0 >= K_UUID && LA59_0 <= K_VARINT)||LA59_0==K_WRITETIME||LA59_0==QUOTED_NAME) ) {
				alt59=1;
			}
			switch (alt59) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:602:11: k= ident v= comparatorType ( ',' k= ident v= comparatorType )*
					{
					pushFollow(FOLLOW_ident_in_createFunctionStatement3068);
					k=ident();
					state._fsp--;

					pushFollow(FOLLOW_comparatorType_in_createFunctionStatement3072);
					v=comparatorType();
					state._fsp--;

					 argsNames.add(k); argsTypes.add(v); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:603:11: ( ',' k= ident v= comparatorType )*
					loop58:
					while (true) {
						int alt58=2;
						int LA58_0 = input.LA(1);
						if ( (LA58_0==171) ) {
							alt58=1;
						}

						switch (alt58) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:603:13: ',' k= ident v= comparatorType
							{
							match(input,171,FOLLOW_171_in_createFunctionStatement3088); 
							pushFollow(FOLLOW_ident_in_createFunctionStatement3092);
							k=ident();
							state._fsp--;

							pushFollow(FOLLOW_comparatorType_in_createFunctionStatement3096);
							v=comparatorType();
							state._fsp--;

							 argsNames.add(k); argsTypes.add(v); 
							}
							break;

						default :
							break loop58;
						}
					}

					}
					break;

			}

			match(input,169,FOLLOW_169_in_createFunctionStatement3120); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:606:7: ( ( K_RETURNS K_NULL ) | ( K_CALLED ) )
			int alt60=2;
			int LA60_0 = input.LA(1);
			if ( (LA60_0==K_RETURNS) ) {
				alt60=1;
			}
			else if ( (LA60_0==K_CALLED) ) {
				alt60=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 60, 0, input);
				throw nvae;
			}

			switch (alt60) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:606:9: ( K_RETURNS K_NULL )
					{
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:606:9: ( K_RETURNS K_NULL )
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:606:10: K_RETURNS K_NULL
					{
					match(input,K_RETURNS,FOLLOW_K_RETURNS_in_createFunctionStatement3131); 
					match(input,K_NULL,FOLLOW_K_NULL_in_createFunctionStatement3133); 
					}

					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:606:30: ( K_CALLED )
					{
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:606:30: ( K_CALLED )
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:606:31: K_CALLED
					{
					match(input,K_CALLED,FOLLOW_K_CALLED_in_createFunctionStatement3139); 
					 calledOnNullInput=true; 
					}

					}
					break;

			}

			match(input,K_ON,FOLLOW_K_ON_in_createFunctionStatement3145); 
			match(input,K_NULL,FOLLOW_K_NULL_in_createFunctionStatement3147); 
			match(input,K_INPUT,FOLLOW_K_INPUT_in_createFunctionStatement3149); 
			match(input,K_RETURNS,FOLLOW_K_RETURNS_in_createFunctionStatement3157); 
			pushFollow(FOLLOW_comparatorType_in_createFunctionStatement3163);
			rt=comparatorType();
			state._fsp--;

			match(input,K_LANGUAGE,FOLLOW_K_LANGUAGE_in_createFunctionStatement3171); 
			language=(Token)match(input,IDENT,FOLLOW_IDENT_in_createFunctionStatement3177); 
			match(input,K_AS,FOLLOW_K_AS_in_createFunctionStatement3185); 
			body=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_createFunctionStatement3191); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:614:1: dropFunctionStatement returns [DropFunctionStatement expr] : K_DROP K_FUNCTION ( K_IF K_EXISTS )? fn= functionName ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )? ;
	public final DropFunctionStatement dropFunctionStatement() throws RecognitionException {
		DropFunctionStatement expr = null;


		FunctionName fn =null;
		CQL3Type.Raw v =null;


		        boolean ifExists = false;
		        List<CQL3Type.Raw> argsTypes = new ArrayList<>();
		        boolean argsPresent = false;
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:620:5: ( K_DROP K_FUNCTION ( K_IF K_EXISTS )? fn= functionName ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )? )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:620:7: K_DROP K_FUNCTION ( K_IF K_EXISTS )? fn= functionName ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )?
			{
			match(input,K_DROP,FOLLOW_K_DROP_in_dropFunctionStatement3229); 
			match(input,K_FUNCTION,FOLLOW_K_FUNCTION_in_dropFunctionStatement3231); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:621:7: ( K_IF K_EXISTS )?
			int alt61=2;
			int LA61_0 = input.LA(1);
			if ( (LA61_0==K_IF) ) {
				alt61=1;
			}
			switch (alt61) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:621:8: K_IF K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_dropFunctionStatement3240); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropFunctionStatement3242); 
					 ifExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_functionName_in_dropFunctionStatement3257);
			fn=functionName();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:623:7: ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )?
			int alt64=2;
			int LA64_0 = input.LA(1);
			if ( (LA64_0==168) ) {
				alt64=1;
			}
			switch (alt64) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:624:9: '(' (v= comparatorType ( ',' v= comparatorType )* )? ')'
					{
					match(input,168,FOLLOW_168_in_dropFunctionStatement3275); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:625:11: (v= comparatorType ( ',' v= comparatorType )* )?
					int alt63=2;
					int LA63_0 = input.LA(1);
					if ( (LA63_0==IDENT||(LA63_0 >= K_AGGREGATE && LA63_0 <= K_ALL)||LA63_0==K_AS||LA63_0==K_ASCII||(LA63_0 >= K_BIGINT && LA63_0 <= K_BOOLEAN)||(LA63_0 >= K_CALLED && LA63_0 <= K_CLUSTERING)||(LA63_0 >= K_COMPACT && LA63_0 <= K_COUNTER)||(LA63_0 >= K_CUSTOM && LA63_0 <= K_DECIMAL)||(LA63_0 >= K_DISTINCT && LA63_0 <= K_DOUBLE)||(LA63_0 >= K_EXISTS && LA63_0 <= K_FLOAT)||LA63_0==K_FROZEN||(LA63_0 >= K_FUNCTION && LA63_0 <= K_FUNCTIONS)||LA63_0==K_INET||(LA63_0 >= K_INITCOND && LA63_0 <= K_INPUT)||LA63_0==K_INT||(LA63_0 >= K_JSON && LA63_0 <= K_KEYS)||(LA63_0 >= K_KEYSPACES && LA63_0 <= K_LANGUAGE)||(LA63_0 >= K_LIST && LA63_0 <= K_MAP)||LA63_0==K_NOLOGIN||LA63_0==K_NOSUPERUSER||LA63_0==K_OPTIONS||(LA63_0 >= K_PASSWORD && LA63_0 <= K_PERMISSIONS)||LA63_0==K_RETURNS||(LA63_0 >= K_ROLE && LA63_0 <= K_ROLES)||(LA63_0 >= K_SET && LA63_0 <= K_TINYINT)||LA63_0==K_TRIGGER||(LA63_0 >= K_TTL && LA63_0 <= K_TYPE)||(LA63_0 >= K_USER && LA63_0 <= K_USERS)||(LA63_0 >= K_UUID && LA63_0 <= K_VARINT)||LA63_0==K_WRITETIME||LA63_0==QUOTED_NAME||LA63_0==STRING_LITERAL) ) {
						alt63=1;
					}
					switch (alt63) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:626:13: v= comparatorType ( ',' v= comparatorType )*
							{
							pushFollow(FOLLOW_comparatorType_in_dropFunctionStatement3303);
							v=comparatorType();
							state._fsp--;

							 argsTypes.add(v); 
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:627:13: ( ',' v= comparatorType )*
							loop62:
							while (true) {
								int alt62=2;
								int LA62_0 = input.LA(1);
								if ( (LA62_0==171) ) {
									alt62=1;
								}

								switch (alt62) {
								case 1 :
									// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:627:15: ',' v= comparatorType
									{
									match(input,171,FOLLOW_171_in_dropFunctionStatement3321); 
									pushFollow(FOLLOW_comparatorType_in_dropFunctionStatement3325);
									v=comparatorType();
									state._fsp--;

									 argsTypes.add(v); 
									}
									break;

								default :
									break loop62;
								}
							}

							}
							break;

					}

					match(input,169,FOLLOW_169_in_dropFunctionStatement3353); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:638:1: createKeyspaceStatement returns [CreateKeyspaceStatement expr] : K_CREATE K_KEYSPACE ( K_IF K_NOT K_EXISTS )? ks= keyspaceName K_WITH properties[attrs] ;
	public final CreateKeyspaceStatement createKeyspaceStatement() throws RecognitionException {
		CreateKeyspaceStatement expr = null;


		String ks =null;


		        KSPropDefs attrs = new KSPropDefs();
		        boolean ifNotExists = false;
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:643:5: ( K_CREATE K_KEYSPACE ( K_IF K_NOT K_EXISTS )? ks= keyspaceName K_WITH properties[attrs] )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:643:7: K_CREATE K_KEYSPACE ( K_IF K_NOT K_EXISTS )? ks= keyspaceName K_WITH properties[attrs]
			{
			match(input,K_CREATE,FOLLOW_K_CREATE_in_createKeyspaceStatement3412); 
			match(input,K_KEYSPACE,FOLLOW_K_KEYSPACE_in_createKeyspaceStatement3414); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:643:27: ( K_IF K_NOT K_EXISTS )?
			int alt65=2;
			int LA65_0 = input.LA(1);
			if ( (LA65_0==K_IF) ) {
				alt65=1;
			}
			switch (alt65) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:643:28: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_createKeyspaceStatement3417); 
					match(input,K_NOT,FOLLOW_K_NOT_in_createKeyspaceStatement3419); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createKeyspaceStatement3421); 
					 ifNotExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_keyspaceName_in_createKeyspaceStatement3430);
			ks=keyspaceName();
			state._fsp--;

			match(input,K_WITH,FOLLOW_K_WITH_in_createKeyspaceStatement3438); 
			pushFollow(FOLLOW_properties_in_createKeyspaceStatement3440);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:654:1: createTableStatement returns [CreateTableStatement.RawStatement expr] : K_CREATE K_COLUMNFAMILY ( K_IF K_NOT K_EXISTS )? cf= columnFamilyName cfamDefinition[expr] ;
	public final CreateTableStatement.RawStatement createTableStatement() throws RecognitionException {
		CreateTableStatement.RawStatement expr = null;


		CFName cf =null;

		 boolean ifNotExists = false; 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:656:5: ( K_CREATE K_COLUMNFAMILY ( K_IF K_NOT K_EXISTS )? cf= columnFamilyName cfamDefinition[expr] )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:656:7: K_CREATE K_COLUMNFAMILY ( K_IF K_NOT K_EXISTS )? cf= columnFamilyName cfamDefinition[expr]
			{
			match(input,K_CREATE,FOLLOW_K_CREATE_in_createTableStatement3475); 
			match(input,K_COLUMNFAMILY,FOLLOW_K_COLUMNFAMILY_in_createTableStatement3477); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:656:31: ( K_IF K_NOT K_EXISTS )?
			int alt66=2;
			int LA66_0 = input.LA(1);
			if ( (LA66_0==K_IF) ) {
				alt66=1;
			}
			switch (alt66) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:656:32: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_createTableStatement3480); 
					match(input,K_NOT,FOLLOW_K_NOT_in_createTableStatement3482); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createTableStatement3484); 
					 ifNotExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_columnFamilyName_in_createTableStatement3499);
			cf=columnFamilyName();
			state._fsp--;

			 expr = new CreateTableStatement.RawStatement(cf, ifNotExists); 
			pushFollow(FOLLOW_cfamDefinition_in_createTableStatement3509);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:661:1: cfamDefinition[CreateTableStatement.RawStatement expr] : '(' cfamColumns[expr] ( ',' ( cfamColumns[expr] )? )* ')' ( K_WITH cfamProperty[expr] ( K_AND cfamProperty[expr] )* )? ;
	public final void cfamDefinition(CreateTableStatement.RawStatement expr) throws RecognitionException {
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:662:5: ( '(' cfamColumns[expr] ( ',' ( cfamColumns[expr] )? )* ')' ( K_WITH cfamProperty[expr] ( K_AND cfamProperty[expr] )* )? )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:662:7: '(' cfamColumns[expr] ( ',' ( cfamColumns[expr] )? )* ')' ( K_WITH cfamProperty[expr] ( K_AND cfamProperty[expr] )* )?
			{
			match(input,168,FOLLOW_168_in_cfamDefinition3528); 
			pushFollow(FOLLOW_cfamColumns_in_cfamDefinition3530);
			cfamColumns(expr);
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:662:29: ( ',' ( cfamColumns[expr] )? )*
			loop68:
			while (true) {
				int alt68=2;
				int LA68_0 = input.LA(1);
				if ( (LA68_0==171) ) {
					alt68=1;
				}

				switch (alt68) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:662:31: ',' ( cfamColumns[expr] )?
					{
					match(input,171,FOLLOW_171_in_cfamDefinition3535); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:662:35: ( cfamColumns[expr] )?
					int alt67=2;
					int LA67_0 = input.LA(1);
					if ( (LA67_0==IDENT||(LA67_0 >= K_AGGREGATE && LA67_0 <= K_ALL)||LA67_0==K_AS||LA67_0==K_ASCII||(LA67_0 >= K_BIGINT && LA67_0 <= K_BOOLEAN)||(LA67_0 >= K_CALLED && LA67_0 <= K_CLUSTERING)||(LA67_0 >= K_COMPACT && LA67_0 <= K_COUNTER)||(LA67_0 >= K_CUSTOM && LA67_0 <= K_DECIMAL)||(LA67_0 >= K_DISTINCT && LA67_0 <= K_DOUBLE)||(LA67_0 >= K_EXISTS && LA67_0 <= K_FLOAT)||LA67_0==K_FROZEN||(LA67_0 >= K_FUNCTION && LA67_0 <= K_FUNCTIONS)||LA67_0==K_INET||(LA67_0 >= K_INITCOND && LA67_0 <= K_INPUT)||LA67_0==K_INT||(LA67_0 >= K_JSON && LA67_0 <= K_KEYS)||(LA67_0 >= K_KEYSPACES && LA67_0 <= K_LANGUAGE)||(LA67_0 >= K_LIST && LA67_0 <= K_MAP)||LA67_0==K_NOLOGIN||LA67_0==K_NOSUPERUSER||LA67_0==K_OPTIONS||(LA67_0 >= K_PASSWORD && LA67_0 <= K_PRIMARY)||LA67_0==K_RETURNS||(LA67_0 >= K_ROLE && LA67_0 <= K_ROLES)||(LA67_0 >= K_SFUNC && LA67_0 <= K_TINYINT)||LA67_0==K_TRIGGER||(LA67_0 >= K_TTL && LA67_0 <= K_TYPE)||(LA67_0 >= K_USER && LA67_0 <= K_USERS)||(LA67_0 >= K_UUID && LA67_0 <= K_VARINT)||LA67_0==K_WRITETIME||LA67_0==QUOTED_NAME) ) {
						alt67=1;
					}
					switch (alt67) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:662:35: cfamColumns[expr]
							{
							pushFollow(FOLLOW_cfamColumns_in_cfamDefinition3537);
							cfamColumns(expr);
							state._fsp--;

							}
							break;

					}

					}
					break;

				default :
					break loop68;
				}
			}

			match(input,169,FOLLOW_169_in_cfamDefinition3544); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:663:7: ( K_WITH cfamProperty[expr] ( K_AND cfamProperty[expr] )* )?
			int alt70=2;
			int LA70_0 = input.LA(1);
			if ( (LA70_0==K_WITH) ) {
				alt70=1;
			}
			switch (alt70) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:663:9: K_WITH cfamProperty[expr] ( K_AND cfamProperty[expr] )*
					{
					match(input,K_WITH,FOLLOW_K_WITH_in_cfamDefinition3554); 
					pushFollow(FOLLOW_cfamProperty_in_cfamDefinition3556);
					cfamProperty(expr);
					state._fsp--;

					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:663:35: ( K_AND cfamProperty[expr] )*
					loop69:
					while (true) {
						int alt69=2;
						int LA69_0 = input.LA(1);
						if ( (LA69_0==K_AND) ) {
							alt69=1;
						}

						switch (alt69) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:663:37: K_AND cfamProperty[expr]
							{
							match(input,K_AND,FOLLOW_K_AND_in_cfamDefinition3561); 
							pushFollow(FOLLOW_cfamProperty_in_cfamDefinition3563);
							cfamProperty(expr);
							state._fsp--;

							}
							break;

						default :
							break loop69;
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:666:1: cfamColumns[CreateTableStatement.RawStatement expr] : (k= ident v= comparatorType ( K_STATIC )? ( K_PRIMARY K_KEY )? | K_PRIMARY K_KEY '(' pkDef[expr] ( ',' c= ident )* ')' );
	public final void cfamColumns(CreateTableStatement.RawStatement expr) throws RecognitionException {
		ColumnIdentifier k =null;
		CQL3Type.Raw v =null;
		ColumnIdentifier c =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:667:5: (k= ident v= comparatorType ( K_STATIC )? ( K_PRIMARY K_KEY )? | K_PRIMARY K_KEY '(' pkDef[expr] ( ',' c= ident )* ')' )
			int alt74=2;
			int LA74_0 = input.LA(1);
			if ( (LA74_0==IDENT||(LA74_0 >= K_AGGREGATE && LA74_0 <= K_ALL)||LA74_0==K_AS||LA74_0==K_ASCII||(LA74_0 >= K_BIGINT && LA74_0 <= K_BOOLEAN)||(LA74_0 >= K_CALLED && LA74_0 <= K_CLUSTERING)||(LA74_0 >= K_COMPACT && LA74_0 <= K_COUNTER)||(LA74_0 >= K_CUSTOM && LA74_0 <= K_DECIMAL)||(LA74_0 >= K_DISTINCT && LA74_0 <= K_DOUBLE)||(LA74_0 >= K_EXISTS && LA74_0 <= K_FLOAT)||LA74_0==K_FROZEN||(LA74_0 >= K_FUNCTION && LA74_0 <= K_FUNCTIONS)||LA74_0==K_INET||(LA74_0 >= K_INITCOND && LA74_0 <= K_INPUT)||LA74_0==K_INT||(LA74_0 >= K_JSON && LA74_0 <= K_KEYS)||(LA74_0 >= K_KEYSPACES && LA74_0 <= K_LANGUAGE)||(LA74_0 >= K_LIST && LA74_0 <= K_MAP)||LA74_0==K_NOLOGIN||LA74_0==K_NOSUPERUSER||LA74_0==K_OPTIONS||(LA74_0 >= K_PASSWORD && LA74_0 <= K_PERMISSIONS)||LA74_0==K_RETURNS||(LA74_0 >= K_ROLE && LA74_0 <= K_ROLES)||(LA74_0 >= K_SFUNC && LA74_0 <= K_TINYINT)||LA74_0==K_TRIGGER||(LA74_0 >= K_TTL && LA74_0 <= K_TYPE)||(LA74_0 >= K_USER && LA74_0 <= K_USERS)||(LA74_0 >= K_UUID && LA74_0 <= K_VARINT)||LA74_0==K_WRITETIME||LA74_0==QUOTED_NAME) ) {
				alt74=1;
			}
			else if ( (LA74_0==K_PRIMARY) ) {
				alt74=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 74, 0, input);
				throw nvae;
			}

			switch (alt74) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:667:7: k= ident v= comparatorType ( K_STATIC )? ( K_PRIMARY K_KEY )?
					{
					pushFollow(FOLLOW_ident_in_cfamColumns3589);
					k=ident();
					state._fsp--;

					pushFollow(FOLLOW_comparatorType_in_cfamColumns3593);
					v=comparatorType();
					state._fsp--;

					 boolean isStatic=false; 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:667:60: ( K_STATIC )?
					int alt71=2;
					int LA71_0 = input.LA(1);
					if ( (LA71_0==K_STATIC) ) {
						alt71=1;
					}
					switch (alt71) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:667:61: K_STATIC
							{
							match(input,K_STATIC,FOLLOW_K_STATIC_in_cfamColumns3598); 
							isStatic = true;
							}
							break;

					}

					 expr.addDefinition(k, v, isStatic); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:668:9: ( K_PRIMARY K_KEY )?
					int alt72=2;
					int LA72_0 = input.LA(1);
					if ( (LA72_0==K_PRIMARY) ) {
						alt72=1;
					}
					switch (alt72) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:668:10: K_PRIMARY K_KEY
							{
							match(input,K_PRIMARY,FOLLOW_K_PRIMARY_in_cfamColumns3615); 
							match(input,K_KEY,FOLLOW_K_KEY_in_cfamColumns3617); 
							 expr.addKeyAliases(Collections.singletonList(k)); 
							}
							break;

					}

					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:669:7: K_PRIMARY K_KEY '(' pkDef[expr] ( ',' c= ident )* ')'
					{
					match(input,K_PRIMARY,FOLLOW_K_PRIMARY_in_cfamColumns3629); 
					match(input,K_KEY,FOLLOW_K_KEY_in_cfamColumns3631); 
					match(input,168,FOLLOW_168_in_cfamColumns3633); 
					pushFollow(FOLLOW_pkDef_in_cfamColumns3635);
					pkDef(expr);
					state._fsp--;

					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:669:39: ( ',' c= ident )*
					loop73:
					while (true) {
						int alt73=2;
						int LA73_0 = input.LA(1);
						if ( (LA73_0==171) ) {
							alt73=1;
						}

						switch (alt73) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:669:40: ',' c= ident
							{
							match(input,171,FOLLOW_171_in_cfamColumns3639); 
							pushFollow(FOLLOW_ident_in_cfamColumns3643);
							c=ident();
							state._fsp--;

							 expr.addColumnAlias(c); 
							}
							break;

						default :
							break loop73;
						}
					}

					match(input,169,FOLLOW_169_in_cfamColumns3650); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:672:1: pkDef[CreateTableStatement.RawStatement expr] : (k= ident | '(' k1= ident ( ',' kn= ident )* ')' );
	public final void pkDef(CreateTableStatement.RawStatement expr) throws RecognitionException {
		ColumnIdentifier k =null;
		ColumnIdentifier k1 =null;
		ColumnIdentifier kn =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:673:5: (k= ident | '(' k1= ident ( ',' kn= ident )* ')' )
			int alt76=2;
			int LA76_0 = input.LA(1);
			if ( (LA76_0==IDENT||(LA76_0 >= K_AGGREGATE && LA76_0 <= K_ALL)||LA76_0==K_AS||LA76_0==K_ASCII||(LA76_0 >= K_BIGINT && LA76_0 <= K_BOOLEAN)||(LA76_0 >= K_CALLED && LA76_0 <= K_CLUSTERING)||(LA76_0 >= K_COMPACT && LA76_0 <= K_COUNTER)||(LA76_0 >= K_CUSTOM && LA76_0 <= K_DECIMAL)||(LA76_0 >= K_DISTINCT && LA76_0 <= K_DOUBLE)||(LA76_0 >= K_EXISTS && LA76_0 <= K_FLOAT)||LA76_0==K_FROZEN||(LA76_0 >= K_FUNCTION && LA76_0 <= K_FUNCTIONS)||LA76_0==K_INET||(LA76_0 >= K_INITCOND && LA76_0 <= K_INPUT)||LA76_0==K_INT||(LA76_0 >= K_JSON && LA76_0 <= K_KEYS)||(LA76_0 >= K_KEYSPACES && LA76_0 <= K_LANGUAGE)||(LA76_0 >= K_LIST && LA76_0 <= K_MAP)||LA76_0==K_NOLOGIN||LA76_0==K_NOSUPERUSER||LA76_0==K_OPTIONS||(LA76_0 >= K_PASSWORD && LA76_0 <= K_PERMISSIONS)||LA76_0==K_RETURNS||(LA76_0 >= K_ROLE && LA76_0 <= K_ROLES)||(LA76_0 >= K_SFUNC && LA76_0 <= K_TINYINT)||LA76_0==K_TRIGGER||(LA76_0 >= K_TTL && LA76_0 <= K_TYPE)||(LA76_0 >= K_USER && LA76_0 <= K_USERS)||(LA76_0 >= K_UUID && LA76_0 <= K_VARINT)||LA76_0==K_WRITETIME||LA76_0==QUOTED_NAME) ) {
				alt76=1;
			}
			else if ( (LA76_0==168) ) {
				alt76=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 76, 0, input);
				throw nvae;
			}

			switch (alt76) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:673:7: k= ident
					{
					pushFollow(FOLLOW_ident_in_pkDef3670);
					k=ident();
					state._fsp--;

					 expr.addKeyAliases(Collections.singletonList(k)); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:674:7: '(' k1= ident ( ',' kn= ident )* ')'
					{
					match(input,168,FOLLOW_168_in_pkDef3680); 
					 List<ColumnIdentifier> l = new ArrayList<ColumnIdentifier>(); 
					pushFollow(FOLLOW_ident_in_pkDef3686);
					k1=ident();
					state._fsp--;

					 l.add(k1); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:674:101: ( ',' kn= ident )*
					loop75:
					while (true) {
						int alt75=2;
						int LA75_0 = input.LA(1);
						if ( (LA75_0==171) ) {
							alt75=1;
						}

						switch (alt75) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:674:103: ',' kn= ident
							{
							match(input,171,FOLLOW_171_in_pkDef3692); 
							pushFollow(FOLLOW_ident_in_pkDef3696);
							kn=ident();
							state._fsp--;

							 l.add(kn); 
							}
							break;

						default :
							break loop75;
						}
					}

					match(input,169,FOLLOW_169_in_pkDef3703); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:677:1: cfamProperty[CreateTableStatement.RawStatement expr] : ( property[expr.properties] | K_COMPACT K_STORAGE | K_CLUSTERING K_ORDER K_BY '(' cfamOrdering[expr] ( ',' cfamOrdering[expr] )* ')' );
	public final void cfamProperty(CreateTableStatement.RawStatement expr) throws RecognitionException {
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:678:5: ( property[expr.properties] | K_COMPACT K_STORAGE | K_CLUSTERING K_ORDER K_BY '(' cfamOrdering[expr] ( ',' cfamOrdering[expr] )* ')' )
			int alt78=3;
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
				alt78=1;
				}
				break;
			case K_COMPACT:
				{
				int LA78_2 = input.LA(2);
				if ( (LA78_2==K_STORAGE) ) {
					alt78=2;
				}
				else if ( (LA78_2==178) ) {
					alt78=1;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 78, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case K_CLUSTERING:
				{
				int LA78_3 = input.LA(2);
				if ( (LA78_3==K_ORDER) ) {
					alt78=3;
				}
				else if ( (LA78_3==178) ) {
					alt78=1;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 78, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 78, 0, input);
				throw nvae;
			}
			switch (alt78) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:678:7: property[expr.properties]
					{
					pushFollow(FOLLOW_property_in_cfamProperty3723);
					property(expr.properties);
					state._fsp--;

					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:679:7: K_COMPACT K_STORAGE
					{
					match(input,K_COMPACT,FOLLOW_K_COMPACT_in_cfamProperty3732); 
					match(input,K_STORAGE,FOLLOW_K_STORAGE_in_cfamProperty3734); 
					 expr.setCompactStorage(); 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:680:7: K_CLUSTERING K_ORDER K_BY '(' cfamOrdering[expr] ( ',' cfamOrdering[expr] )* ')'
					{
					match(input,K_CLUSTERING,FOLLOW_K_CLUSTERING_in_cfamProperty3744); 
					match(input,K_ORDER,FOLLOW_K_ORDER_in_cfamProperty3746); 
					match(input,K_BY,FOLLOW_K_BY_in_cfamProperty3748); 
					match(input,168,FOLLOW_168_in_cfamProperty3750); 
					pushFollow(FOLLOW_cfamOrdering_in_cfamProperty3752);
					cfamOrdering(expr);
					state._fsp--;

					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:680:56: ( ',' cfamOrdering[expr] )*
					loop77:
					while (true) {
						int alt77=2;
						int LA77_0 = input.LA(1);
						if ( (LA77_0==171) ) {
							alt77=1;
						}

						switch (alt77) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:680:57: ',' cfamOrdering[expr]
							{
							match(input,171,FOLLOW_171_in_cfamProperty3756); 
							pushFollow(FOLLOW_cfamOrdering_in_cfamProperty3758);
							cfamOrdering(expr);
							state._fsp--;

							}
							break;

						default :
							break loop77;
						}
					}

					match(input,169,FOLLOW_169_in_cfamProperty3763); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:683:1: cfamOrdering[CreateTableStatement.RawStatement expr] : k= ident ( K_ASC | K_DESC ) ;
	public final void cfamOrdering(CreateTableStatement.RawStatement expr) throws RecognitionException {
		ColumnIdentifier k =null;

		 boolean reversed=false; 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:685:5: (k= ident ( K_ASC | K_DESC ) )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:685:7: k= ident ( K_ASC | K_DESC )
			{
			pushFollow(FOLLOW_ident_in_cfamOrdering3791);
			k=ident();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:685:15: ( K_ASC | K_DESC )
			int alt79=2;
			int LA79_0 = input.LA(1);
			if ( (LA79_0==K_ASC) ) {
				alt79=1;
			}
			else if ( (LA79_0==K_DESC) ) {
				alt79=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 79, 0, input);
				throw nvae;
			}

			switch (alt79) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:685:16: K_ASC
					{
					match(input,K_ASC,FOLLOW_K_ASC_in_cfamOrdering3794); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:685:24: K_DESC
					{
					match(input,K_DESC,FOLLOW_K_DESC_in_cfamOrdering3798); 
					 reversed=true;
					}
					break;

			}

			 expr.setOrdering(k, reversed); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:696:1: createTypeStatement returns [CreateTypeStatement expr] : K_CREATE K_TYPE ( K_IF K_NOT K_EXISTS )? tn= userTypeName '(' typeColumns[expr] ( ',' ( typeColumns[expr] )? )* ')' ;
	public final CreateTypeStatement createTypeStatement() throws RecognitionException {
		CreateTypeStatement expr = null;


		UTName tn =null;

		 boolean ifNotExists = false; 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:698:5: ( K_CREATE K_TYPE ( K_IF K_NOT K_EXISTS )? tn= userTypeName '(' typeColumns[expr] ( ',' ( typeColumns[expr] )? )* ')' )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:698:7: K_CREATE K_TYPE ( K_IF K_NOT K_EXISTS )? tn= userTypeName '(' typeColumns[expr] ( ',' ( typeColumns[expr] )? )* ')'
			{
			match(input,K_CREATE,FOLLOW_K_CREATE_in_createTypeStatement3837); 
			match(input,K_TYPE,FOLLOW_K_TYPE_in_createTypeStatement3839); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:698:23: ( K_IF K_NOT K_EXISTS )?
			int alt80=2;
			int LA80_0 = input.LA(1);
			if ( (LA80_0==K_IF) ) {
				alt80=1;
			}
			switch (alt80) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:698:24: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_createTypeStatement3842); 
					match(input,K_NOT,FOLLOW_K_NOT_in_createTypeStatement3844); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createTypeStatement3846); 
					 ifNotExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_userTypeName_in_createTypeStatement3864);
			tn=userTypeName();
			state._fsp--;

			 expr = new CreateTypeStatement(tn, ifNotExists); 
			match(input,168,FOLLOW_168_in_createTypeStatement3877); 
			pushFollow(FOLLOW_typeColumns_in_createTypeStatement3879);
			typeColumns(expr);
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:700:32: ( ',' ( typeColumns[expr] )? )*
			loop82:
			while (true) {
				int alt82=2;
				int LA82_0 = input.LA(1);
				if ( (LA82_0==171) ) {
					alt82=1;
				}

				switch (alt82) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:700:34: ',' ( typeColumns[expr] )?
					{
					match(input,171,FOLLOW_171_in_createTypeStatement3884); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:700:38: ( typeColumns[expr] )?
					int alt81=2;
					int LA81_0 = input.LA(1);
					if ( (LA81_0==IDENT||(LA81_0 >= K_AGGREGATE && LA81_0 <= K_ALL)||LA81_0==K_AS||LA81_0==K_ASCII||(LA81_0 >= K_BIGINT && LA81_0 <= K_BOOLEAN)||(LA81_0 >= K_CALLED && LA81_0 <= K_CLUSTERING)||(LA81_0 >= K_COMPACT && LA81_0 <= K_COUNTER)||(LA81_0 >= K_CUSTOM && LA81_0 <= K_DECIMAL)||(LA81_0 >= K_DISTINCT && LA81_0 <= K_DOUBLE)||(LA81_0 >= K_EXISTS && LA81_0 <= K_FLOAT)||LA81_0==K_FROZEN||(LA81_0 >= K_FUNCTION && LA81_0 <= K_FUNCTIONS)||LA81_0==K_INET||(LA81_0 >= K_INITCOND && LA81_0 <= K_INPUT)||LA81_0==K_INT||(LA81_0 >= K_JSON && LA81_0 <= K_KEYS)||(LA81_0 >= K_KEYSPACES && LA81_0 <= K_LANGUAGE)||(LA81_0 >= K_LIST && LA81_0 <= K_MAP)||LA81_0==K_NOLOGIN||LA81_0==K_NOSUPERUSER||LA81_0==K_OPTIONS||(LA81_0 >= K_PASSWORD && LA81_0 <= K_PERMISSIONS)||LA81_0==K_RETURNS||(LA81_0 >= K_ROLE && LA81_0 <= K_ROLES)||(LA81_0 >= K_SFUNC && LA81_0 <= K_TINYINT)||LA81_0==K_TRIGGER||(LA81_0 >= K_TTL && LA81_0 <= K_TYPE)||(LA81_0 >= K_USER && LA81_0 <= K_USERS)||(LA81_0 >= K_UUID && LA81_0 <= K_VARINT)||LA81_0==K_WRITETIME||LA81_0==QUOTED_NAME) ) {
						alt81=1;
					}
					switch (alt81) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:700:38: typeColumns[expr]
							{
							pushFollow(FOLLOW_typeColumns_in_createTypeStatement3886);
							typeColumns(expr);
							state._fsp--;

							}
							break;

					}

					}
					break;

				default :
					break loop82;
				}
			}

			match(input,169,FOLLOW_169_in_createTypeStatement3893); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:703:1: typeColumns[CreateTypeStatement expr] : k= ident v= comparatorType ;
	public final void typeColumns(CreateTypeStatement expr) throws RecognitionException {
		ColumnIdentifier k =null;
		CQL3Type.Raw v =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:704:5: (k= ident v= comparatorType )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:704:7: k= ident v= comparatorType
			{
			pushFollow(FOLLOW_ident_in_typeColumns3913);
			k=ident();
			state._fsp--;

			pushFollow(FOLLOW_comparatorType_in_typeColumns3917);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:712:1: createIndexStatement returns [CreateIndexStatement expr] : K_CREATE ( K_CUSTOM )? K_INDEX ( K_IF K_NOT K_EXISTS )? ( idxName[name] )? K_ON cf= columnFamilyName '(' id= indexIdent ')' ( K_USING cls= STRING_LITERAL )? ( K_WITH properties[props] )? ;
	public final CreateIndexStatement createIndexStatement() throws RecognitionException {
		CreateIndexStatement expr = null;


		Token cls=null;
		CFName cf =null;
		IndexTarget.Raw id =null;


		        IndexPropDefs props = new IndexPropDefs();
		        boolean ifNotExists = false;
		        IndexName name = new IndexName();
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:718:5: ( K_CREATE ( K_CUSTOM )? K_INDEX ( K_IF K_NOT K_EXISTS )? ( idxName[name] )? K_ON cf= columnFamilyName '(' id= indexIdent ')' ( K_USING cls= STRING_LITERAL )? ( K_WITH properties[props] )? )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:718:7: K_CREATE ( K_CUSTOM )? K_INDEX ( K_IF K_NOT K_EXISTS )? ( idxName[name] )? K_ON cf= columnFamilyName '(' id= indexIdent ')' ( K_USING cls= STRING_LITERAL )? ( K_WITH properties[props] )?
			{
			match(input,K_CREATE,FOLLOW_K_CREATE_in_createIndexStatement3952); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:718:16: ( K_CUSTOM )?
			int alt83=2;
			int LA83_0 = input.LA(1);
			if ( (LA83_0==K_CUSTOM) ) {
				alt83=1;
			}
			switch (alt83) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:718:17: K_CUSTOM
					{
					match(input,K_CUSTOM,FOLLOW_K_CUSTOM_in_createIndexStatement3955); 
					 props.isCustom = true; 
					}
					break;

			}

			match(input,K_INDEX,FOLLOW_K_INDEX_in_createIndexStatement3961); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:718:63: ( K_IF K_NOT K_EXISTS )?
			int alt84=2;
			int LA84_0 = input.LA(1);
			if ( (LA84_0==K_IF) ) {
				alt84=1;
			}
			switch (alt84) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:718:64: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_createIndexStatement3964); 
					match(input,K_NOT,FOLLOW_K_NOT_in_createIndexStatement3966); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createIndexStatement3968); 
					 ifNotExists = true; 
					}
					break;

			}

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:719:9: ( idxName[name] )?
			int alt85=2;
			int LA85_0 = input.LA(1);
			if ( (LA85_0==IDENT||(LA85_0 >= K_AGGREGATE && LA85_0 <= K_ALL)||LA85_0==K_AS||LA85_0==K_ASCII||(LA85_0 >= K_BIGINT && LA85_0 <= K_BOOLEAN)||(LA85_0 >= K_CALLED && LA85_0 <= K_CLUSTERING)||(LA85_0 >= K_COMPACT && LA85_0 <= K_COUNTER)||(LA85_0 >= K_CUSTOM && LA85_0 <= K_DECIMAL)||(LA85_0 >= K_DISTINCT && LA85_0 <= K_DOUBLE)||(LA85_0 >= K_EXISTS && LA85_0 <= K_FLOAT)||LA85_0==K_FROZEN||(LA85_0 >= K_FUNCTION && LA85_0 <= K_FUNCTIONS)||LA85_0==K_INET||(LA85_0 >= K_INITCOND && LA85_0 <= K_INPUT)||LA85_0==K_INT||(LA85_0 >= K_JSON && LA85_0 <= K_KEYS)||(LA85_0 >= K_KEYSPACES && LA85_0 <= K_LANGUAGE)||(LA85_0 >= K_LIST && LA85_0 <= K_MAP)||LA85_0==K_NOLOGIN||LA85_0==K_NOSUPERUSER||LA85_0==K_OPTIONS||(LA85_0 >= K_PASSWORD && LA85_0 <= K_PERMISSIONS)||LA85_0==K_RETURNS||(LA85_0 >= K_ROLE && LA85_0 <= K_ROLES)||(LA85_0 >= K_SFUNC && LA85_0 <= K_TINYINT)||LA85_0==K_TRIGGER||(LA85_0 >= K_TTL && LA85_0 <= K_TYPE)||(LA85_0 >= K_USER && LA85_0 <= K_USERS)||(LA85_0 >= K_UUID && LA85_0 <= K_VARINT)||LA85_0==K_WRITETIME||(LA85_0 >= QMARK && LA85_0 <= QUOTED_NAME)) ) {
				alt85=1;
			}
			switch (alt85) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:719:10: idxName[name]
					{
					pushFollow(FOLLOW_idxName_in_createIndexStatement3984);
					idxName(name);
					state._fsp--;

					}
					break;

			}

			match(input,K_ON,FOLLOW_K_ON_in_createIndexStatement3989); 
			pushFollow(FOLLOW_columnFamilyName_in_createIndexStatement3993);
			cf=columnFamilyName();
			state._fsp--;

			match(input,168,FOLLOW_168_in_createIndexStatement3995); 
			pushFollow(FOLLOW_indexIdent_in_createIndexStatement3999);
			id=indexIdent();
			state._fsp--;

			match(input,169,FOLLOW_169_in_createIndexStatement4001); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:720:9: ( K_USING cls= STRING_LITERAL )?
			int alt86=2;
			int LA86_0 = input.LA(1);
			if ( (LA86_0==K_USING) ) {
				alt86=1;
			}
			switch (alt86) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:720:10: K_USING cls= STRING_LITERAL
					{
					match(input,K_USING,FOLLOW_K_USING_in_createIndexStatement4012); 
					cls=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_createIndexStatement4016); 
					 props.customClass = (cls!=null?cls.getText():null); 
					}
					break;

			}

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:721:9: ( K_WITH properties[props] )?
			int alt87=2;
			int LA87_0 = input.LA(1);
			if ( (LA87_0==K_WITH) ) {
				alt87=1;
			}
			switch (alt87) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:721:10: K_WITH properties[props]
					{
					match(input,K_WITH,FOLLOW_K_WITH_in_createIndexStatement4031); 
					pushFollow(FOLLOW_properties_in_createIndexStatement4033);
					properties(props);
					state._fsp--;

					}
					break;

			}

			 expr = new CreateIndexStatement(cf, name, id, props, ifNotExists); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:725:1: indexIdent returns [IndexTarget.Raw id] : (c= cident | K_KEYS '(' c= cident ')' | K_ENTRIES '(' c= cident ')' | K_FULL '(' c= cident ')' );
	public final IndexTarget.Raw indexIdent() throws RecognitionException {
		IndexTarget.Raw id = null;


		ColumnIdentifier.Raw c =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:726:5: (c= cident | K_KEYS '(' c= cident ')' | K_ENTRIES '(' c= cident ')' | K_FULL '(' c= cident ')' )
			int alt88=4;
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
			case K_VALUES:
			case K_VARCHAR:
			case K_VARINT:
			case K_WRITETIME:
			case QUOTED_NAME:
				{
				alt88=1;
				}
				break;
			case K_KEYS:
				{
				int LA88_2 = input.LA(2);
				if ( (LA88_2==168) ) {
					alt88=2;
				}
				else if ( (LA88_2==169) ) {
					alt88=1;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 88, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case K_ENTRIES:
				{
				alt88=3;
				}
				break;
			case K_FULL:
				{
				alt88=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 88, 0, input);
				throw nvae;
			}
			switch (alt88) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:726:7: c= cident
					{
					pushFollow(FOLLOW_cident_in_indexIdent4067);
					c=cident();
					state._fsp--;

					 id = IndexTarget.Raw.valuesOf(c); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:727:7: K_KEYS '(' c= cident ')'
					{
					match(input,K_KEYS,FOLLOW_K_KEYS_in_indexIdent4095); 
					match(input,168,FOLLOW_168_in_indexIdent4097); 
					pushFollow(FOLLOW_cident_in_indexIdent4101);
					c=cident();
					state._fsp--;

					match(input,169,FOLLOW_169_in_indexIdent4103); 
					 id = IndexTarget.Raw.keysOf(c); 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:728:7: K_ENTRIES '(' c= cident ')'
					{
					match(input,K_ENTRIES,FOLLOW_K_ENTRIES_in_indexIdent4116); 
					match(input,168,FOLLOW_168_in_indexIdent4118); 
					pushFollow(FOLLOW_cident_in_indexIdent4122);
					c=cident();
					state._fsp--;

					match(input,169,FOLLOW_169_in_indexIdent4124); 
					 id = IndexTarget.Raw.keysAndValuesOf(c); 
					}
					break;
				case 4 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:729:7: K_FULL '(' c= cident ')'
					{
					match(input,K_FULL,FOLLOW_K_FULL_in_indexIdent4134); 
					match(input,168,FOLLOW_168_in_indexIdent4136); 
					pushFollow(FOLLOW_cident_in_indexIdent4140);
					c=cident();
					state._fsp--;

					match(input,169,FOLLOW_169_in_indexIdent4142); 
					 id = IndexTarget.Raw.fullCollection(c); 
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
	// $ANTLR end "indexIdent"



	// $ANTLR start "createTriggerStatement"
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:736:1: createTriggerStatement returns [CreateTriggerStatement expr] : K_CREATE K_TRIGGER ( K_IF K_NOT K_EXISTS )? (name= cident ) K_ON cf= columnFamilyName K_USING cls= STRING_LITERAL ;
	public final CreateTriggerStatement createTriggerStatement() throws RecognitionException {
		CreateTriggerStatement expr = null;


		Token cls=null;
		ColumnIdentifier.Raw name =null;
		CFName cf =null;


		        boolean ifNotExists = false;
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:740:5: ( K_CREATE K_TRIGGER ( K_IF K_NOT K_EXISTS )? (name= cident ) K_ON cf= columnFamilyName K_USING cls= STRING_LITERAL )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:740:7: K_CREATE K_TRIGGER ( K_IF K_NOT K_EXISTS )? (name= cident ) K_ON cf= columnFamilyName K_USING cls= STRING_LITERAL
			{
			match(input,K_CREATE,FOLLOW_K_CREATE_in_createTriggerStatement4180); 
			match(input,K_TRIGGER,FOLLOW_K_TRIGGER_in_createTriggerStatement4182); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:740:26: ( K_IF K_NOT K_EXISTS )?
			int alt89=2;
			int LA89_0 = input.LA(1);
			if ( (LA89_0==K_IF) ) {
				alt89=1;
			}
			switch (alt89) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:740:27: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_createTriggerStatement4185); 
					match(input,K_NOT,FOLLOW_K_NOT_in_createTriggerStatement4187); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createTriggerStatement4189); 
					 ifNotExists = true; 
					}
					break;

			}

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:740:74: (name= cident )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:740:75: name= cident
			{
			pushFollow(FOLLOW_cident_in_createTriggerStatement4199);
			name=cident();
			state._fsp--;

			}

			match(input,K_ON,FOLLOW_K_ON_in_createTriggerStatement4210); 
			pushFollow(FOLLOW_columnFamilyName_in_createTriggerStatement4214);
			cf=columnFamilyName();
			state._fsp--;

			match(input,K_USING,FOLLOW_K_USING_in_createTriggerStatement4216); 
			cls=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_createTriggerStatement4220); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:748:1: dropTriggerStatement returns [DropTriggerStatement expr] : K_DROP K_TRIGGER ( K_IF K_EXISTS )? (name= cident ) K_ON cf= columnFamilyName ;
	public final DropTriggerStatement dropTriggerStatement() throws RecognitionException {
		DropTriggerStatement expr = null;


		ColumnIdentifier.Raw name =null;
		CFName cf =null;

		 boolean ifExists = false; 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:750:5: ( K_DROP K_TRIGGER ( K_IF K_EXISTS )? (name= cident ) K_ON cf= columnFamilyName )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:750:7: K_DROP K_TRIGGER ( K_IF K_EXISTS )? (name= cident ) K_ON cf= columnFamilyName
			{
			match(input,K_DROP,FOLLOW_K_DROP_in_dropTriggerStatement4261); 
			match(input,K_TRIGGER,FOLLOW_K_TRIGGER_in_dropTriggerStatement4263); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:750:24: ( K_IF K_EXISTS )?
			int alt90=2;
			int LA90_0 = input.LA(1);
			if ( (LA90_0==K_IF) ) {
				alt90=1;
			}
			switch (alt90) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:750:25: K_IF K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_dropTriggerStatement4266); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropTriggerStatement4268); 
					 ifExists = true; 
					}
					break;

			}

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:750:63: (name= cident )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:750:64: name= cident
			{
			pushFollow(FOLLOW_cident_in_dropTriggerStatement4278);
			name=cident();
			state._fsp--;

			}

			match(input,K_ON,FOLLOW_K_ON_in_dropTriggerStatement4281); 
			pushFollow(FOLLOW_columnFamilyName_in_dropTriggerStatement4285);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:757:1: alterKeyspaceStatement returns [AlterKeyspaceStatement expr] : K_ALTER K_KEYSPACE ks= keyspaceName K_WITH properties[attrs] ;
	public final AlterKeyspaceStatement alterKeyspaceStatement() throws RecognitionException {
		AlterKeyspaceStatement expr = null;


		String ks =null;

		 KSPropDefs attrs = new KSPropDefs(); 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:759:5: ( K_ALTER K_KEYSPACE ks= keyspaceName K_WITH properties[attrs] )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:759:7: K_ALTER K_KEYSPACE ks= keyspaceName K_WITH properties[attrs]
			{
			match(input,K_ALTER,FOLLOW_K_ALTER_in_alterKeyspaceStatement4325); 
			match(input,K_KEYSPACE,FOLLOW_K_KEYSPACE_in_alterKeyspaceStatement4327); 
			pushFollow(FOLLOW_keyspaceName_in_alterKeyspaceStatement4331);
			ks=keyspaceName();
			state._fsp--;

			match(input,K_WITH,FOLLOW_K_WITH_in_alterKeyspaceStatement4341); 
			pushFollow(FOLLOW_properties_in_alterKeyspaceStatement4343);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:771:1: alterTableStatement returns [AlterTableStatement expr] : K_ALTER K_COLUMNFAMILY cf= columnFamilyName ( K_ALTER id= cident K_TYPE v= comparatorType | K_ADD id= cident v= comparatorType ( K_STATIC )? | K_DROP id= cident | K_WITH properties[props] | K_RENAME id1= cident K_TO toId1= cident ( K_AND idn= cident K_TO toIdn= cident )* ) ;
	public final AlterTableStatement alterTableStatement() throws RecognitionException {
		AlterTableStatement expr = null;


		CFName cf =null;
		ColumnIdentifier.Raw id =null;
		CQL3Type.Raw v =null;
		ColumnIdentifier.Raw id1 =null;
		ColumnIdentifier.Raw toId1 =null;
		ColumnIdentifier.Raw idn =null;
		ColumnIdentifier.Raw toIdn =null;


		        AlterTableStatement.Type type = null;
		        CFPropDefs props = new CFPropDefs();
		        Map<ColumnIdentifier.Raw, ColumnIdentifier.Raw> renames = new HashMap<ColumnIdentifier.Raw, ColumnIdentifier.Raw>();
		        boolean isStatic = false;
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:778:5: ( K_ALTER K_COLUMNFAMILY cf= columnFamilyName ( K_ALTER id= cident K_TYPE v= comparatorType | K_ADD id= cident v= comparatorType ( K_STATIC )? | K_DROP id= cident | K_WITH properties[props] | K_RENAME id1= cident K_TO toId1= cident ( K_AND idn= cident K_TO toIdn= cident )* ) )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:778:7: K_ALTER K_COLUMNFAMILY cf= columnFamilyName ( K_ALTER id= cident K_TYPE v= comparatorType | K_ADD id= cident v= comparatorType ( K_STATIC )? | K_DROP id= cident | K_WITH properties[props] | K_RENAME id1= cident K_TO toId1= cident ( K_AND idn= cident K_TO toIdn= cident )* )
			{
			match(input,K_ALTER,FOLLOW_K_ALTER_in_alterTableStatement4379); 
			match(input,K_COLUMNFAMILY,FOLLOW_K_COLUMNFAMILY_in_alterTableStatement4381); 
			pushFollow(FOLLOW_columnFamilyName_in_alterTableStatement4385);
			cf=columnFamilyName();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:779:11: ( K_ALTER id= cident K_TYPE v= comparatorType | K_ADD id= cident v= comparatorType ( K_STATIC )? | K_DROP id= cident | K_WITH properties[props] | K_RENAME id1= cident K_TO toId1= cident ( K_AND idn= cident K_TO toIdn= cident )* )
			int alt93=5;
			switch ( input.LA(1) ) {
			case K_ALTER:
				{
				alt93=1;
				}
				break;
			case K_ADD:
				{
				alt93=2;
				}
				break;
			case K_DROP:
				{
				alt93=3;
				}
				break;
			case K_WITH:
				{
				alt93=4;
				}
				break;
			case K_RENAME:
				{
				alt93=5;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 93, 0, input);
				throw nvae;
			}
			switch (alt93) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:779:13: K_ALTER id= cident K_TYPE v= comparatorType
					{
					match(input,K_ALTER,FOLLOW_K_ALTER_in_alterTableStatement4399); 
					pushFollow(FOLLOW_cident_in_alterTableStatement4403);
					id=cident();
					state._fsp--;

					match(input,K_TYPE,FOLLOW_K_TYPE_in_alterTableStatement4405); 
					pushFollow(FOLLOW_comparatorType_in_alterTableStatement4409);
					v=comparatorType();
					state._fsp--;

					 type = AlterTableStatement.Type.ALTER; 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:780:13: K_ADD id= cident v= comparatorType ( K_STATIC )?
					{
					match(input,K_ADD,FOLLOW_K_ADD_in_alterTableStatement4425); 
					pushFollow(FOLLOW_cident_in_alterTableStatement4431);
					id=cident();
					state._fsp--;

					pushFollow(FOLLOW_comparatorType_in_alterTableStatement4435);
					v=comparatorType();
					state._fsp--;

					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:780:48: ( K_STATIC )?
					int alt91=2;
					int LA91_0 = input.LA(1);
					if ( (LA91_0==K_STATIC) ) {
						alt91=1;
					}
					switch (alt91) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:780:49: K_STATIC
							{
							 isStatic=true; 
							match(input,K_STATIC,FOLLOW_K_STATIC_in_alterTableStatement4440); 
							}
							break;

					}

					 type = AlterTableStatement.Type.ADD; 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:781:13: K_DROP id= cident
					{
					match(input,K_DROP,FOLLOW_K_DROP_in_alterTableStatement4458); 
					pushFollow(FOLLOW_cident_in_alterTableStatement4463);
					id=cident();
					state._fsp--;

					 type = AlterTableStatement.Type.DROP; 
					}
					break;
				case 4 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:782:13: K_WITH properties[props]
					{
					match(input,K_WITH,FOLLOW_K_WITH_in_alterTableStatement4503); 
					pushFollow(FOLLOW_properties_in_alterTableStatement4506);
					properties(props);
					state._fsp--;

					 type = AlterTableStatement.Type.OPTS; 
					}
					break;
				case 5 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:783:13: K_RENAME id1= cident K_TO toId1= cident ( K_AND idn= cident K_TO toIdn= cident )*
					{
					match(input,K_RENAME,FOLLOW_K_RENAME_in_alterTableStatement4539); 
					 type = AlterTableStatement.Type.RENAME; 
					pushFollow(FOLLOW_cident_in_alterTableStatement4593);
					id1=cident();
					state._fsp--;

					match(input,K_TO,FOLLOW_K_TO_in_alterTableStatement4595); 
					pushFollow(FOLLOW_cident_in_alterTableStatement4599);
					toId1=cident();
					state._fsp--;

					 renames.put(id1, toId1); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:785:16: ( K_AND idn= cident K_TO toIdn= cident )*
					loop92:
					while (true) {
						int alt92=2;
						int LA92_0 = input.LA(1);
						if ( (LA92_0==K_AND) ) {
							alt92=1;
						}

						switch (alt92) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:785:18: K_AND idn= cident K_TO toIdn= cident
							{
							match(input,K_AND,FOLLOW_K_AND_in_alterTableStatement4620); 
							pushFollow(FOLLOW_cident_in_alterTableStatement4624);
							idn=cident();
							state._fsp--;

							match(input,K_TO,FOLLOW_K_TO_in_alterTableStatement4626); 
							pushFollow(FOLLOW_cident_in_alterTableStatement4630);
							toIdn=cident();
							state._fsp--;

							 renames.put(idn, toIdn); 
							}
							break;

						default :
							break loop92;
						}
					}

					}
					break;

			}


			        expr = new AlterTableStatement(cf, type, id, v, props, renames, isStatic);
			    
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



	// $ANTLR start "alterTypeStatement"
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:797:1: alterTypeStatement returns [AlterTypeStatement expr] : K_ALTER K_TYPE name= userTypeName ( K_ALTER f= ident K_TYPE v= comparatorType | K_ADD f= ident v= comparatorType | K_RENAME id1= ident K_TO toId1= ident ( K_AND idn= ident K_TO toIdn= ident )* ) ;
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
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:798:5: ( K_ALTER K_TYPE name= userTypeName ( K_ALTER f= ident K_TYPE v= comparatorType | K_ADD f= ident v= comparatorType | K_RENAME id1= ident K_TO toId1= ident ( K_AND idn= ident K_TO toIdn= ident )* ) )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:798:7: K_ALTER K_TYPE name= userTypeName ( K_ALTER f= ident K_TYPE v= comparatorType | K_ADD f= ident v= comparatorType | K_RENAME id1= ident K_TO toId1= ident ( K_AND idn= ident K_TO toIdn= ident )* )
			{
			match(input,K_ALTER,FOLLOW_K_ALTER_in_alterTypeStatement4676); 
			match(input,K_TYPE,FOLLOW_K_TYPE_in_alterTypeStatement4678); 
			pushFollow(FOLLOW_userTypeName_in_alterTypeStatement4682);
			name=userTypeName();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:799:11: ( K_ALTER f= ident K_TYPE v= comparatorType | K_ADD f= ident v= comparatorType | K_RENAME id1= ident K_TO toId1= ident ( K_AND idn= ident K_TO toIdn= ident )* )
			int alt95=3;
			switch ( input.LA(1) ) {
			case K_ALTER:
				{
				alt95=1;
				}
				break;
			case K_ADD:
				{
				alt95=2;
				}
				break;
			case K_RENAME:
				{
				alt95=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 95, 0, input);
				throw nvae;
			}
			switch (alt95) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:799:13: K_ALTER f= ident K_TYPE v= comparatorType
					{
					match(input,K_ALTER,FOLLOW_K_ALTER_in_alterTypeStatement4696); 
					pushFollow(FOLLOW_ident_in_alterTypeStatement4700);
					f=ident();
					state._fsp--;

					match(input,K_TYPE,FOLLOW_K_TYPE_in_alterTypeStatement4702); 
					pushFollow(FOLLOW_comparatorType_in_alterTypeStatement4706);
					v=comparatorType();
					state._fsp--;

					 expr = AlterTypeStatement.alter(name, f, v); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:800:13: K_ADD f= ident v= comparatorType
					{
					match(input,K_ADD,FOLLOW_K_ADD_in_alterTypeStatement4722); 
					pushFollow(FOLLOW_ident_in_alterTypeStatement4728);
					f=ident();
					state._fsp--;

					pushFollow(FOLLOW_comparatorType_in_alterTypeStatement4732);
					v=comparatorType();
					state._fsp--;

					 expr = AlterTypeStatement.addition(name, f, v); 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:801:13: K_RENAME id1= ident K_TO toId1= ident ( K_AND idn= ident K_TO toIdn= ident )*
					{
					match(input,K_RENAME,FOLLOW_K_RENAME_in_alterTypeStatement4755); 
					 Map<ColumnIdentifier, ColumnIdentifier> renames = new HashMap<ColumnIdentifier, ColumnIdentifier>(); 
					pushFollow(FOLLOW_ident_in_alterTypeStatement4793);
					id1=ident();
					state._fsp--;

					match(input,K_TO,FOLLOW_K_TO_in_alterTypeStatement4795); 
					pushFollow(FOLLOW_ident_in_alterTypeStatement4799);
					toId1=ident();
					state._fsp--;

					 renames.put(id1, toId1); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:804:18: ( K_AND idn= ident K_TO toIdn= ident )*
					loop94:
					while (true) {
						int alt94=2;
						int LA94_0 = input.LA(1);
						if ( (LA94_0==K_AND) ) {
							alt94=1;
						}

						switch (alt94) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:804:20: K_AND idn= ident K_TO toIdn= ident
							{
							match(input,K_AND,FOLLOW_K_AND_in_alterTypeStatement4822); 
							pushFollow(FOLLOW_ident_in_alterTypeStatement4826);
							idn=ident();
							state._fsp--;

							match(input,K_TO,FOLLOW_K_TO_in_alterTypeStatement4828); 
							pushFollow(FOLLOW_ident_in_alterTypeStatement4832);
							toIdn=ident();
							state._fsp--;

							 renames.put(idn, toIdn); 
							}
							break;

						default :
							break loop94;
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:813:1: dropKeyspaceStatement returns [DropKeyspaceStatement ksp] : K_DROP K_KEYSPACE ( K_IF K_EXISTS )? ks= keyspaceName ;
	public final DropKeyspaceStatement dropKeyspaceStatement() throws RecognitionException {
		DropKeyspaceStatement ksp = null;


		String ks =null;

		 boolean ifExists = false; 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:815:5: ( K_DROP K_KEYSPACE ( K_IF K_EXISTS )? ks= keyspaceName )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:815:7: K_DROP K_KEYSPACE ( K_IF K_EXISTS )? ks= keyspaceName
			{
			match(input,K_DROP,FOLLOW_K_DROP_in_dropKeyspaceStatement4899); 
			match(input,K_KEYSPACE,FOLLOW_K_KEYSPACE_in_dropKeyspaceStatement4901); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:815:25: ( K_IF K_EXISTS )?
			int alt96=2;
			int LA96_0 = input.LA(1);
			if ( (LA96_0==K_IF) ) {
				alt96=1;
			}
			switch (alt96) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:815:26: K_IF K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_dropKeyspaceStatement4904); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropKeyspaceStatement4906); 
					 ifExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_keyspaceName_in_dropKeyspaceStatement4915);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:821:1: dropTableStatement returns [DropTableStatement stmt] : K_DROP K_COLUMNFAMILY ( K_IF K_EXISTS )? cf= columnFamilyName ;
	public final DropTableStatement dropTableStatement() throws RecognitionException {
		DropTableStatement stmt = null;


		CFName cf =null;

		 boolean ifExists = false; 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:823:5: ( K_DROP K_COLUMNFAMILY ( K_IF K_EXISTS )? cf= columnFamilyName )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:823:7: K_DROP K_COLUMNFAMILY ( K_IF K_EXISTS )? cf= columnFamilyName
			{
			match(input,K_DROP,FOLLOW_K_DROP_in_dropTableStatement4949); 
			match(input,K_COLUMNFAMILY,FOLLOW_K_COLUMNFAMILY_in_dropTableStatement4951); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:823:29: ( K_IF K_EXISTS )?
			int alt97=2;
			int LA97_0 = input.LA(1);
			if ( (LA97_0==K_IF) ) {
				alt97=1;
			}
			switch (alt97) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:823:30: K_IF K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_dropTableStatement4954); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropTableStatement4956); 
					 ifExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_columnFamilyName_in_dropTableStatement4965);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:829:1: dropTypeStatement returns [DropTypeStatement stmt] : K_DROP K_TYPE ( K_IF K_EXISTS )? name= userTypeName ;
	public final DropTypeStatement dropTypeStatement() throws RecognitionException {
		DropTypeStatement stmt = null;


		UTName name =null;

		 boolean ifExists = false; 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:831:5: ( K_DROP K_TYPE ( K_IF K_EXISTS )? name= userTypeName )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:831:7: K_DROP K_TYPE ( K_IF K_EXISTS )? name= userTypeName
			{
			match(input,K_DROP,FOLLOW_K_DROP_in_dropTypeStatement4999); 
			match(input,K_TYPE,FOLLOW_K_TYPE_in_dropTypeStatement5001); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:831:21: ( K_IF K_EXISTS )?
			int alt98=2;
			int LA98_0 = input.LA(1);
			if ( (LA98_0==K_IF) ) {
				alt98=1;
			}
			switch (alt98) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:831:22: K_IF K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_dropTypeStatement5004); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropTypeStatement5006); 
					 ifExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_userTypeName_in_dropTypeStatement5015);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:837:1: dropIndexStatement returns [DropIndexStatement expr] : K_DROP K_INDEX ( K_IF K_EXISTS )? index= indexName ;
	public final DropIndexStatement dropIndexStatement() throws RecognitionException {
		DropIndexStatement expr = null;


		IndexName index =null;

		 boolean ifExists = false; 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:839:5: ( K_DROP K_INDEX ( K_IF K_EXISTS )? index= indexName )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:839:7: K_DROP K_INDEX ( K_IF K_EXISTS )? index= indexName
			{
			match(input,K_DROP,FOLLOW_K_DROP_in_dropIndexStatement5049); 
			match(input,K_INDEX,FOLLOW_K_INDEX_in_dropIndexStatement5051); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:839:22: ( K_IF K_EXISTS )?
			int alt99=2;
			int LA99_0 = input.LA(1);
			if ( (LA99_0==K_IF) ) {
				alt99=1;
			}
			switch (alt99) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:839:23: K_IF K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_dropIndexStatement5054); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropIndexStatement5056); 
					 ifExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_indexName_in_dropIndexStatement5065);
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



	// $ANTLR start "truncateStatement"
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:846:1: truncateStatement returns [TruncateStatement stmt] : K_TRUNCATE ( K_COLUMNFAMILY )? cf= columnFamilyName ;
	public final TruncateStatement truncateStatement() throws RecognitionException {
		TruncateStatement stmt = null;


		CFName cf =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:847:5: ( K_TRUNCATE ( K_COLUMNFAMILY )? cf= columnFamilyName )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:847:7: K_TRUNCATE ( K_COLUMNFAMILY )? cf= columnFamilyName
			{
			match(input,K_TRUNCATE,FOLLOW_K_TRUNCATE_in_truncateStatement5096); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:847:18: ( K_COLUMNFAMILY )?
			int alt100=2;
			int LA100_0 = input.LA(1);
			if ( (LA100_0==K_COLUMNFAMILY) ) {
				alt100=1;
			}
			switch (alt100) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:847:19: K_COLUMNFAMILY
					{
					match(input,K_COLUMNFAMILY,FOLLOW_K_COLUMNFAMILY_in_truncateStatement5099); 
					}
					break;

			}

			pushFollow(FOLLOW_columnFamilyName_in_truncateStatement5105);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:853:1: grantPermissionsStatement returns [GrantPermissionsStatement stmt] : K_GRANT permissionOrAll K_ON resource K_TO grantee= userOrRoleName ;
	public final GrantPermissionsStatement grantPermissionsStatement() throws RecognitionException {
		GrantPermissionsStatement stmt = null;


		RoleName grantee =null;
		Set<Permission> permissionOrAll1 =null;
		IResource resource2 =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:854:5: ( K_GRANT permissionOrAll K_ON resource K_TO grantee= userOrRoleName )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:854:7: K_GRANT permissionOrAll K_ON resource K_TO grantee= userOrRoleName
			{
			match(input,K_GRANT,FOLLOW_K_GRANT_in_grantPermissionsStatement5130); 
			pushFollow(FOLLOW_permissionOrAll_in_grantPermissionsStatement5142);
			permissionOrAll1=permissionOrAll();
			state._fsp--;

			match(input,K_ON,FOLLOW_K_ON_in_grantPermissionsStatement5150); 
			pushFollow(FOLLOW_resource_in_grantPermissionsStatement5162);
			resource2=resource();
			state._fsp--;

			match(input,K_TO,FOLLOW_K_TO_in_grantPermissionsStatement5170); 
			pushFollow(FOLLOW_userOrRoleName_in_grantPermissionsStatement5184);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:866:1: revokePermissionsStatement returns [RevokePermissionsStatement stmt] : K_REVOKE permissionOrAll K_ON resource K_FROM revokee= userOrRoleName ;
	public final RevokePermissionsStatement revokePermissionsStatement() throws RecognitionException {
		RevokePermissionsStatement stmt = null;


		RoleName revokee =null;
		Set<Permission> permissionOrAll3 =null;
		IResource resource4 =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:867:5: ( K_REVOKE permissionOrAll K_ON resource K_FROM revokee= userOrRoleName )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:867:7: K_REVOKE permissionOrAll K_ON resource K_FROM revokee= userOrRoleName
			{
			match(input,K_REVOKE,FOLLOW_K_REVOKE_in_revokePermissionsStatement5215); 
			pushFollow(FOLLOW_permissionOrAll_in_revokePermissionsStatement5227);
			permissionOrAll3=permissionOrAll();
			state._fsp--;

			match(input,K_ON,FOLLOW_K_ON_in_revokePermissionsStatement5235); 
			pushFollow(FOLLOW_resource_in_revokePermissionsStatement5247);
			resource4=resource();
			state._fsp--;

			match(input,K_FROM,FOLLOW_K_FROM_in_revokePermissionsStatement5255); 
			pushFollow(FOLLOW_userOrRoleName_in_revokePermissionsStatement5269);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:879:1: grantRoleStatement returns [GrantRoleStatement stmt] : K_GRANT role= userOrRoleName K_TO grantee= userOrRoleName ;
	public final GrantRoleStatement grantRoleStatement() throws RecognitionException {
		GrantRoleStatement stmt = null;


		RoleName role =null;
		RoleName grantee =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:880:5: ( K_GRANT role= userOrRoleName K_TO grantee= userOrRoleName )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:880:7: K_GRANT role= userOrRoleName K_TO grantee= userOrRoleName
			{
			match(input,K_GRANT,FOLLOW_K_GRANT_in_grantRoleStatement5300); 
			pushFollow(FOLLOW_userOrRoleName_in_grantRoleStatement5314);
			role=userOrRoleName();
			state._fsp--;

			match(input,K_TO,FOLLOW_K_TO_in_grantRoleStatement5322); 
			pushFollow(FOLLOW_userOrRoleName_in_grantRoleStatement5336);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:890:1: revokeRoleStatement returns [RevokeRoleStatement stmt] : K_REVOKE role= userOrRoleName K_FROM revokee= userOrRoleName ;
	public final RevokeRoleStatement revokeRoleStatement() throws RecognitionException {
		RevokeRoleStatement stmt = null;


		RoleName role =null;
		RoleName revokee =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:891:5: ( K_REVOKE role= userOrRoleName K_FROM revokee= userOrRoleName )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:891:7: K_REVOKE role= userOrRoleName K_FROM revokee= userOrRoleName
			{
			match(input,K_REVOKE,FOLLOW_K_REVOKE_in_revokeRoleStatement5367); 
			pushFollow(FOLLOW_userOrRoleName_in_revokeRoleStatement5381);
			role=userOrRoleName();
			state._fsp--;

			match(input,K_FROM,FOLLOW_K_FROM_in_revokeRoleStatement5389); 
			pushFollow(FOLLOW_userOrRoleName_in_revokeRoleStatement5403);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:898:1: listPermissionsStatement returns [ListPermissionsStatement stmt] : K_LIST permissionOrAll ( K_ON resource )? ( K_OF roleName[grantee] )? ( K_NORECURSIVE )? ;
	public final ListPermissionsStatement listPermissionsStatement() throws RecognitionException {
		ListPermissionsStatement stmt = null;


		IResource resource5 =null;
		Set<Permission> permissionOrAll6 =null;


		        IResource resource = null;
		        boolean recursive = true;
		        RoleName grantee = new RoleName();
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:904:5: ( K_LIST permissionOrAll ( K_ON resource )? ( K_OF roleName[grantee] )? ( K_NORECURSIVE )? )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:904:7: K_LIST permissionOrAll ( K_ON resource )? ( K_OF roleName[grantee] )? ( K_NORECURSIVE )?
			{
			match(input,K_LIST,FOLLOW_K_LIST_in_listPermissionsStatement5441); 
			pushFollow(FOLLOW_permissionOrAll_in_listPermissionsStatement5453);
			permissionOrAll6=permissionOrAll();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:906:7: ( K_ON resource )?
			int alt101=2;
			int LA101_0 = input.LA(1);
			if ( (LA101_0==K_ON) ) {
				alt101=1;
			}
			switch (alt101) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:906:9: K_ON resource
					{
					match(input,K_ON,FOLLOW_K_ON_in_listPermissionsStatement5463); 
					pushFollow(FOLLOW_resource_in_listPermissionsStatement5465);
					resource5=resource();
					state._fsp--;

					 resource = resource5; 
					}
					break;

			}

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:907:7: ( K_OF roleName[grantee] )?
			int alt102=2;
			int LA102_0 = input.LA(1);
			if ( (LA102_0==K_OF) ) {
				alt102=1;
			}
			switch (alt102) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:907:9: K_OF roleName[grantee]
					{
					match(input,K_OF,FOLLOW_K_OF_in_listPermissionsStatement5480); 
					pushFollow(FOLLOW_roleName_in_listPermissionsStatement5482);
					roleName(grantee);
					state._fsp--;

					}
					break;

			}

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:908:7: ( K_NORECURSIVE )?
			int alt103=2;
			int LA103_0 = input.LA(1);
			if ( (LA103_0==K_NORECURSIVE) ) {
				alt103=1;
			}
			switch (alt103) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:908:9: K_NORECURSIVE
					{
					match(input,K_NORECURSIVE,FOLLOW_K_NORECURSIVE_in_listPermissionsStatement5496); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:912:1: permission returns [Permission perm] : p= ( K_CREATE | K_ALTER | K_DROP | K_SELECT | K_MODIFY | K_AUTHORIZE | K_DESCRIBE | K_EXECUTE ) ;
	public final Permission permission() throws RecognitionException {
		Permission perm = null;


		Token p=null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:913:5: (p= ( K_CREATE | K_ALTER | K_DROP | K_SELECT | K_MODIFY | K_AUTHORIZE | K_DESCRIBE | K_EXECUTE ) )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:913:7: p= ( K_CREATE | K_ALTER | K_DROP | K_SELECT | K_MODIFY | K_AUTHORIZE | K_DESCRIBE | K_EXECUTE )
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:917:1: permissionOrAll returns [Set<Permission> perms] : ( K_ALL ( K_PERMISSIONS )? |p= permission ( K_PERMISSION )? );
	public final Set<Permission> permissionOrAll() throws RecognitionException {
		Set<Permission> perms = null;


		Permission p =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:918:5: ( K_ALL ( K_PERMISSIONS )? |p= permission ( K_PERMISSION )? )
			int alt106=2;
			int LA106_0 = input.LA(1);
			if ( (LA106_0==K_ALL) ) {
				alt106=1;
			}
			else if ( (LA106_0==K_ALTER||LA106_0==K_AUTHORIZE||LA106_0==K_CREATE||LA106_0==K_DESCRIBE||LA106_0==K_DROP||LA106_0==K_EXECUTE||LA106_0==K_MODIFY||LA106_0==K_SELECT) ) {
				alt106=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 106, 0, input);
				throw nvae;
			}

			switch (alt106) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:918:7: K_ALL ( K_PERMISSIONS )?
					{
					match(input,K_ALL,FOLLOW_K_ALL_in_permissionOrAll5589); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:918:13: ( K_PERMISSIONS )?
					int alt104=2;
					int LA104_0 = input.LA(1);
					if ( (LA104_0==K_PERMISSIONS) ) {
						alt104=1;
					}
					switch (alt104) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:918:15: K_PERMISSIONS
							{
							match(input,K_PERMISSIONS,FOLLOW_K_PERMISSIONS_in_permissionOrAll5593); 
							}
							break;

					}

					 perms = Permission.ALL; 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:919:7: p= permission ( K_PERMISSION )?
					{
					pushFollow(FOLLOW_permission_in_permissionOrAll5614);
					p=permission();
					state._fsp--;

					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:919:20: ( K_PERMISSION )?
					int alt105=2;
					int LA105_0 = input.LA(1);
					if ( (LA105_0==K_PERMISSION) ) {
						alt105=1;
					}
					switch (alt105) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:919:22: K_PERMISSION
							{
							match(input,K_PERMISSION,FOLLOW_K_PERMISSION_in_permissionOrAll5618); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:922:1: resource returns [IResource res] : (d= dataResource |r= roleResource |f= functionResource );
	public final IResource resource() throws RecognitionException {
		IResource res = null;


		DataResource d =null;
		RoleResource r =null;
		FunctionResource f =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:923:5: (d= dataResource |r= roleResource |f= functionResource )
			int alt107=3;
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
				case 173:
				case 175:
					{
					alt107=1;
					}
					break;
				case K_ROLES:
					{
					alt107=2;
					}
					break;
				case K_FUNCTIONS:
					{
					alt107=3;
					}
					break;
				default:
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 107, 1, input);
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
				alt107=1;
				}
				break;
			case K_ROLE:
				{
				int LA107_3 = input.LA(2);
				if ( (LA107_3==EOF||LA107_3==K_FROM||LA107_3==K_NORECURSIVE||LA107_3==K_OF||LA107_3==K_TO||LA107_3==173||LA107_3==175) ) {
					alt107=1;
				}
				else if ( (LA107_3==IDENT||(LA107_3 >= K_AGGREGATE && LA107_3 <= K_ALL)||LA107_3==K_AS||LA107_3==K_ASCII||(LA107_3 >= K_BIGINT && LA107_3 <= K_BOOLEAN)||(LA107_3 >= K_CALLED && LA107_3 <= K_CLUSTERING)||(LA107_3 >= K_COMPACT && LA107_3 <= K_COUNTER)||(LA107_3 >= K_CUSTOM && LA107_3 <= K_DECIMAL)||(LA107_3 >= K_DISTINCT && LA107_3 <= K_DOUBLE)||(LA107_3 >= K_EXISTS && LA107_3 <= K_FLOAT)||LA107_3==K_FROZEN||(LA107_3 >= K_FUNCTION && LA107_3 <= K_FUNCTIONS)||LA107_3==K_INET||(LA107_3 >= K_INITCOND && LA107_3 <= K_INPUT)||LA107_3==K_INT||(LA107_3 >= K_JSON && LA107_3 <= K_KEYS)||(LA107_3 >= K_KEYSPACES && LA107_3 <= K_LANGUAGE)||(LA107_3 >= K_LIST && LA107_3 <= K_MAP)||LA107_3==K_NOLOGIN||LA107_3==K_NOSUPERUSER||LA107_3==K_OPTIONS||(LA107_3 >= K_PASSWORD && LA107_3 <= K_PERMISSIONS)||LA107_3==K_RETURNS||(LA107_3 >= K_ROLE && LA107_3 <= K_ROLES)||(LA107_3 >= K_SFUNC && LA107_3 <= K_TINYINT)||LA107_3==K_TRIGGER||(LA107_3 >= K_TTL && LA107_3 <= K_TYPE)||(LA107_3 >= K_USER && LA107_3 <= K_USERS)||(LA107_3 >= K_UUID && LA107_3 <= K_VARINT)||LA107_3==K_WRITETIME||(LA107_3 >= QMARK && LA107_3 <= QUOTED_NAME)||LA107_3==STRING_LITERAL) ) {
					alt107=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 107, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case K_FUNCTION:
				{
				int LA107_4 = input.LA(2);
				if ( (LA107_4==EOF||LA107_4==K_FROM||LA107_4==K_NORECURSIVE||LA107_4==K_OF||LA107_4==K_TO||LA107_4==173||LA107_4==175) ) {
					alt107=1;
				}
				else if ( (LA107_4==IDENT||(LA107_4 >= K_AGGREGATE && LA107_4 <= K_ALL)||LA107_4==K_AS||LA107_4==K_ASCII||(LA107_4 >= K_BIGINT && LA107_4 <= K_BOOLEAN)||(LA107_4 >= K_CALLED && LA107_4 <= K_CLUSTERING)||(LA107_4 >= K_COMPACT && LA107_4 <= K_COUNTER)||(LA107_4 >= K_CUSTOM && LA107_4 <= K_DECIMAL)||(LA107_4 >= K_DISTINCT && LA107_4 <= K_DOUBLE)||(LA107_4 >= K_EXISTS && LA107_4 <= K_FLOAT)||LA107_4==K_FROZEN||(LA107_4 >= K_FUNCTION && LA107_4 <= K_FUNCTIONS)||LA107_4==K_INET||(LA107_4 >= K_INITCOND && LA107_4 <= K_INPUT)||LA107_4==K_INT||(LA107_4 >= K_JSON && LA107_4 <= K_KEYS)||(LA107_4 >= K_KEYSPACES && LA107_4 <= K_LANGUAGE)||(LA107_4 >= K_LIST && LA107_4 <= K_MAP)||LA107_4==K_NOLOGIN||LA107_4==K_NOSUPERUSER||LA107_4==K_OPTIONS||(LA107_4 >= K_PASSWORD && LA107_4 <= K_PERMISSIONS)||LA107_4==K_RETURNS||(LA107_4 >= K_ROLE && LA107_4 <= K_ROLES)||(LA107_4 >= K_SFUNC && LA107_4 <= K_TINYINT)||(LA107_4 >= K_TOKEN && LA107_4 <= K_TRIGGER)||(LA107_4 >= K_TTL && LA107_4 <= K_TYPE)||(LA107_4 >= K_USER && LA107_4 <= K_USERS)||(LA107_4 >= K_UUID && LA107_4 <= K_VARINT)||LA107_4==K_WRITETIME||(LA107_4 >= QMARK && LA107_4 <= QUOTED_NAME)) ) {
					alt107=3;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 107, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 107, 0, input);
				throw nvae;
			}
			switch (alt107) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:923:7: d= dataResource
					{
					pushFollow(FOLLOW_dataResource_in_resource5646);
					d=dataResource();
					state._fsp--;

					 res = d; 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:924:7: r= roleResource
					{
					pushFollow(FOLLOW_roleResource_in_resource5658);
					r=roleResource();
					state._fsp--;

					 res = r; 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:925:7: f= functionResource
					{
					pushFollow(FOLLOW_functionResource_in_resource5670);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:928:1: dataResource returns [DataResource res] : ( K_ALL K_KEYSPACES | K_KEYSPACE ks= keyspaceName | ( K_COLUMNFAMILY )? cf= columnFamilyName );
	public final DataResource dataResource() throws RecognitionException {
		DataResource res = null;


		String ks =null;
		CFName cf =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:929:5: ( K_ALL K_KEYSPACES | K_KEYSPACE ks= keyspaceName | ( K_COLUMNFAMILY )? cf= columnFamilyName )
			int alt109=3;
			switch ( input.LA(1) ) {
			case K_ALL:
				{
				int LA109_1 = input.LA(2);
				if ( (LA109_1==K_KEYSPACES) ) {
					alt109=1;
				}
				else if ( (LA109_1==EOF||LA109_1==K_FROM||LA109_1==K_NORECURSIVE||LA109_1==K_OF||LA109_1==K_TO||LA109_1==173||LA109_1==175) ) {
					alt109=3;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 109, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case K_KEYSPACE:
				{
				alt109=2;
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
				alt109=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 109, 0, input);
				throw nvae;
			}
			switch (alt109) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:929:7: K_ALL K_KEYSPACES
					{
					match(input,K_ALL,FOLLOW_K_ALL_in_dataResource5693); 
					match(input,K_KEYSPACES,FOLLOW_K_KEYSPACES_in_dataResource5695); 
					 res = DataResource.root(); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:930:7: K_KEYSPACE ks= keyspaceName
					{
					match(input,K_KEYSPACE,FOLLOW_K_KEYSPACE_in_dataResource5705); 
					pushFollow(FOLLOW_keyspaceName_in_dataResource5711);
					ks=keyspaceName();
					state._fsp--;

					 res = DataResource.keyspace(ks); 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:931:7: ( K_COLUMNFAMILY )? cf= columnFamilyName
					{
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:931:7: ( K_COLUMNFAMILY )?
					int alt108=2;
					int LA108_0 = input.LA(1);
					if ( (LA108_0==K_COLUMNFAMILY) ) {
						alt108=1;
					}
					switch (alt108) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:931:9: K_COLUMNFAMILY
							{
							match(input,K_COLUMNFAMILY,FOLLOW_K_COLUMNFAMILY_in_dataResource5723); 
							}
							break;

					}

					pushFollow(FOLLOW_columnFamilyName_in_dataResource5732);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:935:1: roleResource returns [RoleResource res] : ( K_ALL K_ROLES | K_ROLE role= userOrRoleName );
	public final RoleResource roleResource() throws RecognitionException {
		RoleResource res = null;


		RoleName role =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:936:5: ( K_ALL K_ROLES | K_ROLE role= userOrRoleName )
			int alt110=2;
			int LA110_0 = input.LA(1);
			if ( (LA110_0==K_ALL) ) {
				alt110=1;
			}
			else if ( (LA110_0==K_ROLE) ) {
				alt110=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 110, 0, input);
				throw nvae;
			}

			switch (alt110) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:936:7: K_ALL K_ROLES
					{
					match(input,K_ALL,FOLLOW_K_ALL_in_roleResource5761); 
					match(input,K_ROLES,FOLLOW_K_ROLES_in_roleResource5763); 
					 res = RoleResource.root(); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:937:7: K_ROLE role= userOrRoleName
					{
					match(input,K_ROLE,FOLLOW_K_ROLE_in_roleResource5773); 
					pushFollow(FOLLOW_userOrRoleName_in_roleResource5779);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:940:1: functionResource returns [FunctionResource res] : ( K_ALL K_FUNCTIONS | K_ALL K_FUNCTIONS K_IN K_KEYSPACE ks= keyspaceName | K_FUNCTION fn= functionName ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' ) );
	public final FunctionResource functionResource() throws RecognitionException {
		FunctionResource res = null;


		String ks =null;
		FunctionName fn =null;
		CQL3Type.Raw v =null;


		        List<CQL3Type.Raw> argsTypes = new ArrayList<>();
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:944:5: ( K_ALL K_FUNCTIONS | K_ALL K_FUNCTIONS K_IN K_KEYSPACE ks= keyspaceName | K_FUNCTION fn= functionName ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' ) )
			int alt113=3;
			int LA113_0 = input.LA(1);
			if ( (LA113_0==K_ALL) ) {
				int LA113_1 = input.LA(2);
				if ( (LA113_1==K_FUNCTIONS) ) {
					int LA113_3 = input.LA(3);
					if ( (LA113_3==K_IN) ) {
						alt113=2;
					}
					else if ( (LA113_3==EOF||LA113_3==K_FROM||LA113_3==K_NORECURSIVE||LA113_3==K_OF||LA113_3==K_TO||LA113_3==175) ) {
						alt113=1;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 113, 3, input);
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
							new NoViableAltException("", 113, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA113_0==K_FUNCTION) ) {
				alt113=3;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 113, 0, input);
				throw nvae;
			}

			switch (alt113) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:944:7: K_ALL K_FUNCTIONS
					{
					match(input,K_ALL,FOLLOW_K_ALL_in_functionResource5811); 
					match(input,K_FUNCTIONS,FOLLOW_K_FUNCTIONS_in_functionResource5813); 
					 res = FunctionResource.root(); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:945:7: K_ALL K_FUNCTIONS K_IN K_KEYSPACE ks= keyspaceName
					{
					match(input,K_ALL,FOLLOW_K_ALL_in_functionResource5823); 
					match(input,K_FUNCTIONS,FOLLOW_K_FUNCTIONS_in_functionResource5825); 
					match(input,K_IN,FOLLOW_K_IN_in_functionResource5827); 
					match(input,K_KEYSPACE,FOLLOW_K_KEYSPACE_in_functionResource5829); 
					pushFollow(FOLLOW_keyspaceName_in_functionResource5835);
					ks=keyspaceName();
					state._fsp--;

					 res = FunctionResource.keyspace(ks); 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:947:7: K_FUNCTION fn= functionName ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )
					{
					match(input,K_FUNCTION,FOLLOW_K_FUNCTION_in_functionResource5850); 
					pushFollow(FOLLOW_functionName_in_functionResource5854);
					fn=functionName();
					state._fsp--;

					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:948:7: ( '(' (v= comparatorType ( ',' v= comparatorType )* )? ')' )
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:949:9: '(' (v= comparatorType ( ',' v= comparatorType )* )? ')'
					{
					match(input,168,FOLLOW_168_in_functionResource5872); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:950:11: (v= comparatorType ( ',' v= comparatorType )* )?
					int alt112=2;
					int LA112_0 = input.LA(1);
					if ( (LA112_0==IDENT||(LA112_0 >= K_AGGREGATE && LA112_0 <= K_ALL)||LA112_0==K_AS||LA112_0==K_ASCII||(LA112_0 >= K_BIGINT && LA112_0 <= K_BOOLEAN)||(LA112_0 >= K_CALLED && LA112_0 <= K_CLUSTERING)||(LA112_0 >= K_COMPACT && LA112_0 <= K_COUNTER)||(LA112_0 >= K_CUSTOM && LA112_0 <= K_DECIMAL)||(LA112_0 >= K_DISTINCT && LA112_0 <= K_DOUBLE)||(LA112_0 >= K_EXISTS && LA112_0 <= K_FLOAT)||LA112_0==K_FROZEN||(LA112_0 >= K_FUNCTION && LA112_0 <= K_FUNCTIONS)||LA112_0==K_INET||(LA112_0 >= K_INITCOND && LA112_0 <= K_INPUT)||LA112_0==K_INT||(LA112_0 >= K_JSON && LA112_0 <= K_KEYS)||(LA112_0 >= K_KEYSPACES && LA112_0 <= K_LANGUAGE)||(LA112_0 >= K_LIST && LA112_0 <= K_MAP)||LA112_0==K_NOLOGIN||LA112_0==K_NOSUPERUSER||LA112_0==K_OPTIONS||(LA112_0 >= K_PASSWORD && LA112_0 <= K_PERMISSIONS)||LA112_0==K_RETURNS||(LA112_0 >= K_ROLE && LA112_0 <= K_ROLES)||(LA112_0 >= K_SET && LA112_0 <= K_TINYINT)||LA112_0==K_TRIGGER||(LA112_0 >= K_TTL && LA112_0 <= K_TYPE)||(LA112_0 >= K_USER && LA112_0 <= K_USERS)||(LA112_0 >= K_UUID && LA112_0 <= K_VARINT)||LA112_0==K_WRITETIME||LA112_0==QUOTED_NAME||LA112_0==STRING_LITERAL) ) {
						alt112=1;
					}
					switch (alt112) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:951:13: v= comparatorType ( ',' v= comparatorType )*
							{
							pushFollow(FOLLOW_comparatorType_in_functionResource5900);
							v=comparatorType();
							state._fsp--;

							 argsTypes.add(v); 
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:952:13: ( ',' v= comparatorType )*
							loop111:
							while (true) {
								int alt111=2;
								int LA111_0 = input.LA(1);
								if ( (LA111_0==171) ) {
									alt111=1;
								}

								switch (alt111) {
								case 1 :
									// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:952:15: ',' v= comparatorType
									{
									match(input,171,FOLLOW_171_in_functionResource5918); 
									pushFollow(FOLLOW_comparatorType_in_functionResource5922);
									v=comparatorType();
									state._fsp--;

									 argsTypes.add(v); 
									}
									break;

								default :
									break loop111;
								}
							}

							}
							break;

					}

					match(input,169,FOLLOW_169_in_functionResource5950); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:962:1: createUserStatement returns [CreateRoleStatement stmt] : K_CREATE K_USER ( K_IF K_NOT K_EXISTS )? u= username ( K_WITH userPassword[opts] )? ( K_SUPERUSER | K_NOSUPERUSER )? ;
	public final CreateRoleStatement createUserStatement() throws RecognitionException {
		CreateRoleStatement stmt = null;


		ParserRuleReturnScope u =null;


		        RoleOptions opts = new RoleOptions();
		        opts.setOption(IRoleManager.Option.LOGIN, true);
		        boolean superuser = false;
		        boolean ifNotExists = false;
		        RoleName name = new RoleName();
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:970:5: ( K_CREATE K_USER ( K_IF K_NOT K_EXISTS )? u= username ( K_WITH userPassword[opts] )? ( K_SUPERUSER | K_NOSUPERUSER )? )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:970:7: K_CREATE K_USER ( K_IF K_NOT K_EXISTS )? u= username ( K_WITH userPassword[opts] )? ( K_SUPERUSER | K_NOSUPERUSER )?
			{
			match(input,K_CREATE,FOLLOW_K_CREATE_in_createUserStatement5998); 
			match(input,K_USER,FOLLOW_K_USER_in_createUserStatement6000); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:970:23: ( K_IF K_NOT K_EXISTS )?
			int alt114=2;
			int LA114_0 = input.LA(1);
			if ( (LA114_0==K_IF) ) {
				alt114=1;
			}
			switch (alt114) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:970:24: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_createUserStatement6003); 
					match(input,K_NOT,FOLLOW_K_NOT_in_createUserStatement6005); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createUserStatement6007); 
					 ifNotExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_username_in_createUserStatement6015);
			u=username();
			state._fsp--;

			 name.setName((u!=null?input.toString(u.start,u.stop):null), true); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:971:7: ( K_WITH userPassword[opts] )?
			int alt115=2;
			int LA115_0 = input.LA(1);
			if ( (LA115_0==K_WITH) ) {
				alt115=1;
			}
			switch (alt115) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:971:9: K_WITH userPassword[opts]
					{
					match(input,K_WITH,FOLLOW_K_WITH_in_createUserStatement6027); 
					pushFollow(FOLLOW_userPassword_in_createUserStatement6029);
					userPassword(opts);
					state._fsp--;

					}
					break;

			}

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:972:7: ( K_SUPERUSER | K_NOSUPERUSER )?
			int alt116=3;
			int LA116_0 = input.LA(1);
			if ( (LA116_0==K_SUPERUSER) ) {
				alt116=1;
			}
			else if ( (LA116_0==K_NOSUPERUSER) ) {
				alt116=2;
			}
			switch (alt116) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:972:9: K_SUPERUSER
					{
					match(input,K_SUPERUSER,FOLLOW_K_SUPERUSER_in_createUserStatement6043); 
					 superuser = true; 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:972:45: K_NOSUPERUSER
					{
					match(input,K_NOSUPERUSER,FOLLOW_K_NOSUPERUSER_in_createUserStatement6049); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:980:1: alterUserStatement returns [AlterRoleStatement stmt] : K_ALTER K_USER u= username ( K_WITH userPassword[opts] )? ( K_SUPERUSER | K_NOSUPERUSER )? ;
	public final AlterRoleStatement alterUserStatement() throws RecognitionException {
		AlterRoleStatement stmt = null;


		ParserRuleReturnScope u =null;


		        RoleOptions opts = new RoleOptions();
		        RoleName name = new RoleName();
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:985:5: ( K_ALTER K_USER u= username ( K_WITH userPassword[opts] )? ( K_SUPERUSER | K_NOSUPERUSER )? )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:985:7: K_ALTER K_USER u= username ( K_WITH userPassword[opts] )? ( K_SUPERUSER | K_NOSUPERUSER )?
			{
			match(input,K_ALTER,FOLLOW_K_ALTER_in_alterUserStatement6094); 
			match(input,K_USER,FOLLOW_K_USER_in_alterUserStatement6096); 
			pushFollow(FOLLOW_username_in_alterUserStatement6100);
			u=username();
			state._fsp--;

			 name.setName((u!=null?input.toString(u.start,u.stop):null), true); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:986:7: ( K_WITH userPassword[opts] )?
			int alt117=2;
			int LA117_0 = input.LA(1);
			if ( (LA117_0==K_WITH) ) {
				alt117=1;
			}
			switch (alt117) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:986:9: K_WITH userPassword[opts]
					{
					match(input,K_WITH,FOLLOW_K_WITH_in_alterUserStatement6112); 
					pushFollow(FOLLOW_userPassword_in_alterUserStatement6114);
					userPassword(opts);
					state._fsp--;

					}
					break;

			}

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:987:7: ( K_SUPERUSER | K_NOSUPERUSER )?
			int alt118=3;
			int LA118_0 = input.LA(1);
			if ( (LA118_0==K_SUPERUSER) ) {
				alt118=1;
			}
			else if ( (LA118_0==K_NOSUPERUSER) ) {
				alt118=2;
			}
			switch (alt118) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:987:9: K_SUPERUSER
					{
					match(input,K_SUPERUSER,FOLLOW_K_SUPERUSER_in_alterUserStatement6128); 
					 opts.setOption(IRoleManager.Option.SUPERUSER, true); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:988:11: K_NOSUPERUSER
					{
					match(input,K_NOSUPERUSER,FOLLOW_K_NOSUPERUSER_in_alterUserStatement6142); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:995:1: dropUserStatement returns [DropRoleStatement stmt] : K_DROP K_USER ( K_IF K_EXISTS )? u= username ;
	public final DropRoleStatement dropUserStatement() throws RecognitionException {
		DropRoleStatement stmt = null;


		ParserRuleReturnScope u =null;


		        boolean ifExists = false;
		        RoleName name = new RoleName();
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1000:5: ( K_DROP K_USER ( K_IF K_EXISTS )? u= username )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1000:7: K_DROP K_USER ( K_IF K_EXISTS )? u= username
			{
			match(input,K_DROP,FOLLOW_K_DROP_in_dropUserStatement6188); 
			match(input,K_USER,FOLLOW_K_USER_in_dropUserStatement6190); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1000:21: ( K_IF K_EXISTS )?
			int alt119=2;
			int LA119_0 = input.LA(1);
			if ( (LA119_0==K_IF) ) {
				alt119=1;
			}
			switch (alt119) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1000:22: K_IF K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_dropUserStatement6193); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropUserStatement6195); 
					 ifExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_username_in_dropUserStatement6203);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1006:1: listUsersStatement returns [ListRolesStatement stmt] : K_LIST K_USERS ;
	public final ListRolesStatement listUsersStatement() throws RecognitionException {
		ListRolesStatement stmt = null;


		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1007:5: ( K_LIST K_USERS )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1007:7: K_LIST K_USERS
			{
			match(input,K_LIST,FOLLOW_K_LIST_in_listUsersStatement6228); 
			match(input,K_USERS,FOLLOW_K_USERS_in_listUsersStatement6230); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1019:1: createRoleStatement returns [CreateRoleStatement stmt] : K_CREATE K_ROLE ( K_IF K_NOT K_EXISTS )? name= userOrRoleName ( K_WITH roleOptions[opts] )? ;
	public final CreateRoleStatement createRoleStatement() throws RecognitionException {
		CreateRoleStatement stmt = null;


		RoleName name =null;


		        RoleOptions opts = new RoleOptions();
		        boolean ifNotExists = false;
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1024:5: ( K_CREATE K_ROLE ( K_IF K_NOT K_EXISTS )? name= userOrRoleName ( K_WITH roleOptions[opts] )? )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1024:7: K_CREATE K_ROLE ( K_IF K_NOT K_EXISTS )? name= userOrRoleName ( K_WITH roleOptions[opts] )?
			{
			match(input,K_CREATE,FOLLOW_K_CREATE_in_createRoleStatement6264); 
			match(input,K_ROLE,FOLLOW_K_ROLE_in_createRoleStatement6266); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1024:23: ( K_IF K_NOT K_EXISTS )?
			int alt120=2;
			int LA120_0 = input.LA(1);
			if ( (LA120_0==K_IF) ) {
				alt120=1;
			}
			switch (alt120) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1024:24: K_IF K_NOT K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_createRoleStatement6269); 
					match(input,K_NOT,FOLLOW_K_NOT_in_createRoleStatement6271); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_createRoleStatement6273); 
					 ifNotExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_userOrRoleName_in_createRoleStatement6281);
			name=userOrRoleName();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1025:7: ( K_WITH roleOptions[opts] )?
			int alt121=2;
			int LA121_0 = input.LA(1);
			if ( (LA121_0==K_WITH) ) {
				alt121=1;
			}
			switch (alt121) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1025:9: K_WITH roleOptions[opts]
					{
					match(input,K_WITH,FOLLOW_K_WITH_in_createRoleStatement6291); 
					pushFollow(FOLLOW_roleOptions_in_createRoleStatement6293);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1049:1: alterRoleStatement returns [AlterRoleStatement stmt] : K_ALTER K_ROLE name= userOrRoleName ( K_WITH roleOptions[opts] )? ;
	public final AlterRoleStatement alterRoleStatement() throws RecognitionException {
		AlterRoleStatement stmt = null;


		RoleName name =null;


		        RoleOptions opts = new RoleOptions();
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1053:5: ( K_ALTER K_ROLE name= userOrRoleName ( K_WITH roleOptions[opts] )? )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1053:7: K_ALTER K_ROLE name= userOrRoleName ( K_WITH roleOptions[opts] )?
			{
			match(input,K_ALTER,FOLLOW_K_ALTER_in_alterRoleStatement6337); 
			match(input,K_ROLE,FOLLOW_K_ROLE_in_alterRoleStatement6339); 
			pushFollow(FOLLOW_userOrRoleName_in_alterRoleStatement6343);
			name=userOrRoleName();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1054:7: ( K_WITH roleOptions[opts] )?
			int alt122=2;
			int LA122_0 = input.LA(1);
			if ( (LA122_0==K_WITH) ) {
				alt122=1;
			}
			switch (alt122) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1054:9: K_WITH roleOptions[opts]
					{
					match(input,K_WITH,FOLLOW_K_WITH_in_alterRoleStatement6353); 
					pushFollow(FOLLOW_roleOptions_in_alterRoleStatement6355);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1061:1: dropRoleStatement returns [DropRoleStatement stmt] : K_DROP K_ROLE ( K_IF K_EXISTS )? name= userOrRoleName ;
	public final DropRoleStatement dropRoleStatement() throws RecognitionException {
		DropRoleStatement stmt = null;


		RoleName name =null;


		        boolean ifExists = false;
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1065:5: ( K_DROP K_ROLE ( K_IF K_EXISTS )? name= userOrRoleName )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1065:7: K_DROP K_ROLE ( K_IF K_EXISTS )? name= userOrRoleName
			{
			match(input,K_DROP,FOLLOW_K_DROP_in_dropRoleStatement6399); 
			match(input,K_ROLE,FOLLOW_K_ROLE_in_dropRoleStatement6401); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1065:21: ( K_IF K_EXISTS )?
			int alt123=2;
			int LA123_0 = input.LA(1);
			if ( (LA123_0==K_IF) ) {
				alt123=1;
			}
			switch (alt123) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1065:22: K_IF K_EXISTS
					{
					match(input,K_IF,FOLLOW_K_IF_in_dropRoleStatement6404); 
					match(input,K_EXISTS,FOLLOW_K_EXISTS_in_dropRoleStatement6406); 
					 ifExists = true; 
					}
					break;

			}

			pushFollow(FOLLOW_userOrRoleName_in_dropRoleStatement6414);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1072:1: listRolesStatement returns [ListRolesStatement stmt] : K_LIST K_ROLES ( K_OF roleName[grantee] )? ( K_NORECURSIVE )? ;
	public final ListRolesStatement listRolesStatement() throws RecognitionException {
		ListRolesStatement stmt = null;



		        boolean recursive = true;
		        RoleName grantee = new RoleName();
		    
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1077:5: ( K_LIST K_ROLES ( K_OF roleName[grantee] )? ( K_NORECURSIVE )? )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1077:7: K_LIST K_ROLES ( K_OF roleName[grantee] )? ( K_NORECURSIVE )?
			{
			match(input,K_LIST,FOLLOW_K_LIST_in_listRolesStatement6454); 
			match(input,K_ROLES,FOLLOW_K_ROLES_in_listRolesStatement6456); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1078:7: ( K_OF roleName[grantee] )?
			int alt124=2;
			int LA124_0 = input.LA(1);
			if ( (LA124_0==K_OF) ) {
				alt124=1;
			}
			switch (alt124) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1078:9: K_OF roleName[grantee]
					{
					match(input,K_OF,FOLLOW_K_OF_in_listRolesStatement6466); 
					pushFollow(FOLLOW_roleName_in_listRolesStatement6468);
					roleName(grantee);
					state._fsp--;

					}
					break;

			}

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1079:7: ( K_NORECURSIVE )?
			int alt125=2;
			int LA125_0 = input.LA(1);
			if ( (LA125_0==K_NORECURSIVE) ) {
				alt125=1;
			}
			switch (alt125) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1079:9: K_NORECURSIVE
					{
					match(input,K_NORECURSIVE,FOLLOW_K_NORECURSIVE_in_listRolesStatement6481); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1083:1: roleOptions[RoleOptions opts] : roleOption[opts] ( K_AND roleOption[opts] )* ;
	public final void roleOptions(RoleOptions opts) throws RecognitionException {
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1084:5: ( roleOption[opts] ( K_AND roleOption[opts] )* )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1084:7: roleOption[opts] ( K_AND roleOption[opts] )*
			{
			pushFollow(FOLLOW_roleOption_in_roleOptions6512);
			roleOption(opts);
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1084:24: ( K_AND roleOption[opts] )*
			loop126:
			while (true) {
				int alt126=2;
				int LA126_0 = input.LA(1);
				if ( (LA126_0==K_AND) ) {
					alt126=1;
				}

				switch (alt126) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1084:25: K_AND roleOption[opts]
					{
					match(input,K_AND,FOLLOW_K_AND_in_roleOptions6516); 
					pushFollow(FOLLOW_roleOption_in_roleOptions6518);
					roleOption(opts);
					state._fsp--;

					}
					break;

				default :
					break loop126;
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1087:1: roleOption[RoleOptions opts] : ( K_PASSWORD '=' v= STRING_LITERAL | K_OPTIONS '=' m= mapLiteral | K_SUPERUSER '=' b= BOOLEAN | K_LOGIN '=' b= BOOLEAN );
	public final void roleOption(RoleOptions opts) throws RecognitionException {
		Token v=null;
		Token b=null;
		Maps.Literal m =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1088:5: ( K_PASSWORD '=' v= STRING_LITERAL | K_OPTIONS '=' m= mapLiteral | K_SUPERUSER '=' b= BOOLEAN | K_LOGIN '=' b= BOOLEAN )
			int alt127=4;
			switch ( input.LA(1) ) {
			case K_PASSWORD:
				{
				alt127=1;
				}
				break;
			case K_OPTIONS:
				{
				alt127=2;
				}
				break;
			case K_SUPERUSER:
				{
				alt127=3;
				}
				break;
			case K_LOGIN:
				{
				alt127=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 127, 0, input);
				throw nvae;
			}
			switch (alt127) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1088:8: K_PASSWORD '=' v= STRING_LITERAL
					{
					match(input,K_PASSWORD,FOLLOW_K_PASSWORD_in_roleOption6540); 
					match(input,178,FOLLOW_178_in_roleOption6542); 
					v=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_roleOption6546); 
					 opts.setOption(IRoleManager.Option.PASSWORD, (v!=null?v.getText():null)); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1089:8: K_OPTIONS '=' m= mapLiteral
					{
					match(input,K_OPTIONS,FOLLOW_K_OPTIONS_in_roleOption6557); 
					match(input,178,FOLLOW_178_in_roleOption6559); 
					pushFollow(FOLLOW_mapLiteral_in_roleOption6563);
					m=mapLiteral();
					state._fsp--;

					 opts.setOption(IRoleManager.Option.OPTIONS, convertPropertyMap(m)); 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1090:8: K_SUPERUSER '=' b= BOOLEAN
					{
					match(input,K_SUPERUSER,FOLLOW_K_SUPERUSER_in_roleOption6574); 
					match(input,178,FOLLOW_178_in_roleOption6576); 
					b=(Token)match(input,BOOLEAN,FOLLOW_BOOLEAN_in_roleOption6580); 
					 opts.setOption(IRoleManager.Option.SUPERUSER, Boolean.valueOf((b!=null?b.getText():null))); 
					}
					break;
				case 4 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1091:8: K_LOGIN '=' b= BOOLEAN
					{
					match(input,K_LOGIN,FOLLOW_K_LOGIN_in_roleOption6591); 
					match(input,178,FOLLOW_178_in_roleOption6593); 
					b=(Token)match(input,BOOLEAN,FOLLOW_BOOLEAN_in_roleOption6597); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1095:1: userPassword[RoleOptions opts] : K_PASSWORD v= STRING_LITERAL ;
	public final void userPassword(RoleOptions opts) throws RecognitionException {
		Token v=null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1096:5: ( K_PASSWORD v= STRING_LITERAL )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1096:8: K_PASSWORD v= STRING_LITERAL
			{
			match(input,K_PASSWORD,FOLLOW_K_PASSWORD_in_userPassword6619); 
			v=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_userPassword6623); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1104:1: cident returns [ColumnIdentifier.Raw id] : (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword );
	public final ColumnIdentifier.Raw cident() throws RecognitionException {
		ColumnIdentifier.Raw id = null;


		Token t=null;
		String k =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1105:5: (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword )
			int alt128=3;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				alt128=1;
				}
				break;
			case QUOTED_NAME:
				{
				alt128=2;
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
				alt128=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 128, 0, input);
				throw nvae;
			}
			switch (alt128) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1105:7: t= IDENT
					{
					t=(Token)match(input,IDENT,FOLLOW_IDENT_in_cident6654); 
					 id = new ColumnIdentifier.Raw((t!=null?t.getText():null), false); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1106:7: t= QUOTED_NAME
					{
					t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_cident6679); 
					 id = new ColumnIdentifier.Raw((t!=null?t.getText():null), true); 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1107:7: k= unreserved_keyword
					{
					pushFollow(FOLLOW_unreserved_keyword_in_cident6698);
					k=unreserved_keyword();
					state._fsp--;

					 id = new ColumnIdentifier.Raw(k, false); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1111:1: ident returns [ColumnIdentifier id] : (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword );
	public final ColumnIdentifier ident() throws RecognitionException {
		ColumnIdentifier id = null;


		Token t=null;
		String k =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1112:5: (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword )
			int alt129=3;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				alt129=1;
				}
				break;
			case QUOTED_NAME:
				{
				alt129=2;
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
				alt129=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 129, 0, input);
				throw nvae;
			}
			switch (alt129) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1112:7: t= IDENT
					{
					t=(Token)match(input,IDENT,FOLLOW_IDENT_in_ident6724); 
					 id = new ColumnIdentifier((t!=null?t.getText():null), false); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1113:7: t= QUOTED_NAME
					{
					t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_ident6749); 
					 id = new ColumnIdentifier((t!=null?t.getText():null), true); 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1114:7: k= unreserved_keyword
					{
					pushFollow(FOLLOW_unreserved_keyword_in_ident6768);
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
	// $ANTLR end "ident"



	// $ANTLR start "keyspaceName"
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1118:1: keyspaceName returns [String id] : ksName[name] ;
	public final String keyspaceName() throws RecognitionException {
		String id = null;


		 CFName name = new CFName(); 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1120:5: ( ksName[name] )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1120:7: ksName[name]
			{
			pushFollow(FOLLOW_ksName_in_keyspaceName6801);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1123:1: indexName returns [IndexName name] : ( ksName[name] '.' )? idxName[name] ;
	public final IndexName indexName() throws RecognitionException {
		IndexName name = null;


		 name = new IndexName(); 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1125:5: ( ( ksName[name] '.' )? idxName[name] )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1125:7: ( ksName[name] '.' )? idxName[name]
			{
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1125:7: ( ksName[name] '.' )?
			int alt130=2;
			alt130 = dfa130.predict(input);
			switch (alt130) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1125:8: ksName[name] '.'
					{
					pushFollow(FOLLOW_ksName_in_indexName6835);
					ksName(name);
					state._fsp--;

					match(input,173,FOLLOW_173_in_indexName6838); 
					}
					break;

			}

			pushFollow(FOLLOW_idxName_in_indexName6842);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1128:1: columnFamilyName returns [CFName name] : ( ksName[name] '.' )? cfName[name] ;
	public final CFName columnFamilyName() throws RecognitionException {
		CFName name = null;


		 name = new CFName(); 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1130:5: ( ( ksName[name] '.' )? cfName[name] )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1130:7: ( ksName[name] '.' )? cfName[name]
			{
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1130:7: ( ksName[name] '.' )?
			int alt131=2;
			alt131 = dfa131.predict(input);
			switch (alt131) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1130:8: ksName[name] '.'
					{
					pushFollow(FOLLOW_ksName_in_columnFamilyName6874);
					ksName(name);
					state._fsp--;

					match(input,173,FOLLOW_173_in_columnFamilyName6877); 
					}
					break;

			}

			pushFollow(FOLLOW_cfName_in_columnFamilyName6881);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1133:1: userTypeName returns [UTName name] : (ks= ident '.' )? ut= non_type_ident ;
	public final UTName userTypeName() throws RecognitionException {
		UTName name = null;


		ColumnIdentifier ks =null;
		ColumnIdentifier ut =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1134:5: ( (ks= ident '.' )? ut= non_type_ident )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1134:7: (ks= ident '.' )? ut= non_type_ident
			{
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1134:7: (ks= ident '.' )?
			int alt132=2;
			switch ( input.LA(1) ) {
				case IDENT:
					{
					int LA132_1 = input.LA(2);
					if ( (LA132_1==173) ) {
						alt132=1;
					}
					}
					break;
				case QUOTED_NAME:
					{
					int LA132_2 = input.LA(2);
					if ( (LA132_2==173) ) {
						alt132=1;
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
					int LA132_3 = input.LA(2);
					if ( (LA132_3==173) ) {
						alt132=1;
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
					alt132=1;
					}
					break;
				case K_KEY:
					{
					int LA132_5 = input.LA(2);
					if ( (LA132_5==173) ) {
						alt132=1;
					}
					}
					break;
			}
			switch (alt132) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1134:8: ks= ident '.'
					{
					pushFollow(FOLLOW_ident_in_userTypeName6906);
					ks=ident();
					state._fsp--;

					match(input,173,FOLLOW_173_in_userTypeName6908); 
					}
					break;

			}

			pushFollow(FOLLOW_non_type_ident_in_userTypeName6914);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1137:1: userOrRoleName returns [RoleName name] : roleName[name] ;
	public final RoleName userOrRoleName() throws RecognitionException {
		RoleName name = null;


		 name = new RoleName(); 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1139:5: ( roleName[name] )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1139:7: roleName[name]
			{
			pushFollow(FOLLOW_roleName_in_userOrRoleName6946);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1142:1: ksName[KeyspaceElementName name] : (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword | QMARK );
	public final void ksName(KeyspaceElementName name) throws RecognitionException {
		Token t=null;
		String k =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1143:5: (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword | QMARK )
			int alt133=4;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				alt133=1;
				}
				break;
			case QUOTED_NAME:
				{
				alt133=2;
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
				alt133=3;
				}
				break;
			case QMARK:
				{
				alt133=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 133, 0, input);
				throw nvae;
			}
			switch (alt133) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1143:7: t= IDENT
					{
					t=(Token)match(input,IDENT,FOLLOW_IDENT_in_ksName6969); 
					 name.setKeyspace((t!=null?t.getText():null), false);
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1144:7: t= QUOTED_NAME
					{
					t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_ksName6994); 
					 name.setKeyspace((t!=null?t.getText():null), true);
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1145:7: k= unreserved_keyword
					{
					pushFollow(FOLLOW_unreserved_keyword_in_ksName7013);
					k=unreserved_keyword();
					state._fsp--;

					 name.setKeyspace(k, false);
					}
					break;
				case 4 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1146:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_ksName7023); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1149:1: cfName[CFName name] : (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword | QMARK );
	public final void cfName(CFName name) throws RecognitionException {
		Token t=null;
		String k =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1150:5: (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword | QMARK )
			int alt134=4;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				alt134=1;
				}
				break;
			case QUOTED_NAME:
				{
				alt134=2;
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
				alt134=3;
				}
				break;
			case QMARK:
				{
				alt134=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 134, 0, input);
				throw nvae;
			}
			switch (alt134) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1150:7: t= IDENT
					{
					t=(Token)match(input,IDENT,FOLLOW_IDENT_in_cfName7045); 
					 name.setColumnFamily((t!=null?t.getText():null), false); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1151:7: t= QUOTED_NAME
					{
					t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_cfName7070); 
					 name.setColumnFamily((t!=null?t.getText():null), true); 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1152:7: k= unreserved_keyword
					{
					pushFollow(FOLLOW_unreserved_keyword_in_cfName7089);
					k=unreserved_keyword();
					state._fsp--;

					 name.setColumnFamily(k, false); 
					}
					break;
				case 4 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1153:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_cfName7099); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1156:1: idxName[IndexName name] : (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword | QMARK );
	public final void idxName(IndexName name) throws RecognitionException {
		Token t=null;
		String k =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1157:5: (t= IDENT |t= QUOTED_NAME |k= unreserved_keyword | QMARK )
			int alt135=4;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				alt135=1;
				}
				break;
			case QUOTED_NAME:
				{
				alt135=2;
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
				alt135=3;
				}
				break;
			case QMARK:
				{
				alt135=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 135, 0, input);
				throw nvae;
			}
			switch (alt135) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1157:7: t= IDENT
					{
					t=(Token)match(input,IDENT,FOLLOW_IDENT_in_idxName7121); 
					 name.setIndex((t!=null?t.getText():null), false); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1158:7: t= QUOTED_NAME
					{
					t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_idxName7146); 
					 name.setIndex((t!=null?t.getText():null), true);
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1159:7: k= unreserved_keyword
					{
					pushFollow(FOLLOW_unreserved_keyword_in_idxName7165);
					k=unreserved_keyword();
					state._fsp--;

					 name.setIndex(k, false); 
					}
					break;
				case 4 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1160:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_idxName7175); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1163:1: roleName[RoleName name] : (t= IDENT |s= STRING_LITERAL |t= QUOTED_NAME |k= unreserved_keyword | QMARK );
	public final void roleName(RoleName name) throws RecognitionException {
		Token t=null;
		Token s=null;
		String k =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1164:5: (t= IDENT |s= STRING_LITERAL |t= QUOTED_NAME |k= unreserved_keyword | QMARK )
			int alt136=5;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				alt136=1;
				}
				break;
			case STRING_LITERAL:
				{
				alt136=2;
				}
				break;
			case QUOTED_NAME:
				{
				alt136=3;
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
				alt136=4;
				}
				break;
			case QMARK:
				{
				alt136=5;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 136, 0, input);
				throw nvae;
			}
			switch (alt136) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1164:7: t= IDENT
					{
					t=(Token)match(input,IDENT,FOLLOW_IDENT_in_roleName7197); 
					 name.setName((t!=null?t.getText():null), false); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1165:7: s= STRING_LITERAL
					{
					s=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_roleName7222); 
					 name.setName((s!=null?s.getText():null), true); 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1166:7: t= QUOTED_NAME
					{
					t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_roleName7238); 
					 name.setName((t!=null?t.getText():null), true); 
					}
					break;
				case 4 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1167:7: k= unreserved_keyword
					{
					pushFollow(FOLLOW_unreserved_keyword_in_roleName7257);
					k=unreserved_keyword();
					state._fsp--;

					 name.setName(k, false); 
					}
					break;
				case 5 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1168:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_roleName7267); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1171:1: constant returns [Constants.Literal constant] : (t= STRING_LITERAL |t= INTEGER |t= FLOAT |t= BOOLEAN |t= UUID |t= HEXNUMBER | ( '-' )? t= ( K_NAN | K_INFINITY ) );
	public final Constants.Literal constant() throws RecognitionException {
		Constants.Literal constant = null;


		Token t=null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1172:5: (t= STRING_LITERAL |t= INTEGER |t= FLOAT |t= BOOLEAN |t= UUID |t= HEXNUMBER | ( '-' )? t= ( K_NAN | K_INFINITY ) )
			int alt138=7;
			switch ( input.LA(1) ) {
			case STRING_LITERAL:
				{
				alt138=1;
				}
				break;
			case INTEGER:
				{
				alt138=2;
				}
				break;
			case FLOAT:
				{
				alt138=3;
				}
				break;
			case BOOLEAN:
				{
				alt138=4;
				}
				break;
			case UUID:
				{
				alt138=5;
				}
				break;
			case HEXNUMBER:
				{
				alt138=6;
				}
				break;
			case K_INFINITY:
			case K_NAN:
			case 172:
				{
				alt138=7;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 138, 0, input);
				throw nvae;
			}
			switch (alt138) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1172:7: t= STRING_LITERAL
					{
					t=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_constant7292); 
					 constant = Constants.Literal.string((t!=null?t.getText():null)); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1173:7: t= INTEGER
					{
					t=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_constant7304); 
					 constant = Constants.Literal.integer((t!=null?t.getText():null)); 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1174:7: t= FLOAT
					{
					t=(Token)match(input,FLOAT,FOLLOW_FLOAT_in_constant7323); 
					 constant = Constants.Literal.floatingPoint((t!=null?t.getText():null)); 
					}
					break;
				case 4 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1175:7: t= BOOLEAN
					{
					t=(Token)match(input,BOOLEAN,FOLLOW_BOOLEAN_in_constant7344); 
					 constant = Constants.Literal.bool((t!=null?t.getText():null)); 
					}
					break;
				case 5 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1176:7: t= UUID
					{
					t=(Token)match(input,UUID,FOLLOW_UUID_in_constant7363); 
					 constant = Constants.Literal.uuid((t!=null?t.getText():null)); 
					}
					break;
				case 6 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1177:7: t= HEXNUMBER
					{
					t=(Token)match(input,HEXNUMBER,FOLLOW_HEXNUMBER_in_constant7385); 
					 constant = Constants.Literal.hex((t!=null?t.getText():null)); 
					}
					break;
				case 7 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1178:7: ( '-' )? t= ( K_NAN | K_INFINITY )
					{
					 String sign=""; 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1178:27: ( '-' )?
					int alt137=2;
					int LA137_0 = input.LA(1);
					if ( (LA137_0==172) ) {
						alt137=1;
					}
					switch (alt137) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1178:28: '-'
							{
							match(input,172,FOLLOW_172_in_constant7403); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1181:1: mapLiteral returns [Maps.Literal map] : '{' (k1= term ':' v1= term ( ',' kn= term ':' vn= term )* )? '}' ;
	public final Maps.Literal mapLiteral() throws RecognitionException {
		Maps.Literal map = null;


		Term.Raw k1 =null;
		Term.Raw v1 =null;
		Term.Raw kn =null;
		Term.Raw vn =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1182:5: ( '{' (k1= term ':' v1= term ( ',' kn= term ':' vn= term )* )? '}' )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1182:7: '{' (k1= term ':' v1= term ( ',' kn= term ':' vn= term )* )? '}'
			{
			match(input,184,FOLLOW_184_in_mapLiteral7441); 
			 List<Pair<Term.Raw, Term.Raw>> m = new ArrayList<Pair<Term.Raw, Term.Raw>>(); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1183:11: (k1= term ':' v1= term ( ',' kn= term ':' vn= term )* )?
			int alt140=2;
			int LA140_0 = input.LA(1);
			if ( (LA140_0==BOOLEAN||LA140_0==FLOAT||LA140_0==HEXNUMBER||(LA140_0 >= IDENT && LA140_0 <= INTEGER)||(LA140_0 >= K_AGGREGATE && LA140_0 <= K_ALL)||LA140_0==K_AS||LA140_0==K_ASCII||(LA140_0 >= K_BIGINT && LA140_0 <= K_BOOLEAN)||(LA140_0 >= K_CALLED && LA140_0 <= K_CLUSTERING)||(LA140_0 >= K_COMPACT && LA140_0 <= K_COUNTER)||(LA140_0 >= K_CUSTOM && LA140_0 <= K_DECIMAL)||(LA140_0 >= K_DISTINCT && LA140_0 <= K_DOUBLE)||(LA140_0 >= K_EXISTS && LA140_0 <= K_FLOAT)||LA140_0==K_FROZEN||(LA140_0 >= K_FUNCTION && LA140_0 <= K_FUNCTIONS)||(LA140_0 >= K_INET && LA140_0 <= K_INPUT)||LA140_0==K_INT||(LA140_0 >= K_JSON && LA140_0 <= K_KEYS)||(LA140_0 >= K_KEYSPACES && LA140_0 <= K_LANGUAGE)||(LA140_0 >= K_LIST && LA140_0 <= K_MAP)||(LA140_0 >= K_NAN && LA140_0 <= K_NOLOGIN)||LA140_0==K_NOSUPERUSER||LA140_0==K_NULL||LA140_0==K_OPTIONS||(LA140_0 >= K_PASSWORD && LA140_0 <= K_PERMISSIONS)||LA140_0==K_RETURNS||(LA140_0 >= K_ROLE && LA140_0 <= K_ROLES)||(LA140_0 >= K_SFUNC && LA140_0 <= K_TINYINT)||(LA140_0 >= K_TOKEN && LA140_0 <= K_TRIGGER)||(LA140_0 >= K_TTL && LA140_0 <= K_TYPE)||(LA140_0 >= K_USER && LA140_0 <= K_USERS)||(LA140_0 >= K_UUID && LA140_0 <= K_VARINT)||LA140_0==K_WRITETIME||(LA140_0 >= QMARK && LA140_0 <= QUOTED_NAME)||LA140_0==STRING_LITERAL||LA140_0==UUID||LA140_0==168||LA140_0==172||LA140_0==174||LA140_0==181||LA140_0==184) ) {
				alt140=1;
			}
			switch (alt140) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1183:13: k1= term ':' v1= term ( ',' kn= term ':' vn= term )*
					{
					pushFollow(FOLLOW_term_in_mapLiteral7459);
					k1=term();
					state._fsp--;

					match(input,174,FOLLOW_174_in_mapLiteral7461); 
					pushFollow(FOLLOW_term_in_mapLiteral7465);
					v1=term();
					state._fsp--;

					 m.add(Pair.create(k1, v1)); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1183:65: ( ',' kn= term ':' vn= term )*
					loop139:
					while (true) {
						int alt139=2;
						int LA139_0 = input.LA(1);
						if ( (LA139_0==171) ) {
							alt139=1;
						}

						switch (alt139) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1183:67: ',' kn= term ':' vn= term
							{
							match(input,171,FOLLOW_171_in_mapLiteral7471); 
							pushFollow(FOLLOW_term_in_mapLiteral7475);
							kn=term();
							state._fsp--;

							match(input,174,FOLLOW_174_in_mapLiteral7477); 
							pushFollow(FOLLOW_term_in_mapLiteral7481);
							vn=term();
							state._fsp--;

							 m.add(Pair.create(kn, vn)); 
							}
							break;

						default :
							break loop139;
						}
					}

					}
					break;

			}

			match(input,185,FOLLOW_185_in_mapLiteral7497); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1187:1: setOrMapLiteral[Term.Raw t] returns [Term.Raw value] : ( ':' v= term ( ',' kn= term ':' vn= term )* | ( ',' tn= term )* );
	public final Term.Raw setOrMapLiteral(Term.Raw t) throws RecognitionException {
		Term.Raw value = null;


		Term.Raw v =null;
		Term.Raw kn =null;
		Term.Raw vn =null;
		Term.Raw tn =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1188:5: ( ':' v= term ( ',' kn= term ':' vn= term )* | ( ',' tn= term )* )
			int alt143=2;
			int LA143_0 = input.LA(1);
			if ( (LA143_0==174) ) {
				alt143=1;
			}
			else if ( (LA143_0==171||LA143_0==185) ) {
				alt143=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 143, 0, input);
				throw nvae;
			}

			switch (alt143) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1188:7: ':' v= term ( ',' kn= term ':' vn= term )*
					{
					match(input,174,FOLLOW_174_in_setOrMapLiteral7521); 
					pushFollow(FOLLOW_term_in_setOrMapLiteral7525);
					v=term();
					state._fsp--;

					 List<Pair<Term.Raw, Term.Raw>> m = new ArrayList<Pair<Term.Raw, Term.Raw>>(); m.add(Pair.create(t, v)); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1189:11: ( ',' kn= term ':' vn= term )*
					loop141:
					while (true) {
						int alt141=2;
						int LA141_0 = input.LA(1);
						if ( (LA141_0==171) ) {
							alt141=1;
						}

						switch (alt141) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1189:13: ',' kn= term ':' vn= term
							{
							match(input,171,FOLLOW_171_in_setOrMapLiteral7541); 
							pushFollow(FOLLOW_term_in_setOrMapLiteral7545);
							kn=term();
							state._fsp--;

							match(input,174,FOLLOW_174_in_setOrMapLiteral7547); 
							pushFollow(FOLLOW_term_in_setOrMapLiteral7551);
							vn=term();
							state._fsp--;

							 m.add(Pair.create(kn, vn)); 
							}
							break;

						default :
							break loop141;
						}
					}

					 value = new Maps.Literal(m); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1191:7: ( ',' tn= term )*
					{
					 List<Term.Raw> s = new ArrayList<Term.Raw>(); s.add(t); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1192:11: ( ',' tn= term )*
					loop142:
					while (true) {
						int alt142=2;
						int LA142_0 = input.LA(1);
						if ( (LA142_0==171) ) {
							alt142=1;
						}

						switch (alt142) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1192:13: ',' tn= term
							{
							match(input,171,FOLLOW_171_in_setOrMapLiteral7586); 
							pushFollow(FOLLOW_term_in_setOrMapLiteral7590);
							tn=term();
							state._fsp--;

							 s.add(tn); 
							}
							break;

						default :
							break loop142;
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1196:1: collectionLiteral returns [Term.Raw value] : ( '[' (t1= term ( ',' tn= term )* )? ']' | '{' t= term v= setOrMapLiteral[t] '}' | '{' '}' );
	public final Term.Raw collectionLiteral() throws RecognitionException {
		Term.Raw value = null;


		Term.Raw t1 =null;
		Term.Raw tn =null;
		Term.Raw t =null;
		Term.Raw v =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1197:5: ( '[' (t1= term ( ',' tn= term )* )? ']' | '{' t= term v= setOrMapLiteral[t] '}' | '{' '}' )
			int alt146=3;
			int LA146_0 = input.LA(1);
			if ( (LA146_0==181) ) {
				alt146=1;
			}
			else if ( (LA146_0==184) ) {
				int LA146_2 = input.LA(2);
				if ( (LA146_2==185) ) {
					alt146=3;
				}
				else if ( (LA146_2==BOOLEAN||LA146_2==FLOAT||LA146_2==HEXNUMBER||(LA146_2 >= IDENT && LA146_2 <= INTEGER)||(LA146_2 >= K_AGGREGATE && LA146_2 <= K_ALL)||LA146_2==K_AS||LA146_2==K_ASCII||(LA146_2 >= K_BIGINT && LA146_2 <= K_BOOLEAN)||(LA146_2 >= K_CALLED && LA146_2 <= K_CLUSTERING)||(LA146_2 >= K_COMPACT && LA146_2 <= K_COUNTER)||(LA146_2 >= K_CUSTOM && LA146_2 <= K_DECIMAL)||(LA146_2 >= K_DISTINCT && LA146_2 <= K_DOUBLE)||(LA146_2 >= K_EXISTS && LA146_2 <= K_FLOAT)||LA146_2==K_FROZEN||(LA146_2 >= K_FUNCTION && LA146_2 <= K_FUNCTIONS)||(LA146_2 >= K_INET && LA146_2 <= K_INPUT)||LA146_2==K_INT||(LA146_2 >= K_JSON && LA146_2 <= K_KEYS)||(LA146_2 >= K_KEYSPACES && LA146_2 <= K_LANGUAGE)||(LA146_2 >= K_LIST && LA146_2 <= K_MAP)||(LA146_2 >= K_NAN && LA146_2 <= K_NOLOGIN)||LA146_2==K_NOSUPERUSER||LA146_2==K_NULL||LA146_2==K_OPTIONS||(LA146_2 >= K_PASSWORD && LA146_2 <= K_PERMISSIONS)||LA146_2==K_RETURNS||(LA146_2 >= K_ROLE && LA146_2 <= K_ROLES)||(LA146_2 >= K_SFUNC && LA146_2 <= K_TINYINT)||(LA146_2 >= K_TOKEN && LA146_2 <= K_TRIGGER)||(LA146_2 >= K_TTL && LA146_2 <= K_TYPE)||(LA146_2 >= K_USER && LA146_2 <= K_USERS)||(LA146_2 >= K_UUID && LA146_2 <= K_VARINT)||LA146_2==K_WRITETIME||(LA146_2 >= QMARK && LA146_2 <= QUOTED_NAME)||LA146_2==STRING_LITERAL||LA146_2==UUID||LA146_2==168||LA146_2==172||LA146_2==174||LA146_2==181||LA146_2==184) ) {
					alt146=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 146, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 146, 0, input);
				throw nvae;
			}

			switch (alt146) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1197:7: '[' (t1= term ( ',' tn= term )* )? ']'
					{
					match(input,181,FOLLOW_181_in_collectionLiteral7624); 
					 List<Term.Raw> l = new ArrayList<Term.Raw>(); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1198:11: (t1= term ( ',' tn= term )* )?
					int alt145=2;
					int LA145_0 = input.LA(1);
					if ( (LA145_0==BOOLEAN||LA145_0==FLOAT||LA145_0==HEXNUMBER||(LA145_0 >= IDENT && LA145_0 <= INTEGER)||(LA145_0 >= K_AGGREGATE && LA145_0 <= K_ALL)||LA145_0==K_AS||LA145_0==K_ASCII||(LA145_0 >= K_BIGINT && LA145_0 <= K_BOOLEAN)||(LA145_0 >= K_CALLED && LA145_0 <= K_CLUSTERING)||(LA145_0 >= K_COMPACT && LA145_0 <= K_COUNTER)||(LA145_0 >= K_CUSTOM && LA145_0 <= K_DECIMAL)||(LA145_0 >= K_DISTINCT && LA145_0 <= K_DOUBLE)||(LA145_0 >= K_EXISTS && LA145_0 <= K_FLOAT)||LA145_0==K_FROZEN||(LA145_0 >= K_FUNCTION && LA145_0 <= K_FUNCTIONS)||(LA145_0 >= K_INET && LA145_0 <= K_INPUT)||LA145_0==K_INT||(LA145_0 >= K_JSON && LA145_0 <= K_KEYS)||(LA145_0 >= K_KEYSPACES && LA145_0 <= K_LANGUAGE)||(LA145_0 >= K_LIST && LA145_0 <= K_MAP)||(LA145_0 >= K_NAN && LA145_0 <= K_NOLOGIN)||LA145_0==K_NOSUPERUSER||LA145_0==K_NULL||LA145_0==K_OPTIONS||(LA145_0 >= K_PASSWORD && LA145_0 <= K_PERMISSIONS)||LA145_0==K_RETURNS||(LA145_0 >= K_ROLE && LA145_0 <= K_ROLES)||(LA145_0 >= K_SFUNC && LA145_0 <= K_TINYINT)||(LA145_0 >= K_TOKEN && LA145_0 <= K_TRIGGER)||(LA145_0 >= K_TTL && LA145_0 <= K_TYPE)||(LA145_0 >= K_USER && LA145_0 <= K_USERS)||(LA145_0 >= K_UUID && LA145_0 <= K_VARINT)||LA145_0==K_WRITETIME||(LA145_0 >= QMARK && LA145_0 <= QUOTED_NAME)||LA145_0==STRING_LITERAL||LA145_0==UUID||LA145_0==168||LA145_0==172||LA145_0==174||LA145_0==181||LA145_0==184) ) {
						alt145=1;
					}
					switch (alt145) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1198:13: t1= term ( ',' tn= term )*
							{
							pushFollow(FOLLOW_term_in_collectionLiteral7642);
							t1=term();
							state._fsp--;

							 l.add(t1); 
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1198:36: ( ',' tn= term )*
							loop144:
							while (true) {
								int alt144=2;
								int LA144_0 = input.LA(1);
								if ( (LA144_0==171) ) {
									alt144=1;
								}

								switch (alt144) {
								case 1 :
									// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1198:38: ',' tn= term
									{
									match(input,171,FOLLOW_171_in_collectionLiteral7648); 
									pushFollow(FOLLOW_term_in_collectionLiteral7652);
									tn=term();
									state._fsp--;

									 l.add(tn); 
									}
									break;

								default :
									break loop144;
								}
							}

							}
							break;

					}

					match(input,183,FOLLOW_183_in_collectionLiteral7668); 
					 value = new Lists.Literal(l); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1200:7: '{' t= term v= setOrMapLiteral[t] '}'
					{
					match(input,184,FOLLOW_184_in_collectionLiteral7678); 
					pushFollow(FOLLOW_term_in_collectionLiteral7682);
					t=term();
					state._fsp--;

					pushFollow(FOLLOW_setOrMapLiteral_in_collectionLiteral7686);
					v=setOrMapLiteral(t);
					state._fsp--;

					 value = v; 
					match(input,185,FOLLOW_185_in_collectionLiteral7691); 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1203:7: '{' '}'
					{
					match(input,184,FOLLOW_184_in_collectionLiteral7709); 
					match(input,185,FOLLOW_185_in_collectionLiteral7711); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1206:1: usertypeLiteral returns [UserTypes.Literal ut] : '{' k1= ident ':' v1= term ( ',' kn= ident ':' vn= term )* '}' ;
	public final UserTypes.Literal usertypeLiteral() throws RecognitionException {
		UserTypes.Literal ut = null;


		ColumnIdentifier k1 =null;
		Term.Raw v1 =null;
		ColumnIdentifier kn =null;
		Term.Raw vn =null;

		 Map<ColumnIdentifier, Term.Raw> m = new HashMap<ColumnIdentifier, Term.Raw>(); 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1210:5: ( '{' k1= ident ':' v1= term ( ',' kn= ident ':' vn= term )* '}' )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1210:7: '{' k1= ident ':' v1= term ( ',' kn= ident ':' vn= term )* '}'
			{
			match(input,184,FOLLOW_184_in_usertypeLiteral7755); 
			pushFollow(FOLLOW_ident_in_usertypeLiteral7759);
			k1=ident();
			state._fsp--;

			match(input,174,FOLLOW_174_in_usertypeLiteral7761); 
			pushFollow(FOLLOW_term_in_usertypeLiteral7765);
			v1=term();
			state._fsp--;

			 m.put(k1, v1); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1210:51: ( ',' kn= ident ':' vn= term )*
			loop147:
			while (true) {
				int alt147=2;
				int LA147_0 = input.LA(1);
				if ( (LA147_0==171) ) {
					alt147=1;
				}

				switch (alt147) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1210:53: ',' kn= ident ':' vn= term
					{
					match(input,171,FOLLOW_171_in_usertypeLiteral7771); 
					pushFollow(FOLLOW_ident_in_usertypeLiteral7775);
					kn=ident();
					state._fsp--;

					match(input,174,FOLLOW_174_in_usertypeLiteral7777); 
					pushFollow(FOLLOW_term_in_usertypeLiteral7781);
					vn=term();
					state._fsp--;

					 m.put(kn, vn); 
					}
					break;

				default :
					break loop147;
				}
			}

			match(input,185,FOLLOW_185_in_usertypeLiteral7788); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1213:1: tupleLiteral returns [Tuples.Literal tt] : '(' t1= term ( ',' tn= term )* ')' ;
	public final Tuples.Literal tupleLiteral() throws RecognitionException {
		Tuples.Literal tt = null;


		Term.Raw t1 =null;
		Term.Raw tn =null;

		 List<Term.Raw> l = new ArrayList<Term.Raw>(); 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1216:5: ( '(' t1= term ( ',' tn= term )* ')' )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1216:7: '(' t1= term ( ',' tn= term )* ')'
			{
			match(input,168,FOLLOW_168_in_tupleLiteral7825); 
			pushFollow(FOLLOW_term_in_tupleLiteral7829);
			t1=term();
			state._fsp--;

			 l.add(t1); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1216:34: ( ',' tn= term )*
			loop148:
			while (true) {
				int alt148=2;
				int LA148_0 = input.LA(1);
				if ( (LA148_0==171) ) {
					alt148=1;
				}

				switch (alt148) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1216:36: ',' tn= term
					{
					match(input,171,FOLLOW_171_in_tupleLiteral7835); 
					pushFollow(FOLLOW_term_in_tupleLiteral7839);
					tn=term();
					state._fsp--;

					 l.add(tn); 
					}
					break;

				default :
					break loop148;
				}
			}

			match(input,169,FOLLOW_169_in_tupleLiteral7846); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1219:1: value returns [Term.Raw value] : (c= constant |l= collectionLiteral |u= usertypeLiteral |t= tupleLiteral | K_NULL | ':' id= ident | QMARK );
	public final Term.Raw value() throws RecognitionException {
		Term.Raw value = null;


		Constants.Literal c =null;
		Term.Raw l =null;
		UserTypes.Literal u =null;
		Tuples.Literal t =null;
		ColumnIdentifier id =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1220:5: (c= constant |l= collectionLiteral |u= usertypeLiteral |t= tupleLiteral | K_NULL | ':' id= ident | QMARK )
			int alt149=7;
			alt149 = dfa149.predict(input);
			switch (alt149) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1220:7: c= constant
					{
					pushFollow(FOLLOW_constant_in_value7869);
					c=constant();
					state._fsp--;

					 value = c; 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1221:7: l= collectionLiteral
					{
					pushFollow(FOLLOW_collectionLiteral_in_value7891);
					l=collectionLiteral();
					state._fsp--;

					 value = l; 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1222:7: u= usertypeLiteral
					{
					pushFollow(FOLLOW_usertypeLiteral_in_value7904);
					u=usertypeLiteral();
					state._fsp--;

					 value = u; 
					}
					break;
				case 4 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1223:7: t= tupleLiteral
					{
					pushFollow(FOLLOW_tupleLiteral_in_value7919);
					t=tupleLiteral();
					state._fsp--;

					 value = t; 
					}
					break;
				case 5 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1224:7: K_NULL
					{
					match(input,K_NULL,FOLLOW_K_NULL_in_value7935); 
					 value = Constants.NULL_LITERAL; 
					}
					break;
				case 6 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1225:7: ':' id= ident
					{
					match(input,174,FOLLOW_174_in_value7959); 
					pushFollow(FOLLOW_ident_in_value7963);
					id=ident();
					state._fsp--;

					 value = newBindVariables(id); 
					}
					break;
				case 7 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1226:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_value7981); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1229:1: intValue returns [Term.Raw value] : (|t= INTEGER | ':' id= ident | QMARK );
	public final Term.Raw intValue() throws RecognitionException {
		Term.Raw value = null;


		Token t=null;
		ColumnIdentifier id =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1230:5: (|t= INTEGER | ':' id= ident | QMARK )
			int alt150=4;
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
			case 175:
				{
				alt150=1;
				}
				break;
			case INTEGER:
				{
				alt150=2;
				}
				break;
			case 174:
				{
				alt150=3;
				}
				break;
			case QMARK:
				{
				alt150=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 150, 0, input);
				throw nvae;
			}
			switch (alt150) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1231:5: 
					{
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1231:7: t= INTEGER
					{
					t=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_intValue8027); 
					 value = Constants.Literal.integer((t!=null?t.getText():null)); 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1232:7: ':' id= ident
					{
					match(input,174,FOLLOW_174_in_intValue8041); 
					pushFollow(FOLLOW_ident_in_intValue8045);
					id=ident();
					state._fsp--;

					 value = newBindVariables(id); 
					}
					break;
				case 4 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1233:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_intValue8056); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1236:1: functionName returns [FunctionName s] : (ks= keyspaceName '.' )? f= allowedFunctionName ;
	public final FunctionName functionName() throws RecognitionException {
		FunctionName s = null;


		String ks =null;
		String f =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1237:5: ( (ks= keyspaceName '.' )? f= allowedFunctionName )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1237:7: (ks= keyspaceName '.' )? f= allowedFunctionName
			{
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1237:7: (ks= keyspaceName '.' )?
			int alt151=2;
			alt151 = dfa151.predict(input);
			switch (alt151) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1237:8: ks= keyspaceName '.'
					{
					pushFollow(FOLLOW_keyspaceName_in_functionName8090);
					ks=keyspaceName();
					state._fsp--;

					match(input,173,FOLLOW_173_in_functionName8092); 
					}
					break;

			}

			pushFollow(FOLLOW_allowedFunctionName_in_functionName8098);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1240:1: allowedFunctionName returns [String s] : (f= IDENT |f= QUOTED_NAME |u= unreserved_function_keyword | K_TOKEN | K_COUNT );
	public final String allowedFunctionName() throws RecognitionException {
		String s = null;


		Token f=null;
		String u =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1241:5: (f= IDENT |f= QUOTED_NAME |u= unreserved_function_keyword | K_TOKEN | K_COUNT )
			int alt152=5;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				alt152=1;
				}
				break;
			case QUOTED_NAME:
				{
				alt152=2;
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
				alt152=3;
				}
				break;
			case K_TOKEN:
				{
				alt152=4;
				}
				break;
			case K_COUNT:
				{
				alt152=5;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 152, 0, input);
				throw nvae;
			}
			switch (alt152) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1241:7: f= IDENT
					{
					f=(Token)match(input,IDENT,FOLLOW_IDENT_in_allowedFunctionName8125); 
					 s = (f!=null?f.getText():null).toLowerCase(); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1242:7: f= QUOTED_NAME
					{
					f=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_allowedFunctionName8159); 
					 s = (f!=null?f.getText():null); 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1243:7: u= unreserved_function_keyword
					{
					pushFollow(FOLLOW_unreserved_function_keyword_in_allowedFunctionName8187);
					u=unreserved_function_keyword();
					state._fsp--;

					 s = u; 
					}
					break;
				case 4 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1244:7: K_TOKEN
					{
					match(input,K_TOKEN,FOLLOW_K_TOKEN_in_allowedFunctionName8197); 
					 s = "token"; 
					}
					break;
				case 5 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1245:7: K_COUNT
					{
					match(input,K_COUNT,FOLLOW_K_COUNT_in_allowedFunctionName8229); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1248:1: function returns [Term.Raw t] : (f= functionName '(' ')' |f= functionName '(' args= functionArgs ')' );
	public final Term.Raw function() throws RecognitionException {
		Term.Raw t = null;


		FunctionName f =null;
		List<Term.Raw> args =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1249:5: (f= functionName '(' ')' |f= functionName '(' args= functionArgs ')' )
			int alt153=2;
			alt153 = dfa153.predict(input);
			switch (alt153) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1249:7: f= functionName '(' ')'
					{
					pushFollow(FOLLOW_functionName_in_function8276);
					f=functionName();
					state._fsp--;

					match(input,168,FOLLOW_168_in_function8278); 
					match(input,169,FOLLOW_169_in_function8280); 
					 t = new FunctionCall.Raw(f, Collections.<Term.Raw>emptyList()); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1250:7: f= functionName '(' args= functionArgs ')'
					{
					pushFollow(FOLLOW_functionName_in_function8310);
					f=functionName();
					state._fsp--;

					match(input,168,FOLLOW_168_in_function8312); 
					pushFollow(FOLLOW_functionArgs_in_function8316);
					args=functionArgs();
					state._fsp--;

					match(input,169,FOLLOW_169_in_function8318); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1253:1: functionArgs returns [List<Term.Raw> args] : t1= term ( ',' tn= term )* ;
	public final List<Term.Raw> functionArgs() throws RecognitionException {
		List<Term.Raw> args = null;


		Term.Raw t1 =null;
		Term.Raw tn =null;

		 args = new ArrayList<Term.Raw>(); 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1255:5: (t1= term ( ',' tn= term )* )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1255:7: t1= term ( ',' tn= term )*
			{
			pushFollow(FOLLOW_term_in_functionArgs8351);
			t1=term();
			state._fsp--;

			args.add(t1); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1255:32: ( ',' tn= term )*
			loop154:
			while (true) {
				int alt154=2;
				int LA154_0 = input.LA(1);
				if ( (LA154_0==171) ) {
					alt154=1;
				}

				switch (alt154) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1255:34: ',' tn= term
					{
					match(input,171,FOLLOW_171_in_functionArgs8357); 
					pushFollow(FOLLOW_term_in_functionArgs8361);
					tn=term();
					state._fsp--;

					 args.add(tn); 
					}
					break;

				default :
					break loop154;
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1258:1: term returns [Term.Raw term] : (v= value |f= function | '(' c= comparatorType ')' t= term );
	public final Term.Raw term() throws RecognitionException {
		Term.Raw term = null;


		Term.Raw v =null;
		Term.Raw f =null;
		CQL3Type.Raw c =null;
		Term.Raw t =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1259:5: (v= value |f= function | '(' c= comparatorType ')' t= term )
			int alt155=3;
			alt155 = dfa155.predict(input);
			switch (alt155) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1259:7: v= value
					{
					pushFollow(FOLLOW_value_in_term8389);
					v=value();
					state._fsp--;

					 term = v; 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1260:7: f= function
					{
					pushFollow(FOLLOW_function_in_term8426);
					f=function();
					state._fsp--;

					 term = f; 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1261:7: '(' c= comparatorType ')' t= term
					{
					match(input,168,FOLLOW_168_in_term8458); 
					pushFollow(FOLLOW_comparatorType_in_term8462);
					c=comparatorType();
					state._fsp--;

					match(input,169,FOLLOW_169_in_term8464); 
					pushFollow(FOLLOW_term_in_term8468);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1264:1: columnOperation[List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations] : key= cident columnOperationDifferentiator[operations, key] ;
	public final void columnOperation(List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations) throws RecognitionException {
		ColumnIdentifier.Raw key =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1265:5: (key= cident columnOperationDifferentiator[operations, key] )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1265:7: key= cident columnOperationDifferentiator[operations, key]
			{
			pushFollow(FOLLOW_cident_in_columnOperation8491);
			key=cident();
			state._fsp--;

			pushFollow(FOLLOW_columnOperationDifferentiator_in_columnOperation8493);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1268:1: columnOperationDifferentiator[List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations, ColumnIdentifier.Raw key] : ( '=' normalColumnOperation[operations, key] | '[' k= term ']' specializedColumnOperation[operations, key, k] );
	public final void columnOperationDifferentiator(List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations, ColumnIdentifier.Raw key) throws RecognitionException {
		Term.Raw k =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1269:5: ( '=' normalColumnOperation[operations, key] | '[' k= term ']' specializedColumnOperation[operations, key, k] )
			int alt156=2;
			int LA156_0 = input.LA(1);
			if ( (LA156_0==178) ) {
				alt156=1;
			}
			else if ( (LA156_0==181) ) {
				alt156=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 156, 0, input);
				throw nvae;
			}

			switch (alt156) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1269:7: '=' normalColumnOperation[operations, key]
					{
					match(input,178,FOLLOW_178_in_columnOperationDifferentiator8512); 
					pushFollow(FOLLOW_normalColumnOperation_in_columnOperationDifferentiator8514);
					normalColumnOperation(operations, key);
					state._fsp--;

					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1270:7: '[' k= term ']' specializedColumnOperation[operations, key, k]
					{
					match(input,181,FOLLOW_181_in_columnOperationDifferentiator8523); 
					pushFollow(FOLLOW_term_in_columnOperationDifferentiator8527);
					k=term();
					state._fsp--;

					match(input,183,FOLLOW_183_in_columnOperationDifferentiator8529); 
					pushFollow(FOLLOW_specializedColumnOperation_in_columnOperationDifferentiator8531);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1273:1: normalColumnOperation[List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations, ColumnIdentifier.Raw key] : (t= term ( '+' c= cident )? |c= cident sig= ( '+' | '-' ) t= term |c= cident i= INTEGER );
	public final void normalColumnOperation(List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations, ColumnIdentifier.Raw key) throws RecognitionException {
		Token sig=null;
		Token i=null;
		Term.Raw t =null;
		ColumnIdentifier.Raw c =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1274:5: (t= term ( '+' c= cident )? |c= cident sig= ( '+' | '-' ) t= term |c= cident i= INTEGER )
			int alt158=3;
			alt158 = dfa158.predict(input);
			switch (alt158) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1274:7: t= term ( '+' c= cident )?
					{
					pushFollow(FOLLOW_term_in_normalColumnOperation8552);
					t=term();
					state._fsp--;

					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1274:14: ( '+' c= cident )?
					int alt157=2;
					int LA157_0 = input.LA(1);
					if ( (LA157_0==170) ) {
						alt157=1;
					}
					switch (alt157) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1274:15: '+' c= cident
							{
							match(input,170,FOLLOW_170_in_normalColumnOperation8555); 
							pushFollow(FOLLOW_cident_in_normalColumnOperation8559);
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
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1287:7: c= cident sig= ( '+' | '-' ) t= term
					{
					pushFollow(FOLLOW_cident_in_normalColumnOperation8580);
					c=cident();
					state._fsp--;

					sig=input.LT(1);
					if ( input.LA(1)==170||input.LA(1)==172 ) {
						input.consume();
						state.errorRecovery=false;
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_term_in_normalColumnOperation8594);
					t=term();
					state._fsp--;


					          if (!key.equals(c))
					              addRecognitionError("Only expressions of the form X = X " + (sig!=null?sig.getText():null) + "<value> are supported.");
					          addRawUpdate(operations, key, (sig!=null?sig.getText():null).equals("+") ? new Operation.Addition(t) : new Operation.Substraction(t));
					      
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1293:7: c= cident i= INTEGER
					{
					pushFollow(FOLLOW_cident_in_normalColumnOperation8612);
					c=cident();
					state._fsp--;

					i=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_normalColumnOperation8616); 

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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1303:1: specializedColumnOperation[List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations, ColumnIdentifier.Raw key, Term.Raw k] : '=' t= term ;
	public final void specializedColumnOperation(List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations, ColumnIdentifier.Raw key, Term.Raw k) throws RecognitionException {
		Term.Raw t =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1304:5: ( '=' t= term )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1304:7: '=' t= term
			{
			match(input,178,FOLLOW_178_in_specializedColumnOperation8642); 
			pushFollow(FOLLOW_term_in_specializedColumnOperation8646);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1310:1: columnCondition[List<Pair<ColumnIdentifier.Raw, ColumnCondition.Raw>> conditions] : key= cident (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) | '[' element= term ']' (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) ) ) ;
	public final void columnCondition(List<Pair<ColumnIdentifier.Raw, ColumnCondition.Raw>> conditions) throws RecognitionException {
		ColumnIdentifier.Raw key =null;
		Operator op =null;
		Term.Raw t =null;
		List<Term.Raw> values =null;
		AbstractMarker.INRaw marker =null;
		Term.Raw element =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1312:5: (key= cident (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) | '[' element= term ']' (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) ) ) )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1312:7: key= cident (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) | '[' element= term ']' (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) ) )
			{
			pushFollow(FOLLOW_cident_in_columnCondition8679);
			key=cident();
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1313:9: (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) | '[' element= term ']' (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) ) )
			int alt162=3;
			switch ( input.LA(1) ) {
			case 167:
			case 176:
			case 177:
			case 178:
			case 179:
			case 180:
				{
				alt162=1;
				}
				break;
			case K_IN:
				{
				alt162=2;
				}
				break;
			case 181:
				{
				alt162=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 162, 0, input);
				throw nvae;
			}
			switch (alt162) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1313:11: op= relationType t= term
					{
					pushFollow(FOLLOW_relationType_in_columnCondition8693);
					op=relationType();
					state._fsp--;

					pushFollow(FOLLOW_term_in_columnCondition8697);
					t=term();
					state._fsp--;

					 conditions.add(Pair.create(key, ColumnCondition.Raw.simpleCondition(t, op))); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1314:11: K_IN (values= singleColumnInValues |marker= inMarker )
					{
					match(input,K_IN,FOLLOW_K_IN_in_columnCondition8711); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1315:13: (values= singleColumnInValues |marker= inMarker )
					int alt159=2;
					int LA159_0 = input.LA(1);
					if ( (LA159_0==168) ) {
						alt159=1;
					}
					else if ( (LA159_0==QMARK||LA159_0==174) ) {
						alt159=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 159, 0, input);
						throw nvae;
					}

					switch (alt159) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1315:15: values= singleColumnInValues
							{
							pushFollow(FOLLOW_singleColumnInValues_in_columnCondition8729);
							values=singleColumnInValues();
							state._fsp--;

							 conditions.add(Pair.create(key, ColumnCondition.Raw.simpleInCondition(values))); 
							}
							break;
						case 2 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1316:15: marker= inMarker
							{
							pushFollow(FOLLOW_inMarker_in_columnCondition8749);
							marker=inMarker();
							state._fsp--;

							 conditions.add(Pair.create(key, ColumnCondition.Raw.simpleInCondition(marker))); 
							}
							break;

					}

					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1318:11: '[' element= term ']' (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) )
					{
					match(input,181,FOLLOW_181_in_columnCondition8777); 
					pushFollow(FOLLOW_term_in_columnCondition8781);
					element=term();
					state._fsp--;

					match(input,183,FOLLOW_183_in_columnCondition8783); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1319:13: (op= relationType t= term | K_IN (values= singleColumnInValues |marker= inMarker ) )
					int alt161=2;
					int LA161_0 = input.LA(1);
					if ( (LA161_0==167||(LA161_0 >= 176 && LA161_0 <= 180)) ) {
						alt161=1;
					}
					else if ( (LA161_0==K_IN) ) {
						alt161=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 161, 0, input);
						throw nvae;
					}

					switch (alt161) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1319:15: op= relationType t= term
							{
							pushFollow(FOLLOW_relationType_in_columnCondition8801);
							op=relationType();
							state._fsp--;

							pushFollow(FOLLOW_term_in_columnCondition8805);
							t=term();
							state._fsp--;

							 conditions.add(Pair.create(key, ColumnCondition.Raw.collectionCondition(t, element, op))); 
							}
							break;
						case 2 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1320:15: K_IN (values= singleColumnInValues |marker= inMarker )
							{
							match(input,K_IN,FOLLOW_K_IN_in_columnCondition8823); 
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1321:17: (values= singleColumnInValues |marker= inMarker )
							int alt160=2;
							int LA160_0 = input.LA(1);
							if ( (LA160_0==168) ) {
								alt160=1;
							}
							else if ( (LA160_0==QMARK||LA160_0==174) ) {
								alt160=2;
							}

							else {
								NoViableAltException nvae =
									new NoViableAltException("", 160, 0, input);
								throw nvae;
							}

							switch (alt160) {
								case 1 :
									// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1321:19: values= singleColumnInValues
									{
									pushFollow(FOLLOW_singleColumnInValues_in_columnCondition8845);
									values=singleColumnInValues();
									state._fsp--;

									 conditions.add(Pair.create(key, ColumnCondition.Raw.collectionInCondition(element, values))); 
									}
									break;
								case 2 :
									// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1322:19: marker= inMarker
									{
									pushFollow(FOLLOW_inMarker_in_columnCondition8869);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1328:1: properties[PropertyDefinitions props] : property[props] ( K_AND property[props] )* ;
	public final void properties(PropertyDefinitions props) throws RecognitionException {
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1329:5: ( property[props] ( K_AND property[props] )* )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1329:7: property[props] ( K_AND property[props] )*
			{
			pushFollow(FOLLOW_property_in_properties8931);
			property(props);
			state._fsp--;

			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1329:23: ( K_AND property[props] )*
			loop163:
			while (true) {
				int alt163=2;
				int LA163_0 = input.LA(1);
				if ( (LA163_0==K_AND) ) {
					alt163=1;
				}

				switch (alt163) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1329:24: K_AND property[props]
					{
					match(input,K_AND,FOLLOW_K_AND_in_properties8935); 
					pushFollow(FOLLOW_property_in_properties8937);
					property(props);
					state._fsp--;

					}
					break;

				default :
					break loop163;
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1332:1: property[PropertyDefinitions props] : (k= ident '=' simple= propertyValue |k= ident '=' map= mapLiteral );
	public final void property(PropertyDefinitions props) throws RecognitionException {
		ColumnIdentifier k =null;
		String simple =null;
		Maps.Literal map =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1333:5: (k= ident '=' simple= propertyValue |k= ident '=' map= mapLiteral )
			int alt164=2;
			alt164 = dfa164.predict(input);
			switch (alt164) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1333:7: k= ident '=' simple= propertyValue
					{
					pushFollow(FOLLOW_ident_in_property8960);
					k=ident();
					state._fsp--;

					match(input,178,FOLLOW_178_in_property8962); 
					pushFollow(FOLLOW_propertyValue_in_property8966);
					simple=propertyValue();
					state._fsp--;

					 try { props.addProperty(k.toString(), simple); } catch (SyntaxException e) { addRecognitionError(e.getMessage()); } 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1334:7: k= ident '=' map= mapLiteral
					{
					pushFollow(FOLLOW_ident_in_property8978);
					k=ident();
					state._fsp--;

					match(input,178,FOLLOW_178_in_property8980); 
					pushFollow(FOLLOW_mapLiteral_in_property8984);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1337:1: propertyValue returns [String str] : (c= constant |u= unreserved_keyword );
	public final String propertyValue() throws RecognitionException {
		String str = null;


		Constants.Literal c =null;
		String u =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1338:5: (c= constant |u= unreserved_keyword )
			int alt165=2;
			int LA165_0 = input.LA(1);
			if ( (LA165_0==BOOLEAN||LA165_0==FLOAT||LA165_0==HEXNUMBER||LA165_0==INTEGER||LA165_0==K_INFINITY||LA165_0==K_NAN||LA165_0==STRING_LITERAL||LA165_0==UUID||LA165_0==172) ) {
				alt165=1;
			}
			else if ( ((LA165_0 >= K_AGGREGATE && LA165_0 <= K_ALL)||LA165_0==K_AS||LA165_0==K_ASCII||(LA165_0 >= K_BIGINT && LA165_0 <= K_BOOLEAN)||(LA165_0 >= K_CALLED && LA165_0 <= K_CLUSTERING)||(LA165_0 >= K_COMPACT && LA165_0 <= K_COUNTER)||(LA165_0 >= K_CUSTOM && LA165_0 <= K_DECIMAL)||(LA165_0 >= K_DISTINCT && LA165_0 <= K_DOUBLE)||(LA165_0 >= K_EXISTS && LA165_0 <= K_FLOAT)||LA165_0==K_FROZEN||(LA165_0 >= K_FUNCTION && LA165_0 <= K_FUNCTIONS)||LA165_0==K_INET||(LA165_0 >= K_INITCOND && LA165_0 <= K_INPUT)||LA165_0==K_INT||(LA165_0 >= K_JSON && LA165_0 <= K_KEYS)||(LA165_0 >= K_KEYSPACES && LA165_0 <= K_LANGUAGE)||(LA165_0 >= K_LIST && LA165_0 <= K_MAP)||LA165_0==K_NOLOGIN||LA165_0==K_NOSUPERUSER||LA165_0==K_OPTIONS||(LA165_0 >= K_PASSWORD && LA165_0 <= K_PERMISSIONS)||LA165_0==K_RETURNS||(LA165_0 >= K_ROLE && LA165_0 <= K_ROLES)||(LA165_0 >= K_SFUNC && LA165_0 <= K_TINYINT)||LA165_0==K_TRIGGER||(LA165_0 >= K_TTL && LA165_0 <= K_TYPE)||(LA165_0 >= K_USER && LA165_0 <= K_USERS)||(LA165_0 >= K_UUID && LA165_0 <= K_VARINT)||LA165_0==K_WRITETIME) ) {
				alt165=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 165, 0, input);
				throw nvae;
			}

			switch (alt165) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1338:7: c= constant
					{
					pushFollow(FOLLOW_constant_in_propertyValue9009);
					c=constant();
					state._fsp--;

					 str = c.getRawText(); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1339:7: u= unreserved_keyword
					{
					pushFollow(FOLLOW_unreserved_keyword_in_propertyValue9031);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1342:1: relationType returns [Operator op] : ( '=' | '<' | '<=' | '>' | '>=' | '!=' );
	public final Operator relationType() throws RecognitionException {
		Operator op = null;


		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1343:5: ( '=' | '<' | '<=' | '>' | '>=' | '!=' )
			int alt166=6;
			switch ( input.LA(1) ) {
			case 178:
				{
				alt166=1;
				}
				break;
			case 176:
				{
				alt166=2;
				}
				break;
			case 177:
				{
				alt166=3;
				}
				break;
			case 179:
				{
				alt166=4;
				}
				break;
			case 180:
				{
				alt166=5;
				}
				break;
			case 167:
				{
				alt166=6;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 166, 0, input);
				throw nvae;
			}
			switch (alt166) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1343:7: '='
					{
					match(input,178,FOLLOW_178_in_relationType9054); 
					 op = Operator.EQ; 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1344:7: '<'
					{
					match(input,176,FOLLOW_176_in_relationType9065); 
					 op = Operator.LT; 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1345:7: '<='
					{
					match(input,177,FOLLOW_177_in_relationType9076); 
					 op = Operator.LTE; 
					}
					break;
				case 4 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1346:7: '>'
					{
					match(input,179,FOLLOW_179_in_relationType9086); 
					 op = Operator.GT; 
					}
					break;
				case 5 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1347:7: '>='
					{
					match(input,180,FOLLOW_180_in_relationType9097); 
					 op = Operator.GTE; 
					}
					break;
				case 6 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1348:7: '!='
					{
					match(input,167,FOLLOW_167_in_relationType9107); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1351:1: relation[List<Relation> clauses] : (name= cident type= relationType t= term | K_TOKEN l= tupleOfIdentifiers type= relationType t= term |name= cident K_IN marker= inMarker |name= cident K_IN inValues= singleColumnInValues |name= cident K_CONTAINS ( K_KEY )? t= term |name= cident '[' key= term ']' type= relationType t= term |ids= tupleOfIdentifiers ( K_IN ( '(' ')' |tupleInMarker= inMarkerForTuple |literals= tupleOfTupleLiterals |markers= tupleOfMarkersForTuples ) |type= relationType literal= tupleLiteral |type= relationType tupleMarker= markerForTuple ) | '(' relation[$clauses] ')' );
	public final void relation(List<Relation> clauses) throws RecognitionException {
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
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1352:5: (name= cident type= relationType t= term | K_TOKEN l= tupleOfIdentifiers type= relationType t= term |name= cident K_IN marker= inMarker |name= cident K_IN inValues= singleColumnInValues |name= cident K_CONTAINS ( K_KEY )? t= term |name= cident '[' key= term ']' type= relationType t= term |ids= tupleOfIdentifiers ( K_IN ( '(' ')' |tupleInMarker= inMarkerForTuple |literals= tupleOfTupleLiterals |markers= tupleOfMarkersForTuples ) |type= relationType literal= tupleLiteral |type= relationType tupleMarker= markerForTuple ) | '(' relation[$clauses] ')' )
			int alt170=8;
			alt170 = dfa170.predict(input);
			switch (alt170) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1352:7: name= cident type= relationType t= term
					{
					pushFollow(FOLLOW_cident_in_relation9129);
					name=cident();
					state._fsp--;

					pushFollow(FOLLOW_relationType_in_relation9133);
					type=relationType();
					state._fsp--;

					pushFollow(FOLLOW_term_in_relation9137);
					t=term();
					state._fsp--;

					 clauses.add(new SingleColumnRelation(name, type, t)); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1353:7: K_TOKEN l= tupleOfIdentifiers type= relationType t= term
					{
					match(input,K_TOKEN,FOLLOW_K_TOKEN_in_relation9147); 
					pushFollow(FOLLOW_tupleOfIdentifiers_in_relation9151);
					l=tupleOfIdentifiers();
					state._fsp--;

					pushFollow(FOLLOW_relationType_in_relation9155);
					type=relationType();
					state._fsp--;

					pushFollow(FOLLOW_term_in_relation9159);
					t=term();
					state._fsp--;

					 clauses.add(new TokenRelation(l, type, t)); 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1355:7: name= cident K_IN marker= inMarker
					{
					pushFollow(FOLLOW_cident_in_relation9179);
					name=cident();
					state._fsp--;

					match(input,K_IN,FOLLOW_K_IN_in_relation9181); 
					pushFollow(FOLLOW_inMarker_in_relation9185);
					marker=inMarker();
					state._fsp--;

					 clauses.add(new SingleColumnRelation(name, Operator.IN, marker)); 
					}
					break;
				case 4 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1357:7: name= cident K_IN inValues= singleColumnInValues
					{
					pushFollow(FOLLOW_cident_in_relation9205);
					name=cident();
					state._fsp--;

					match(input,K_IN,FOLLOW_K_IN_in_relation9207); 
					pushFollow(FOLLOW_singleColumnInValues_in_relation9211);
					inValues=singleColumnInValues();
					state._fsp--;

					 clauses.add(SingleColumnRelation.createInRelation(name, inValues)); 
					}
					break;
				case 5 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1359:7: name= cident K_CONTAINS ( K_KEY )? t= term
					{
					pushFollow(FOLLOW_cident_in_relation9231);
					name=cident();
					state._fsp--;

					match(input,K_CONTAINS,FOLLOW_K_CONTAINS_in_relation9233); 
					 Operator rt = Operator.CONTAINS; 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1359:67: ( K_KEY )?
					int alt167=2;
					int LA167_0 = input.LA(1);
					if ( (LA167_0==K_KEY) ) {
						int LA167_1 = input.LA(2);
						if ( (LA167_1==BOOLEAN||LA167_1==FLOAT||LA167_1==HEXNUMBER||(LA167_1 >= IDENT && LA167_1 <= INTEGER)||(LA167_1 >= K_AGGREGATE && LA167_1 <= K_ALL)||LA167_1==K_AS||LA167_1==K_ASCII||(LA167_1 >= K_BIGINT && LA167_1 <= K_BOOLEAN)||(LA167_1 >= K_CALLED && LA167_1 <= K_CLUSTERING)||(LA167_1 >= K_COMPACT && LA167_1 <= K_COUNTER)||(LA167_1 >= K_CUSTOM && LA167_1 <= K_DECIMAL)||(LA167_1 >= K_DISTINCT && LA167_1 <= K_DOUBLE)||(LA167_1 >= K_EXISTS && LA167_1 <= K_FLOAT)||LA167_1==K_FROZEN||(LA167_1 >= K_FUNCTION && LA167_1 <= K_FUNCTIONS)||(LA167_1 >= K_INET && LA167_1 <= K_INPUT)||LA167_1==K_INT||(LA167_1 >= K_JSON && LA167_1 <= K_KEYS)||(LA167_1 >= K_KEYSPACES && LA167_1 <= K_LANGUAGE)||(LA167_1 >= K_LIST && LA167_1 <= K_MAP)||(LA167_1 >= K_NAN && LA167_1 <= K_NOLOGIN)||LA167_1==K_NOSUPERUSER||LA167_1==K_NULL||LA167_1==K_OPTIONS||(LA167_1 >= K_PASSWORD && LA167_1 <= K_PERMISSIONS)||LA167_1==K_RETURNS||(LA167_1 >= K_ROLE && LA167_1 <= K_ROLES)||(LA167_1 >= K_SFUNC && LA167_1 <= K_TINYINT)||(LA167_1 >= K_TOKEN && LA167_1 <= K_TRIGGER)||(LA167_1 >= K_TTL && LA167_1 <= K_TYPE)||(LA167_1 >= K_USER && LA167_1 <= K_USERS)||(LA167_1 >= K_UUID && LA167_1 <= K_VARINT)||LA167_1==K_WRITETIME||(LA167_1 >= QMARK && LA167_1 <= QUOTED_NAME)||LA167_1==STRING_LITERAL||LA167_1==UUID||LA167_1==168||LA167_1==172||LA167_1==174||LA167_1==181||LA167_1==184) ) {
							alt167=1;
						}
					}
					switch (alt167) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1359:68: K_KEY
							{
							match(input,K_KEY,FOLLOW_K_KEY_in_relation9238); 
							 rt = Operator.CONTAINS_KEY; 
							}
							break;

					}

					pushFollow(FOLLOW_term_in_relation9254);
					t=term();
					state._fsp--;

					 clauses.add(new SingleColumnRelation(name, rt, t)); 
					}
					break;
				case 6 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1361:7: name= cident '[' key= term ']' type= relationType t= term
					{
					pushFollow(FOLLOW_cident_in_relation9266);
					name=cident();
					state._fsp--;

					match(input,181,FOLLOW_181_in_relation9268); 
					pushFollow(FOLLOW_term_in_relation9272);
					key=term();
					state._fsp--;

					match(input,183,FOLLOW_183_in_relation9274); 
					pushFollow(FOLLOW_relationType_in_relation9278);
					type=relationType();
					state._fsp--;

					pushFollow(FOLLOW_term_in_relation9282);
					t=term();
					state._fsp--;

					 clauses.add(new SingleColumnRelation(name, key, type, t)); 
					}
					break;
				case 7 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1362:7: ids= tupleOfIdentifiers ( K_IN ( '(' ')' |tupleInMarker= inMarkerForTuple |literals= tupleOfTupleLiterals |markers= tupleOfMarkersForTuples ) |type= relationType literal= tupleLiteral |type= relationType tupleMarker= markerForTuple )
					{
					pushFollow(FOLLOW_tupleOfIdentifiers_in_relation9294);
					ids=tupleOfIdentifiers();
					state._fsp--;

					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1363:7: ( K_IN ( '(' ')' |tupleInMarker= inMarkerForTuple |literals= tupleOfTupleLiterals |markers= tupleOfMarkersForTuples ) |type= relationType literal= tupleLiteral |type= relationType tupleMarker= markerForTuple )
					int alt169=3;
					alt169 = dfa169.predict(input);
					switch (alt169) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1363:9: K_IN ( '(' ')' |tupleInMarker= inMarkerForTuple |literals= tupleOfTupleLiterals |markers= tupleOfMarkersForTuples )
							{
							match(input,K_IN,FOLLOW_K_IN_in_relation9304); 
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1364:11: ( '(' ')' |tupleInMarker= inMarkerForTuple |literals= tupleOfTupleLiterals |markers= tupleOfMarkersForTuples )
							int alt168=4;
							int LA168_0 = input.LA(1);
							if ( (LA168_0==168) ) {
								switch ( input.LA(2) ) {
								case 169:
									{
									alt168=1;
									}
									break;
								case 168:
									{
									alt168=3;
									}
									break;
								case QMARK:
								case 174:
									{
									alt168=4;
									}
									break;
								default:
									int nvaeMark = input.mark();
									try {
										input.consume();
										NoViableAltException nvae =
											new NoViableAltException("", 168, 1, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}
							}
							else if ( (LA168_0==QMARK||LA168_0==174) ) {
								alt168=2;
							}

							else {
								NoViableAltException nvae =
									new NoViableAltException("", 168, 0, input);
								throw nvae;
							}

							switch (alt168) {
								case 1 :
									// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1364:13: '(' ')'
									{
									match(input,168,FOLLOW_168_in_relation9318); 
									match(input,169,FOLLOW_169_in_relation9320); 
									 clauses.add(MultiColumnRelation.createInRelation(ids, new ArrayList<Tuples.Literal>())); 
									}
									break;
								case 2 :
									// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1366:13: tupleInMarker= inMarkerForTuple
									{
									pushFollow(FOLLOW_inMarkerForTuple_in_relation9352);
									tupleInMarker=inMarkerForTuple();
									state._fsp--;

									 clauses.add(MultiColumnRelation.createSingleMarkerInRelation(ids, tupleInMarker)); 
									}
									break;
								case 3 :
									// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1368:13: literals= tupleOfTupleLiterals
									{
									pushFollow(FOLLOW_tupleOfTupleLiterals_in_relation9386);
									literals=tupleOfTupleLiterals();
									state._fsp--;


									                  clauses.add(MultiColumnRelation.createInRelation(ids, literals));
									              
									}
									break;
								case 4 :
									// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1372:13: markers= tupleOfMarkersForTuples
									{
									pushFollow(FOLLOW_tupleOfMarkersForTuples_in_relation9420);
									markers=tupleOfMarkersForTuples();
									state._fsp--;

									 clauses.add(MultiColumnRelation.createInRelation(ids, markers)); 
									}
									break;

							}

							}
							break;
						case 2 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1375:9: type= relationType literal= tupleLiteral
							{
							pushFollow(FOLLOW_relationType_in_relation9462);
							type=relationType();
							state._fsp--;

							pushFollow(FOLLOW_tupleLiteral_in_relation9466);
							literal=tupleLiteral();
							state._fsp--;


							              clauses.add(MultiColumnRelation.createNonInRelation(ids, type, literal));
							          
							}
							break;
						case 3 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1379:9: type= relationType tupleMarker= markerForTuple
							{
							pushFollow(FOLLOW_relationType_in_relation9492);
							type=relationType();
							state._fsp--;

							pushFollow(FOLLOW_markerForTuple_in_relation9496);
							tupleMarker=markerForTuple();
							state._fsp--;

							 clauses.add(MultiColumnRelation.createNonInRelation(ids, type, tupleMarker)); 
							}
							break;

					}

					}
					break;
				case 8 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1382:7: '(' relation[$clauses] ')'
					{
					match(input,168,FOLLOW_168_in_relation9526); 
					pushFollow(FOLLOW_relation_in_relation9528);
					relation(clauses);
					state._fsp--;

					match(input,169,FOLLOW_169_in_relation9531); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1385:1: inMarker returns [AbstractMarker.INRaw marker] : ( QMARK | ':' name= ident );
	public final AbstractMarker.INRaw inMarker() throws RecognitionException {
		AbstractMarker.INRaw marker = null;


		ColumnIdentifier name =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1386:5: ( QMARK | ':' name= ident )
			int alt171=2;
			int LA171_0 = input.LA(1);
			if ( (LA171_0==QMARK) ) {
				alt171=1;
			}
			else if ( (LA171_0==174) ) {
				alt171=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 171, 0, input);
				throw nvae;
			}

			switch (alt171) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1386:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_inMarker9552); 
					 marker = newINBindVariables(null); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1387:7: ':' name= ident
					{
					match(input,174,FOLLOW_174_in_inMarker9562); 
					pushFollow(FOLLOW_ident_in_inMarker9566);
					name=ident();
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1390:1: tupleOfIdentifiers returns [List<ColumnIdentifier.Raw> ids] : '(' n1= cident ( ',' ni= cident )* ')' ;
	public final List<ColumnIdentifier.Raw> tupleOfIdentifiers() throws RecognitionException {
		List<ColumnIdentifier.Raw> ids = null;


		ColumnIdentifier.Raw n1 =null;
		ColumnIdentifier.Raw ni =null;

		 ids = new ArrayList<ColumnIdentifier.Raw>(); 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1392:5: ( '(' n1= cident ( ',' ni= cident )* ')' )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1392:7: '(' n1= cident ( ',' ni= cident )* ')'
			{
			match(input,168,FOLLOW_168_in_tupleOfIdentifiers9598); 
			pushFollow(FOLLOW_cident_in_tupleOfIdentifiers9602);
			n1=cident();
			state._fsp--;

			 ids.add(n1); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1392:39: ( ',' ni= cident )*
			loop172:
			while (true) {
				int alt172=2;
				int LA172_0 = input.LA(1);
				if ( (LA172_0==171) ) {
					alt172=1;
				}

				switch (alt172) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1392:40: ',' ni= cident
					{
					match(input,171,FOLLOW_171_in_tupleOfIdentifiers9607); 
					pushFollow(FOLLOW_cident_in_tupleOfIdentifiers9611);
					ni=cident();
					state._fsp--;

					 ids.add(ni); 
					}
					break;

				default :
					break loop172;
				}
			}

			match(input,169,FOLLOW_169_in_tupleOfIdentifiers9617); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1395:1: singleColumnInValues returns [List<Term.Raw> terms] : '(' (t1= term ( ',' ti= term )* )? ')' ;
	public final List<Term.Raw> singleColumnInValues() throws RecognitionException {
		List<Term.Raw> terms = null;


		Term.Raw t1 =null;
		Term.Raw ti =null;

		 terms = new ArrayList<Term.Raw>(); 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1397:5: ( '(' (t1= term ( ',' ti= term )* )? ')' )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1397:7: '(' (t1= term ( ',' ti= term )* )? ')'
			{
			match(input,168,FOLLOW_168_in_singleColumnInValues9647); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1397:11: (t1= term ( ',' ti= term )* )?
			int alt174=2;
			int LA174_0 = input.LA(1);
			if ( (LA174_0==BOOLEAN||LA174_0==FLOAT||LA174_0==HEXNUMBER||(LA174_0 >= IDENT && LA174_0 <= INTEGER)||(LA174_0 >= K_AGGREGATE && LA174_0 <= K_ALL)||LA174_0==K_AS||LA174_0==K_ASCII||(LA174_0 >= K_BIGINT && LA174_0 <= K_BOOLEAN)||(LA174_0 >= K_CALLED && LA174_0 <= K_CLUSTERING)||(LA174_0 >= K_COMPACT && LA174_0 <= K_COUNTER)||(LA174_0 >= K_CUSTOM && LA174_0 <= K_DECIMAL)||(LA174_0 >= K_DISTINCT && LA174_0 <= K_DOUBLE)||(LA174_0 >= K_EXISTS && LA174_0 <= K_FLOAT)||LA174_0==K_FROZEN||(LA174_0 >= K_FUNCTION && LA174_0 <= K_FUNCTIONS)||(LA174_0 >= K_INET && LA174_0 <= K_INPUT)||LA174_0==K_INT||(LA174_0 >= K_JSON && LA174_0 <= K_KEYS)||(LA174_0 >= K_KEYSPACES && LA174_0 <= K_LANGUAGE)||(LA174_0 >= K_LIST && LA174_0 <= K_MAP)||(LA174_0 >= K_NAN && LA174_0 <= K_NOLOGIN)||LA174_0==K_NOSUPERUSER||LA174_0==K_NULL||LA174_0==K_OPTIONS||(LA174_0 >= K_PASSWORD && LA174_0 <= K_PERMISSIONS)||LA174_0==K_RETURNS||(LA174_0 >= K_ROLE && LA174_0 <= K_ROLES)||(LA174_0 >= K_SFUNC && LA174_0 <= K_TINYINT)||(LA174_0 >= K_TOKEN && LA174_0 <= K_TRIGGER)||(LA174_0 >= K_TTL && LA174_0 <= K_TYPE)||(LA174_0 >= K_USER && LA174_0 <= K_USERS)||(LA174_0 >= K_UUID && LA174_0 <= K_VARINT)||LA174_0==K_WRITETIME||(LA174_0 >= QMARK && LA174_0 <= QUOTED_NAME)||LA174_0==STRING_LITERAL||LA174_0==UUID||LA174_0==168||LA174_0==172||LA174_0==174||LA174_0==181||LA174_0==184) ) {
				alt174=1;
			}
			switch (alt174) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1397:13: t1= term ( ',' ti= term )*
					{
					pushFollow(FOLLOW_term_in_singleColumnInValues9655);
					t1=term();
					state._fsp--;

					 terms.add(t1); 
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1397:43: ( ',' ti= term )*
					loop173:
					while (true) {
						int alt173=2;
						int LA173_0 = input.LA(1);
						if ( (LA173_0==171) ) {
							alt173=1;
						}

						switch (alt173) {
						case 1 :
							// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1397:44: ',' ti= term
							{
							match(input,171,FOLLOW_171_in_singleColumnInValues9660); 
							pushFollow(FOLLOW_term_in_singleColumnInValues9664);
							ti=term();
							state._fsp--;

							 terms.add(ti); 
							}
							break;

						default :
							break loop173;
						}
					}

					}
					break;

			}

			match(input,169,FOLLOW_169_in_singleColumnInValues9673); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1400:1: tupleOfTupleLiterals returns [List<Tuples.Literal> literals] : '(' t1= tupleLiteral ( ',' ti= tupleLiteral )* ')' ;
	public final List<Tuples.Literal> tupleOfTupleLiterals() throws RecognitionException {
		List<Tuples.Literal> literals = null;


		Tuples.Literal t1 =null;
		Tuples.Literal ti =null;

		 literals = new ArrayList<>(); 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1402:5: ( '(' t1= tupleLiteral ( ',' ti= tupleLiteral )* ')' )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1402:7: '(' t1= tupleLiteral ( ',' ti= tupleLiteral )* ')'
			{
			match(input,168,FOLLOW_168_in_tupleOfTupleLiterals9703); 
			pushFollow(FOLLOW_tupleLiteral_in_tupleOfTupleLiterals9707);
			t1=tupleLiteral();
			state._fsp--;

			 literals.add(t1); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1402:50: ( ',' ti= tupleLiteral )*
			loop175:
			while (true) {
				int alt175=2;
				int LA175_0 = input.LA(1);
				if ( (LA175_0==171) ) {
					alt175=1;
				}

				switch (alt175) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1402:51: ',' ti= tupleLiteral
					{
					match(input,171,FOLLOW_171_in_tupleOfTupleLiterals9712); 
					pushFollow(FOLLOW_tupleLiteral_in_tupleOfTupleLiterals9716);
					ti=tupleLiteral();
					state._fsp--;

					 literals.add(ti); 
					}
					break;

				default :
					break loop175;
				}
			}

			match(input,169,FOLLOW_169_in_tupleOfTupleLiterals9722); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1405:1: markerForTuple returns [Tuples.Raw marker] : ( QMARK | ':' name= ident );
	public final Tuples.Raw markerForTuple() throws RecognitionException {
		Tuples.Raw marker = null;


		ColumnIdentifier name =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1406:5: ( QMARK | ':' name= ident )
			int alt176=2;
			int LA176_0 = input.LA(1);
			if ( (LA176_0==QMARK) ) {
				alt176=1;
			}
			else if ( (LA176_0==174) ) {
				alt176=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 176, 0, input);
				throw nvae;
			}

			switch (alt176) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1406:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_markerForTuple9743); 
					 marker = newTupleBindVariables(null); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1407:7: ':' name= ident
					{
					match(input,174,FOLLOW_174_in_markerForTuple9753); 
					pushFollow(FOLLOW_ident_in_markerForTuple9757);
					name=ident();
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1410:1: tupleOfMarkersForTuples returns [List<Tuples.Raw> markers] : '(' m1= markerForTuple ( ',' mi= markerForTuple )* ')' ;
	public final List<Tuples.Raw> tupleOfMarkersForTuples() throws RecognitionException {
		List<Tuples.Raw> markers = null;


		Tuples.Raw m1 =null;
		Tuples.Raw mi =null;

		 markers = new ArrayList<Tuples.Raw>(); 
		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1412:5: ( '(' m1= markerForTuple ( ',' mi= markerForTuple )* ')' )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1412:7: '(' m1= markerForTuple ( ',' mi= markerForTuple )* ')'
			{
			match(input,168,FOLLOW_168_in_tupleOfMarkersForTuples9789); 
			pushFollow(FOLLOW_markerForTuple_in_tupleOfMarkersForTuples9793);
			m1=markerForTuple();
			state._fsp--;

			 markers.add(m1); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1412:51: ( ',' mi= markerForTuple )*
			loop177:
			while (true) {
				int alt177=2;
				int LA177_0 = input.LA(1);
				if ( (LA177_0==171) ) {
					alt177=1;
				}

				switch (alt177) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1412:52: ',' mi= markerForTuple
					{
					match(input,171,FOLLOW_171_in_tupleOfMarkersForTuples9798); 
					pushFollow(FOLLOW_markerForTuple_in_tupleOfMarkersForTuples9802);
					mi=markerForTuple();
					state._fsp--;

					 markers.add(mi); 
					}
					break;

				default :
					break loop177;
				}
			}

			match(input,169,FOLLOW_169_in_tupleOfMarkersForTuples9808); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1415:1: inMarkerForTuple returns [Tuples.INRaw marker] : ( QMARK | ':' name= ident );
	public final Tuples.INRaw inMarkerForTuple() throws RecognitionException {
		Tuples.INRaw marker = null;


		ColumnIdentifier name =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1416:5: ( QMARK | ':' name= ident )
			int alt178=2;
			int LA178_0 = input.LA(1);
			if ( (LA178_0==QMARK) ) {
				alt178=1;
			}
			else if ( (LA178_0==174) ) {
				alt178=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 178, 0, input);
				throw nvae;
			}

			switch (alt178) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1416:7: QMARK
					{
					match(input,QMARK,FOLLOW_QMARK_in_inMarkerForTuple9829); 
					 marker = newTupleINBindVariables(null); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1417:7: ':' name= ident
					{
					match(input,174,FOLLOW_174_in_inMarkerForTuple9839); 
					pushFollow(FOLLOW_ident_in_inMarkerForTuple9843);
					name=ident();
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1420:1: comparatorType returns [CQL3Type.Raw t] : (n= native_type |c= collection_type |tt= tuple_type |id= userTypeName | K_FROZEN '<' f= comparatorType '>' |s= STRING_LITERAL );
	public final CQL3Type.Raw comparatorType() throws RecognitionException {
		CQL3Type.Raw t = null;


		Token s=null;
		CQL3Type n =null;
		CQL3Type.Raw c =null;
		CQL3Type.Raw tt =null;
		UTName id =null;
		CQL3Type.Raw f =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1421:5: (n= native_type |c= collection_type |tt= tuple_type |id= userTypeName | K_FROZEN '<' f= comparatorType '>' |s= STRING_LITERAL )
			int alt179=6;
			alt179 = dfa179.predict(input);
			switch (alt179) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1421:7: n= native_type
					{
					pushFollow(FOLLOW_native_type_in_comparatorType9868);
					n=native_type();
					state._fsp--;

					 t = CQL3Type.Raw.from(n); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1422:7: c= collection_type
					{
					pushFollow(FOLLOW_collection_type_in_comparatorType9884);
					c=collection_type();
					state._fsp--;

					 t = c; 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1423:7: tt= tuple_type
					{
					pushFollow(FOLLOW_tuple_type_in_comparatorType9896);
					tt=tuple_type();
					state._fsp--;

					 t = tt; 
					}
					break;
				case 4 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1424:7: id= userTypeName
					{
					pushFollow(FOLLOW_userTypeName_in_comparatorType9912);
					id=userTypeName();
					state._fsp--;

					 t = CQL3Type.Raw.userType(id); 
					}
					break;
				case 5 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1425:7: K_FROZEN '<' f= comparatorType '>'
					{
					match(input,K_FROZEN,FOLLOW_K_FROZEN_in_comparatorType9924); 
					match(input,176,FOLLOW_176_in_comparatorType9926); 
					pushFollow(FOLLOW_comparatorType_in_comparatorType9930);
					f=comparatorType();
					state._fsp--;

					match(input,179,FOLLOW_179_in_comparatorType9932); 

					        try {
					            t = CQL3Type.Raw.frozen(f);
					        } catch (InvalidRequestException e) {
					            addRecognitionError(e.getMessage());
					        }
					      
					}
					break;
				case 6 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1433:7: s= STRING_LITERAL
					{
					s=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_comparatorType9950); 

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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1445:1: native_type returns [CQL3Type t] : ( K_ASCII | K_BIGINT | K_BLOB | K_BOOLEAN | K_COUNTER | K_DECIMAL | K_DOUBLE | K_FLOAT | K_INET | K_INT | K_SMALLINT | K_TEXT | K_TIMESTAMP | K_TINYINT | K_UUID | K_VARCHAR | K_VARINT | K_TIMEUUID | K_DATE | K_TIME );
	public final CQL3Type native_type() throws RecognitionException {
		CQL3Type t = null;


		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1446:5: ( K_ASCII | K_BIGINT | K_BLOB | K_BOOLEAN | K_COUNTER | K_DECIMAL | K_DOUBLE | K_FLOAT | K_INET | K_INT | K_SMALLINT | K_TEXT | K_TIMESTAMP | K_TINYINT | K_UUID | K_VARCHAR | K_VARINT | K_TIMEUUID | K_DATE | K_TIME )
			int alt180=20;
			switch ( input.LA(1) ) {
			case K_ASCII:
				{
				alt180=1;
				}
				break;
			case K_BIGINT:
				{
				alt180=2;
				}
				break;
			case K_BLOB:
				{
				alt180=3;
				}
				break;
			case K_BOOLEAN:
				{
				alt180=4;
				}
				break;
			case K_COUNTER:
				{
				alt180=5;
				}
				break;
			case K_DECIMAL:
				{
				alt180=6;
				}
				break;
			case K_DOUBLE:
				{
				alt180=7;
				}
				break;
			case K_FLOAT:
				{
				alt180=8;
				}
				break;
			case K_INET:
				{
				alt180=9;
				}
				break;
			case K_INT:
				{
				alt180=10;
				}
				break;
			case K_SMALLINT:
				{
				alt180=11;
				}
				break;
			case K_TEXT:
				{
				alt180=12;
				}
				break;
			case K_TIMESTAMP:
				{
				alt180=13;
				}
				break;
			case K_TINYINT:
				{
				alt180=14;
				}
				break;
			case K_UUID:
				{
				alt180=15;
				}
				break;
			case K_VARCHAR:
				{
				alt180=16;
				}
				break;
			case K_VARINT:
				{
				alt180=17;
				}
				break;
			case K_TIMEUUID:
				{
				alt180=18;
				}
				break;
			case K_DATE:
				{
				alt180=19;
				}
				break;
			case K_TIME:
				{
				alt180=20;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 180, 0, input);
				throw nvae;
			}
			switch (alt180) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1446:7: K_ASCII
					{
					match(input,K_ASCII,FOLLOW_K_ASCII_in_native_type9979); 
					 t = CQL3Type.Native.ASCII; 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1447:7: K_BIGINT
					{
					match(input,K_BIGINT,FOLLOW_K_BIGINT_in_native_type9993); 
					 t = CQL3Type.Native.BIGINT; 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1448:7: K_BLOB
					{
					match(input,K_BLOB,FOLLOW_K_BLOB_in_native_type10006); 
					 t = CQL3Type.Native.BLOB; 
					}
					break;
				case 4 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1449:7: K_BOOLEAN
					{
					match(input,K_BOOLEAN,FOLLOW_K_BOOLEAN_in_native_type10021); 
					 t = CQL3Type.Native.BOOLEAN; 
					}
					break;
				case 5 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1450:7: K_COUNTER
					{
					match(input,K_COUNTER,FOLLOW_K_COUNTER_in_native_type10033); 
					 t = CQL3Type.Native.COUNTER; 
					}
					break;
				case 6 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1451:7: K_DECIMAL
					{
					match(input,K_DECIMAL,FOLLOW_K_DECIMAL_in_native_type10045); 
					 t = CQL3Type.Native.DECIMAL; 
					}
					break;
				case 7 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1452:7: K_DOUBLE
					{
					match(input,K_DOUBLE,FOLLOW_K_DOUBLE_in_native_type10057); 
					 t = CQL3Type.Native.DOUBLE; 
					}
					break;
				case 8 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1453:7: K_FLOAT
					{
					match(input,K_FLOAT,FOLLOW_K_FLOAT_in_native_type10070); 
					 t = CQL3Type.Native.FLOAT; 
					}
					break;
				case 9 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1454:7: K_INET
					{
					match(input,K_INET,FOLLOW_K_INET_in_native_type10084); 
					 t = CQL3Type.Native.INET;
					}
					break;
				case 10 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1455:7: K_INT
					{
					match(input,K_INT,FOLLOW_K_INT_in_native_type10099); 
					 t = CQL3Type.Native.INT; 
					}
					break;
				case 11 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1456:7: K_SMALLINT
					{
					match(input,K_SMALLINT,FOLLOW_K_SMALLINT_in_native_type10115); 
					 t = CQL3Type.Native.SMALLINT; 
					}
					break;
				case 12 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1457:7: K_TEXT
					{
					match(input,K_TEXT,FOLLOW_K_TEXT_in_native_type10126); 
					 t = CQL3Type.Native.TEXT; 
					}
					break;
				case 13 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1458:7: K_TIMESTAMP
					{
					match(input,K_TIMESTAMP,FOLLOW_K_TIMESTAMP_in_native_type10141); 
					 t = CQL3Type.Native.TIMESTAMP; 
					}
					break;
				case 14 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1459:7: K_TINYINT
					{
					match(input,K_TINYINT,FOLLOW_K_TINYINT_in_native_type10151); 
					 t = CQL3Type.Native.TINYINT; 
					}
					break;
				case 15 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1460:7: K_UUID
					{
					match(input,K_UUID,FOLLOW_K_UUID_in_native_type10163); 
					 t = CQL3Type.Native.UUID; 
					}
					break;
				case 16 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1461:7: K_VARCHAR
					{
					match(input,K_VARCHAR,FOLLOW_K_VARCHAR_in_native_type10178); 
					 t = CQL3Type.Native.VARCHAR; 
					}
					break;
				case 17 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1462:7: K_VARINT
					{
					match(input,K_VARINT,FOLLOW_K_VARINT_in_native_type10190); 
					 t = CQL3Type.Native.VARINT; 
					}
					break;
				case 18 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1463:7: K_TIMEUUID
					{
					match(input,K_TIMEUUID,FOLLOW_K_TIMEUUID_in_native_type10203); 
					 t = CQL3Type.Native.TIMEUUID; 
					}
					break;
				case 19 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1464:7: K_DATE
					{
					match(input,K_DATE,FOLLOW_K_DATE_in_native_type10214); 
					 t = CQL3Type.Native.DATE; 
					}
					break;
				case 20 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1465:7: K_TIME
					{
					match(input,K_TIME,FOLLOW_K_TIME_in_native_type10229); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1468:1: collection_type returns [CQL3Type.Raw pt] : ( K_MAP '<' t1= comparatorType ',' t2= comparatorType '>' | K_LIST '<' t= comparatorType '>' | K_SET '<' t= comparatorType '>' );
	public final CQL3Type.Raw collection_type() throws RecognitionException {
		CQL3Type.Raw pt = null;


		CQL3Type.Raw t1 =null;
		CQL3Type.Raw t2 =null;
		CQL3Type.Raw t =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1469:5: ( K_MAP '<' t1= comparatorType ',' t2= comparatorType '>' | K_LIST '<' t= comparatorType '>' | K_SET '<' t= comparatorType '>' )
			int alt181=3;
			switch ( input.LA(1) ) {
			case K_MAP:
				{
				alt181=1;
				}
				break;
			case K_LIST:
				{
				alt181=2;
				}
				break;
			case K_SET:
				{
				alt181=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 181, 0, input);
				throw nvae;
			}
			switch (alt181) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1469:7: K_MAP '<' t1= comparatorType ',' t2= comparatorType '>'
					{
					match(input,K_MAP,FOLLOW_K_MAP_in_collection_type10257); 
					match(input,176,FOLLOW_176_in_collection_type10260); 
					pushFollow(FOLLOW_comparatorType_in_collection_type10264);
					t1=comparatorType();
					state._fsp--;

					match(input,171,FOLLOW_171_in_collection_type10266); 
					pushFollow(FOLLOW_comparatorType_in_collection_type10270);
					t2=comparatorType();
					state._fsp--;

					match(input,179,FOLLOW_179_in_collection_type10272); 

					            // if we can't parse either t1 or t2, antlr will "recover" and we may have t1 or t2 null.
					            if (t1 != null && t2 != null)
					                pt = CQL3Type.Raw.map(t1, t2);
					        
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1475:7: K_LIST '<' t= comparatorType '>'
					{
					match(input,K_LIST,FOLLOW_K_LIST_in_collection_type10290); 
					match(input,176,FOLLOW_176_in_collection_type10292); 
					pushFollow(FOLLOW_comparatorType_in_collection_type10296);
					t=comparatorType();
					state._fsp--;

					match(input,179,FOLLOW_179_in_collection_type10298); 
					 if (t != null) pt = CQL3Type.Raw.list(t); 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1477:7: K_SET '<' t= comparatorType '>'
					{
					match(input,K_SET,FOLLOW_K_SET_in_collection_type10316); 
					match(input,176,FOLLOW_176_in_collection_type10319); 
					pushFollow(FOLLOW_comparatorType_in_collection_type10323);
					t=comparatorType();
					state._fsp--;

					match(input,179,FOLLOW_179_in_collection_type10325); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1481:1: tuple_type returns [CQL3Type.Raw t] : K_TUPLE '<' t1= comparatorType ( ',' tn= comparatorType )* '>' ;
	public final CQL3Type.Raw tuple_type() throws RecognitionException {
		CQL3Type.Raw t = null;


		CQL3Type.Raw t1 =null;
		CQL3Type.Raw tn =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1482:5: ( K_TUPLE '<' t1= comparatorType ( ',' tn= comparatorType )* '>' )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1482:7: K_TUPLE '<' t1= comparatorType ( ',' tn= comparatorType )* '>'
			{
			match(input,K_TUPLE,FOLLOW_K_TUPLE_in_tuple_type10356); 
			match(input,176,FOLLOW_176_in_tuple_type10358); 
			 List<CQL3Type.Raw> types = new ArrayList<>(); 
			pushFollow(FOLLOW_comparatorType_in_tuple_type10373);
			t1=comparatorType();
			state._fsp--;

			 types.add(t1); 
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1483:47: ( ',' tn= comparatorType )*
			loop182:
			while (true) {
				int alt182=2;
				int LA182_0 = input.LA(1);
				if ( (LA182_0==171) ) {
					alt182=1;
				}

				switch (alt182) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1483:48: ',' tn= comparatorType
					{
					match(input,171,FOLLOW_171_in_tuple_type10378); 
					pushFollow(FOLLOW_comparatorType_in_tuple_type10382);
					tn=comparatorType();
					state._fsp--;

					 types.add(tn); 
					}
					break;

				default :
					break loop182;
				}
			}

			match(input,179,FOLLOW_179_in_tuple_type10394); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1487:1: username : ( IDENT | STRING_LITERAL | QUOTED_NAME );
	public final CqlParser.username_return username() throws RecognitionException {
		CqlParser.username_return retval = new CqlParser.username_return();
		retval.start = input.LT(1);

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1488:5: ( IDENT | STRING_LITERAL | QUOTED_NAME )
			int alt183=3;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				alt183=1;
				}
				break;
			case STRING_LITERAL:
				{
				alt183=2;
				}
				break;
			case QUOTED_NAME:
				{
				alt183=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 183, 0, input);
				throw nvae;
			}
			switch (alt183) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1488:7: IDENT
					{
					match(input,IDENT,FOLLOW_IDENT_in_username10413); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1489:7: STRING_LITERAL
					{
					match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_username10421); 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1490:7: QUOTED_NAME
					{
					match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_username10429); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1495:1: non_type_ident returns [ColumnIdentifier id] : (t= IDENT |t= QUOTED_NAME |k= basic_unreserved_keyword |kk= K_KEY );
	public final ColumnIdentifier non_type_ident() throws RecognitionException {
		ColumnIdentifier id = null;


		Token t=null;
		Token kk=null;
		String k =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1496:5: (t= IDENT |t= QUOTED_NAME |k= basic_unreserved_keyword |kk= K_KEY )
			int alt184=4;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				alt184=1;
				}
				break;
			case QUOTED_NAME:
				{
				alt184=2;
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
				alt184=3;
				}
				break;
			case K_KEY:
				{
				alt184=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 184, 0, input);
				throw nvae;
			}
			switch (alt184) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1496:7: t= IDENT
					{
					t=(Token)match(input,IDENT,FOLLOW_IDENT_in_non_type_ident10456); 
					 if (reservedTypeNames.contains((t!=null?t.getText():null))) addRecognitionError("Invalid (reserved) user type name " + (t!=null?t.getText():null)); id = new ColumnIdentifier((t!=null?t.getText():null), false); 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1497:7: t= QUOTED_NAME
					{
					t=(Token)match(input,QUOTED_NAME,FOLLOW_QUOTED_NAME_in_non_type_ident10487); 
					 id = new ColumnIdentifier((t!=null?t.getText():null), true); 
					}
					break;
				case 3 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1498:7: k= basic_unreserved_keyword
					{
					pushFollow(FOLLOW_basic_unreserved_keyword_in_non_type_ident10512);
					k=basic_unreserved_keyword();
					state._fsp--;

					 id = new ColumnIdentifier(k, false); 
					}
					break;
				case 4 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1499:7: kk= K_KEY
					{
					kk=(Token)match(input,K_KEY,FOLLOW_K_KEY_in_non_type_ident10524); 
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1502:1: unreserved_keyword returns [String str] : (u= unreserved_function_keyword |k= ( K_TTL | K_COUNT | K_WRITETIME | K_KEY ) );
	public final String unreserved_keyword() throws RecognitionException {
		String str = null;


		Token k=null;
		String u =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1503:5: (u= unreserved_function_keyword |k= ( K_TTL | K_COUNT | K_WRITETIME | K_KEY ) )
			int alt185=2;
			int LA185_0 = input.LA(1);
			if ( ((LA185_0 >= K_AGGREGATE && LA185_0 <= K_ALL)||LA185_0==K_AS||LA185_0==K_ASCII||(LA185_0 >= K_BIGINT && LA185_0 <= K_BOOLEAN)||(LA185_0 >= K_CALLED && LA185_0 <= K_CLUSTERING)||(LA185_0 >= K_COMPACT && LA185_0 <= K_CONTAINS)||LA185_0==K_COUNTER||(LA185_0 >= K_CUSTOM && LA185_0 <= K_DECIMAL)||(LA185_0 >= K_DISTINCT && LA185_0 <= K_DOUBLE)||(LA185_0 >= K_EXISTS && LA185_0 <= K_FLOAT)||LA185_0==K_FROZEN||(LA185_0 >= K_FUNCTION && LA185_0 <= K_FUNCTIONS)||LA185_0==K_INET||(LA185_0 >= K_INITCOND && LA185_0 <= K_INPUT)||LA185_0==K_INT||LA185_0==K_JSON||LA185_0==K_KEYS||(LA185_0 >= K_KEYSPACES && LA185_0 <= K_LANGUAGE)||(LA185_0 >= K_LIST && LA185_0 <= K_MAP)||LA185_0==K_NOLOGIN||LA185_0==K_NOSUPERUSER||LA185_0==K_OPTIONS||(LA185_0 >= K_PASSWORD && LA185_0 <= K_PERMISSIONS)||LA185_0==K_RETURNS||(LA185_0 >= K_ROLE && LA185_0 <= K_ROLES)||(LA185_0 >= K_SFUNC && LA185_0 <= K_TINYINT)||LA185_0==K_TRIGGER||(LA185_0 >= K_TUPLE && LA185_0 <= K_TYPE)||(LA185_0 >= K_USER && LA185_0 <= K_USERS)||(LA185_0 >= K_UUID && LA185_0 <= K_VARINT)) ) {
				alt185=1;
			}
			else if ( (LA185_0==K_COUNT||LA185_0==K_KEY||LA185_0==K_TTL||LA185_0==K_WRITETIME) ) {
				alt185=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 185, 0, input);
				throw nvae;
			}

			switch (alt185) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1503:7: u= unreserved_function_keyword
					{
					pushFollow(FOLLOW_unreserved_function_keyword_in_unreserved_keyword10567);
					u=unreserved_function_keyword();
					state._fsp--;

					 str = u; 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1504:7: k= ( K_TTL | K_COUNT | K_WRITETIME | K_KEY )
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1507:1: unreserved_function_keyword returns [String str] : (u= basic_unreserved_keyword |t= native_type );
	public final String unreserved_function_keyword() throws RecognitionException {
		String str = null;


		String u =null;
		CQL3Type t =null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1508:5: (u= basic_unreserved_keyword |t= native_type )
			int alt186=2;
			int LA186_0 = input.LA(1);
			if ( ((LA186_0 >= K_AGGREGATE && LA186_0 <= K_ALL)||LA186_0==K_AS||(LA186_0 >= K_CALLED && LA186_0 <= K_CLUSTERING)||(LA186_0 >= K_COMPACT && LA186_0 <= K_CONTAINS)||LA186_0==K_CUSTOM||LA186_0==K_DISTINCT||(LA186_0 >= K_EXISTS && LA186_0 <= K_FINALFUNC)||LA186_0==K_FROZEN||(LA186_0 >= K_FUNCTION && LA186_0 <= K_FUNCTIONS)||(LA186_0 >= K_INITCOND && LA186_0 <= K_INPUT)||LA186_0==K_JSON||LA186_0==K_KEYS||(LA186_0 >= K_KEYSPACES && LA186_0 <= K_LANGUAGE)||(LA186_0 >= K_LIST && LA186_0 <= K_MAP)||LA186_0==K_NOLOGIN||LA186_0==K_NOSUPERUSER||LA186_0==K_OPTIONS||(LA186_0 >= K_PASSWORD && LA186_0 <= K_PERMISSIONS)||LA186_0==K_RETURNS||(LA186_0 >= K_ROLE && LA186_0 <= K_ROLES)||LA186_0==K_SFUNC||(LA186_0 >= K_STATIC && LA186_0 <= K_SUPERUSER)||LA186_0==K_TRIGGER||(LA186_0 >= K_TUPLE && LA186_0 <= K_TYPE)||(LA186_0 >= K_USER && LA186_0 <= K_USERS)||LA186_0==K_VALUES) ) {
				alt186=1;
			}
			else if ( (LA186_0==K_ASCII||(LA186_0 >= K_BIGINT && LA186_0 <= K_BOOLEAN)||LA186_0==K_COUNTER||(LA186_0 >= K_DATE && LA186_0 <= K_DECIMAL)||LA186_0==K_DOUBLE||LA186_0==K_FLOAT||LA186_0==K_INET||LA186_0==K_INT||LA186_0==K_SMALLINT||(LA186_0 >= K_TEXT && LA186_0 <= K_TINYINT)||LA186_0==K_UUID||(LA186_0 >= K_VARCHAR && LA186_0 <= K_VARINT)) ) {
				alt186=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 186, 0, input);
				throw nvae;
			}

			switch (alt186) {
				case 1 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1508:7: u= basic_unreserved_keyword
					{
					pushFollow(FOLLOW_basic_unreserved_keyword_in_unreserved_function_keyword10622);
					u=basic_unreserved_keyword();
					state._fsp--;

					 str = u; 
					}
					break;
				case 2 :
					// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1509:7: t= native_type
					{
					pushFollow(FOLLOW_native_type_in_unreserved_function_keyword10634);
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
	// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1512:1: basic_unreserved_keyword returns [String str] : k= ( K_KEYS | K_AS | K_CLUSTERING | K_COMPACT | K_STORAGE | K_TYPE | K_VALUES | K_MAP | K_LIST | K_FILTERING | K_PERMISSION | K_PERMISSIONS | K_KEYSPACES | K_ALL | K_USER | K_USERS | K_ROLE | K_ROLES | K_SUPERUSER | K_NOSUPERUSER | K_LOGIN | K_NOLOGIN | K_OPTIONS | K_PASSWORD | K_EXISTS | K_CUSTOM | K_TRIGGER | K_DISTINCT | K_CONTAINS | K_STATIC | K_FROZEN | K_TUPLE | K_FUNCTION | K_FUNCTIONS | K_AGGREGATE | K_SFUNC | K_STYPE | K_FINALFUNC | K_INITCOND | K_RETURNS | K_LANGUAGE | K_JSON | K_CALLED | K_INPUT ) ;
	public final String basic_unreserved_keyword() throws RecognitionException {
		String str = null;


		Token k=null;

		try {
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1513:5: (k= ( K_KEYS | K_AS | K_CLUSTERING | K_COMPACT | K_STORAGE | K_TYPE | K_VALUES | K_MAP | K_LIST | K_FILTERING | K_PERMISSION | K_PERMISSIONS | K_KEYSPACES | K_ALL | K_USER | K_USERS | K_ROLE | K_ROLES | K_SUPERUSER | K_NOSUPERUSER | K_LOGIN | K_NOLOGIN | K_OPTIONS | K_PASSWORD | K_EXISTS | K_CUSTOM | K_TRIGGER | K_DISTINCT | K_CONTAINS | K_STATIC | K_FROZEN | K_TUPLE | K_FUNCTION | K_FUNCTIONS | K_AGGREGATE | K_SFUNC | K_STYPE | K_FINALFUNC | K_INITCOND | K_RETURNS | K_LANGUAGE | K_JSON | K_CALLED | K_INPUT ) )
			// /Users/vroyer/git/cassandra/src/java/org/apache/cassandra/cql3/Cql.g:1513:7: k= ( K_KEYS | K_AS | K_CLUSTERING | K_COMPACT | K_STORAGE | K_TYPE | K_VALUES | K_MAP | K_LIST | K_FILTERING | K_PERMISSION | K_PERMISSIONS | K_KEYSPACES | K_ALL | K_USER | K_USERS | K_ROLE | K_ROLES | K_SUPERUSER | K_NOSUPERUSER | K_LOGIN | K_NOLOGIN | K_OPTIONS | K_PASSWORD | K_EXISTS | K_CUSTOM | K_TRIGGER | K_DISTINCT | K_CONTAINS | K_STATIC | K_FROZEN | K_TUPLE | K_FUNCTION | K_FUNCTIONS | K_AGGREGATE | K_SFUNC | K_STYPE | K_FINALFUNC | K_INITCOND | K_RETURNS | K_LANGUAGE | K_JSON | K_CALLED | K_INPUT )
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
	protected DFA40 dfa40 = new DFA40(this);
	protected DFA130 dfa130 = new DFA130(this);
	protected DFA131 dfa131 = new DFA131(this);
	protected DFA149 dfa149 = new DFA149(this);
	protected DFA151 dfa151 = new DFA151(this);
	protected DFA153 dfa153 = new DFA153(this);
	protected DFA155 dfa155 = new DFA155(this);
	protected DFA158 dfa158 = new DFA158(this);
	protected DFA164 dfa164 = new DFA164(this);
	protected DFA170 dfa170 = new DFA170(this);
	protected DFA169 dfa169 = new DFA169(this);
	protected DFA179 dfa179 = new DFA179(this);
	static final String DFA2_eotS =
		"\60\uffff";
	static final String DFA2_eofS =
		"\60\uffff";
	static final String DFA2_minS =
		"\1\34\7\uffff\2\31\1\53\2\24\1\32\7\uffff\1\153\20\uffff\1\142\2\uffff"+
		"\1\100\5\uffff\1\31";
	static final String DFA2_maxS =
		"\1\u0086\7\uffff\3\u0087\2\u009d\1\u0088\7\uffff\1\153\20\uffff\1\175"+
		"\2\uffff\1\150\5\uffff\1\103";
	static final String DFA2_acceptS =
		"\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\6\uffff\1\10\1\11\1\23\1\27\1\31"+
		"\1\40\1\12\1\uffff\1\34\1\36\1\13\1\14\1\15\1\25\1\30\1\33\1\35\1\37\1"+
		"\42\1\16\1\17\1\24\1\32\1\41\1\uffff\1\20\1\44\1\uffff\1\21\1\45\1\26"+
		"\1\43\1\22\1\uffff";
	static final String DFA2_specialS =
		"\60\uffff}>";
	static final String[] DFA2_transitionS = {
			"\1\12\7\uffff\1\4\13\uffff\1\10\3\uffff\1\5\4\uffff\1\11\13\uffff\1\13"+
			"\7\uffff\1\2\11\uffff\1\15\25\uffff\1\14\2\uffff\1\1\17\uffff\1\7\4\uffff"+
			"\1\3\1\6",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\27\21\uffff\1\17\5\uffff\1\24\21\uffff\1\26\4\uffff\1\24\12\uffff"+
			"\1\16\20\uffff\1\25\11\uffff\1\23\20\uffff\1\21\3\uffff\1\22\3\uffff"+
			"\1\20",
			"\1\37\21\uffff\1\31\27\uffff\1\36\4\uffff\1\32\12\uffff\1\30\32\uffff"+
			"\1\40\20\uffff\1\34\3\uffff\1\35\3\uffff\1\33",
			"\1\41\47\uffff\1\42\32\uffff\1\45\24\uffff\1\44\3\uffff\1\43",
			"\1\50\4\uffff\1\50\1\46\1\uffff\1\47\2\uffff\1\50\1\uffff\1\50\1\47"+
			"\2\uffff\3\50\1\uffff\2\50\1\uffff\4\50\1\47\3\50\2\uffff\1\47\2\50\1"+
			"\47\1\uffff\1\47\4\50\1\uffff\1\50\1\uffff\2\50\4\uffff\1\50\1\uffff"+
			"\2\50\1\uffff\1\50\1\uffff\3\50\1\uffff\2\50\1\uffff\3\50\1\47\1\uffff"+
			"\1\50\1\uffff\1\50\4\uffff\1\50\2\uffff\3\50\3\uffff\1\50\1\uffff\2\50"+
			"\1\47\1\uffff\13\50\2\uffff\1\50\1\uffff\3\50\3\uffff\2\50\1\uffff\4"+
			"\50\2\uffff\1\50\10\uffff\2\50\2\uffff\1\50",
			"\1\53\4\uffff\1\53\1\51\1\uffff\1\52\2\uffff\1\53\1\uffff\1\53\1\52"+
			"\2\uffff\3\53\1\uffff\2\53\1\uffff\4\53\1\52\3\53\2\uffff\1\52\2\53\1"+
			"\52\1\uffff\1\52\4\53\1\uffff\1\53\1\uffff\2\53\4\uffff\1\53\1\uffff"+
			"\2\53\1\uffff\1\53\1\uffff\3\53\1\uffff\2\53\1\uffff\3\53\1\52\1\uffff"+
			"\1\53\1\uffff\1\53\4\uffff\1\53\2\uffff\3\53\3\uffff\1\53\1\uffff\2\53"+
			"\1\52\1\uffff\13\53\2\uffff\1\53\1\uffff\3\53\3\uffff\2\53\1\uffff\4"+
			"\53\2\uffff\1\53\10\uffff\2\53\2\uffff\1\53",
			"\1\56\1\uffff\1\56\5\uffff\1\56\15\uffff\1\56\5\uffff\1\56\2\uffff\1"+
			"\56\1\uffff\1\56\36\uffff\1\56\24\uffff\1\55\1\56\27\uffff\1\54",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\57",
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
			"\1\47\5\uffff\1\47\24\uffff\1\50",
			"",
			"",
			"\1\53\41\uffff\1\52\5\uffff\1\52",
			"",
			"",
			"",
			"",
			"",
			"\1\27\51\uffff\1\26"
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
			return "234:1: cqlStatement returns [ParsedStatement stmt] : (st1= selectStatement |st2= insertStatement |st3= updateStatement |st4= batchStatement |st5= deleteStatement |st6= useStatement |st7= truncateStatement |st8= createKeyspaceStatement |st9= createTableStatement |st10= createIndexStatement |st11= dropKeyspaceStatement |st12= dropTableStatement |st13= dropIndexStatement |st14= alterTableStatement |st15= alterKeyspaceStatement |st16= grantPermissionsStatement |st17= revokePermissionsStatement |st18= listPermissionsStatement |st19= createUserStatement |st20= alterUserStatement |st21= dropUserStatement |st22= listUsersStatement |st23= createTriggerStatement |st24= dropTriggerStatement |st25= createTypeStatement |st26= alterTypeStatement |st27= dropTypeStatement |st28= createFunctionStatement |st29= dropFunctionStatement |st30= createAggregateStatement |st31= dropAggregateStatement |st32= createRoleStatement |st33= alterRoleStatement |st34= dropRoleStatement |st35= listRolesStatement |st36= grantRoleStatement |st37= revokeRoleStatement );";
		}
	}

	static final String DFA13_eotS =
		"\73\uffff";
	static final String DFA13_eofS =
		"\73\uffff";
	static final String DFA13_minS =
		"\1\24\33\37\1\uffff\1\24\1\uffff\1\24\2\uffff\30\37\1\uffff";
	static final String DFA13_maxS =
		"\1\u009a\33\u00ad\1\uffff\1\u009a\1\uffff\1\u00b6\2\uffff\30\u00ad\1\uffff";
	static final String DFA13_acceptS =
		"\34\uffff\1\5\1\uffff\1\1\1\uffff\1\3\1\4\30\uffff\1\2";
	static final String DFA13_specialS =
		"\73\uffff}>";
	static final String[] DFA13_transitionS = {
			"\1\1\4\uffff\2\3\4\uffff\1\3\1\uffff\1\4\3\uffff\1\5\1\6\1\7\1\uffff"+
			"\2\3\1\uffff\2\3\1\30\1\10\1\uffff\1\3\1\26\1\11\3\uffff\1\3\1\12\3\uffff"+
			"\3\3\1\13\1\uffff\1\3\1\uffff\2\3\4\uffff\1\14\1\uffff\2\3\1\uffff\1"+
			"\15\1\uffff\1\3\1\33\1\3\1\uffff\2\3\1\uffff\3\3\2\uffff\1\3\1\uffff"+
			"\1\3\4\uffff\1\3\2\uffff\3\3\3\uffff\1\3\1\uffff\2\3\2\uffff\1\3\1\16"+
			"\4\3\1\17\1\27\1\20\1\25\1\21\1\uffff\1\34\1\3\1\uffff\1\32\2\3\3\uffff"+
			"\2\3\1\uffff\1\22\1\3\1\23\1\24\2\uffff\1\31\10\uffff\1\34\1\2",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\37\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\40\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\147\uffff\1\41\1\36\1\uffff\1\36\1\uffff\1\35",
			"\1\36\40\uffff\1\36\150\uffff\1\36\1\uffff\1\36\1\uffff\1\35",
			"",
			"\1\42\4\uffff\2\44\4\uffff\1\44\1\uffff\1\45\3\uffff\1\46\1\47\1\50"+
			"\1\uffff\2\44\1\uffff\2\44\1\71\1\51\1\uffff\1\44\1\67\1\52\3\uffff\1"+
			"\44\1\53\3\uffff\3\44\1\54\1\uffff\1\44\1\uffff\2\44\4\uffff\1\55\1\uffff"+
			"\2\44\1\uffff\1\56\1\uffff\1\44\1\36\1\44\1\uffff\2\44\1\uffff\3\44\2"+
			"\uffff\1\44\1\uffff\1\44\4\uffff\1\44\2\uffff\3\44\3\uffff\1\44\1\uffff"+
			"\2\44\2\uffff\1\44\1\57\4\44\1\60\1\70\1\61\1\66\1\62\1\uffff\1\34\1"+
			"\44\1\uffff\1\36\2\44\3\uffff\2\44\1\uffff\1\63\1\44\1\64\1\65\2\uffff"+
			"\1\36\11\uffff\1\43",
			"",
			"\1\34\1\72\3\uffff\2\34\4\uffff\1\34\1\uffff\1\34\3\uffff\3\34\1\uffff"+
			"\2\34\1\uffff\4\34\1\uffff\3\34\3\uffff\2\34\3\uffff\4\34\1\uffff\1\34"+
			"\1\uffff\2\34\4\uffff\1\34\1\uffff\2\34\1\uffff\1\34\1\uffff\3\34\1\uffff"+
			"\2\34\1\uffff\3\34\2\uffff\1\34\1\uffff\1\34\4\uffff\1\34\2\uffff\3\34"+
			"\3\uffff\1\34\1\uffff\2\34\2\uffff\13\34\1\uffff\2\34\1\uffff\3\34\3"+
			"\uffff\2\34\1\uffff\4\34\2\uffff\1\34\10\uffff\2\34\16\uffff\1\34\14"+
			"\uffff\1\72",
			"",
			"",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
			"\1\36\40\uffff\1\36\147\uffff\1\34\1\36\1\uffff\1\36\1\uffff\1\36",
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
			return "325:8: (c= cident | K_COUNT '(' countArgument ')' | K_WRITETIME '(' c= cident ')' | K_TTL '(' c= cident ')' |f= functionName args= selectionFunctionArgs )";
		}
	}

	static final String DFA40_eotS =
		"\33\uffff";
	static final String DFA40_eofS =
		"\33\uffff";
	static final String DFA40_minS =
		"\1\24\30\100\2\uffff";
	static final String DFA40_maxS =
		"\1\u009a\30\u00b5\2\uffff";
	static final String DFA40_acceptS =
		"\31\uffff\1\1\1\2";
	static final String DFA40_specialS =
		"\33\uffff}>";
	static final String[] DFA40_transitionS = {
			"\1\1\4\uffff\2\3\4\uffff\1\3\1\uffff\1\4\3\uffff\1\5\1\6\1\7\1\uffff"+
			"\2\3\1\uffff\2\3\1\30\1\10\1\uffff\1\3\1\26\1\11\3\uffff\1\3\1\12\3\uffff"+
			"\3\3\1\13\1\uffff\1\3\1\uffff\2\3\4\uffff\1\14\1\uffff\2\3\1\uffff\1"+
			"\15\1\uffff\1\3\1\30\1\3\1\uffff\2\3\1\uffff\3\3\2\uffff\1\3\1\uffff"+
			"\1\3\4\uffff\1\3\2\uffff\3\3\3\uffff\1\3\1\uffff\2\3\2\uffff\1\3\1\16"+
			"\4\3\1\17\1\27\1\20\1\25\1\21\2\uffff\1\3\1\uffff\1\30\2\3\3\uffff\2"+
			"\3\1\uffff\1\22\1\3\1\23\1\24\2\uffff\1\30\11\uffff\1\2",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"\1\31\152\uffff\1\31\11\uffff\1\32",
			"",
			""
	};

	static final short[] DFA40_eot = DFA.unpackEncodedString(DFA40_eotS);
	static final short[] DFA40_eof = DFA.unpackEncodedString(DFA40_eofS);
	static final char[] DFA40_min = DFA.unpackEncodedStringToUnsignedChars(DFA40_minS);
	static final char[] DFA40_max = DFA.unpackEncodedStringToUnsignedChars(DFA40_maxS);
	static final short[] DFA40_accept = DFA.unpackEncodedString(DFA40_acceptS);
	static final short[] DFA40_special = DFA.unpackEncodedString(DFA40_specialS);
	static final short[][] DFA40_transition;

	static {
		int numStates = DFA40_transitionS.length;
		DFA40_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA40_transition[i] = DFA.unpackEncodedString(DFA40_transitionS[i]);
		}
	}

	protected class DFA40 extends DFA {

		public DFA40(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 40;
			this.eot = DFA40_eot;
			this.eof = DFA40_eof;
			this.min = DFA40_min;
			this.max = DFA40_max;
			this.accept = DFA40_accept;
			this.special = DFA40_special;
			this.transition = DFA40_transition;
		}
		@Override
		public String getDescription() {
			return "483:1: deleteOp returns [Operation.RawDeletion op] : (c= cident |c= cident '[' t= term ']' );";
		}
	}

	static final String DFA130_eotS =
		"\34\uffff";
	static final String DFA130_eofS =
		"\1\uffff\31\33\2\uffff";
	static final String DFA130_minS =
		"\1\24\31\u00ad\2\uffff";
	static final String DFA130_maxS =
		"\1\u009a\31\u00af\2\uffff";
	static final String DFA130_acceptS =
		"\32\uffff\1\1\1\2";
	static final String DFA130_specialS =
		"\34\uffff}>";
	static final String[] DFA130_transitionS = {
			"\1\1\4\uffff\2\3\4\uffff\1\3\1\uffff\1\4\3\uffff\1\5\1\6\1\7\1\uffff"+
			"\2\3\1\uffff\2\3\1\30\1\10\1\uffff\1\3\1\26\1\11\3\uffff\1\3\1\12\3\uffff"+
			"\3\3\1\13\1\uffff\1\3\1\uffff\2\3\4\uffff\1\14\1\uffff\2\3\1\uffff\1"+
			"\15\1\uffff\1\3\1\30\1\3\1\uffff\2\3\1\uffff\3\3\2\uffff\1\3\1\uffff"+
			"\1\3\4\uffff\1\3\2\uffff\3\3\3\uffff\1\3\1\uffff\2\3\2\uffff\1\3\1\16"+
			"\4\3\1\17\1\27\1\20\1\25\1\21\2\uffff\1\3\1\uffff\1\30\2\3\3\uffff\2"+
			"\3\1\uffff\1\22\1\3\1\23\1\24\2\uffff\1\30\10\uffff\1\31\1\2",
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

	static final short[] DFA130_eot = DFA.unpackEncodedString(DFA130_eotS);
	static final short[] DFA130_eof = DFA.unpackEncodedString(DFA130_eofS);
	static final char[] DFA130_min = DFA.unpackEncodedStringToUnsignedChars(DFA130_minS);
	static final char[] DFA130_max = DFA.unpackEncodedStringToUnsignedChars(DFA130_maxS);
	static final short[] DFA130_accept = DFA.unpackEncodedString(DFA130_acceptS);
	static final short[] DFA130_special = DFA.unpackEncodedString(DFA130_specialS);
	static final short[][] DFA130_transition;

	static {
		int numStates = DFA130_transitionS.length;
		DFA130_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA130_transition[i] = DFA.unpackEncodedString(DFA130_transitionS[i]);
		}
	}

	protected class DFA130 extends DFA {

		public DFA130(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 130;
			this.eot = DFA130_eot;
			this.eof = DFA130_eof;
			this.min = DFA130_min;
			this.max = DFA130_max;
			this.accept = DFA130_accept;
			this.special = DFA130_special;
			this.transition = DFA130_transition;
		}
		@Override
		public String getDescription() {
			return "1125:7: ( ksName[name] '.' )?";
		}
	}

	static final String DFA131_eotS =
		"\34\uffff";
	static final String DFA131_eofS =
		"\1\uffff\31\33\2\uffff";
	static final String DFA131_minS =
		"\1\24\31\30\2\uffff";
	static final String DFA131_maxS =
		"\1\u009a\31\u00af\2\uffff";
	static final String DFA131_acceptS =
		"\32\uffff\1\1\1\2";
	static final String DFA131_specialS =
		"\34\uffff}>";
	static final String[] DFA131_transitionS = {
			"\1\1\4\uffff\2\3\4\uffff\1\3\1\uffff\1\4\3\uffff\1\5\1\6\1\7\1\uffff"+
			"\2\3\1\uffff\2\3\1\30\1\10\1\uffff\1\3\1\26\1\11\3\uffff\1\3\1\12\3\uffff"+
			"\3\3\1\13\1\uffff\1\3\1\uffff\2\3\4\uffff\1\14\1\uffff\2\3\1\uffff\1"+
			"\15\1\uffff\1\3\1\30\1\3\1\uffff\2\3\1\uffff\3\3\2\uffff\1\3\1\uffff"+
			"\1\3\4\uffff\1\3\2\uffff\3\3\3\uffff\1\3\1\uffff\2\3\2\uffff\1\3\1\16"+
			"\4\3\1\17\1\27\1\20\1\25\1\21\2\uffff\1\3\1\uffff\1\30\2\3\3\uffff\2"+
			"\3\1\uffff\1\22\1\3\1\23\1\24\2\uffff\1\30\10\uffff\1\31\1\2",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"\1\33\2\uffff\2\33\34\uffff\1\33\6\uffff\1\33\17\uffff\1\33\5\uffff"+
			"\1\33\6\uffff\1\33\3\uffff\1\33\3\uffff\1\33\4\uffff\1\33\6\uffff\1\33"+
			"\13\uffff\1\33\13\uffff\1\33\4\uffff\2\33\30\uffff\1\33\4\uffff\1\32"+
			"\1\uffff\1\33",
			"",
			""
	};

	static final short[] DFA131_eot = DFA.unpackEncodedString(DFA131_eotS);
	static final short[] DFA131_eof = DFA.unpackEncodedString(DFA131_eofS);
	static final char[] DFA131_min = DFA.unpackEncodedStringToUnsignedChars(DFA131_minS);
	static final char[] DFA131_max = DFA.unpackEncodedStringToUnsignedChars(DFA131_maxS);
	static final short[] DFA131_accept = DFA.unpackEncodedString(DFA131_acceptS);
	static final short[] DFA131_special = DFA.unpackEncodedString(DFA131_specialS);
	static final short[][] DFA131_transition;

	static {
		int numStates = DFA131_transitionS.length;
		DFA131_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA131_transition[i] = DFA.unpackEncodedString(DFA131_transitionS[i]);
		}
	}

	protected class DFA131 extends DFA {

		public DFA131(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 131;
			this.eot = DFA131_eot;
			this.eof = DFA131_eof;
			this.min = DFA131_min;
			this.max = DFA131_max;
			this.accept = DFA131_accept;
			this.special = DFA131_special;
			this.transition = DFA131_transition;
		}
		@Override
		public String getDescription() {
			return "1130:7: ( ksName[name] '.' )?";
		}
	}

	static final String DFA149_eotS =
		"\42\uffff";
	static final String DFA149_eofS =
		"\42\uffff";
	static final String DFA149_minS =
		"\1\6\2\uffff\1\6\4\uffff\30\u00a8\1\u00ad\1\uffff";
	static final String DFA149_maxS =
		"\1\u00b8\2\uffff\1\u00b9\4\uffff\31\u00ae\1\uffff";
	static final String DFA149_acceptS =
		"\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\7\31\uffff\1\3";
	static final String DFA149_specialS =
		"\42\uffff}>";
	static final String[] DFA149_transitionS = {
			"\1\1\7\uffff\1\1\3\uffff\1\1\2\uffff\1\1\64\uffff\1\1\20\uffff\1\1\4"+
			"\uffff\1\5\70\uffff\1\7\3\uffff\1\1\2\uffff\1\1\7\uffff\1\4\3\uffff\1"+
			"\1\1\uffff\1\6\6\uffff\1\2\2\uffff\1\3",
			"",
			"",
			"\1\2\7\uffff\1\2\3\uffff\1\2\1\uffff\1\10\1\2\3\uffff\2\12\4\uffff\1"+
			"\12\1\uffff\1\13\3\uffff\1\14\1\15\1\16\1\uffff\2\12\1\uffff\2\12\1\37"+
			"\1\17\1\uffff\1\12\1\35\1\20\3\uffff\1\12\1\21\3\uffff\3\12\1\22\1\uffff"+
			"\1\12\1\uffff\2\12\4\uffff\1\23\1\2\2\12\1\uffff\1\24\1\uffff\1\12\1"+
			"\40\1\12\1\uffff\2\12\1\uffff\3\12\1\uffff\1\2\1\12\1\uffff\1\12\1\uffff"+
			"\1\2\2\uffff\1\12\2\uffff\3\12\3\uffff\1\12\1\uffff\2\12\2\uffff\1\12"+
			"\1\25\4\12\1\26\1\36\1\27\1\34\1\30\1\uffff\1\2\1\12\1\uffff\1\40\2\12"+
			"\3\uffff\2\12\1\uffff\1\31\1\12\1\32\1\33\2\uffff\1\40\10\uffff\1\2\1"+
			"\11\2\uffff\1\2\2\uffff\1\2\7\uffff\1\2\3\uffff\1\2\1\uffff\1\2\6\uffff"+
			"\1\2\2\uffff\2\2",
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

	static final short[] DFA149_eot = DFA.unpackEncodedString(DFA149_eotS);
	static final short[] DFA149_eof = DFA.unpackEncodedString(DFA149_eofS);
	static final char[] DFA149_min = DFA.unpackEncodedStringToUnsignedChars(DFA149_minS);
	static final char[] DFA149_max = DFA.unpackEncodedStringToUnsignedChars(DFA149_maxS);
	static final short[] DFA149_accept = DFA.unpackEncodedString(DFA149_acceptS);
	static final short[] DFA149_special = DFA.unpackEncodedString(DFA149_specialS);
	static final short[][] DFA149_transition;

	static {
		int numStates = DFA149_transitionS.length;
		DFA149_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA149_transition[i] = DFA.unpackEncodedString(DFA149_transitionS[i]);
		}
	}

	protected class DFA149 extends DFA {

		public DFA149(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 149;
			this.eot = DFA149_eot;
			this.eof = DFA149_eof;
			this.min = DFA149_min;
			this.max = DFA149_max;
			this.accept = DFA149_accept;
			this.special = DFA149_special;
			this.transition = DFA149_transition;
		}
		@Override
		public String getDescription() {
			return "1219:1: value returns [Term.Raw value] : (c= constant |l= collectionLiteral |u= usertypeLiteral |t= tupleLiteral | K_NULL | ':' id= ident | QMARK );";
		}
	}

	static final String DFA151_eotS =
		"\33\uffff";
	static final String DFA151_eofS =
		"\1\uffff\30\32\2\uffff";
	static final String DFA151_minS =
		"\1\24\30\u00a8\2\uffff";
	static final String DFA151_maxS =
		"\1\u009a\30\u00af\2\uffff";
	static final String DFA151_acceptS =
		"\31\uffff\1\1\1\2";
	static final String DFA151_specialS =
		"\33\uffff}>";
	static final String[] DFA151_transitionS = {
			"\1\1\4\uffff\2\3\4\uffff\1\3\1\uffff\1\4\3\uffff\1\5\1\6\1\7\1\uffff"+
			"\2\3\1\uffff\2\3\1\30\1\10\1\uffff\1\3\1\26\1\11\3\uffff\1\3\1\12\3\uffff"+
			"\3\3\1\13\1\uffff\1\3\1\uffff\2\3\4\uffff\1\14\1\uffff\2\3\1\uffff\1"+
			"\15\1\uffff\1\3\1\31\1\3\1\uffff\2\3\1\uffff\3\3\2\uffff\1\3\1\uffff"+
			"\1\3\4\uffff\1\3\2\uffff\3\3\3\uffff\1\3\1\uffff\2\3\2\uffff\1\3\1\16"+
			"\4\3\1\17\1\27\1\20\1\25\1\21\1\uffff\1\32\1\3\1\uffff\1\31\2\3\3\uffff"+
			"\2\3\1\uffff\1\22\1\3\1\23\1\24\2\uffff\1\31\10\uffff\1\31\1\2",
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

	static final short[] DFA151_eot = DFA.unpackEncodedString(DFA151_eotS);
	static final short[] DFA151_eof = DFA.unpackEncodedString(DFA151_eofS);
	static final char[] DFA151_min = DFA.unpackEncodedStringToUnsignedChars(DFA151_minS);
	static final char[] DFA151_max = DFA.unpackEncodedStringToUnsignedChars(DFA151_maxS);
	static final short[] DFA151_accept = DFA.unpackEncodedString(DFA151_acceptS);
	static final short[] DFA151_special = DFA.unpackEncodedString(DFA151_specialS);
	static final short[][] DFA151_transition;

	static {
		int numStates = DFA151_transitionS.length;
		DFA151_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA151_transition[i] = DFA.unpackEncodedString(DFA151_transitionS[i]);
		}
	}

	protected class DFA151 extends DFA {

		public DFA151(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 151;
			this.eot = DFA151_eot;
			this.eof = DFA151_eof;
			this.min = DFA151_min;
			this.max = DFA151_max;
			this.accept = DFA151_accept;
			this.special = DFA151_special;
			this.transition = DFA151_transition;
		}
		@Override
		public String getDescription() {
			return "1237:7: (ks= keyspaceName '.' )?";
		}
	}

	static final String DFA153_eotS =
		"\70\uffff";
	static final String DFA153_eofS =
		"\70\uffff";
	static final String DFA153_minS =
		"\1\24\30\u00a8\1\u00ad\1\u00a8\1\u00ad\1\24\1\6\30\u00a8\2\uffff";
	static final String DFA153_maxS =
		"\1\u009a\31\u00ad\1\u00a8\1\u00ad\1\u009a\1\u00b8\30\u00a8\2\uffff";
	static final String DFA153_acceptS =
		"\66\uffff\1\1\1\2";
	static final String DFA153_specialS =
		"\70\uffff}>";
	static final String[] DFA153_transitionS = {
			"\1\1\4\uffff\2\3\4\uffff\1\3\1\uffff\1\4\3\uffff\1\5\1\6\1\7\1\uffff"+
			"\2\3\1\uffff\2\3\1\30\1\10\1\uffff\1\3\1\26\1\11\3\uffff\1\3\1\12\3\uffff"+
			"\3\3\1\13\1\uffff\1\3\1\uffff\2\3\4\uffff\1\14\1\uffff\2\3\1\uffff\1"+
			"\15\1\uffff\1\3\1\33\1\3\1\uffff\2\3\1\uffff\3\3\2\uffff\1\3\1\uffff"+
			"\1\3\4\uffff\1\3\2\uffff\3\3\3\uffff\1\3\1\uffff\2\3\2\uffff\1\3\1\16"+
			"\4\3\1\17\1\27\1\20\1\25\1\21\1\uffff\1\32\1\3\1\uffff\1\33\2\3\3\uffff"+
			"\2\3\1\uffff\1\22\1\3\1\23\1\24\2\uffff\1\33\10\uffff\1\31\1\2",
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
			"\2\40\1\uffff\1\52\1\uffff\1\40\1\uffff\1\40\1\uffff\2\40\1\uffff\3\40"+
			"\2\uffff\1\40\1\uffff\1\40\4\uffff\1\40\2\uffff\3\40\3\uffff\1\40\1\uffff"+
			"\2\40\2\uffff\1\40\1\53\4\40\1\54\1\64\1\55\1\62\1\56\1\uffff\1\32\1"+
			"\40\2\uffff\2\40\3\uffff\2\40\1\uffff\1\57\1\40\1\60\1\61\14\uffff\1"+
			"\37",
			"\1\67\7\uffff\1\67\3\uffff\1\67\1\uffff\2\67\3\uffff\2\67\4\uffff\1"+
			"\67\1\uffff\1\67\3\uffff\3\67\1\uffff\2\67\1\uffff\4\67\1\uffff\3\67"+
			"\3\uffff\2\67\3\uffff\4\67\1\uffff\1\67\1\uffff\2\67\4\uffff\4\67\1\uffff"+
			"\1\67\1\uffff\3\67\1\uffff\2\67\1\uffff\3\67\1\uffff\2\67\1\uffff\1\67"+
			"\1\uffff\1\67\2\uffff\1\67\2\uffff\3\67\3\uffff\1\67\1\uffff\2\67\2\uffff"+
			"\13\67\1\uffff\2\67\1\uffff\3\67\3\uffff\2\67\1\uffff\4\67\2\uffff\1"+
			"\67\10\uffff\2\67\2\uffff\1\67\2\uffff\1\67\7\uffff\1\67\1\66\2\uffff"+
			"\1\67\1\uffff\1\67\6\uffff\1\67\2\uffff\1\67",
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

	static final short[] DFA153_eot = DFA.unpackEncodedString(DFA153_eotS);
	static final short[] DFA153_eof = DFA.unpackEncodedString(DFA153_eofS);
	static final char[] DFA153_min = DFA.unpackEncodedStringToUnsignedChars(DFA153_minS);
	static final char[] DFA153_max = DFA.unpackEncodedStringToUnsignedChars(DFA153_maxS);
	static final short[] DFA153_accept = DFA.unpackEncodedString(DFA153_acceptS);
	static final short[] DFA153_special = DFA.unpackEncodedString(DFA153_specialS);
	static final short[][] DFA153_transition;

	static {
		int numStates = DFA153_transitionS.length;
		DFA153_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA153_transition[i] = DFA.unpackEncodedString(DFA153_transitionS[i]);
		}
	}

	protected class DFA153 extends DFA {

		public DFA153(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 153;
			this.eot = DFA153_eot;
			this.eof = DFA153_eof;
			this.min = DFA153_min;
			this.max = DFA153_max;
			this.accept = DFA153_accept;
			this.special = DFA153_special;
			this.transition = DFA153_transition;
		}
		@Override
		public String getDescription() {
			return "1248:1: function returns [Term.Raw t] : (f= functionName '(' ')' |f= functionName '(' args= functionArgs ')' );";
		}
	}

	static final String DFA155_eotS =
		"\104\uffff";
	static final String DFA155_eofS =
		"\3\uffff\1\1\41\uffff\1\1\5\uffff\31\41";
	static final String DFA155_minS =
		"\1\6\1\uffff\1\6\1\33\1\uffff\1\u00a9\30\u00a8\1\u00a9\2\u00a8\1\uffff"+
		"\1\u00a8\1\u00ad\1\u00a8\1\6\1\24\1\6\3\u00a8\31\33";
	static final String DFA155_maxS =
		"\1\u00b8\1\uffff\1\u00b8\1\u00b9\1\uffff\1\u00ab\2\u00ad\1\u00b0\26\u00ad"+
		"\2\u00b0\1\uffff\1\u00b0\2\u00ad\1\u00b9\1\u009a\1\u00b8\3\u00a9\31\u00b9";
	static final String DFA155_acceptS =
		"\1\uffff\1\1\2\uffff\1\2\34\uffff\1\3\42\uffff";
	static final String DFA155_specialS =
		"\104\uffff}>";
	static final String[] DFA155_transitionS = {
			"\1\1\7\uffff\1\1\3\uffff\1\1\1\uffff\1\4\1\1\3\uffff\2\4\4\uffff\1\4"+
			"\1\uffff\1\4\3\uffff\3\4\1\uffff\2\4\1\uffff\4\4\1\uffff\3\4\3\uffff"+
			"\2\4\3\uffff\4\4\1\uffff\1\4\1\uffff\2\4\4\uffff\1\4\1\1\2\4\1\uffff"+
			"\1\4\1\uffff\3\4\1\uffff\2\4\1\uffff\3\4\1\uffff\1\1\1\4\1\uffff\1\4"+
			"\1\uffff\1\1\2\uffff\1\4\2\uffff\3\4\3\uffff\1\4\1\uffff\2\4\2\uffff"+
			"\13\4\1\uffff\2\4\1\uffff\3\4\3\uffff\2\4\1\uffff\4\4\2\uffff\1\4\10"+
			"\uffff\1\3\1\4\2\uffff\1\1\2\uffff\1\1\7\uffff\1\2\3\uffff\1\1\1\uffff"+
			"\1\1\6\uffff\1\1\2\uffff\1\1",
			"",
			"\1\1\7\uffff\1\1\3\uffff\1\1\1\uffff\1\6\1\1\3\uffff\2\44\4\uffff\1"+
			"\44\1\uffff\1\11\3\uffff\1\12\1\13\1\14\1\uffff\2\44\1\uffff\2\44\1\35"+
			"\1\15\1\uffff\1\44\1\33\1\16\3\uffff\1\44\1\17\3\uffff\3\44\1\20\1\uffff"+
			"\1\42\1\uffff\2\44\4\uffff\1\21\1\1\2\44\1\uffff\1\22\1\uffff\1\44\1"+
			"\36\1\44\1\uffff\2\44\1\uffff\1\37\1\44\1\10\1\uffff\1\1\1\44\1\uffff"+
			"\1\44\1\uffff\1\1\2\uffff\1\44\2\uffff\3\44\3\uffff\1\44\1\uffff\2\44"+
			"\1\uffff\1\41\1\44\1\23\4\44\1\24\1\34\1\25\1\32\1\26\1\uffff\1\1\1\44"+
			"\1\uffff\1\43\1\40\1\44\3\uffff\2\44\1\uffff\1\27\1\44\1\30\1\31\2\uffff"+
			"\1\43\10\uffff\1\1\1\7\2\uffff\1\5\2\uffff\1\1\7\uffff\1\1\3\uffff\1"+
			"\1\1\uffff\1\1\6\uffff\1\1\2\uffff\1\1",
			"\1\1\1\uffff\2\1\25\uffff\1\1\21\uffff\1\1\6\uffff\1\1\10\uffff\1\1"+
			"\16\uffff\1\1\37\uffff\1\1\10\uffff\1\1\32\uffff\3\1\1\uffff\1\4\2\1"+
			"\7\uffff\1\1\1\uffff\1\1",
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
			"\1\1\2\uffff\4\41\1\1\1\41\1\uffff\3\41\1\uffff\2\41\1\1\3\41\1\uffff"+
			"\2\41\1\uffff\1\41\1\uffff\1\41\2\uffff\1\41\1\uffff\1\1\3\41\3\uffff"+
			"\1\41\1\uffff\2\41\2\uffff\13\41\1\uffff\2\41\1\uffff\3\41\1\uffff\1"+
			"\1\1\uffff\2\41\1\uffff\4\41\1\1\1\uffff\1\41\10\uffff\2\41\2\uffff\1"+
			"\41\2\uffff\1\41\7\uffff\1\41\3\1\1\41\1\uffff\1\47\1\1\5\uffff\1\41"+
			"\1\uffff\1\1\1\41\1\1",
			"\1\50\4\uffff\2\52\4\uffff\1\52\1\uffff\1\1\3\uffff\3\1\1\uffff\2\52"+
			"\1\uffff\2\52\2\1\1\uffff\1\52\2\1\3\uffff\1\52\1\1\3\uffff\3\52\1\1"+
			"\1\uffff\1\52\1\uffff\2\52\4\uffff\1\1\1\uffff\2\52\1\uffff\1\1\1\uffff"+
			"\1\52\1\41\1\52\1\uffff\2\52\1\uffff\3\52\2\uffff\1\52\1\uffff\1\52\4"+
			"\uffff\1\52\2\uffff\3\52\3\uffff\1\52\1\uffff\2\52\2\uffff\1\52\1\1\4"+
			"\52\5\1\1\uffff\1\1\1\52\2\uffff\2\52\3\uffff\2\52\1\uffff\1\1\1\52\2"+
			"\1\14\uffff\1\51",
			"\1\1\7\uffff\1\1\3\uffff\1\1\1\uffff\1\53\1\1\3\uffff\2\55\4\uffff\1"+
			"\55\1\uffff\1\56\3\uffff\1\57\1\60\1\61\1\uffff\2\55\1\uffff\2\55\1\102"+
			"\1\62\1\uffff\1\55\1\100\1\63\3\uffff\1\55\1\64\3\uffff\3\55\1\65\1\uffff"+
			"\1\55\1\uffff\2\55\4\uffff\1\66\1\1\2\55\1\uffff\1\67\1\uffff\1\55\1"+
			"\103\1\55\1\uffff\2\55\1\uffff\3\55\1\uffff\1\1\1\55\1\uffff\1\55\1\uffff"+
			"\1\1\2\uffff\1\55\2\uffff\3\55\3\uffff\1\55\1\uffff\2\55\2\uffff\1\55"+
			"\1\70\4\55\1\71\1\101\1\72\1\77\1\73\1\uffff\1\1\1\55\1\uffff\1\103\2"+
			"\55\3\uffff\2\55\1\uffff\1\74\1\55\1\75\1\76\2\uffff\1\103\10\uffff\1"+
			"\1\1\54\2\uffff\1\1\2\uffff\1\1\7\uffff\1\1\3\uffff\1\1\1\uffff\1\1\6"+
			"\uffff\1\1\2\uffff\1\1",
			"\1\1\1\41",
			"\1\1\1\41",
			"\1\1\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\31\uffff\1\1\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41",
			"\1\41\1\uffff\2\41\25\uffff\1\41\21\uffff\1\41\6\uffff\1\41\10\uffff"+
			"\1\41\16\uffff\1\41\37\uffff\1\41\10\uffff\1\41\32\uffff\3\41\1\uffff"+
			"\1\1\2\41\7\uffff\1\41\1\uffff\1\41"
	};

	static final short[] DFA155_eot = DFA.unpackEncodedString(DFA155_eotS);
	static final short[] DFA155_eof = DFA.unpackEncodedString(DFA155_eofS);
	static final char[] DFA155_min = DFA.unpackEncodedStringToUnsignedChars(DFA155_minS);
	static final char[] DFA155_max = DFA.unpackEncodedStringToUnsignedChars(DFA155_maxS);
	static final short[] DFA155_accept = DFA.unpackEncodedString(DFA155_acceptS);
	static final short[] DFA155_special = DFA.unpackEncodedString(DFA155_specialS);
	static final short[][] DFA155_transition;

	static {
		int numStates = DFA155_transitionS.length;
		DFA155_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA155_transition[i] = DFA.unpackEncodedString(DFA155_transitionS[i]);
		}
	}

	protected class DFA155 extends DFA {

		public DFA155(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 155;
			this.eot = DFA155_eot;
			this.eof = DFA155_eof;
			this.min = DFA155_min;
			this.max = DFA155_max;
			this.accept = DFA155_accept;
			this.special = DFA155_special;
			this.transition = DFA155_transition;
		}
		@Override
		public String getDescription() {
			return "1258:1: term returns [Term.Raw term] : (v= value |f= function | '(' c= comparatorType ')' t= term );";
		}
	}

	static final String DFA158_eotS =
		"\35\uffff";
	static final String DFA158_eofS =
		"\35\uffff";
	static final String DFA158_minS =
		"\1\6\1\uffff\31\25\2\uffff";
	static final String DFA158_maxS =
		"\1\u00b8\1\uffff\31\u00ad\2\uffff";
	static final String DFA158_acceptS =
		"\1\uffff\1\1\31\uffff\1\2\1\3";
	static final String DFA158_specialS =
		"\35\uffff}>";
	static final String[] DFA158_transitionS = {
			"\1\1\7\uffff\1\1\3\uffff\1\1\1\uffff\1\2\1\1\3\uffff\2\4\4\uffff\1\4"+
			"\1\uffff\1\5\3\uffff\1\6\1\7\1\10\1\uffff\2\4\1\uffff\2\4\1\31\1\11\1"+
			"\uffff\1\4\1\27\1\12\3\uffff\1\4\1\13\3\uffff\3\4\1\14\1\uffff\1\4\1"+
			"\uffff\2\4\4\uffff\1\15\1\1\2\4\1\uffff\1\16\1\uffff\1\4\1\32\1\4\1\uffff"+
			"\2\4\1\uffff\3\4\1\uffff\1\1\1\4\1\uffff\1\4\1\uffff\1\1\2\uffff\1\4"+
			"\2\uffff\3\4\3\uffff\1\4\1\uffff\2\4\2\uffff\1\4\1\17\4\4\1\20\1\30\1"+
			"\21\1\26\1\22\1\uffff\1\1\1\4\1\uffff\1\32\2\4\3\uffff\2\4\1\uffff\1"+
			"\23\1\4\1\24\1\25\2\uffff\1\32\10\uffff\1\1\1\3\2\uffff\1\1\2\uffff\1"+
			"\1\7\uffff\1\1\3\uffff\1\1\1\uffff\1\1\6\uffff\1\1\2\uffff\1\1",
			"",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0092\uffff\1\1\1\uffff\1\33\1\uffff\1\33\1\1",
			"\1\34\u0094\uffff\1\33\1\uffff\1\33\1\1",
			"",
			""
	};

	static final short[] DFA158_eot = DFA.unpackEncodedString(DFA158_eotS);
	static final short[] DFA158_eof = DFA.unpackEncodedString(DFA158_eofS);
	static final char[] DFA158_min = DFA.unpackEncodedStringToUnsignedChars(DFA158_minS);
	static final char[] DFA158_max = DFA.unpackEncodedStringToUnsignedChars(DFA158_maxS);
	static final short[] DFA158_accept = DFA.unpackEncodedString(DFA158_acceptS);
	static final short[] DFA158_special = DFA.unpackEncodedString(DFA158_specialS);
	static final short[][] DFA158_transition;

	static {
		int numStates = DFA158_transitionS.length;
		DFA158_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA158_transition[i] = DFA.unpackEncodedString(DFA158_transitionS[i]);
		}
	}

	protected class DFA158 extends DFA {

		public DFA158(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 158;
			this.eot = DFA158_eot;
			this.eof = DFA158_eof;
			this.min = DFA158_min;
			this.max = DFA158_max;
			this.accept = DFA158_accept;
			this.special = DFA158_special;
			this.transition = DFA158_transition;
		}
		@Override
		public String getDescription() {
			return "1273:1: normalColumnOperation[List<Pair<ColumnIdentifier.Raw, Operation.RawUpdate>> operations, ColumnIdentifier.Raw key] : (t= term ( '+' c= cident )? |c= cident sig= ( '+' | '-' ) t= term |c= cident i= INTEGER );";
		}
	}

	static final String DFA164_eotS =
		"\34\uffff";
	static final String DFA164_eofS =
		"\34\uffff";
	static final String DFA164_minS =
		"\1\24\30\u00b2\1\6\2\uffff";
	static final String DFA164_maxS =
		"\1\u009a\30\u00b2\1\u00b8\2\uffff";
	static final String DFA164_acceptS =
		"\32\uffff\1\1\1\2";
	static final String DFA164_specialS =
		"\34\uffff}>";
	static final String[] DFA164_transitionS = {
			"\1\1\4\uffff\2\3\4\uffff\1\3\1\uffff\1\4\3\uffff\1\5\1\6\1\7\1\uffff"+
			"\2\3\1\uffff\2\3\1\30\1\10\1\uffff\1\3\1\26\1\11\3\uffff\1\3\1\12\3\uffff"+
			"\3\3\1\13\1\uffff\1\3\1\uffff\2\3\4\uffff\1\14\1\uffff\2\3\1\uffff\1"+
			"\15\1\uffff\1\3\1\30\1\3\1\uffff\2\3\1\uffff\3\3\2\uffff\1\3\1\uffff"+
			"\1\3\4\uffff\1\3\2\uffff\3\3\3\uffff\1\3\1\uffff\2\3\2\uffff\1\3\1\16"+
			"\4\3\1\17\1\27\1\20\1\25\1\21\2\uffff\1\3\1\uffff\1\30\2\3\3\uffff\2"+
			"\3\1\uffff\1\22\1\3\1\23\1\24\2\uffff\1\30\11\uffff\1\2",
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
			"\1\32\1\uffff\3\32\1\uffff\2\32\1\uffff\3\32\1\uffff\2\32\1\uffff\1\32"+
			"\4\uffff\1\32\2\uffff\3\32\3\uffff\1\32\1\uffff\2\32\2\uffff\13\32\2"+
			"\uffff\1\32\1\uffff\3\32\3\uffff\2\32\1\uffff\4\32\2\uffff\1\32\14\uffff"+
			"\1\32\2\uffff\1\32\13\uffff\1\32\13\uffff\1\33",
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
			return "1332:1: property[PropertyDefinitions props] : (k= ident '=' simple= propertyValue |k= ident '=' map= mapLiteral );";
		}
	}

	static final String DFA170_eotS =
		"\73\uffff";
	static final String DFA170_eofS =
		"\73\uffff";
	static final String DFA170_minS =
		"\1\24\30\55\1\uffff\1\24\1\uffff\1\u0099\2\uffff\30\55\4\uffff";
	static final String DFA170_maxS =
		"\1\u00a8\30\u00b5\1\uffff\1\u00a8\1\uffff\1\u00ae\2\uffff\30\u00b5\4\uffff";
	static final String DFA170_acceptS =
		"\31\uffff\1\2\1\uffff\1\1\1\uffff\1\5\1\6\30\uffff\1\10\1\3\1\4\1\7";
	static final String DFA170_specialS =
		"\73\uffff}>";
	static final String[] DFA170_transitionS = {
			"\1\1\4\uffff\2\3\4\uffff\1\3\1\uffff\1\4\3\uffff\1\5\1\6\1\7\1\uffff"+
			"\2\3\1\uffff\2\3\1\30\1\10\1\uffff\1\3\1\26\1\11\3\uffff\1\3\1\12\3\uffff"+
			"\3\3\1\13\1\uffff\1\3\1\uffff\2\3\4\uffff\1\14\1\uffff\2\3\1\uffff\1"+
			"\15\1\uffff\1\3\1\30\1\3\1\uffff\2\3\1\uffff\3\3\2\uffff\1\3\1\uffff"+
			"\1\3\4\uffff\1\3\2\uffff\3\3\3\uffff\1\3\1\uffff\2\3\2\uffff\1\3\1\16"+
			"\4\3\1\17\1\27\1\20\1\25\1\21\1\uffff\1\31\1\3\1\uffff\1\30\2\3\3\uffff"+
			"\2\3\1\uffff\1\22\1\3\1\23\1\24\2\uffff\1\30\11\uffff\1\2\15\uffff\1"+
			"\32",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"\1\35\31\uffff\1\34\137\uffff\1\33\10\uffff\5\33\1\36",
			"",
			"\1\37\4\uffff\2\41\4\uffff\1\41\1\uffff\1\42\3\uffff\1\43\1\44\1\45"+
			"\1\uffff\2\41\1\uffff\2\41\1\66\1\46\1\uffff\1\41\1\64\1\47\3\uffff\1"+
			"\41\1\50\3\uffff\3\41\1\51\1\uffff\1\41\1\uffff\2\41\4\uffff\1\52\1\uffff"+
			"\2\41\1\uffff\1\53\1\uffff\1\41\1\66\1\41\1\uffff\2\41\1\uffff\3\41\2"+
			"\uffff\1\41\1\uffff\1\41\4\uffff\1\41\2\uffff\3\41\3\uffff\1\41\1\uffff"+
			"\2\41\2\uffff\1\41\1\54\4\41\1\55\1\65\1\56\1\63\1\57\1\uffff\1\67\1"+
			"\41\1\uffff\1\66\2\41\3\uffff\2\41\1\uffff\1\60\1\41\1\61\1\62\2\uffff"+
			"\1\66\11\uffff\1\40\15\uffff\1\67",
			"",
			"\1\70\16\uffff\1\71\5\uffff\1\70",
			"",
			"",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"\1\67\31\uffff\1\67\137\uffff\1\67\1\uffff\1\72\1\uffff\1\72\4\uffff"+
			"\6\67",
			"",
			"",
			"",
			""
	};

	static final short[] DFA170_eot = DFA.unpackEncodedString(DFA170_eotS);
	static final short[] DFA170_eof = DFA.unpackEncodedString(DFA170_eofS);
	static final char[] DFA170_min = DFA.unpackEncodedStringToUnsignedChars(DFA170_minS);
	static final char[] DFA170_max = DFA.unpackEncodedStringToUnsignedChars(DFA170_maxS);
	static final short[] DFA170_accept = DFA.unpackEncodedString(DFA170_acceptS);
	static final short[] DFA170_special = DFA.unpackEncodedString(DFA170_specialS);
	static final short[][] DFA170_transition;

	static {
		int numStates = DFA170_transitionS.length;
		DFA170_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA170_transition[i] = DFA.unpackEncodedString(DFA170_transitionS[i]);
		}
	}

	protected class DFA170 extends DFA {

		public DFA170(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 170;
			this.eot = DFA170_eot;
			this.eof = DFA170_eof;
			this.min = DFA170_min;
			this.max = DFA170_max;
			this.accept = DFA170_accept;
			this.special = DFA170_special;
			this.transition = DFA170_transition;
		}
		@Override
		public String getDescription() {
			return "1351:1: relation[List<Relation> clauses] : (name= cident type= relationType t= term | K_TOKEN l= tupleOfIdentifiers type= relationType t= term |name= cident K_IN marker= inMarker |name= cident K_IN inValues= singleColumnInValues |name= cident K_CONTAINS ( K_KEY )? t= term |name= cident '[' key= term ']' type= relationType t= term |ids= tupleOfIdentifiers ( K_IN ( '(' ')' |tupleInMarker= inMarkerForTuple |literals= tupleOfTupleLiterals |markers= tupleOfMarkersForTuples ) |type= relationType literal= tupleLiteral |type= relationType tupleMarker= markerForTuple ) | '(' relation[$clauses] ')' );";
		}
	}

	static final String DFA169_eotS =
		"\12\uffff";
	static final String DFA169_eofS =
		"\12\uffff";
	static final String DFA169_minS =
		"\1\107\1\uffff\6\u0099\2\uffff";
	static final String DFA169_maxS =
		"\1\u00b4\1\uffff\6\u00ae\2\uffff";
	static final String DFA169_acceptS =
		"\1\uffff\1\1\6\uffff\1\2\1\3";
	static final String DFA169_specialS =
		"\12\uffff}>";
	static final String[] DFA169_transitionS = {
			"\1\1\137\uffff\1\7\10\uffff\1\3\1\4\1\2\1\5\1\6",
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

	static final short[] DFA169_eot = DFA.unpackEncodedString(DFA169_eotS);
	static final short[] DFA169_eof = DFA.unpackEncodedString(DFA169_eofS);
	static final char[] DFA169_min = DFA.unpackEncodedStringToUnsignedChars(DFA169_minS);
	static final char[] DFA169_max = DFA.unpackEncodedStringToUnsignedChars(DFA169_maxS);
	static final short[] DFA169_accept = DFA.unpackEncodedString(DFA169_acceptS);
	static final short[] DFA169_special = DFA.unpackEncodedString(DFA169_specialS);
	static final short[][] DFA169_transition;

	static {
		int numStates = DFA169_transitionS.length;
		DFA169_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA169_transition[i] = DFA.unpackEncodedString(DFA169_transitionS[i]);
		}
	}

	protected class DFA169 extends DFA {

		public DFA169(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 169;
			this.eot = DFA169_eot;
			this.eof = DFA169_eof;
			this.min = DFA169_min;
			this.max = DFA169_max;
			this.accept = DFA169_accept;
			this.special = DFA169_special;
			this.transition = DFA169_transition;
		}
		@Override
		public String getDescription() {
			return "1363:7: ( K_IN ( '(' ')' |tupleInMarker= inMarkerForTuple |literals= tupleOfTupleLiterals |markers= tupleOfMarkersForTuples ) |type= relationType literal= tupleLiteral |type= relationType tupleMarker= markerForTuple )";
		}
	}

	static final String DFA179_eotS =
		"\37\uffff";
	static final String DFA179_eofS =
		"\1\uffff\24\34\2\31\1\uffff\1\31\1\uffff\1\31\4\uffff";
	static final String DFA179_minS =
		"\1\24\26\76\1\uffff\1\76\1\uffff\1\76\4\uffff";
	static final String DFA179_maxS =
		"\1\u009d\26\u00b3\1\uffff\1\u00b3\1\uffff\1\u00b3\4\uffff";
	static final String DFA179_acceptS =
		"\27\uffff\1\2\1\uffff\1\4\1\uffff\1\6\1\1\1\3\1\5";
	static final String DFA179_specialS =
		"\37\uffff}>";
	static final String[] DFA179_transitionS = {
			"\1\31\4\uffff\2\31\4\uffff\1\31\1\uffff\1\1\3\uffff\1\2\1\3\1\4\1\uffff"+
			"\2\31\1\uffff\3\31\1\5\1\uffff\1\31\1\23\1\6\3\uffff\1\31\1\7\3\uffff"+
			"\3\31\1\10\1\uffff\1\32\1\uffff\2\31\4\uffff\1\11\1\uffff\2\31\1\uffff"+
			"\1\12\1\uffff\3\31\1\uffff\2\31\1\uffff\1\26\1\31\1\25\2\uffff\1\31\1"+
			"\uffff\1\31\4\uffff\1\31\2\uffff\3\31\3\uffff\1\31\1\uffff\2\31\1\uffff"+
			"\1\27\1\31\1\13\4\31\1\14\1\24\1\15\1\22\1\16\2\uffff\1\31\1\uffff\1"+
			"\31\1\30\1\31\3\uffff\2\31\1\uffff\1\17\1\31\1\20\1\21\2\uffff\1\31\11"+
			"\uffff\1\31\2\uffff\1\33",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\34\14\uffff\1\34\11\uffff\1\34\23\uffff\1\34\12\uffff\1\34\64\uffff"+
			"\1\34\1\uffff\1\34\1\uffff\1\31\1\uffff\1\34\3\uffff\1\34",
			"\1\31\14\uffff\1\31\11\uffff\1\31\23\uffff\1\31\12\uffff\1\31\64\uffff"+
			"\1\31\1\uffff\1\31\1\uffff\1\31\1\uffff\1\31\1\27\2\uffff\1\31",
			"\1\31\14\uffff\1\31\11\uffff\1\31\23\uffff\1\31\12\uffff\1\31\64\uffff"+
			"\1\31\1\uffff\1\31\1\uffff\1\31\1\uffff\1\31\1\27\2\uffff\1\31",
			"",
			"\1\31\14\uffff\1\31\11\uffff\1\31\23\uffff\1\31\12\uffff\1\31\64\uffff"+
			"\1\31\1\uffff\1\31\1\uffff\1\31\1\uffff\1\31\1\35\2\uffff\1\31",
			"",
			"\1\31\14\uffff\1\31\11\uffff\1\31\23\uffff\1\31\12\uffff\1\31\64\uffff"+
			"\1\31\1\uffff\1\31\1\uffff\1\31\1\uffff\1\31\1\36\2\uffff\1\31",
			"",
			"",
			"",
			""
	};

	static final short[] DFA179_eot = DFA.unpackEncodedString(DFA179_eotS);
	static final short[] DFA179_eof = DFA.unpackEncodedString(DFA179_eofS);
	static final char[] DFA179_min = DFA.unpackEncodedStringToUnsignedChars(DFA179_minS);
	static final char[] DFA179_max = DFA.unpackEncodedStringToUnsignedChars(DFA179_maxS);
	static final short[] DFA179_accept = DFA.unpackEncodedString(DFA179_acceptS);
	static final short[] DFA179_special = DFA.unpackEncodedString(DFA179_specialS);
	static final short[][] DFA179_transition;

	static {
		int numStates = DFA179_transitionS.length;
		DFA179_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA179_transition[i] = DFA.unpackEncodedString(DFA179_transitionS[i]);
		}
	}

	protected class DFA179 extends DFA {

		public DFA179(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 179;
			this.eot = DFA179_eot;
			this.eof = DFA179_eof;
			this.min = DFA179_min;
			this.max = DFA179_max;
			this.accept = DFA179_accept;
			this.special = DFA179_special;
			this.transition = DFA179_transition;
		}
		@Override
		public String getDescription() {
			return "1420:1: comparatorType returns [CQL3Type.Raw t] : (n= native_type |c= collection_type |tt= tuple_type |id= userTypeName | K_FROZEN '<' f= comparatorType '>' |s= STRING_LITERAL );";
		}
	}

	public static final BitSet FOLLOW_cqlStatement_in_query72 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000800000000000L});
	public static final BitSet FOLLOW_175_in_query75 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000800000000000L});
	public static final BitSet FOLLOW_EOF_in_query79 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_selectStatement_in_cqlStatement113 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insertStatement_in_cqlStatement138 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_updateStatement_in_cqlStatement163 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_batchStatement_in_cqlStatement188 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_deleteStatement_in_cqlStatement214 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_useStatement_in_cqlStatement239 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_truncateStatement_in_cqlStatement267 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_createKeyspaceStatement_in_cqlStatement290 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_createTableStatement_in_cqlStatement307 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_createIndexStatement_in_cqlStatement326 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dropKeyspaceStatement_in_cqlStatement345 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dropTableStatement_in_cqlStatement363 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dropIndexStatement_in_cqlStatement384 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_alterTableStatement_in_cqlStatement405 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_alterKeyspaceStatement_in_cqlStatement425 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_grantPermissionsStatement_in_cqlStatement442 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_revokePermissionsStatement_in_cqlStatement456 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_listPermissionsStatement_in_cqlStatement469 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_createUserStatement_in_cqlStatement484 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_alterUserStatement_in_cqlStatement504 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dropUserStatement_in_cqlStatement525 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_listUsersStatement_in_cqlStatement547 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_createTriggerStatement_in_cqlStatement568 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dropTriggerStatement_in_cqlStatement585 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_createTypeStatement_in_cqlStatement604 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_alterTypeStatement_in_cqlStatement624 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dropTypeStatement_in_cqlStatement645 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_createFunctionStatement_in_cqlStatement667 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dropFunctionStatement_in_cqlStatement683 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_createAggregateStatement_in_cqlStatement701 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dropAggregateStatement_in_cqlStatement716 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_createRoleStatement_in_cqlStatement733 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_alterRoleStatement_in_cqlStatement753 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dropRoleStatement_in_cqlStatement774 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_listRolesStatement_in_cqlStatement796 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_grantRoleStatement_in_cqlStatement817 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_revokeRoleStatement_in_cqlStatement838 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_USE_in_useStatement871 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_keyspaceName_in_useStatement875 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_SELECT_in_selectStatement909 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A1AL,0x0040000006013D8EL});
	public static final BitSet FOLLOW_K_JSON_in_selectStatement920 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A1AL,0x0040000006013D8EL});
	public static final BitSet FOLLOW_K_DISTINCT_in_selectStatement937 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A1AL,0x0040000006013D8EL});
	public static final BitSet FOLLOW_selectClause_in_selectStatement946 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
	public static final BitSet FOLLOW_K_FROM_in_selectStatement956 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_columnFamilyName_in_selectStatement960 = new BitSet(new long[]{0x0000000008000002L,0x0000002000400000L,0x0000000000004000L});
	public static final BitSet FOLLOW_K_WHERE_in_selectStatement970 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A1AL,0x0000010004013D8EL});
	public static final BitSet FOLLOW_whereClause_in_selectStatement974 = new BitSet(new long[]{0x0000000008000002L,0x0000002000400000L});
	public static final BitSet FOLLOW_K_ORDER_in_selectStatement987 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_K_BY_in_selectStatement989 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_orderByClause_in_selectStatement991 = new BitSet(new long[]{0x0000000008000002L,0x0000000000400000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_selectStatement996 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_orderByClause_in_selectStatement998 = new BitSet(new long[]{0x0000000008000002L,0x0000000000400000L,0x0000080000000000L});
	public static final BitSet FOLLOW_K_LIMIT_in_selectStatement1015 = new BitSet(new long[]{0x0000000008200000L,0x0000000000000000L,0x0000400002000000L});
	public static final BitSet FOLLOW_intValue_in_selectStatement1019 = new BitSet(new long[]{0x0000000008000002L});
	public static final BitSet FOLLOW_K_ALLOW_in_selectStatement1034 = new BitSet(new long[]{0x2000000000000000L});
	public static final BitSet FOLLOW_K_FILTERING_in_selectStatement1036 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_selector_in_selectClause1073 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_selectClause1078 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_selector_in_selectClause1082 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_182_in_selectClause1094 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unaliasedSelector_in_selector1127 = new BitSet(new long[]{0x0000000080000002L});
	public static final BitSet FOLLOW_K_AS_in_selector1130 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_ident_in_selector1134 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_unaliasedSelector1175 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000200000000000L});
	public static final BitSet FOLLOW_K_COUNT_in_unaliasedSelector1221 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_168_in_unaliasedSelector1223 = new BitSet(new long[]{0x0000000000200000L,0x0000000000000000L,0x0040000000000000L});
	public static final BitSet FOLLOW_countArgument_in_unaliasedSelector1225 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000020000000000L});
	public static final BitSet FOLLOW_169_in_unaliasedSelector1227 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000200000000000L});
	public static final BitSet FOLLOW_K_WRITETIME_in_unaliasedSelector1252 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_168_in_unaliasedSelector1254 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_unaliasedSelector1258 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000020000000000L});
	public static final BitSet FOLLOW_169_in_unaliasedSelector1260 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000200000000000L});
	public static final BitSet FOLLOW_K_TTL_in_unaliasedSelector1286 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_168_in_unaliasedSelector1294 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_unaliasedSelector1298 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000020000000000L});
	public static final BitSet FOLLOW_169_in_unaliasedSelector1300 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000200000000000L});
	public static final BitSet FOLLOW_functionName_in_unaliasedSelector1328 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_selectionFunctionArgs_in_unaliasedSelector1332 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000200000000000L});
	public static final BitSet FOLLOW_173_in_unaliasedSelector1347 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_unaliasedSelector1351 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000200000000000L});
	public static final BitSet FOLLOW_168_in_selectionFunctionArgs1379 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000020000000000L});
	public static final BitSet FOLLOW_169_in_selectionFunctionArgs1381 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_168_in_selectionFunctionArgs1391 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_unaliasedSelector_in_selectionFunctionArgs1395 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_171_in_selectionFunctionArgs1411 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_unaliasedSelector_in_selectionFunctionArgs1415 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_169_in_selectionFunctionArgs1428 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_182_in_countArgument1447 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGER_in_countArgument1457 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_relation_in_whereClause1488 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_AND_in_whereClause1492 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A1AL,0x0000010004013D8EL});
	public static final BitSet FOLLOW_relation_in_whereClause1494 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_cident_in_orderByClause1525 = new BitSet(new long[]{0x0020000100000002L});
	public static final BitSet FOLLOW_K_ASC_in_orderByClause1528 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DESC_in_orderByClause1532 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_INSERT_in_insertStatement1561 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
	public static final BitSet FOLLOW_K_INTO_in_insertStatement1563 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_columnFamilyName_in_insertStatement1567 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L,0x0000010000000000L});
	public static final BitSet FOLLOW_normalInsertStatement_in_insertStatement1581 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_JSON_in_insertStatement1596 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L,0x0000400022000200L});
	public static final BitSet FOLLOW_jsonInsertStatement_in_insertStatement1600 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_168_in_normalInsertStatement1636 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_normalInsertStatement1640 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_171_in_normalInsertStatement1647 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_normalInsertStatement1651 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_169_in_normalInsertStatement1658 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000800L});
	public static final BitSet FOLLOW_K_VALUES_in_normalInsertStatement1666 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_168_in_normalInsertStatement1674 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_normalInsertStatement1678 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_171_in_normalInsertStatement1684 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_normalInsertStatement1688 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_169_in_normalInsertStatement1695 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L,0x0000000000000200L});
	public static final BitSet FOLLOW_K_IF_in_normalInsertStatement1705 = new BitSet(new long[]{0x0000000000000000L,0x0000000080000000L});
	public static final BitSet FOLLOW_K_NOT_in_normalInsertStatement1707 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_normalInsertStatement1709 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000200L});
	public static final BitSet FOLLOW_usingClause_in_normalInsertStatement1724 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_jsonValue_in_jsonInsertStatement1770 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L,0x0000000000000200L});
	public static final BitSet FOLLOW_K_IF_in_jsonInsertStatement1780 = new BitSet(new long[]{0x0000000000000000L,0x0000000080000000L});
	public static final BitSet FOLLOW_K_NOT_in_jsonInsertStatement1782 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_jsonInsertStatement1784 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000200L});
	public static final BitSet FOLLOW_usingClause_in_jsonInsertStatement1799 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_jsonValue1840 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_174_in_jsonValue1850 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_ident_in_jsonValue1854 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_jsonValue1868 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_USING_in_usingClause1899 = new BitSet(new long[]{0x0000000000000000L,0x0400000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_usingClauseObjective_in_usingClause1901 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_AND_in_usingClause1906 = new BitSet(new long[]{0x0000000000000000L,0x0400000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_usingClauseObjective_in_usingClause1908 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_TIMESTAMP_in_usingClauseObjective1930 = new BitSet(new long[]{0x0000000000200000L,0x0000000000000000L,0x0000400002000000L});
	public static final BitSet FOLLOW_intValue_in_usingClauseObjective1934 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TTL_in_usingClauseObjective1944 = new BitSet(new long[]{0x0000000000200000L,0x0000000000000000L,0x0000400002000000L});
	public static final BitSet FOLLOW_intValue_in_usingClauseObjective1948 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_UPDATE_in_updateStatement1982 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_columnFamilyName_in_updateStatement1986 = new BitSet(new long[]{0x0000000000000000L,0x0002000000000000L,0x0000000000000200L});
	public static final BitSet FOLLOW_usingClause_in_updateStatement1996 = new BitSet(new long[]{0x0000000000000000L,0x0002000000000000L});
	public static final BitSet FOLLOW_K_SET_in_updateStatement2008 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_columnOperation_in_updateStatement2010 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000004000L});
	public static final BitSet FOLLOW_171_in_updateStatement2014 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_columnOperation_in_updateStatement2016 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000004000L});
	public static final BitSet FOLLOW_K_WHERE_in_updateStatement2027 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A1AL,0x0000010004013D8EL});
	public static final BitSet FOLLOW_whereClause_in_updateStatement2031 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
	public static final BitSet FOLLOW_K_IF_in_updateStatement2041 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_K_EXISTS_in_updateStatement2045 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_updateConditions_in_updateStatement2053 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_columnCondition_in_updateConditions2095 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_AND_in_updateConditions2100 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_columnCondition_in_updateConditions2102 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_DELETE_in_deleteStatement2139 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1BL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_deleteSelection_in_deleteStatement2145 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
	public static final BitSet FOLLOW_K_FROM_in_deleteStatement2158 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_columnFamilyName_in_deleteStatement2162 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000004200L});
	public static final BitSet FOLLOW_usingClauseDelete_in_deleteStatement2172 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_K_WHERE_in_deleteStatement2184 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A1AL,0x0000010004013D8EL});
	public static final BitSet FOLLOW_whereClause_in_deleteStatement2188 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
	public static final BitSet FOLLOW_K_IF_in_deleteStatement2198 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_K_EXISTS_in_deleteStatement2202 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_updateConditions_in_deleteStatement2210 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_deleteOp_in_deleteSelection2257 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_deleteSelection2272 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_deleteOp_in_deleteSelection2276 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_cident_in_deleteOp2303 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_deleteOp2330 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0020000000000000L});
	public static final BitSet FOLLOW_181_in_deleteOp2332 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_deleteOp2336 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0080000000000000L});
	public static final BitSet FOLLOW_183_in_deleteOp2338 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_USING_in_usingClauseDelete2358 = new BitSet(new long[]{0x0000000000000000L,0x0400000000000000L});
	public static final BitSet FOLLOW_K_TIMESTAMP_in_usingClauseDelete2360 = new BitSet(new long[]{0x0000000000200000L,0x0000000000000000L,0x0000400002000000L});
	public static final BitSet FOLLOW_intValue_in_usingClauseDelete2364 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_BEGIN_in_batchStatement2398 = new BitSet(new long[]{0x0000800800000000L,0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_K_UNLOGGED_in_batchStatement2408 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_K_COUNTER_in_batchStatement2414 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_K_BATCH_in_batchStatement2427 = new BitSet(new long[]{0x0010000040000000L,0x0000000000002000L,0x0000000000000220L});
	public static final BitSet FOLLOW_usingClause_in_batchStatement2431 = new BitSet(new long[]{0x0010000040000000L,0x0000000000002000L,0x0000000000000020L});
	public static final BitSet FOLLOW_batchStatementObjective_in_batchStatement2451 = new BitSet(new long[]{0x0010000040000000L,0x0000000000002000L,0x0000800000000020L});
	public static final BitSet FOLLOW_175_in_batchStatement2453 = new BitSet(new long[]{0x0010000040000000L,0x0000000000002000L,0x0000000000000020L});
	public static final BitSet FOLLOW_K_APPLY_in_batchStatement2467 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_K_BATCH_in_batchStatement2469 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insertStatement_in_batchStatementObjective2500 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_updateStatement_in_batchStatementObjective2513 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_deleteStatement_in_batchStatementObjective2526 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CREATE_in_createAggregateStatement2559 = new BitSet(new long[]{0x0000000002000000L,0x0000001000000000L});
	public static final BitSet FOLLOW_K_OR_in_createAggregateStatement2562 = new BitSet(new long[]{0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_K_REPLACE_in_createAggregateStatement2564 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_K_AGGREGATE_in_createAggregateStatement2576 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A5AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_K_IF_in_createAggregateStatement2585 = new BitSet(new long[]{0x0000000000000000L,0x0000000080000000L});
	public static final BitSet FOLLOW_K_NOT_in_createAggregateStatement2587 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_createAggregateStatement2589 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_functionName_in_createAggregateStatement2603 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_168_in_createAggregateStatement2611 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000020024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_createAggregateStatement2635 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_171_in_createAggregateStatement2651 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_createAggregateStatement2655 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_169_in_createAggregateStatement2679 = new BitSet(new long[]{0x0000000000000000L,0x0004000000000000L});
	public static final BitSet FOLLOW_K_SFUNC_in_createAggregateStatement2687 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B55A1AL,0x0000000004003D8CL});
	public static final BitSet FOLLOW_allowedFunctionName_in_createAggregateStatement2693 = new BitSet(new long[]{0x0000000000000000L,0x0040000000000000L});
	public static final BitSet FOLLOW_K_STYPE_in_createAggregateStatement2701 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_createAggregateStatement2707 = new BitSet(new long[]{0x4000000000000002L,0x0000000000000800L});
	public static final BitSet FOLLOW_K_FINALFUNC_in_createAggregateStatement2725 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B55A1AL,0x0000000004003D8CL});
	public static final BitSet FOLLOW_allowedFunctionName_in_createAggregateStatement2731 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000800L});
	public static final BitSet FOLLOW_K_INITCOND_in_createAggregateStatement2758 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_createAggregateStatement2764 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_dropAggregateStatement2811 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_K_AGGREGATE_in_dropAggregateStatement2813 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A5AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_K_IF_in_dropAggregateStatement2822 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_dropAggregateStatement2824 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_functionName_in_dropAggregateStatement2839 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_168_in_dropAggregateStatement2857 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000020024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_dropAggregateStatement2885 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_171_in_dropAggregateStatement2903 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_dropAggregateStatement2907 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_169_in_dropAggregateStatement2935 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CREATE_in_createFunctionStatement2992 = new BitSet(new long[]{0x0000000000000000L,0x0000001000000008L});
	public static final BitSet FOLLOW_K_OR_in_createFunctionStatement2995 = new BitSet(new long[]{0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_K_REPLACE_in_createFunctionStatement2997 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_K_FUNCTION_in_createFunctionStatement3009 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A5AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_K_IF_in_createFunctionStatement3018 = new BitSet(new long[]{0x0000000000000000L,0x0000000080000000L});
	public static final BitSet FOLLOW_K_NOT_in_createFunctionStatement3020 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_createFunctionStatement3022 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_functionName_in_createFunctionStatement3036 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_168_in_createFunctionStatement3044 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000020004013D8EL});
	public static final BitSet FOLLOW_ident_in_createFunctionStatement3068 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_createFunctionStatement3072 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_171_in_createFunctionStatement3088 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_ident_in_createFunctionStatement3092 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_createFunctionStatement3096 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_169_in_createFunctionStatement3120 = new BitSet(new long[]{0x0000020000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_K_RETURNS_in_createFunctionStatement3131 = new BitSet(new long[]{0x0000000000000000L,0x0000000100000000L});
	public static final BitSet FOLLOW_K_NULL_in_createFunctionStatement3133 = new BitSet(new long[]{0x0000000000000000L,0x0000000400000000L});
	public static final BitSet FOLLOW_K_CALLED_in_createFunctionStatement3139 = new BitSet(new long[]{0x0000000000000000L,0x0000000400000000L});
	public static final BitSet FOLLOW_K_ON_in_createFunctionStatement3145 = new BitSet(new long[]{0x0000000000000000L,0x0000000100000000L});
	public static final BitSet FOLLOW_K_NULL_in_createFunctionStatement3147 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
	public static final BitSet FOLLOW_K_INPUT_in_createFunctionStatement3149 = new BitSet(new long[]{0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_K_RETURNS_in_createFunctionStatement3157 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_createFunctionStatement3163 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
	public static final BitSet FOLLOW_K_LANGUAGE_in_createFunctionStatement3171 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_IDENT_in_createFunctionStatement3177 = new BitSet(new long[]{0x0000000080000000L});
	public static final BitSet FOLLOW_K_AS_in_createFunctionStatement3185 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000020000000L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_createFunctionStatement3191 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_dropFunctionStatement3229 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_K_FUNCTION_in_dropFunctionStatement3231 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A5AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_K_IF_in_dropFunctionStatement3240 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_dropFunctionStatement3242 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_functionName_in_dropFunctionStatement3257 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_168_in_dropFunctionStatement3275 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000020024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_dropFunctionStatement3303 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_171_in_dropFunctionStatement3321 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_dropFunctionStatement3325 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_169_in_dropFunctionStatement3353 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CREATE_in_createKeyspaceStatement3412 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
	public static final BitSet FOLLOW_K_KEYSPACE_in_createKeyspaceStatement3414 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A5AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_K_IF_in_createKeyspaceStatement3417 = new BitSet(new long[]{0x0000000000000000L,0x0000000080000000L});
	public static final BitSet FOLLOW_K_NOT_in_createKeyspaceStatement3419 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_createKeyspaceStatement3421 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_keyspaceName_in_createKeyspaceStatement3430 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000008000L});
	public static final BitSet FOLLOW_K_WITH_in_createKeyspaceStatement3438 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_properties_in_createKeyspaceStatement3440 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CREATE_in_createTableStatement3475 = new BitSet(new long[]{0x0000080000000000L});
	public static final BitSet FOLLOW_K_COLUMNFAMILY_in_createTableStatement3477 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A5AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_K_IF_in_createTableStatement3480 = new BitSet(new long[]{0x0000000000000000L,0x0000000080000000L});
	public static final BitSet FOLLOW_K_NOT_in_createTableStatement3482 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_createTableStatement3484 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_columnFamilyName_in_createTableStatement3499 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_cfamDefinition_in_createTableStatement3509 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_168_in_cfamDefinition3528 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD3C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cfamColumns_in_cfamDefinition3530 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_171_in_cfamDefinition3535 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD3C853B75A1AL,0x00000A0004013D8EL});
	public static final BitSet FOLLOW_cfamColumns_in_cfamDefinition3537 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_169_in_cfamDefinition3544 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000008000L});
	public static final BitSet FOLLOW_K_WITH_in_cfamDefinition3554 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cfamProperty_in_cfamDefinition3556 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_AND_in_cfamDefinition3561 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cfamProperty_in_cfamDefinition3563 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_ident_in_cfamColumns3589 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_cfamColumns3593 = new BitSet(new long[]{0x0000000000000002L,0x0010020000000000L});
	public static final BitSet FOLLOW_K_STATIC_in_cfamColumns3598 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000000L});
	public static final BitSet FOLLOW_K_PRIMARY_in_cfamColumns3615 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L});
	public static final BitSet FOLLOW_K_KEY_in_cfamColumns3617 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_PRIMARY_in_cfamColumns3629 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L});
	public static final BitSet FOLLOW_K_KEY_in_cfamColumns3631 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_168_in_cfamColumns3633 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000010004013D8EL});
	public static final BitSet FOLLOW_pkDef_in_cfamColumns3635 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_171_in_cfamColumns3639 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_ident_in_cfamColumns3643 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_169_in_cfamColumns3650 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ident_in_pkDef3670 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_168_in_pkDef3680 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_ident_in_pkDef3686 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_171_in_pkDef3692 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_ident_in_pkDef3696 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_169_in_pkDef3703 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_property_in_cfamProperty3723 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_COMPACT_in_cfamProperty3732 = new BitSet(new long[]{0x0000000000000000L,0x0020000000000000L});
	public static final BitSet FOLLOW_K_STORAGE_in_cfamProperty3734 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CLUSTERING_in_cfamProperty3744 = new BitSet(new long[]{0x0000000000000000L,0x0000002000000000L});
	public static final BitSet FOLLOW_K_ORDER_in_cfamProperty3746 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_K_BY_in_cfamProperty3748 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_168_in_cfamProperty3750 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cfamOrdering_in_cfamProperty3752 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_171_in_cfamProperty3756 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cfamOrdering_in_cfamProperty3758 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_169_in_cfamProperty3763 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ident_in_cfamOrdering3791 = new BitSet(new long[]{0x0020000100000000L});
	public static final BitSet FOLLOW_K_ASC_in_cfamOrdering3794 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DESC_in_cfamOrdering3798 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CREATE_in_createTypeStatement3837 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_K_TYPE_in_createTypeStatement3839 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A5AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_K_IF_in_createTypeStatement3842 = new BitSet(new long[]{0x0000000000000000L,0x0000000080000000L});
	public static final BitSet FOLLOW_K_NOT_in_createTypeStatement3844 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_createTypeStatement3846 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_userTypeName_in_createTypeStatement3864 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_168_in_createTypeStatement3877 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_typeColumns_in_createTypeStatement3879 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_171_in_createTypeStatement3884 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x00000A0004013D8EL});
	public static final BitSet FOLLOW_typeColumns_in_createTypeStatement3886 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_169_in_createTypeStatement3893 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ident_in_typeColumns3913 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_typeColumns3917 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CREATE_in_createIndexStatement3952 = new BitSet(new long[]{0x0002000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_K_CUSTOM_in_createIndexStatement3955 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_K_INDEX_in_createIndexStatement3961 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1CC53B75A5AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_K_IF_in_createIndexStatement3964 = new BitSet(new long[]{0x0000000000000000L,0x0000000080000000L});
	public static final BitSet FOLLOW_K_NOT_in_createIndexStatement3966 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_createIndexStatement3968 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1CC53B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_idxName_in_createIndexStatement3984 = new BitSet(new long[]{0x0000000000000000L,0x0000000400000000L});
	public static final BitSet FOLLOW_K_ON_in_createIndexStatement3989 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_columnFamilyName_in_createIndexStatement3993 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_168_in_createIndexStatement3995 = new BitSet(new long[]{0xF58EF6E286100000L,0x9FFCD1C853B75A1EL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_indexIdent_in_createIndexStatement3999 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000020000000000L});
	public static final BitSet FOLLOW_169_in_createIndexStatement4001 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000008200L});
	public static final BitSet FOLLOW_K_USING_in_createIndexStatement4012 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000020000000L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_createIndexStatement4016 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000008000L});
	public static final BitSet FOLLOW_K_WITH_in_createIndexStatement4031 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_properties_in_createIndexStatement4033 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_indexIdent4067 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_KEYS_in_indexIdent4095 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_168_in_indexIdent4097 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_indexIdent4101 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000020000000000L});
	public static final BitSet FOLLOW_169_in_indexIdent4103 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ENTRIES_in_indexIdent4116 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_168_in_indexIdent4118 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_indexIdent4122 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000020000000000L});
	public static final BitSet FOLLOW_169_in_indexIdent4124 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_FULL_in_indexIdent4134 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_168_in_indexIdent4136 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_indexIdent4140 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000020000000000L});
	public static final BitSet FOLLOW_169_in_indexIdent4142 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CREATE_in_createTriggerStatement4180 = new BitSet(new long[]{0x0000000000000000L,0x8000000000000000L});
	public static final BitSet FOLLOW_K_TRIGGER_in_createTriggerStatement4182 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A5AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_K_IF_in_createTriggerStatement4185 = new BitSet(new long[]{0x0000000000000000L,0x0000000080000000L});
	public static final BitSet FOLLOW_K_NOT_in_createTriggerStatement4187 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_createTriggerStatement4189 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_createTriggerStatement4199 = new BitSet(new long[]{0x0000000000000000L,0x0000000400000000L});
	public static final BitSet FOLLOW_K_ON_in_createTriggerStatement4210 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_columnFamilyName_in_createTriggerStatement4214 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000200L});
	public static final BitSet FOLLOW_K_USING_in_createTriggerStatement4216 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000020000000L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_createTriggerStatement4220 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_dropTriggerStatement4261 = new BitSet(new long[]{0x0000000000000000L,0x8000000000000000L});
	public static final BitSet FOLLOW_K_TRIGGER_in_dropTriggerStatement4263 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A5AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_K_IF_in_dropTriggerStatement4266 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_dropTriggerStatement4268 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_dropTriggerStatement4278 = new BitSet(new long[]{0x0000000000000000L,0x0000000400000000L});
	public static final BitSet FOLLOW_K_ON_in_dropTriggerStatement4281 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_columnFamilyName_in_dropTriggerStatement4285 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ALTER_in_alterKeyspaceStatement4325 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
	public static final BitSet FOLLOW_K_KEYSPACE_in_alterKeyspaceStatement4327 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_keyspaceName_in_alterKeyspaceStatement4331 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000008000L});
	public static final BitSet FOLLOW_K_WITH_in_alterKeyspaceStatement4341 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_properties_in_alterKeyspaceStatement4343 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ALTER_in_alterTableStatement4379 = new BitSet(new long[]{0x0000080000000000L});
	public static final BitSet FOLLOW_K_COLUMNFAMILY_in_alterTableStatement4381 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_columnFamilyName_in_alterTableStatement4385 = new BitSet(new long[]{0x0200000011000000L,0x0000040000000000L,0x0000000000008000L});
	public static final BitSet FOLLOW_K_ALTER_in_alterTableStatement4399 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_alterTableStatement4403 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_K_TYPE_in_alterTableStatement4405 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_alterTableStatement4409 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ADD_in_alterTableStatement4425 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_alterTableStatement4431 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_alterTableStatement4435 = new BitSet(new long[]{0x0000000000000002L,0x0010000000000000L});
	public static final BitSet FOLLOW_K_STATIC_in_alterTableStatement4440 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_alterTableStatement4458 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_alterTableStatement4463 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_WITH_in_alterTableStatement4503 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_properties_in_alterTableStatement4506 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_RENAME_in_alterTableStatement4539 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_alterTableStatement4593 = new BitSet(new long[]{0x0000000000000000L,0x2000000000000000L});
	public static final BitSet FOLLOW_K_TO_in_alterTableStatement4595 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_alterTableStatement4599 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_AND_in_alterTableStatement4620 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_alterTableStatement4624 = new BitSet(new long[]{0x0000000000000000L,0x2000000000000000L});
	public static final BitSet FOLLOW_K_TO_in_alterTableStatement4626 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_alterTableStatement4630 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_ALTER_in_alterTypeStatement4676 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_K_TYPE_in_alterTypeStatement4678 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_userTypeName_in_alterTypeStatement4682 = new BitSet(new long[]{0x0000000011000000L,0x0000040000000000L});
	public static final BitSet FOLLOW_K_ALTER_in_alterTypeStatement4696 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_ident_in_alterTypeStatement4700 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_K_TYPE_in_alterTypeStatement4702 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_alterTypeStatement4706 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ADD_in_alterTypeStatement4722 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_ident_in_alterTypeStatement4728 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_alterTypeStatement4732 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_RENAME_in_alterTypeStatement4755 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_ident_in_alterTypeStatement4793 = new BitSet(new long[]{0x0000000000000000L,0x2000000000000000L});
	public static final BitSet FOLLOW_K_TO_in_alterTypeStatement4795 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_ident_in_alterTypeStatement4799 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_AND_in_alterTypeStatement4822 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_ident_in_alterTypeStatement4826 = new BitSet(new long[]{0x0000000000000000L,0x2000000000000000L});
	public static final BitSet FOLLOW_K_TO_in_alterTypeStatement4828 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_ident_in_alterTypeStatement4832 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_DROP_in_dropKeyspaceStatement4899 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
	public static final BitSet FOLLOW_K_KEYSPACE_in_dropKeyspaceStatement4901 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A5AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_K_IF_in_dropKeyspaceStatement4904 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_dropKeyspaceStatement4906 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_keyspaceName_in_dropKeyspaceStatement4915 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_dropTableStatement4949 = new BitSet(new long[]{0x0000080000000000L});
	public static final BitSet FOLLOW_K_COLUMNFAMILY_in_dropTableStatement4951 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A5AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_K_IF_in_dropTableStatement4954 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_dropTableStatement4956 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_columnFamilyName_in_dropTableStatement4965 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_dropTypeStatement4999 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_K_TYPE_in_dropTypeStatement5001 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A5AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_K_IF_in_dropTypeStatement5004 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_dropTypeStatement5006 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_userTypeName_in_dropTypeStatement5015 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_dropIndexStatement5049 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_K_INDEX_in_dropIndexStatement5051 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A5AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_K_IF_in_dropIndexStatement5054 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_dropIndexStatement5056 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_indexName_in_dropIndexStatement5065 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TRUNCATE_in_truncateStatement5096 = new BitSet(new long[]{0xF18EFEE286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_K_COLUMNFAMILY_in_truncateStatement5099 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_columnFamilyName_in_truncateStatement5105 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_GRANT_in_grantPermissionsStatement5130 = new BitSet(new long[]{0x0A41000414000000L,0x0001000004000000L});
	public static final BitSet FOLLOW_permissionOrAll_in_grantPermissionsStatement5142 = new BitSet(new long[]{0x0000000000000000L,0x0000000400000000L});
	public static final BitSet FOLLOW_K_ON_in_grantPermissionsStatement5150 = new BitSet(new long[]{0xF18EFEE286100000L,0x9FFCD1C853BF5A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_resource_in_grantPermissionsStatement5162 = new BitSet(new long[]{0x0000000000000000L,0x2000000000000000L});
	public static final BitSet FOLLOW_K_TO_in_grantPermissionsStatement5170 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000026013D8EL});
	public static final BitSet FOLLOW_userOrRoleName_in_grantPermissionsStatement5184 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_REVOKE_in_revokePermissionsStatement5215 = new BitSet(new long[]{0x0A41000414000000L,0x0001000004000000L});
	public static final BitSet FOLLOW_permissionOrAll_in_revokePermissionsStatement5227 = new BitSet(new long[]{0x0000000000000000L,0x0000000400000000L});
	public static final BitSet FOLLOW_K_ON_in_revokePermissionsStatement5235 = new BitSet(new long[]{0xF18EFEE286100000L,0x9FFCD1C853BF5A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_resource_in_revokePermissionsStatement5247 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
	public static final BitSet FOLLOW_K_FROM_in_revokePermissionsStatement5255 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000026013D8EL});
	public static final BitSet FOLLOW_userOrRoleName_in_revokePermissionsStatement5269 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_GRANT_in_grantRoleStatement5300 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000026013D8EL});
	public static final BitSet FOLLOW_userOrRoleName_in_grantRoleStatement5314 = new BitSet(new long[]{0x0000000000000000L,0x2000000000000000L});
	public static final BitSet FOLLOW_K_TO_in_grantRoleStatement5322 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000026013D8EL});
	public static final BitSet FOLLOW_userOrRoleName_in_grantRoleStatement5336 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_REVOKE_in_revokeRoleStatement5367 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000026013D8EL});
	public static final BitSet FOLLOW_userOrRoleName_in_revokeRoleStatement5381 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
	public static final BitSet FOLLOW_K_FROM_in_revokeRoleStatement5389 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000026013D8EL});
	public static final BitSet FOLLOW_userOrRoleName_in_revokeRoleStatement5403 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_LIST_in_listPermissionsStatement5441 = new BitSet(new long[]{0x0A41000414000000L,0x0001000004000000L});
	public static final BitSet FOLLOW_permissionOrAll_in_listPermissionsStatement5453 = new BitSet(new long[]{0x0000000000000002L,0x0000000620000000L});
	public static final BitSet FOLLOW_K_ON_in_listPermissionsStatement5463 = new BitSet(new long[]{0xF18EFEE286100000L,0x9FFCD1C853BF5A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_resource_in_listPermissionsStatement5465 = new BitSet(new long[]{0x0000000000000002L,0x0000000220000000L});
	public static final BitSet FOLLOW_K_OF_in_listPermissionsStatement5480 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000026013D8EL});
	public static final BitSet FOLLOW_roleName_in_listPermissionsStatement5482 = new BitSet(new long[]{0x0000000000000002L,0x0000000020000000L});
	public static final BitSet FOLLOW_K_NORECURSIVE_in_listPermissionsStatement5496 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_permission5532 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ALL_in_permissionOrAll5589 = new BitSet(new long[]{0x0000000000000002L,0x0000010000000000L});
	public static final BitSet FOLLOW_K_PERMISSIONS_in_permissionOrAll5593 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_permission_in_permissionOrAll5614 = new BitSet(new long[]{0x0000000000000002L,0x0000008000000000L});
	public static final BitSet FOLLOW_K_PERMISSION_in_permissionOrAll5618 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_dataResource_in_resource5646 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_roleResource_in_resource5658 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_functionResource_in_resource5670 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ALL_in_dataResource5693 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
	public static final BitSet FOLLOW_K_KEYSPACES_in_dataResource5695 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_KEYSPACE_in_dataResource5705 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_keyspaceName_in_dataResource5711 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_COLUMNFAMILY_in_dataResource5723 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_columnFamilyName_in_dataResource5732 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ALL_in_roleResource5761 = new BitSet(new long[]{0x0000000000000000L,0x0000800000000000L});
	public static final BitSet FOLLOW_K_ROLES_in_roleResource5763 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ROLE_in_roleResource5773 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000026013D8EL});
	public static final BitSet FOLLOW_userOrRoleName_in_roleResource5779 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ALL_in_functionResource5811 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_K_FUNCTIONS_in_functionResource5813 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ALL_in_functionResource5823 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_K_FUNCTIONS_in_functionResource5825 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_K_IN_in_functionResource5827 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
	public static final BitSet FOLLOW_K_KEYSPACE_in_functionResource5829 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_keyspaceName_in_functionResource5835 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_FUNCTION_in_functionResource5850 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_functionName_in_functionResource5854 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_168_in_functionResource5872 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000020024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_functionResource5900 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_171_in_functionResource5918 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_functionResource5922 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_169_in_functionResource5950 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CREATE_in_createUserStatement5998 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_K_USER_in_createUserStatement6000 = new BitSet(new long[]{0x0000000000100000L,0x0000000000000040L,0x0000000024000000L});
	public static final BitSet FOLLOW_K_IF_in_createUserStatement6003 = new BitSet(new long[]{0x0000000000000000L,0x0000000080000000L});
	public static final BitSet FOLLOW_K_NOT_in_createUserStatement6005 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_createUserStatement6007 = new BitSet(new long[]{0x0000000000100000L,0x0000000000000000L,0x0000000024000000L});
	public static final BitSet FOLLOW_username_in_createUserStatement6015 = new BitSet(new long[]{0x0000000000000002L,0x0080000040000000L,0x0000000000008000L});
	public static final BitSet FOLLOW_K_WITH_in_createUserStatement6027 = new BitSet(new long[]{0x0000000000000000L,0x0000004000000000L});
	public static final BitSet FOLLOW_userPassword_in_createUserStatement6029 = new BitSet(new long[]{0x0000000000000002L,0x0080000040000000L});
	public static final BitSet FOLLOW_K_SUPERUSER_in_createUserStatement6043 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_NOSUPERUSER_in_createUserStatement6049 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ALTER_in_alterUserStatement6094 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_K_USER_in_alterUserStatement6096 = new BitSet(new long[]{0x0000000000100000L,0x0000000000000000L,0x0000000024000000L});
	public static final BitSet FOLLOW_username_in_alterUserStatement6100 = new BitSet(new long[]{0x0000000000000002L,0x0080000040000000L,0x0000000000008000L});
	public static final BitSet FOLLOW_K_WITH_in_alterUserStatement6112 = new BitSet(new long[]{0x0000000000000000L,0x0000004000000000L});
	public static final BitSet FOLLOW_userPassword_in_alterUserStatement6114 = new BitSet(new long[]{0x0000000000000002L,0x0080000040000000L});
	public static final BitSet FOLLOW_K_SUPERUSER_in_alterUserStatement6128 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_NOSUPERUSER_in_alterUserStatement6142 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_dropUserStatement6188 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_K_USER_in_dropUserStatement6190 = new BitSet(new long[]{0x0000000000100000L,0x0000000000000040L,0x0000000024000000L});
	public static final BitSet FOLLOW_K_IF_in_dropUserStatement6193 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_dropUserStatement6195 = new BitSet(new long[]{0x0000000000100000L,0x0000000000000000L,0x0000000024000000L});
	public static final BitSet FOLLOW_username_in_dropUserStatement6203 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_LIST_in_listUsersStatement6228 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_K_USERS_in_listUsersStatement6230 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_CREATE_in_createRoleStatement6264 = new BitSet(new long[]{0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_K_ROLE_in_createRoleStatement6266 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A5AL,0x0000000026013D8EL});
	public static final BitSet FOLLOW_K_IF_in_createRoleStatement6269 = new BitSet(new long[]{0x0000000000000000L,0x0000000080000000L});
	public static final BitSet FOLLOW_K_NOT_in_createRoleStatement6271 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_createRoleStatement6273 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000026013D8EL});
	public static final BitSet FOLLOW_userOrRoleName_in_createRoleStatement6281 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000008000L});
	public static final BitSet FOLLOW_K_WITH_in_createRoleStatement6291 = new BitSet(new long[]{0x0000000000000000L,0x0080004801000000L});
	public static final BitSet FOLLOW_roleOptions_in_createRoleStatement6293 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ALTER_in_alterRoleStatement6337 = new BitSet(new long[]{0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_K_ROLE_in_alterRoleStatement6339 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000026013D8EL});
	public static final BitSet FOLLOW_userOrRoleName_in_alterRoleStatement6343 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000008000L});
	public static final BitSet FOLLOW_K_WITH_in_alterRoleStatement6353 = new BitSet(new long[]{0x0000000000000000L,0x0080004801000000L});
	public static final BitSet FOLLOW_roleOptions_in_alterRoleStatement6355 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DROP_in_dropRoleStatement6399 = new BitSet(new long[]{0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_K_ROLE_in_dropRoleStatement6401 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A5AL,0x0000000026013D8EL});
	public static final BitSet FOLLOW_K_IF_in_dropRoleStatement6404 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_K_EXISTS_in_dropRoleStatement6406 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000026013D8EL});
	public static final BitSet FOLLOW_userOrRoleName_in_dropRoleStatement6414 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_LIST_in_listRolesStatement6454 = new BitSet(new long[]{0x0000000000000000L,0x0000800000000000L});
	public static final BitSet FOLLOW_K_ROLES_in_listRolesStatement6456 = new BitSet(new long[]{0x0000000000000002L,0x0000000220000000L});
	public static final BitSet FOLLOW_K_OF_in_listRolesStatement6466 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000026013D8EL});
	public static final BitSet FOLLOW_roleName_in_listRolesStatement6468 = new BitSet(new long[]{0x0000000000000002L,0x0000000020000000L});
	public static final BitSet FOLLOW_K_NORECURSIVE_in_listRolesStatement6481 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_roleOption_in_roleOptions6512 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_AND_in_roleOptions6516 = new BitSet(new long[]{0x0000000000000000L,0x0080004801000000L});
	public static final BitSet FOLLOW_roleOption_in_roleOptions6518 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_PASSWORD_in_roleOption6540 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0004000000000000L});
	public static final BitSet FOLLOW_178_in_roleOption6542 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000020000000L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_roleOption6546 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_OPTIONS_in_roleOption6557 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0004000000000000L});
	public static final BitSet FOLLOW_178_in_roleOption6559 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0100000000000000L});
	public static final BitSet FOLLOW_mapLiteral_in_roleOption6563 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_SUPERUSER_in_roleOption6574 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0004000000000000L});
	public static final BitSet FOLLOW_178_in_roleOption6576 = new BitSet(new long[]{0x0000000000000040L});
	public static final BitSet FOLLOW_BOOLEAN_in_roleOption6580 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_LOGIN_in_roleOption6591 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0004000000000000L});
	public static final BitSet FOLLOW_178_in_roleOption6593 = new BitSet(new long[]{0x0000000000000040L});
	public static final BitSet FOLLOW_BOOLEAN_in_roleOption6597 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_PASSWORD_in_userPassword6619 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000020000000L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_userPassword6623 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_cident6654 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUOTED_NAME_in_cident6679 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unreserved_keyword_in_cident6698 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_ident6724 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUOTED_NAME_in_ident6749 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unreserved_keyword_in_ident6768 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ksName_in_keyspaceName6801 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ksName_in_indexName6835 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000200000000000L});
	public static final BitSet FOLLOW_173_in_indexName6838 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_idxName_in_indexName6842 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ksName_in_columnFamilyName6874 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000200000000000L});
	public static final BitSet FOLLOW_173_in_columnFamilyName6877 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000006013D8EL});
	public static final BitSet FOLLOW_cfName_in_columnFamilyName6881 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ident_in_userTypeName6906 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000200000000000L});
	public static final BitSet FOLLOW_173_in_userTypeName6908 = new BitSet(new long[]{0x7082360086100000L,0x80F4D1C853B7181AL,0x000000000400098CL});
	public static final BitSet FOLLOW_non_type_ident_in_userTypeName6914 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_roleName_in_userOrRoleName6946 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_ksName6969 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUOTED_NAME_in_ksName6994 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unreserved_keyword_in_ksName7013 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_ksName7023 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_cfName7045 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUOTED_NAME_in_cfName7070 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unreserved_keyword_in_cfName7089 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_cfName7099 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_idxName7121 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUOTED_NAME_in_idxName7146 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unreserved_keyword_in_idxName7165 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_idxName7175 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_roleName7197 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_roleName7222 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUOTED_NAME_in_roleName7238 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unreserved_keyword_in_roleName7257 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_roleName7267 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_constant7292 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGER_in_constant7304 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FLOAT_in_constant7323 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BOOLEAN_in_constant7344 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_UUID_in_constant7363 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_HEXNUMBER_in_constant7385 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_172_in_constant7403 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000400L});
	public static final BitSet FOLLOW_set_in_constant7412 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_184_in_mapLiteral7441 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0320510126013D8EL});
	public static final BitSet FOLLOW_term_in_mapLiteral7459 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_174_in_mapLiteral7461 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_mapLiteral7465 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0200080000000000L});
	public static final BitSet FOLLOW_171_in_mapLiteral7471 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_mapLiteral7475 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_174_in_mapLiteral7477 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_mapLiteral7481 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0200080000000000L});
	public static final BitSet FOLLOW_185_in_mapLiteral7497 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_174_in_setOrMapLiteral7521 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_setOrMapLiteral7525 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_setOrMapLiteral7541 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_setOrMapLiteral7545 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_174_in_setOrMapLiteral7547 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_setOrMapLiteral7551 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_setOrMapLiteral7586 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_setOrMapLiteral7590 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_181_in_collectionLiteral7624 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x01A0510126013D8EL});
	public static final BitSet FOLLOW_term_in_collectionLiteral7642 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0080080000000000L});
	public static final BitSet FOLLOW_171_in_collectionLiteral7648 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_collectionLiteral7652 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0080080000000000L});
	public static final BitSet FOLLOW_183_in_collectionLiteral7668 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_184_in_collectionLiteral7678 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_collectionLiteral7682 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0200480000000000L});
	public static final BitSet FOLLOW_setOrMapLiteral_in_collectionLiteral7686 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0200000000000000L});
	public static final BitSet FOLLOW_185_in_collectionLiteral7691 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_184_in_collectionLiteral7709 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0200000000000000L});
	public static final BitSet FOLLOW_185_in_collectionLiteral7711 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_184_in_usertypeLiteral7755 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_ident_in_usertypeLiteral7759 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_174_in_usertypeLiteral7761 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_usertypeLiteral7765 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0200080000000000L});
	public static final BitSet FOLLOW_171_in_usertypeLiteral7771 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_ident_in_usertypeLiteral7775 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000400000000000L});
	public static final BitSet FOLLOW_174_in_usertypeLiteral7777 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_usertypeLiteral7781 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0200080000000000L});
	public static final BitSet FOLLOW_185_in_usertypeLiteral7788 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_168_in_tupleLiteral7825 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_tupleLiteral7829 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_171_in_tupleLiteral7835 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_tupleLiteral7839 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_169_in_tupleLiteral7846 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_constant_in_value7869 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_collectionLiteral_in_value7891 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_usertypeLiteral_in_value7904 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_tupleLiteral_in_value7919 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_NULL_in_value7935 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_174_in_value7959 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_ident_in_value7963 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_value7981 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGER_in_intValue8027 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_174_in_intValue8041 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_ident_in_intValue8045 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_intValue8056 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_keyspaceName_in_functionName8090 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000200000000000L});
	public static final BitSet FOLLOW_173_in_functionName8092 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B55A1AL,0x0000000004003D8CL});
	public static final BitSet FOLLOW_allowedFunctionName_in_functionName8098 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_allowedFunctionName8125 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUOTED_NAME_in_allowedFunctionName8159 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unreserved_function_keyword_in_allowedFunctionName8187 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TOKEN_in_allowedFunctionName8197 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_COUNT_in_allowedFunctionName8229 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_functionName_in_function8276 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_168_in_function8278 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000020000000000L});
	public static final BitSet FOLLOW_169_in_function8280 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_functionName_in_function8310 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_168_in_function8312 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_functionArgs_in_function8316 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000020000000000L});
	public static final BitSet FOLLOW_169_in_function8318 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_term_in_functionArgs8351 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_functionArgs8357 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_functionArgs8361 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_value_in_term8389 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_function_in_term8426 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_168_in_term8458 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_term8462 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000020000000000L});
	public static final BitSet FOLLOW_169_in_term8464 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_term8468 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_columnOperation8491 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0024000000000000L});
	public static final BitSet FOLLOW_columnOperationDifferentiator_in_columnOperation8493 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_178_in_columnOperationDifferentiator8512 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_normalColumnOperation_in_columnOperationDifferentiator8514 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_181_in_columnOperationDifferentiator8523 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_columnOperationDifferentiator8527 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0080000000000000L});
	public static final BitSet FOLLOW_183_in_columnOperationDifferentiator8529 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0004000000000000L});
	public static final BitSet FOLLOW_specializedColumnOperation_in_columnOperationDifferentiator8531 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_term_in_normalColumnOperation8552 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000040000000000L});
	public static final BitSet FOLLOW_170_in_normalColumnOperation8555 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_normalColumnOperation8559 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_normalColumnOperation8580 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000140000000000L});
	public static final BitSet FOLLOW_set_in_normalColumnOperation8584 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_normalColumnOperation8594 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_normalColumnOperation8612 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_INTEGER_in_normalColumnOperation8616 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_178_in_specializedColumnOperation8642 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_specializedColumnOperation8646 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_columnCondition8679 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L,0x003F008000000000L});
	public static final BitSet FOLLOW_relationType_in_columnCondition8693 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_columnCondition8697 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_IN_in_columnCondition8711 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000410002000000L});
	public static final BitSet FOLLOW_singleColumnInValues_in_columnCondition8729 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_inMarker_in_columnCondition8749 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_181_in_columnCondition8777 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_columnCondition8781 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0080000000000000L});
	public static final BitSet FOLLOW_183_in_columnCondition8783 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L,0x001F008000000000L});
	public static final BitSet FOLLOW_relationType_in_columnCondition8801 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_columnCondition8805 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_IN_in_columnCondition8823 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000410002000000L});
	public static final BitSet FOLLOW_singleColumnInValues_in_columnCondition8845 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_inMarker_in_columnCondition8869 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_property_in_properties8931 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_K_AND_in_properties8935 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_property_in_properties8937 = new BitSet(new long[]{0x0000000020000002L});
	public static final BitSet FOLLOW_ident_in_property8960 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0004000000000000L});
	public static final BitSet FOLLOW_178_in_property8962 = new BitSet(new long[]{0xF18EF6E286244040L,0x9FFCD1C85BB75E1AL,0x0000100120013D8EL});
	public static final BitSet FOLLOW_propertyValue_in_property8966 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ident_in_property8978 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0004000000000000L});
	public static final BitSet FOLLOW_178_in_property8980 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0100000000000000L});
	public static final BitSet FOLLOW_mapLiteral_in_property8984 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_constant_in_propertyValue9009 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unreserved_keyword_in_propertyValue9031 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_178_in_relationType9054 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_176_in_relationType9065 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_177_in_relationType9076 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_179_in_relationType9086 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_180_in_relationType9097 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_167_in_relationType9107 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_relation9129 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x001F008000000000L});
	public static final BitSet FOLLOW_relationType_in_relation9133 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_relation9137 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TOKEN_in_relation9147 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_tupleOfIdentifiers_in_relation9151 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x001F008000000000L});
	public static final BitSet FOLLOW_relationType_in_relation9155 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_relation9159 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_relation9179 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_K_IN_in_relation9181 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000400002000000L});
	public static final BitSet FOLLOW_inMarker_in_relation9185 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_relation9205 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_K_IN_in_relation9207 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_singleColumnInValues_in_relation9211 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_relation9231 = new BitSet(new long[]{0x0000200000000000L});
	public static final BitSet FOLLOW_K_CONTAINS_in_relation9233 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_K_KEY_in_relation9238 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_relation9254 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_cident_in_relation9266 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0020000000000000L});
	public static final BitSet FOLLOW_181_in_relation9268 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_relation9272 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0080000000000000L});
	public static final BitSet FOLLOW_183_in_relation9274 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x001F008000000000L});
	public static final BitSet FOLLOW_relationType_in_relation9278 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_relation9282 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_tupleOfIdentifiers_in_relation9294 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L,0x001F008000000000L});
	public static final BitSet FOLLOW_K_IN_in_relation9304 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000410002000000L});
	public static final BitSet FOLLOW_168_in_relation9318 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000020000000000L});
	public static final BitSet FOLLOW_169_in_relation9320 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_inMarkerForTuple_in_relation9352 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_tupleOfTupleLiterals_in_relation9386 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_tupleOfMarkersForTuples_in_relation9420 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_relationType_in_relation9462 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_tupleLiteral_in_relation9466 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_relationType_in_relation9492 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000400002000000L});
	public static final BitSet FOLLOW_markerForTuple_in_relation9496 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_168_in_relation9526 = new BitSet(new long[]{0xF18EF6E286100000L,0xDFFCD1C853B75A1AL,0x0000010004013D8EL});
	public static final BitSet FOLLOW_relation_in_relation9528 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000020000000000L});
	public static final BitSet FOLLOW_169_in_relation9531 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_inMarker9552 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_174_in_inMarker9562 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_ident_in_inMarker9566 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_168_in_tupleOfIdentifiers9598 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_tupleOfIdentifiers9602 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_171_in_tupleOfIdentifiers9607 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_cident_in_tupleOfIdentifiers9611 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_169_in_tupleOfIdentifiers9617 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_168_in_singleColumnInValues9647 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120530126013D8EL});
	public static final BitSet FOLLOW_term_in_singleColumnInValues9655 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_171_in_singleColumnInValues9660 = new BitSet(new long[]{0xF18EF6E286344040L,0xDFFCD1C95BB75E1AL,0x0120510126013D8EL});
	public static final BitSet FOLLOW_term_in_singleColumnInValues9664 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_169_in_singleColumnInValues9673 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_168_in_tupleOfTupleLiterals9703 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_tupleLiteral_in_tupleOfTupleLiterals9707 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_171_in_tupleOfTupleLiterals9712 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_tupleLiteral_in_tupleOfTupleLiterals9716 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_169_in_tupleOfTupleLiterals9722 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_markerForTuple9743 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_174_in_markerForTuple9753 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_ident_in_markerForTuple9757 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_168_in_tupleOfMarkersForTuples9789 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000400002000000L});
	public static final BitSet FOLLOW_markerForTuple_in_tupleOfMarkersForTuples9793 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_171_in_tupleOfMarkersForTuples9798 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000400002000000L});
	public static final BitSet FOLLOW_markerForTuple_in_tupleOfMarkersForTuples9802 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x00000A0000000000L});
	public static final BitSet FOLLOW_169_in_tupleOfMarkersForTuples9808 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QMARK_in_inMarkerForTuple9829 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_174_in_inMarkerForTuple9839 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFCD1C853B75A1AL,0x0000000004013D8EL});
	public static final BitSet FOLLOW_ident_in_inMarkerForTuple9843 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_native_type_in_comparatorType9868 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_collection_type_in_comparatorType9884 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_tuple_type_in_comparatorType9896 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_userTypeName_in_comparatorType9912 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_FROZEN_in_comparatorType9924 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_176_in_comparatorType9926 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_comparatorType9930 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0008000000000000L});
	public static final BitSet FOLLOW_179_in_comparatorType9932 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_comparatorType9950 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_ASCII_in_native_type9979 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_BIGINT_in_native_type9993 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_BLOB_in_native_type10006 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_BOOLEAN_in_native_type10021 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_COUNTER_in_native_type10033 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DECIMAL_in_native_type10045 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DOUBLE_in_native_type10057 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_FLOAT_in_native_type10070 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_INET_in_native_type10084 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_INT_in_native_type10099 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_SMALLINT_in_native_type10115 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TEXT_in_native_type10126 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TIMESTAMP_in_native_type10141 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TINYINT_in_native_type10151 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_UUID_in_native_type10163 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_VARCHAR_in_native_type10178 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_VARINT_in_native_type10190 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TIMEUUID_in_native_type10203 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_DATE_in_native_type10214 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TIME_in_native_type10229 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_MAP_in_collection_type10257 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_176_in_collection_type10260 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_collection_type10264 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000080000000000L});
	public static final BitSet FOLLOW_171_in_collection_type10266 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_collection_type10270 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0008000000000000L});
	public static final BitSet FOLLOW_179_in_collection_type10272 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_LIST_in_collection_type10290 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_176_in_collection_type10292 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_collection_type10296 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0008000000000000L});
	public static final BitSet FOLLOW_179_in_collection_type10298 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_SET_in_collection_type10316 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_176_in_collection_type10319 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_collection_type10323 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0008000000000000L});
	public static final BitSet FOLLOW_179_in_collection_type10325 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_TUPLE_in_tuple_type10356 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0001000000000000L});
	public static final BitSet FOLLOW_176_in_tuple_type10358 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_tuple_type10373 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0008080000000000L});
	public static final BitSet FOLLOW_171_in_tuple_type10378 = new BitSet(new long[]{0xF18EF6E286100000L,0x9FFED1C853B75A1AL,0x0000000024013D8EL});
	public static final BitSet FOLLOW_comparatorType_in_tuple_type10382 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0008080000000000L});
	public static final BitSet FOLLOW_179_in_tuple_type10394 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_username10413 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_username10421 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUOTED_NAME_in_username10429 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_non_type_ident10456 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUOTED_NAME_in_non_type_ident10487 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_basic_unreserved_keyword_in_non_type_ident10512 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_K_KEY_in_non_type_ident10524 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unreserved_function_keyword_in_unreserved_keyword10567 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_unreserved_keyword10583 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_basic_unreserved_keyword_in_unreserved_function_keyword10622 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_native_type_in_unreserved_function_keyword10634 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_basic_unreserved_keyword10672 = new BitSet(new long[]{0x0000000000000002L});
}
