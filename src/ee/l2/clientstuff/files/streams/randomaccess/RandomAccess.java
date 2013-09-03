package ee.l2.clientstuff.files.streams.randomaccess;

import java.io.IOException;

/**
 * @author acmi
 */
public interface RandomAccess {
    public abstract int position() throws IOException;

    public abstract void seek(int pos) throws IOException;
}
