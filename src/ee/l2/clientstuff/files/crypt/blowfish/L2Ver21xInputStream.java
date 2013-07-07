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
package ee.l2.clientstuff.files.crypt.blowfish;

import java.io.*;

/**
 * @author acmi
 */
public class L2Ver21xInputStream extends FilterInputStream implements L2Ver21x{
    private BlowfishEngine blowfish = new BlowfishEngine();

    private byte[] readBuffer = new byte[8];
    private byte[] dataBuffer = new byte[8];
    private int pos = dataBuffer.length - 1;

    public L2Ver21xInputStream(InputStream input, byte[] key){
        super(input);

        blowfish.init(false, key);
    }

    @Override
    public int read() throws IOException {
        if (pos == dataBuffer.length - 1) {
            int r = in.read(readBuffer);
            if (r != readBuffer.length)
                return -1;

            blowfish.processBlock(readBuffer, 0, dataBuffer, 0);
        }
        pos++;
        pos &= 0b111;
        return dataBuffer[pos];
    }

    @Override
    public synchronized void mark(int readlimit) {}

    @Override
    public synchronized void reset() throws IOException {}

    @Override
    public boolean markSupported() {
        return false;
    }
}
