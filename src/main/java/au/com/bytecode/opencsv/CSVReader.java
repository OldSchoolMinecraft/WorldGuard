package au.com.bytecode.opencsv;

import java.util.*;
import java.io.*;

public class CSVReader implements Closeable
{
    private BufferedReader br;
    private boolean hasNext;
    private final char separator;
    private final char quotechar;
    private final char escape;
    private int skipLines;
    private boolean linesSkiped;
    public static final char DEFAULT_SEPARATOR = ',';
    public static final int INITIAL_READ_SIZE = 64;
    public static final char DEFAULT_QUOTE_CHARACTER = '\"';
    public static final char DEFAULT_ESCAPE_CHARACTER = '\\';
    public static final int DEFAULT_SKIP_LINES = 0;
    
    public CSVReader(final Reader reader) {
        this(reader, ',');
    }
    
    public CSVReader(final Reader reader, final char separator) {
        this(reader, separator, '\"', '\\');
    }
    
    public CSVReader(final Reader reader, final char separator, final char quotechar) {
        this(reader, separator, quotechar, '\\', 0);
    }
    
    public CSVReader(final Reader reader, final char separator, final char quotechar, final char escape) {
        this(reader, separator, quotechar, escape, 0);
    }
    
    public CSVReader(final Reader reader, final char separator, final char quotechar, final int line) {
        this(reader, separator, quotechar, '\\', line);
    }
    
    public CSVReader(final Reader reader, final char separator, final char quotechar, final char escape, final int line) {
        this.hasNext = true;
        this.br = new BufferedReader(reader);
        this.separator = separator;
        this.quotechar = quotechar;
        this.escape = escape;
        this.skipLines = line;
    }
    
    public List<String[]> readAll() throws IOException {
        final List<String[]> allElements = new ArrayList<String[]>();
        while (this.hasNext) {
            final String[] nextLineAsTokens = this.readNext();
            if (nextLineAsTokens != null) {
                allElements.add(nextLineAsTokens);
            }
        }
        return allElements;
    }
    
    public String[] readNext() throws IOException {
        final String nextLine = this.getNextLine();
        return (String[])(this.hasNext ? this.parseLine(nextLine) : null);
    }
    
    private String getNextLine() throws IOException {
        if (!this.linesSkiped) {
            for (int i = 0; i < this.skipLines; ++i) {
                this.br.readLine();
            }
            this.linesSkiped = true;
        }
        final String nextLine = this.br.readLine();
        if (nextLine == null) {
            this.hasNext = false;
        }
        return this.hasNext ? nextLine : null;
    }
    
    private String[] parseLine(String nextLine) throws IOException {
        if (nextLine == null) {
            return null;
        }
        final List<String> tokensOnThisLine = new ArrayList<String>();
        StringBuilder sb = new StringBuilder(64);
        boolean inQuotes = false;
        do {
            if (inQuotes) {
                sb.append("\n");
                nextLine = this.getNextLine();
                if (nextLine == null) {
                    break;
                }
            }
            for (int i = 0; i < nextLine.length(); ++i) {
                final char c = nextLine.charAt(i);
                if (c == this.escape) {
                    if (this.isEscapable(nextLine, inQuotes, i)) {
                        sb.append(nextLine.charAt(i + 1));
                        ++i;
                    }
                    else {
                        ++i;
                    }
                }
                else if (c == this.quotechar) {
                    if (this.isEscapedQuote(nextLine, inQuotes, i)) {
                        sb.append(nextLine.charAt(i + 1));
                        ++i;
                    }
                    else {
                        inQuotes = !inQuotes;
                        if (i > 2 && nextLine.charAt(i - 1) != this.separator && nextLine.length() > i + 1 && nextLine.charAt(i + 1) != this.separator) {
                            sb.append(c);
                        }
                    }
                }
                else if (c == this.separator && !inQuotes) {
                    tokensOnThisLine.add(sb.toString());
                    sb = new StringBuilder(64);
                }
                else {
                    sb.append(c);
                }
            }
        } while (inQuotes);
        tokensOnThisLine.add(sb.toString());
        return tokensOnThisLine.toArray(new String[0]);
    }
    
    private boolean isEscapedQuote(final String nextLine, final boolean inQuotes, final int i) {
        return inQuotes && nextLine.length() > i + 1 && nextLine.charAt(i + 1) == this.quotechar;
    }
    
    private boolean isEscapable(final String nextLine, final boolean inQuotes, final int i) {
        return inQuotes && nextLine.length() > i + 1 && (nextLine.charAt(i + 1) == this.quotechar || nextLine.charAt(i + 1) == this.escape);
    }
    
    public void close() throws IOException {
        this.br.close();
    }
}
