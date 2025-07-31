package cn.edu.nju.cs;
import java.util.*;

public class Method {
    public String iden;

    public List<Map<String, MiniJavaObject>> argList = null;
    public List<MiniJavaObject> argType = null;
    public MiniJavaParser.MethodDeclarationContext methodDeclarationContext = null;
    public MiniJavaObject retVal = null;

    public Method(String iden, List<MiniJavaObject> argType, List<Map<String, MiniJavaObject>> argList,  MiniJavaParser.MethodDeclarationContext methodDeclarationContext) {
        this.iden = iden;
        this.argList = argList;
        this.argType = argType;
        this.methodDeclarationContext = methodDeclarationContext;
    }

    public void methodCopy(Method m) {
        this.iden = m.iden;
        this.argList = new ArrayList<>();
        for (int i = 0; i < m.argList.size(); i++) {
            for (Map.Entry<String, MiniJavaObject> entry : m.argList.get(i).entrySet()) {
                String key = entry.getKey();
                MiniJavaObject obj = entry.getValue();
                MiniJavaObject tmp = new MiniJavaObject(null, null);
                tmp.assign(obj);
                HashMap<String, MiniJavaObject> map = new HashMap<>();
                map.put(key, tmp);
                argList.add(map);
            }
        }

        this.argType = new ArrayList<>();
        for (int i = 0; i < m.argType.size(); i++) {
            MiniJavaObject obj = new MiniJavaObject(null, null);
            obj.assign(m.argType.get(i));
            this.argType.add(obj);
        }

        this.methodDeclarationContext = m.methodDeclarationContext;
    }

}
