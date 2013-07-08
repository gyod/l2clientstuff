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

import ee.l2.clientstuff.files.FinishableOutputStream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author acmi
 */
public class L2Ver121OutputStream extends FinishableOutputStream {
    private OutputStream output;
    private int xorKey;

    public L2Ver121OutputStream(OutputStream output, int xorKey) {
        this.output = output;
        this.xorKey = xorKey;
    }

    @Override
    public void write(int b) throws IOException {
        output.write(b ^ xorKey);
    }

    @Override
    public void flush() throws IOException {
        output.flush();
    }

    @Override
    public void close() throws IOException {
        output.close();
    }
}
