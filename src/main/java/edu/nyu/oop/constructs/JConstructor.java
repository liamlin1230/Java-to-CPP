package edu.nyu.oop.constructs;

import java.util.Arrays;
import java.util.List;

import edu.nyu.oop.util.VisitorUtil;
import edu.nyu.oop.visitors.MethodVisitor;

import xtc.tree.GNode;


public class JConstructor extends JMethod implements JModifiers {

    public JConstructor(JClass cls, List<Integer> mods, String name, GNode node) {
        super(cls, mods, name, node);
        this.isConstructor = true;
    }

    public void decorate() {
        if (Arrays.asList(JClass.bundled).contains(cls.getName())) return;

        GNode block = (GNode) node.get(5);
        GNode params = GNode.ensureVariable(GNode.create((GNode) node.get(3)));
        GNode thisNode = GNode.create("FormalParameter");
        thisNode.add(GNode.create("Modifiers"));
        thisNode.add(GNode.create("Type",
                                  GNode.create("QualifiedIdentifier", "__" + cls.getName() + "*"),
                                  GNode.create("Dimensions")));
        thisNode.add(null);
        thisNode.add("__this");
        thisNode.add(null);
        if (params.size() > 0)
            params.add(0, thisNode);
        else
            params.add(thisNode);
        GNode initFunc = VisitorUtil.createFunctionNode(
                             cls,
                             "init__" + cls.getName(),
                             "MethodDeclaration",
                             params,
                             block,
                             GNode.create("VoidType")
                         );

        GNode body = (GNode) cls.getNode().get(5);
        body = GNode.ensureVariable(body);
        body.add(initFunc);
        cls.getNode().set(5, body);

        node.set(2, "__" + cls.getName() + "::" + "__" + cls.getName());

        JClass parent = cls.getParentCls();
        String parentName = cls.getParent();

        GNode newBlock = GNode.create("Block");

        String initArgs = "(this";
        for (Object o : params) {
            GNode w = (GNode) o;
            if (w.getString(3) != "__this")
                initArgs = initArgs + ", " + w.getString(3);
        }
        initArgs = initArgs + ")";
        if (!Arrays.asList(JClass.bundled).contains(parentName) && parent != null)
            newBlock.add(GNode.create("this->__vptr->init__" + parentName + "(this);\n"));
        newBlock.add(GNode.create("this->__vptr->init__" + cls.getName() + initArgs + ";\n"));
        node.set(5, newBlock);

        MethodVisitor visitor = new MethodVisitor();
        JMethod initJMethod = visitor.parse(initFunc, cls);
        initJMethod.decorate();
        this.cls.addMethod(initJMethod);
    }

}
