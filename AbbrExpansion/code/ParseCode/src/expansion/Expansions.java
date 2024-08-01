package expansion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public abstract class Expansions {
    public String type;
    protected List<String> expansionKey;
    protected HashMap<String, HashSet<String>> expansions;

    Expansions() {
        expansions = new HashMap<>();
        setType();
        setKey();
        initExpansions();
    }

    public HashSet<String> getExpansions(String key) {
        if (expansions.containsKey(key)) {
            return expansions.get(key);
        } else {
            return null;
        }
    }

    public boolean setExpansions(String key, String value) {
        if (expansions.containsKey(key)) {
            return expansions.get(key).add(value);
        } else {
            return false;
        }
    }

    private void initExpansions() {
        for (String k : expansionKey) {
            expansions.put(k, new HashSet<>());
        }
    }

    abstract protected void setType();
    abstract protected void setKey();
}
