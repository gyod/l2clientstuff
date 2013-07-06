package ee.l2.clientstuff.files.dat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author acmi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DatFile {
    /**
     * Name of dat file.
     */
    public String value();
    /**
     * Dat file contains "-*" suffix.
     */
    public boolean localizable() default false;
}
