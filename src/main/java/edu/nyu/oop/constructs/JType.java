package edu.nyu.oop.constructs;

import xtc.tree.GNode;

import edu.nyu.oop.ArrayTemplateGenerator;

public class JType {

    // Primitive types
    public static final int JTYPE_BYTE        = 0;
    public static final int JTYPE_SHORT       = 1;
    public static final int JTYPE_INT         = 2;
    public static final int JTYPE_LONG        = 3;
    public static final int JTYPE_FLOAT       = 4;
    public static final int JTYPE_DOUBLE      = 5;
    public static final int JTYPE_CHAR        = 6;
    public static final int JTYPE_BOOL        = 7;
    // Reference types
    public static final int JTYPE_STRING      = 8;
    public static final int JTYPE_OBJECT      = 9;
    public static final int JTYPE_CLASS       = 10;
    // Other reference type
    public static final int JTYPE_REF         = 11;

    public static final int JTYPE_VOID        = 12;

    public static final int JTYPE_CONSTRUCTOR = -1;

    protected int typeInt;
    protected String typeString;
    protected int dimensions;

    protected GNode node;

    protected JScope scope;

    protected String arrayName = "";

    public JType(JScope scope, String typeString, GNode node) {
        this(scope, typeString, 0, node);
    }

    public JType(JScope scope, String typeString, int dimensions, GNode node) {
        this.scope = scope;
        this.typeString = typeString;
        this.typeInt = getIdentifier(typeString);
        this.dimensions = dimensions;
        this.node = node;
    }

    public void decorate() {
        typeString = mapType();

        if (node != null) {
            node.set(0, typeString);
        }

        if (dimensions > 0)
            ArrayTemplateGenerator.addArrayType(typeString);
    }

    public void decorateArray() {
        if (dimensions == 0) return;
        formatDimensions(dimensions);
        node.set(0, arrayName);
    }

    protected void formatDimensions(int paramInt) {
        if (paramInt == 0) {
            String newType = mapType();
            arrayName += newType;
            return;
        }
        arrayName += "__rt::Array<";
        formatDimensions(paramInt - 1);
        arrayName += ">* ";
    }

    private String mapType() { // {{{
        switch (typeInt) {
        case JTYPE_BYTE:
            return "int8_t";
        case JTYPE_SHORT:
            return "int16_t";
        case JTYPE_INT:
            return "int32_t";
        case JTYPE_LONG:
            return "int64_t";
        case JTYPE_FLOAT:
            return "float";
        case JTYPE_DOUBLE:
            return "double";
        case JTYPE_CHAR:
            return "char";
        case JTYPE_BOOL:
            return "bool";
        case JTYPE_STRING:
            return "String";
        case JTYPE_OBJECT:
            return "Object";
        case JTYPE_CLASS:
            return "Class";
        case JTYPE_VOID:
            return "void";
        default:
            return typeString;
        }
    } // }}}

// Getters // {{{

    private int getIdentifier(String typeString) {
        int typeInt;

        switch (typeString.toLowerCase()) {
        case "byte":
            typeInt = JTYPE_BYTE;
            break;
        case "short":
            typeInt = JTYPE_SHORT;
            break;
        case "int":
            typeInt = JTYPE_INT;
            break;
        case "long":
            typeInt = JTYPE_LONG;
            break;
        case "float":
            typeInt = JTYPE_FLOAT;
            break;
        case "double":
            typeInt = JTYPE_DOUBLE;
            break;
        case "char":
            typeInt = JTYPE_CHAR;
            break;
        case "boolean":
            typeInt = JTYPE_BOOL;
            break;
        case "string":
            typeInt = JTYPE_STRING;
            break;
        case "object":
            typeInt = JTYPE_OBJECT;
            break;
        case "class":
            typeInt = JTYPE_CLASS;
            break;
        case "void":
            typeInt = JTYPE_VOID;
            break;
        case "constructor":
            typeInt = JTYPE_CONSTRUCTOR;
            break;
        default:
            typeInt = JTYPE_REF;
            break;
        }

        return typeInt;
    }

    public int getType() {
        return typeInt;
    }

    public String getString() {
        return typeString;
    }

    public int getDimensions() {
        return dimensions;
    }

    public JScope getScope() {
        return scope;
    }

    public void setNode(GNode node) {
        this.node = node;
    }

// }}}

}
