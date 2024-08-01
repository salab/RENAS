package visitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

import entity.ClassName;
import entity.Field;
import entity.Identifier;
import entity.MethodName;
import entity.Variable;
import menuHandles.HandleOneFile;
import relation.AssignInfo;
import relation.ClassInfo;
import relation.FieldInfo;
import relation.MethodDeclarationInfo;

/**
 * parse names of super class and super interface
 */
public class ClassVisitor extends ASTVisitor {
    private CompilationUnit compilationUnit;
    private HandleOneFile globalVariables;

	public ClassVisitor(CompilationUnit compilationUnit, HandleOneFile globalVariables) {
		super();
		this.compilationUnit = compilationUnit;
		this.globalVariables = globalVariables;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		SimpleVisitor simpleVisitor = new SimpleVisitor();
		node.accept(simpleVisitor);
		ArrayList<Identifier> identifiers = simpleVisitor.identifiers;
		ArrayList<MethodName> methodNames = new ArrayList<>();
		MethodDeclaration[] methods = node.getMethods();
		for (MethodDeclaration methodDeclaration : methods) {
			MethodDeclarationInfo methodDeclarationInfo =
			MethodDeclarationVisitor.getMethodDeclarationInfo(methodDeclaration, compilationUnit);
			if (methodDeclarationInfo != null) {
				methodNames.add(methodDeclarationInfo.methodName);
			}
		}
		
		ArrayList<FieldInfo> fields = new ArrayList<>();
		FieldDeclaration[] fieldDeclarations = node.getFields();
		for (FieldDeclaration fieldDeclaration : fieldDeclarations) {
			
			List<VariableDeclarationFragment> fragments = fieldDeclaration.fragments();
			for (VariableDeclarationFragment variableDeclarationFragment : fragments) {
				AssignInfo assignInfo =
				AssignVistor.getAssignInfo(variableDeclarationFragment, compilationUnit);
				if (assignInfo == null) {
					continue;
				}
				
				if (assignInfo.left.size() != 1) {
					System.err.println("field size not 1");
				}
				if (!(assignInfo.left.get(0) instanceof Variable)) {
					System.err.println("field not variable");
				}
				int fieldLine = assignInfo.line;
				Variable variable = (Variable) assignInfo.left.get(0);
				Field field = new Field(variable);
				fields.add(new FieldInfo(fieldLine, field));
			}
		}

		ITypeBinding binding = node.resolveBinding();
		if (binding == null) {
			return super.visit(node);
		}

		String id1 = binding.getKey();
		String name1 = node.getName().toString();
		ClassName className1 = new ClassName(id1, name1);
		
		ArrayList<ClassName> expans = new ArrayList<>();
		// super class
		Type superClass = node.getSuperclassType();
		if (superClass != null) {
			ITypeBinding temp = superClass.resolveBinding();
			if (temp == null) {
				return super.visit(node);
			}
			String id2 = temp.getKey();
			String name2 = temp.getName();
			ClassName className2 = new ClassName(id2, name2);
			expans.add(className2);
		}
		// super interfaces
		List<Type> interfaceTypes = node.superInterfaceTypes();
		for (Type type : interfaceTypes) {
			ITypeBinding typeBinding = type.resolveBinding();
			if (typeBinding == null) {
				return super.visit(node);
			}
			String id = typeBinding.getKey();
			String name = typeBinding.getName();
			ClassName className = new ClassName(id, name);
			expans.add(className);
		}
		
		// get line number
		Javadoc javaDoc = node.getJavadoc();
		int line;
		if (javaDoc == null) {
			line = compilationUnit.getLineNumber(node.getStartPosition());
		} else {
			line = compilationUnit.getLineNumber(node.getStartPosition()+javaDoc.getLength())+1;
		}
		
		ClassInfo extendInfo = new ClassInfo(line, className1, expans, fields, methodNames, identifiers);
		globalVariables.infos.add(extendInfo);
		return super.visit(node);
	}
}
