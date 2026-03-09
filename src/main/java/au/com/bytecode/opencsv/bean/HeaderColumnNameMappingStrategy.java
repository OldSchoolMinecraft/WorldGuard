package au.com.bytecode.opencsv.bean;

import au.com.bytecode.opencsv.*;
import java.io.*;
import java.util.*;
import java.beans.*;

public class HeaderColumnNameMappingStrategy<T> implements MappingStrategy<T>
{
    protected String[] header;
    protected Map<String, PropertyDescriptor> descriptorMap;
    protected Class<T> type;
    
    public HeaderColumnNameMappingStrategy() {
        this.descriptorMap = null;
    }
    
    public void captureHeader(final CSVReader reader) throws IOException {
        this.header = reader.readNext();
    }
    
    public PropertyDescriptor findDescriptor(final int col) throws IntrospectionException {
        final String columnName = this.getColumnName(col);
        return (null != columnName && columnName.trim().length() > 0) ? this.findDescriptor(columnName) : null;
    }
    
    protected String getColumnName(final int col) {
        return (null != this.header && col < this.header.length) ? this.header[col] : null;
    }
    
    protected PropertyDescriptor findDescriptor(final String name) throws IntrospectionException {
        if (null == this.descriptorMap) {
            this.descriptorMap = this.loadDescriptorMap(this.getType());
        }
        return this.descriptorMap.get(name.toUpperCase().trim());
    }
    
    protected boolean matches(final String name, final PropertyDescriptor desc) {
        return desc.getName().equals(name.trim());
    }
    
    protected Map<String, PropertyDescriptor> loadDescriptorMap(final Class<T> cls) throws IntrospectionException {
        final Map<String, PropertyDescriptor> map = new HashMap<String, PropertyDescriptor>();
        final PropertyDescriptor[] arr$;
        final PropertyDescriptor[] descriptors = arr$ = this.loadDescriptors(this.getType());
        for (final PropertyDescriptor descriptor : arr$) {
            map.put(descriptor.getName().toUpperCase().trim(), descriptor);
        }
        return map;
    }
    
    private PropertyDescriptor[] loadDescriptors(final Class<T> cls) throws IntrospectionException {
        final BeanInfo beanInfo = Introspector.getBeanInfo(cls);
        return beanInfo.getPropertyDescriptors();
    }
    
    public T createBean() throws InstantiationException, IllegalAccessException {
        return this.type.newInstance();
    }
    
    public Class<T> getType() {
        return this.type;
    }
    
    public void setType(final Class<T> type) {
        this.type = type;
    }
}
