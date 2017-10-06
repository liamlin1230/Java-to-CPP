package edu.nyu.oop.constructs;

import java.util.LinkedList;
import java.util.List;

import edu.nyu.oop.util.VisitorUtil;

import xtc.tree.GNode;


public class JMethod implements JScope, JModifiers {

    protected JClass cls;

    protected String name;
    protected List<JIdentifier> args;
    protected JType retType;
    protected List<Integer> mods;
    protected GNode node;

    protected boolean isConstructor;

    public JMethod(JMethod method) {
        this(method.getCls(), method.getMods(), method.getName(), method.getNode());
        this.setRetType(method.getRetType());
        this.setArgs(method.getArgs());
    }

    public JMethod(JClass cls, List<Integer> mods, String name, GNode node) {
        this.cls = cls;
        this.mods = mods;
        this.name = name;
        this.node = node;
        this.isConstructor = false;

        this.retType = null;
        this.args = new LinkedList<JIdentifier>();
    }

    public void decorate() {
        node.set(4, GNode.ensureVariable((GNode) node.get(4)));

        if (name.equals("main")) {
            GNode intType = GNode.create("PrimitiveType", "int");
            GNode intRet = GNode.create("Type", intType, null);
            node.set(2, intRet);
            node.set(4, GNode.create("FormalParameters"));
        } else
            node.set(3, "__" + cls.getName() + "::" + node.getString(3));
    }

// Getters and Setters // {{{

    public String getName() {
        return name;
    }

    public List<String> getPkg() {
        return cls.getPkg();
    }

    public JClass getCls() {
        return cls;
    }

    public List<JIdentifier> getArgs() {
        return args;
    }

    public void setArgs(List<JIdentifier> args) {
        this.args = args;
    }

    public List<Integer> getMods() {
        return mods;
    }

    public JType getRetType() {
        return retType;
    }

    public void setRetType(JType retType) {
        this.retType = retType;
    }

    public boolean isConstructor() {
        return this.isConstructor;
    }

    public GNode getNode() {
        return this.node;
    }

// }}}

}
