package edu.nyu.oop.constructs;

import java.util.List;
import java.util.Map;

import xtc.tree.GNode;


public class JSource {

    private String name;
    private List<String> pkg;
    private List<String> imports;
    private Map<String, JClass> classes;

    private GNode node;

    public JSource(GNode node) {
        this.node = node;
    }

    public JSource(List<String> pkg, List<String> imports, Map<String, JClass> classes, GNode node) {
        this.pkg = pkg;
        this.imports = imports;
        this.classes = classes;
        this.node = node;
    }

// Getters and Setters // {{{

    public GNode getNode() {
        return node;
    }

    public String getName() {
        return name;
    }

    public List<String> getPkg() {
        return pkg;
    }

    public void setPkg(List<String> pkg) {
        this.pkg = pkg;
    }

    public List<String> getImports() {
        return imports;
    }

    public void setImport(List<String> imports) {
        this.imports = imports;
    }

    public Map<String, JClass> getClasses() {
        return classes;
    }

    public void setClasses(Map<String, JClass> classes) {
        this.classes = classes;
    }

// }}}

}
