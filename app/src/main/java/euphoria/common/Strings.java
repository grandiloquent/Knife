package euphoria.common;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.util.Pair;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strings {
    // 0123456789
    private static final String NUMBERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static SecureRandom sSecureRandom;

    public static boolean containsLetter(String value) {
        if (isNullOrWhiteSpace(value)) return false;
        for (int i = 0, j = value.length(); i < j; i++) {
            if (Character.isLetter(value.charAt(i))) return true;
        }
        return false;
    }

    public static String[] copyOf(String[] source, int newSize) {
        String[] result = new String[newSize];
        newSize = Math.min(source.length, newSize);
        System.arraycopy(source, 0, result, 0, newSize);
        return result;
    }

    public static int count(CharSequence text, char c) {
        int count = 0;
        for (int i = 0, j = text.length(); i < j; i++) {
            if (text.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    public static int countStart(String s, char c) {
        if (s == null || s.length() == 0) return 0;
        int r = 0;
        for (int i = 0, j = s.length(); i < j; i++) {
            if (s.charAt(i) == c) r++;
            else return r;
        }
        return r;
    }

    public static List<String> distinct(List<String> list) {
        List<String> result = new ArrayList<>();
        for (int i = 0, j = list.size(); i < j; i++) {
            String item = list.get(i);
            if (result.indexOf(item) == -1) {
                result.add(item);
            }
        }
        return result;
    }

    public static String ensureNotNull(String value) {
        return value == null ? "" : value;
    }

    public static String escapeHTML(String s) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static byte[] getBytes(String in) {
        byte[] result = new byte[in.length() * 2];
        int output = 0;
        for (char ch : in.toCharArray()) {
            result[output++] = (byte) (ch & 0xFF);
            result[output++] = (byte) (ch >> 8);
        }
        return result;
    }

    public static String getText(ClipboardManager manager) {
        ClipData clip = manager.getPrimaryClip();
        if (clip != null && clip.getItemCount() > 0) {
            CharSequence text = clip.getItemAt(0).getText();
            if (text != null) {
                return text.toString();
            }
        }
        return null;
    }

    public static int indexOf(char chars[], char c) {
        for (int i = 0, j = chars.length; i < j; i++) {
            if (chars[i] == c) return i;
        }
        return -1;
    }

    public static boolean isDigit(String value) {
        for (int i = 0, len = value.length(); i < len; i++) {
            if (!Character.isDigit(value.charAt(i))) return false;
        }
        return true;
    }

    public static boolean isNullOrWhiteSpace(CharSequence value) {
        if (value == null || value.length() == 0) {
            return true;
        }
        for (int i = 0, j = value.length(); i < j; i++) {
            if (!Character.isWhitespace(value.charAt(i))) return false;
        }
        return false;
    }

    public static boolean isNullOrWhiteSpace(String value) {
        if (value == null) return true;
        for (int i = 0; i < value.length(); i++) {
         /*
         Determines if the specified character is white space according to Java.
         A character is a Java whitespace character if and only if it satisfies
         one of the following criteria:
         It is a Unicode space character (SPACE_SEPARATOR,
         LINE_SEPARATOR, or PARAGRAPH_SEPARATOR)
         but is not also a non-breaking space ('\u005Cu00A0',
         '\u005Cu2007', '\u005Cu202F').
         It is '\u005Ct', U+0009 HORIZONTAL TABULATION.
         It is '\u005Cn', U+000A LINE FEED.
         It is '\u005Cu000B', U+000B VERTICAL TABULATION.
         It is '\u005Cf', U+000C FORM FEED.
         It is '\u005Cr', U+000D CARRIAGE RETURN.
         It is '\u005Cu001C', U+001C FILE SEPARATOR.
         It is '\u005Cu001D', U+001D GROUP SEPARATOR.
         It is '\u005Cu001E', U+001E RECORD SEPARATOR.
         It is '\u005Cu001F', U+001F UNIT SEPARATOR.
         Note: This method cannot handle  supplementary characters. To support
         all Unicode characters, including supplementary characters, use
         the isWhitespace(int) method.
         @param   ch the character to be tested.
         @return  true if the character is a Java whitespace
         character; false otherwise.
         @see     Character#isSpaceChar(char)
         @since   1.1
         */
            if (!Character.isWhitespace(value.charAt(i))) return false;
        }
        return true;
    }

    public static String join(String separator, byte[] values) {
        if (values.length == 0)
            return "";
        if (separator == null)
            separator = "";
        StringBuilder result = new StringBuilder();
        String value = Byte.toString(values[0]);
        result.append(value);
        for (int i = 1, j = values.length; i < j; i++) {
            result.append(separator);
            // handle the case where their ToString() override is broken
            value = Byte.toString(values[i]);
            result.append(value);
        }
        return result.toString();
    }

    public static <T> String join(String separator, List<T> values) {
        if (values.size() == 0 || values.get(0) == null)
            return "";
        if (separator == null)
            separator = "";
        StringBuilder result = new StringBuilder();
        String value = values.get(0).toString();
        result.append(value);
        for (int i = 1, j = values.size(); i < j; i++) {
            result.append(separator);
            if (values.get(i) != null) {
                // handle the case where their ToString() override is broken
                value = values.get(i).toString();
                result.append(value);
            }
        }
        return result.toString();
    }

    public static String join(String separator, String[] values) {
        if (values.length == 0)
            return "";
        if (separator == null)
            separator = "";
        StringBuilder result = new StringBuilder();
        result.append(values[0]);
        for (int i = 1, j = values.length; i < j; i++) {
            result.append(separator);
            result.append(values[i]);
        }
        return result.toString();
    }

    public static List<String> lines(String text) {
        String[] lines = text.split("\n");
        List<String> list = new ArrayList<>();
        for (String line : lines) {
            String s = line.trim();
            if (s.length() == 0 || list.indexOf(s) != -1) continue;
            list.add(s);
        }
        return list;
    }

    public static List<String> matchAll(String text, String pattern) {
        Matcher matcher = Pattern.compile(pattern).matcher(text);
        List<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }

    public static String paddingStartZero(String s, int length) {
        Pattern p = Pattern.compile("^\\d+");
        Matcher m = p.matcher(s);
        if (m.find()) {
            int len = m.group().length();
            if (len >= length) return s;

            return s.replaceFirst("^\\d+", String.format("%0" + length + "d"
                    , Integer.parseInt(m.group())));
        }
        return s;
    }

    public static long parseDuration(String s) {
        String[] pieces = s.split(":");
        int length = pieces.length - 1;
        long r = 0;
        for (int i = length; i > -1; i--) {
            r += (long) (Integer.parseInt(pieces[i]) * Math.pow(60, length - i));
        }
        return r * 1000;
    }

    public static float parseFloatSafely(String content, float defaultValue) {
        if (content == null) return defaultValue;
        try {
            return Float.parseFloat(content);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static int parseIntAggressively(String content, int defaultValue) {
        if (content == null) return defaultValue;
        StringBuilder sb = new StringBuilder();
        for (int i = 0, j = content.length(); i < j; i++) {
            char c = content.charAt(i);
            if (Character.isDigit(c)) {
                sb.append(c);
                while (i + 1 < j && Character.isDigit((c = content.charAt(++i)))) {
                    sb.append(c);
                }
            }
        }
        if (sb.length() > 0)
            return Integer.parseInt(sb.toString());
        return defaultValue;
    }

    public static int parseIntSafely(String content, int defaultValue) {
        if (content == null) return defaultValue;
        try {
            return Integer.parseInt(content);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static List<Integer> parseIntsSafely(String content) {
        if (content == null) return null;
        try {
            Pattern numberPattern = Pattern.compile("\\d+");
            Matcher numberMatcher = numberPattern.matcher(content);
            List<Integer> integers = new ArrayList<>();
            while (numberMatcher.find()) {
                integers.add(Integer.parseInt(numberMatcher.group()));
            }
            Collections.sort(integers, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o1.compareTo(o2);
                }
            });
            return integers;
        } catch (NumberFormatException e) {
        }
        return null;
    }

    public static List<Pair<String, String>> parseSettings(String text, String separator) {
        if (text == null) return null;
        List<Pair<String, String>> settings = new ArrayList<>();
        String[] lines = text.split("\n");
        for (int i = 0, j = lines.length; i < j; i++) {
            if (isNullOrWhiteSpace(lines[i])) continue;
            String[] parts = lines[i].split(separator);
            if (parts.length < 1) continue;
            if (parts.length < 2) {
                settings.add(Pair.create(parts[0], ""));
                continue;
            }
            settings.add(Pair.create(parts[0], parts[1]));
        }
        return settings;
    }

    public static String randSeq(int length) {
        if (sSecureRandom == null) {
         /*
         Constructs a secure random number generator (RNG) implementing the
         default random number algorithm.
         This constructor traverses the list of registered security Providers,
         starting with the most preferred Provider.
         A new SecureRandom object encapsulating the
         SecureRandomSpi implementation from the first
         Provider that supports a SecureRandom (RNG) algorithm is returned.
         If none of the Providers support a RNG algorithm,
         then an implementation-specific default is returned.
         Note that the list of registered providers may be retrieved via
         the Security#getProviders() Security.getProviders() method.
         See the SecureRandom section in the
         Java Cryptography Architecture Standard Algorithm Name Documentation
         for information about standard RNG algorithm names.
         The returned SecureRandom object has not been seeded.  To seed the
         returned object, call the setSeed method.
         If setSeed is not called, the first call to
         nextBytes will force the SecureRandom object to seed itself.
         This self-seeding will not occur if setSeed was
         previously called.
         */
            sSecureRandom = new SecureRandom();
        }
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(NUMBERS.charAt(sSecureRandom.nextInt(NUMBERS.length())));
        }
        return stringBuilder.toString();
    }

    public static String repeat(char c, int count) {
        if (count <= 0) return "";
        char[] chars = new char[count];
        for (int i = 0; i < count; i++)
            chars[i] = c;
        return new String(chars);
    }

    public static String replaceAll(String s, char f, char r) {
        if (s == null) return null;
        int length = s.length();
        if (length == 0) return s;
        char[] newString = new char[length];
        for (int i = 0; i < length; i++) {
            if (s.charAt(i) == f) {
                newString[i] = r;
            } else {
                newString[i] = s.charAt(i);
            }
        }
        return new String(newString);
    }

    public static String substringAfter(String text, char delimiter) {
        int index = text.indexOf(delimiter);
        if (index == -1) return text;
        else return text.substring(index + 1, text.length());
    }

    public static String substringAfter(String text, String delimiter) {
        int index = text.indexOf(delimiter);
        if (index == -1) return text;
        else return text.substring(index + delimiter.length(), text.length());
    }

    public static String substringAfterLast(String text, String subString) {
        int i = text.lastIndexOf(subString);
        if (i == -1) return null;
        return text.substring(i + subString.length());
    }

    public static String substringAfterLast(String text, char delimiter) {
        int index = text.lastIndexOf(delimiter);
        if (index == -1) return text;
        else return text.substring(index + 1, text.length());
    }

    public static String substringAfterRegex(String text, String delimiter) {
        Pattern p = Pattern.compile(delimiter);
        Matcher m = p.matcher(text);
        if (m.find()) {
            String result = text.substring(m.end(), text.length());
            return result;
        }
        return text;
    }

    public static String substringBefore(String text, char delimiter) {
         /*
         Returns the index within this string of the first occurrence of the
         specified character, starting the search at the specified index.
         If a character with value ch occurs in the
         character sequence represented by this String
         object at an index no smaller than fromIndex, then
         the index of the first such occurrence is returned. For values
         of ch in the range from 0 to 0xFFFF (inclusive),
         this is the smallest value k such that:
         (this.charAt(k) == ch) && (k &gt;= fromIndex)
         is true. For other values of ch, it is the
         smallest value k such that:
         (this.codePointAt(k) == ch) && (k &gt;= fromIndex)
         is true. In either case, if no such character occurs in this
         string at or after position fromIndex, then
         -1 is returned.
         There is no restriction on the value of fromIndex. If it
         is negative, it has the same effect as if it were zero: this entire
         string may be searched. If it is greater than the length of this
         string, it has the same effect as if it were equal to the length of
         this string: -1 is returned.
         All indices are specified in char values
         (Unicode code units).
         @param   ch          a character (Unicode code point).
         @param   fromIndex   the index to start the search from.
         @return  the index of the first occurrence of the character in the
         character sequence represented by this object that is greater
         than or equal to fromIndex, or -1
         if the character does not occur.
         */
        int index = text.indexOf(delimiter);
        if (index == -1) return text;
         /*
         Returns a string that is a substring of this string. The
         substring begins at the specified beginIndex and
         extends to the character at index endIndex - 1.
         Thus the length of the substring is endIndex-beginIndex.
         Examples:
         "hamburger".substring(4, 8) returns "urge"
         "smiles".substring(1, 5) returns "mile"
         @param      beginIndex   the beginning index, inclusive.
         @param      endIndex     the ending index, exclusive.
         @return     the specified substring.
         @exception  IndexOutOfBoundsException  if the
         beginIndex is negative, or
         endIndex is larger than the length of
         this String object, or
         beginIndex is larger than
         endIndex.
         */
        else return text.substring(0, index);
    }

    public static String substringBefore(String text, String separator) {
        int index = text.indexOf(separator);
        if (index == -1) return text;
        return text.substring(0, index);
    }

    public static String substringBeforeLast(String text, char delimiter) {
        int index = text.lastIndexOf(delimiter);
        if (index == -1) return text;
        return text.substring(0, index);
    }

    public static String substringBeforeLast(String text, String separator) {
        int index = text.lastIndexOf(separator);
        if (index == -1) return text;
        return text.substring(0, index);
    }
}
