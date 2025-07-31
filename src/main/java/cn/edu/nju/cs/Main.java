package cn.edu.nju.cs;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;

public class Main {
    public static void run(File mjFile) throws Exception {
        var input = CharStreams.fromFileName(mjFile.getAbsolutePath());
        MiniJavaLexer lexer = new MiniJavaLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        MiniJavaParser parser = new MiniJavaParser(tokenStream);
        ParseTree pt = parser.compilationUnit();
//        System.out.println(pt.toStringTree(parser));
        new MyTreeWalker().visit(pt);
    }


    public static void main(String[] args) throws Exception  {
        if(args.length!= 1) {
            System.err.println("Error: Only one argument is allowed: the path of MiniJava file.");
            throw new RuntimeException();
        }

        File mjFile = new File(args[0]);
        run(mjFile);
    }
}