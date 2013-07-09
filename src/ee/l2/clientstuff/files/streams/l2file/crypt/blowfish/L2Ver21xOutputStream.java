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
package ee.l2.clientstuff.files.streams.l2file.crypt.blowfish;

import ee.l2.clientstuff.files.streams.FinishableOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author acmi
 */
public class L2Ver21xOutputStream extends FinishableOutputStream implements L2Ver21x{
    private OutputStream output;

    private BlowfishEngine blowfish = new BlowfishEngine();

    private byte[] writeBuffer = new byte[8];
    private ByteBuffer dataBuffer = ByteBuffer.allocate(8);

    private boolean finished;

    public L2Ver21xOutputStream(OutputStream output, byte[] key) {
        this.output = output;

        blowfish.init(true, key);
    }

    @Override
    public void write(int b) throws IOException {
        if (finished)
            throw new IOException("write beyond end of stream");

        dataBuffer.put((byte) b);
        if (dataBuffer.position() == dataBuffer.limit()) {
            writeData();

            dataBuffer.clear();
        }
    }

    @Override
    public void flush() throws IOException {
        output.flush();
    }

    @Override
    public void finish() throws IOException {
        if (finished)
            return;

        finished = true;
        writeData();
        flush();
    }

    @Override
    public void close() throws IOException {
        super.close();

        output.close();
    }

    private void writeData() throws IOException {
        if (dataBuffer.position() == 0)
            return;

        Arrays.fill(dataBuffer.array(), dataBuffer.position(), dataBuffer.limit(), (byte)0);
        blowfish.processBlock(dataBuffer.array(), dataBuffer.arrayOffset(), writeBuffer, 0);
        output.write(writeBuffer);
    }
}
