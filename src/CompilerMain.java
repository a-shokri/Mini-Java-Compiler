import analyser.MethodSymbol;
import analyser.NameAnalyzer;
import analyser.errors.AnalysisError;
import ast.MethodDeclaration;
import ast.Program;
import ast.Statement;
import ast.Tree;
import codegen.CodeGenerator;
import java_cup.runtime.Symbol;
import lexer.CommonConstants;
import lexer.Lexer;
import misc.PrettyPrint;
import optimizer.ControlFlowGraphGenerator;
import optimizer.ControlFlowGraphNode;
import optimizer.DeadVariableEliminator;
import parser.ParseException;
import parser.Parser;
import parser.TreePrinter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*

    Filename: CompilerMain.java
    Version: 1
    ID: 1.1

 */

/*

    This file is the main class that handles and executes all the compiler operations

    @author Ali Shokri
    @author Manan Joshi

 */

public class CompilerMain {

    // Default constructor
    CompilerMain() {
    }


    public static void main(String[] args) throws IOException, ParseException {

        CompilerMain main = new CompilerMain();

        if (args.length < 2) {

            if (!(args.length == 1)) {

                System.out.println("USAGE: java -jar (Main JAR File) <options> <filenames>");
                System.exit(0);

            }

        }

        String options = args[0];

        switch (options) {

            case "--lex":
                main.lexer(args);
                break;

            case "--ast":
                main.parser(args);
                break;

            case "--name":
                main.nameAnalyser(args);
                break;

            case "--pp":
                main.prettyPrinter(args);
                break;

            case "--type":
                main.typeAnalysis(args);
                break;

            case "--completeTest":
                main.completeTest(args[1]);
                break;

            case "--cgen":
                main.codeGenerator(args);
                break;

            case "--cfg":
                main.cfgGenerator(args);
                break;
            case "--opt":
                main.opt(args);
                break;
            case "--help":
                System.out.println("Available options:");
                System.out.println("--lex");
                System.out.println("--ast");
                System.out.println("--name");
                System.out.println("--pp");
                System.out.println("--type");
                System.out.println("--cgen");
                System.out.println("--cfg");
                System.out.println("--opt");

                System.exit(0);

            default:
                System.out.println("Invalid option");
                System.out.println("Available options:");
                System.out.println("--lex");
                System.out.println("--ast");
                System.out.println("--name");
                System.out.println("--pp");
                System.out.println("--type");
                System.out.println("--help");
                System.out.println("--cgen");
                System.out.println("--cfg");
                System.out.println("--opt");
                System.exit(0);

        }

    }

    /*
        This methods contains calls to the main lexer file and write the output generated to a
        lexed file.

        @param filenames
     */

    private void lexer(String[] filenames) throws IOException {

        if (filenames.length == 1) {
            System.out.println("Please specify at least 1 input file.");
            System.exit(0);
        }

        FileReader reader = null;
        FileWriter writer = null;

        for (int i = 1; i < filenames.length; i++) {

            String fileExt = filenames[i].substring(filenames[i].lastIndexOf('.') + 1);

            if (!fileExt.equals("emj")) {

                System.err.println("Invalid file extension.");
                System.exit(0);

            }

            String filePath = new File("").getAbsolutePath();
            File inputFile = new File(filePath + File.separator + filenames[i]);

            if (inputFile.exists() && inputFile.canRead())
                System.out.println("Lexing File :: " + filenames[i]);

            try {

                reader = new FileReader(inputFile);
                writer = new FileWriter(filenames[i].substring(0, filenames[i].lastIndexOf(".")) + ".lexed");

                Lexer lexer = new Lexer(reader);

                Symbol symbol = lexer.next_token();

                while (symbol != null && symbol.sym != CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.EOF)) {

                    writer.write(symbol.toString());
                    writer.write("\n");

                    symbol = lexer.next_token();

                    if (symbol.sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.EOF)) {
                        writer.write(symbol.toString());
                        break;
                    }

                }

                System.out.println("File Created :: " + filenames[i].substring(0, filenames[i].lastIndexOf(".")) + ".lexed");

                if (filenames.length > 2) {
                    System.out.println();
                    System.out.println("###########################");
                    System.out.println();
                }

            } catch (FileNotFoundException e) {
                System.err.println(filenames[i] + " - File not found!");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Please check the input parameters.");
            } finally {
                if (writer != null)
                    writer.close();
                if (reader != null)
                    reader.close();
            }

        }

    }

    private void parser(String[] filenames) throws ParseException {

        if (filenames.length == 1) {
            System.out.println("Please specify at least 1 input file.");
            System.exit(0);
        }

        TreePrinter printer = new TreePrinter();

        for (int i = 1; i < filenames.length; i++) {

            if (checkFileValidity(filenames[i])) {

                try {

                    System.out.println("Parsing File :: " + filenames[i]);

                    File file = new File(filenames[i]);
                    FileReader reader = new FileReader(file);

                    Lexer lexer = new Lexer(reader);

                    Parser parser = new Parser(lexer);

                    Tree tree = parser.parseTree();

                    if (tree != null) {

                        String string = printer.visit((Program) tree);
                        FileWriter writer = new FileWriter(filenames[i].substring(0, filenames[i].lastIndexOf(".")) + ".ast");
                        writer.write(string);
                        writer.close();

                    }


                    System.out.println("File Created :: " + filenames[i].substring(0, filenames[i].lastIndexOf(".")) + ".ast");

                    if (filenames.length > 2) {
                        System.out.println();
                        System.out.println("###########################");
                        System.out.println();
                    }


                } catch (FileNotFoundException e) {

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException p) {
                    System.out.println(p.getMessage());
                }

            } else
                return;

        }

    }

    private void nameAnalyser(String[] filenames) throws ParseException {

        if (filenames.length == 1) {
            System.out.println("Please specify at least 1 input file.");
            System.exit(0);
        }

        for (int i = 1; i < filenames.length; i++) {

            if (checkFileValidity(filenames[i])) {


                new CompilerMain().analysis(filenames[i], true);

                if (filenames.length > 2) {
                    System.out.println();
                    System.out.println("###########################");
                    System.out.println();
                }


            } else
                return;

        }

    }

    private void prettyPrinter(String[] filenames) throws ParseException {

        if (filenames.length == 1) {
            System.out.println("Please specify at least 1 input file.");
            System.exit(0);
        }

        for (int i = 1; i < filenames.length; i++) {

            if (checkFileValidity(filenames[i])) {

                try {

                    File file = new File(filenames[i]);

                    FileReader reader = new FileReader(file);

                    Lexer lexer = new Lexer(reader);

                    Parser parser = new Parser(lexer);

                    Tree tree = parser.parseTree();

                    if (tree != null) {

                        NameAnalyzer nameAnalyzer = new NameAnalyzer((Program) tree, true);
                        nameAnalyzer.nameAnalysis();

                        String string = new PrettyPrint(nameAnalyzer).visit(nameAnalyzer);
                        System.out.println(string);

                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    System.exit(1);
                }

            } else
                return;

        }

    }

    private void completeTest(String benchmarkFolderName) {
        File folder = new File(benchmarkFolderName);
        List<String> fileNameList = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.getName().endsWith(".emj"))
                fileNameList.add(benchmarkFolderName + "/" + fileEntry.getName());

        }
        for (int i = 0; i < fileNameList.size(); i++) {
            String testFileName = fileNameList.get(i);
            String[] fileNameArray = {"", testFileName};
            System.out.println("\n------------------ Starting testing the compiler with file " + testFileName + ". " + (i + 1) + "/" + fileNameList.size());
            System.out.println("--- Lexer ---");

            try {
                new CompilerMain().lexer(fileNameArray);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("\n--- Parser ---");
            try {
                new CompilerMain().parser(fileNameArray);
            } catch (ParseException e) {
                System.out.println(e.getMessage());
            }
            System.out.println("\n--- Name Analyzer ---");
            try {
                new CompilerMain().nameAnalyser(fileNameArray);
            } catch (ParseException e) {
                System.out.println(e.getMessage());
            }
            System.out.println("\n--- Pretty Printer ---");
            try {
                new CompilerMain().prettyPrinter(fileNameArray);
            } catch (ParseException e) {
                System.out.println(e.getMessage());
            }
            System.out.println("\n--- Type Analyzer ---");
            try {
                new CompilerMain().typeAnalysis(fileNameArray);
            } catch (ParseException e) {
                System.out.println(e.getMessage());
            }
            System.out.println("------------------ End of testing the compiler with file " + testFileName + "\n");

        }


    }

    private void typeAnalysis(String[] filenames) throws ParseException {

        if (filenames.length == 1) {
            System.out.println("Please specify at least 1 input file.");
            System.exit(0);
        }

        for (int i = 1; i < filenames.length; i++) {

            if (checkFileValidity(filenames[i])) {

                new CompilerMain().analysis(filenames[i], false);

                if (filenames.length > 2) {
                    System.out.println();
                    System.out.println("###########################");
                    System.out.println();
                }

            } else
                return;

        }

    }

    private void codeGenerator(String[] filenames) throws ParseException, FileNotFoundException {

        if (filenames.length == 1) {
            System.out.println("Please specify at least 1 input file.");
            System.exit(0);
        }

        for (int i = 1; i < filenames.length; i++) {

            if (checkFileValidity(filenames[i])) {

                File file = new File(filenames[i]);

                FileReader reader = new FileReader(file);

                Lexer lexer = new Lexer(reader);

                Parser parser = new Parser(lexer);

                Tree tree = parser.parseTree();

                if (tree != null) {

                    new CompilerMain().analysis(filenames[i], true);

                    new CompilerMain().analysis(filenames[i], false);

                    NameAnalyzer nameAnalyzer = new NameAnalyzer((Program) tree, true);
                    nameAnalyzer.nameAnalysis();

                    CodeGenerator codeGenerator = new CodeGenerator(nameAnalyzer, file.getAbsolutePath());
                    codeGenerator.visit(nameAnalyzer.program);

                    if (filenames.length > 2) {
                        System.out.println();
                        System.out.println("###########################");
                        System.out.println();
                    }

                }

            }

        }

    }

    private void opt( String[] filenames ) throws ParseException, FileNotFoundException {


        if (filenames.length == 1) {
            System.out.println("Please specify at least 1 input file.");
            System.exit(0);
        }

        for (int i = 1; i < filenames.length; i++) {

            if (checkFileValidity(filenames[i])) {

                File file = new File(filenames[i]);

                FileReader reader = new FileReader(file);

                Lexer lexer = new Lexer(reader);

                Parser parser = new Parser(lexer);

                Tree tree = parser.parseTree();

                if (tree != null) {


                    NameAnalyzer nameAnalyzer = new CompilerMain().analysis(filenames[i], false);

                    ControlFlowGraphGenerator cfgGenerator = new ControlFlowGraphGenerator(nameAnalyzer);
                    HashMap<MethodSymbol, ControlFlowGraphNode> map = cfgGenerator.generateControlFlowGraphsForMethods();

//                    StringBuilder sb = new StringBuilder();
//                    for (MethodSymbol methodSymbol : map.keySet()) {
//                        ControlFlowGraphNode cfgNode = map.get(methodSymbol);
//                        DeadVariableEliminator.applyOprimizationComputationOnEdges(cfgNode);
//                        String str = "digraph " + methodSymbol.getSymbolName() + "_Before {\n";
//                        str += cfgNode.toString();
//                        str += "}\n\n";
//                        sb.append(str);
//                    }
//
//                    System.out.println( sb );



                    // Going to eliminate dead variable assignments
                    for (MethodSymbol methodSymbol : map.keySet()) {
                        ControlFlowGraphNode cfgNode = map.get(methodSymbol);
                        List<Statement> eliminatedStatements = DeadVariableEliminator.optimizeTheMethodDeclaration(methodSymbol.getMethodDeclaration(), cfgNode);

                    }

                    /**
                     * Here, you could used nameAnalyzer which is optimized now!
                     * **/



                    cfgGenerator = new ControlFlowGraphGenerator(nameAnalyzer);
                    map = cfgGenerator.generateControlFlowGraphsForMethods();
                    StringBuilder sb2 = new StringBuilder();
                    for (MethodSymbol methodSymbol : map.keySet()) {
                        ControlFlowGraphNode cfgNode = map.get(methodSymbol);
                        DeadVariableEliminator.applyOprimizationComputationOnEdges(cfgNode);
                        String str = "digraph " + methodSymbol.getSymbolName() + "_Optimized {\n";
                        str += cfgNode.toString();
                        str += "}\n\n";
                        sb2.append(str);
                    }

                    System.out.println( sb2 );

                }
            }
        }
    }


    private void optInfo( String[] filenames ) throws ParseException, FileNotFoundException {


        if (filenames.length == 1) {
            System.out.println("Please specify at least 1 input file.");
            System.exit(0);
        }

        for (int i = 1; i < filenames.length; i++) {

            if (checkFileValidity(filenames[i])) {

                File file = new File(filenames[i]);

                FileReader reader = new FileReader(file);

                Lexer lexer = new Lexer(reader);

                Parser parser = new Parser(lexer);

                Tree tree = parser.parseTree();

                if (tree != null) {


                    NameAnalyzer nameAnalyzer = new CompilerMain().analysis(filenames[i], false);


                    /**
                     *
                     * Here you need to generate byte codes once, So be able to compute the total number of the byte codes
                     * before applying optimization
                     */






                    // Going to apply optimization
                    ControlFlowGraphGenerator cfgGenerator = new ControlFlowGraphGenerator(nameAnalyzer);
                    HashMap<MethodSymbol, ControlFlowGraphNode> map = cfgGenerator.generateControlFlowGraphsForMethods();

                    for (MethodSymbol methodSymbol : map.keySet()) {
                        ControlFlowGraphNode cfgNode = map.get(methodSymbol);
                        DeadVariableEliminator.applyOprimizationComputationOnEdges(cfgNode);

                    }

                    // Going to eliminate dead variable assignments
                    for (MethodSymbol methodSymbol : map.keySet()) {
                        ControlFlowGraphNode cfgNode = map.get(methodSymbol);
                        DeadVariableEliminator.optimizeTheMethodDeclaration(methodSymbol.getMethodDeclaration(), cfgNode);
                    }





                    /**
                     * Here, we could used nameAnalyzer which is optimized now and generate the byte code which will be optimized.
                     * You need to compute the total number of byte codes as well and print a simple report as bellow:
                     * #bytecode before optimization: <number>
                     * #bytecode after optimization:  <number>
                     * **/


                }
            }
        }
    }

    private void cfgGenerator(String[] filenames) throws ParseException, FileNotFoundException {

        if (filenames.length == 1) {
            System.out.println("Please specify at least 1 input file.");
            System.exit(0);
        }

        for (int i = 1; i < filenames.length; i++) {

            if (checkFileValidity(filenames[i])) {

                File file = new File(filenames[i]);

                FileReader reader = new FileReader(file);

                Lexer lexer = new Lexer(reader);

                Parser parser = new Parser(lexer);

                Tree tree = parser.parseTree();

                if (tree != null) {


                    NameAnalyzer nameAnalyzer = new CompilerMain().analysis(filenames[i],false);

                    ControlFlowGraphGenerator cfgGenerator = new ControlFlowGraphGenerator(nameAnalyzer);
                    HashMap<MethodSymbol, ControlFlowGraphNode> map = cfgGenerator.generateControlFlowGraphsForMethods();
                    for( MethodSymbol methodSymbol : map.keySet() ) {
                        ControlFlowGraphNode cfgNode = map.get(methodSymbol);
                        String str = "digraph " + methodSymbol.getSymbolName() + " {\n";
                        str += cfgNode.toString();
                        str += "}\n\n";

                        FileWriter writer = null;
                        try {
                            writer = new FileWriter(filenames[i].substring(0, filenames[i].lastIndexOf(".")) +
                                    "_" + methodSymbol.getClassSymbol().getSymbolName() + "_" +
                                    methodSymbol.getMethodDeclaration().methodName.idName + ".dot");
                            writer.write(str);
                            writer.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }


                    if (filenames.length > 2) {
                        System.out.println();
                        System.out.println("###########################");
                        System.out.println();
                    }

                }

            }

        }

    }

    private boolean checkFileValidity(String file) {

        String fileExt = file.substring(file.lastIndexOf('.') + 1);

        String filePath = new File(file).getAbsolutePath();
        File inputFile = new File(filePath);

        if (fileExt.equals("emj") && inputFile.exists() && inputFile.canRead())
            return true;
        else {

            if (!fileExt.equals("emj")) {

                System.err.println("Invalid file extension.");
                System.exit(0);

            } else if (!inputFile.exists()) {

                System.err.println(inputFile + " - File not found!");
                System.exit(0);

            } else if (!inputFile.canRead()) {

                System.err.println("Unable to read file.");
                System.exit(0);

            }

        }

        return false;

    }

    private NameAnalyzer analysis(String filename, boolean onlyNameAnalysis) {

        try {

            File file = new File(filename);
            FileReader reader = new FileReader(file);

            Lexer lexer = new Lexer(reader);

            Parser parser = new Parser(lexer);

            Tree tree = parser.parseTree();

            if (tree != null) {

                NameAnalyzer nameAnalyzer = new NameAnalyzer((Program) tree, true);
                nameAnalyzer.nameAnalysis();

                List<AnalysisError> errors = nameAnalyzer.errorList;

                if (!onlyNameAnalysis && errors.isEmpty()) {

                    file = new File(filename);
                    reader = new FileReader(file);
                    lexer = new Lexer(reader);
                    parser = new Parser(lexer);

                    tree = parser.parseTree();

                    NameAnalyzer nameAndTypeAnalyzer = new NameAnalyzer((Program) tree, false);
                    nameAndTypeAnalyzer.nameAnalysis();
                    errors = nameAndTypeAnalyzer.errorList;
                    if (errors.size() == 0)
                        System.out.println("File :: " + filename + " --> Valid EMiniJava program.");

                    else {
                        for (AnalysisError error : errors) {
                            System.out.println(error.getError());
                        }

                    }

                } else {

                    if (errors.size() == 0)
                        System.out.println("File :: " + filename + " --> Valid EMiniJava program.");

                    else {

                        if (!onlyNameAnalysis)
                            System.out.println("There are some Naming errors as below. Please fix them first, then do the type analysis.");

                        for (AnalysisError error : errors) {
                            System.out.println(error.getError());
                        }

                    }

                }

                return nameAnalyzer;
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        return null;

    }

}
