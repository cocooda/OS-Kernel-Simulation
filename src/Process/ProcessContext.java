package Process;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import Instruction.Value;

public class ProcessContext {
    public Value result;
    public Value a;
    public Value b;
    public String content; 
    public Path path;
    public LinkedList<Value> values;
    public Map<String, Value> vars;

    public ProcessContext() {
        this.result = Value.ofInt(0);
        this.a = Value.ofInt(0);
        this.b = Value.ofInt(0);
        this.content = "";
        this.path = null;
        this.values = new LinkedList<>();
        this.vars = new HashMap<>();
        this.vars.put("a", this.a);
        this.vars.put("b", this.b);
        this.vars.put("result", this.result);
    };

    public void setA (Value a) {
        this.a = a; 
        this.vars.put("a", a);
    };


    public void setB (Value b) {
        this.b = b; 
        this.vars.put("b", b);
    };

    public void setRes (Value res) {
        this.result = res; 
        this.vars.put("result", res);
    };

    public void setContent (String content) {
        this.content = content;
    };

    public void setPath (Path path) {
        this.path = path;
    }

    public void setVar(String name, Value value) {
        if ("a".equals(name)) {
            setA(value);
            return;
        }
        if ("b".equals(name)) {
            setB(value);
            return;
        }
        if ("result".equals(name)) {
            setRes(value);
            return;
        }
        this.vars.put(name, value);
    }

    public Value getVar(String name) {
        if ("a".equals(name)) return this.a;
        if ("b".equals(name)) return this.b;
        if ("result".equals(name)) return this.result;
        return this.vars.get(name);
    }

}
