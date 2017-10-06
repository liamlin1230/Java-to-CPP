package edu.nyu.oop;

import java.util.*;
import java.util.Map.Entry;

import xtc.tree.Printer;
import edu.nyu.oop.constructs.JClass;
import edu.nyu.oop.util.VisitorUtil;

public class CastController {

    static class ParentChild {
        public String parent;
        public String child;
        ParentChild(String parent, String child) {
            this.parent = parent;
            this.child = child;
        }
    }

    private static Set<ParentChild> illegalCasts = new HashSet<>();

    static void traverseUpClass(JClass cls) {
        // temp hack check for Test
        if (cls.getName().startsWith("Test")) return;

        JClass parent = cls.getParentCls();
        while (parent != null && parent.getName() != "Object") {
            illegalCasts.add(new ParentChild(parent.getName(), cls.getName()));
            parent = parent.getParentCls();
        }
    }

    static void print(Printer p, Map<String, JClass> classes) {
        for (JClass cls : classes.values())
            traverseUpClass(cls);

        for (ParentChild pair : illegalCasts) {
            String cls = pair.child;
            String parent = pair.parent;

            if (!Arrays.asList(JClass.bundled).contains(parent))
                parent = "__" + parent;
            if (!Arrays.asList(JClass.bundled).contains(cls))
                cls = "__" + cls;

            p.pln("template<>");
            p.pln("template<>");
            p.pln("Ptr<" + cls + ">::Ptr(const Ptr<" + parent + ">& other) {");
            p.incr().indent().pln("throw new ClassCastException();");
            p.decr();
            p.pln("}");
            p.pln();
        }
    }
}
