package au.com.bytecode.opencsv;

import java.text.*;
import java.util.*;
import java.math.*;
import java.sql.*;
import java.io.*;

public class CSVWriter implements Closeable
{
    public static final int INITIAL_STRING_SIZE = 128;
    private Writer rawWriter;
    private PrintWriter pw;
    private char separator;
    private char quotechar;
    private char escapechar;
    private String lineEnd;
    public static final char DEFAULT_ESCAPE_CHARACTER = '\"';
    public static final char DEFAULT_SEPARATOR = ',';
    public static final char DEFAULT_QUOTE_CHARACTER = '\"';
    public static final char NO_QUOTE_CHARACTER = '\0';
    public static final char NO_ESCAPE_CHARACTER = '\0';
    public static final String DEFAULT_LINE_END = "\n";
    
    public CSVWriter(final Writer writer) {
        this(writer, ',');
    }
    
    public CSVWriter(final Writer writer, final char separator) {
        this(writer, separator, '\"');
    }
    
    public CSVWriter(final Writer writer, final char separator, final char quotechar) {
        this(writer, separator, quotechar, '\"');
    }
    
    public CSVWriter(final Writer writer, final char separator, final char quotechar, final char escapechar) {
        this(writer, separator, quotechar, escapechar, "\n");
    }
    
    public CSVWriter(final Writer writer, final char separator, final char quotechar, final String lineEnd) {
        this(writer, separator, quotechar, '\"', lineEnd);
    }
    
    public CSVWriter(final Writer writer, final char separator, final char quotechar, final char escapechar, final String lineEnd) {
        this.rawWriter = writer;
        this.pw = new PrintWriter(writer);
        this.separator = separator;
        this.quotechar = quotechar;
        this.escapechar = escapechar;
        this.lineEnd = lineEnd;
    }
    
    public void writeAll(final List<String[]> allLines) {
        for (final String[] line : allLines) {
            this.writeNext(line);
        }
    }
    
    protected void writeColumnNames(final ResultSetMetaData metadata) throws SQLException {
        final int columnCount = metadata.getColumnCount();
        final String[] nextLine = new String[columnCount];
        for (int i = 0; i < columnCount; ++i) {
            nextLine[i] = metadata.getColumnName(i + 1);
        }
        this.writeNext(nextLine);
    }
    
    public void writeAll(final ResultSet rs, final boolean includeColumnNames) throws SQLException, IOException {
        final ResultSetMetaData metadata = rs.getMetaData();
        if (includeColumnNames) {
            this.writeColumnNames(metadata);
        }
        final int columnCount = metadata.getColumnCount();
        while (rs.next()) {
            final String[] nextLine = new String[columnCount];
            for (int i = 0; i < columnCount; ++i) {
                nextLine[i] = getColumnValue(rs, metadata.getColumnType(i + 1), i + 1);
            }
            this.writeNext(nextLine);
        }
    }
    
    private static String getColumnValue(final ResultSet rs, final int colType, final int colIndex) throws SQLException, IOException {
        String value = "";
        switch (colType) {
            case -7: {
                final Object bit = rs.getObject(colIndex);
                if (bit != null) {
                    value = String.valueOf(bit);
                    break;
                }
                break;
            }
            case 16: {
                final boolean b = rs.getBoolean(colIndex);
                if (!rs.wasNull()) {
                    value = Boolean.valueOf(b).toString();
                    break;
                }
                break;
            }
            case 2005: {
                final Clob c = rs.getClob(colIndex);
                if (c != null) {
                    value = read(c);
                    break;
                }
                break;
            }
            case -5: {
                final long lv = rs.getLong(colIndex);
                if (!rs.wasNull()) {
                    value = Long.toString(lv);
                    break;
                }
                break;
            }
            case 2:
            case 3:
            case 6:
            case 7:
            case 8: {
                final BigDecimal bd = rs.getBigDecimal(colIndex);
                if (bd != null) {
                    value = bd.toString();
                    break;
                }
                break;
            }
            case -6:
            case 4:
            case 5: {
                final int intValue = rs.getInt(colIndex);
                if (!rs.wasNull()) {
                    value = Integer.toString(intValue);
                    break;
                }
                break;
            }
            case 2000: {
                final Object obj = rs.getObject(colIndex);
                if (obj != null) {
                    value = String.valueOf(obj);
                    break;
                }
                break;
            }
            case 91: {
                final java.sql.Date date = rs.getDate(colIndex);
                if (date != null) {
                    final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
                    value = dateFormat.format(date);
                    break;
                }
                break;
            }
            case 92: {
                final Time t = rs.getTime(colIndex);
                if (t != null) {
                    value = t.toString();
                    break;
                }
                break;
            }
            case 93: {
                final Timestamp tstamp = rs.getTimestamp(colIndex);
                if (tstamp != null) {
                    final SimpleDateFormat timeFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
                    value = timeFormat.format(tstamp);
                    break;
                }
                break;
            }
            case -1:
            case 1:
            case 12: {
                value = rs.getString(colIndex);
                break;
            }
            default: {
                value = "";
                break;
            }
        }
        if (value == null) {
            value = "";
        }
        return value;
    }
    
    private static String read(final Clob c) throws SQLException, IOException {
        final StringBuilder sb = new StringBuilder((int)c.length());
        final Reader r = c.getCharacterStream();
        final char[] cbuf = new char[2048];
        int n = 0;
        while ((n = r.read(cbuf, 0, cbuf.length)) != -1) {
            if (n > 0) {
                sb.append(cbuf, 0, n);
            }
        }
        return sb.toString();
    }
    
    public void writeNext(final String[] nextLine) {
        if (nextLine == null) {
            return;
        }
        final StringBuilder sb = new StringBuilder(128);
        for (int i = 0; i < nextLine.length; ++i) {
            if (i != 0) {
                sb.append(this.separator);
            }
            final String nextElement = nextLine[i];
            if (nextElement != null) {
                if (this.quotechar != '\0') {
                    sb.append(this.quotechar);
                }
                sb.append((CharSequence)(this.stringContainsSpecialCharacters(nextElement) ? this.processLine(nextElement) : nextElement));
                if (this.quotechar != '\0') {
                    sb.append(this.quotechar);
                }
            }
        }
        sb.append(this.lineEnd);
        this.pw.write(sb.toString());
    }
    
    private boolean stringContainsSpecialCharacters(final String line) {
        return line.indexOf(this.quotechar) != -1 || line.indexOf(this.escapechar) != -1;
    }
    
    private StringBuilder processLine(final String nextElement) {
        final StringBuilder sb = new StringBuilder(128);
        for (int j = 0; j < nextElement.length(); ++j) {
            final char nextChar = nextElement.charAt(j);
            if (this.escapechar != '\0' && nextChar == this.quotechar) {
                sb.append(this.escapechar).append(nextChar);
            }
            else if (this.escapechar != '\0' && nextChar == this.escapechar) {
                sb.append(this.escapechar).append(nextChar);
            }
            else {
                sb.append(nextChar);
            }
        }
        return sb;
    }
    
    public void flush() throws IOException {
        this.pw.flush();
    }
    
    public void close() throws IOException {
        this.pw.flush();
        this.pw.close();
        this.rawWriter.close();
    }
}
