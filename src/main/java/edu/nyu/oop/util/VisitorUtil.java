package edu.nyu.oop.util;

import java.util.LinkedList;
import java.util.List;

import edu.nyu.oop.constructs.JClass;
import edu.nyu.oop.constructs.JModifiers;

import xtc.tree.GNode;


public class VisitorUtil {

    public static List<Integer> getModifiers(GNode n, String defaultMod) {
        List<Integer> mods = new LinkedList<Integer>();

        for (Object w : (GNode) n.get(0)) {
            String mod = ((GNode) w).getString(0);
            mods.add(new Integer(mapModifier(mod)));
        }

        if (mods.size() == 0)
            mods.add(new Integer(mapModifier(defaultMod)));

        return mods;
    }

    public static String javaPkgStr(List<String> pkg) {
        String pkgStr = "";

        if (pkg != null)
            for (String str : pkg)
                pkgStr = pkgStr + str + ".";

        return pkgStr.substring(0, pkgStr.length() - 1);
    }

    public static String pkgStr(List<String> pkg) {
        String pkgStr = "";

        if (pkg != null)
            for (String str : pkg)
                pkgStr = pkgStr + str + "::";

        return pkgStr;
    }

    public static GNode createFunctionNode(JClass cls, String funcName, String nodeName, GNode params, GNode block, GNode retType) {
        GNode function = GNode.create(nodeName);

        function.add(GNode.create("Modifiers"));
        function.add(null);

        if (retType != null)
            function.add(retType);

        function.add(funcName);

        if (params != null)
            function.add(params);
        else
            function.add(GNode.create("FormalParameters"));

        function.add(null);
        if (retType != null) // extra null for method declarations
            function.add(null);

        if (block != null)
            function.add(block);
        else
            function.add(GNode.create("Block"));

        return function;
    }

    public static void decorateParameters(GNode n, String currentClass, int i) {
        GNode paramList = (GNode) n.get(i);

        GNode newParam = GNode.create("FormalParameter");
        GNode type = GNode.create("Type");
        GNode ident = GNode.create("QualifiedIdentifier", currentClass);

        type.add(ident);
        type.add(null);

        newParam.add(GNode.create("Modifiers"));
        newParam.add(type);
        newParam.add(null);
        newParam.add("__this");
        newParam.add(null);

        paramList = GNode.ensureVariable(paramList);
        if (paramList.size() > 0)
            paramList.add(0, newParam);
        else
            paramList.add(newParam);

        n.set(i, paramList);
    }

    private static Integer mapModifier(String mod) {
        switch (mod) {
        case "public":
            return JModifiers.PUBLIC;
        case "private":
            return JModifiers.PRIVATE;
        case "protected":
            return JModifiers.PROTECTED;
        case "static":
            return JModifiers.STATIC;
        case "final":
            return JModifiers.FINAL;
        case "package":
            return JModifiers.PACKAGE;
        case "local":
            return JModifiers.LOCAL;
        }

        return -1; //error?
    }

    // i is the ith child node of the node n

    public static String getType(GNode n, int i) {
        GNode w = (GNode) n.get(i);
        return ((GNode) w.get(0)).getString(0);
    }

    public static int getDimensions(GNode n, int i) {
        GNode w = (GNode) ((GNode) n.get(i)).get(1);
        if (w != null)
            return w.size();
        else
            return 0;
    }

    public static Object parseLiteral(GNode n, int i) {
        GNode w = (GNode) n.get(i);
        if (w == null) return null;
        String str = w.getString(0);

        switch (w.getName()) {
        case "StringLiteral":
            return str.substring(1, str.length() - 1);
        case "CharacterLiteral":
            return str.charAt(1);
        case "IntegerLiteral":
            return Integer.parseInt(str);
        case "FloatingPointLiteral":
            return Float.parseFloat(str);
        case "BooleanLiteral":
            return Boolean.parseBoolean(str);
        }

        return null; // something went wrong
    }

}
