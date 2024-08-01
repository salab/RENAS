package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class Expansion {
    private final String abbreviation;
    private final String expansion;
    private final String partCase;
    private final Heu.Heuristic heuristic;
    public ArrayList<String> split;
    public ArrayList<String> expansions;
    public ArrayList<String> delimiter;
    public ArrayList<String> partCases;
    public ArrayList<String> heuristics;

    public Expansion(String abbreviation, String expansion, String partCase, Heu.Heuristic heuristic) {
        this.abbreviation = abbreviation;
        this.expansion = expansion;
        this.partCase = partCase;
        this.heuristic = heuristic;
        setExpansions();
        setSplit();
        setDelimiter();
        setCase();
        setHeuristics();
    }

    @Override
    public String toString() {
        return "Expansion["
                + "abbreviation=" + this.abbreviation
                + ", expansions=" + this.expansion
                + ", case=" + this.partCase
                + ", heuristic=" + this.heuristic
                + "]";
    }

    private void setExpansions() {
        expansions = new ArrayList<>(Arrays.asList(expansion.split("#")));
    }

    private void setSplit() {
        split = new ArrayList<>();
        if (expansions.size() > 1) {
            IntStream.range(0, expansions.size())
                    .forEach(i -> split.add(String.valueOf(abbreviation.charAt(i))));
        } else {
            split.add(abbreviation);
        }
    }

    private void setDelimiter() {
        delimiter = new ArrayList<>();
        IntStream.range(0, expansions.size()-1)
                .forEach(i -> delimiter.add(""));
    }

    private void setCase() {
        partCases = new ArrayList<>();
        int size = expansions.size();
        if (size > 1 && Util.Case.valueOf(partCase) == Util.Case.TITLE) {
            partCases.add(Util.Case.UPPER.name());
            IntStream.range(1, size)
                    .forEach(i -> partCases.add(Util.Case.LOWER.name()));
        } else {
            IntStream.range(0, size)
                    .forEach(i -> partCases.add(partCase));
        }
    }

    private void setHeuristics() {
        heuristics = new ArrayList<>();
        IntStream.range(0, expansions.size())
                .forEach(i -> heuristics.add(heuristic.name()));
    }
}
