package edu.nyu.oop.constructs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.nyu.oop.util.VisitorUtil;
import edu.nyu.oop.visitors.MethodVisitor;

import xtc.tree.GNode;


public class JClass implements JScope, JModifiers {

    public static final String[] bundled = {"Object", "Class", "String"};

    private JSource source;

    private List<Integer> mods;
    private String name;
    private List<JMethod> methods;
    private List<JMethod> constructors;
    private Map<JMethod, String> resolvedMethods; // will hold all methods (including those from parent) String is name of class where method is from
    private Map<JIdentifier, Object> members;
    private Map<JIdentifier, Object> resolvedMembers; // will hold all members (including those from parent) Object is the initialized value of member
    private String parent;
    private JClass parentCls;
    private List<JClass> children;
    private List<String> pkg;

    private boolean isMainClass;

    private GNode node;

    public JClass(JSource source, List<Integer> mods, String name, String parent, GNode node) {
        this.source = source;
        this.mods = mods;
        this.name = name;
        this.parent = parent;
        this.node = node;

        this.isMainClass = false;
        this.children = new LinkedList<JClass>();
        this.methods = new LinkedList<JMethod>();
        this.constructors = new LinkedList<JMethod>();
        this.members = new HashMap<JIdentifier, Object>();
        this.resolvedMethods = new HashMap<JMethod, String>();
        this.resolvedMembers = new HashMap<JIdentifier, Object>();
    }

    public JClass(JSource source, List<Integer> mods, String name, String parent, Map<JIdentifier, Object> members, List<JMethod> constructors, List<JMethod> methods, GNode node) {
        this.source = source;
        this.mods = mods;
        this.name = name;
        this.parent = parent;
        this.members = members;
        this.constructors = constructors;
        this.methods = methods;
        this.node = node;

        this.isMainClass = false;
        this.children = new LinkedList<JClass>();
        this.resolvedMethods = new HashMap<JMethod, String>();
        this.resolvedMembers = new HashMap<JIdentifier, Object>();
    }

    public void decorate() {
        MethodVisitor visitor = new MethodVisitor();

        if (constructors.size() == 0 && !isMainClass) {
            GNode body = (GNode) node.get(5);
            GNode constructor = VisitorUtil.createFunctionNode(this, "__" + name, "ConstructorDeclaration", null, null, null);

            body = GNode.ensureVariable(body);
            body.add(constructor);

            node.set(5, body);

            JMethod newConst = visitor.parseConstructor(constructor, this);
            newConst.decorate();

            constructors.add(new JConstructor(this, null, "__" + name, constructor));
        }

        if (!isMainClass) {
            GNode retType = GNode.create("Type", GNode.create("QualifiedIdentifier", "Class"), null);
            GNode block = GNode.create("Block", GNode.create("ReturnStatement",
                                       GNode.create("NewClassExpression",
                                                    null,
                                                    null,
                                                    GNode.create("QualifiedIdentifier", "__Class"),
                                                    GNode.create("Arguments", GNode.create("__rt::literal(\"" + VisitorUtil.javaPkgStr(pkg) + "." + name + "\")"),
                                                            GNode.create(VisitorUtil.pkgStr(parentCls.getPkg()) + "__" + parent + "::__class()")),
                                                    null))); // visions of lisp
            GNode classMethod = VisitorUtil.createFunctionNode(this, "__class", "MethodDeclaration", null, block, retType);
            JMethod classJMethod = visitor.parse(classMethod, this);
            classJMethod.decorate();

            GNode body = (GNode) node.get(5);
            body = GNode.ensureVariable(body);
            body.add(classMethod);
            node.set(5, body);
        }
    }

    public void addMember(JIdentifier ident, Object val) {
        members.put(ident, val);
    }

    public void addMethod(JMethod method) {
        methods.add(method);
    }

    public void addConstructor(JMethod constructor) {
        constructors.add(constructor);
    }

// Getters and Setters // {{{

    public String getName() {
        return name;
    }

    public GNode getNode() {
        return node;
    }

    public JSource getSource() {
        return source;
    }

    public JClass getParentCls() {
        return parentCls;
    }

    public void setParentCls(JClass parentCls) {
        this.parentCls = parentCls;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public void setIsMainClass(boolean isMainClass) {
        this.isMainClass = isMainClass;
    }

    public boolean isMainClass() {
        return this.isMainClass;
    }

    public List<JMethod> getMethods() {
        return methods;
    }

    public void setMethods(List<JMethod> methods) {
        this.methods = methods;
    }

    public List<JMethod> getConstructors() {
        return constructors;
    }

    public void setConstructors(List<JMethod> constructors) {
        this.constructors = constructors;
    }

    public Map<JIdentifier, Object> getMembers() {
        return members;
    }

    public void setMembers(Map<JIdentifier, Object> members) {
        this.members = members;
    }

    public List<Integer> getMods() {
        return mods;
    }

    public List<JClass> getChildren() {
        return children;
    }

    public void addChild(JClass child) {
        children.add(child);
    }

    public Map<JMethod, String> getResolvedMethods() {
        return resolvedMethods;
    }

    public void setResolvedMethods(Map<JMethod, String> methods) {
        this.resolvedMethods = methods;
    }

    public Map<JIdentifier, Object> getResolvedMembers() {
        return resolvedMembers;
    }

    public void setResolvedMembers(Map<JIdentifier, Object> members) {
        this.resolvedMembers = members;
    }

    public List<String> getPkg() {
        return this.pkg;
    }

    public void setPkg(List<String> pkg) {
        this.pkg = pkg;
    }

// }}}

}
