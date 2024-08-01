package relation;

import entity.Variable;

public class VariableInfo extends Info {
    public Variable variable;

    public VariableInfo(int line, Variable variable) {
        super(line);
        this.variable = variable;
    }
}
