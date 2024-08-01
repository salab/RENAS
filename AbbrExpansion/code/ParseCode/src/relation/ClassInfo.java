package relation;

import java.util.ArrayList;

import entity.ClassName;
import entity.Field;
import entity.Identifier;
import entity.MethodName;

public class ClassInfo extends Info {
	public ClassName className;
	public ArrayList<ClassName> expans;
	public ArrayList<Field> fields;
	public ArrayList<FieldInfo> fieldInfos;
	public ArrayList<MethodName> methodNames;
	public ArrayList<Identifier> identifiers;

	public ClassInfo(int line, ClassName className, ArrayList<ClassName> expans, ArrayList<FieldInfo> fieldInfos,
			ArrayList<MethodName> methodNames, ArrayList<Identifier> identifiers) {
		super(line);
		this.className = className;
		this.expans = expans;
		this.fieldInfos = fieldInfos;
		this.methodNames = methodNames;
		this.identifiers = identifiers;
		setFields();
	}

	private void setFields() {
		this.fields = new ArrayList<>();
		fieldInfos.forEach(info -> fields.add(info.field));
	}

	@Override
	public String toString() {
		return "ExtendInfo [line=" + line + ", className=" + className + ", expans=" + expans + "]";
	}
	
	
}
