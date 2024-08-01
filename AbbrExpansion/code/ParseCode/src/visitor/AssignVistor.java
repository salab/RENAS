package visitor;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.*;
import relation.VariableInfo;
import util.Util;

import entity.ClassName;
import entity.Identifier;
import entity.Variable;
import menuHandles.HandleOneFile;
import relation.AssignInfo;

public class AssignVistor extends ASTVisitor {
	// used to get line numbers
    private CompilationUnit compilationUnit;
    // used to collect relation info
    private HandleOneFile globalVariables;
	public AssignVistor(CompilationUnit compilationUnit, HandleOneFile globalVariables) {
		super();
		this.compilationUnit = compilationUnit;
		this.globalVariables = globalVariables;
	}

	@Override
	public boolean visit(Assignment node) {
		Expression left = node.getLeftHandSide();
		Expression right = node.getRightHandSide();
		
		ArrayList<Identifier> leftIdentifiers = Util.parseExpression(left);
		ArrayList<Identifier> rightIdentifiers = Util.parseExpression(right);
		
		int line = compilationUnit.getLineNumber(node.getStartPosition());
		AssignInfo assignInfo = new AssignInfo(line, leftIdentifiers, rightIdentifiers);
		globalVariables.infos.add(assignInfo);
		return super.visit(node);
	}
	
	// field declarations, local variable declarations, 
	// ForStatement initializers, and LambdaExpression parameters
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		AssignInfo assignInfo = getAssignInfo(node, compilationUnit);
		if (assignInfo != null) {
			globalVariables.infos.add(assignInfo);
			ASTNode parent = node.getParent();
			if (parent instanceof VariableDeclarationStatement ||
					parent instanceof VariableDeclarationExpression ) {
				VariableInfo variableInfo = new VariableInfo(assignInfo.line, (Variable) assignInfo.left.get(0));
				globalVariables.infos.add(variableInfo);
			}
		}
		return super.visit(node);
	}
	
	public static AssignInfo getAssignInfo(VariableDeclarationFragment node, CompilationUnit compilationUnit) {
		IVariableBinding binding = node.resolveBinding();
		if (binding == null) {
			return null;
		}
		String id = binding.getKey();
		String name = node.getName().toString();
		ITypeBinding iType = binding.getType();
		ClassName type = iType != null ?
				new ClassName(iType.getKey(), iType.getName()) :
				new ClassName("", "");
		Variable variable = new Variable(id, name, type);
		
		ArrayList<Identifier> leftIdentifiers = new ArrayList<>();
		leftIdentifiers.add(variable);
		ArrayList<Identifier> rightIdentifiers = Util.parseExpression(node.getInitializer());
		
		int line = compilationUnit.getLineNumber(node.getStartPosition());
		return new AssignInfo(line, leftIdentifiers, rightIdentifiers);
	}

	// formal parameter lists and catch clauses
	@Override
	public boolean visit(SingleVariableDeclaration node) {
		IVariableBinding binding = node.resolveBinding();
		if (binding == null) {
			return super.visit(node);
		}
		String id = binding.getKey();
		String name = node.getName().toString();
		ITypeBinding typeBinding = binding.getType();
		if (typeBinding == null) {
			return super.visit(node);
		}
		ClassName type = new ClassName(typeBinding.getKey(),
				typeBinding.getName());
		int variableLine = compilationUnit.getLineNumber(node.getStartPosition());
		Variable variable = new Variable(id, name, type);
		VariableInfo variableInfo = new VariableInfo(variableLine, variable);
		
		ArrayList<Identifier> leftIdentifiers = new ArrayList<>();
		leftIdentifiers.add(variable);
		ArrayList<Identifier> rightIdentifiers = Util.parseExpression(node.getInitializer());
		
		int line = compilationUnit.getLineNumber(node.getStartPosition());
		AssignInfo assignInfo = new AssignInfo(line, leftIdentifiers, rightIdentifiers);
		
		globalVariables.infos.add(assignInfo);
		globalVariables.infos.add(variableInfo);
		return super.visit(node);
	}
}
