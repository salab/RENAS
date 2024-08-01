package main;

import util.Dic;

// based on trainSampleFromAddAbbrAndHResultFile
public class Step4_Expan {

    public static String LinsenAbbrDic(String part) {
        if (Dic.abbrDicHashMap.containsKey(part)) {
            return Dic.abbrDicHashMap.get(part);
        }
        return "";
    }
    public static String ComputerAbbrDic(String part) {
        if (Dic.computerAbbrDicHashMap.containsKey(part)) {
            return Dic.computerAbbrDicHashMap.get(part);
        }
        return "";
    }
}
