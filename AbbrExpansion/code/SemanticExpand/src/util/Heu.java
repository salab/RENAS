package util;

import java.util.ArrayList;
import java.util.Comparator;

public class Heu {
    public enum Heuristic {
        ST("stable"),
        H1("acronym"),
        H2("prefix"),
        H3("dropped-letter");

        public final String description;
        Heuristic(String description) {
            this.description = description;
        }
    }

    public static String H1(String abbr, ArrayList<String> terms) {
        if (abbr.length() == 1) {
            return null;
        }
        if (abbr.length() > terms.size()) {
            return null;
        }

        for (int i = 0; i <= terms.size()-abbr.length(); i++) {
            StringBuilder ics = new StringBuilder();

            for (int j = i; j < abbr.length()+i; j++) {
                String term = terms.get(j);
                ics.append(term.charAt(0));
            }
            if (abbr.equals(ics.toString())) {
                StringBuilder temp = new StringBuilder();

                for (int j = i; j < abbr.length()+i; j++) {
                    String term = terms.get(j);
                    if (!Dic.isInDict(term)) {
                        // expand fail
                        return null;
                    }
                    temp.append(term).append("#");
                }

                return temp.toString();
            }
        }
        return null;
    }

    public static String H2(String abbr, ArrayList<String> terms) {
        if (H1(abbr, terms) != null) {
            return null;
        }
        ArrayList<String> possibleExpansions = new ArrayList<>();
        for (String term : terms) {
            if (term.startsWith(abbr) && Dic.isInDict(term)) {
                possibleExpansions.add(term);
            }
        }
        if (possibleExpansions.size() == 0) {
            // expand fail
            return null;
        }
        return possibleExpansions
                .stream()
                .min(Comparator.comparingInt(String::length))
                .orElse(null);
    }

    public static String H3(String abbr, ArrayList<String> terms) {
        if (H1(abbr, terms) != null || H2(abbr, terms) != null) {
            return null;
        }
        if (abbr.length() <= 1) {
            return null;
        }

        ArrayList<String> possibleExpansions = new ArrayList<String>();
        for (String term : terms) {
            if (Util.isSequence(term, abbr) && Dic.isInDict(term)) {
                possibleExpansions.add(term);
            }
        }
        if (possibleExpansions.size() == 0) {
            return null;
        }

        return possibleExpansions
                .stream()
                .min(Comparator.comparingInt(String::length))
                .orElse(null);
    }

    public static ArrayList<String> handleCommentForH(String part, String comment, String H) {
        ArrayList<String> result = new ArrayList<>();
        comment = comment.replaceAll(";", " ");
        String[] parts = comment.split(" ");

        ArrayList<String> dicWordList = new ArrayList<>();
        for (String s : parts) {
            if (s.length() != 0 && Dic.isInDict(s)) {
                dicWordList.add(s);
            }
        }

        switch (H) {
            case "H1":
                if (dicWordList.size() >= part.length()) {
                    for (int i = 0; i <= dicWordList.size() - part.length(); i++) {
                        boolean flag = true;
                        StringBuilder expansion = new StringBuilder();
                        for (int j = 0; j < part.length(); j++) {
                            if (part.charAt(j) != dicWordList.get(i + j).charAt(0)) {
                                flag = false;
                                break;
                            }
                            expansion.append(dicWordList.get(i + j));
                        }
                        if (flag) {
                            result.add(expansion.toString());
                        }
                    }
                }
                break;
            case "H2":
                for (String value : parts) {
                    if (value.length() != 0) {
                        String expansion = Heu.H2(part, Util.split(value).get("split"));
                        if (expansion != null) {
                            result.add(expansion);
                        }
                    }
                }
                break;
            case "H3":
                for (String s : parts) {
                    if (s.length() != 0) {
                        String expansion = Heu.H3(part, Util.split(s).get("split"));
                        if (expansion != null) {
                            result.add(expansion);
                        }
                    }
                }
                break;
        }
        return result;
    }

    public static ArrayList<String> handleExpansionForH(String part, String candidate, String H) {
        ArrayList<String> result = new ArrayList<>();
        String[] identifiers = candidate.split(" - ");
        for (int j = 0; j < identifiers.length; j++) {
            // may not contain the name of identifier
            if (identifiers[j].split(":").length != 2) {
                continue;
            }
            String nameOfIdentifier = identifiers[j].split(":")[1];
            String expansion = null;

            switch (H) {
                case "H1":
                    expansion = Heu.H1(part, Util.split(nameOfIdentifier).get("split"));
                    break;
                case "H2":
                    expansion = Heu.H2(part, Util.split(nameOfIdentifier).get("split"));
                    break;
                case "H3":
                    expansion = Heu.H3(part, Util.split(nameOfIdentifier).get("split"));
                    break;
            }

            if (expansion != null) {
                result.add(expansion);
            } else { // plural
                if (part.length() > 1 && part.charAt(part.length()-1) == 's') {
                    String singlePart = part.substring(0, part.length()-1);
                    switch (H) {
                        case "H1":
                            expansion = Heu.H1(singlePart, Util.split(nameOfIdentifier).get("split"));
                            break;
                        case "H2":
                            expansion = Heu.H2(singlePart, Util.split(nameOfIdentifier).get("split"));
                            break;
                        case "H3":
                            expansion = Heu.H3(singlePart, Util.split(nameOfIdentifier).get("split"));
                            break;
                    }
                    if (expansion != null) {
                        result.add(expansion);
                    }
                }
            }
        }
        return result;
    }

    public static boolean H1EqualOf(String selfExpansion, String HExpansion) {
        selfExpansion = selfExpansion.trim();
        HExpansion = HExpansion.trim();
        selfExpansion = selfExpansion.toLowerCase();
        HExpansion = HExpansion.toLowerCase();
        if (selfExpansion.length() == 0 || HExpansion.length() == 0) {
            return false;
        }
        selfExpansion = selfExpansion.replace(" ", "");
        HExpansion = HExpansion.replace(" ", "");

        String[] HExpansions = HExpansion.split(";");
        for (int i = 0; i < HExpansions.length; i++) {
            if (Util.equalOfWord(selfExpansion, HExpansions[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean H2H3EqualOf(String selfExpansion, String HExpansion) {
        selfExpansion = selfExpansion.trim();
        HExpansion = HExpansion.trim();
        selfExpansion = selfExpansion.toLowerCase();
        HExpansion = HExpansion.toLowerCase();
        if (selfExpansion.length() == 0 || HExpansion.length() == 0) {
            return false;
        }
        selfExpansion = selfExpansion.replace(" ", "");
        HExpansion = HExpansion.replace(" ", "");
        String[] HExpansions = HExpansion.split(";");
        for (int i = 0; i < HExpansions.length; i++) {
            String[] expansions = HExpansions[i].split("#");
            for (int j = 0; j < expansions.length; j++) {
                if (Util.equalOfWord(selfExpansion, expansions[j])) {
                    return true;
                }
            }
        }
        return false;
    }
}

