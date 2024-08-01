package entity;

public class Parameter extends TypedIdentifier {

	public Parameter(String id, String name, ClassName typeClass) {
		super(id, name, typeClass);
	}

	@Override
	public String toString() {
		return "Parameter [id=" + id + ", name=" + name + ", type=" + typeClass + "]";
	}

	@Override
	protected void setType() {
		type = "ParameterName";
	}
}
