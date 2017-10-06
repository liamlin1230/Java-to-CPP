package edu.nyu.oop;

import xtc.tree.GNode;
import edu.nyu.oop.constructs.*;
import java.util.*;
import java.util.Map.Entry;

public class ASTGenerator {

    public static GNode createTree(JClass object, JSource source) {
        GNode root = GNode.create("AstRoot");
        GNode last = root;

        for (String pkg : source.getPkg()) {
            GNode w = GNode.create("Pkg");
            w.add(pkg);
            last.add(w);
            last = w;
        }

        last.add(createClass(object));

        return root;
    }

    private static GNode createClass(JClass cls) {
        boolean isBundled = false;

        GNode root = GNode.create("__" + cls.getName());
        GNode header = GNode.create("HeaderDeclaration");

        GNode dataLayout = GNode.create("DataLayout");
        dataLayout.add(createFieldDeclaration(
                           null,
                           "__" + cls.getName() + "_VT*",
                           "__vptr"
                       ));
        dataLayout.add(createFieldDeclaration(
                           new LinkedList<String>(Arrays.asList("static")),
                           "__" + cls.getName() + "_VT",
                           "vtable"
                       ));
        dataLayout.add(createMethodDeclaration(
                           "DataLayoutMethodDeclaration",
                           new LinkedList<String>(Arrays.asList("static")),
                           "Class",
                           "__class",
                           cls.getName(),
                           null
                       ));

        GNode vTable = GNode.create("VTable");
        vTable.add(createFieldDeclaration(null, "Class", "__isa"));
        vTable.add(createMethodDeclaration(
                       "VTableMethodDeclaration",
                       null,
                       "void",
                       "__delete",
                       cls.getName(),
                       new LinkedList<JIdentifier>(Arrays.asList(new JIdentifier(null, null, "o", "__" + cls.getName() + "*", 0, null)))
                   ));
        GNode VTinitializers = GNode.create("InitializationList");
        VTinitializers
        .add(GNode.create("__isa")
             .add("__" + cls.getName() + "::__class()"));
        VTinitializers
        .add(GNode.create("__delete")
             .add("&__rt::__delete<__" + cls.getName() + ">"));

        for (Entry<JIdentifier, Object> entry : cls.getResolvedMembers().entrySet()) {
            JIdentifier member = entry.getKey();
            dataLayout.add(createFieldDeclaration(member));
        }

        if (cls.getConstructors().size() > 0) {
            for (JMethod constructor : cls.getConstructors()) {
                dataLayout.add(createConstructorDeclaraton(
                                   cls.getName(),
                                   constructor.getArgs(),
                                   cls.getResolvedMembers()
                               ));
            }
        } else {
            dataLayout.add(createConstructorDeclaraton(
                               cls.getName(),
                               new LinkedList<JIdentifier>(),
                               cls.getResolvedMembers()
                           ));
        }

        boolean isMain = false;
        for (JMethod method : cls.getMethods()) {
            if (method.getName().equals("main")) {
                //     root.add(GNode.create("main"));
                //     isMain = true;
                //     break;
                continue;
            }

            dataLayout.add(createMethodDeclaration(
                               "DataLayoutMethodDeclaration",
                               new LinkedList<String>(Arrays.asList("static")),
                               cls,
                               cls.getName(),
                               method
                           ));
        }

        for (Entry<JMethod, String> entry : cls.getResolvedMethods().entrySet()) {
            JMethod method = entry.getKey();
            String origin = entry.getValue();

            if (method.getName().equals("main"))
                continue;

            if (!origin.equals(cls.getName()))
                vTable.add(createMethodDeclaration(
                               "VTableMethodDeclaration",
                               null,
                               cls,
                               origin,
                               method
                           ));
            else
                vTable.add(createMethodDeclaration(
                               "VTableMethodDeclaration",
                               null,
                               cls,
                               cls.getName(),
                               method
                           ));

            String argsList = "(";
            if (Arrays.asList(JClass.bundled).contains(origin) || method.getName().startsWith("init")) {
                argsList = (method.getName().startsWith("init")) ? argsList + "__" + cls.getName() + "*" :
                           argsList + cls.getName();
                List<JIdentifier> args = method.getArgs();
                if (args.size() > 1)
                    for (JIdentifier arg : args.subList(1, args.size()))
                        argsList += ", " + arg.getString();
            } else {
                List<JIdentifier> args = method.getArgs();
                for (JIdentifier arg : args)
                    argsList += ", " + arg.getString();
                // Remove the first unnecessary comma
                argsList = "(" + argsList.substring(3);
            }
            argsList += ")";

            String cast = null;
            if (origin != cls.getName()) {
                cast = "(" +
                       mapType(method.getRetType()) +
                       "(*)" +
                       argsList +
                       ")";
            }

            GNode initializer = GNode.create("Initializer");
            initializer
            .add(method.getName())
            .add(GNode.create("Cast").add(cast))
            .add("&__" + origin + "::" + method.getName());
            VTinitializers.add(initializer);
        }

        if (!isMain) {
            vTable.add(GNode.create("ConstructorDeclaration")
                       .add("__" + cls.getName() + "_VT")
                       .add(GNode.create("Parameters"))
                       .add(VTinitializers));

            header.add(dataLayout);
            header.add(vTable);
            root.add(header);
        }

        for (JClass child : cls.getChildren())
            root.add(createClass(child));

        return root;
    }

    private static String mapType(JType type) { // {{{
        int typeInt = type.getType();
        String typeString = type.getString();

        switch (typeInt) {
        case JType.JTYPE_BYTE:
            typeString = "int8_t";
            break;
        case JType.JTYPE_SHORT:
            typeString = "int16_t";
            break;
        case JType.JTYPE_INT:
            typeString = "int32_t";
            break;
        case JType.JTYPE_LONG:
            typeString = "int64_t";
            break;
        case JType.JTYPE_FLOAT:
            typeString = "float";
            break;
        case JType.JTYPE_DOUBLE:
            typeString = "double";
            break;
        case JType.JTYPE_CHAR:
            typeString = "char";
            break;
        case JType.JTYPE_BOOL:
            typeString = "bool";
            break;
        case JType.JTYPE_STRING:
            typeString = "String";
            break;
        case JType.JTYPE_OBJECT:
            typeString = "Object";
            break;
        case JType.JTYPE_CLASS:
            typeString = "Class";
            break;
        case JType.JTYPE_VOID:
            typeString = "void";
            break;
        case JType.JTYPE_CONSTRUCTOR:
            typeString = "CONSTRUCTOR";
            break;
        // If ref, the typeString should already be set
        default:
            typeString = typeString;
            break;
        }

        if (type.getDimensions() != 0) {
            for (int i = 0; i < type.getDimensions(); i++)
                typeString = "__rt::Array<" + typeString;
            for (int i = 0; i < type.getDimensions(); i++)
                typeString += ">*";
        }

        return typeString;
    } // }}}

// Constructor/Method Declaration // {{{

    private static GNode createConstructorDeclaraton(String cls, List<JIdentifier> params, Map<JIdentifier, Object> members) {
        GNode root = GNode.create("ConstructorDeclaration");

        GNode parameters = GNode.create("Parameters");
        if (params != null)
            for (JIdentifier param : params)
                parameters.add(mapType(param));

        GNode initializers = GNode.create("InitializationList");
        if (members != null) {
            for (Entry<JIdentifier, Object> entry : members.entrySet()) {
                JIdentifier type = entry.getKey();
                Object value = entry.getValue();

                if (value != null) {
                    GNode initializer = GNode.create("Initializer");
                    initializer
                    .add(type.getName())
                    .add(null);

                    if (value instanceof String)
                        initializer.add(GNode.create("StringLiteral", value.toString()));
                    else
                        initializer.add(GNode.create("NonStringLiteral", value.toString()));

                    initializers.add(initializer);
                }
            }
        }

        root.add("__" + cls);
        root.add(parameters);
        root.add(initializers);

        return root;
    }

    private static GNode createMethodDeclaration(String name, List<String> mods, JClass cls, String origin, JMethod method) {
        GNode root = createMethodDeclaration(
                         name,
                         mods,
                         mapType(method.getRetType()),
                         method.getName(),
                         origin,
                         method.getArgs()
                     );

        return root;
    }

    private static GNode createMethodDeclaration(String nodeName, List<String> mods, String retType, String name, String cls, List<JIdentifier> params) {
        GNode root = GNode.create(nodeName);

        GNode modifiers = GNode.create("Modifiers");
        if (mods != null)
            for (String mod : mods)
                modifiers.add(mod);

        GNode parameters = GNode.create("Parameters");
        if (params != null)
            for (JIdentifier param : params)
                parameters.add(mapType(param));

        root.add(modifiers);
        root.add(retType);
        root.add(name);
        root.add(cls);
        root.add(parameters);

        return root;
    }

// }}}

// Field Declaration // {{{

    private static GNode createFieldDeclaration(JIdentifier member) {
        List<Integer> mods = member.getMods();
        if (mods.contains(JModifiers.STATIC))
            return createFieldDeclaration(Arrays.asList("static"), mapType(member), member.getName());
        return createFieldDeclaration(null, mapType(member), member.getName());
    }

    private static GNode createFieldDeclaration(List<String> mods, String type, String name) {
        GNode root = GNode.create("FieldDeclaration");

        GNode modifiers = GNode.create("Modifiers");
        if (mods != null)
            for (String mod : mods)
                modifiers.add(mod);

        root.add(modifiers);
        root.add(type);
        root.add(name);

        return root;
    }

// }}}

}
