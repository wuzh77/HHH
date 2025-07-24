package cn.edu.nju.cs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MiniJavaObject {
    // "int", "char", "boolean", "string"
    public String type = null;
    public String arrName = null;
    public List<Integer> arrIndex = null;
    // "abc", 1, 2, 4, ..., true, false
    public Object value = null;

    public boolean isArr = false;

    public boolean isTypeCasting = false;

    public boolean isFunc = false;

    public boolean isBreak = false;

    public boolean isContinue = false;

    public boolean isReturn = false;

    // constructor
    public MiniJavaObject(String ty, Object val) {
        this.type  = ty;
        this.value = val;
    }

    public boolean isInt() {
        return "int".equals(type);
    }

    public boolean isChar() {
        return "char".equals(type);
    }

    public boolean isBoolean() {
        return "boolean".equals(type);
    }

    public boolean isString() {
        return "string".equals(type);
    }

    public boolean isVAR() {
        return "var".equals(type);
    }

    public boolean isNull() {
        return "null".equals(type);
    }


    public void  setArr() {
        isArr = true;
    }

    public void assign(MiniJavaObject obj) {
        this.value = obj.value;
        this.type = obj.type;
        this.isArr = obj.isArr;
        this.isTypeCasting = obj.isTypeCasting;
        this.isFunc = obj.isFunc;
        this.isBreak = obj.isBreak;
        this.isContinue = obj.isContinue;
        this.isReturn = obj.isReturn;
        this.arrName =  obj.arrName;
        this.arrIndex = obj.arrIndex;
    }
    @Override
    public String toString() {
        return value.toString();
    }
}