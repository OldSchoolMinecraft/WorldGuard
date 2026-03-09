package au.com.bytecode.opencsv.bean;

import java.util.*;

public class HeaderColumnNameTranslateMappingStrategy<T> extends HeaderColumnNameMappingStrategy<T>
{
    private Map<String, String> columnMapping;
    
    public HeaderColumnNameTranslateMappingStrategy() {
        this.columnMapping = new HashMap<String, String>();
    }
    
    @Override
    protected String getColumnName(final int col) {
        return this.getColumnMapping().get(this.header[col]);
    }
    
    public Map<String, String> getColumnMapping() {
        return this.columnMapping;
    }
    
    public void setColumnMapping(final Map<String, String> columnMapping) {
        this.columnMapping = columnMapping;
    }
}
