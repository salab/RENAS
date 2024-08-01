package entity;

public class Variable extends TypedIdentifier {

	public Variable(String id, String name, ClassName typeClass) {
		super(id, name, typeClass);
	}

	@Override
	public String toString() {
		return "Variable [id=" + id + ", name=" + name + ", type=" + typeClass + "]";
	}

	@Override
	protected void setType() {
		type = "VariableName";
	}
}
