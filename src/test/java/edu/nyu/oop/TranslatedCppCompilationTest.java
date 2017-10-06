package edu.nyu.oop;

import edu.nyu.oop.constructs.JClass;
import edu.nyu.oop.constructs.JSource;
import edu.nyu.oop.util.NodeUtil;
import org.junit.*;
import org.slf4j.Logger;

import xtc.tree.GNode;

import java.io.*;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TranslatedCppCompilationTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TranslatedCppCompilationTest.class);

    private static String input = "src/test/java/inputs/test020/Test020.java";

    private static List<JSource> sources = null;
    private static GNode n = null;

    private static ClassResolver resolver = new ClassResolver();

    @BeforeClass
    public static void beforeClass() {
        File f = SourceParser.loadSourceFile(input);
        n = (GNode) NodeUtil.parseJavaFile(f);

        logger.info("Executing CppCombinedPrinter with input file: " + input);
    }

    @Test
    public void testPrint() {
        logger.info("Printing header and implementation files...");

        List<JSource> sources = SourceParser.parse(n);
        Map<String, JClass> classes = SourceParser.getClasses();
        JClass object = resolver.resolve(sources);
        JSource source = sources.get(3);
        GNode root = ASTGenerator.createTree(object, source);

        logger.info("Root name: " + root.getName());

        CppHeaderPrinter headerPrinter = new CppHeaderPrinter(root);
        CppImplementationPrinter implementationPrinter = new CppImplementationPrinter(source.getNode(), classes);

        headerPrinter.print();
        implementationPrinter.print();

        // Compile C++
        logger.info("Compiling C++ files...");
        OutputCompiler output = new OutputCompiler(headerPrinter, implementationPrinter, false);

        for (String w : output.getWarnings()) {
            logger.warn(w);
        }

        for (String e : output.getErrors()) {
            logger.error(e);
        }

        assertEquals(output.didCompile(), true);
    }
}
