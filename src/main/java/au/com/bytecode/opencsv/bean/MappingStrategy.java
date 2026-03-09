package au.com.bytecode.opencsv.bean;

import java.beans.*;
import au.com.bytecode.opencsv.*;
import java.io.*;

public interface MappingStrategy<T>
{
    PropertyDescriptor findDescriptor(final int p0) throws IntrospectionException;
    
    T createBean() throws InstantiationException, IllegalAccessException;
    
    void captureHeader(final CSVReader p0) throws IOException;
}
