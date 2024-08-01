package util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.Expression;

import entity.Identifier;
import org.osgi.framework.Configurable;
import visitor.SimpleVisitor;

public class Util {	

	public static void main(String[] args) {
		System.out.println("sss");
		StringBuilder sb = new StringBuilder();
		appendString(sb, "1");
		appendString(sb, "2");
		appendString(sb, "3");
	}
	public static boolean isLetter(char c) {
		if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
			return true;
		} else {
			return false;
		}
	}
	public static void put(HashMap<String, ArrayList<String>> hashMap, String key, String value) {
		if (hashMap.containsKey(key)) {
			hashMap.get(key).add(value);
		} else {
			ArrayList<String> arrayList = new ArrayList<>();
			arrayList.add(value);
			hashMap.put(key,arrayList);
		}
	}
	public static void putHashSet(HashMap<String, HashSet<String>> hashMap, String key, String value) {
		if (hashMap.containsKey(key)) {
			hashMap.get(key).add(value);
		} else {
			HashSet<String> arrayList = new HashSet<>();
			arrayList.add(value);
			hashMap.put(key,arrayList);
		}
	}

	public static ArrayList<Identifier> parseExpression(Expression expression) {
		if (expression == null) {
			return null;
		}
		SimpleVisitor simpleVisitor = new SimpleVisitor();
		expression.accept(simpleVisitor);
		return simpleVisitor.identifiers;
	}

	public static void appendString(StringBuilder sb,  String line) {
		sb.append(line);
	}

	public static void exportToFile(StringBuilder sb) {
		BufferedWriter fw;
		try {
			System.out.println("export " + Config.outFile);
			File f = new File(Config.outFile);
			fw = new BufferedWriter(new FileWriter(f));
			fw.append(sb);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			System.err.println("cannot export csv");
			e.printStackTrace();
		}
	}

	public static void appendString(String line, String fileName) {
		FileWriter fw = null;
		try {
			File f=new File(fileName);

			fw = new FileWriter(f, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.print(line);
		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
