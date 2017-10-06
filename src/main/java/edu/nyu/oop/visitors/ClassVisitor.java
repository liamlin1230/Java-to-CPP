package edu.nyu.oop.visitors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.nyu.oop.constructs.*;
import edu.nyu.oop.StaticVariableController;
import edu.nyu.oop.util.RecursiveVisitor;
import edu.nyu.oop.util.VisitorUtil;

import xtc.tree.GNode;
import xtc.tree.Node;


public class ClassVisitor extends RecursiveVisitor {

    MethodVisitor visitor = new MethodVisitor();

    private JClass cls = null;

    private List<Integer> mods;
    private String parent;
    private String name;
    private List<JMethod> methods;
    private List<JMethod> constructors;
    private Map<JIdentifier, Object> members;

    private Map<String, JClass> srcClasses;

    public JClass process(GNode n, JSource source, Map<String, JClass> srcClasses) {
        this.srcClasses = srcClasses;

        mods = VisitorUtil.getModifiers(n, "package");
        name = n.getString(1);
        parent = "Object";

        cls = new JClass(source, mods, name, parent, n);

        methods = new LinkedList<JMethod>();
        constructors = new LinkedList<JMethod>();
        members = new HashMap<JIdentifier, Object>();

        if (source != null)
            cls.setPkg(source.getPkg());

        getExtension((GNode) n.get(3));

        dispatch(n);

        return cls;
    }

    public void visitFieldDeclaration(GNode n) {
        List<Integer> mods = VisitorUtil.getModifiers(n, "package");
        String type = VisitorUtil.getType(n, 1);
        int dimensions = VisitorUtil.getDimensions(n, 1);

        for (Object w : (Node) n.get(2)) {
            if (w instanceof Node) {
                String name = ((GNode) w).getString(0);
                Object value = VisitorUtil.parseLiteral((GNode) w, 2);
                JIdentifier ident = new JIdentifier(mods, cls, name, type, dimensions, (GNode) w);
                if (mods.contains(JModifiers.STATIC))
                    StaticVariableController.addStatic(ident, value);
                ident.decorate();
                ident.decorateArray();
                cls.addMember(ident, value);
            }
        }
    }

    public void visitMethodDeclaration(GNode n) {
        JMethod method = visitor.parse(n, cls);

        // if (method.getName().equals("main"))
        //     cls.setIsMainClass(true);

        method.decorate();
        cls.addMethod(method);
    }

    public void visitConstructorDeclaration(GNode n) {
        JMethod constructor = visitor.parseConstructor(n, cls);

        constructor.decorate();
        cls.addConstructor(constructor);
    }

    public void getExtension(GNode n) {
        if (n == null) parent = "Object";
        else {
            GNode w = (GNode) n.get(0);
            parent = ((GNode) w.get(0)).getString(0);

            cls.setParent(parent);
        }

        if (!name.equals("Object") && srcClasses != null)
            cls.setParentCls(srcClasses.get(parent));
    }
}
