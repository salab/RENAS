package main;

import org.json.JSONArray;
import org.json.JSONObject;
import util.*;

import javax.swing.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.IntStream;


public class  Step1_AddAbbrAndH {
    public static HashMap<Heu.Heuristic, HashMap<String, String>> expansionRecord;
    public static HashMap<String, HashMap<String, Integer>> expansionClassRecord;

    static {
        expansionRecord = new HashMap<>();
        expansionRecord.put(Heu.Heuristic.H2, new HashMap<>());
        expansionRecord.put(Heu.Heuristic.H3, new HashMap<>());

        expansionClassRecord = new HashMap();
    }

    public static void main(String[] args) throws FileNotFoundException {
        GlobleVariable.parseResultFile = Paths.get(args[0], "idTable.csv").toString();
        System.out.println(GlobleVariable.parseResultFile);
        GlobleVariable.addAbbrAndHResultFile = Paths.get(args[0], "exTable.csv").toString();
        ArrayList<ParseResultLine> lines = readParseResult(GlobleVariable.parseResultFile);

        handleParseReult(lines);
        exportExpansionRecord(Paths.get(args[0], "record.json").toString());
        exportExpansionClassRecord(Paths.get(args[0], "classRecord.json").toString());
    }

    public static class ParseResultLine extends Util.Line {
        private static final List<String> columns = Arrays.asList(
                "id",
                "files",
                "line",
                "name",
                "typeOfIdentifier",
                "subclass",
                "descendant",
                "parent",
                "ancestor",
                "method",
                "field",
                "sibling-members",
                "comment",
                "type",
                "enclosingClass",
                "assignmentEquation",
                "pass",
                "argumentToParameter",
                "parameter",
                "enclosingMethod",
                "parameterToArgument"
        );

        public ParseResultLine(String line) {
            super(line);
        }

        public static void printHeader() {
            System.out.println(String.join(",", columns.toArray(new String[0])));
        }

        @Override
        protected void setColumns() {
            super.columns = columns;
        }

        public Expansion handleExpansion(String part, String stringCase) {
            HashMap<String, Integer> collectedCandidates = new HashMap<>();
            HashMap<String, ArrayList<Heu.Heuristic>> heuristics = new HashMap<>();
            for (Util.Relation relation : Util.Relation.values()) {
                String candidate = get(relation.toColumnName());
                if (candidate == null) {
                    continue;
                }
                if (relation == Util.Relation.CO) {
                    Step1_AddAbbrAndH.handleComment(part, candidate, collectedCandidates, heuristics);
                } else {
                    Step1_AddAbbrAndH.handleExpansion(part, candidate, collectedCandidates, heuristics);
                }
                //Step1_AddAbbrAndH.handleDic(part, collectedCandidates, heuristics);
            }

            if (collectedCandidates.isEmpty()) {
                Step1_AddAbbrAndH.handleDic(part, collectedCandidates, heuristics);
            }

            if (collectedCandidates.isEmpty()) {
                return new Expansion(part, part, stringCase, Heu.Heuristic.ST);
            } else {
                int maxCount = Collections.max(collectedCandidates.values());
                String expanded = collectedCandidates.keySet().stream()
                        .filter(c -> collectedCandidates.get(c) == maxCount)
                        .min(Comparator.comparingInt(String::length))
                        .orElse("");
                Heu.Heuristic heuristic = heuristics.get(expanded).get(0);
                recordExpansion(part, expanded, heuristic);
                recordClassExpansion(part, expanded, get("files"));
                return new Expansion(part, expanded, stringCase, heuristic);
            }
        }
    }

    private static void recordExpansion(String part, String expanded, Heu.Heuristic heuristic) {
        switch (heuristic) {
            case H2 -> Util.putHashMap(expansionRecord.get(Heu.Heuristic.H2), expanded, part);
            case H3 -> Util.putHashMap(expansionRecord.get(Heu.Heuristic.H3), expanded, part);
        }
    }

    private static void recordClassExpansion(String part, String expanded, String className){
        if (!expansionClassRecord.containsKey(className)){
            expansionClassRecord.put(className, new HashMap<>());
        }
        expanded = expanded.replaceAll("#", " ").replaceAll( " *$", "" );;
        String key = part+"=="+expanded;
        if (!expansionClassRecord.get(className).containsKey(key)){
            expansionClassRecord.get(className).put(key, 1);
        }
        else{
            int value = expansionClassRecord.get(className).get(key);
            expansionClassRecord.get(className).put(key, value+1);
        }
    }

    private static void handleParseReult(ArrayList<ParseResultLine> lines) {
        StringBuilder sb = new StringBuilder();

        sb.append(exportHeader()).append("\n");
        for (ParseResultLine line : lines) {
            if (Objects.equals(line.get("line"), "-1")) {
                continue;
            }
            String name = line.get("name");
            ArrayList<Expansion> expanded = new ArrayList<>();

            HashMap<String, ArrayList<String>> map = Util.split(name);
            if (map != null) {
                ArrayList<String> parts = map.get("split");
                ArrayList<String> cases = map.get("case");
                int length = parts.size();

                for (int i = 0; i < length; i++) {
                    String p = parts.get(i);
                    String c = cases.get(i);
                    if (!Dic.isInDict(p)) {
                        expanded.add(line.handleExpansion(p, c));
                    } else {
                        expanded.add(new Expansion(p, p, c, Heu.Heuristic.ST));
                    }
                }
                sb.append(exportLine(line, expanded, map)).append("\n");
            } else {
                sb.append(exportLine(line, expanded, null)).append("\n");
            }
        }
        try (FileWriter fw = new FileWriter(GlobleVariable.addAbbrAndHResultFile, false)) {
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append(sb);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            System.err.println("Cannot export csv");
            e.printStackTrace();
        }
    }

    private static void handleExpansion(String part,
                                        String candidate,
                                        HashMap<String, Integer> collectedCandidates,
                                        HashMap<String, ArrayList<Heu.Heuristic>> collectedHeuristics) {
        collectCandidates(Heu.handleExpansionForH(part, candidate, "H1"), Heu.Heuristic.H1, collectedCandidates, collectedHeuristics);
        collectCandidates(Heu.handleExpansionForH(part, candidate, "H2"), Heu.Heuristic.H2, collectedCandidates, collectedHeuristics);
        collectCandidates(Heu.handleExpansionForH(part, candidate, "H3"), Heu.Heuristic.H3, collectedCandidates, collectedHeuristics);
    }

    private static void collectCandidates(ArrayList<String> candidates,
                                          Heu.Heuristic heuristic,
                                          HashMap<String, Integer> collectedCandidates,
                                          HashMap<String, ArrayList<Heu.Heuristic>> collectedHeuristics) {
        for (String c : candidates) {
            if (collectedCandidates.containsKey(c)) {
                collectedCandidates.put(c, collectedCandidates.get(c) + 1);
            } else  {
                collectedCandidates.put(c, 1);
            }
            if (collectedHeuristics.containsKey(c)) {
                collectedHeuristics.get(c).add(heuristic);
            } else {
                ArrayList<Heu.Heuristic> h = new ArrayList<>();
                h.add(heuristic);
                collectedHeuristics.put(c, h);
            }
        }
    }

    private static void handleComment(String part,
                                      String comment,
                                      HashMap<String, Integer> collectedCandidates,
                                      HashMap<String, ArrayList<Heu.Heuristic>> collectedHeuristics) {
        collectCandidates(Heu.handleCommentForH(part, comment, "H1"), Heu.Heuristic.H1, collectedCandidates, collectedHeuristics);
        collectCandidates(Heu.handleCommentForH(part, comment, "H2"), Heu.Heuristic.H2, collectedCandidates, collectedHeuristics);
        collectCandidates(Heu.handleCommentForH(part, comment, "H3"), Heu.Heuristic.H3, collectedCandidates, collectedHeuristics);
    }

    private static void handleDic(String part,
                                  HashMap<String, Integer> collectedCandidates,
                                  HashMap<String, ArrayList<Heu.Heuristic>> collectedHeuristics) {
        
        String expan =Step4_Expan.LinsenAbbrDic(part).replaceAll(" ", "_");
        if (expan.equals("")){
            return;
        }
        expan = ":" + expan;
        collectCandidates(Heu.handleExpansionForH(part, expan, "H1"), Heu.Heuristic.H1, collectedCandidates, collectedHeuristics);
        collectCandidates(Heu.handleExpansionForH(part, expan, "H2"), Heu.Heuristic.H2, collectedCandidates, collectedHeuristics);
        collectCandidates(Heu.handleExpansionForH(part, expan, "H3"), Heu.Heuristic.H3, collectedCandidates, collectedHeuristics);
    }

    private static void exportExpansionRecord(String outputPath) {
        JSONObject jsonObject = new JSONObject(expansionRecord);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath))) {
            jsonObject.write(bw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void exportExpansionClassRecord(String outputPath) {
        JSONObject jsonObject = new JSONObject(expansionClassRecord);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath))) {
            jsonObject.write(bw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<ParseResultLine> readParseResult(String fileName) {
        ArrayList<ParseResultLine> line = new ArrayList<>();
        ArrayList<String> rawLines = Util.readFile(fileName);
        rawLines.forEach(l -> line.add(new ParseResultLine(l)));
        return line;
    }

    private static String exportHeader() {
        ArrayList<String> header = new ArrayList<>(ParseResultLine.columns);
        header.add("split");
        header.add("delimiter");
        header.add("case");
        header.add("pattern");
        header.add("heuristic");
        header.add("expanded");
        return String.join(",", header.toArray(new String[0]));
    }

    private static String exportLine(ParseResultLine line,
                                   ArrayList<Expansion> expansions,
                                   HashMap<String, ArrayList<String>> map
                                   ) {
        ArrayList<String> content = new ArrayList<>();
        for (String column : ParseResultLine.columns) {
            content.add(line.get(column));
        }
        if (expansions.size() > 0) {
            ArrayList<String> split = new ArrayList<>();
            ArrayList<String> delimiters = new ArrayList<>();
            ArrayList<String> partCases = new ArrayList<>();
            ArrayList<String> heuristics = new ArrayList<>();
            ArrayList<String> expanded = new ArrayList<>();

            int length = expansions.size();
            ArrayList<String> delim = map.get("delimiter");

            delimiters.add(delim.get(0));
            for (int i = 0; i < length; i++) {
                Expansion expansion = expansions.get(i);
                split.addAll(expansion.split);

                delimiters.addAll(expansion.delimiter);
                delimiters.add(delim.get(i + 1));

                partCases.addAll(expansion.partCases);
                heuristics.addAll(expansion.heuristics);
                expanded.addAll(expansion.expansions);
            }

            content.add(String.join(";", split.toArray(new String[0])));
            content.add(String.join(";", delimiters.toArray(new String[0])));
            content.add(String.join(";", partCases.toArray(new String[0])));
            content.add(String.join(";", map.get("pattern").toArray(new String[0])));
            content.add(String.join(";", heuristics.toArray(new String[0])));
            content.add(String.join(";", expanded.toArray(new String[0])));
        } else {
            for (int i = 0; i < 6; i++) {
                content.add("");
            }
        }
        if (content.size() != ParseResultLine.columns.size() + 6) {
            System.err.println("content size is invalid: " + content.size());
            System.err.println(content);
        }
        return String.join(",", content.toArray(new String[0]));
    }
}
