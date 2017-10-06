package edu.nyu.oop;

import java.util.*;

import xtc.tree.Printer;
import edu.nyu.oop.constructs.JClass;
import edu.nyu.oop.util.VisitorUtil;

public class ArrayTemplateGenerator {

    private static final List<String> primitives = new LinkedList<String>(Arrays.asList(
                "int8_t",
                "int16_t",
                "int32_t",
                "int64_t",
                "float",
                "double",
                "char",
                "bool",
                "void"
            ));

    private static Set<String> nonPrimitive = new HashSet<String>();
    private static Set<String> primitive = new HashSet<String>();
    private static Map<String, JClass> classes;

    public static void addArrayType(String aType) {
        if (primitives.contains(aType))
            primitive.add(aType);
        else
            nonPrimitive.add(aType);
    }

    public static void print(Printer p, Map<String, JClass> srcClasses) {
        classes = srcClasses;
        for (String e : nonPrimitive)
            printNonPrimitive(e, p);
        for (String e : primitive)
            printPrimitive(e, p);
    }

    // template<>
    // java::lang::Class Array<int32_t>::__class()
    // {
    //     static java::lang::Class ik =
    //         new java::lang::__Class(__rt::literal("int"),
    //                                (java::lang::Class) __rt::null(),
    //                                (java::lang::Class) __rt::null(),
    //                                true);
    //
    //     static java::lang::Class k =
    //         new java::lang::__Class(literal("[I"),
    //                                 java::lang::__Object::__class(),
    //                                 ik);
    //     return k;
    // }

    private static void printPrimitive(String var, Printer p) {
        p.pln("template<>");
        p.pln("Class Array<" + var + ">::__class() {");
        p.incr();
        p.incr().indent().pln("static Class ik =");
        p.incr().indent().pln("new __Class(__rt::literal(\"" + var + "\"),");
        p.incr().indent().pln("(Class) __rt::null(),");
        p.indent().pln("(Class) __rt::null(),");
        p.indent().pln("true);");
        p.decr();
        p.decr();
        p.pln();

        printClassPrimitive(var, "__Object::__class()", "ik", p);

        p.decr().pln("}");
        p.pln();
    }

    // template<>
    // java::lang::Class Array<java::lang::Object>::__class()
    // {
    //     static java::lang::Class k =
    //         new java::lang::__Class(literal("[Ljava.lang.Object;"),
    //                                 java::lang::__Object::__class(),
    //                                 java::lang::__Object::__class());
    //     return k;
    // }

    private static void printNonPrimitive(String var, Printer p) {
        p.pln("template<>");
        p.pln("Class Array<" + var + ">::__class() {");

        JClass cls = classes.get(var);
        String parent = "__" + cls.getParent() + "::__class()";
        String type = "__" + var + "::__class()";

        p.incr();
        printClassNonPrimitive(var, parent, type, p);

        p.decr().pln("}");
        p.pln();
    }

    private static void printClassPrimitive(String var, String p1, String p2, Printer p) {
        p.indent().pln("static java::lang::Class k =");
        p.incr().indent().pln("new java::lang::__Class(__rt::literal(\"[" + Character.toUpperCase(var.toCharArray()[0]) + "\"),");
        p.incr().indent().pln(p1 + ",");
        p.indent().pln(p2 + ");");
        p.decr();
        p.pln();
        p.decr();
        p.indent().pln("return k;");
    }

    private static void printClassNonPrimitive(String var, String p1, String p2, Printer p) {
        JClass cls = classes.get(var);
        p.indent().pln("static java::lang::Class k =");
        p.incr().indent().pln("new java::lang::__Class(__rt::literal(\"[L" + VisitorUtil.javaPkgStr(cls.getPkg()) + "." + var + "\"),");
        p.incr().indent().pln(p1 + ",");
        p.indent().pln(p2 + ");");
        p.decr();
        p.pln();
        p.decr();
        p.indent().pln("return k;");
    }

    public static Set<String> getNonPrimitives() {
        return nonPrimitive;
    }

    public static Set<String> getPrimitives() {
        return primitive;
    }
}
