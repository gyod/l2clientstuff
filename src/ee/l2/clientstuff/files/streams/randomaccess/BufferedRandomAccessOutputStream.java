/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ee.l2.clientstuff.files.streams.randomaccess;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * @author acmi
 */
public class BufferedRandomAccessOutputStream extends RandomAccessOutputStream{
    private static int bufferSizeIncrement = 0x1000000;  //16MB

    private ByteBuffer buffer = ByteBuffer.allocate(bufferSizeIncrement);
    {
        buffer.limit(0);
    }

    public BufferedRandomAccessOutputStream(OutputStream output){
        super(Objects.requireNonNull(output));
    }

    @Override
    public int position() throws IOException {
        if (buffer == null)
            throw new IOException("Stream closed");

        return buffer.position();
    }

    @Override
    public void seek(int pos) throws IOException {
        if (buffer == null)
            throw new IOException("Stream closed");

        if (buffer.limit() < pos){
            buffer.position(buffer.limit());

            write(new byte[pos - buffer.position()]);
        }

        try{
            buffer.position(pos);
        }catch (IllegalArgumentException e){
            throw new IOException(e);
        }
    }

    @Override
    public void write(int b) throws IOException {
        if (buffer == null)
            throw new IOException("Stream closed");

        if (buffer.position() == buffer.capacity()){
            ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity()+bufferSizeIncrement);
            newBuffer.put(buffer.array());
            newBuffer.limit(buffer.position());
            buffer = newBuffer;
        }

        if (buffer.position() == buffer.limit())
            buffer.limit(buffer.limit()+1);

        buffer.put((byte)b);
    }

    @Override
    public void flush() throws IOException {
        if (buffer == null)
            throw new IOException("Stream closed");

        out.flush();
    }

    @Override
    public void finish() throws IOException {
        if (buffer == null)
            throw new IOException("Stream closed");

        write(buffer.array(), 0, buffer.limit());
        flush();
    }

    @Override
    public void close() throws IOException {
        if (buffer == null)
            return;

        buffer = null;
        super.close();
    }
}
