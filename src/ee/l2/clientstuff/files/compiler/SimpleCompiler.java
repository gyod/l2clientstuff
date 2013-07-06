package ee.l2.clientstuff.files.compiler;

import javax.tools.*;
import java.io.*;
import java.util.*;

/**
 * @author acmi
 */
public class SimpleCompiler {
    private File outputFolder;
    private JavaCompiler compiler;

    public SimpleCompiler(File outputFolder) {
        compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null)
            throw new RuntimeException("Compiler is not available");

        this.outputFolder = outputFolder;
        outputFolder.mkdir();
    }

    public void compile(Iterable<JavaFileObject> sources) throws IOException{
        JavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.getDefault(), null);
        Iterable<? extends JavaFileObject> compilationUnits = sources;
        Iterable<String> options = Arrays.asList(new String[]{
                "-d", outputFolder.getAbsolutePath()
        });
        DiagnosticCollector diagnosticListener = new DiagnosticCollector();
        JavaCompiler.CompilationTask compilerTask = compiler.getTask(null, fileManager, diagnosticListener, options, null, compilationUnits);
        boolean status = compilerTask.call();
        if (!status) {
            for (Iterator<Diagnostic> it = diagnosticListener.getDiagnostics().iterator(); it.hasNext(); ) {
                Diagnostic diagnostic = it.next();
                System.err.format("Error on line %d in %s", diagnostic.getLineNumber(), diagnostic);
            }
        }
        fileManager.close();
    }

    public void compile(File inputFolder) throws IOException {
        List<JavaFileObject> sources = new ArrayList<>();
        find(inputFolder, inputFolder.getPath().length() + 1, sources);
        compile(sources);
    }

    private void find(File inputFolder, int n, Collection<JavaFileObject> col) throws IOException {
        for (File file : inputFolder.listFiles()) {
            if (file.isDirectory()){
                find(file, n, col);
            }else if (file.getName().endsWith(".java")){
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String packageName = file.getParent().substring(n).replaceAll("\\"+File.separator, ".");
                    boolean datClass = false;
                    String className = null;
                    StringBuilder code = null;
                    String tmp;
                    while ((tmp = br.readLine()) != null) {
                        tmp = tmp.trim();
                        if (tmp.isEmpty())
                            continue;

                        if (tmp.matches("package.+")) {
                            packageName = tmp.substring(8, tmp.indexOf(';')).trim();
                            continue;
                        }

                        if (code == null) {
                            className = "";
                            code = new StringBuilder();
                            if (!packageName.isEmpty())
                                code.append("package " + packageName + ";\n\n");
                            code.append("import ee.l2.clientstuff.files.*;\n");
                            code.append("import ee.l2.clientstuff.files.dat.*;\n");
                            code.append("import javax.xml.bind.annotation.*;\n");
                            code.append("\n");
                            datClass = false;
                        }

                        if (tmp.contains("@DatFile"))
                            datClass = true;


                        if (tmp.matches("\\w*class \\w+\\s*\\{")) {
                            className = tmp.substring(tmp.indexOf("class") + 5, tmp.indexOf('{')).trim();
                            if (!packageName.isEmpty())
                                className = packageName + "." + className;

                            tmp = "public " + tmp;

                            if (datClass)
                                tmp = "@XmlRootElement\n" + tmp;
                        }
                        if (tmp.contains(";")) {
                            tmp = "\tpublic " + tmp;
                        }
                        code.append(tmp);

                        if (tmp.equals("}")) {
                            JavaFileObject src = new StringSource(className, code.toString());
                            col.add(src);
                            code = null;
                        } else {
                            code.append('\n');
                        }
                    }
                } catch (IOException e) {
                    System.err.println(e.getClass()+": "+e.getMessage());
                }
            }
        }
    }
}
