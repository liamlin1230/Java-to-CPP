package edu.nyu.oop.visitors;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.nyu.oop.constructs.JClass;
import edu.nyu.oop.constructs.JConstructor;
import edu.nyu.oop.constructs.JIdentifier;
import edu.nyu.oop.constructs.JMethod;
import edu.nyu.oop.constructs.JType;
import edu.nyu.oop.util.RecursiveVisitor;
import edu.nyu.oop.util.VisitorUtil;

import xtc.tree.GNode;


public class MethodVisitor extends RecursiveVisitor {

    private BlockVisitor visitor;

    private List<Integer> mods;
    private List<JIdentifier> args;
    private String name = null;
    private JType retVal = null;

    private JClass cls = null;
    private JMethod method = null;

    public JMethod parse(GNode n, JClass cls) {
        this.cls = cls;

        mods = new LinkedList<Integer>();
        args = new LinkedList<JIdentifier>();

        name = n.getString(3);
        mods = VisitorUtil.getModifiers(n, "package");

        method = new JMethod(cls, mods, name, n);

        visitor = new BlockVisitor(method);

        dispatch(n);

        method.setRetType(getReturn(n));

        if (cls != null && !name.equals("__class") &&
                !Arrays.asList(JClass.bundled).contains(cls.getName()) &&
                !name.startsWith("init")) {
            VisitorUtil.decorateParameters(n, cls.getName(), 4);
            args.add(0, new JIdentifier(null, cls, "__this", cls.getName(), 0, null));
        }
        method.setArgs(args);

        return method;
    }

    public JMethod parseConstructor(GNode n, JClass cls) {
        this.cls = cls;

        mods = new LinkedList<Integer>();
        args = new LinkedList<JIdentifier>();

        name = n.getString(2);
        mods = VisitorUtil.getModifiers(n, "package");

        method = new JConstructor(cls, mods, name, n);

        visitor = new BlockVisitor(method);

        dispatch(n);

        method.setRetType(new JType(method, "contructor", null));
        method.setArgs(args);

        return method;
    }

    public void visitFormalParameter(GNode n) {
        String name = n.getString(3);
        String type = VisitorUtil.getType(n, 1);
        int dimensions = VisitorUtil.getDimensions(n, 1);

        GNode typeNode = (GNode) n.get(1);
        GNode ident = (GNode) typeNode.get(0);
        JIdentifier param = new JIdentifier(null, cls, name, type, dimensions, ident);
        param.decorate();
        param.decorateArray();
        args.add(param);
    }

    public void visitBlock(GNode n) {
        visitor.queue(n);
    }

    private JType getReturn(GNode n) {
        GNode w = (GNode) n.get(2);

        if (w.getName() == "VoidType") return new JType(method, "void", w);
        else {
            String type = VisitorUtil.getType(n, 2);
            int dimensions = VisitorUtil.getDimensions(n, 2);

            return new JType(method, type, dimensions, w);
        }
    }
}
