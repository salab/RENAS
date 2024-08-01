package expansion;

import java.util.Arrays;
import java.util.List;

/**
 * Expansions for an identifier
 */
public class VariableNameExpansions extends Expansions {
	private static List<String> key = Arrays.asList("type", "pass", "comment", "enclosingMethod", "enclosingClass", "argumentToParameter", "assignmentEquation");

	public VariableNameExpansions() {
		super();
	}

	@Override
	protected void setType() {
		type = "VariableName";
	}

	@Override
	protected void setKey() {
		expansionKey = key;
	}

}
