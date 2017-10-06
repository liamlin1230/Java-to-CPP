package edu.nyu.oop;

import org.slf4j.Logger;
import xtc.parser.ParseException;
import xtc.tree.GNode;
import edu.nyu.oop.util.JavaFiveImportParser;
import edu.nyu.oop.util.XtcProps;
import edu.nyu.oop.util.NodeUtil;
import edu.nyu.oop.visitors.SourceVisitor;
import edu.nyu.oop.constructs.JSource;
import edu.nyu.oop.constructs.JClass;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;


public class SourceParser {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(SourceParser.class);

    private static String classesPath = "src/main/java/edu/nyu/oop/classes";

    private static Map<String, JClass> classes = null;

    public static List<JSource> parse(GNode n) {
        SourceVisitor visitor = new SourceVisitor();

        Queue<GNode> sources = new LinkedList<GNode>();
        List<JSource> processedSources = new LinkedList<JSource>();
        classes = new HashMap<String, JClass>();

        sources.add((GNode) NodeUtil.parseJavaFile(loadSourceFile(classesPath + "/Object.java")));
        sources.add((GNode) NodeUtil.parseJavaFile(loadSourceFile(classesPath + "/Class.java")));
        sources.add((GNode) NodeUtil.parseJavaFile(loadSourceFile(classesPath + "/String.java")));

        sources.add(n);
        // TODO: import the imports' imports
        sources.addAll(JavaFiveImportParser.parse(n));

        while (!sources.isEmpty()) {
            JSource source = visitor.process(sources.remove(), classes);
            processedSources.add(source);
        }

        return processedSources;
    }

    public static File loadSourceFile(String path) {
        File f = new File(path);
        if(f == null)  {
            logger.warn("Invalid path provided for file. " + path);
            return null;
        }

        if(f.isFile() && f.getName().endsWith(".java")) {
            logger.debug("Loading " + f.getName());
            return f;
        } else {
            return null;
        }
    }

    public static Map<String, JClass> getClasses() {
        return classes;
    }
}
