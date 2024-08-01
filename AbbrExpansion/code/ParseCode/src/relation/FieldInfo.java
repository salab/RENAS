package relation;

import entity.Field;

public class FieldInfo extends Info {
    public Field field;

    public FieldInfo(int line, Field field) {
        super(line);
        this.field = field;
    }
}
