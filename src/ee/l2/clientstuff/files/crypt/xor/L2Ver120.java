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

/**
 * @author acmi
 */
public class L2Ver120 {
    public static final int START_IND = 0xE6;

    public static int getXORKey(int n) {
        int d1 = (n >> 0) & 0xf;
        int d2 = (n >> 4) & 0xf;
        int d3 = (n >> 8) & 0xf;
        int d4 = (n >> 12) & 0xf;
        return ((d2 ^ d4) << 4) | (d1 ^ d3);
    }
}
