package ee.l2.clientstuff.files.compiler;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.net.URI;

/**
 * @author acmi
 */
public class StringSource extends SimpleJavaFileObject {
    private final String className;
    private final String code;

    public StringSource(String className, String code) {
        super(URI.create("string:///" + className.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);

        this.className = className;
        this.code = code;
    }

    public String getClassName(){
        return className;
    }

    public String getCode(){
        return code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return code;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"["+className+"]";
    }
}
