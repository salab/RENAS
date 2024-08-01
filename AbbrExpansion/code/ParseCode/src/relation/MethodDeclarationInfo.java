package relation;

import java.util.ArrayList;

import entity.Identifier;
import entity.MethodName;
import entity.Parameter;

public class MethodDeclarationInfo extends Info {
	public MethodName methodName;
	public ArrayList<Parameter> parameters;
	public ArrayList<ParameterInfo> parameterInfos;
	public ArrayList<Identifier> identifiers;

	public MethodDeclarationInfo(int line, MethodName methodName, ArrayList<ParameterInfo> parameterInfos,
			ArrayList<Identifier> identifiers) {
		super(line);
		this.methodName = methodName;
		this.parameterInfos = parameterInfos;
		this.identifiers = identifiers;
		setParameter();
	}

	private void setParameter() {
		this.parameters = new ArrayList<>();
		parameterInfos.forEach(info -> parameters.add(info.parameter));
	}

	@Override
	public String toString() {
		return "MethodDeclarationInfo [methodName=" + methodName + ", parameters=" + parameters + ", line=" + line
				+ "]";
	}
}
