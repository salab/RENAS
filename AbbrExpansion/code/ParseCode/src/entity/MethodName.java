package entity;

public class MethodName extends TypedIdentifier {

	public MethodName(String id, String name, ClassName typeClass) {
		super(id, name, typeClass);
	}

	@Override
	public String toString() {
		return "MethodName [id=" + id + ", name=" + name + ", type=" + typeClass + "]";
	}

	@Override
	protected void setType() {
		type = "MethodName";
	}
}
