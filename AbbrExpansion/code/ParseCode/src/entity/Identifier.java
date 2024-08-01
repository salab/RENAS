package entity;

import relation.Info;

import java.util.HashMap;
import java.util.HashSet;

/**
 * The base class of identifiers, whose subclasses include 
 * ClassName, MethodName, Variable, Parameter (a special type of Variable)
 */
public abstract class Identifier {
	public String id;
	public String name;
	public String type;
	public Identifier(String id, String name) {
		super();
		this.id = id;
		this.name = name;
		setType();
	}
	@Override
	public String toString() {
		return "Identifier [id=" + id + ", name=" + name + "]";
	}

	abstract protected void setType();

	public void putToIdMap(HashMap<String, Identifier> map) {
		map.put(id, this);
	}
	
	public LocatedIdentifier locate(HashMap<String, Info> info) {
		if (info.containsKey(id)) {
			return new LocatedIdentifier(this, info.get(id).line);
		} else {
			return new LocatedIdentifier(this, -1);
		}
	}
}
