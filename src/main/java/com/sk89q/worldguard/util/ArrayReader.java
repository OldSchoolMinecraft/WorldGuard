package com.sk89q.worldguard.util;

public class ArrayReader<T>
{
    private T[] arr;
    
    public ArrayReader(final T[] arr) {
        this.arr = arr;
    }
    
    public T get(final int index) {
        if (this.arr.length > index) {
            return this.arr[index];
        }
        return null;
    }
    
    public T get(final int index, final T def) {
        if (this.arr.length > index) {
            return this.arr[index];
        }
        return def;
    }
}
