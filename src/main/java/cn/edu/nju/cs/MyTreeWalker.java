package cn.edu.nju.cs;
import java.text.MessageFormat;
import java.util.*;


public class MyTreeWalker extends MiniJavaParserBaseVisitor<MiniJavaObject> {

    private List<Map<String, MiniJavaObject>> resultList = null;
    private Stack<List<Map<String, MiniJavaObject>>> methodStack = new Stack<>();
    private List<Method> methodsList = new ArrayList<>();
    private List<Map<String, MiniJavaObject>> CurArgList = null;
    private List<MiniJavaObject> CurArgType = null;

    MyTreeWalker() {}
    @Override
    public MiniJavaObject visitCompilationUnit(MiniJavaParser.CompilationUnitContext ctx) {
        for (MiniJavaParser.MethodDeclarationContext methodContext : ctx.methodDeclaration()) {
            CurArgList = new ArrayList<>();
            CurArgType = new ArrayList<>();
            visitFormalParameters(methodContext.formalParameters());
            String method_iden = methodContext.identifier().getText();
            Method method = new Method(method_iden, CurArgType, CurArgList, methodContext);
            methodsList.add(method);
        }

        for (Method method : methodsList) {
            if (Objects.equals(method.iden, "main")) {
                processMethod(method, new ArrayList<>());
                System.out.println("Process exits with the code " + method.retVal + ".");
                break;
            }
        }

        return null;
    }


    private MiniJavaObject processMethod(Method cur_method, List<MiniJavaObject> args) {
        for (int i = 0; i < args.size(); i++) {
            for (Map.Entry<String, MiniJavaObject> entry : cur_method.argList.get(i).entrySet()) {
                MiniJavaObject obj = entry.getValue();
                MiniJavaObject tmp = new MiniJavaObject(null, null);
                tmp.assign(args.get(i));
                obj.assign(tmp);
            }
        }


        List<Map<String, MiniJavaObject>> methodArgList = new ArrayList<>();
        for (int i = 0; i < cur_method.argList.size(); i++) {
            for (Map.Entry<String, MiniJavaObject> entry : cur_method.argList.get(i).entrySet()) {
                String key = entry.getKey();
                MiniJavaObject obj = entry.getValue();
                MiniJavaObject tmp = new MiniJavaObject(null, null);
                tmp.assign(obj);
                HashMap<String, MiniJavaObject> map = new HashMap<>();
                map.put(key, tmp);
                methodArgList.add(map);
            }
        }
        resultList = methodArgList;
        System.out.println(resultList);
        methodStack.push(methodArgList);
        MiniJavaObject res = visitMethodDeclaration(cur_method.methodDeclarationContext);
        cur_method.retVal = res;
        methodStack.pop();
        if (!methodStack.isEmpty()) {
            resultList = methodStack.peek();
        }

        return res;
    }

    @Override
    public MiniJavaObject visitMethodDeclaration(MiniJavaParser.MethodDeclarationContext ctx) {
        MiniJavaObject resObj = new MiniJavaObject(null, null);
        MiniJavaObject resType = null;
        if (ctx.typeType() != null) {
            resType = visitTypeType(ctx.typeType());
        } else {
            resType =  new MiniJavaObject(null, null);
        }
        resObj.type = resType.type;
        resObj.isArr = resType.isArr;
        String resIdentifier = ctx.identifier().IDENTIFIER().getText();
        MiniJavaObject blockres = visitBlock(ctx.block());
        resObj.value = blockres.value;
        resObj.isFunc = true;
        HashMap<String, MiniJavaObject> tmp = new HashMap<>();
        tmp.put(resIdentifier, resObj);
        resultList.add(tmp);
        return resObj;
    }

    @Override
    public MiniJavaObject visitArguments(MiniJavaParser.ArgumentsContext ctx) {
        if (ctx.expressionList() != null) {
            return visitExpressionList(ctx.expressionList());
        }
        return new MiniJavaObject(null, null);
    }

    @Override
    public MiniJavaObject visitMethodCall(MiniJavaParser.MethodCallContext ctx) {
        String iden = ctx.identifier().getText();
        MiniJavaObject arg = visitArguments(ctx.arguments());
        List<MiniJavaObject> argList = arg.value == null ? null : (List<MiniJavaObject>) arg.value;
        if (iden.equals("print")) {
            if (argList != null && argList.isEmpty()) {
                System.out.print("");
            } else {
                System.out.print(argList.getFirst().value);
            }
        } else if (iden.equals("println")) {
            if (argList == null) {
                System.out.print("\n");
            } else {
                if (argList.isEmpty()) {
                    System.out.print("");
                } else { // hello world!
                    System.out.println(argList.getFirst().value);
                }
            }
        } else if (iden.equals("assert")) {
            if (!argList.getFirst().type.equals("boolean")) {
                System.out.println("Process exits with the code 34.");
                System.exit(0);
            }

            if (!convert2Bool(argList.getFirst())) {
                System.out.println("Process exits with the code 33.");
                System.exit(0);
            }
        } else if (iden.equals("length")) {
            if (argList == null || argList.isEmpty()) {
                System.out.println("Process exits with the code 34.");
                System.exit(0);
            }
            if (!(argList.getFirst().isString() || argList.getFirst().isArr)) {
                System.out.println("Process exits with the code 34.");
                System.exit(0);
            }
            MiniJavaObject resObj = new MiniJavaObject(null, null);
            resObj.type = "int";
            if (argList.getFirst().isString() && !argList.getFirst().isArr) {
                resObj.value = ((String)argList.getFirst().value).length();
            } else {
                if (argList.getFirst().value == null) {
                    System.out.println("Process exits with the code 34.");
                    System.exit(0);
                }
                resObj.value = ((List<MiniJavaObject>)argList.getFirst().value).size();
            }
            return resObj;
        } else if (iden.equals("to_char_array")) {
            if (argList == null) {
                System.out.println("Process exits with the code 34.");
                System.exit(0);
            }
            if (!argList.getFirst().isString()) {
                System.out.println("Process exits with the code 34.");
                System.exit(0);
            }
            MiniJavaObject resObj = new MiniJavaObject(null, null);
            resObj.setArr();
            resObj.type = "char";
            String str = argList.getFirst().value.toString();
            List<MiniJavaObject> tmp = new ArrayList<>();
            for (int i=0; i<str.length(); i++) {
                MiniJavaObject elem = new MiniJavaObject(null, null);
                elem.value = str.charAt(i);
                elem.type = "char";
                tmp.add(elem);
            }
            resObj.value = tmp;
            return resObj;
        } else if (iden.equals("to_string")) {
            if (argList == null) {
                System.out.println("Process exits with the code 34.");
                System.exit(0);
            }
            if (!(argList.getFirst().isArr && argList.getFirst().isChar())) {
                System.out.println("Process exits with the code 34.");
                System.exit(0);
            }
            MiniJavaObject resObj = new MiniJavaObject(null, null);
            resObj.type = "string";
            List<MiniJavaObject> tmp = (List<MiniJavaObject>) argList.getFirst().value;
            if (tmp == null) {
                System.out.println("Process exits with the code 34.");
                System.exit(0);
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tmp.size(); i++) {
                sb.append(tmp.get(i).value);
            }
            resObj.value = sb.toString();
            return resObj;
        } else if (iden.equals("atoi")) {
            if (argList == null) {
                System.out.println("Process exits with the code 34.");
                System.exit(0);
            }
            if (!argList.getFirst().isString()) {
                System.out.println("Process exits with the code 34.");
                System.exit(0);
            }
            MiniJavaObject resObj = new MiniJavaObject(null, null);
            resObj.type = "int";
            String str_num = (String) argList.getFirst().value;
            int tmp = Integer.parseInt(str_num);
            resObj.value = tmp;
            return resObj;
        } else if (iden.equals("itoa")) {
            if (argList == null) {
                System.out.println("Process exits with the code 34.");
                System.exit(0);
            }
            if (!(argList.getFirst().isInt() || argList.getFirst().isChar())) {
                System.out.println("Process exits with the code 34.");
                System.exit(0);
            }

            MiniJavaObject resObj = new MiniJavaObject(null, null);
            resObj.type = "string";
            resObj.value = argList.getFirst().isChar() ? convert2Int(argList.getFirst()) : argList.getFirst().value.toString();
            return resObj;
        }
        MiniJavaObject resObj = null;
        for (Method method : methodsList) {
            if (Objects.equals(method.iden, iden)) {
                List<MiniJavaObject> tmp = (List<MiniJavaObject>) arg.value;
                if (tmp == null) {
                    if (method.argType.isEmpty()) {
                        processMethod(method, new ArrayList<>());
                    } else {
                        continue;
                    }
                } else if (tmp.size() != method.argType.size()) {
                    continue;
                }
                boolean flag = true;
                for (int i = 0; i < method.argType.size(); i++) {
                    if (method.argType.get(i).isArr && tmp.get(i) == null) {
                        continue;
                    }
                    if (!method.argType.get(i).type.equals(tmp.get(i).type)) {
                        flag = false;
                        break;
                    }
                }
                if (!flag) {
                    continue;
                }
                resObj = processMethod(method, (List<MiniJavaObject>) arg.value);
            }
        }
        return resObj;
    }



    @Override
    public MiniJavaObject visitFormalParameters(MiniJavaParser.FormalParametersContext ctx) {
        MiniJavaObject resFormal = new MiniJavaObject(null, null);
        if (ctx.formalParameterList() != null) {
            resFormal = visitFormalParameterList(ctx.formalParameterList());
        }
        return resFormal;
    }

    @Override
    public MiniJavaObject visitFormalParameterList(MiniJavaParser.FormalParameterListContext ctx) {
        int cnt = ctx.formalParameter().size();
        for (int i = 0; i < cnt; i++) {
            visitFormalParameter(ctx.formalParameter().get(i));
        }
        return null;
    }


    @Override
    public MiniJavaObject visitFormalParameter(MiniJavaParser.FormalParameterContext ctx) {
        MiniJavaObject resType = visitTypeType(ctx.typeType());
        String iden = ctx.identifier().IDENTIFIER().getText();
        Map<String, MiniJavaObject> tmp = new HashMap<>();
        tmp.put(iden, resType);
        CurArgList.add(tmp);
        MiniJavaObject argType = new MiniJavaObject(new String(resType.type), null);
        argType.assign(resType);
        CurArgType.add(argType);
        return null;
    }


    @Override
    public MiniJavaObject visitCreator(MiniJavaParser.CreatorContext ctx) {
        var type = visitCreatedName(ctx.createdName());
        var arrRest = visitArrayCreatorRest(ctx.arrayCreatorRest());
//        System.out.println("arr rest = " + arrRest);
        if (ctx.arrayCreatorRest().getChildCount() == 3 && ctx.arrayCreatorRest().arrayInitializer() == null) {
            initArr(type.type, arrRest);
        }

        return arrRest;
    }

    @Override
    public MiniJavaObject visitArrayCreatorRest(MiniJavaParser.ArrayCreatorRestContext ctx) {
        if (ctx.arrayInitializer() != null) {
            MiniJavaObject res = visitArrayInitializer(ctx.arrayInitializer());
            return res;
        } else {
            boolean initNull = ctx.getChildCount() % 3 == 0;
            var res = visitExpression(ctx.expression(0));
            int num = convert2Int(res);
            MiniJavaObject resObj = new MiniJavaObject(null, null);
            resObj.setArr();
            resObj.arrInitNull = initNull;
            List<MiniJavaObject> resList = new ArrayList<>();
            resObj.value = resList;
            if (ctx.getChildCount() > 3) {
                for (int i = 0; i < ctx.expression().size(); i++) {
                    MiniJavaObject arrNum = visitExpression(ctx.expression(i));
                    int intArrNum = convert2Int(arrNum);
                    if (i == 0) {
                        for (int j = 0; j < intArrNum; j++) {
                            MiniJavaObject arrEle = new MiniJavaObject(null, null);
                            if (ctx.expression().size() > 1) {
                                arrEle.setArr();
                                arrEle.arrInitNull = initNull;
                                arrEle.value = new ArrayList<>();
                            }
                            resList.add(arrEle);
                        }
                    } else {
                        initMultiDimensionArr(resObj, intArrNum, i == (ctx.expression().size() - 1), initNull);
                    }
                }
            } else {
                for (int i = 0; i < num; i++) {
                    MiniJavaObject obj = new MiniJavaObject(null, null);
                    resList.add(obj);
                }
            }
            return resObj;
        }
    }

    private void initMultiDimensionArr(MiniJavaObject arr, int elenum, boolean finalDimen, boolean initNull) {

        List<MiniJavaObject> objVal = (List<MiniJavaObject>) arr.value;
        if (!objVal.isEmpty()) {
            for (MiniJavaObject miniJavaObject : objVal) {
                initMultiDimensionArr(miniJavaObject, elenum, finalDimen, initNull);
            }
            return;
        }
        for (int i = 0; i < elenum; i++) {
            MiniJavaObject addEle = new MiniJavaObject(null, null);
            if (!finalDimen) {
                addEle.setArr();
                addEle.arrInitNull = initNull;
                addEle.value = new ArrayList<>();
            }
            objVal.add(addEle);
        }

    }
    @Override
    public MiniJavaObject visitCreatedName(MiniJavaParser.CreatedNameContext ctx) {
        return visitPrimitiveType(ctx.primitiveType());
    }

    @Override
    public MiniJavaObject visitBlock(MiniJavaParser.BlockContext ctx) {
        int cnt = ctx.getChildCount() - 2;
        MiniJavaObject res = new MiniJavaObject(null, null);
        res.setArr();
        int start_index = resultList.size();
        for (int i = 0; i < cnt; i++) {
            MiniJavaObject obj = visitBlockStatement(ctx.blockStatement().get(i));

            if (obj != null && obj.isBreak) {
                res.isBreak = true;
                break;
            }
            if (obj != null && obj.isContinue) {
                res.isContinue = true;
                break;
            }
            if (obj != null && obj.isReturn) {
                res.assign(obj);
                res.isReturn = true;
                break;
            }
        }
        int end_index = resultList.size();
        if (start_index != end_index) {
            resultList.subList(start_index, resultList.size()).clear();
        }
        return res;
    }

    @Override
    public MiniJavaObject visitBlockStatement(MiniJavaParser.BlockStatementContext ctx) {
        MiniJavaObject res = null;
        if (ctx.localVariableDeclaration() != null) {
            res = visitLocalVariableDeclaration(ctx.localVariableDeclaration());
        }
        if (ctx.statement() != null) {
            res = visitStatement(ctx.statement());
        }
        return res;
    }


    @Override
    public MiniJavaObject visitVariableDeclarator(MiniJavaParser.VariableDeclaratorContext ctx) {
        var resVariable = new MiniJavaObject(null, null);
        if (ctx.variableInitializer() != null) {
            resVariable = visitVariableInitializer(ctx.variableInitializer());
        }
        return resVariable;
    }
    @Override
    public MiniJavaObject visitVariableInitializer(MiniJavaParser.VariableInitializerContext ctx) {
        if (ctx.expression() != null) {
            return visitExpression(ctx.expression());
        } else {
            MiniJavaObject res = visitArrayInitializer(ctx.arrayInitializer());
            res.isArr = true;
            return res;
        }
    }

    @Override
    public MiniJavaObject visitArrayInitializer(MiniJavaParser.ArrayInitializerContext ctx) {
        if (ctx.variableInitializer().getFirst().expression() != null) {
            int cnt = ctx.variableInitializer().size();
            List<MiniJavaObject> MiniJavalist = new ArrayList<>();
            String elementType = null;
            for (int i = 0; i < cnt; i++) {
                MiniJavaObject element = visitExpression(ctx.variableInitializer().get(i).expression());

                if (element != null) {
                    elementType = element.type;
                }
                MiniJavalist.add(element);
            }
            MiniJavaObject res = new MiniJavaObject(elementType, MiniJavalist);
            res.setArr();
            return res;
        } else {
            List<MiniJavaObject> MiniJavalist = new ArrayList<>();
            int cnt = ctx.variableInitializer().size();
            String elementType = null;
            for (int i = 0; i < cnt; i++) {
                MiniJavaObject element = visitVariableInitializer(ctx.variableInitializer().get(i));
                MiniJavalist.add(element);
                if (element != null) {
                    elementType = element.type;
                }
            }

            MiniJavaObject res = new MiniJavaObject(elementType, MiniJavalist);
            res.setArr();
            return res;
        }

    }

    @Override
    public MiniJavaObject visitLocalVariableDeclaration(MiniJavaParser.LocalVariableDeclarationContext ctx) {
        if (ctx.getChildCount() == 2) {
            var defaultType = visitTypeType(ctx.typeType());
            var varDec = visitVariableDeclarator(ctx.variableDeclarator());
            String resIden = ctx.variableDeclarator().identifier().getText();
            MiniJavaObject res = new MiniJavaObject("", null);
            assignMiniJavaObject(defaultType, varDec, res);
            HashMap<String, MiniJavaObject> tmp = new HashMap<>();
            tmp.put(resIden, res);
            resultList.add(tmp);
            return res;
        } else {
            String identifier = ctx.identifier().getText();
            MiniJavaObject type = new MiniJavaObject("var", null);
            MiniJavaObject value = visitExpression(ctx.expression());
            MiniJavaObject assignmentResult = new MiniJavaObject(null, null);
            assignMiniJavaObject(type, value, assignmentResult);
            HashMap<String, MiniJavaObject> tmp = new HashMap<>();
            tmp.put(identifier, assignmentResult);
            resultList.add(tmp);
            return assignmentResult;
        }
    }

    @Override
    public MiniJavaObject visitStatement(MiniJavaParser.StatementContext ctx)  {
        if (ctx.RETURN() != null) {
            MiniJavaObject res = new MiniJavaObject(null, null);
            res.isReturn = true;
            if (ctx.expression() == null) {
                return res;
            } else {
                MiniJavaObject exObj = visitExpression(ctx.expression());
                res.value = exObj.value;
                return res;
            }
        } else if (ctx.IF() != null) {
            MiniJavaObject parExp = visitParExpression(ctx.parExpression());
            if ((boolean)parExp.value) {
                return visitStatement(ctx.statement().getFirst());
            } else {
                if (ctx.statement().size() > 1) {
                    return visitStatement(ctx.statement().get(1));
                }
            }
        } else if (ctx.FOR() != null) {
            String localDefine = null;
            if (ctx.forControl().forInit() != null) {
                visitForInit(ctx.forControl().forInit());
                if (ctx.forControl().forInit().localVariableDeclaration() != null) {
                    Map<String, MiniJavaObject> lastMap = resultList.getLast();
                    for (Map.Entry<String, MiniJavaObject> entry : lastMap.entrySet()) {
                        localDefine = entry.getKey();
                    }
                }
            }
            MiniJavaObject forCon = null;
            if (ctx.forControl().expression() != null) {
                forCon = visitExpression(ctx.forControl().expression());
            } else {
                forCon = new MiniJavaObject("boolean", true);
            }
            boolean flag = (boolean) forCon.value;
            while (flag) {
                MiniJavaObject resState = visitStatement(ctx.statement().getFirst());
                if (resState != null && resState.isBreak) {
                    break;
                } else if (resState != null && resState.isContinue) {
                    visitExpressionList(ctx.forControl().expressionList());
                    if (ctx.forControl().expression() != null) {
                        forCon = visitExpression(ctx.forControl().expression());
                    } else {
                        forCon.value = true;
                    }
                    flag = (boolean) forCon.value;
                    continue;
                }
                visitExpressionList(ctx.forControl().expressionList());
                if (ctx.forControl().expression() != null) {
                    forCon = visitExpression(ctx.forControl().expression());
                } else {
                    forCon.value = true;
                }
                flag = (boolean) forCon.value;
            }
            if (localDefine != null) {
                delEle(localDefine);
            }
        } else if (ctx.getChildCount() == 2 && ctx.expression() != null) {
            return visitExpression(ctx.expression());
        } else if (ctx.block() != null) {
            MiniJavaObject res = visitBlock(ctx.block());
            return res;
        } else if (ctx.RETURN() != null) {
            MiniJavaObject resExp = visitExpression(ctx.expression());
            resExp.isReturn = true;
            return resExp;
        } else if (ctx.BREAK() != null) {
            MiniJavaObject resBreak = new MiniJavaObject(null, null);
            resBreak.isBreak = true;
            return resBreak;
        } else if (ctx.CONTINUE() != null) {
            MiniJavaObject resContinue = new MiniJavaObject(null, null);
            resContinue.isContinue = true;
            return resContinue;
        } else if (ctx.WHILE() != null) {
            MiniJavaObject parExp = visitParExpression(ctx.parExpression());
            boolean whileFlag = convert2Bool(parExp);

            while (whileFlag) {
                MiniJavaObject resState = visitStatement(ctx.statement().getFirst());
                if (resState != null && resState.isBreak) {
                    break;
                }
                parExp = visitParExpression(ctx.parExpression());
                whileFlag = convert2Bool(parExp);
            }
        }

        return null;
    }


    @Override
    public MiniJavaObject visitForInit(MiniJavaParser.ForInitContext ctx) { return visitChildren(ctx); }


    @Override
    public MiniJavaObject visitExpressionList(MiniJavaParser.ExpressionListContext ctx) {
        MiniJavaObject resExp = new MiniJavaObject(null, null);
        List<MiniJavaObject> save_res = new ArrayList<>();
        int cnt = ctx.expression().size();
        for (int i = 0; i < cnt; i++) {
            MiniJavaObject tmp = visitExpression(ctx.expression().get(i));
            save_res.add(tmp);
        }
        resExp.value = save_res;
        return resExp;
    }


    @Override
    public MiniJavaObject visitForControl(MiniJavaParser.ForControlContext ctx) {
        MiniJavaObject res = new MiniJavaObject("boolean", false);
        if (ctx.forInit() != null) {
            visitForInit(ctx.forInit());
        }
        if (ctx.expression() != null) {
            var resExp = visitExpression(ctx.expression());
            if (resExp != null) {
                res.value = resExp.value;
            }
        }
        if (ctx.expressionList() != null) {
            visitExpressionList(ctx.expressionList());
        }
        return res;
    }

    @Override
    public MiniJavaObject visitParExpression(MiniJavaParser.ParExpressionContext ctx) {
        MiniJavaObject exp = visitExpression(ctx.expression());
        MiniJavaObject res = new MiniJavaObject("boolean", false);
        if (exp == null || exp.value == null) {
            return res;
        }
        if (exp.isInt()) {
            res.value = convert2Int(exp) != 0;
            return res;
        }
        res.value = exp.value;
        return res;
    }

    @Override
    public MiniJavaObject visitExpression(MiniJavaParser.ExpressionContext ctx) {
        MiniJavaObject result = null;
        if (ctx.primary() != null) {
            result = visitPrimary(ctx.primary());
        } else if (ctx.postfix != null) {
            result = visitPostFix(ctx);
        } else if (ctx.prefix != null) {
            result = visitPreFix(ctx);
        } else if (ctx.getChildCount() == 4 && ctx.typeType() != null) {
            result = typeCasting(ctx);
        } else if (ctx.bop != null) {
            result = visitBopExp(ctx);
        } else if (ctx.typeType() != null) {
            result = visitTypeType(ctx.typeType());
        } else if (ctx.NEW() != null) {
            result = visitCreator(ctx.creator());
        } else if (ctx.getChildCount() == 4 && ctx.expression().size() == 2) {
            List<MiniJavaObject> indexList = new ArrayList<>();
            MiniJavaObject finalIndex = visitExpression(ctx.expression().get(1));
            var exp = ctx.expression().get(0);
            indexList.add(finalIndex);
            String iden = null;
            MiniJavaObject arr = null;
            if (exp.primary() == null) {
                while (true) {
                    if (exp.expression().size() > 1) {
                        MiniJavaObject backIndex = visitExpression(exp.expression().get(1));
                        indexList.add(backIndex);
                        exp = exp.expression().get(0);
                    } else {
                        if (exp.primary() == null) {
                            iden = null;
                        } else {
                            iden = exp.primary().identifier().getText();
                        }
                        arr = visitExpression(exp);
                        break;
                    }
                }
            } else {
                arr = visitExpression(exp);
                if (exp.primary() == null) {
                    iden = null;
                } else {
                    iden = exp.primary().identifier().getText();
                }
            }
            Collections.reverse(indexList);
//            System.out.println(indexList);
            List<Integer> indexlist_ = convert2IntList(indexList);
            if (!arr.isArr) {
                System.out.println("Process exits with the code 34.");
                System.exit(0);
            }
            if (arr.value == null) {
                System.out.println("Process exits with the code 34.");
                System.exit(0);
            }
            MiniJavaObject findObj = null;
            List<MiniJavaObject> tmp = (List<MiniJavaObject>) arr.value;
            if (indexlist_.size() > 1) {
                for (int i = 0; i < indexlist_.size(); i++) {
                    if (i != indexlist_.size() - 1) {
                        if (indexlist_.get(i) >= tmp.size()) {
                            System.out.println("Process exits with the code 34.");
                            System.exit(0);
                        }
                        tmp = (List<MiniJavaObject>) tmp.get(indexlist_.get(i)).value;
                    } else {
                        if (indexlist_.get(i) >= tmp.size()) {
                            System.out.println("Process exits with the code 34.");
                            System.exit(0);
                        }
                        findObj = tmp.get(indexlist_.get(i));
                    }
                }
            } else {
                if (indexlist_.get(0) >= tmp.size()) {
                    System.out.println("Process exits with the code 34.");
                    System.exit(0);
                }
                findObj = tmp.get(indexlist_.get(0));
            }
            MiniJavaObject res = new MiniJavaObject(null,null);

            res.assign(findObj);
            res.arrName = iden;
            res.arrIndex = indexlist_;
            return res;
        } else if (ctx.methodCall() != null) {
            result = visitMethodCall(ctx.methodCall());
        }
        return result;
    }

    @Override
    public MiniJavaObject visitPrimary(MiniJavaParser.PrimaryContext ctx) {
        MiniJavaObject result = null;
        if (ctx.literal() != null) {
            result = visitLiteral(ctx.literal());
        } else if (ctx.identifier() != null) {
            result = visitIdentifier(ctx.identifier());
        } else if (ctx.getChildCount() == 3){
            result = visitExpression(ctx.expression());
        }
        return result;
    }

    @Override
    public MiniJavaObject visitLiteral(MiniJavaParser.LiteralContext ctx) {
        if (ctx.DECIMAL_LITERAL() != null) {
            return new MiniJavaObject("int", ctx.DECIMAL_LITERAL().getText());
        } else if (ctx.BOOL_LITERAL() != null) {
            return new MiniJavaObject("boolean", ctx.BOOL_LITERAL().getText());
        } else if (ctx.CHAR_LITERAL() != null) {
            String str = ctx.CHAR_LITERAL().getText().substring(1, ctx.CHAR_LITERAL().getText().length() - 1);
            return new MiniJavaObject("char", str.charAt(0));
        } else if (ctx.STRING_LITERAL() != null) {
            return new MiniJavaObject("string", ctx.STRING_LITERAL().getText().substring(1, ctx.STRING_LITERAL().getText().length() - 1));
        }
        return null;
    }

    @Override
    public MiniJavaObject visitIdentifier(MiniJavaParser.IdentifierContext ctx) {
        String identifierName = ctx.IDENTIFIER().getText();
        var tmp = findIdentifierObject(identifierName, resultList);
        if (tmp != null) {
            tmp.arrName = identifierName;
            return tmp;
        } else {
            MiniJavaObject res = new MiniJavaObject("func", null);
            HashMap<String, MiniJavaObject> tmp1 = new HashMap<>();
            tmp1.put(identifierName, res);
            resultList.add(tmp1);
            return new MiniJavaObject("func", null);
        }
    }

    @Override
    public MiniJavaObject visitPrimitiveType(MiniJavaParser.PrimitiveTypeContext ctx) {
        return new MiniJavaObject(ctx.getText(), Objects.equals(ctx.getText(), "int") ? 0 : null);
    }

    @Override
    public MiniJavaObject visitTypeType(MiniJavaParser.TypeTypeContext ctx) {
        MiniJavaObject res = visitPrimitiveType(ctx.primitiveType());
        if (ctx.getChildCount() != 1) {
            res.setArr();
            return res;
        }
        return res;
    }

    private void assignMiniJavaObject(MiniJavaObject left, MiniJavaObject right, MiniJavaObject  result) {
        if (right == null && left.isArr) {
            result.isArr = true;
            result.type = left.type;
            result.value = null;
            return;
        }
        if (!left.isVAR() && right.type != null && !Objects.equals(left.type, right.type) && !judgeIntChar(left, right)) {
            System.out.println("Process exits with the code 34.");
            System.exit(0);
        }
        if (left.isVAR() && right == null) {
            System.out.println("Process exits with the code 34.");
            System.exit(0);
        }
        if (!left.isVAR() && right.isTypeCasting && !Objects.equals(left.type, right.type)) {
            System.out.println("Process exits with the code 34.");
            System.exit(0);
        }

        if (right.arrName != null && (right.isInt() && left.isChar())) {
            System.out.println("Process exits with the code 34.");
            System.exit(0);
        }

        String type = left.isVAR()? right.type: left.type;
        result.type = type;
        result.value = right.value;
        result.isArr = right.isArr;
        if (right.isArr) {
            List<MiniJavaObject> MiniJavalist = (List<MiniJavaObject>) result.value;
            result.value = _initArr(MiniJavalist, left.isVAR()? right.type: left.type, right.arrInitNull);
        }
    }

    private List<MiniJavaObject> _initArr(List<MiniJavaObject> arr, String type, boolean initNull) {
        if (arr == null || arr.isEmpty()) {
            return new ArrayList<>();
        }
        if (arr.getFirst() == null && !initNull) {
            return arr;
        }
        if (arr.getFirst() != null && arr.getFirst().isArr) {
            List<MiniJavaObject> res = new ArrayList<>();
            for (int i=0; i<arr.size(); i++) {
                List<MiniJavaObject> tmp = null;
                if (arr.get(i) != null) {
                    tmp = _initArr((List<MiniJavaObject>)arr.get(i).value, type, initNull);
                }
                MiniJavaObject objRes = new MiniJavaObject(type, tmp);
                objRes.isArr = true;
                res.add(objRes);
            }
            return res;
        }
        List<MiniJavaObject> res = new ArrayList<>();
        if (arr.getFirst().value != null) {
            for (int i = 0; i < arr.size(); i++) {
                MiniJavaObject tmp = null;
                if (type.equals("int")) {
                    tmp = new MiniJavaObject("int", convert2Int(arr.get(i)));
                } else if (type.equals("char")) {
                    if (arr.get(i).arrName != null && !arr.get(i).isChar()) {
                        System.out.println("Process exits with the code 34.");
                        System.exit(0);
                    }
                    char val = convert2Char(arr.get(i));
                    tmp = new MiniJavaObject("char", val);
                } else if (type.equals("boolean")) {
                    tmp = new MiniJavaObject("boolean", arr.get(i).value);
                } else {
                    tmp = new MiniJavaObject("string", arr.get(i).value);
                }
                res.add(tmp);
            }
        } else {
            for (int i = 0; i < arr.size(); i++) {
                MiniJavaObject tmp = new MiniJavaObject(type, null);
                switch (type) {
                    case "int": tmp.value = 0; break;
                    case "char": tmp.value = (char) 0; break;
                    case "boolean": tmp.value = false; break;
                    case "string": tmp.value = ""; break;
                }
                res.add(tmp);
            }
        }
        return res;
    }

    private boolean judgeIntChar(MiniJavaObject left, MiniJavaObject right) {
        return (left.isInt() && right.isChar()) || (left.isChar() && right.isInt());
    }
    private MiniJavaObject findIdentifierObject(String identifier, List<Map<String, MiniJavaObject>> symbolList) {
        for (Map<String, MiniJavaObject> tmp : symbolList) {
            if (tmp.containsKey(identifier)) {
                return tmp.get(identifier);
            }
        }
        return null;
    }

    private MiniJavaObject visitPostFix(MiniJavaParser.ExpressionContext ctx) {
        MiniJavaObject tmp = visitExpression(ctx.expression().getFirst());
        int tmpvalue = convert2Int(tmp);
        int original = tmpvalue;
        String iden = null;
        List<Integer> index = null;
        if (ctx.expression().getFirst().primary() != null) {
            iden = ctx.expression(0).primary().identifier().getText();
        } else {
            iden = tmp.arrName;
            index = tmp.arrIndex;
        }

        if (MiniJavaParser.INC == ctx.postfix.getType()) {
            tmpvalue++;
        } else {
            tmpvalue--;
        }
        for (Map<String, MiniJavaObject> map : resultList) {
            if (map.containsKey(iden)) {

                MiniJavaObject obj = map.get(iden);
                if (!obj.isArr) {
                    obj.value = tmp.isChar()? (char)tmpvalue : tmpvalue;
                } else {
                    List<MiniJavaObject> tmpArr = (List<MiniJavaObject>) obj.value;

                    if (index.size() == 1) {
                        int resIndex = index.get(0);
                        MiniJavaObject objRes = null;
                        if (tmpArr.get(resIndex).isChar()) {
                            objRes = new MiniJavaObject("char", (char) tmpvalue);
                        } else {
                            objRes = new MiniJavaObject("int", tmpvalue);
                        }
                        tmpArr.set(resIndex, objRes);
                    } else {
                        for (int i=0; i<index.size(); i++) {
                            if (i != index.size() - 1) {
                                tmpArr = (List<MiniJavaObject>) tmpArr.get(index.get(i)).value;
                            } else {
                                MiniJavaObject objRes = null;
                                if (tmpArr.get(index.get(i)).isChar()) {
                                    objRes = new MiniJavaObject("char", (char) tmpvalue);
                                } else {
                                    objRes = new MiniJavaObject("int", tmpvalue);
                                }
                                tmpArr.set(index.get(i), objRes);
                            }
                        }
                    }
                }

            }
        }
        if (tmp.isChar()) {
            return new MiniJavaObject("char", (char) original);
        } else {
            return new MiniJavaObject("int", original);
        }
    }

    private MiniJavaObject visitBopExp(MiniJavaParser.ExpressionContext ctx) {
        if (ctx.bop.getType() == MiniJavaParser.ADD || ctx.bop.getType() == MiniJavaParser.SUB || ctx.bop.getType() == MiniJavaParser.MUL || ctx.bop.getType() == MiniJavaParser.DIV || ctx.bop.getType() == MiniJavaParser.MOD ||
                ctx.bop.getType() ==MiniJavaParser.BITAND || ctx.bop.getType() == MiniJavaParser.BITOR || ctx.bop.getType() == MiniJavaParser.CARET || ctx.bop.getType() == MiniJavaParser.LSHIFT ||
                ctx.bop.getType() == MiniJavaParser.RSHIFT || ctx.bop.getType() == MiniJavaParser.URSHIFT
        ) {
            // +, -, *, /, %, &, |, ^, <<, >>, >>>
            MiniJavaObject Op_left  = visit(ctx.expression(0));
            MiniJavaObject Op_right = visit(ctx.expression(1));

            if (ctx.bop.getType() == MiniJavaParser.ADD) {
                if ((Op_left.isInt() &&  Op_right.isInt()) || (Op_left.isChar() ||  Op_right.isChar())) {
                    int leftnum = convert2Int(Op_left);
                    int rightnum = convert2Int(Op_right);
                    int res = leftnum + rightnum;
                    MiniJavaObject resObj = new MiniJavaObject("int", res);
                    resObj.isTypeCasting = true;
                    return resObj;
                } else if (Op_left.isString()) {
                    String left = Op_left.value.toString();
                    String res = left + Op_right.value.toString();
                    return new MiniJavaObject("string", res);
                } else if (Op_right.isString()) {
                    String right = (String) Op_right.value;
                    String res = Op_left.value.toString() + right;
                    return new MiniJavaObject("string", res);
                }
            }

            Integer res = null;
            int leftnum = convert2Int(Op_left);
            int rightnum = convert2Int(Op_right);
            switch (ctx.bop.getType()) {
                case MiniJavaParser.SUB: res = leftnum -  rightnum; break;
                case MiniJavaParser.MUL: res = leftnum * rightnum; break;
                case MiniJavaParser.DIV: res = leftnum / rightnum; break;
                case MiniJavaParser.MOD: res = leftnum % rightnum; break;
                case MiniJavaParser.BITAND: res = leftnum & rightnum; break;
                case MiniJavaParser.BITOR: res = leftnum | rightnum; break;
                case MiniJavaParser.CARET: res = leftnum ^ rightnum; break;
                case MiniJavaParser.LSHIFT: res = leftnum << rightnum; break;
                case MiniJavaParser.RSHIFT: res = leftnum >> rightnum; break;
                case MiniJavaParser.URSHIFT: res = leftnum >>> rightnum; break;
            }
            return  new MiniJavaObject("int", res);
        } else if (ctx.bop.getType() == MiniJavaParser.ASSIGN) {
            MiniJavaObject left = visit(ctx.expression(0));
            MiniJavaObject right = visit(ctx.expression(1));
            MiniJavaObject res = new MiniJavaObject(null, null);

            assignMiniJavaObject(left, right, res);
            String identifier = left.arrName;
            modifyValue(identifier, res, left.arrIndex);
            return res;
        } else if (ctx.bop.getType() == MiniJavaParser.LE || ctx.bop.getType() == MiniJavaParser.GE || ctx.bop.getType() == MiniJavaParser.GT || ctx.bop.getType() == MiniJavaParser.LT) {
            MiniJavaObject left  = visit(ctx.expression(0));
            MiniJavaObject right = visit(ctx.expression(1));
            boolean res = false;
            int leftnum = convert2Int(left);
            int rightnum = convert2Int(right);
            switch (ctx.bop.getType()) {
                case MiniJavaParser.LE: res = leftnum <= rightnum; break;
                case MiniJavaParser.GE: res = leftnum >= rightnum; break;
                case MiniJavaParser.GT: res = leftnum > rightnum; break;
                case MiniJavaParser.LT: res = leftnum < rightnum; break;
            }
            return new MiniJavaObject("boolean", res);
        } else if (ctx.bop.getType() == MiniJavaParser.EQUAL || ctx.bop.getType() == MiniJavaParser.NOTEQUAL) {
            MiniJavaObject left = visitExpression(ctx.expression(0));
            MiniJavaObject right = visitExpression(ctx.expression(1));
            boolean res = false;
            if (left == null) {
                if (right == null) {
                    res = true;
                } else {
                    if (right.isArr) {
                        res = right.value == null;
                    }
                }
            } else if (right == null) {
                if (left.isArr) {
                    res = left.value == null;
                }
            } else if (left.isArr && right.isArr) {
                res = false;
            } else if (left.isArr || right.isArr) {
                System.out.println("Process exits with the code 34.");
                System.exit(0);
            } else {

                if ((left.isChar() && right.isInt()) || (left.isInt() && right.isChar())) {
                    int leftnum = convert2Int(left);
                    int rightnum = convert2Int(right);
                    switch (ctx.bop.getType()) {
                        case MiniJavaParser.EQUAL:  // 95
                            res = leftnum == rightnum;
                            break;
                        case MiniJavaParser.NOTEQUAL:   // 98
                            res = ! (leftnum == rightnum);
                            break;
                    }

                } else if (left.isInt() && right.isInt()) {
                    int leftnum = convert2Int(left);
                    int rightnum = convert2Int(right);
                    switch (ctx.bop.getType()) {
                        case MiniJavaParser.EQUAL:
                            res = leftnum == rightnum;
                            break;
                        case MiniJavaParser.NOTEQUAL:
                            res = ! (leftnum == rightnum);
                            break;
                    }
                } else {
                    if (!left.type.equals(right.type)) {
                        System.out.println("Process exits with the code 34.");
                        System.exit(0);
                    }
                    switch (ctx.bop.getType()) {
                        case MiniJavaParser.EQUAL:
                            res = left.value.equals(right.value);
                            break;
                        case MiniJavaParser.NOTEQUAL:
                            res = !left.value.equals(right.value);
                            break;
                    }
                }
            }
            return new MiniJavaObject("boolean", res);
        } else if (ctx.bop.getType() == MiniJavaParser.AND || ctx.bop.getType() == MiniJavaParser.OR) {
            MiniJavaObject left = visit(ctx.expression(0));
            if (!convert2Bool(left) && ctx.bop.getType() == MiniJavaParser.AND) {
                return new MiniJavaObject("boolean", false);
            }
            MiniJavaObject right = visit(ctx.expression(1));

            if (!left.isBoolean() || !right.isBoolean()) {
                System.out.println("Process exits with the code 34.");
                System.exit(0);
            }

            boolean res = false;
            switch (ctx.bop.getType()) {
                case MiniJavaParser.AND: res = convert2Bool(left) && convert2Bool(right); break;
                case MiniJavaParser.OR: res = convert2Bool(left) || convert2Bool(right); break;
            }
//            if (ctx.bop.getType() == MiniJavaParser.OR) {
//                System.out.println(" res :" + res);
//            }
            return new MiniJavaObject("boolean", res);
        } else if (ctx.bop.getType() == MiniJavaParser.ADD_ASSIGN) {
            MiniJavaObject left = visit(ctx.expression(0));
            MiniJavaObject right = visit(ctx.expression(1));

            if (left.isInt() && right.isInt()) {
                int leftnum = convert2Int(left);
                int rightnum = convert2Int(right);

                int res = leftnum + rightnum;
                MiniJavaObject assign_res = new MiniJavaObject("int", res);
                modifyValue(left.arrName,  assign_res, left.arrIndex);
                return assign_res;
            }

            String res = left.value.toString() + right.value.toString();
            return new MiniJavaObject("string", res);
        } else if (
                ctx.bop.getType() == MiniJavaParser.SUB_ASSIGN ||
                        ctx.bop.getType() == MiniJavaParser.MUL_ASSIGN ||
                        ctx.bop.getType() == MiniJavaParser.DIV_ASSIGN ||
                        ctx.bop.getType() == MiniJavaParser.MOD_ASSIGN ||
                        ctx.bop.getType() == MiniJavaParser.AND_ASSIGN ||
                        ctx.bop.getType() == MiniJavaParser.OR_ASSIGN  ||
                        ctx.bop.getType() == MiniJavaParser.XOR_ASSIGN ||
                        ctx.bop.getType() == MiniJavaParser.LSHIFT_ASSIGN ||
                        ctx.bop.getType() == MiniJavaParser.RSHIFT_ASSIGN ||
                        ctx.bop.getType() == MiniJavaParser.URSHIFT_ASSIGN
        ) {
            MiniJavaObject left  = visit(ctx.expression(0));
            MiniJavaObject right = visit(ctx.expression(1));

            int res = 0;
            int leftnum = convert2Int(left);
            int rightnum = convert2Int(right);

            switch (ctx.bop.getType()) {
                case MiniJavaParser.SUB_ASSIGN:  res = leftnum - rightnum; break;
                case MiniJavaParser.MUL_ASSIGN: res = leftnum * rightnum; break;
                case MiniJavaParser.DIV_ASSIGN:  res = leftnum / rightnum; break;
                case MiniJavaParser.MOD_ASSIGN: res = leftnum % rightnum; break;
                case MiniJavaParser.AND_ASSIGN:  res = leftnum & rightnum; break;
                case MiniJavaParser.OR_ASSIGN:  res = leftnum | rightnum; break;
                case MiniJavaParser.XOR_ASSIGN: res = leftnum ^ rightnum; break;
                case MiniJavaParser.LSHIFT_ASSIGN: res = leftnum << rightnum; break;
                case MiniJavaParser.RSHIFT_ASSIGN: res = leftnum >> rightnum; break;
                case MiniJavaParser.URSHIFT_ASSIGN: res = leftnum >>> rightnum; break;
            }

            return new MiniJavaObject("int", res);
        } else if(ctx.bop.getType() == MiniJavaParser.QUESTION && ctx.getChildCount() == 5) {
            MiniJavaObject cond =  visit(ctx.expression(0));

            if (!cond.isBoolean()) {
                System.out.println("Process exits with the code 34.");
                System.exit(0);
            }

            if (convert2Bool(cond)) {
                return visit(ctx.expression(1));
            } else {
                return visit(ctx.expression(2));
            }
        } else {
            System.out.println("Process exits with the code 34.");
            System.exit(0);
            return null;
        }
    }

    public MiniJavaObject visitPreFix(MiniJavaParser.ExpressionContext ctx) {
        MiniJavaObject tmp = visit(ctx.expression(0));
        MiniJavaObject res = null;
        if (!tmp.isBoolean()) {
            int tmpvalue = convert2Int(tmp);
            if (MiniJavaParser.INC == ctx.prefix.getType()) {
                tmpvalue++;
            } else if (MiniJavaParser.DEC == ctx.prefix.getType()) {
                tmpvalue--;
            } else if (ctx.ADD() != null) {
                tmpvalue = +tmpvalue;
            } else if (ctx.SUB() != null) {
                tmpvalue = -tmpvalue;
            }
            res = new MiniJavaObject("int", tmpvalue);
        } else {
            boolean tmpvalue = convert2Bool(tmp);
            tmpvalue = !tmpvalue;
            res = new MiniJavaObject("boolean", tmpvalue);
        }
        return res;
    }

    public MiniJavaObject typeCasting(MiniJavaParser.ExpressionContext ctx) {
        var exp      = visit(ctx.expression(0));
        var type = visitTypeType(ctx.typeType());
        MiniJavaObject res = null;
        if (type.type.equals(exp.type)) {
            res = new MiniJavaObject(exp.type, exp.value);
        }
//        System.out.println("type :" + type.type + " exp :" + exp);
        if (judgeIntChar(type, exp)) {
            if (type.isInt()) {
                String tmp = exp.value.toString();
                int tmpInt = tmp.charAt(0);
                res = new MiniJavaObject(type.type, String.valueOf(tmpInt));
            } else {
                int tmpInt = convert2Int(exp);
                char tmpChar = (char) tmpInt;
                res = new MiniJavaObject(type.type, String.valueOf(tmpChar));
            }
        }
        res.isTypeCasting = true;
        return res;
    }

    public int convert2Int(MiniJavaObject obj) {
        Integer res = null;
        if (obj.value instanceof Integer) {
            res = (Integer) obj.value;
            return res;
        } else if (obj.isChar()) {
            if (obj.value instanceof String) {
                return (int)obj.value.toString().charAt(0);
            } else {
                return (int) ((Character)obj.value);
            }
        } else {
            String tmp = (String)  obj.value;
            int tmpInt = Integer.parseInt(tmp);
            return tmpInt;
        }
    }


    private boolean convert2Bool(MiniJavaObject inputObj) {
        if (inputObj.isBoolean()) {
            return inputObj.value.toString().equals("true");
        } else if (inputObj.isInt()) {
            return inputObj.value.toString().equals("1");
        } else {
            return inputObj.value.toString().equals("true");
        }
    }
    public static char convert2Char(MiniJavaObject inputobj) {
        Object obj = inputobj.value;
        if (obj == null) {
            throw new IllegalArgumentException("输入不能为null");
        }

        if (obj instanceof Character) {
            return (Character) obj; // 直接强转为char
        }

        if (obj instanceof String) {
            int str = Integer.parseInt((String) obj);
            return (char) str;
        }

        if (obj instanceof Integer) {
            int codePoint = (Integer) obj;
            if (Character.isValidCodePoint(codePoint)) {
                return (char) codePoint; // 整数视为Unicode码点
            } else {
                throw new IllegalArgumentException("无效的Unicode码点: " + codePoint);
            }
        }

        // 其他类型不支持转换
        throw new IllegalArgumentException("不支持的类型: " + obj.getClass().getName());
    }

    private void modifyValue(String iden, MiniJavaObject newValue, List<Integer> arrIndex) {

        for (Map<String, MiniJavaObject> map : resultList) {
            if (map != null && map.containsKey(iden)) {
                if (arrIndex == null || arrIndex.size() == 0) {
                    map.put(iden, newValue);
                } else {
                    MiniJavaObject obj = map.get(iden);
                    List<MiniJavaObject> tmpArr = (List<MiniJavaObject>) obj.value;

                    if (arrIndex.size() == 1) {
                        int resIndex = arrIndex.get(0);
                        tmpArr.set(resIndex, newValue);
                    } else {
                        for (int i = 0; i < arrIndex.size(); i++) {
                            if (i != arrIndex.size() - 1) {
                                tmpArr = (List<MiniJavaObject>) tmpArr.get(arrIndex.get(i)).value;
                            } else {
                                tmpArr.set(arrIndex.get(i), newValue);
                            }
                        }
                    }
                }
            }
        }
    }

    private List<Integer> convert2IntList(List<MiniJavaObject> indexList) {
        List<Integer> res = new ArrayList<>();
        for (MiniJavaObject obj : indexList) {
            int index = convert2Int(obj);
            res.add(index);
        }
        return res;
    }

    private void delEle(String strIndex) {
        if (resultList == null || resultList.isEmpty()) return;

        int index = -1;
        // 查找第一个包含键"a"的Map的索引
        for (int i = 0; i < resultList.size(); i++) {
            Map<String, MiniJavaObject> map = resultList.get(i);
            if (map != null && map.containsKey(strIndex)) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            resultList.subList(index, resultList.size()).clear();
        }
    }

    private String convert2string(MiniJavaObject obj) {
        if (obj == null) {
            return null; // 处理 null 值
        }
        Object val = obj.value;

        if (val instanceof Boolean) {
            return ((Boolean) val).toString();
        }
        else if (val instanceof Integer) {
            return ((Integer) val).toString();
        }
        else if (val instanceof Character) {
            return ((Character) val).toString();
        }
        else if (val instanceof String) {
            return (String) val;
        }
        return "";
    }
    private void initArr(String type, MiniJavaObject arr) {
        if (arr == null) {
            return;
        }
        List<MiniJavaObject> arrList = (List<MiniJavaObject>) arr.value;
        if (arrList == null || arrList.isEmpty()) {
            return;
        }
        if (arrList.getFirst().isArr) {
            for (int i = 0; i < arrList.size(); i++) {
                initArr(type, arrList.get(i));
            }
        }
        for (int i = 0; i < arrList.size(); i++) {
            arrList.get(i).type=type;
            switch (type) {
                case "int": arrList.get(i).value=0;  break;
                case "string": arrList.get(i).value=""; break;
                case "char": arrList.get(i).value='0'; break;
                case "boolean": arrList.get(i).value=false; break;
            }
        }
    }
}
