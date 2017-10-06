package edu.nyu.oop.visitors;

import org.junit.*;
import org.slf4j.Logger;

import xtc.tree.GNode;
import edu.nyu.oop.XtcTestUtils;
import edu.nyu.oop.util.NodeUtil;
import edu.nyu.oop.constructs.*;

import java.util.List;

import static org.junit.Assert.*;


public class MethodVisitorTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(MethodVisitorTest.class);

    private static GNode node = null;

    @BeforeClass
    public static void beforeClass() {
        String input = "src/test/java/inputs/test000/Test000.java";
        node = (GNode) XtcTestUtils.loadTestFile(input);
        node = (GNode) NodeUtil.dfs(node, "MethodDeclaration");

        logger.info("Executing MethodVisitorTest with input file: " + input);
    }

    @Test
    public void testProcess() {
        MethodVisitor visitor = new MethodVisitor();
        JMethod method = visitor.parse(node, null);

        logger.info("Checking name is main");
        assertEquals(method.getName(), "main");

        logger.info("Checking args has String[]");
        List<JIdentifier> args = method.getArgs();
        JIdentifier onlyArg = args.get(0);
        assertEquals(onlyArg.getName(), "args");
        assertEquals(onlyArg.getType(), JType.JTYPE_STRING);
        assertEquals(onlyArg.getDimensions(), 1);

        logger.info("Checking that return is void");
        JType retType = method.getRetType();
        assertEquals(retType.getType(), JType.JTYPE_VOID);

        logger.info("Checking that method is public and static");
        List<Integer> mods = method.getMods();
        assertEquals(mods.get(0).intValue(), JModifiers.PUBLIC);
        assertEquals(mods.get(1).intValue(), JModifiers.STATIC);
    }
}
