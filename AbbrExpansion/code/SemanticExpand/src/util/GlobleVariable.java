package util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class GlobleVariable {
    public static Path dicRoot = Paths.get(System.getProperty("user.dir")).resolve("dic");
    public static String englishDicFile = dicRoot.resolve("EnglishDic.txt").toString();
    public static String abbrDicFile = dicRoot.resolve("abbrDic.txt").toString();
    public static String computerAbbrDicFile = dicRoot.resolve("computerAbbr.txt").toString();

    public static String parseResultFile;
    public static String addAbbrAndHResultFile ;


}
