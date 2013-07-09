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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * @author acmi
 */
public class BufferedRandomAccessInputStream extends RandomAccessInputStream {
    private InputStream input;

    private static int bufferSizeIncrement = 0x1000000;  //16MB

    private ByteBuffer buffer = ByteBuffer.allocate(bufferSizeIncrement);
    {
        buffer.limit(0);
    }

    public BufferedRandomAccessInputStream(InputStream input) {
        this.input = Objects.requireNonNull(input);
    }

    @Override
    public int read() throws IOException {
        if (buffer == null)
            throw new IOException("Stream closed");

        if (buffer.position() == buffer.capacity()) {
            ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() + bufferSizeIncrement);
            buffer.flip();
            newBuffer.put(buffer);
            newBuffer.limit(newBuffer.position());
            buffer = newBuffer;
        }

        if (buffer.position() == buffer.limit()) {
            int b = input.read();

            if (b < 0)
                return b;

            buffer.limit(buffer.limit() + 1);
            buffer.put((byte) b);
            return b;
        }

        return buffer.get() & 0xff;
    }

    @Override
    public void seek(int pos) throws IOException {
        if (buffer == null)
            throw new IOException("Stream closed");

        if (buffer.limit() < pos){
            buffer.position(buffer.limit());

            int remaining = pos - buffer.position();
            int r;
            byte[] skipBuffer = new byte[remaining];
            while(remaining > 0){
                r = read(skipBuffer, 0, remaining);
                if (r < 0)
                    throw new IOException("End of stream reached");
                remaining -= r;
            }
        }

        buffer.position(pos);
    }

    @Override
    public int position() throws IOException{
        if (buffer == null)
            throw new IOException("Stream closed");

        return buffer.position();
    }

    @Override
    public int available() throws IOException {
        if (buffer == null)
            throw new IOException("Stream closed");

        return buffer.limit()-buffer.position();
    }

    @Override
    public void close() throws IOException {
        if (buffer == null)
            return;

        buffer = null;
        input.close();
    }
}

