package expansion;

import java.lang.reflect.Type;
import java.util.*;

import entity.*;
import relation.*;
import util.Util;

public class AllExpansions {
	public static HashMap<String, ClassName> idToClassName = new HashMap<>();
	public static HashMap<String, MethodName> idToMethodName = new HashMap<>();
	public static HashMap<String, Field> idToField = new HashMap<>();
	public static HashMap<String, Parameter> idToParameter = new HashMap<>();
	public static HashMap<String, Variable> idToVariable = new HashMap<>();

	// type to ding	
	public static HashMap<String, Identifier> idToIdentifier = new HashMap<>();

	public static HashMap<String, String> idToFile = new HashMap<>();


	public static HashMap<String, ArrayList<String>> childToParent = new HashMap<>();
	public static HashMap<String, ArrayList<String>> parentToChild = new HashMap<>();

	public static ArrayList<MethodDeclarationInfo> methodDeclarationInfos = new ArrayList<>();
	public static ArrayList<MethodInvocationInfo> methodInvocationInfos = new ArrayList<>();
	public static ArrayList<AssignInfo> assignInfos = new ArrayList<>();
	public static ArrayList<ClassInfo> classInfos = new ArrayList<>();
	public static HashMap<String, HashSet<String>> idToComments = new HashMap<>();

//	introduced by shklan
	public static HashMap<String, Info> idToDeclarationInfo = new HashMap<>();
	public static HashMap<String, Expansions> idToExpansions = new HashMap<>();
	private static HashMap<String, LocatedIdentifier> idToLocatedIdentifier = new HashMap<>();
	private static List<String> expansions = Arrays.asList(
			"subclass",
			"descendant",
			"parent",
			"ancestor",
			"method",
			"field",
			"sibling-members",
			"comment",
			"type",
			"enclosingClass",
			"assignmentEquation",
			"pass",
			"argumentToParameter",
			"parameter",
			"enclosingMethod",
			"parameterToArgument"
	);
//

	public static void postprocess() {
		handleFieldParameterVariable();
		locateIdentifiers();

		handleAssign();
		handleMethodInvocation();
		handleComment();

		handleExtend();
		handleMethodDeclaration();
		handleExpansionType();

		toFile();
	}

	private static void handleMethodDeclaration() {
		for (MethodDeclarationInfo methodDeclarationInfo : methodDeclarationInfos) {
			ArrayList<Parameter> parameters = methodDeclarationInfo.parameters;
			for (Parameter parameter : parameters) {
				// method name --> parameter
				AllExpansions.addExpansion(methodDeclarationInfo.methodName.id, parameter.id, "MethodName", "parameter");
				// parameter --> method name
				AllExpansions.addExpansion(parameter.id, methodDeclarationInfo.methodName.id, "ParameterName", "enclosingMethod");
			}

			ArrayList<Identifier> identifiers = methodDeclarationInfo.identifiers;
			for (Identifier identifier : identifiers) {
				if (idToVariable.keySet().contains(identifier.id)) {
					addExpansion(identifier.id, methodDeclarationInfo.methodName.id, "VariableName", "enclosingMethod");
				}
			}
		}
	}

	private static void handleExtend() {
		handleChildToParent();

		for (ClassInfo classInfo : classInfos) {
			ClassName className = classInfo.className;

			ArrayList<Field> fields = classInfo.fields;
			for (Field field : fields) {
				AllExpansions.addExpansion(className.id, field.id, "ClassName", "field");
				AllExpansions.addExpansion(field.id, className.id, "FieldName", "enclosingClass");
			}
			ArrayList<MethodName> methodNames = classInfo.methodNames;
			for (MethodName methodName : methodNames) {
				AllExpansions.addExpansion(className.id, methodName.id, "ClassName", "method");
				AllExpansions.addExpansion(methodName.id, className.id, "MethodName", "enclosingClass");
			}
			handleSiblings(methodNames, fields);

			ArrayList<Identifier> identifiers = classInfo.identifiers;
			for (Identifier identifier : identifiers) {
				if (idToVariable.containsKey(identifier.id)) {
					addExpansion(identifier.id, className.id, "VariableName", "enclosingClass");
				}
			}
		}
	}

	private static void handleSiblings(ArrayList<MethodName> mList, ArrayList<Field> fList) {
		ArrayList<TypedIdentifier> members = new ArrayList<>();
		members.addAll(mList);
		members.addAll(fList);
		while(members.size() > 0) {
			TypedIdentifier member = members.remove(members.size()-1);
			for (TypedIdentifier sibling : members) {
				addExpansion(member.id, sibling.id, member.type, "sibling-members");
				addExpansion(sibling.id, member.id, member.type, "sibling-members");
			}
		}
	}

	public static void handleChildToParent() {
		for (String id: idToClassName.keySet()) {
			if (idToClassName.get(id) instanceof ClassName) {
				ArrayList<String> parents = childToParent.get(id);
				ArrayList<String> ancestor = new ArrayList<>();
				if (parents != null) {
					for (String pId : parents) {
						AllExpansions.addExpansion(id, pId, "ClassName", "parent");
					}
					search(childToParent, ancestor, parents);
				}
				ArrayList<String> subclass = parentToChild.get(id);
				ArrayList<String> descendant = new ArrayList<>();
				if (subclass != null) {
					for (String sId : subclass) {
						AllExpansions.addExpansion(id, sId, "ClassName", "subclass");
					}
					search(parentToChild, descendant, subclass);
				}
				for (String an : ancestor) {
					AllExpansions.addExpansion(id, an, "ClassName", "ancestor");
				}
				for (String ssId : descendant) {
					AllExpansions.addExpansion(id, ssId, "ClassName", "descendant");
				}
			}

		}	
	}

	public static void search(HashMap<String, ArrayList<String>> tree, ArrayList<String> ancestor, ArrayList<String> parents) {
		ArrayList<String> temp = new ArrayList<>();
		for (String t : parents) {
			ArrayList<String> tt = tree.get(t);
			if (tt == null) {
				continue;
			}
			temp.addAll(tt);
		}
		if (temp.size() == 0) {
			return;
		}
//		ancestor <- parentsのすべての親クラス
		ancestor.addAll(temp);
		search(tree, ancestor, temp);
	}

	private static String printHashSet(HashSet<String> set) {
		StringBuilder sb = new StringBuilder();
		for (String id : set) {
			if (idToLocatedIdentifier.containsKey(id)) {
				LocatedIdentifier identifier = idToLocatedIdentifier.get(id);
				sb.append(id)
						.append(":")
						.append(identifier.getName())
						.append(" - ");
			} else {
				sb.append(printHashSetString(set));
			}
		}
		return sb.toString();
	}

	private static String commaToOther(String str) {
		if (str == null) {
			return null;
		} else {
			return str.replaceAll(",", "_");
		}
	}


	private static void locateIdentifiers() {
		idToIdentifier.putAll(idToClassName);
		idToIdentifier.putAll(idToMethodName);
		idToIdentifier.putAll(idToParameter);
		idToIdentifier.putAll(idToField);
		idToIdentifier.putAll(idToVariable);
		for (String key : idToIdentifier.keySet()) {
			Identifier identifier = idToIdentifier.get(key);
			idToLocatedIdentifier.put(key, identifier.locate(idToDeclarationInfo));
		}
	}

	private static void toFile() {
		StringBuilder sb = new StringBuilder();
		sb.append("id,")
				.append("files,")
				.append("line,")
				.append("name,")
				.append("typeOfIdentifier,")
				.append("subclass,")
				.append("descendant,")
				.append("parent,")
				.append("ancestor,")
				.append("method,")
				.append("field,")
				.append("sibling-members,")
				.append("comment,")
				.append("type,")
				.append("enclosingClass,")
				.append("assignmentEquation,")
				.append("pass,")
				.append("argumentToParameter,")
				.append("parameter,")
				.append("enclosingMethod,")
				.append("parameterToArgument")
				.append("\n");

		for (String key : idToLocatedIdentifier.keySet()) {
			appendColumn(sb, key);
		}

		Util.exportToFile(sb);
	}

	private static void appendColumn(StringBuilder sb, String key) {
		Expansions base = idToExpansions.get(key);
		String files = idToFile.get(key);
		LocatedIdentifier identifier = idToLocatedIdentifier.get(key);
		sb.append(commaToOther(key)) // id
				.append(",")
				.append(commaToOther(files)) // files
				.append(",")
				.append(identifier.getLine()) // line
				.append(",")
				.append(commaToOther(identifier.getName())) // name
				.append(",")
				.append(identifier.getType()); //type
		for (String expan : expansions) {
			sb.append(",");
			if (base == null) {
				continue;
			}
			HashSet<String> set = base.getExpansions(expan);
			if (set != null) {
				sb.append(commaToOther(printHashSet(set)));
			}
		}
		sb.append("\n");
	}
	
	
	private static String printHashSetString(HashSet<String> hashSet) {
		if (hashSet == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String string : hashSet) {
			sb.append(string)
					.append(";");
		}
		return sb.toString();
	}

	private static String parseComment(String comment) {
		String result = "";
		for (int i = 0; i < comment.length(); i++) {
			if (Util.isLetter(comment.charAt(i))) {
				result += comment.charAt(i);
			} else {
				result += " ";
			}
		}
		while (result.contains("  ")) {
			result = result.replaceAll("  ", " ");
		}
		return result.trim();
	}


	private static void handleExpansionType() {
		for (String id : idToLocatedIdentifier.keySet()) {
			LocatedIdentifier identifier = idToLocatedIdentifier.get(id);
			ClassName typeClass = identifier.getTypeClass();
			if (typeClass != null) {
				addExpansion(identifier.getId(), typeClass.id, identifier.getType(), "type");
				addExpansion(typeClass.id, identifier.getId(),"ClassName", "type");
			}
		}
	}

	private static void handleMethodInvocation() {
		HashMap<String, MethodDeclarationInfo> hashMap = new HashMap<>();
		HashSet<String> unresolved = new HashSet<>();
		for (MethodDeclarationInfo declarationInfo : methodDeclarationInfos) {
			hashMap.put(declarationInfo.methodName.id, declarationInfo);
		}
		for (MethodInvocationInfo methodInvocationInfo : methodInvocationInfos) {
			String methodId = methodInvocationInfo.methodName.id;
			if (hashMap.containsKey(methodId)) {
				MethodDeclarationInfo methodDeclarationInfo = hashMap.get(methodId);
				ArrayList<Parameter> parameters = methodDeclarationInfo.parameters;
				ArrayList<Argument> arguments = methodInvocationInfo.arguments;
				if (parameters.size() != arguments.size()) {
//					System.err.println("not equal size of parameters and arguments " + parameters.size() + " " + arguments.size());
					continue;
				}
				for (int j = 0; j < parameters.size(); j++) {
					Parameter parameter = parameters.get(j);
					Argument argument = arguments.get(j);
					for (int k = 0; k < argument.identifiers.size(); k++) {
						Identifier identifier = argument.identifiers.get(k);
						if (!idToClassName.containsKey(identifier.id)) {
							addExpansion(parameter.id, identifier.id, "ParameterName", "parameterToArgument");
						}
					}
				}
				for (int j = 0; j < methodInvocationInfo.arguments.size(); j++) {
					Parameter parameter = parameters.get(j);
					Argument argument = arguments.get(j);
					for (int k = 0; k < argument.identifiers.size(); k++) {
						Identifier identifier = argument.identifiers.get(k);
						if (!idToClassName.containsKey(identifier.id)) {
							addExpansionAccordingToType(identifier.id, methodInvocationInfo.methodName.id, "pass");
							addExpansionAccordingToType(identifier.id, parameter.id, "argumentToParameter");
						}
					}
				}
			} else {
				unresolved.add(methodId);
			}
		}
	}

	private static void handleAssign() {
		for (AssignInfo assignInfo : assignInfos) {
			ArrayList<Identifier> left = assignInfo.left;
			ArrayList<Identifier> right = assignInfo.right;

			if (right != null) {

				for (Identifier identifier1 : left) {
					for (Identifier identifier2 : right) {
						addExpansionAccordingToType(identifier1.id, identifier2.id, "assignmentEquation");
						addExpansionAccordingToType(identifier2.id, identifier1.id, "assignmentEquation");
					}
				}
			}
		}
	}

	private static void handleComment() {
		for (String id : idToComments.keySet()) {
			String comment = "";
			HashSet<String> temp = idToComments.get(id);
			for (String string : temp) {
				comment += string + " ";
			}
			if (idToLocatedIdentifier.containsKey(id)) {
				LocatedIdentifier identifier = idToLocatedIdentifier.get(id);
				addExpansion(id, parseComment(comment), identifier.getType(), "comment");
			}
		}
	}

	private static void addExpansionAccordingToType(String id, String value, String relationType) {
		if (idToLocatedIdentifier.containsKey(id)) {
			LocatedIdentifier identifier = idToLocatedIdentifier.get(id);
			addExpansion(id, value, identifier.getType(), relationType);
		}
	}

	private static void handleFieldParameterVariable() {
		for (String id : idToIdentifier.keySet()) {
			if (idToParameter.containsKey(id) ||
					idToField.containsKey(id) ||
					idToClassName.containsKey(id) ||
					idToMethodName.containsKey(id)) {

			} else {
				Identifier identifier = idToIdentifier.get(id);
				if (identifier instanceof Field) {
					idToField.put(id, (Field) identifier);
				} else if (identifier instanceof ClassName) {
					idToClassName.put(id, (ClassName) identifier);
				} else if (identifier instanceof Parameter) {
					idToParameter.put(id, (Parameter) identifier);
				} else if (identifier instanceof MethodName) {
					idToMethodName.put(id, (MethodName) identifier);
				} else if (identifier instanceof Variable) {
					idToVariable.put(id, (Variable) identifier);
				}
			}
		}
	}

	public static void addExpansion(String id, String value, String identifierType, String relationType) {
		if (idToExpansions.containsKey(id)) {
			idToExpansions.get(id).setExpansions(relationType, value);
		} else {
			Expansions expansions = ExpansionsFactory.create(identifierType);
			expansions.setExpansions(relationType, value);
			idToExpansions.put(id, expansions);
		}
	}
}
