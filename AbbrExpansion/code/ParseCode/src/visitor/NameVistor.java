package visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;

import util.Util;

public class NameVistor extends ASTVisitor {
	@Override
	public boolean visit(SimpleName node) {
		String id = node.resolveBinding().getKey();
		String name = node.resolveBinding().getName().toString();
		id = id.replaceAll(",", "_");
		name = name.replaceAll(",", "_");
		return super.visit(node);
	}
}
