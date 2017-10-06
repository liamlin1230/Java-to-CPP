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


public class ASTGeneratorTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ASTGeneratorTest.class);

    private static String input = "src/test/java/inputs/test010/Test010.java";

    private static GNode n = null;
    private static List<JSource> sources = null;

    private static ClassResolver resolver = new ClassResolver(true);

    @BeforeClass
    public static void beforeClass() {
        File f = SourceParser.loadSourceFile(input);
        n = (GNode) NodeUtil.parseJavaFile(f);
        sources = SourceParser.parse(n);

        logger.info("Executing SourceParserTest with input file: " + input);
    }

    @Test
    public void testProcess() {
        JClass object = resolver.resolve(sources);

        GNode root = ASTGenerator.createTree(object, sources.get(3));
        XtcTestUtils.prettyPrintAst(root);
    }
}
