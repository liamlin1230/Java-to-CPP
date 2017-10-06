package edu.nyu.oop;

import java.util.*;
import java.util.Map.Entry;
import java.io.*;

import xtc.tree.Comment;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Printer;
import xtc.tree.Token;
import xtc.tree.Visitor;

import edu.nyu.oop.util.XtcProps;
import edu.nyu.oop.constructs.*;

public class CppImplementationPrinter
    extends Visitor {
    public static final int PREC_BASE = 0;
    public static final int PREC_LIST = 10;
    public static final int PREC_CONSTANT = 1;
    public static final int STMT_ANY = 0;
    public static final int STMT_IF = 1;
    public static final int STMT_IF_ELSE = 2;
    protected final Printer printer;
    protected List<String> packageName;
    protected String currentClass;
    protected boolean isDeclaration;
    protected boolean isStatement;
    protected boolean isOpenLine;
    protected boolean isNested;
    protected boolean isIfElse;
    protected boolean isInClass;
    protected boolean isInMethod;
    protected boolean isMain;
    protected int precedence;
    protected GNode ast;

    protected boolean holdMain = true;
    protected boolean parensPrinted = false;
    protected GNode main;

    protected Map<String, JClass> classes;

    private String outputLocation = XtcProps.get("output.location");

    public CppImplementationPrinter(Node ast, Map<String, JClass> classes) {
        Writer w = null;
        this.ast = (GNode) ast;
        this.classes = classes;

        try {
            FileOutputStream fos = new FileOutputStream(outputLocation + "/output.cpp");
            OutputStreamWriter ows = new OutputStreamWriter(fos, "utf-8");
            w = new BufferedWriter(ows);
            this.printer = new Printer(w);
        } catch (Exception e) {
            throw new RuntimeException("Output location not found. Create the /output directory.");
        }

        // Register the visitor as being associated with this printer.
        // We do this so we get some nice convenience methods on the printer,
        // such as "dispatch", You should read the code for Printer to learn more.
        printer.register(this);
    }

    public void print() {
        headOfFile();
        printer.pln();
        printer.pln("namespace __rt {");
        printer.pln();
        ArrayTemplateGenerator.print(printer, classes);
        CastController.print(printer, classes);
        printer.pln("}");
        visitCompilationUnit(ast);
        printer.pln();
        StaticVariableController.print(printer);
        tailOfFile();
        printer.flush(); // important!
    }

    private void printIndentedLine(String line, boolean newLine) {
        printer.indent();
        printer.indent();
        if (newLine)
            printer.pln(line);
        else
            printer.p(line);
    }

    private void tailOfFile() {
        isMain = true;
        holdMain = false;
        printer.pln();
        printer.p(main);
    }

    private void headOfFile() {
        printer.pln("#pragma once");
        printer.pln();
        printer.pln("#include \"java_lang.h\"");
        printer.pln("#include \"output.h\"");
        printer.pln();
        printer.pln("#include <sstream>");
        printer.pln("#include <iostream>");
        printer.pln();
        printUniqueClassNamespaces();
        printer.pln("using namespace std;");
        printer.pln();
    }

    private void printUniqueClassNamespaces() {
        Set<List<String>> pkgs = new HashSet<List<String>>();
        for (JClass cls : classes.values()) {
            pkgs.add(cls.getPkg());
        }
        for (List<String> pkg : pkgs) {
            printer.pln("using namespace " + pkgStr(pkg) + ";");
        }
    }

    protected String fold(GNode paramGNode, int paramInt) {
        StringBuilder localStringBuilder = new StringBuilder();
        for (int i = 0; i < paramInt; i++) {
            localStringBuilder.append(paramGNode.getString(i));
            if (i < paramInt - 1) {
                localStringBuilder.append('.');
            }
        }
        return localStringBuilder.toString();
    }

    protected String getPackage(GNode paramGNode) {
        assert (paramGNode.hasName("ImportDeclaration"));

        GNode localGNode = paramGNode.getGeneric(1);
        int i = localGNode.size();
        if (null == paramGNode.get(2)) {
            i--;
        }
        return 0 >= i ? "" : fold(localGNode, i);
    }

    private static final Visitor containsLongExprVisitor = new Visitor() {
        public Boolean visitBlock(GNode paramAnonymousGNode) {
            return Boolean.TRUE;
        }

        public Boolean visitArrayInitializer(GNode paramAnonymousGNode) {
            return Boolean.TRUE;
        }

        public Boolean visit(GNode paramAnonymousGNode) {
            for (Object localObject : paramAnonymousGNode) {
                if (((localObject instanceof Node)) && (((Boolean)dispatch((Node)localObject)).booleanValue())) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }
    };

    protected boolean containsLongExpression(GNode paramGNode) {
        return ((Boolean)containsLongExprVisitor.dispatch(paramGNode)).booleanValue();
    }

    protected boolean isLongDeclaration(GNode paramGNode) {
        return (paramGNode.hasName("ConstructorDeclaration")) || (paramGNode.hasName("ClassDeclaration")) || (paramGNode.hasName("InterfaceDeclaration")) || (paramGNode.hasName("AnnotationDeclaration")) || (paramGNode.hasName("EnumDeclaration")) || (paramGNode.hasName("BlockDeclaration")) || ((paramGNode.hasName("MethodDeclaration")) && (null != paramGNode.get(7))) || ((paramGNode.hasName("FieldDeclaration")) && (containsLongExpression(paramGNode))) || ((paramGNode.hasName("AnnotationMethod")) && (containsLongExpression(paramGNode)));
    }

    protected void printDeclsAndStmts(GNode paramGNode) {
        isOpenLine = false;
        isNested = false;
        isIfElse = false;
        isDeclaration = false;
        isStatement = false;
        Object localObject1 = null;

        for (Object localObject2 : paramGNode) {
            Node localNode = (Node)localObject2;
            if (null != localNode) {
                //     if (localNode instanceof String)
                //         localNode = GNode.create("__" + currentClass + "::" + localNode);
                GNode localGNode = GNode.cast(localNode);
                if ((null != localObject1) && ((((GNode)localObject1).hasName("Block")) || ((isLongDeclaration((GNode)localObject1)) && (localGNode.getName().endsWith("Declaration"))) || (localGNode.hasName("Block")) || (isLongDeclaration(localGNode)) || ((!((GNode)localObject1).getName().endsWith("Declaration")) && (localGNode.getName().endsWith("Declaration"))))) {
                    printer.pln();
                }
                printer.p(localNode);
                if (isOpenLine) {
                    printer.pln();
                }
                isOpenLine = false;
                localObject1 = localGNode;
            }
        }
    }

    protected void formatAsTruthValue(Node paramNode) {
        if (GNode.cast(paramNode).hasName("AssignmentExpression")) {
            printer.p('(').p(paramNode).p(')');
        } else {
            printer.p(paramNode);
        }
    }

    protected void formatDimensions(int paramInt, String type) {
//        if (paramInt == 0) {
//            printer.p(type);
//            return;
//        }
//        printer.p("Array<");
//        formatDimensions(paramInt - 1, type);
//        printer.p(">");
    }

    protected boolean startStatement(int paramInt) {
        if ((isIfElse) && ((1 == paramInt) || (2 == paramInt))) {
            isNested = false;
        } else {
            if (isOpenLine) {
                printer.pln();
            }
            if (isDeclaration) {
                printer.pln();
            }
            if (isNested) {
                printer.incr();
            }
        }
        isOpenLine = false;
        boolean bool = isNested;
        isNested = false;

        return bool;
    }

    protected void prepareNested() {
        isDeclaration = false;
        isStatement = false;
        isOpenLine = true;
        isNested = true;
    }

    protected void endStatement(boolean paramBoolean) {
        if (paramBoolean) {
            printer.decr();
        }
        isDeclaration = false;
        isStatement = true;
    }

    protected int enterContext(int paramInt) {
        int i = precedence;
        precedence = paramInt;
        return i;
    }

    protected int enterContext() {
        int i = precedence;
        precedence += 1;
        return i;
    }

    protected void exitContext(int paramInt) {
        precedence = paramInt;
    }

    protected int startExpression(int paramInt) {
        if (paramInt < precedence) {
            printer.p('(');
        }
        int i = precedence;
        precedence = paramInt;
        return i;
    }

    protected void endExpression(int paramInt) {
        if (precedence < paramInt) {
            printer.p(')');
        }
        precedence = paramInt;
    }

    public void visit(GNode node) {
        printer.indent().p(node.getName());
    }

    // public void visit(Comment paramComment) {
    //     printer.indent().p(paramComment).p(paramComment.getNode());
    // }

    public void visitCompilationUnit(GNode paramGNode) {
        packageName = null;
        isDeclaration = false;
        isStatement = false;
        isOpenLine = false;
        isNested = false;
        isIfElse = false;
        isInClass = false;
        isInMethod = false;
        precedence = 0;

        printDeclsAndStmts(paramGNode);
    }

    public void visitPackageDeclaration(GNode paramGNode) {
        // GNode localGNode = paramGNode.getGeneric(1);
        // packageName = fold(localGNode, localGNode.size());
        //
        // printer.indent().p(paramGNode.getNode(0)).p("package ").p(paramGNode.getNode(1)).pln(';');
        // isOpenLine = false;
        packageName = new LinkedList<String>();
        GNode w = (GNode) paramGNode.get(1);
        for (Object o : w)
            if (o instanceof String)
                packageName.add((String) o);
    }

    public void visitImportDeclaration(GNode paramGNode) {
        // String str = getPackage(paramGNode);
        // if ((null != packageName) && (!str.equals(packageName))) {
        //     printer.pln();
        // }
        // packageName = str;

        // printer.indent().p("import ");
        // if (null != paramGNode.get(0)) {
        //     printer.p("static ");
        // }
        // printer.p(paramGNode.getNode(1));
        // if (null != paramGNode.get(2)) {
        //     printer.p(".*");
        // }
        // printer.pln(';');
        // isOpenLine = false;
    }

    public void visitModifiers(GNode paramGNode) {
        if (isInMethod) return;
        Object localObject;
        for (Iterator localIterator = paramGNode.iterator(); localIterator.hasNext(); printer.p((Node)localObject).p(' ')) {
            localObject = localIterator.next();
        }
    }

    public void visitModifier(GNode paramGNode) {
        String mod = paramGNode.getString(0);
        if (!mod.equals("public"))
            printer.p(mod);
    }

    public void visitFormalParameter(GNode paramGNode) {
        int i = paramGNode.size();
        printer.p(paramGNode.getNode(0)).p(paramGNode.getNode(1));
        for (int j = 2; j < i - 3; j++) {
            printer.p(" | ").p(paramGNode.getNode(j));
        }
        if (null != paramGNode.get(i - 3)) {
            printer.p(paramGNode.getString(i - 3));
        }
        printer.p(' ').p(paramGNode.getString(i - 2)).p(paramGNode.getNode(i - 1));
    }

    public void visitFinalClause(GNode paramGNode) {
        printer.p("final");
    }

    public void visitFormalParameters(GNode paramGNode) {
        printer.p('(');
        for (Iterator localIterator = paramGNode.iterator(); localIterator.hasNext();) {
            printer.p((Node)localIterator.next());
            if (localIterator.hasNext()) {
                printer.p(", ");
            }
        }
        printer.p(')');
    }

    public void visitDeclarator(GNode paramGNode) {
        printer.p(paramGNode.getString(0));
//        if (null != paramGNode.get(1)) {
//            if (Token.test(paramGNode.get(1))) {
//                formatDimensions(((GNode) paramGNode.get(1)).getString(0).length(), ((GNode) paramGNode.get(0)).getString(0));
//            } else {
//                printer.p(paramGNode.getNode(1));
//            }
//        }
        if (null != paramGNode.get(2)) {
            printer.p(" = ").p(paramGNode.getNode(2));
        }
    }

    public void visitDeclarators(GNode paramGNode) {
        for (Iterator localIterator = paramGNode.iterator(); localIterator.hasNext();) {
            printer.p((Node)localIterator.next());
            if (localIterator.hasNext()) {
                printer.p(", ");
            }
        }
    }

    public void visitAnnotations(GNode paramGNode) {
        Object localObject;
        for (Iterator localIterator = paramGNode.iterator(); localIterator.hasNext(); printer.p((Node)localObject).p(' ')) {
            localObject = localIterator.next();
        }
    }

    public void visitAnnotation(GNode paramGNode) {
        printer.p('@').p(paramGNode.getNode(0));
        if (null != paramGNode.get(1)) {
            printer.p('(').p(paramGNode.getNode(1)).p(')');
        }
    }

    public void visitElementValuePairs(GNode paramGNode) {
        int i = 1;
        for (Object localObject : paramGNode) {
            if (i != 0) {
                i = 0;
            } else {
                printer.p(", ");
            }
            printer.p((Node)localObject);
        }
    }

    public void visitElementValuePair(GNode paramGNode) {
        printer.p(paramGNode.getNode(0)).p(" = ").p(paramGNode.getNode(1));
    }

    public void visitDefaultValue(GNode paramGNode) {
        printer.p("default ").p(paramGNode.getNode(0));
    }

    public void visitClassBody(GNode paramGNode) {
        if (isOpenLine) {
            printer.p(' ');
        }
        // printer.pln('{').incr();
        isInMethod = false;

        printDeclsAndStmts(paramGNode);

        // printer.decr().indent().p('}');
        isOpenLine = true;
        isNested = false;
        isIfElse = false;
    }

    public void visitFieldDeclaration(GNode paramGNode) {
        if (isInMethod)
            printer.indent().p(paramGNode.getNode(0)).p(paramGNode.getNode(1)).p(' ').p(paramGNode.getNode(2)).p(';').pln();

        // isDeclaration = true;
        isOpenLine = false;
    }

    public void visitMethodDeclaration(GNode paramGNode) {
        isInClass = false;
        isInMethod = true;
        if (paramGNode.getString(3).equals("main") && holdMain) {
            main = paramGNode;
            return;
        } else
            isMain = false;
        printer.indent().p(paramGNode.getNode(0));
        if (null != paramGNode.get(1)) {
            printer.p(paramGNode.getNode(1)).p(' ');
        }
        printer.p(paramGNode.getNode(2));
        if (!"<init>".equals(paramGNode.get(3))) {
            printer.p(' ').p(paramGNode.getString(3));
        }
        printer.p(paramGNode.getNode(4));
        if (null != paramGNode.get(5)) {
            printer.p(' ').p(paramGNode.getNode(5));
        }
        if (null != paramGNode.get(6)) {
            printer.p(' ').p(paramGNode.getNode(6));
        }
        if (null != paramGNode.get(7)) {
            isOpenLine = true;
            printer.p(paramGNode.getNode(7)).pln();
        } else {
            printer.pln(';');
        }
        isOpenLine = false;
    }

    public void visitConstructorDeclaration(GNode paramGNode) {
        isInClass = false;
        isInMethod = true;
        printer.indent().p(paramGNode.getNode(0));
        if (null != paramGNode.get(1)) {
            printer.p(paramGNode.getNode(1));
        }
        printer.p(paramGNode.getString(2)).p(paramGNode.getNode(3));
        if (null != paramGNode.get(4)) {
            printer.p(paramGNode.getNode(4));
        }
        printer.p(" : __vptr(&vtable)");
        printInitializationList(classes.get(currentClass));
        isOpenLine = true;
        printer.p(paramGNode.getNode(5));
    }

    private void printInitializationList(JClass cls) {
        Map<JIdentifier, Object> members = cls.getResolvedMembers();
        if (members != null) {
            for (Entry<JIdentifier, Object> entry : members.entrySet()) {
                JIdentifier type = entry.getKey();
                Object value = entry.getValue();

                if (!type.getMods().contains(JModifiers.STATIC)) {
                    printer.p(", " + type.getName() + "(");
                    if (value != null) {
                        if (value instanceof String)
                            printer.p("__rt::literal(\"" + value + "\")");
                        else
                            printer.p(value.toString());
                    }
                    printer.p(")");
                }
            }
        }
    }

    public void visitClassDeclaration(GNode paramGNode) {
        isInClass = true;
        isInMethod = false;
        currentClass = paramGNode.getString(1);
        printDeclsAndStmts((GNode) paramGNode.getNode(5));

        if (!classes.get(currentClass).isMainClass()) {
            printer.pln();
            printer.pln("__" + currentClass + "_VT " + "__" + currentClass + "::" + "vtable;");
        }
    }

    // public void visitInterfaceDeclaration(GNode paramGNode)
    // {
    //   printer.indent().p(paramGNode.getNode(0)).p("interface ").p(paramGNode.getString(1)).p(paramGNode.getNode(2));
    //   if (null != paramGNode.get(3)) {
    //     printer.p(' ').p(paramGNode.getNode(3));
    //   }
    //   isOpenLine = true;
    //   printer.p(paramGNode.getNode(4)).pln();
    //   isDeclaration = true;
    //   isOpenLine = false;
    // }
    //
    // public void visitAnnotationDeclaration(GNode paramGNode)
    // {
    //   printer.indent().p(paramGNode.getNode(0)).p("@interface ").p(paramGNode.getString(1));
    //   isOpenLine = true;
    //   printer.p(paramGNode.getNode(2)).pln();
    //   isDeclaration = true;
    //   isOpenLine = false;
    // }
    //
    // public void visitAnnotationMethod(GNode paramGNode)
    // {
    //   printer.indent().p(paramGNode.getNode(0)).p(paramGNode.getNode(1)).p(' ').p(paramGNode.getString(2)).p("()");
    //   if (null != paramGNode.get(3)) {
    //     printer.p(" default ").p(paramGNode.getNode(3));
    //   }
    //   printer.pln(';');
    //   isOpenLine = false;
    // }
    //
    // public void visitEnumDeclaration(GNode paramGNode)
    // {
    //   printer.indent().p(paramGNode.getNode(0)).p("enum ").p(paramGNode.getString(1));
    //   if (null != paramGNode.get(2)) {
    //     printer.p(' ').p(paramGNode.getNode(2));
    //   }
    //   printer.pln(" {").incr();
    //
    //   isOpenLine = false;
    //   isNested = false;
    //   isIfElse = false;
    //   printer.p(paramGNode.getNode(3));
    //   if (null != paramGNode.get(4))
    //   {
    //     printer.pln(';').pln();
    //     isOpenLine = false;
    //     isNested = false;
    //     isIfElse = false;
    //     printer.p(paramGNode.getNode(4));
    //   }
    //   if (isOpenLine) {
    //     printer.pln();
    //   }
    //   printer.decr().indent().pln('}');
    //   isOpenLine = false;
    //   isNested = false;
    //   isIfElse = false;
    //   isDeclaration = true;
    // }
    //
    // public void visitEnumConstants(GNode paramGNode)
    // {
    //   for (Iterator localIterator = paramGNode.iterator(); localIterator.hasNext();)
    //   {
    //     isDeclaration = false;
    //     printer.indent().p((Node)localIterator.next());
    //     if (localIterator.hasNext())
    //     {
    //       printer.pln(',');
    //       if (isDeclaration) {
    //         printer.pln();
    //       }
    //     }
    //   }
    //   isOpenLine = true;
    // }
    //
    // public void visitEnumConstant(GNode paramGNode)
    // {
    //   printer.p(paramGNode.getNode(0)).p(paramGNode.getString(1)).p(paramGNode.getNode(2));
    //   if (null != paramGNode.get(3))
    //   {
    //     isOpenLine = true;
    //     printer.p(paramGNode.getNode(3));
    //     isDeclaration = true;
    //   }
    //   else
    //   {
    //     isDeclaration = false;
    //   }
    // }
    //
    // public void visitEnumMembers(GNode paramGNode)
    // {
    //   printDeclsAndStmts(paramGNode);
    // }

    public void visitBlockDeclaration(GNode paramGNode) {
        printer.indent();
        if (null != paramGNode.get(0)) {
            printer.p(paramGNode.getString(0));
            isOpenLine = true;
        }
        printer.p(paramGNode.getNode(1)).pln();
        isOpenLine = false;
    }

    public void visitEmptyDeclaration(GNode paramGNode) {}

    public void visitThrowsClause(GNode paramGNode) {
        printer.p("throws ");
        for (Iterator localIterator = paramGNode.iterator(); localIterator.hasNext();) {
            printer.p((Node)localIterator.next());
            if (localIterator.hasNext()) {
                printer.p(", ");
            }
        }
    }

    public void visitExtension(GNode paramGNode) {
        printer.p("extends ");
        for (Iterator localIterator = paramGNode.iterator(); localIterator.hasNext();) {
            printer.p((Node)localIterator.next());
            if (localIterator.hasNext()) {
                printer.p(", ");
            }
        }
    }

    public void visitImplementation(GNode paramGNode) {
        printer.p("implements ");
        for (Iterator localIterator = paramGNode.iterator(); localIterator.hasNext();) {
            printer.p((Node)localIterator.next());
            if (localIterator.hasNext()) {
                printer.p(", ");
            }
        }
    }

    public void visitBlock(GNode paramGNode) {
        if (isOpenLine) {
            printer.p(' ');
        } else {
            printer.indent();
        }
        printer.pln('{').incr();

        isOpenLine = false;
        isNested = false;
        isIfElse = false;
        isDeclaration = false;
        isStatement = false;

        printDeclsAndStmts(paramGNode);

        printer.decr().indent().p('}');
        isOpenLine = true;
        isNested = false;
        isIfElse = false;
    }

    public void visitConditionalStatement(GNode paramGNode) {
        int i = null == paramGNode.get(2) ? 1 : 2;
        boolean bool1 = startStatement(i);
        if (isIfElse) {
            printer.p(' ');
        } else {
            printer.indent();
        }
        printer.p("if (").p(paramGNode.getNode(0)).p(')');
        prepareNested();
        printer.p(paramGNode.getNode(1));
        if (null != paramGNode.get(2)) {
            if (isOpenLine) {
                printer.p(" else");
            } else {
                printer.indent().p("else");
            }
            prepareNested();
            boolean bool2 = isIfElse;
            isIfElse = true;
            printer.p(paramGNode.getNode(2));
            isIfElse = bool2;
        }
        endStatement(bool1);
    }

    public void visitForStatement(GNode paramGNode) {
        boolean bool = startStatement(0);

        printer.indent().p("for (").p(paramGNode.getNode(0)).p(')');
        prepareNested();
        printer.p(paramGNode.getNode(1));

        endStatement(bool);
    }

    public void visitBasicForControl(GNode paramGNode) {
        printer.p(paramGNode.getNode(0));
        if (null != paramGNode.get(1)) {
            printer.p(paramGNode.getNode(1)).p(' ');
        }
        int i = enterContext(0);
        printer.p(paramGNode.getNode(2)).p("; ");
        exitContext(i);
        if (null != paramGNode.get(3)) {
            int j = enterContext(0);
            formatAsTruthValue(paramGNode.getNode(3));
            exitContext(j);
        }
        printer.p("; ");

        int j = enterContext(0);
        printer.p(paramGNode.getNode(4));
        exitContext(j);
    }

    public void visitEnhancedForControl(GNode paramGNode) {
        printer.p(paramGNode.getNode(0)).p(paramGNode.getNode(1)).p(' ').p(paramGNode.getString(2)).p(" : ");

        int i = enterContext(0);
        printer.p(paramGNode.getNode(3));
        exitContext(i);
    }

    public void visitWhileStatement(GNode paramGNode) {
        boolean bool = startStatement(0);
        printer.indent().p("while (").p(paramGNode.getNode(0)).p(')');
        prepareNested();
        printer.p(paramGNode.getNode(1));
        endStatement(bool);
    }

    public void visitDoWhileStatement(GNode paramGNode) {
        boolean bool = startStatement(0);
        printer.indent().p("do");
        prepareNested();
        printer.p(paramGNode.getNode(0));
        if (isOpenLine) {
            printer.p(' ');
        } else {
            printer.indent();
        }
        printer.p("while (").p(paramGNode.getNode(1)).pln(");");
        endStatement(bool);
        isOpenLine = false;
    }

    public void visitTryCatchFinallyStatement(GNode paramGNode) {
        boolean bool = startStatement(0);

        printer.indent().p("try");
        if (null != paramGNode.get(0)) {
            printer.p(" (").p(paramGNode.getNode(0)).p(')');
        }
        isOpenLine = true;
        printer.p(paramGNode.getNode(1)).p(' ');

        Iterator localIterator = paramGNode.iterator();
        localIterator.next();
        localIterator.next();
        while (localIterator.hasNext()) {
            GNode localGNode = GNode.cast(localIterator.next());

            isOpenLine = true;
            if (localIterator.hasNext()) {
                printer.p(localGNode).p(' ');
            } else if (null != localGNode) {
                printer.p("finally").p(localGNode);
            }
        }
        endStatement(bool);
    }

    public void visitResourceSpecification(GNode paramGNode) {
        int i = printer.column();
        for (Iterator localIterator = paramGNode.iterator(); localIterator.hasNext();) {
            GNode localGNode = GNode.cast(localIterator.next());
            printer.p(localGNode.getNode(0)).p(localGNode.getNode(1)).p(' ').p(localGNode.getNode(2));
            if (localIterator.hasNext()) {
                printer.pln(';').align(i);
            }
        }
    }

    public void visitCatchClause(GNode paramGNode) {
        printer.p("catch (").p(paramGNode.getNode(0)).p(")").p(paramGNode.getNode(1));
    }

    public void visitSwitchStatement(GNode paramGNode) {
        boolean bool = startStatement(0);

        int i = enterContext(1);
        printer.indent().p("switch (").p(paramGNode.getNode(0)).pln(") {").incr();
        exitContext(i);

        isOpenLine = false;
        isNested = false;
        isIfElse = false;
        Iterator localIterator = paramGNode.iterator();
        localIterator.next();
        while (localIterator.hasNext()) {
            printer.p((Node)localIterator.next());
        }
        if (isOpenLine) {
            printer.pln();
        }
        printer.decr().indent().p('}');

        isOpenLine = true;
        isNested = false;
        isIfElse = false;
        endStatement(bool);
    }

    public void visitCaseClause(GNode paramGNode) {
        boolean bool = startStatement(0);

        int i = enterContext(1);
        printer.indentLess().p("case ").p(paramGNode.getNode(0)).pln(':');
        exitContext(i);

        isOpenLine = false;
        isNested = false;
        isIfElse = false;
        Iterator localIterator = paramGNode.iterator();
        localIterator.next();
        while (localIterator.hasNext()) {
            printer.p((Node)localIterator.next());
        }
        endStatement(bool);
    }

    public void visitDefaultClause(GNode paramGNode) {
        boolean bool = startStatement(0);
        printer.indentLess().pln("default:");
        isOpenLine = false;
        isNested = false;
        isIfElse = false;
        Object localObject;
        for (Iterator localIterator = paramGNode.iterator(); localIterator.hasNext(); printer.p((Node)localObject)) {
            localObject = localIterator.next();
        }
        endStatement(bool);
    }

    public void visitSynchronizedStatement(GNode paramGNode) {
        boolean bool = startStatement(0);
        printer.indent().p("synchronized (").p(paramGNode.getNode(0)).p(')');
        prepareNested();
        printer.p(paramGNode.getNode(1));
        endStatement(bool);
    }

    public void visitReturnStatement(GNode paramGNode) {
        boolean bool = startStatement(0);
        printer.indent().p("return");
        if (null != paramGNode.get(0)) {
            printer.p(' ').p(paramGNode.getNode(0));
        }
        printer.pln(';');
        endStatement(bool);
        isOpenLine = false;
    }

    public void visitThrowStatement(GNode paramGNode) {
        boolean bool = startStatement(0);
        printer.indent().p("throw").p(' ').p(paramGNode.getNode(0));
        printer.pln(';');
        endStatement(bool);
        isOpenLine = false;
    }

    public void visitBreakStatement(GNode paramGNode) {
        boolean bool = startStatement(0);
        printer.indent().p("break");
        if (paramGNode.getString(0) != null) {
            printer.p(' ').p(paramGNode.getString(0));
        }
        printer.pln(';');
        endStatement(bool);
        isOpenLine = false;
    }

    public void visitContinueStatement(GNode paramGNode) {
        boolean bool = startStatement(0);
        printer.indent().p("continue");
        if (null != paramGNode.getString(0)) {
            printer.p(' ').p(paramGNode.getString(0));
        }
        printer.p(';').pln();
        endStatement(bool);
        isOpenLine = false;
    }

    public void visitLabeledStatement(GNode paramGNode) {
        boolean bool = startStatement(0);
        printer.indent().p(paramGNode.getString(0)).p(": ").p(paramGNode.getNode(1)).pln();
        endStatement(bool);
        isOpenLine = false;
    }

    public void visitExpressionStatement(GNode paramGNode) {
        boolean bool = startStatement(0);
        int i = enterContext(0);
        printer.indent().p(paramGNode.getNode(0)).pln(';');
        exitContext(i);
        endStatement(bool);
        isOpenLine = false;
    }

    public void visitAssertStatement(GNode paramGNode) {
        boolean bool = startStatement(0);
        printer.indent().p("assert ").p(paramGNode.getNode(0));
        if (null != paramGNode.get(1)) {
            printer.p(" : ").p(paramGNode.getNode(1));
        }
        printer.pln(';');
        endStatement(bool);
        isOpenLine = false;
    }

    public void visitEmptyStatement(GNode paramGNode) {
        boolean bool = startStatement(0);
        printer.indent().pln(';');
        endStatement(bool);
        isOpenLine = false;
    }

    public void visitExpressionList(GNode paramGNode) {
        for (Iterator localIterator = paramGNode.iterator(); localIterator.hasNext();) {
            int i = enterContext(10);
            printer.p((Node)localIterator.next());
            exitContext(i);
            if (localIterator.hasNext()) {
                printer.p(", ");
            }
        }
    }

    public void visitExpression(GNode paramGNode) {
        int i = startExpression(10);
        int j = enterContext();
        printer.p(paramGNode.getNode(0));
        exitContext(j);

        printer.p(' ').p(paramGNode.getString(1)).p(' ').p(paramGNode.getNode(2));
        endExpression(i);
    }

    public void visitConditionalExpression(GNode paramGNode) {
        int i = startExpression(20);

        int j = enterContext();
        printer.p(paramGNode.getNode(0)).p(" ? ");
        exitContext(j);

        int k = enterContext();
        if (null != paramGNode.get(1)) {
            printer.p(paramGNode.getNode(1)).p(" : ");
        } else {
            printer.p(" /* Empty */ : ");
        }
        exitContext(k);

        printer.p(paramGNode.getNode(2));
        endExpression(i);
    }

    public void visitLogicalOrExpression(GNode paramGNode) {
        int i = startExpression(30);
        printer.p(paramGNode.getNode(0));
        printer.p(" || ");
        int j = enterContext();
        printer.p(paramGNode.getNode(1));
        exitContext(j);
        endExpression(i);
    }

    public void visitLogicalAndExpression(GNode paramGNode) {
        int i = startExpression(40);
        printer.p(paramGNode.getNode(0));
        printer.p(" && ");
        int j = enterContext();
        printer.p(paramGNode.getNode(1));
        exitContext(j);
        endExpression(i);
    }

    public void visitBitwiseOrExpression(GNode paramGNode) {
        int i = startExpression(50);
        printer.p(paramGNode.getNode(0));
        printer.p(" | ");
        int j = enterContext();
        printer.p(paramGNode.getNode(1));
        exitContext(j);
        endExpression(i);
    }

    public void visitBitwiseXorExpression(GNode paramGNode) {
        int i = startExpression(60);
        printer.p(paramGNode.getNode(0));
        printer.p(" ^ ");
        int j = enterContext();
        printer.p(paramGNode.getNode(1));
        exitContext(j);
        endExpression(i);
    }

    public void visitBitwiseAndExpression(GNode paramGNode) {
        int i = startExpression(70);
        printer.p(paramGNode.getNode(0)).p(" & ");
        int j = enterContext();
        printer.p(paramGNode.getNode(1));
        exitContext(j);
        endExpression(i);
    }

    public void visitEqualityExpression(GNode paramGNode) {
        int i = startExpression(80);
        printer.p(paramGNode.getNode(0)).p(' ').p(paramGNode.getString(1)).p(' ');
        int j = enterContext();
        printer.p(paramGNode.getNode(2));
        exitContext(j);
        endExpression(i);
    }

    public void visitInstanceOfExpression(GNode paramGNode) {
        int i = startExpression(90);
        printer.p(paramGNode.getNode(0)).p(' ').p("instanceof").p(' ');
        int j = enterContext();
        printer.p(paramGNode.getNode(1));
        exitContext(j);
        endExpression(i);
    }

    public void visitRelationalExpression(GNode paramGNode) {
        int i = startExpression(100);
        printer.p(paramGNode.getNode(0)).p(' ').p(paramGNode.getString(1)).p(' ');
        int j = enterContext();
        printer.p(paramGNode.getNode(2));
        exitContext(j);

        endExpression(i);
    }

    public void visitShiftExpression(GNode paramGNode) {
        int i = startExpression(110);
        printer.p(paramGNode.getNode(0));
        printer.p(' ').p(paramGNode.getString(1)).p(' ');
        int j = enterContext();
        printer.p(paramGNode.getNode(2));
        exitContext(j);
        endExpression(i);
    }

    public void visitAdditiveExpression(GNode paramGNode) {
        int i = startExpression(120);
        printer.p(paramGNode.getNode(0)).p(' ').p(paramGNode.getString(1)).p(' ');

        int j = enterContext();
        printer.p(paramGNode.getNode(2));
        exitContext(j);

        endExpression(i);
    }

    public void visitMultiplicativeExpression(GNode paramGNode) {
        int i = startExpression(130);
        printer.p(paramGNode.getNode(0)).p(' ').p(paramGNode.getString(1)).p(' ');

        int j = enterContext();
        printer.p(paramGNode.getNode(2));
        exitContext(j);

        endExpression(i);
    }

    public void visitUnaryExpression(GNode paramGNode) {
        int i = startExpression(150);
        printer.p(paramGNode.getString(0)).p(paramGNode.getNode(1));
        endExpression(i);
    }

    public void visitBitwiseNegationExpression(GNode paramGNode) {
        int i = startExpression(150);
        printer.p('~').p(paramGNode.getNode(0));
        endExpression(i);
    }

    public void visitLogicalNegationExpression(GNode paramGNode) {
        int i = startExpression(150);
        printer.p('!').p(paramGNode.getNode(0));
        endExpression(i);
    }

    public void visitBasicCastExpression(GNode paramGNode) {
        int i = startExpression(140);
        printer.p('(').p(paramGNode.getNode(0));
        if (null != paramGNode.get(1)) {
            printer.p(paramGNode.getNode(1));
        }
        printer.p(')').p(paramGNode.getNode(2));

        endExpression(i);
    }

    public void visitCastExpression(GNode paramGNode) {
        int i = startExpression(140);
        printer.p('(').p(paramGNode.getNode(0)).p(')').p(paramGNode.getNode(1));
        endExpression(i);
    }

    public void visitCallExpression(GNode paramGNode) {
        int i = startExpression(160);

        if (null != paramGNode.get(0)) {
            Node var = paramGNode.getNode(3);
            if (paramGNode.getString(2).equals("print")) {
                printer.p("cout << ").p(var);
            } else if (paramGNode.getString(2).equals("println")) {
                printer.p("cout << ").p(var).p(" << endl");
            } else {
//         printer.p(paramGNode.getNode(0)).p(paramGNode.getNode(1)).p(paramGNode.getString(2)).p(paramGNode.getNode(3));

                dispatch((Node) paramGNode.get(0));

                printer.p("->__vptr->");
                printer.p(paramGNode.getString(2));

                printer.p("(");
                parensPrinted = true;

                dispatch((Node) paramGNode.get(0));

                // String self = ((GNode) paramGNode.get(0)).getString(0);

                GNode args = GNode.ensureVariable((GNode) paramGNode.get(3));
                // args.add(0, GNode.create("PrimaryIdentifier", self));
                // dispatch(args);

                if(args.size() > 0) { // Print comma when more than one parameters
                    printer.p(", ");
                    dispatch(args); // Print the other arguments
                }
                printer.p(")");
                parensPrinted = false;
            }

//       printer.p(paramGNode.getNode(0)).p(paramGNode.getNode(1)).p(paramGNode.getString(2)).p(paramGNode.getNode(3));

        } else {
            printer.p(paramGNode.getString(2));

            Node args = (Node) paramGNode.get(3);

            printer.p("(");
            if(args.size() > 0) { // Print comma when more than one parameters
                dispatch((Node) paramGNode.get(3)); // Print the other arguments
                if(args.size() > 1)
                    printer.p(", ");
            }
            printer.p(")");
        }

//   printer.p(paramGNode.getNode(1)).p(paramGNode.getString(2)).p(paramGNode.getNode(3));

        endExpression(i);
    }

    public void visitSelectionExpression(GNode paramGNode) {
        int i = startExpression(160);
        printer.p(paramGNode.getNode(0)).p("->").p(paramGNode.getString(1));
        endExpression(i);
    }

    public void visitSubscriptExpression(GNode paramGNode) {
        int i = startExpression(160);
        printer.p(paramGNode.getNode(0)).p('[');
        int j = enterContext(0);
        printer.p(paramGNode.getNode(1)).p(']');
        exitContext(j);
        endExpression(i);
    }

    public void visitPostfixExpression(GNode paramGNode) {
        int i = startExpression(160);
        printer.p(paramGNode.getNode(0)).p(paramGNode.getString(1));
        endExpression(i);
    }

    public void visitClassLiteralExpression(GNode paramGNode) {
        int i = startExpression(160);
        printer.p(paramGNode.getNode(0)).p(".class");
        endExpression(i);
    }

    public void visitThisExpression(GNode paramGNode) {
        int i = startExpression(160);
        if (null != paramGNode.get(0)) {
            printer.p(paramGNode.getNode(0)).p('.');
        }
        printer.p("__this");
        endExpression(i);
    }

    public void visitSuperExpression(GNode paramGNode) {
        int i = startExpression(160);
        if (null != paramGNode.get(0)) {
            printer.p(paramGNode.getNode(0)).p('.');
        }
        printer.p("super");
        endExpression(i);
    }

    public void visitPrimaryIdentifier(GNode paramGNode) {
        int i = startExpression(160);
        printer.p(paramGNode.getString(0));
        endExpression(i);
    }

    public void visitNewClassExpression(GNode paramGNode) {
        int i = startExpression(160);
        if (null != paramGNode.get(0)) {
            printer.p(paramGNode.getNode(0)).p('.');
        }
        printer.p("new ");
        if (null != paramGNode.get(1)) {
            printer.p(paramGNode.getNode(1)).p(' ');
        }
        printer.p(paramGNode.getNode(2)).p(paramGNode.getNode(3));
        if (null != paramGNode.get(4)) {
            prepareNested();
            printer.p(paramGNode.getNode(4));
        }
        endExpression(i);
    }

    public void visitNewArrayExpression(GNode paramGNode) {
        int i = startExpression(160);
        printer.p("new ");
//        printer.p("new ").p(paramGNode.getNode(0)).p(paramGNode.getNode(1)).p(paramGNode.getNode(2));
//        if (null != paramGNode.get(3)) {
//            printer.p(' ').p(paramGNode.getNode(3));
//        }
        String type = ((GNode) paramGNode.get(0)).getString(0);
        GNode dimens = (GNode) paramGNode.get(1);
        if (dimens != null)
            makeNewArray(type, dimens.size(), dimens.iterator());
        endExpression(i);
    }

    private void makeNewArray(String type, int dims, Iterator it) {
        if (dims == 0) {
            printer.p(type);
            return;
        }
        GNode localObject = (GNode) it.next();
        printer.p("__rt::Array<");
        makeNewArray(type, dims - 1, it);
        printer.p(">(" + localObject.getString(0) + ")");

        // TODO: loop through and initialize if dimens > 1
    }

    public void visitConcreteDimensions(GNode paramGNode) {
        Object localObject;
        for (Iterator localIterator = paramGNode.iterator(); localIterator.hasNext(); printer.p('[').p((Node)localObject).p(']')) {
            localObject = localIterator.next();
        }
    }

    public void visitArrayInitializer(GNode paramGNode) {
        if (!paramGNode.isEmpty()) {
            printer.pln('{').incr().indent();
            for (Iterator localIterator = paramGNode.iterator(); localIterator.hasNext();) {
                printer.buffer().p((Node)localIterator.next());
                if (localIterator.hasNext()) {
                    printer.p(", ");
                }
                printer.fit();
            }
            printer.pln().decr().indent().p('}');
        } else {
            printer.p("{ }");
        }
    }

    public void visitArguments(GNode paramGNode) {
        if (!parensPrinted)
            printer.p('(');
        for (Iterator localIterator = paramGNode.iterator(); localIterator.hasNext();) {
            int i = enterContext(10);
            printer.p((Node)localIterator.next());
            exitContext(i);
            if (localIterator.hasNext()) {
                printer.p(", ");
            }
        }
        if (!parensPrinted)
            printer.p(')');
    }

    public void visitVoidType(GNode paramGNode) {
        printer.p("void");
    }

    public void visitType(GNode paramGNode) {
        printer.p((GNode) paramGNode.get(0));
//        String type = ((GNode) paramGNode.get(0)).getString(0);
//        if (null != paramGNode.get(1)) {
//            formatDimensions(((GNode) paramGNode.get(1)).getString(0).length(), type);
//        } else
//            printer.p(type);
    }

    public void visitPrimitiveType(GNode paramGNode) {
        printer.p(paramGNode.getString(0));
    }

    public void visitInstantiatedType(GNode paramGNode) {
        int i = 1;
        for (Object localObject : paramGNode) {
            if (i != 0) {
                i = 0;
            } else {
                printer.p('.');
            }
            printer.p((Node)localObject);
        }
    }

    public void visitTypeInstantiation(GNode paramGNode) {
        printer.p(paramGNode.getString(0)).p(paramGNode.getNode(1));
    }

    public void visitDimensions(GNode paramGNode) {
        for (int i = 0; i < paramGNode.size(); i++) {
            printer.p("[]");
        }
    }

    public void visitTypeParameters(GNode paramGNode) {
        printer.p('<');
        for (Iterator localIterator = paramGNode.iterator(); localIterator.hasNext();) {
            printer.p((Node)localIterator.next());
            if (localIterator.hasNext()) {
                printer.p(", ");
            }
        }
        printer.p('>');
    }

    public void visitTypeParameter(GNode paramGNode) {
        printer.p(paramGNode.getString(0));
        if (null != paramGNode.get(1)) {
            printer.p(" extends ").p(paramGNode.getNode(1));
        }
    }

    public void visitBound(GNode paramGNode) {
        for (Iterator localIterator = paramGNode.iterator(); localIterator.hasNext();) {
            printer.p((Node)localIterator.next());
            if (localIterator.hasNext()) {
                printer.p(" & ");
            }
        }
    }

    public void visitTypeArguments(GNode paramGNode) {
        printer.p('<');
        for (Iterator localIterator = paramGNode.iterator(); localIterator.hasNext();) {
            printer.p((Node)localIterator.next());
            if (localIterator.hasNext()) {
                printer.p(", ");
            }
        }
        printer.p('>');
    }

    public void visitWildcard(GNode paramGNode) {
        printer.p('?').p(paramGNode.getNode(0));
    }

    public void visitWildcardBound(GNode paramGNode) {
        printer.p(' ').p(paramGNode.getString(0)).p(' ').p(paramGNode.getNode(1));
    }

    public void visitIntegerLiteral(GNode paramGNode) {
        int i = startExpression(160);
        printer.p(paramGNode.getString(0));
        endExpression(i);
    }

    public void visitFloatingPointLiteral(GNode paramGNode) {
        int i = startExpression(160);
        printer.p(paramGNode.getString(0));
        endExpression(i);
    }

    public void visitCharacterLiteral(GNode paramGNode) {
        int i = startExpression(160);
        printer.p(paramGNode.getString(0));
        endExpression(i);
    }

    public void visitStringLiteral(GNode paramGNode) {
        int i = startExpression(160);
        printer.p(paramGNode.getString(0));
        endExpression(i);
    }

    public void visitBooleanLiteral(GNode paramGNode) {
        int i = startExpression(160);
        printer.p(paramGNode.getString(0));
        endExpression(i);
    }

    public void visitNullLiteral(GNode paramGNode) {
        int i = startExpression(160);
        printer.p("NULL");
        endExpression(i);
    }

    public void visitQualifiedIdentifier(GNode paramGNode) {
        int i = startExpression(160);
        Iterator localIterator;
        if (1 == paramGNode.size()) {
            printer.p(paramGNode.getString(0));
        } else {
            for (localIterator = paramGNode.iterator(); localIterator.hasNext();) {
                printer.p(Token.cast(localIterator.next()));
                if (localIterator.hasNext()) {
                    printer.p('.');
                }
            }
        }
        endExpression(i);
    }

    private String pkgStr(List<String> pkg) {
        String pkgStr = "";

        if (pkg != null)
            for (String str : pkg)
                pkgStr = pkgStr + str + "::";

        pkgStr = pkgStr.substring(0, pkgStr.length()-2);
        return pkgStr;
    }
}
