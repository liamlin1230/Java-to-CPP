package edu.nyu.oop;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import edu.nyu.oop.constructs.JClass;
import edu.nyu.oop.constructs.JSource;
import edu.nyu.oop.util.NodeUtil;

import xtc.tree.GNode;


public class CppImplementationPrinterTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(CppImplementationPrinterTest.class);

    private static ClassResolver resolver = new ClassResolver();

    private static String input = "src/test/java/inputs/test001/Test001.java";

    private static GNode n = null;


    @BeforeClass
    public static void beforeClass() {
        File f = SourceParser.loadSourceFile(input);
        n = (GNode) NodeUtil.parseJavaFile(f);

        logger.info("Executing JavaPrinterTest with input file: " + input);
    }

    @Test
    public void testCreateTree() {
        logger.info("Printing file...");

        List<JSource> sources = SourceParser.parse(n);
        Map<String, JClass> classes = SourceParser.getClasses();
        resolver.resolve(sources);

        JSource source = sources.get(3);

        CppImplementationPrinter p = new CppImplementationPrinter(source.getNode(), classes);
        p.print();
    }
}
