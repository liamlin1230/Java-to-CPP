package edu.nyu.oop;

import java.util.*;
import java.util.Map.Entry;

import xtc.tree.Printer;
import edu.nyu.oop.constructs.*;
import edu.nyu.oop.util.VisitorUtil;

public class StaticVariableController {

    private static Map<JIdentifier, Object> staticVariables = new HashMap<>();

    public static void addStatic(JIdentifier var, Object val) {
        staticVariables.put(var, val);
    }

    static void print(Printer p) {
        for (Entry<JIdentifier, Object> entry : staticVariables.entrySet()) {
            JIdentifier var = entry.getKey();
            Object val = entry.getValue();

            p.p(var.getString() + " __" + var.getScope().getName() + "::" + var.getName() + " = ");
            if (val == null && var.getString() == "String")
                p.p("\"\"");
            else if (val == null)
                p.p("0");
            else
                p.p(val.toString());
            p.pln(";");
        }
    }
}
