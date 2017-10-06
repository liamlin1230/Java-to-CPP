package edu.nyu.oop;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import edu.nyu.oop.constructs.JClass;
import edu.nyu.oop.constructs.JSource;
import edu.nyu.oop.util.NodeUtil;

import xtc.tree.GNode;


public class SourceParserTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(SourceParserTest.class);

    private static String input = "src/test/java/inputs/test021/Test021.java";

    private static GNode n = null;
    private static List<JSource> sources = null;

    @BeforeClass
    public static void beforeClass() {
        File f = SourceParser.loadSourceFile(input);
        n = (GNode) NodeUtil.parseJavaFile(f);
        sources = SourceParser.parse(n);

        logger.info("Executing SourceParserTest with input file: " + input);
    }

    @Test
    public void testProcess() {
        JSource source = sources.get(3);
        Map<String, JClass> classes = source.getClasses();

        logger.info("Testing to see if classes contains: A, B, and Test015");
        assertEquals(classes.size(), 3);
        assertEquals(classes.get("A").getName(), "A");
        assertEquals(classes.get("B").getName(), "B");
        assertEquals(classes.get("Test021").getName(), "Test021");

        logger.info("Testing package");
        assertEquals(source.getPkg().get(0), "inputs");
        assertEquals(source.getPkg().get(1), "test021");

        logger.info("Testing import");
        assertEquals(source.getImports().get(0), "inputs.test019.Test019");
    }
}
