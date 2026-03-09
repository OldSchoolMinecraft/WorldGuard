package au.com.bytecode.opencsv.bean;

import java.io.*;
import au.com.bytecode.opencsv.*;
import java.lang.reflect.*;
import java.util.*;
import java.beans.*;

public class CsvToBean<T>
{
    Map<Class<?>, PropertyEditor> editorMap;
    
    public CsvToBean() {
        this.editorMap = null;
    }
    
    public List<T> parse(final MappingStrategy<T> mapper, final Reader reader) {
        return this.parse(mapper, new CSVReader(reader));
    }
    
    public List<T> parse(final MappingStrategy<T> mapper, final CSVReader csv) {
        try {
            mapper.captureHeader(csv);
            final List<T> list = new ArrayList<T>();
            String[] line;
            while (null != (line = csv.readNext())) {
                final T obj = this.processLine(mapper, line);
                list.add(obj);
            }
            return list;
        }
        catch (Exception e) {
            throw new RuntimeException("Error parsing CSV!", e);
        }
    }
    
    protected T processLine(final MappingStrategy<T> mapper, final String[] line) throws IllegalAccessException, InvocationTargetException, InstantiationException, IntrospectionException {
        final T bean = mapper.createBean();
        for (int col = 0; col < line.length; ++col) {
            final String value = line[col];
            final PropertyDescriptor prop = mapper.findDescriptor(col);
            if (null != prop) {
                final Object obj = this.convertValue(value, prop);
                prop.getWriteMethod().invoke(bean, obj);
            }
        }
        return bean;
    }
    
    protected Object convertValue(final String value, final PropertyDescriptor prop) throws InstantiationException, IllegalAccessException {
        final PropertyEditor editor = this.getPropertyEditor(prop);
        Object obj = value;
        if (null != editor) {
            editor.setAsText(value.trim());
            obj = editor.getValue();
        }
        return obj;
    }
    
    private PropertyEditor getPropertyEditorValue(final Class<?> cls) {
        if (this.editorMap == null) {
            this.editorMap = new HashMap<Class<?>, PropertyEditor>();
        }
        PropertyEditor editor = this.editorMap.get(cls);
        if (editor == null) {
            editor = PropertyEditorManager.findEditor(cls);
            this.addEditorToMap(cls, editor);
        }
        return editor;
    }
    
    private void addEditorToMap(final Class<?> cls, final PropertyEditor editor) {
        if (editor != null) {
            this.editorMap.put(cls, editor);
        }
    }
    
    protected PropertyEditor getPropertyEditor(final PropertyDescriptor desc) throws InstantiationException, IllegalAccessException {
        final Class<?> cls = desc.getPropertyEditorClass();
        if (null != cls) {
            return (PropertyEditor)cls.newInstance();
        }
        return this.getPropertyEditorValue(desc.getPropertyType());
    }
}
