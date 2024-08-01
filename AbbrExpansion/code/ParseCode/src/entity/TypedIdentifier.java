package entity;

public abstract class TypedIdentifier extends Identifier {
    public ClassName typeClass;

    public TypedIdentifier(String id, String name, ClassName typeClass) {
        super(id, name);
        this.typeClass = typeClass;
    }
}
