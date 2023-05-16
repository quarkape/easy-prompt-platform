package club.hue.util;

// 转义字符
public class CharacterEscapeUtil {

    public static String escapeCharacters(String rawString) {
        String escapedString = "";
        escapedString = rawString.replaceAll(" ", "")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "apos")
                .replaceAll("&", "&amp;");
        return escapedString;
    }

}
