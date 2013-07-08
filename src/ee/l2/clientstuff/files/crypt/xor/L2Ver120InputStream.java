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
package ee.l2.clientstuff.files.crypt.xor;

import java.io.*;

/**
 * @author acmi
 */
public class L2Ver120InputStream extends FilterInputStream {
    private int ind = L2Ver120.START_IND;

    public L2Ver120InputStream(InputStream input) {
        super(input);
    }

    @Override
    public int read() throws IOException {
        int b = in.read();
        return b < 0 ? b : b ^ L2Ver120.getXORKey(ind++);
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
