package sk.jm.consoleparser;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by Juraj on 7.9.2014.
 */
public class TextProcessor {
    public static final String SQL_PREFIX = "[debug] - c.j.b.PreparedStatementHandle - ";
    public static final int SQL_PREFIX_LEN = SQL_PREFIX.length();
    public static final String NEW_LINE = "\n";
    private Deque<SQLStatement> sqlList = new ArrayDeque<>();
    private List<String> ignoredList;
    private Document documentOut;
    private Document documentIgnored;
    private Document documentSQL;
    private JLabel sqlCountsLabel;
    private Style style, normalStyle, newStyle, lastStyle;
    private Style styleSQL, styleTime, styleInfo;
    private int listedSqlCount = 0;

    private class SQLStatement {
        public String time;
        public String statement;
        private SQLStatement(String time, String statement) {
            this.time = time;
            this.statement = statement;
        }
    }

    public enum SearchOptions {
        contains,
        startsWith,
        endsWith
    }

    private static final HashSet<String> sqlKeyWords = Sets.newHashSet(
            "A","ABORT","ABS","ABSOLUTE","ACCESS","ACTION","ADA","ADD","ADMIN","AFTER","AGGREGATE","ALIAS","ALL","ALLOCATE","ALSO","ALTER","ALWAYS","ANALYSE","ANALYZE","AND","ANY","ARE","ARRAY","AS","ASC","ASENSITIVE","ASSERTION","ASSIGNMENT","ASYMMETRIC","AT","ATOMIC","ATTRIBUTE","ATTRIBUTES","AUTHORIZATION","AVG","BACKWARD","BEFORE","BEGIN","BERNOULLI","BETWEEN","BIGINT","BINARY","BIT","BITVAR","BIT_LENGTH","BLOB","BOOLEAN","BOTH","BREADTH","BY","C","CACHE","CALL","CALLED","CARDINALITY","CASCADE","CASCADED","CASE","CAST","CATALOG","CATALOG_NAME","CEIL","CEILING","CHAIN","CHAR","CHARACTER","CHARACTERISTICS","CHARACTERS","CHARACTER_LENGTH","CHARACTER_SET_CATALOG","CHARACTER_SET_NAME","CHARACTER_SET_SCHEMA","CHAR_LENGTH","CHECK","CHECKED","CHECKPOINT","CLASS","CLASS_ORIGIN","CLOB","CLOSE","CLUSTER","COALESCE","COBOL","COLLATE","COLLATION","COLLATION_CATALOG","COLLATION_NAME","COLLATION_SCHEMA","COLLECT","COLUMN","COLUMN_NAME","COMMAND_FUNCTION","COMMAND_FUNCTION_CODE","COMMENT","COMMIT","COMMITTED","COMPLETION","CONDITION","CONDITION_NUMBER","CONNECT","CONNECTION","CONNECTION_NAME","CONSTRAINT","CONSTRAINTS","CONSTRAINT_CATALOG","CONSTRAINT_NAME","CONSTRAINT_SCHEMA","CONSTRUCTOR","CONTAINS","CONTINUE","CONVERSION","CONVERT","COPY","CORR","CORRESPONDING","COUNT","COVAR_POP","COVAR_SAMP","CREATE","CREATEDB","CREATEROLE","CREATEUSER","CROSS","CSV","CUBE","CUME_DIST","CURRENT","CURRENT_DATE","CURRENT_DEFAULT_TRANSFORM_GROUP","CURRENT_PATH","CURRENT_ROLE","CURRENT_TIME","CURRENT_TIMESTAMP","CURRENT_TRANSFORM_GROUP_FOR_TYPE","CURRENT_USER","CURSOR","CURSOR_NAME","CYCLE","DATA","DATABASE","DATE","DATETIME_INTERVAL_CODE","DATETIME_INTERVAL_PRECISION","DAY","DEALLOCATE","DEC","DECIMAL","DECLARE","DEFAULT","DEFAULTS","DEFERRABLE","DEFERRED","DEFINED","DEFINER","DEGREE","DELETE","DELIMITER","DELIMITERS","DENSE_RANK","DEPTH","DEREF","DERIVED","DESC","DESCRIBE","DESCRIPTOR","DESTROY","DESTRUCTOR","DETERMINISTIC","DIAGNOSTICS","DICTIONARY","DISABLE","DISCONNECT","DISPATCH","DISTINCT","DO","DOMAIN","DOUBLE","DROP","DYNAMIC","DYNAMIC_FUNCTION","DYNAMIC_FUNCTION_CODE","EACH","ELEMENT","ELSE","ENABLE","ENCODING","ENCRYPTED","END","END-EXEC","EQUALS","ESCAPE","EVERY","EXCEPT","EXCEPTION","EXCLUDE","EXCLUDING","EXCLUSIVE","EXEC","EXECUTE","EXISTING","EXISTS","EXP","EXPLAIN","EXTERNAL","EXTRACT","FALSE","FETCH","FILTER","FINAL","FIRST","FLOAT","FLOOR","FOLLOWING","FOR","FORCE","FOREIGN","FORTRAN","FORWARD","FOUND","FREE","FREEZE","FROM","FULL","FUNCTION","FUSION","G","GENERAL","GENERATED","GET","GLOBAL","GO","GOTO","GRANT","GRANTED","GREATEST","GROUP","GROUPING","HANDLER","HAVING","HEADER","HIERARCHY","HOLD","HOST","HOUR","IDENTITY","IGNORE","ILIKE","IMMEDIATE","IMMUTABLE","IMPLEMENTATION","IMPLICIT","IN","INCLUDING","INCREMENT","INDEX","INDICATOR","INFIX","INHERIT","INHERITS","INITIALIZE","INITIALLY","INNER","INOUT","INPUT","INSENSITIVE","INSERT","INSTANCE","INSTANTIABLE","INSTEAD","INT","INTEGER","INTERSECT","INTERSECTION","INTERVAL","INTO","INVOKER","IS","ISNULL","ISOLATION","ITERATE","JOIN","K","KEY","KEY_MEMBER","KEY_TYPE","LANCOMPILER","LANGUAGE","LARGE","LAST","LATERAL","LEADING","LEAST","LEFT","LENGTH","LESS","LEVEL","LIKE","LIMIT","LISTEN","LN","LOAD","LOCAL","LOCALTIME","LOCALTIMESTAMP","LOCATION","LOCATOR","LOCK","LOGIN","LOWER","M","MAP","MATCH","MATCHED","MAX","MAXVALUE","MEMBER","MERGE","MESSAGE_LENGTH","MESSAGE_OCTET_LENGTH","MESSAGE_TEXT","METHOD","MIN","MINUTE","MINVALUE","MOD","MODE","MODIFIES","MODIFY","MODULE","MONTH","MORE","MOVE","MULTISET","MUMPS","NAME","NAMES","NATIONAL","NATURAL","NCHAR","NCLOB","NESTING","NEW","NEXT","NO","NOCREATEDB","NOCREATEROLE","NOCREATEUSER","NOINHERIT","NOLOGIN","NONE","NORMALIZE","NORMALIZED","NOSUPERUSER","NOT","NOTHING","NOTIFY","NOTNULL","NOWAIT","NULL","NULLABLE","NULLIF","NULLS","NUMBER","NUMERIC","OBJECT","OCTETS","OCTET_LENGTH","OF","OFF","OFFSET","OIDS","OLD","ON","ONLY","OPEN","OPERATION","OPERATOR","OPTION","OPTIONS","OR","ORDER","ORDERING","ORDINALITY","OTHERS","OUT","OUTER","OUTPUT","OVER","OVERLAPS","OVERLAY","OVERRIDING","OWNER","PAD","PARAMETER","PARAMETERS","PARAMETER_MODE","PARAMETER_NAME","PARAMETER_ORDINAL_POSITION","PARAMETER_SPECIFIC_CATALOG","PARAMETER_SPECIFIC_NAME","PARAMETER_SPECIFIC_SCHEMA","PARTIAL","PARTITION","PASCAL","PASSWORD","PATH","PERCENTILE_CONT","PERCENTILE_DISC","PERCENT_RANK","PLACING","PLI","POSITION","POSTFIX","POWER","PRECEDING","PRECISION","PREFIX","PREORDER","PREPARE","PREPARED","PRESERVE","PRIMARY","PRIOR","PRIVILEGES","PROCEDURAL","PROCEDURE","PUBLIC","QUOTE","RANGE","RANK","READ","READS","REAL","RECHECK","RECURSIVE","REF","REFERENCES","REFERENCING","REGR_AVGX","REGR_AVGY","REGR_COUNT","REGR_INTERCEPT","REGR_R2","REGR_SLOPE","REGR_SXX","REGR_SXY","REGR_SYY","REINDEX","RELATIVE","RELEASE","RENAME","REPEATABLE","REPLACE","RESET","RESTART","RESTRICT","RESULT","RETURN","RETURNED_CARDINALITY","RETURNED_LENGTH","RETURNED_OCTET_LENGTH","RETURNED_SQLSTATE","RETURNS","REVOKE","RIGHT","ROLE","ROLLBACK","ROLLUP","ROUTINE","ROUTINE_CATALOG","ROUTINE_NAME","ROUTINE_SCHEMA","ROW","ROWS","ROW_COUNT","ROW_NUMBER","RULE","SAVEPOINT","SCALE","SCHEMA","SCHEMA_NAME","SCOPE","SCOPE_CATALOG","SCOPE_NAME","SCOPE_SCHEMA","SCROLL","SEARCH","SECOND","SECTION","SECURITY","SELECT","SELF","SENSITIVE","SEQUENCE","SERIALIZABLE","SERVER_NAME","SESSION","SESSION_USER","SET","SETOF","SETS","SHARE","SHOW","SIMILAR","SIMPLE","SIZE","SMALLINT","SOME","SOURCE","SPACE","SPECIFIC","SPECIFICTYPE","SPECIFIC_NAME","SQL","SQLCODE","SQLERROR","SQLEXCEPTION","SQLSTATE","SQLWARNING","SQRT","STABLE","START","STATE","STATEMENT","STATIC","STATISTICS","STDDEV_POP","STDDEV_SAMP","STDIN","STDOUT","STORAGE","STRICT","STRUCTURE","STYLE","SUBCLASS_ORIGIN","SUBLIST","SUBMULTISET","SUBSTRING","SUM","SUPERUSER","SYMMETRIC","SYSID","SYSTEM","SYSTEM_USER","TABLE","TABLESAMPLE","TABLESPACE","TABLE_NAME","TEMP","TEMPLATE","TEMPORARY","TERMINATE","THAN","THEN","TIES","TIME","TIMESTAMP","TIMEZONE_HOUR","TIMEZONE_MINUTE","TO","TOAST","TOP_LEVEL_COUNT","TRAILING","TRANSACTION","TRANSACTIONS_COMMITTED","TRANSACTIONS_ROLLED_BACK","TRANSACTION_ACTIVE","TRANSFORM","TRANSFORMS","TRANSLATE","TRANSLATION","TREAT","TRIGGER","TRIGGER_CATALOG","TRIGGER_NAME","TRIGGER_SCHEMA","TRIM","TRUE","TRUNCATE","TRUSTED","TYPE","UESCAPE","UNBOUNDED","UNCOMMITTED","UNDER","UNENCRYPTED","UNION","UNIQUE","UNKNOWN","UNLISTEN","UNNAMED","UNNEST","UNTIL","UPDATE","UPPER","USAGE","USER","USER_DEFINED_TYPE_CATALOG","USER_DEFINED_TYPE_CODE","USER_DEFINED_TYPE_NAME","USER_DEFINED_TYPE_SCHEMA","USING","VACUUM","VALID","VALIDATOR","VALUE","VALUES","VARCHAR","VARIABLE","VARYING","VAR_POP","VAR_SAMP","VERBOSE","VIEW","VOLATILE","WHEN","WHENEVER","WHERE","WIDTH_BUCKET","WINDOW","WITH","WITHIN","WITHOUT","WORK","WRITE","YEAR","ZONE",
            "a","abort","abs","absolute","access","action","ada","add","admin","after","aggregate","alias","all","allocate","also","alter","always","analyse","analyze","and","any","are","array","as","asc","asensitive","assertion","assignment","asymmetric","at","atomic","attribute","attributes","authorization","avg","backward","before","begin","bernoulli","between","bigint","binary","bit","bitvar","bit_length","blob","boolean","both","breadth","by","c","cache","call","called","cardinality","cascade","cascaded","case","cast","catalog","catalog_name","ceil","ceiling","chain","char","character","characteristics","characters","character_length","character_set_catalog","character_set_name","character_set_schema","char_length","check","checked","checkpoint","class","class_origin","clob","close","cluster","coalesce","cobol","collate","collation","collation_catalog","collation_name","collation_schema","collect","column","column_name","command_function","command_function_code","comment","commit","committed","completion","condition","condition_number","connect","connection","connection_name","constraint","constraints","constraint_catalog","constraint_name","constraint_schema","constructor","contains","continue","conversion","convert","copy","corr","corresponding","count","covar_pop","covar_samp","create","createdb","createrole","createuser","cross","csv","cube","cume_dist","current","current_date","current_default_transform_group","current_path","current_role","current_time","current_timestamp","current_transform_group_for_type","current_user","cursor","cursor_name","cycle","data","database","date","datetime_interval_code","datetime_interval_precision","day","deallocate","dec","decimal","declare","default","defaults","deferrable","deferred","defined","definer","degree","delete","delimiter","delimiters","dense_rank","depth","deref","derived","desc","describe","descriptor","destroy","destructor","deterministic","diagnostics","dictionary","disable","disconnect","dispatch","distinct","do","domain","double","drop","dynamic","dynamic_function","dynamic_function_code","each","element","else","enable","encoding","encrypted","end","end-exec","equals","escape","every","except","exception","exclude","excluding","exclusive","exec","execute","existing","exists","exp","explain","external","extract","false","fetch","filter","final","first","float","floor","following","for","force","foreign","fortran","forward","found","free","freeze","from","full","function","fusion","g","general","generated","get","global","go","goto","grant","granted","greatest","group","grouping","handler","having","header","hierarchy","hold","host","hour","identity","ignore","ilike","immediate","immutable","implementation","implicit","in","including","increment","index","indicator","infix","inherit","inherits","initialize","initially","inner","inout","input","insensitive","insert","instance","instantiable","instead","int","integer","intersect","intersection","interval","into","invoker","is","isnull","isolation","iterate","join","k","key","key_member","key_type","lancompiler","language","large","last","lateral","leading","least","left","length","less","level","like","limit","listen","ln","load","local","localtime","localtimestamp","location","locator","lock","login","lower","m","map","match","matched","max","maxvalue","member","merge","message_length","message_octet_length","message_text","method","min","minute","minvalue","mod","mode","modifies","modify","module","month","more","move","multiset","mumps","name","names","national","natural","nchar","nclob","nesting","new","next","no","nocreatedb","nocreaterole","nocreateuser","noinherit","nologin","none","normalize","normalized","nosuperuser","not","nothing","notify","notnull","nowait","null","nullable","nullif","nulls","number","numeric","object","octets","octet_length","of","off","offset","oids","old","on","only","open","operation","operator","option","options","or","order","ordering","ordinality","others","out","outer","output","over","overlaps","overlay","overriding","owner","pad","parameter","parameters","parameter_mode","parameter_name","parameter_ordinal_position","parameter_specific_catalog","parameter_specific_name","parameter_specific_schema","partial","partition","pascal","password","path","percentile_cont","percentile_disc","percent_rank","placing","pli","position","postfix","power","preceding","precision","prefix","preorder","prepare","prepared","preserve","primary","prior","privileges","procedural","procedure","public","quote","range","rank","read","reads","real","recheck","recursive","ref","references","referencing","regr_avgx","regr_avgy","regr_count","regr_intercept","regr_r2","regr_slope","regr_sxx","regr_sxy","regr_syy","reindex","relative","release","rename","repeatable","replace","reset","restart","restrict","result","return","returned_cardinality","returned_length","returned_octet_length","returned_sqlstate","returns","revoke","right","role","rollback","rollup","routine","routine_catalog","routine_name","routine_schema","row","rows","row_count","row_number","rule","savepoint","scale","schema","schema_name","scope","scope_catalog","scope_name","scope_schema","scroll","search","second","section","security","select","self","sensitive","sequence","serializable","server_name","session","session_user","set","setof","sets","share","show","similar","simple","size","smallint","some","source","space","specific","specifictype","specific_name","sql","sqlcode","sqlerror","sqlexception","sqlstate","sqlwarning","sqrt","stable","start","state","statement","static","statistics","stddev_pop","stddev_samp","stdin","stdout","storage","strict","structure","style","subclass_origin","sublist","submultiset","substring","sum","superuser","symmetric","sysid","system","system_user","table","tablesample","tablespace","table_name","temp","template","temporary","terminate","than","then","ties","time","timestamp","timezone_hour","timezone_minute","to","toast","top_level_count","trailing","transaction","transactions_committed","transactions_rolled_back","transaction_active","transform","transforms","translate","translation","treat","trigger","trigger_catalog","trigger_name","trigger_schema","trim","true","truncate","trusted","type","uescape","unbounded","uncommitted","under","unencrypted","union","unique","unknown","unlisten","unnamed","unnest","until","update","upper","usage","user","user_defined_type_catalog","user_defined_type_code","user_defined_type_name","user_defined_type_schema","using","vacuum","valid","validator","value","values","varchar","variable","varying","var_pop","var_samp","verbose","view","volatile","when","whenever","where","width_bucket","window","with","within","without","work","write","year","zone");

    private static final HashSet<String> breakingKeyWords = Sets.newHashSet(
            "select", "update", "insert", "from", "left", "right", "where", "order", "limit",
            "SELECT", "UPDATE", "INSERT", "FROM", "LEFT", "RIGHT", "WHERE", "ORDER", "LIMIT");

    public TextProcessor(Document documentOut, Document documentIgnored, Document documentSQL, JLabel sqlCountsLabel) {
        this.documentOut = documentOut;
        this.documentIgnored = documentIgnored;
        this.documentSQL = documentSQL;
        this.sqlCountsLabel = sqlCountsLabel;       // TODO: this is lazy proprammer style! FIX!

        StyledDocument styledDocumentOut = (StyledDocument) documentOut;
        normalStyle = styledDocumentOut.addStyle("Parent style", null);
        StyleConstants.setFontSize(normalStyle, 12);
        StyleConstants.setFontFamily(normalStyle, "Courier new");
        StyleConstants.setForeground(normalStyle, Color.WHITE);

        styleSQL = styledDocumentOut.addStyle("SQL style", normalStyle);
        StyleConstants.setBold(styleSQL, true);
        StyleConstants.setForeground(styleSQL, new Color(170, 179, 255));

        styleTime = styledDocumentOut.addStyle("SQL time style", normalStyle);
        StyleConstants.setForeground(styleTime, new Color(255, 77, 74));
        StyleConstants.setBold(styleTime, true);

        styleInfo = styledDocumentOut.addStyle("Info style", normalStyle);
        StyleConstants.setBold(styleInfo, true);
        StyleConstants.setForeground(styleInfo, new Color(2, 255, 2));

    }

    public void setIgnoredList(String ignoredList) {
        String[] split = StringUtils.split(ignoredList, '\n');
        List<String> result = new ArrayList<>();
        for (String s : split)
            if (!StringUtils.isBlank(s))
                result.add(s);
        this.ignoredList = ImmutableList.copyOf(result);
    }

    private boolean ignoreLine(String line) {
        return ignoredList.stream().anyMatch(x -> StringUtils.startsWith(line, x));
    }

    public void processLine(String line) {
        if (line.startsWith(SQL_PREFIX) && !ignoreLine(line))
            sqlList.offerFirst(new SQLStatement(Utils.time(), line.substring(SQL_PREFIX_LEN)));
    }

    private void appendOut(String str) {
        try {
            documentOut.insertString(documentOut.getLength(), str, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void appendIgnored(String str) {
        try {
            documentIgnored.insertString(documentIgnored.getLength(), str, normalStyle);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void appendText(ArrayList<String> textBlock, boolean printAsIs) {
        lastStyle = normalStyle;
        int length;
        boolean breakLine = false, isLast;
        StringBuilder sb = new StringBuilder(), sbIgnored = new StringBuilder();
        String str;
        if (textBlock.size() < 200 && !printAsIs) {
            for (String line : textBlock) {

                if (ignoreLine(line)) {
                    sbIgnored.append(line).append(NEW_LINE);
                    continue;
                }

                if (line.startsWith(SQL_PREFIX))
                    line = /*NEW_LINE + */Utils.time() + line.substring(SQL_PREFIX_LEN);

//                isLast = i == spltLines.Length - 1;
                Iterable<String> split = Splitter.on(' ').split(line);

                for (String word : split) {
                    if (sqlKeyWords.contains(word)) {
                        newStyle = styleSQL;
//                        newFont = boldFont;
//                        newColor = this.sqlColor;
                        if (breakingKeyWords.contains(word))
                            breakLine = true;
                    }
                    else {
                        if ((word.startsWith(NEW_LINE) || word.startsWith("[")) && word.endsWith("]")) {
                            length = word.length();
                            if (length > 5 && ":".equals(word.substring(length - 4, length - 3)))              // TODO: imrpove
                                newStyle = styleTime;
//                                newColor = this.sqlTimeColor;
                            else
                                newStyle =styleInfo;
//                                newColor = this.infoColor;
                        } else {
                            newStyle = normalStyle;
//                            newColor = this.normalColor;
                        }
//                        newFont = normalFont;
                    }
//                    str = (breakLine ? Environment.NewLine : "") + word + (isLast ? "" : " ");
                    str = (breakLine ? NEW_LINE : "") + word + " ";
                    if (lastStyle == newStyle)
                        sb.append(str);
                    else {
                        style = lastStyle;
                        appendOut(sb.toString());
                        sb.setLength(0);
                        sb.append(str);
                        lastStyle = newStyle;
                    }

                    breakLine = false;
                }
//                if (!isLast)
//                    sb.Append(Environment.NewLine);
                sb.append(NEW_LINE);
            }

            if (sb.length() > 0) {
                style = lastStyle;
//                richBox.SelectionColor = lastColor;
//                richBox.SelectionFont = lastFont;
                appendOut(sb.toString());
            }

            if (sbIgnored.length() > 0) {
                appendIgnored(sbIgnored.toString());
            }
        } else {
            style = normalStyle;
//            richBox.SelectionColor = this.normalColor;
//            richBox.SelectionFont = normalFont;
            appendOut(StringUtils.join(textBlock, NEW_LINE));
        }
    }


    public void appendText(String text) {
        appendText(Lists.newArrayList(text), true);
    }

    public String getFilteredSQL(String filter, SearchOptions searchOptions) {
        StringBuilder sb = new StringBuilder();
        listedSqlCount = 0;
        switch (searchOptions) {
            case contains:
                sqlList.stream().filter(x -> StringUtils.containsIgnoreCase(x.statement, filter)).forEachOrdered(y -> {sb.append(y.time).append(" ").append(y.statement).append("\n\n"); ++listedSqlCount;});
                break;
            case startsWith:
                sqlList.stream().filter(x -> StringUtils.startsWithIgnoreCase(x.statement, filter)).forEachOrdered(y -> {sb.append(y.time).append(" ").append(y.statement).append("\n\n"); ++listedSqlCount;});
                break;
            case endsWith:
                sqlList.stream().filter(x -> StringUtils.endsWithIgnoreCase(x.statement, filter)).forEachOrdered(y -> {sb.append(y.time).append(" ").append(y.statement).append("\n\n"); ++listedSqlCount;});
                break;
        }
        return sb.toString();
    }

    public String getSqlsCounts() {
        return listedSqlCount + "/" + sqlList.size();
    }

    public void clearSqls() {
        sqlList.clear();
    }

}
