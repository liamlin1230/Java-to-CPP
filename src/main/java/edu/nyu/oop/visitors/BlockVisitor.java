package edu.nyu.oop.visitors;

import java.util.Map.Entry;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Arrays;

import edu.nyu.oop.constructs.JClass;
import edu.nyu.oop.constructs.JIdentifier;
import edu.nyu.oop.constructs.JMethod;
import edu.nyu.oop.constructs.JType;
import edu.nyu.oop.util.RecursiveVisitor;
import edu.nyu.oop.util.VisitorUtil;

import xtc.tree.GNode;

public class BlockVisitor extends RecursiveVisitor {

    //We don't model the java block so we need to decorate it manually with a visitor
    //We process all blocks after all classes have been resolved (Ghetto symbol table)

    private static Map<JMethod, GNode> blocks = new HashMap<JMethod, GNode>();

    private JClass cls;
    private JMethod method;

    private Map<String, GNode> local;

    private boolean insideThis = false;

    BlockVisitor(JMethod method) {
        this.method = method;
        this.cls = method.getCls();
        this.local = new HashMap<String, GNode>();
    }

    public BlockVisitor() {  }

    public void process(GNode n) {
        visit(n);
    }

    public void processAll() {
        for (Entry<JMethod, GNode> entry : blocks.entrySet()) {
            this.method = entry.getKey();
            this.cls = method.getCls();
            this.local = new HashMap<String, GNode>();
            visit(entry.getValue());
        }
    }

    public void queue(GNode n) {
        blocks.put(method, n);
    }

    public void visitPrimaryIdentifier(GNode n) {
        String ident = resolveIdent(n.getString(0));

        if (!method.getName().equals("main") &&
                !local.keySet().contains(ident) &&
                isMember(ident) &&
                !insideThis)
            n.set(0, "__this->" + ident);
        else if (method.getName().equals("main") &&
                 !local.keySet().contains(ident)) {
            n.set(0, "__" + cls.getName() + "::" + ident);
        }

        visit(n);
    }

    public void visitBlock(GNode n) {
        this.local = new HashMap<String, GNode>();
    }

    public void visitExpression(GNode n) {
        insideThis = false;

        visit(n);

        insideThis = false;
    }

    public void visitThisExpression(GNode n) {
        insideThis = true;

        visit(n);
    }

    public void visitType(GNode n) {
        GNode typeNode = (GNode) n.get(0);
        GNode dimNode = (GNode) n.get(1);

        int dim = 0;
        if (dimNode != null)
            dim = dimNode.size();

        JType type = new JType(method, typeNode.getString(0), dim, typeNode);
        type.decorate();
        type.decorateArray();

        visit(n);
    }

    public void visitPrimitiveType(GNode n) {
        JType type = new JType(method, n.getString(0), n);
        type.decorate();
    }

    public void visitStringLiteral(GNode n) {
        String originalStr = n.getString(0);

        if (originalStr.contains("__rt"))
            return; // already processed

        n.set(0, "__rt::literal(" + originalStr + ")");
    }

    public void visitFieldDeclaration(GNode n) {
        GNode declarators = (GNode) n.get(2);
        for (Object o : declarators) {
            if (o instanceof GNode) {
                GNode w = (GNode) o;
                local.put(w.getString(0), n);
            }
        }

        visit(n);
    }

    public void visitNewClassExpression(GNode n) {
        GNode qualifiedIdent = (GNode) n.get(2);
        String originalIdent = qualifiedIdent.getString(0);
        if (!originalIdent.startsWith("_")) // bundled classes
            qualifiedIdent.set(0, "__" + originalIdent);

        visit(n);
    }

    public void visitCallExpression(GNode n) {
        String func = n.getString(2);
        if (n.get(0) == null &&
                method.getName().equals("main") &&
                !func.equals("print") &&
                !func.equals("println")) {
            n.set(2, "__" + method.getCls().getName() + "::" + func);
            n.set(3, GNode.create("Arguments", GNode.create("NullLiteral")));
        } else
            visit(n);
    }

    public void visitSubscriptExpression(GNode n) {
        GNode originalIdent = (GNode) n.get(0);
        String originalType = originalIdent.getString(0);
        n.set(0, GNode.create("PrimaryIdentifier", "(*" + originalType + ")"));

        visit(n);
    }

    private String resolveIdent(String ident) {
        String newIdent = ident + "Member";
        for (Entry<JIdentifier, Object> entry : cls.getResolvedMembers().entrySet()) {
            JIdentifier member = entry.getKey();
            if (newIdent.equals(member.getName()))
                return newIdent;
        }

        return ident;
    }

    private boolean isMember(String ident) {
        for (Entry<JIdentifier, Object> entry : cls.getResolvedMembers().entrySet()) {
            JIdentifier member = entry.getKey();
            if (ident.equals(member.getName()))
                return true;
        }

        return false;
    }
}
