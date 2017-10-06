package edu.nyu.oop;

import org.slf4j.Logger;

import edu.nyu.oop.visitors.BlockVisitor;
import edu.nyu.oop.constructs.*;
import edu.nyu.oop.util.*;
import xtc.tree.GNode;

import java.util.*;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;

public class ClassResolver {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ClassResolver.class);

    private boolean debug = false;

    private Queue<String> unprocessed;
    private Queue<String> blocked;
    private Map<String, JClass> classes;

    public ClassResolver() {
        this(false);
    }

    public ClassResolver(boolean debug) {
        this.unprocessed = new LinkedList<String>();
        this.blocked = new LinkedList<String>();
        this.classes = new LinkedHashMap<String, JClass>();
        this.debug = debug;
    }

    // Returns Object JClass the head of the hierarchy
    public JClass resolve(List<JSource> sources) {
        JClass object = null;

        // Initialize the unprocessed classes
        for (JSource source : sources) {
            for (Entry<String, JClass> entry : source.getClasses().entrySet()) {
                JClass cls = entry.getValue();

                if (!Arrays.asList(JClass.bundled).contains(cls.getName()))
                    cls.setPkg(source.getPkg());
                else
                    cls.setPkg(new LinkedList<String>(Arrays.asList("java", "lang")));

                classes.put(cls.getName(), cls);
                unprocessed.add(cls.getName());
            }
        }

        if (debug) logger.debug("Started resolver with: " + unprocessed.size() + " classes in unprocessed");

        // Initialize each classes children list
        for (Entry<String, JClass> entry : classes.entrySet()) {
            JClass cls = entry.getValue();
            if (!cls.getName().equals("Object")) {
                JClass parent = classes.get(cls.getParent());
                parent.addChild(cls);
            }
        }

        while (!unprocessed.isEmpty()) {
            JClass cls = classes.get(unprocessed.remove());
            String parent = cls.getParent();
            JClass parentCls = classes.get(parent);

            if (debug) {
                logger.debug("dequeued: " + cls.getName());
                logger.debug("unprocessed has " + unprocessed.size() + " classes");
                for (String clsString : unprocessed)
                    logger.debug("in unprocessed - " + clsString);
            }

            if (cls.getName().equals("Object")) object = cls;

            if (unprocessed.contains(parent)) {
                if (debug) logger.debug("blocked: " + cls.getName());

                blocked.add(cls.getName());
            } else {
                if (debug) logger.debug("processed: " + cls.getName());

                if (cls.getChildren().size() != 0)
                    unblockChildren(cls);

                if (parentCls != null) {
                    cls.setResolvedMethods(updatedMethods(cls, parentCls));
                    cls.setResolvedMembers(updateMembers(cls, parentCls));
                }
            }
        }

        BlockVisitor blockV = new BlockVisitor();
        blockV.processAll();

        return object;
    }

    private void unblockChildren(JClass cls) {
        for (JClass child : cls.getChildren()) {
            if (blocked.contains(child.getName())) {
                blocked.remove(child.getName());
                unprocessed.add(child.getName());
            }
        }
    }

    private Map<JMethod, String> updatedMethods(JClass cls, JClass parent) {
        List<JMethod> methods = new LinkedList<JMethod>(cls.getMethods());
        Map<JMethod, String> parentMethods = parent.getResolvedMethods();
        Map<JMethod, String> resolvedMethods = new LinkedHashMap<JMethod, String>();

        for (Entry<JMethod, String> entry : parentMethods.entrySet()) {
            JMethod pMethod = entry.getKey();
            String origin = entry.getValue();

            JMethod newMethod = null;
            String newOrigin = null;
            boolean bundledMethod = false;
            for (JMethod method : methods) {
                if (method.getName().equals(pMethod.getName())) {
                    newMethod = method;
                    newOrigin = cls.getName();
                    methods.remove(method);
                    if (Arrays.asList(JClass.bundled).contains(origin))
                        bundledMethod = true;
                    break;
                }
            }
            if (newMethod == null) {
                newMethod = new JMethod(pMethod);
                newOrigin = origin;
            }

            List<JIdentifier> newArgs = new LinkedList<JIdentifier>(newMethod.getArgs());
            if (pMethod.getName().startsWith("init"))
                newArgs.set(0, new JIdentifier(null, cls, "o", "__" + cls.getName() + "*", 0, null));
            else if (newArgs.size() > 0 || bundledMethod)
                newArgs.set(0, new JIdentifier(null, cls, "o", cls.getName(), 0, null));
            else
                newArgs.add(0, new JIdentifier(null, cls, "o", cls.getName(), 0, null));

            newMethod.setArgs(newArgs);

            resolvedMethods.put(newMethod, newOrigin);
        }

        Map<JMethod, String> mappedMethods = new LinkedHashMap<JMethod, String>();

        for (JMethod method : methods)
            mappedMethods.put(method, cls.getName());

        resolvedMethods.putAll(mappedMethods);

        return resolvedMethods;
    }

    private Map<JIdentifier, Object> updateMembers(JClass cls, JClass parent) {
        Map<JIdentifier, Object> members = cls.getMembers();
        Map<JIdentifier, Object> parentMembers = parent.getResolvedMembers();
        Map<JIdentifier, Object> resolvedMembers = new LinkedHashMap<JIdentifier, Object>();
        List<String> usedNames = new LinkedList<>();

        for (Entry<JMethod, String> entry : cls.getResolvedMethods().entrySet())
            usedNames.add(entry.getKey().getName());

        for (Entry<JIdentifier, Object> entry : parentMembers.entrySet()) {
            boolean inherit = true;
            JIdentifier pMember = entry.getKey();
            Object pValue = entry.getValue();
            for (Entry<JIdentifier, Object> localEntry : members.entrySet()) {
                JIdentifier member = localEntry.getKey();
                if (member.getName().equals(pMember.getName()))
                    inherit = false;
            }
            if (inherit)
                resolvedMembers.put(getUnusedName(usedNames, pMember), pValue);
        }

        for (Entry<JIdentifier, Object> entry : members.entrySet()) {
            JIdentifier member = entry.getKey();
            Object value = entry.getValue();

            resolvedMembers.put(getUnusedName(usedNames, member), value);
        }

        return resolvedMembers;
    }

    private JIdentifier getUnusedName(List<String> usedNames, JIdentifier ident) {
        String newName = ident.getName();
        if (usedNames.contains(newName))
            newName += "Member";

        ident.setName(newName);
        GNode newNameNode = GNode.create("Declarator", newName, null, null);
        ident.setNode(newNameNode);

        return ident;
    }

}
