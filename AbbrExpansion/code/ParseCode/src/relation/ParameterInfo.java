package relation;

import entity.ClassName;
import entity.Parameter;

public class ParameterInfo extends Info {
    public Parameter parameter;

    public ParameterInfo(int line, Parameter parameter) {
        super(line);
        this.parameter = parameter;
    }
}
