package edu.nyu.oop;

import edu.nyu.oop.util.SymbolTableUtil;
import org.slf4j.Logger;
import xtc.lang.JavaEntities;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import xtc.util.SymbolTable;
import xtc.util.Runtime;
import xtc.type.*;
import xtc.type.Type.Tag;

import java.util.*;

public class SymbolTableLookupExample extends Visitor {
    final private Map<String, Tag> summary = new HashMap<>();

    final private SymbolTable table;
    final private Runtime runtime;

    public SymbolTableLookupExample(final Runtime runtime, final SymbolTable table) {
        this.runtime = runtime;
        this.table = table;
    }

    public void visitCompilationUnit(GNode n) {
        if (null == n.get(0))
            visitPackageDeclaration(null);
        else
            dispatch(n.getNode(0));

        SymbolTableUtil.enterScope(table, n);
        runtime.console().p("Entered scope ").pln(table.current().getName()).flush();

        for (int i = 1; i < n.size(); i++) {
            GNode child = n.getGeneric(i);
            dispatch(child);
        }

        SymbolTableUtil.exitScope(table, n);
    }

    public void visitPackageDeclaration(final GNode n) {
        SymbolTableUtil.enterScope(table, n);
        runtime.console().p("Entered scope ").pln(table.current().getName()).flush();
        SymbolTableUtil.exitScope(table, n);
    }

//    public void visitClassDeclaration(GNode n) {
//        visitClassBody((GNode) n.getNode(5));
//    }

    public void visitClassBody(GNode n) {
        SymbolTableUtil.enterScope(table, n);
        runtime.console().p("Entered scope ").pln(table.current().getName()).flush();
        visit(n);
        SymbolTableUtil.exitScope(table, n);
    }

    public void visitMethodDeclaration(GNode n) {
        SymbolTableUtil.enterScope(table, n);

        summary.put(n.getString(3)+"()", Tag.FUNCTION);

        // Extract a list representing the parameters to this method.
        List<VariableT> params = SymbolTableUtil.extractFormalParams(table.current());
        for(VariableT p : params) {
            summary.put(p.getName(), p.tag());
        }

        runtime.console().p("Entered scope ").pln(table.current().getName());
        visit(n);
        SymbolTableUtil.exitScope(table, n);
    }

    public void visitBlock(GNode n) {
        SymbolTableUtil.enterScope(table, n);
        runtime.console().p("Entered scope ").pln(table.current().getName());
        visit(n);
        SymbolTableUtil.exitScope(table, n);
    }

    public void visitForStatement(GNode n) {
        SymbolTableUtil.enterScope(table, n);
        runtime.console().p("Entered scope ").pln(table.current().getName());
        visit(n);
        SymbolTableUtil.exitScope(table, n);
    }

    public void visitPrimaryIdentifier(final GNode n) {
        String name = n.getString(0);

        if (table.current().isDefined(name)) {
            Type type = (Type) table.current().lookup(name);
            summary.put(name, type.tag());
            if (JavaEntities.isLocalT(type))
                runtime.console().p("Found occurrence of local variable ").p(name).p(" with type ").pln(type.tag().name());
        }
    }

    public void visit(GNode n) {
        for (Object o : n) {
            if (o instanceof Node) dispatch((Node) o);
        }
        runtime.console().flush();
    }

    public Map<String, Tag> getSummary(Node n) {
        this.dispatch(n);
        return this.summary;
    }

    public void printSummary() {
        runtime.console().pln("printSummary()");
        Set<String> keys = summary.keySet();
        for(String k : keys) {
            runtime.console().pln("  " + k + ":" + summary.get(k));
        }
        runtime.console().flush();
    }

}