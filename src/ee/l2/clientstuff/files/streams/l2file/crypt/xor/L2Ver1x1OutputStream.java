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
package ee.l2.clientstuff.files.streams.l2file.crypt.xor;

import ee.l2.clientstuff.files.streams.FinishableOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * @author acmi
 */
public class L2Ver1x1OutputStream extends FinishableOutputStream {
    private int xorKey;

    public L2Ver1x1OutputStream(OutputStream output, int xorKey) {
        super(Objects.requireNonNull(output));
        this.xorKey = xorKey;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b ^ xorKey);
    }
}
