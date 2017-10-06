package edu.nyu.oop.visitors;

import java.util.LinkedList;
import java.util.List;

import edu.nyu.oop.constructs.JClass;
import edu.nyu.oop.constructs.JIdentifier;
import edu.nyu.oop.constructs.JMethod;
import edu.nyu.oop.constructs.JType;
import edu.nyu.oop.util.RecursiveVisitor;
import edu.nyu.oop.util.VisitorUtil;

import xtc.tree.GNode;


public class IdentifierVisitor extends RecursiveVisitor {

    // public JIdentifier parseFormalParameter(GNode n) {
    //     List<Integer> mods = VisitorUtil.getModifiers(n, "local");
    //     String name = n.getString(3);
    //     String type = VisitorUtil.getType(n, 1);
    //     int dimensions = VisitorUtil.getDimensions(n, 1);

    //     return new JIdentifier(mods, name, type, dimensions);
    // }

}
