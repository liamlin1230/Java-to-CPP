package edu.nyu.oop;

import edu.nyu.oop.constructs.JClass;
import edu.nyu.oop.constructs.JSource;
import edu.nyu.oop.util.NodeUtil;
import edu.nyu.oop.SourceParser;
import org.junit.*;
import org.slf4j.Logger;

import xtc.tree.GNode;

import java.util.List;
import java.io.File;

public class CppHeaderPrinterTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(CppHeaderPrinterTest.class);

    private static String input = "src/test/java/inputs/test010/Test010.java";

    private static List<JSource> sources = null;
    private static GNode n = null;

    private static ClassResolver resolver = new ClassResolver();

    @BeforeClass
    public static void beforeClass() {
        File f = SourceParser.loadSourceFile(input);
        n = (GNode) NodeUtil.parseJavaFile(f);

        logger.info("Executing CppHeaderPrinter with input file: " + input);
    }

    @Test
    public void testPrint() {
        logger.info("Printing file...");

        List<JSource> sources = SourceParser.parse(n);
        JClass object = resolver.resolve(sources);
        JSource source = sources.get(3);
        GNode root = ASTGenerator.createTree(object, source);

        logger.info("Root name: " + root.getName());

        CppHeaderPrinter printer = new CppHeaderPrinter(root);
        printer.print();
    }
}
