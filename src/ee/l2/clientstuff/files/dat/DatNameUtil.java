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
