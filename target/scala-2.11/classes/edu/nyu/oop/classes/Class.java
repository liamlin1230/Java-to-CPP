package edu.nyu.oop.classes;

public abstract class Class {

    // public Class() {};
    public abstract String toString(Class c);
    public abstract String getName(Class c);
    public abstract Class getSuperClass(Class c);
    public abstract boolean isPrimitive(Class c);
    public abstract boolean isArray(Class c);
    public abstract Class getComponentType(Class c);
    public abstract boolean isInstance(Class c, Object o);
}
