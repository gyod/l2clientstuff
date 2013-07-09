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
package ee.l2.clientstuff.files.streams;

import java.io.*;

/**
 * @author acmi
 */
public abstract class FinishableOutputStream extends OutputStream{
    /**
     * Finishes writing and flushes data to the output stream without closing
     * the underlying stream.
     * @throws IOException if an I/O error has occurred
     */
    public void finish() throws IOException{}

    /**
     * Closes this output stream and releases any system resources
     * associated with this stream. The general contract of <code>close</code>
     * is that it closes the output stream. A closed stream cannot perform
     * output operations and cannot be reopened.
     * <p>
     * The <code>close</code> method of <code>FinishableOutputStream</code> call <code>finish</code> method.
     *
     * @throws  IOException  if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        finish();
    }
}
