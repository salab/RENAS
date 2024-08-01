package visitor;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.*;

import entity.ClassName;
import entity.Identifier;
import entity.MethodName;
import entity.Variable;
import org.eclipse.jdt.internal.core.search.IRestrictedAccessMethodRequestor;

public class SimpleVisitor extends ASTVisitor {
	public ArrayList<Identifier> identifiers = new ArrayList<>();
	@Override
	public boolean visit(SimpleName node) {
		IBinding binding = node.resolveBinding();
		if (binding == null) {
			return super.visit(node);
		}
		int kind = binding.getKind();
		switch (kind) {
		case IBinding.TYPE: {
			String id = binding.getKey();
			String name = binding.getName();
			ClassName className = new ClassName(id, name);
			identifiers.add(className);
			break; 
			}
		case IBinding.METHOD: {
			String id = binding.getKey();
			String name = binding.getName();
			ITypeBinding typeBinding = ((IMethodBinding) binding).getReturnType();
			if (typeBinding == null) {
				return super.visit(node);
			}
			String typeid = typeBinding.getKey();
			String typename = typeBinding.getName();
			ClassName className = new ClassName(typeid, typename);
			
			MethodName methodName = new MethodName(id, name, className);
			identifiers.add(methodName);
			break; 
			}
		case IBinding.VARIABLE: {
			String id = binding.getKey();
			String name = binding.getName();
			ITypeBinding typeBinding = ((IVariableBinding) binding).getType();
			if (typeBinding == null) {
				return super.visit(node);
			}
			Variable variable = new Variable(id, name, new ClassName(typeBinding.getKey(),
					typeBinding.getName()));
			identifiers.add(variable);
			break; 
			}
		default:
			break;
		}
		return super.visit(node);
	}
}
