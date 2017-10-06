package edu.nyu.oop;

import org.junit.*;
import org.slf4j.Logger;

import xtc.tree.GNode;
import xtc.tree.Node;
import edu.nyu.oop.util.NodeUtil;
import edu.nyu.oop.SourceParser;
import edu.nyu.oop.constructs.*;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.io.File;

import static org.junit.Assert.*;


public class ClassResolverTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ClassResolverTest.class);

    private static String input = "src/test/java/inputs/test007/Test007.java";

    private static GNode n = null;
    private static List<JSource> sources = null;

    private static ClassResolver resolver = new ClassResolver(true);

    @BeforeClass
    public static void beforeClass() {
        File f = SourceParser.loadSourceFile(input);
        n = (GNode) NodeUtil.parseJavaFile(f);
        sources = SourceParser.parse(n);

        logger.info("Executing ClassResolverTest with input file: " + input);
    }

    @Test
    public void testResolve() {
        resolver.resolve(sources);

        JSource test = sources.get(3);

        List<JClass> classes = new LinkedList(test.getClasses().values());

        for (JClass cls : classes) {
            System.out.println("Class name: " + cls.getName());
            for (Entry<JMethod, String> entry : cls.getResolvedMethods().entrySet()) {
                JMethod method = entry.getKey();
                String origin = entry.getValue();
                System.out.println("Method name: " + method.getName() + " Origin: " + origin);
            }
            for (Entry<JIdentifier, Object> entry : cls.getResolvedMembers().entrySet()) {
                JIdentifier member = entry.getKey();
                Object value = entry.getValue();
                String valString = "";
                if (value != null)
                    valString = value.toString();
                System.out.println("Member name: " + member.getName() + " Value: " + valString);
            }
        }
    }
}
