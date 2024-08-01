package entity;

public class LocatedIdentifier {
    private Identifier identifier;
    private int line;

    public LocatedIdentifier(Identifier identifier, int line) {
        this.identifier = identifier;
        this.line = line;
    }

    public String getId() {
        return identifier.id;
    }

    public String getName() {
        return identifier.name;
    }

    public String getType() {
        return identifier.type;
    }

    public ClassName getTypeClass() {
        if (identifier instanceof TypedIdentifier) {
            return ((TypedIdentifier) identifier).typeClass;
        } else {
            return null;
        }
    }

    public int getLine() {
        return line;
    }
}
