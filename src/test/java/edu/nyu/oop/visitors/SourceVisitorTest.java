package edu.nyu.oop.visitors;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import edu.nyu.oop.XtcTestUtils;
import edu.nyu.oop.constructs.JClass;
import edu.nyu.oop.constructs.JSource;

import xtc.tree.GNode;


public class SourceVisitorTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(SourceVisitorTest.class);

    private static GNode node = null;

    @BeforeClass
    public static void beforeClass() {
        String input = "src/test/java/inputs/test021/Test021.java";
        node = (GNode) XtcTestUtils.loadTestFile(input);

        logger.info("Executing SourceVisitorTest with input file: " + input);
    }

    @Test
    public void testProcess() {
        SourceVisitor visitor = new SourceVisitor();
        JSource source = visitor.process(node, null);
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
