package edu.nyu.oop.visitors;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.nyu.oop.constructs.JClass;
import edu.nyu.oop.constructs.JIdentifier;
import edu.nyu.oop.constructs.JSource;
import edu.nyu.oop.util.NodeUtil;
import edu.nyu.oop.util.RecursiveVisitor;
import edu.nyu.oop.util.VisitorUtil;

import xtc.tree.GNode;


public class SourceVisitor extends RecursiveVisitor {

    private ClassVisitor visitor = new ClassVisitor();

    private JSource source = null;

    private Map<String, JClass> classes = null;
    private Map<String, JClass> srcClasses = null;
    private List<String> imports = null;

    public JSource process(GNode n, Map<String, JClass> srcClasses) {
        if (srcClasses != null)
            this.srcClasses = srcClasses;
        else
            this.srcClasses = new HashMap<String, JClass>();

        source = new JSource(n);
        setPackageDeclaration((GNode) n.get(0));

        classes = new HashMap<String, JClass>();
        imports = new LinkedList<String>();

        dispatch(n);

        source.setClasses(classes);
        source.setImport(imports);
        return source;
    }

    public void visitImportDeclaration(GNode n) {
        GNode w = (GNode) n.get(1);
        imports.add(NodeUtil.mkString(w, "."));
    }

    public void visitClassDeclaration(GNode n) {
        JClass cls = visitor.process(n, source, srcClasses);

        if (cls.getParentCls() != null)
            cls.decorate();

        classes.put(cls.getName(), cls);
        srcClasses.put(cls.getName(), cls);
    }

    private void setPackageDeclaration(GNode n) {
        List<String> pkg = new LinkedList<String>();

        GNode w = (GNode) n.get(1);
        for (Object o : w)
            if (o instanceof String)
                pkg.add((String) o);


        if (pkg.get(0).equals("edu"))
            source.setPkg(new LinkedList<String>(Arrays.asList("java", "lang")));
        else
            source.setPkg(pkg);
    }
}
