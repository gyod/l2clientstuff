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

/**
 * @author acmi
 */
public class DatNameUtil {
    public static String getFileName(Class<?> clazz, String lang) {
        DatFile datFileAnn = clazz.getAnnotation(DatFile.class);
        if (datFileAnn == null)
            throw new UnsupportedOperationException("Not a dat file class");

        String fileName = datFileAnn.value();
        if (datFileAnn.localizable()) {
            fileName += "-" + lang;
        }

        return fileName + ".dat";
    }

    public static String getFileName(Class<?> clazz) {
        return getFileName(clazz, "e");
    }

    public static String getFileNamePattern(Class<?> clazz) {
        DatFile datFileAnn = clazz.getAnnotation(DatFile.class);
        if (datFileAnn == null)
            throw new UnsupportedOperationException("Not a dat file class");

        String pattern = datFileAnn.value();
        if (datFileAnn.localizable()) {
            pattern += "-\\w+";
        }

        return pattern + "\\.dat";
    }

}
