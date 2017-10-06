package edu.nyu.oop.constructs;

import java.util.List;
import java.util.Arrays;

import edu.nyu.oop.util.VisitorUtil;

import xtc.tree.GNode;

public class JIdentifier extends JType implements JModifiers {

    protected List<Integer> mods;
    protected String name;

    public JIdentifier(List<Integer> mods, JScope scope, String name, String type, int dimensions, GNode node) {
        super(scope, type, dimensions, node);
        this.mods = mods;
        this.name = name;
    }

    // Getters // {{{

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getMods() {
        return mods;
    }

    // }}}

}
