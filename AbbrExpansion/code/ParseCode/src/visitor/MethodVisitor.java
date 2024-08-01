package visitor;


import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodVisitor extends ASTVisitor{
	@Override
	public boolean visit(MethodDeclaration node) {
		String id = node.resolveBinding().getKey();
		NameVistor2 simpleVisitor = new NameVistor2(id);
		node.accept(simpleVisitor);
		return super.visit(node);
	}
}
