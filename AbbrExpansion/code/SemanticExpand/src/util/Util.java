package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    private final static Pattern delimiter = Pattern.compile("[\\_\\$\\d]+");
    private final static Pattern upperCase = Pattern.compile("[A-Z]+");
    private final static Pattern lowerCase = Pattern.compile("[a-z]+");
    private final static Pattern titleCase = Pattern.compile("[A-Z][a-z]+");
    public enum Case {
        UPPER,
        LOWER,
        TITLE,
        UNKNOWN
    }

    public enum Relation {
        SC("subclass"),
        SSC("descendant"),
        PAREN("parent"),
        AN("ancestor"),
        ME("method"),
        FI("field"),
        CO("comment"),
        TY("type"),
        EN_C("enclosingClass"),
        AS("assignmentEquation"),
        ME_I("pass"),
        PARAM_A("argumentToParameter"),
        PARAM("parameter"),
        EN_M("enclosingMethod"),
        AR("parameterToArgument");

        private final String name;
        Relation(String name) {
            this.name = name;
        }

        public String toColumnName() {
            return name;
        }
    }

    public static abstract class Line {
        protected List<String> columns;
        public final String rawLine;
        private HashMap<String, String> content;

        public Line(String line) {
            rawLine = line;
            setColumns();
            setContent();
        }

        private void setContent() {
            content = new HashMap<>();
            String[] split = rawLine.split(",", -1);
            if (split.length != columns.size()) {
                System.err.println("Invalid line Size: " + split.length);
                System.err.println(rawLine);
            } else {
                for (int i = 0; i < columns.size(); i++) {
                    String value = split[i];
                    content.put(columns.get(i), value);
                }
            }
        }

        public String get(String key) {
            return content.get(key);
        }

        abstract protected void setColumns();
    }

    public static boolean isLetter(char c) {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNum(char c) {
        if (c >= '0' && c <= '9') {
            return true;
        } else {
            return false;
        }
    }

    public static void splitBigLetter(String str, String lastSep, HashMap<String, ArrayList<String>> map) {
        char[] decoratedList = ("A" + str + "A").toCharArray();
        char[] tmpList = new char[str.length()];

        for (int i = 1; i < decoratedList.length - 1; i++) {
            if (decoratedList[i] >= 'A' && decoratedList[i] <= 'Z' &&
                    decoratedList[i - 1] >= 'A' && decoratedList[i - 1] <= 'Z' &&
                    decoratedList[i + 1] >= 'A' && decoratedList[i + 1] <= 'Z') { // big letter
                tmpList[i - 1] = (char) (decoratedList[i] - 'A' + 'a');
            } else {
                tmpList[i - 1] = decoratedList[i];
            }
        }

        String tmpStr = new String(tmpList) + "A";
        int startPositionOfSubstring = 0;
        for (int endPositionOfSubstring = 0; endPositionOfSubstring < tmpStr.length(); endPositionOfSubstring++) {
            if (tmpStr.charAt(endPositionOfSubstring) >= 'A' && tmpStr.charAt(endPositionOfSubstring) <= 'Z') {
                // to exclude initial up case letter
                if (str.substring(startPositionOfSubstring, endPositionOfSubstring).length() > 0) {
                    // to lower case
                    String fragment = str.substring(startPositionOfSubstring, endPositionOfSubstring);
                    map.get("split").add(fragment.toLowerCase());
                    map.get("delimiter").add((endPositionOfSubstring == tmpStr.length() - 1) ? lastSep : "");
                    if (getCase(fragment).equals(Case.UNKNOWN.name())) {
                        System.err.println(str);
                    }
                    map.get("case").add(getCase(fragment));
                    startPositionOfSubstring = endPositionOfSubstring;
                }
            }
        }
    }

    private static String getCase(String str) {
        if (upperCase.matcher(str).matches()) {
            return Case.UPPER.name();
        } else if (titleCase.matcher(str).matches()) {
            return Case.TITLE.name();
        } else if (lowerCase.matcher(str).matches()) {
            return Case.LOWER.name();
        } else {
            System.err.println("Unknown Case Found: " + str);
            return Case.UNKNOWN.name();
        }
    }

    private static ArrayList<String> getPattern(HashMap<String, ArrayList<String>> map) {
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> cases = map.get("case");
        ArrayList<String> delims = map.get("delimiter");

        Case case0 = cases.size() > 0 ?
                Case.valueOf(cases.get(0)) :
                null;
        String[] remainCases = cases.size() > 1 ?
                Arrays.copyOfRange(cases.toArray(new String[0]), 1, cases.size()) :
                null;
        String[] centerDelims = delims.size() > 2 ?
                Arrays.copyOfRange(delims.toArray(new String[0]), 1, delims.size() - 1) :
                null;

        if (remainCases == null || Arrays.stream(remainCases).allMatch(c -> Case.valueOf(c) == Case.TITLE)) {
            if (case0 == Case.LOWER) {
                result.add("LCAMEL");
            } else if (case0 == Case.TITLE) {
                result.add("TCAMEL");
            }
        }
        if (centerDelims != null &&
                Arrays.stream(centerDelims).allMatch(d -> d.contains("_"))) {
            result.add("SNAKE");
        }

        return result;
    }

    // identifiers consist of
    // letters
    // numbers
    // _
    // $
    // not start with numbers
    // the size of return value may be 0 (e.g., $)
    public static HashMap<String, ArrayList<String>> split(String str) {
        String preStr = str;
        int temp1 = preStr.indexOf("<");
        int temp2 = preStr.lastIndexOf(">");

        if (temp1 != -1 && temp2 != -1) {
            preStr = preStr.substring(0, temp1) + preStr.substring(temp2 + 1);
        }
        temp1 = preStr.indexOf("[");
        temp2 = preStr.lastIndexOf("]");
        if (temp1 != -1 && temp2 != -1) {
            preStr = preStr.substring(0, temp1) + preStr.substring(temp2 + 1);
        }

        if (preStr.length() == 0) {
            System.err.println("error: split length 0");
            return null;
        }

        HashMap<String, ArrayList<String>> result = new HashMap<>();
        result.put("split", new ArrayList<>());
        result.put("delimiter", new ArrayList<>());
        result.put("case", new ArrayList<>());

        Matcher matcher = delimiter.matcher(preStr);
        int startIndex = 0;
        int endIndex = 0;
        while (endIndex != preStr.length()) {
            boolean found = matcher.find();
            endIndex = found ? matcher.start() : preStr.length();
            String delim = found ? matcher.group() : "";
            String sub = preStr.substring(startIndex, endIndex);
            if (startIndex == 0) {
                result.get("delimiter").add((endIndex == 0) ? delim : "");
            }
            splitBigLetter(sub, delim, result);
            startIndex = found ? matcher.end() : endIndex;
        }
        result.put("pattern", getPattern(result));

        return result;
    }

    public static boolean isSequence(String ori, String sequence) {
        int j = 0;
        if (ori.charAt(0) != sequence.charAt(0)) {
            return false;
        }
        for (int i = 0; i < ori.length(); i++) {
            if (j < sequence.length() && ori.charAt(i) == sequence.charAt(j)) {
                j++;
            }
        }
        return j == sequence.length();
    }

    // convert text file to ArrayList<String> line by line
    public static ArrayList<String> readFile(String fileName) {
        ArrayList<String> result = new ArrayList<String>();
        File file = new File(fileName);
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            String header = reader.readLine();
            while ((tempString = reader.readLine()) != null) {
                if (!tempString.equals("")) {
                    result.add(tempString);
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return result;
    }

    public static boolean equalOfWord(String str1, String str2) {
        str1 = str1.trim();
        str2 = str2.trim();
        str1 = str1.toLowerCase();
        str2 = str2.toLowerCase();

        if (str1.length() == 0 || str2.length() == 0) {
            return false;
        }

        if (str1.equals(str2)) {
            return true;
        } else {
            String str1Single = str1;
            String str2Single = str2;
            if (str1.charAt(str1.length()-1) == 's' &&
                    str1.length()>1) {
                str1Single = str1.substring(0, str1.length()-1);
            }
            if (str2.charAt(str2.length()-1) == 's' &&
                    str2.length()>1) {
                str2Single = str2.substring(0, str2.length()-1);
            }
            return str1Single.equals(str2) ||
                    str2Single.equals(str1) ||
                    str1Single.equals(str2Single);
        }
    }

    public static boolean equalComputerExpansion(String expansion, String dicExpansion) {
        expansion = expansion.trim();
        dicExpansion = dicExpansion.trim();
        expansion = expansion.toLowerCase();
        dicExpansion = dicExpansion.toLowerCase();
        if (expansion.length() == 0 || dicExpansion.length() == 0) {
            return false;
        }
        expansion = expansion.replace(" ", "");
        dicExpansion = dicExpansion.replace(" ", "");
        String[] dicExpansions = dicExpansion.split(Dic.computerAbbrDelimiter);
        for (int i = 0; i < dicExpansions.length; i++) {
            if (Util.equalOfWord(expansion, dicExpansions[i])) {
                return true;
            }
        }
        return false;
    }

    public static double sigmoid(double x) {
        return 1/(1+Math.exp(-x));
    }

    public static <K,V> void putHashMap(HashMap<K, V> map, K key, V value) {
        if (!map.containsKey(key)) {
            map.put(key, value);
        }
    }

    public static void main(String[] args) {
        // test readFile
        // test isSequence
        System.out.println(isSequence("abcd", "adc"));

        // test split
        String str1 = "getResult";
        System.out.println("split " + str1);
        System.out.println(split(str1));
        System.out.println();

        String str2 = "TLS_RSA_EXPORT1024_WITH_RC2_CBC_56_MD5";
        System.out.println("split " + str2);
        System.out.println(split(str2));
        System.out.println();

        System.out.println(sigmoid(-1));
    }
}		

