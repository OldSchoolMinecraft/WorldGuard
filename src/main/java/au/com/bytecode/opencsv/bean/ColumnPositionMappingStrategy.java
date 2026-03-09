package au.com.bytecode.opencsv.bean;

import au.com.bytecode.opencsv.*;
import java.io.*;

public class ColumnPositionMappingStrategy<T> extends HeaderColumnNameMappingStrategy<T>
{
    protected String[] columnMapping;
    
    public ColumnPositionMappingStrategy() {
        this.columnMapping = new String[0];
    }
    
    @Override
    public void captureHeader(final CSVReader reader) throws IOException {
    }
    
    @Override
    protected String getColumnName(final int col) {
        return (null != this.columnMapping && col < this.columnMapping.length) ? this.columnMapping[col] : null;
    }
    
    public String[] getColumnMapping() {
        return this.columnMapping;
    }
    
    public void setColumnMapping(final String[] columnMapping) {
        this.columnMapping = columnMapping;
    }
}
