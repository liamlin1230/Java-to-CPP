package edu.nyu.oop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import edu.nyu.oop.constructs.*;

import xtc.tree.GNode;
import xtc.util.Runtime;
import xtc.lang.JavaEntities;

public class Translator {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(Translator.class);

    private static ClassResolver resolver = new ClassResolver();

    static void translate(GNode n) {
        List<JSource> sources = SourceParser.parse(n);
        Map<String, JClass> classes = SourceParser.getClasses();

        JClass object = resolver.resolve(sources);

        JSource source = sources.get(3);
        GNode headerRoot = ASTGenerator.createTree(object, source);

        CppHeaderPrinter headerPrinter = new CppHeaderPrinter(headerRoot);
        headerPrinter.print();

        CppImplementationPrinter implementationPrinter = new CppImplementationPrinter(source.getNode(), classes);
        implementationPrinter.print();

        OutputCompiler output = new OutputCompiler(headerPrinter, implementationPrinter, false);

        for (String w : output.getWarnings()) {
            logger.warn(w);
        }

        for (String e : output.getErrors()) {
            logger.error(e);
        }
    }

}
