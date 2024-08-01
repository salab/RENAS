package visitor;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.*;

import entity.ClassName;
import entity.Identifier;
import entity.MethodName;
import entity.Parameter;
import menuHandles.HandleOneFile;
import relation.MethodDeclarationInfo;
import relation.ParameterInfo;

public class MethodDeclarationVisitor extends ASTVisitor{
	private CompilationUnit compilationUnit;
	private HandleOneFile handler;

	public MethodDeclarationVisitor(CompilationUnit compilationUnit, HandleOneFile handler) {
		super();
		this.compilationUnit = compilationUnit;
		this.handler = handler;

	}

	@Override
	public boolean visit(MethodDeclaration node) {
		MethodDeclarationInfo methodDeclarationInfo = getMethodDeclarationInfo(node, compilationUnit);
		if (methodDeclarationInfo != null) {
			handler.infos.add(methodDeclarationInfo);
		}
		return super.visit(node);
	}

	public static MethodDeclarationInfo getMethodDeclarationInfo(MethodDeclaration node,
			CompilationUnit compilationUnit) {
		SimpleVisitor simpleVisitor = new SimpleVisitor();
		node.accept(simpleVisitor);
		ArrayList<Identifier> identifiers = simpleVisitor.identifiers;
		
		
		if (node.resolveBinding() == null) {
			return null;
		}
		// method name
		IMethodBinding binding = node.resolveBinding();
		String id = binding.getKey();
		String name = node.getName().toString();
		MethodName methodName;
		if (!node.isConstructor()) {
			ITypeBinding returnTypeBinding = binding.getReturnType();
			if (returnTypeBinding == null) {
				return null;
			}
			String typeid = returnTypeBinding.getKey();
			String typename = returnTypeBinding.getName();
			ClassName className = new ClassName(typeid, typename);
			methodName = new MethodName(id, name, className);
		} else {
			methodName = new MethodName(id, name, null);
		}

		ArrayList<ParameterInfo> parameters = new ArrayList<>();
		for (Object object : node.parameters()) {
			SingleVariableDeclaration svd = (SingleVariableDeclaration) object;
			IVariableBinding variableBinding = svd.resolveBinding();
			String id2 = variableBinding.getKey();;
			String name2 = svd.getName().toString();
			ITypeBinding typeBinding = svd.getType().resolveBinding();
			if (typeBinding == null) {
				return null;
			}
			int parameterLine = compilationUnit.getLineNumber(svd.getStartPosition());
			Parameter parameter = new Parameter(id2, name2, new ClassName(typeBinding.getKey(),
					typeBinding.getName()));
			parameters.add(new ParameterInfo(parameterLine, parameter));
		}

		// get line number
		Javadoc javaDoc = node.getJavadoc();
		int line;
		if (javaDoc == null) {
			line = compilationUnit.getLineNumber(node.getStartPosition());
		} else {
			line = compilationUnit.getLineNumber(node.getStartPosition()+javaDoc.getLength())+1;
		}

		return new MethodDeclarationInfo(line, methodName, parameters, identifiers);
	}
}
