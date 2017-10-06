package edu.nyu.oop;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class OutputCompiler {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(OutputCompiler.class);

    private CppHeaderPrinter headerPrinter;
    private CppImplementationPrinter implementationPrinter;
    private int result = 1;
    private String inputStreamString;

    public OutputCompiler(CppHeaderPrinter headerPrinter, CppImplementationPrinter implementationPrinter, boolean logInputStream) {
        this.headerPrinter = headerPrinter;
        this.implementationPrinter = implementationPrinter;

        compile(logInputStream);
    }

    public void compile(boolean logInputStream) {
        try {
            Process process = new ProcessBuilder("g++", "output/java_lang.cpp", "output/output.cpp", "-o", "output/a.out")
                    .redirectErrorStream(true).start();

            result = process.waitFor();

            InputStream es = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(es));
            StringBuilder sb = new StringBuilder();
            String line = null;

            while ((line = br.readLine()) != null) {
                if (logInputStream)
                    logger.error(line);
                sb.append(line);
                sb.append(System.getProperty("line.separator"));
            }

            inputStreamString = sb.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean didCompile() {
        return (result == 0) ? true : false;
    }

    public String getInputStream() {
        return inputStreamString;
    }

    public ArrayList<String> getWarnings() {
        ArrayList<String> list = new ArrayList<String>();
        boolean partOfWarning = false;

        String[] lines = inputStreamString.split("\\n");

        for (String l: lines) {
            if (l.startsWith("output/")) {
                // new block - error or warning
                if (l.matches("^(output/(output.cpp|output.h|java_lang.h):[0-9]*\\:[0-9]*\\:)\\ (warning|note):.*$")) {
                    partOfWarning = true;
                    list.add(l);
                } else {
                    partOfWarning = false;
                }
            } else {
                if (partOfWarning) {
                    list.add(l);
                }
            }
        }
        return list;
    }

    public ArrayList<String> getErrors() {
        ArrayList<String> list = new ArrayList<String>();
        boolean partOfError = false;

        String[] lines = inputStreamString.split("\\n");

        for (String l: lines) {
            if (l.startsWith("output/")) {
                // new block - error or warning
                if (l.matches("^(output/(output.cpp|output.h|java_lang.h):[0-9]*\\:[0-9]*\\:)\\ (error|note):.*$")) {
                    partOfError = true;
                    list.add(l);
                } else {
                    partOfError = false;
                }
            } else {
                if (partOfError) {
                    list.add(l);
                }
            }
        }
        return list;
    }

    public int getWarningCount() {
        String[] lines = inputStreamString.split("\\n");
        String lastLine = lines[lines.length - 1];

        if (lastLine.matches("^([0-9]*\\ (warning(|s)|error(|s))(| and [0-9]*\\ error(|s))) generated.$")) {

            Pattern p = Pattern.compile("\\d+ warning(|s)");
            Matcher m = p.matcher(lastLine);

            return (m.find()) ? Integer.parseInt(m.group().replaceAll(" warning(|s)", "")) : 0;

        } else {
            return 0;
        }
    }

    public int getErrorCount() {
        String[] lines = inputStreamString.split("\\n");
        String lastLine = lines[lines.length - 1];

        if (lastLine.matches("^([0-9]*\\ (warning(|s)|error(|s))(| and [0-9]*\\ error(|s))) generated.$")) {

            Pattern p = Pattern.compile("\\d+ error(|s)");
            Matcher m = p.matcher(lastLine);

            return (m.find()) ? Integer.parseInt(m.group().replaceAll(" error(|s)", "")) : 0;

        } else {
            return 0;
        }
    }
}