package entity;

public class Field extends TypedIdentifier {

	public Field(String id, String name, ClassName typeClass) {
		super(id, name, typeClass);
	}
	
	public Field(Variable variable) {
		this(variable.id, variable.name, variable.typeClass);
	}

	@Override
	public String toString() {
		return "Field [id=" + id + ", name=" + name + ", type=" + typeClass + "]";
	}

	@Override
	protected void setType() {
		type = "FieldName";
	}
}
