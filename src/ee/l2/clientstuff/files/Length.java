package ee.l2.clientstuff.files;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author acmi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Length {
    public int value() default 0;

    public LengthType lengthType() default LengthType.INT;

    public String sameAs() default "";

    public int add() default 0;
}
