package menuHandles;

import java.util.ArrayList;

import entity.*;
import expansion.AllExpansions;
import relation.*;
import util.Config;
import util.Util;

public class HandleOneFile {
	// relationBase that are selected from visitors in Package visitor
	public ArrayList<Info> infos = new ArrayList<>();

	public void parse() {
		for (Info info : infos) {
			if (info instanceof AssignInfo a) {
				handleAssign(a);
			} else if (info instanceof ClassInfo c) {
				handleExtend(c);
			} else if (info instanceof MethodDeclarationInfo m) {
				handleMethodDeclaration(m);
			} else if (info instanceof MethodInvocationInfo m) {
				handleMethodInvocation(m);
			} else if (info instanceof VariableInfo v) {
				handleVariable(v);
			} else if (info instanceof CommentInfo c) {
				handleComment(c);
			}
		}
	}

	private void handleComment(CommentInfo commentInfo) {
		int startLine = commentInfo.startLine;
		int endLine = commentInfo.line;

		boolean currentLine = true;
		// comment in current line
		for (int i = 0; i < infos.size(); i++) {
			if (infos.get(i).line == startLine && infos.get(i) != commentInfo) {
				currentLine = false;
				handleRelationBaseAndCommentInfo(infos.get(i), commentInfo);
			}
		}
		if (currentLine == false) {
			return;
		}
		// comment in previous line
		for (int i = 0; i < infos.size(); i++) {
			if (infos.get(i).line == (endLine + 1)) {
				handleRelationBaseAndCommentInfo(infos.get(i), commentInfo);
			}
		}
	}

	private void handleRelationBaseAndCommentInfo(Info info, CommentInfo commentInfo) {
		if (info instanceof AssignInfo) {
			AssignInfo assignInfo = (AssignInfo) info;
			ArrayList<Identifier> left = assignInfo.left;
			ArrayList<Identifier> right = assignInfo.right;
			for (int i = 0; i < left.size(); i++) {
				Util.putHashSet(AllExpansions.idToComments, left.get(i).id, commentInfo.content);
			}
			if (right != null) {
				for (int i = 0; i < right.size(); i++) {
					Util.putHashSet(AllExpansions.idToComments, right.get(i).id, commentInfo.content);
				}
			}
		} else if (info instanceof ClassInfo) {
			ClassInfo extendInfo = (ClassInfo) info;
			ClassName className = extendInfo.className;
			Util.putHashSet(AllExpansions.idToComments, className.id, commentInfo.content);

			ArrayList<ClassName> classNames = extendInfo.expans;
			for (int j = 0; j < classNames.size(); j++) {
				Util.putHashSet(AllExpansions.idToComments, classNames.get(j).id, commentInfo.content);
			}
		} else if (info instanceof MethodDeclarationInfo) {
			MethodDeclarationInfo methodDeclarationInfo = (MethodDeclarationInfo) info;
			Util.putHashSet(AllExpansions.idToComments, methodDeclarationInfo.methodName.id, commentInfo.content);

			for (int j = 0; j < methodDeclarationInfo.parameters.size(); j++) {
				Util.putHashSet(AllExpansions.idToComments, methodDeclarationInfo.parameters.get(j).id, commentInfo.content);
			}

			// not constructor
			if (methodDeclarationInfo.methodName.typeClass != null) {
				Util.putHashSet(AllExpansions.idToComments, methodDeclarationInfo.methodName.typeClass.id, commentInfo.content);
			}

		} else if (info instanceof MethodInvocationInfo) {
			MethodInvocationInfo methodInvocationInfo = (MethodInvocationInfo) info;
			Util.putHashSet(AllExpansions.idToComments, methodInvocationInfo.methodName.typeClass.id, commentInfo.content);

			for (int j = 0; j < methodInvocationInfo.arguments.size(); j++) {
				Argument argument = methodInvocationInfo.arguments.get(j);
				for (int k = 0; k < argument.identifiers.size(); k++) {
					Util.putHashSet(AllExpansions.idToComments, argument.identifiers.get(k).id, commentInfo.content);
				}
			}
		}
	}

	private void handleMethodInvocation(MethodInvocationInfo methodInvocationInfo) {

		AllExpansions.idToMethodName.put(methodInvocationInfo.methodName.id, methodInvocationInfo.methodName);
		AllExpansions.idToClassName.put(methodInvocationInfo.methodName.typeClass.id, methodInvocationInfo.methodName.typeClass);

		ArrayList<Argument> arguments = methodInvocationInfo.arguments;
		for (Argument argument : arguments) {
			ClassName type = argument.type;
			
			AllExpansions.idToClassName.put(type.id, type);

			ArrayList<Identifier> identifiers = argument.identifiers;
			
			for (Identifier identifier : identifiers) {
				AllExpansions.idToIdentifier.put(identifier.id, identifier);
				handleType(identifier);
			}
		}
		
		AllExpansions.methodInvocationInfos.add(methodInvocationInfo);
	}

	private void handleMethodDeclaration(MethodDeclarationInfo methodDeclarationInfo) {
		AllExpansions.methodDeclarationInfos.add(methodDeclarationInfo);
		AllExpansions.idToDeclarationInfo.put(methodDeclarationInfo.methodName.id, methodDeclarationInfo);

		AllExpansions.idToFile.put(methodDeclarationInfo.methodName.id, Config.projectName);
		AllExpansions.idToMethodName.put(methodDeclarationInfo.methodName.id, methodDeclarationInfo.methodName);

		if (methodDeclarationInfo.methodName.typeClass != null) {
			AllExpansions.idToClassName.put(methodDeclarationInfo.methodName.typeClass.id, methodDeclarationInfo.methodName.typeClass);
		}

		ArrayList<Parameter> parameters = methodDeclarationInfo.parameters;
		for (Parameter value : parameters) {
			AllExpansions.idToFile.put(value.id, Config.projectName);
			AllExpansions.idToParameter.put(value.id, value);

			AllExpansions.idToClassName.put(value.typeClass.id, value.typeClass);
		}
		methodDeclarationInfo.parameterInfos.forEach(info -> AllExpansions.idToDeclarationInfo.put(info.parameter.id, info));

		ArrayList<Identifier> identifiers = methodDeclarationInfo.identifiers;
		
		for (Identifier identifier : identifiers) {
			AllExpansions.idToIdentifier.put(identifier.id, identifier);
			handleType(identifier);
		}
		
	}

	private void handleExtend(ClassInfo classInfo) {
		AllExpansions.classInfos.add(classInfo);
		AllExpansions.idToDeclarationInfo.put(classInfo.className.id, classInfo);

		ClassName className = classInfo.className;
		AllExpansions.idToFile.put(className.id, Config.projectName);
		AllExpansions.idToClassName.put(className.id, className);

		ArrayList<ClassName> classNames = classInfo.expans;
		for (ClassName name : classNames) {
			AllExpansions.idToClassName.put(name.id, name);
			Util.put(AllExpansions.childToParent, className.id, name.id);
			Util.put(AllExpansions.parentToChild, name.id, className.id);
		}

		ArrayList<Field> fields = classInfo.fields;
		for (Field field : fields) {
			AllExpansions.idToFile.put(field.id, Config.projectName);
			AllExpansions.idToField.put(field.id, field);

			AllExpansions.idToClassName.put(field.typeClass.id, field.typeClass);
		}
		classInfo.fieldInfos.forEach(info -> AllExpansions.idToDeclarationInfo.put(info.field.id, info));
		
		ArrayList<MethodName> methodNames = classInfo.methodNames;
		for (MethodName methodName : methodNames) {
			AllExpansions.idToMethodName.put(methodName.id, methodName);

			if (methodName.typeClass != null) {
				AllExpansions.idToClassName.put(methodName.typeClass.id, methodName.typeClass);
			}
		}
		ArrayList<Identifier> identifiers = classInfo.identifiers;
		
		for (Identifier identifier : identifiers) {
			AllExpansions.idToIdentifier.put(identifier.id, identifier);
			handleType(identifier);
		}
	}

	private void handleAssign(AssignInfo assignInfo) {
		AllExpansions.assignInfos.add(assignInfo);

		ArrayList<Identifier> left = assignInfo.left;
		ArrayList<Identifier> right = assignInfo.right;
		for (Identifier identifier : left) {
			AllExpansions.idToIdentifier.put(identifier.id, identifier);
			handleType(identifier);
		}
		if (right != null) {
			for (Identifier identifier : right) {
				AllExpansions.idToIdentifier.put(identifier.id, identifier);
				handleType(identifier);
			}
		}
	}

//	Just add to idToDeclarationInfo and idToFile
	private void handleVariable(VariableInfo variableInfo) {
		AllExpansions.idToDeclarationInfo.put(variableInfo.variable.id, variableInfo);
		AllExpansions.idToFile.put(variableInfo.variable.id, Config.projectName);
	}

	private void handleType(Identifier identifier) {
		if (identifier instanceof TypedIdentifier typedIdentifier) {
			AllExpansions.idToClassName.put(typedIdentifier.typeClass.id, typedIdentifier.typeClass);
		}
	}
}
