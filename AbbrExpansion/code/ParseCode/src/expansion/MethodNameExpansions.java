package expansion;

import java.util.Arrays;
import java.util.List;

/**
 * Expansions for an identifier
 */
public class MethodNameExpansions extends Expansions {
	private static List<String> key = Arrays.asList("type", "sibling-members", "comment", "enclosingClass", "parameter", "assignmentEquation", "argumentToParameter", "pass");


	public MethodNameExpansions() {
		super();
	}

	@Override
	protected void setType() {
		type = "MethodName";
	}

	@Override
	protected void setKey() {
		expansionKey = key;
	}
}
