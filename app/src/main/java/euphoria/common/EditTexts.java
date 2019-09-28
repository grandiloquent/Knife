package euphoria.common;

import android.text.Editable;
import android.widget.EditText;


public class EditTexts {
    public static CharSequence deleteExtend(EditText editText) {
        Editable text = editText.getText();
        int len = text.length();
        if (len == 0) return null;
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        if (start == len) {
            start--;
        }
        boolean found = false;
        for (int i = start; i >= 0; i--) {
            char c = text.charAt(i);
            if (c == '\n') {
                for (int j = i - 1; j >= 0; j--) {
                    c = text.charAt(j);
                    if (!Character.isWhitespace(c)) break;
                    if (c == '\n') {
                        start = j;
                        found = true;
                        break;
                    }
                }
            }
            if (found) break;
        }
        if (!found) {
            start = 0;
        }
        found = false;

        for (int i = end; i < len; i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                for (int j = i + 1; j < len; j++) {
                    c = text.charAt(j);
                    if (!Character.isWhitespace(c)) break;
                    if (c == '\n') {
                        end = j + 1;
                        found = true;
                        break;

                    }
                }
            }
            if (found) break;
        }
        if (!found) {
            end = len;
        }
        CharSequence value = text.subSequence(start, end);

        text.replace(start, end, "\n");
        return value;
    }

    public static void insertAfter(EditText editText, String text) {

        int end = editText.getSelectionEnd();
        editText.getText().insert(end, text);
        editText.setSelection(end);
    }

    public static CharSequence deleteLineStrict(EditText editText) {
        Editable text = editText.getText();
        int len = text.length();
        if (len == 0) return null;
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        if (start == len || text.charAt(start) == '\n') {
            start--;
        }
        boolean found = false;
        for (int i = start; i >= 0; i--) {
            char c = text.charAt(i);
            if (c == '\n') {
                for (int j = i - 1; j >= 0; j--) {
                    c = text.charAt(j);

                    if (!Character.isWhitespace(c)) {
                        start = j + 1;
                        found = true;
                        break;
                    }
                }
            }
            if (found) break;
        }
        if (!found) {
            start = 0;
        }
        found = false;

        for (int i = end; i < len; i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                for (int j = i + 1; j < len; j++) {
                    c = text.charAt(j);
                    if (!Character.isWhitespace(c)) {
                        end = j;
                        found = true;
                        break;

                    }
                }
            }
            if (found) break;
        }
        if (!found) {
            end = len;
        }
        CharSequence value = text.subSequence(start, end);

        text.delete(start, end);
        return value;
    }

    public static void insertBefore(EditText editText, String text) {

        int start = editText.getSelectionStart();
        editText.getText().insert(start, text);
        editText.setSelection(start);
    }

    public static boolean isWhitespace(EditText editText) {
        Editable editable = editText.getText();
        if (editable.length() == 0) return true;
        for (int i = 0, j = editable.length(); i < j; i++) {
            if (!Character.isWhitespace(editable.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String selectLine(EditText editText) {

        if (isWhitespace(editText)) return null;

        Editable text = editText.getText();

        int len = text.length();
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        if (start == len) {
            start--;
        }
        while (start > 0 && text.charAt(start - 1) != '\n') {
            start--;
        }
        while (end + 1 < len && text.charAt(end) != '\n') {
            end++;
        }
//        char c=text.charAt(end);
//        String v=text.subSequence(start, end).toString();;
        if (end < len && text.charAt(end) != '\n') {
            end++;
        }
        editText.setSelection(start, end);
        return text.subSequence(start, end).toString();
//
//        int len = text.length();
//
//        int start = editText.getSelectionStart();
//        int end = editText.getSelectionEnd();
//        if (start == len) {
//            start--;
//        }
//        if (start == end && text.charAt(start) == '\n') {
//            start--;
//
//            while (start > 0 && text.charAt(start) != '\n') {
//                start--;
//            }
//            if (text.charAt(start) == '\n') {
//                start++;
//            }
//
//        } else {
//
//
//            while (start > 0 && text.charAt(start) != '\n') {
//                start--;
//            }
//            if (text.charAt(start) == '\n') {
//                start++;
//            }
//
//            while (end < len && text.charAt(end) != '\n') {
//                end++;
//            }
//
//
//        }
//        editText.setSelection(start, end);
//
//        // String str=text.substring(start, end);
//
//        return text.substring(start, end);
    }


    public static CharSequence cutLine(EditText editText) {
        Editable text = editText.getText();
        int len = text.length();
        if (len == 0) return null;
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        if (start == len || text.charAt(start) == '\n') {
            start--;
        }

        while (start > 0 && text.charAt(start) != '\n') {
            start--;
        }
        while (end < len && text.charAt(end) != '\n') {
            end++;
        }
        CharSequence value = editText.getText().subSequence(start, end);
        editText.getText().delete(start, end);
        return value;
    }
}
