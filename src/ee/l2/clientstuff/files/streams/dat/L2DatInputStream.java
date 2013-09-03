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
package ee.l2.clientstuff.files.streams.dat;

import ee.l2.clientstuff.files.streams.l2data.L2DataInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author acmi
 */
public class L2DatInputStream extends L2DataInputStream {
    public L2DatInputStream(InputStream input) {
        super(Objects.requireNonNull(input));
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
