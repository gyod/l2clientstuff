package ee.l2.clientstuff.files.dat;

import ee.l2.clientstuff.files.L2DataInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author acmi
 */
public class L2DatInputStream extends L2DataInputStream{
    public L2DatInputStream(InputStream input) {
        super(input);
    }

    @Override
    public <T> T readObject(Class<T> clazz) throws IOException, ReflectiveOperationException {
        T obj = super.readObject(clazz);

        if (clazz.isAnnotationPresent(SafePackage.class) &&
                !"SafePackage".equals(readLine()))
            throw new IOException("\"SafePackage\" expected");

        return obj;
    }
}
