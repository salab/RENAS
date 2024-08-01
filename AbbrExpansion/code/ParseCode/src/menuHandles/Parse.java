package menuHandles;

import expansion.AllExpansions;
//import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import util.Config;
import visitor.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class Parse {

	private String[] sourcepathEntries;
	private String[] sourcepaths;
	private Path root;

	public static void main(String[] args) {
		Path rootDir = FileSystems.getDefault()
				.getPath(args[0])
				.resolve("repo")
				.toAbsolutePath()
				.normalize();
		Config.outFile = Paths.get(args[0]).resolve("idTable.csv").toString();
		Parse parse = new Parse(rootDir);
		parse.parse();
	}

	public Parse(Path dir) {
		this.root = dir;
		try(Stream<Path> paths = Files.walk(dir)) {
			this.sourcepaths = paths
					.map(Path::toString)
					.filter(p -> p.toLowerCase().endsWith(".java"))
					.toList()
					.toArray(new String[0]);
			this.sourcepathEntries = getSourcepathEntries(sourcepaths);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void parse() {
		ASTParser parser = createParser();
		parser.createASTs(sourcepaths, null, new String[0], new FileHandler(root), null);
		AllExpansions.postprocess();
	}

	private String[] getSourcepathEntries(String[] sourcepathStrings) {
		Pattern pattern = Pattern.compile("(?<=^package ).+(?=;$)");
		HashSet<String> entries = new HashSet<>();
		for (String ss : sourcepathStrings) {
			Path ap = Paths.get(ss);
			Path fName = ap.getFileName();
			try (Stream<String> lines = Files.lines(ap)) {
				Matcher m = lines.map(pattern::matcher)
						.filter(Matcher::find)
						.findAny()
						.orElse(null);
				String entry;
				if (m != null) {
					String pName = m.group();
					String pPath = Paths.get("", pName.split("\\."))
							.resolve(fName)
							.toString();
					entry = ss.replace(pPath, "");
					if (entry.endsWith(".java")) { 
						entry = ap.getParent().toString();
					}
				} else {
					entry = ap.getParent().toString();
				}
				entries.add(entry);
			}	
			catch (UncheckedIOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			catch (IOException e) {
				e.printStackTrace();
				System.exit(1);

			}
		}
		return entries.toArray(new String[0]);
	}

	private ASTParser createParser() {
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_16);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_16);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_16);
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.VERSION_16);

		ASTParser parser = ASTParser.newParser(AST.JLS16);
		parser.setCompilerOptions(options);
		parser.setEnvironment(null, sourcepathEntries, null, true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		return parser;
	}

	private static class FileHandler extends FileASTRequestor {
		private Path root;
		FileHandler(Path root) {
			super();
			this.root = root;
		}

		@Override
		public void acceptAST(String sourceFilePath, CompilationUnit ast) {
			super.acceptAST(sourceFilePath, ast);
			Config.projectName = root.relativize(Paths.get(sourceFilePath)).toString();
			HandleOneFile resultOfOneFile = new HandleOneFile();
			ast.accept(new ClassVisitor(ast, resultOfOneFile));
			ast.accept(new MethodDeclarationVisitor(ast, resultOfOneFile));
			ast.accept(new MethodInvocationVisitor(ast, resultOfOneFile));
			ast.accept(new AssignVistor(ast, resultOfOneFile));
			// comment
			try {
				String[] source = Files.readString(Paths.get(sourceFilePath)).split("\n");
				for (Object object: ast.getCommentList()) {
					Comment comment = (Comment) object;
					comment.accept(new CommentVisitor(ast, source, resultOfOneFile));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			resultOfOneFile.parse();
		}
	}
}
