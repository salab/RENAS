package visitor;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.*;

import entity.Argument;
import entity.ClassName;
import entity.Identifier;
import entity.MethodName;
import menuHandles.HandleOneFile;
import relation.MethodInvocationInfo;
import util.Util;


public class MethodInvocationVisitor extends ASTVisitor{
	private CompilationUnit compilationUnit;
	private HandleOneFile globalVariables;

	public MethodInvocationVisitor(CompilationUnit compilationUnit, HandleOneFile globalVariables) {
		super();
		this.compilationUnit = compilationUnit;
		this.globalVariables = globalVariables;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding methodBinding = node.resolveMethodBinding();
		if (methodBinding == null) {
			return super.visit(node);
		}
		String id = methodBinding.getKey();
		String name = node.getName().toString();
		ITypeBinding typeBinding = node.resolveTypeBinding();
		if (typeBinding == null) {
			return super.visit(node);
		}
		ClassName className = new ClassName(typeBinding.getKey(),
				typeBinding.getName());
		MethodName methodName = new MethodName(id, name, className);

		// arguments
		ArrayList<Argument> arguments = new ArrayList<>();
		try {
			for (Object object : node.arguments()) {
				Expression expression = (Expression) object;
				ArrayList<Identifier> identifiers = Util.parseExpression(expression);
				ITypeBinding expressionBinding = expression.resolveTypeBinding();
				if (expressionBinding == null) {
					return super.visit(node);
				}
				Argument argument = new Argument(new ClassName(expressionBinding.getKey(),
						expressionBinding.getName()), identifiers);
				arguments.add(argument);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		int line = compilationUnit.getLineNumber(node.getStartPosition());
		MethodInvocationInfo methodInvocationInfo = new MethodInvocationInfo(line, methodName, arguments);
		globalVariables.infos.add(methodInvocationInfo);
		return super.visit(node);
	}
}
