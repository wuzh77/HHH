package cn.edu.nju.cs;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Method {
    public String iden = null;

    public List<Map<String, MiniJavaObject>> argList = null;
    public List<MiniJavaObject> argType = null;
    public MiniJavaParser.MethodDeclarationContext methodDeclarationContext = null;

    public Method() {}

    public void initMethod(MiniJavaParser.MethodDeclarationContext ctx) {
        this.iden = ctx.identifier().IDENTIFIER().getText();
        this.methodDeclarationContext = ctx;
    }

}
