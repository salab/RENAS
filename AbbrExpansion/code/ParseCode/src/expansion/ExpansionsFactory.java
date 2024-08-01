package expansion;

public class ExpansionsFactory {
    public static Expansions create(String identifierType) {
        switch (identifierType) {
            case "ClassName" -> {
                return new ClassNameExpansions();
            }
            case "MethodName" -> {
                return new MethodNameExpansions();
            }
            case "FieldName" -> {
                return new FieldNameExpansions();
            }
            case "ParameterName" -> {
                return new ParameterNameExpansions();
            }
            case "VariableName" -> {
                return new VariableNameExpansions();
            }
            default -> {
                System.err.println(ExpansionsFactory.class + " Illegal Identifier Type: " + identifierType);
                return null;
            }
        }
    }
}
