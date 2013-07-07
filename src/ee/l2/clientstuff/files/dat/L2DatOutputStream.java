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
package ee.l2.clientstuff.files.dat;

import ee.l2.clientstuff.files.L2DataOutputStream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author acmi
 */
public class L2DatOutputStream extends L2DataOutputStream{
    public L2DatOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void writeObject(Object obj) throws IOException, ReflectiveOperationException {
        super.writeObject(obj);

        if (obj.getClass().isAnnotationPresent(SafePackage.class))
            writeLine("SafePackage");
    }
}
