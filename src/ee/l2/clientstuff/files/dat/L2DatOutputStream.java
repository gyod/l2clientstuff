package ee.l2.clientstuff.files.dat;

import ee.l2.clientstuff.files.L2DataOutputStream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author acmi
 */
public class L2DatOutputStream extends L2DataOutputStream{
    public L2DatOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void writeObject(Object obj) throws IOException, ReflectiveOperationException {
        super.writeObject(obj);

        if (obj.getClass().isAnnotationPresent(SafePackage.class))
            writeLine("SafePackage");
    }
}
