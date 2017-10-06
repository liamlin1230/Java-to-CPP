package edu.nyu.oop.visitors;

import org.junit.*;
import org.slf4j.Logger;

import xtc.tree.GNode;
import xtc.tree.Node;
import edu.nyu.oop.XtcTestUtils;
import edu.nyu.oop.util.NodeUtil;
import edu.nyu.oop.constructs.*;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.*;


public class ClassVisitorTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ClassVisitorTest.class);

    private static GNode node = null;

    @BeforeClass
    public static void beforeClass() {
        String input = "src/test/java/inputs/test021/Test021.java";
        node = (GNode) XtcTestUtils.loadTestFile(input);
        List<Node> nodes = NodeUtil.dfsAll(node, "ClassDeclaration");
        for (Node n : nodes) {
            if (n.getString(1).equals("Test021"))
                node = (GNode) n;
        }

        logger.info("Executing ClassVisitorTest with input file: " + input);
    }

    @Test
    public void testProcess() {
        ClassVisitor visitor = new ClassVisitor();
        JClass cls = visitor.process(node, null, null);

        // public class Test021
        logger.info("testing public class Test021");
        assertEquals(cls.getName(), "Test021");
        assertEquals(cls.getMods().get(0).intValue(), JModifiers.PUBLIC);

        Map<JIdentifier, Object> members = cls.getMembers();

        // private int x = 3
        logger.info("testing private member int x = 3, y = 4");
        JIdentifier xIdent = VisitorTestUtils.findMemberId(members, "x");
        Integer xVar = (Integer) members.get(xIdent);
        assertEquals(xIdent.getType(), JType.JTYPE_INT);
        assertEquals(xIdent.getName(), "x");
        assertEquals(xVar.intValue(), 3);
        JIdentifier yIdent = VisitorTestUtils.findMemberId(members, "y");
        Integer yVar = (Integer) members.get(yIdent);
        assertEquals(yIdent.getType(), JType.JTYPE_INT);
        assertEquals(yIdent.getName(), "y");
        assertEquals(yVar.intValue(), 4);

        // private int[] z
        logger.info("testing private member int[] z");
        JIdentifier zIdent = VisitorTestUtils.findMemberId(members, "z");
        Integer zVar = (Integer) members.get(zIdent);
        assertEquals(zIdent.getType(), JType.JTYPE_INT);
        assertEquals(zIdent.getName(), "z");
        assertEquals(zIdent.getDimensions(), 1);

        List<JMethod> methods = new LinkedList(cls.getMethods());

        // public static void main(String[] args) -- removed main from Test021
        // logger.info("Testing main");
        // JMethod main = methods.get(1);
        // assertEquals(main.getName(), "main");
        // assertEquals(main.getRetType().getType(), JType.JTYPE_VOID);
        // List<JIdentifier> args = main.getArgs();
        // JIdentifier arg = args.get(0);
        // assertEquals(arg.getType(), JType.JTYPE_STRING);
        // List<Integer> mods = main.getMods();
        // assertEquals(mods.get(0).intValue(), JModifiers.PUBLIC);
        // assertEquals(mods.get(1).intValue(), JModifiers.STATIC);

        // static int x()
        logger.info("Testing method x");
        JMethod x = methods.get(1);
        assertEquals(x.getName(), "x");
        assertEquals(x.getRetType().getType(), JType.JTYPE_INT);
        List<JIdentifier> args = x.getArgs();
        // empty arg list
//        assertEquals(args, new LinkedList<GNode>());
        List<Integer> mods = x.getMods();
        assertEquals(mods.get(0).intValue(), JModifiers.STATIC);

        // Test021(int i)
        logger.info("Testing the constructor");
        JMethod constructor = cls.getConstructors().get(0);
        args = constructor.getArgs();
        JIdentifier arg = args.get(0);
        assertEquals(arg.getType(), JType.JTYPE_INT);
        mods = constructor.getMods();
        assertEquals(mods.get(0).intValue(), JModifiers.PACKAGE);
    }
}
