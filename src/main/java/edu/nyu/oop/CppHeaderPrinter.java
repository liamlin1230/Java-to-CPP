package edu.nyu.oop;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import edu.nyu.oop.util.XtcProps;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Printer;
import xtc.tree.Visitor;

public class CppHeaderPrinter extends Visitor {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private Printer printer;
    private String outputLocation = XtcProps.get("output.location");
    private GNode ast;
    private String classLevel = "__Object";
    private String currentVTable;

    public CppHeaderPrinter(Node ast) {
        Writer w = null;
        this.ast = (GNode) ast;

        try {
            FileOutputStream fos = new FileOutputStream(outputLocation + "/output.h");
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

    public void visit(Node n) {
        boolean skipChildren = false;
        for (Object o : n) {
            if (o instanceof Node) {
                if (((Node) o).hasName("__Class") || ((Node) o).hasName("__String")) {
                    // Ignore these classes since java_lang.h will include them
                    continue;
                }
                if (isInDifferentClassScope(((Node) o).getName())) {
                    createStruct((Node) o);
                }

                if (((Node) o).hasName("VTable") && !classLevel.equals("__Object")) {
                    skipChildren = true;
                    createVTableStruct((Node) o);
                }

                if (!skipChildren) {
                    // We need to go deeper...
                    dispatch((GNode) o);
                } else {
                    skipChildren = false;
                }
            }
        }
    }

    public void visitPkg(GNode n) {
        printIndentedLine("namespace " + n.getString(0) + " {", true);
        printer.incr();
        printer.pln();

        for (Object o : n)
            if (o instanceof Node)
                dispatch((Node) o);

        printer.pln();
        printer.decr();
        printIndentedLine("}", true);
    }

    private void createStruct(Node n) {
        if (! ((GNode) n.get(0)).getName().equals("main")) {
            String className = n.getName();
            classLevel = className;
            printIndentedLine("struct " + className + ";", true);
            printIndentedLine("struct " + className + "_VT;", true);
            printer.pln();
            printIndentedLine("typedef __rt::Ptr<" + className + "> " + className.substring(2) + ";", true);
            printer.pln();
            printIndentedLine("struct " + className + " {", true);
            printer.incr();
            // HeaderDeclaration > DataLayout | VTable
            for (Object hdr : n) {
                // HeaderDeclaration or "main"
                if (hdr instanceof Node) {
                    for (Object o : (Node) hdr) {
                        if (o instanceof Node) {
                            // DataLayout || VTable
                            if (((Node) o).hasName("DataLayout")) {
                                printDataLayout((Node) o);
                            }
                        }
                    }
                } else if (hdr instanceof String) {
                    if (hdr.equals("main")) {
                        // main method
                        printIndentedLine("int " + hdr + "();", true);
                    }
                }
            }
            printer.decr();
            printIndentedLine("};", true);
            printer.pln();
        }
    }

    private void createVTableStruct(Node n) {
        classLevel = n.getName();
        // VTable struct should already be declared by this point
        printIndentedLine("struct " + currentVTable + " {", true);
        printer.incr();
        // VTable > FieldDeclaration | VTableMethodDeclaration | ConstructorDeclaration | InitializationList
        for (Object o : n) {
            if (o instanceof Node) {
                if (((Node) o).hasName("FieldDeclaration")) {
                    printFieldDeclaration((Node) o);
                } else if (((Node) o).hasName("VTableMethodDeclaration")) {
                    printVTableMethodDeclaration((Node) o);
                } else if (((Node) o).hasName("ConstructorDeclaration")) {
                    printConstructorDeclaration((Node) o);
                }
            }
        }
        printer.decr();
        printIndentedLine("};", true);
        printer.pln();
    }

    private boolean isInDifferentClassScope(String nodeName) {
        if (nodeName.length() >= 2)
            return (nodeName.substring(0, 2).equals("__") && !nodeName.equals("__isa")) && !nodeName.equals("__delete") ? true : false;
        else
            return false;
    }

    private void printIndentedLine(String line, boolean newLine) {
//        int level = printer.level();
//        if (level > 0) {
//            for (int i = 0; i < level; i++) {
//                printer.indent();
//            }
//        }
        printer.indent();
        printer.indent();
        if (newLine)
            printer.pln(line);
        else
            printer.p(line);
    }

    public void print() {
        headOfFile();
        // namespace java { namespace lang {
        dispatch(ast);
        tailOfFile();
        printer.flush(); // important!
    }

    private void headOfFile() {
        printer.pln("#pragma once");
        printer.pln();
        printer.pln("#include <stdint.h>");
        printer.pln("#include <string>");
        printer.pln("#include \"java_lang.h\"");
        printer.pln();
        printer.pln("using namespace java::lang;");
        printer.pln();
        // printer.pln("namespace java {");
        // printer.incr();
        // printIndentedLine("namespace lang {", true);
        // printer.incr();
    }

    private void printDataLayout(Node n) {
        int i = 0;
        for (Object o : n) {
            if (o instanceof Node) {
                i++;
                if (((Node) o).hasName("FieldDeclaration")) {
                    printFieldDeclaration((Node) o);
                } else if (((Node) o).hasName("DataLayoutMethodDeclaration")) {
                    printDataLayoutMethodDeclaration((Node) o);
                } else if (((Node) o).hasName("ConstructorDeclaration")) {
                    printConstructorDeclaration((Node) o);
                }
            }
        }
    }

    private void printFieldDeclaration(Node n) {
        boolean hasModifier = false;
        if (n.size() != 3) {
            // This shouldn't happen unless the AST is corrupted
            logger.error("FieldDeclaration size expected 3, was " + n.size());
            return;
        }
        if (classLevel.equals("__Object")) {
            // java_lang.h will include them instead
            return;
        }

        if (n.getNode(0).hasTraversal()) {
            if (!n.getNode(0).isEmpty()) {
                printIndentedLine(n.getNode(0).getString(0) + " ", false);
                hasModifier = true;
            }
            if (n.getString(2).equals("vtable")) {
                currentVTable = n.getString(1);
            }
            if (hasModifier)
                printer.p(n.getString(1) + " ");
            else
                printIndentedLine(n.getString(1) + " ", false);
            printer.pln(n.getString(2) + ";");

        } else {
            logger.error("No modifiers found for FieldDeclaration");
        }
    }

    private void printDataLayoutMethodDeclaration(Node n) {
        boolean hasModifier = false;
        if (n.size() != 5) {
            logger.error("DataLayoutMethodDeclaration size expected 5, was " + n.size());
            return;
        }
        if (classLevel.equals("__Object")) {
            // java_lang.h will include them instead
            return;
        }

        if (n.getNode(0).hasTraversal()) {
            if (!n.getNode(0).isEmpty()) {
                printIndentedLine(n.getNode(0).getString(0) + " ", false);
                hasModifier = true;
            }
            if (hasModifier)
                printer.p(n.getString(1) + " ");
            else
                printIndentedLine(n.getString(1) + " ", false);
            printer.p(n.getString(2) + "(");
            if (n.getNode(4).hasTraversal()) {
                // has parameters
                StringBuilder params = new StringBuilder();
                for (Object s : n.getNode(4)) {
                    if (s instanceof String) {
                        params.append((String) s + ", ");
                    }
                }
                if (params.length() >= 2) {
                    params.setLength(params.length() - 2);
                }
                printer.pln(params.toString() + ");");
            } else {
                // no parameters
                printer.pln(");");
            }
        } else {
            logger.error("No modifiers found for DataLayoutMethodDeclaration");
        }
    }

    private void printVTableMethodDeclaration(Node n) {
        boolean hasModifier = false;
        if (n.size() != 5) {
            logger.error("VTableMethodDeclaration size expected 5, was ", n.size());
            return;
        }
        if (classLevel.equals("__Object")) {
            // java_lang.h will include them instead
            return;
        }
        if (n.getNode(0).hasTraversal()) {
            if (!n.getNode(0).isEmpty()) {
                printIndentedLine(n.getNode(0).getString(0) + " ", false);
                hasModifier = true;
            }
            if (hasModifier)
                printer.p(n.getString(1) + " ");
            else
                printIndentedLine(n.getString(1) + " ", false);
            printer.p("(*" + n.getString(2) + ")(");
            if (n.getNode(4).hasTraversal()) {
                // has parameters
                StringBuilder params = new StringBuilder();
                for (Object s : n.getNode(4)) {
                    if (s instanceof String) {
                        params.append((String) s + ", ");
                    }
                }
                if (params.length() >= 2) {
                    params.setLength(params.length() - 2);
                }
                printer.pln(params.toString() + ");");
            } else {
                // no parameters
                printer.pln(");");
            }

        }

    }

    private void printConstructorDeclaration(Node n) {
        boolean hasInitializationList = false;
        if (n.size() != 3) {
            logger.error("ConstructorDeclaration size expected 3, was " + n.size());
            return;
        }
        if (classLevel.equals("__Object")) {
            // java_lang.h will include them instead
            return;
        }
        printIndentedLine(n.getString(0) + "(", false);

        if (n.getNode(1).hasTraversal()) {
            // has parameters
            StringBuilder params = new StringBuilder();
            for (Object s : n.getNode(1)) {
                if (s instanceof String) {
                    params.append((String) s + ", ");
                }
            }
            if (params.length() >= 2) {
                params.setLength(params.length() - 2);
            }
            printer.p(params.toString() + ")");
        } else {
            // no parameters
            printer.p(")");
        }

        if (n.getNode(2).hasTraversal()) {
            printer.incr();
            List<String> initializers = new ArrayList<String>();
            //printer.pln(" : ");
            for (Object o : n.getNode(2)) {
                if (o instanceof Node) {
                    if (((Node) o).getName().equals("__isa")) {
                        hasInitializationList = true;
                        printer.pln(" : ");
                        printIndentedLine("__isa(" + ((Node) o).getString(0) + "),", true);
                    } else if (((Node) o).getName().equals("__delete")) {
                        printIndentedLine("__delete(" + ((Node) o).getString(0) + "),", true);
                    } else if (((Node) o).getName().equals("Initializer")) {
                        if (!((Node) o).hasTraversal()) {
                            continue;
                        }
                        if (!classLevel.equals("VTable")) {
                            // Don't print datalayout constructor declarations here: we do that in implementation
                            break;
                            // maintain format consistency
                            // hasInitializationList = true;
                            // printer.pln(" : ");
                        }

                        String cast = (((Node) o).get(1) != null) ? ((Node) o).getNode(1).getString(0) : null;
                        if (cast != null) {
                            initializers.add(((Node) o).getString(0) + "(" + cast +
                                             ((((Node) o).get(2) instanceof String) ?
                                              ((Node) o).getString(2) : "\"" + (((Node) o).getNode(2).getString(0)) + "\"")
                                             + ")" + ",");
                        } else {
                            initializers.add(((Node) o).getString(0) + "(" +
                                             ((((Node) o).get(2) instanceof String) ?
                                              ((Node) o).getString(2) : "\"" + (((Node) o).getNode(2).getString(0)) + "\"")
                                             + ")" + ",");
                        }
                    } else {
                        hasInitializationList = true;
                        printer.pln(" : ");
                        printIndentedLine(((Node) o).getName() + "(" + ((Node) o).getString(0) + ") { }", true);
                    }
                }
            }
            if (!initializers.isEmpty()) {
                // all this to remove that last comma...
                String lastInit = initializers.get(initializers.size() - 1);
                initializers.set(initializers.size() - 1, lastInit.substring(0, lastInit.length() - 1) + " {");

                for (String s : initializers) {
                    printIndentedLine(s, true);
                }
                printIndentedLine("}", true);
            }
            if (!hasInitializationList)
                printer.pln(";");
            printer.decr();
        }
    }

    private void tailOfFile() {
        // close remaining scopes
        int level = printer.level();
        if (level > 0) {
            for (int i = 0; i < level; i++) {
                printer.decr();
                printIndentedLine("}", true);
            }
        }
    }
}
