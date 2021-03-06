package euphoria.psycho.notes.common;

import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import euphoria.common.Files;
import euphoria.common.Keys;
import euphoria.common.Strings;

public class Markdowns {

    private static final String TAG = "TAG/" + Markdowns.class.getSimpleName();

    public static String table2markdown(Node table) {

        //Log.e(TAG, "Debug: table2markdown, " + table.outerHtml());

        Element element = (Element) table;
        Elements elements = element.select("tr");
        //Log.e(TAG, "table2markdown: " + elements.size());
        if (elements.size() == 0)
            return null;
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"table-wrapper table-nowrap\">\n");

        boolean isFirst = true;
        for (Element e : elements) {
            Elements tds = e.select("td");
            if (tds == null || tds.size() == 0) continue;

            if (isFirst) {
                for (int i = 0; i < tds.size(); i++) {
                    sb.append('|');
                }
                sb.append('\n');
                for (int i = 0; i < tds.size(); i++) {
                    sb.append("|---");
                }
                sb.append("|\n");
                isFirst = false;
            }
            for (Element t : tds) {
                sb.append('|').append(t.text());
            }
            sb.append("|\n");
        }
        sb.append("\n</div>\n");

        return sb.toString();

    }

    public static String html2markdown1(File htmlFile) throws IOException {
        byte[] buf = euphoria.common.Files.readFully(htmlFile);
        Document document = Jsoup.parse(new String(buf, "UTF-8"));

        String imagePrefix = Long.toString(Keys.crc64Long(Keys.getBytes(htmlFile.getParentFile().getAbsolutePath())));
        String imageFormat = "<div class=\"img-center\"><img alt=\"\" src=\"../static/pictures/%s\"><div class=\"img-caption\"></div></div>";
        FormattingVisitor1 formatter = new FormattingVisitor1(imagePrefix, imageFormat);
        new NodeTraversor(formatter).traverse(document.root()); // walk the DOM, and call .head() and .tail() for each node
        String value = formatter.toString();

        String[] values = value.split("\n");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            list.add(values[i]);
            if (euphoria.common.Strings.isNullOrWhiteSpace(values[i])) {
                while (i + 1 < values.length && euphoria.common.Strings.isNullOrWhiteSpace(values[i + 1])) {
                    i++;
                }
            }
        }
        return euphoria.common.Strings.join("\n", list);
    }

    public static String html2markdown2(File htmlFile) throws IOException {
        byte[] buf = euphoria.common.Files.readFully(htmlFile);
        Document document = Jsoup.parse(new String(buf, "UTF-8"));

        String imagePrefix = Long.toString(Keys.crc64Long(Keys.getBytes(htmlFile.getParentFile().getAbsolutePath())));
        String imageFormat = "<div class=\"img-center\"><img alt=\"\" src=\"../static/pictures/%s\"><div class=\"img-caption\"></div></div>";
        FormattingVisitor2 formatter = new FormattingVisitor2(imagePrefix, imageFormat);
        new NodeTraversor(formatter).traverse(document.root()); // walk the DOM, and call .head() and .tail() for each node
        String value = formatter.toString();

        String[] values = value.split("\n");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            list.add(values[i]);
            if (euphoria.common.Strings.isNullOrWhiteSpace(values[i])) {
                while (i + 1 < values.length && euphoria.common.Strings.isNullOrWhiteSpace(values[i + 1])) {
                    i++;
                }
            }
        }
        return euphoria.common.Strings.join("\n", list) + "\n\n>====>\nid:\ntoc:\ntags:\n>====>\n";
    }

    public static String html2markdown3(File htmlFile) throws IOException {
        byte[] buf = euphoria.common.Files.readFully(htmlFile);
        Document document = Jsoup.parse(new String(buf, "UTF-8"));

        String imagePrefix = Long.toString(Keys.crc64Long(Keys.getBytes(htmlFile.getParentFile().getAbsolutePath())));
        String imageFormat = "<div class=\"img-center\"><img alt=\"\" src=\"../static/pictures/%s\"><div class=\"img-caption\"></div></div>";
        FormattingVisitor3 formatter = new FormattingVisitor3(imagePrefix, imageFormat);
        new NodeTraversor(formatter).traverse(document.root()); // walk the DOM, and call .head() and .tail() for each node
        String value = formatter.toString();

        String[] values = value.split("\n");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            list.add(values[i]);
            if (euphoria.common.Strings.isNullOrWhiteSpace(values[i])) {
                while (i + 1 < values.length && euphoria.common.Strings.isNullOrWhiteSpace(values[i + 1])) {
                    i++;
                }
            }
        }
        return euphoria.common.Strings.join("\n", list);
    }

    public static String html2markdown4(File htmlFile) throws IOException {
        byte[] buf = Files.readFully(htmlFile);
        Document document = Jsoup.parse(new String(buf, "UTF-8"));

        String imagePrefix = Long.toString(Keys.crc64Long(Keys.getBytes(htmlFile.getParentFile().getAbsolutePath())));
        String imageFormat = "<div class=\"img-center\"><img alt=\"\" src=\"../static/pictures/%s\"><div class=\"img-caption\"></div></div>";
        FormattingVisitor4 formatter = new FormattingVisitor4(imagePrefix, imageFormat);
        new NodeTraversor(formatter).traverse(document.root()); // walk the DOM, and call .head() and .tail() for each node
        String value = formatter.toString();

        String[] values = value.split("\n");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            list.add(values[i]);
            if (euphoria.common.Strings.isNullOrWhiteSpace(values[i])) {
                while (i + 1 < values.length && euphoria.common.Strings.isNullOrWhiteSpace(values[i + 1])) {
                    i++;
                }
            }
        }
        return euphoria.common.Strings.join("\n", list);
    }

    private static class FormattingVisitor2 implements NodeVisitor {
        private StringBuilder accum = new StringBuilder(); // holds the accumulated text
        private final String mImagePrefix;
        private final String mImageFormat;

        private FormattingVisitor2(String imagePrefix, String imageFormat) {
            mImagePrefix = imagePrefix;
            mImageFormat = imageFormat;
        }

        // appends text to the string builder with a simple word wrap method
        private void append(String text) {
            accum.append(text);
        }

        // hit when the node is first seen
        public void head(Node node, int depth) {
            String name = node.nodeName();
            if (node instanceof TextNode) {
                String value = ((TextNode) node).text().trim();
                append(value.replaceAll("^(\\d+\\.)\\s+", "$1")); // TextNodes carry all user-readable text in the DOM.
                return;
            }
            if (name.equals("sub")) {
                append("<sub>");
                return;
            } else if (name.equals("sup")) {
                append("<sup>");
                return;
            }
            String klass = node.attr("class");
            if (klass.equals("h3")) {
                append("# ");
                return;
            } else if (klass.equals("h5")) {
                append("## ");
                return;
            } else if (klass.equals("h6")) {
                append("### ");
                return;
            }
            switch (name) {
                case "h1":
                    append("# ");
                    break;
                case "h2":
                    append("## ");
                    break;
                case "h3":
                    append("### ");
                    break;
                case "h4":
                    append("#### ");
                    break;
                case "h5":
                    append("#### ");
                    break;
                case "img":

                    String imgSrc = node.attr("src");
                    imgSrc = euphoria.common.Strings.substringAfterLast(imgSrc, '/');

                    append(String.format(mImageFormat, mImagePrefix + "_" + imgSrc));
                    break;
                default:
                    append("\n");
            }

        }

        // hit when all of the node's children (if any) have been visited
        public void tail(Node node, int depth) {
            String name = node.nodeName();
            if (name.equals("sub")) {
                append("</sub>");
                return;
            } else if (name.equals("sup")) {
                append("</sup>");
                return;
            }

            if (StringUtil.in(name, "br", "img", "li", "div", "p", "h1", "h2", "h3", "h4", "h5", "h6"))
                append("\n\n");

//            else if (name.equals("a"))
//                append(String.format(" <%s>", node.absUrl("href")));
        }

        @Override
        public String toString() {
            return accum.toString();
        }
    }

    private static class FormattingVisitor1 implements NodeVisitor {
        private StringBuilder accum = new StringBuilder(); // holds the accumulated text
        private final String mImagePrefix;
        private final String mImageFormat;

        private FormattingVisitor1(String imagePrefix, String imageFormat) {
            mImagePrefix = imagePrefix;
            mImageFormat = imageFormat;
        }

        // appends text to the string builder with a simple word wrap method
        private void append(String text) {
            accum.append(text);
        }

        // hit when the node is first seen
        public void head(Node node, int depth) {
            String name = node.nodeName();
            if (node instanceof TextNode) {
                append(((TextNode) node).text().trim()); // TextNodes carry all user-readable text in the DOM.
                return;
            }
            if (name.equals("sub")) {
                append("<sub>");
                return;
            } else if (name.equals("sup")) {
                append("<sup>");
                return;
            }
            String klass = node.attr("class");
            if (klass.equals("h3")) {
                append("## ");
                return;
            } else if (klass.equals("h4")) {
                append("### ");
                return;
            }
            switch (name) {
                case "h1":
                    append("# ");
                    break;
                case "h2":
                    append("## ");
                    break;
                case "h3":
                    append("### ");
                    break;
                case "h4":
                    append("#### ");
                    break;
                case "h5":
                    append("#### ");
                    break;
                case "img":

                    String imgSrc = node.attr("src");
                    imgSrc = euphoria.common.Strings.substringAfterLast(imgSrc, '/');

                    append(String.format(mImageFormat, mImagePrefix + "_" + imgSrc));
                    break;
                case "table":
                    append(table2markdown(node));
                    break;
                default:
                    append("\n");
            }

        }

        // hit when all of the node's children (if any) have been visited
        public void tail(Node node, int depth) {
            String name = node.nodeName();
            if (name.equals("sub")) {
                append("</sub>");
                return;
            } else if (name.equals("sup")) {
                append("</sup>");
                return;
            }

            if (StringUtil.in(name, "br", "img", "li", "div", "p", "h1", "h2", "h3", "h4", "h5", "h6"))
                append("\n\n");

//            else if (name.equals("a"))
//                append(String.format(" <%s>", node.absUrl("href")));
        }

        @Override
        public String toString() {
            return accum.toString();
        }
    }

    private static class FormattingVisitor4 implements NodeVisitor {
        private StringBuilder accum = new StringBuilder(); // holds the accumulated text
        private final String mImagePrefix;
        private final String mImageFormat;

        private FormattingVisitor4(String imagePrefix, String imageFormat) {
            mImagePrefix = imagePrefix;
            mImageFormat = imageFormat;
        }

        // appends text to the string builder with a simple word wrap method
        private void append(String text) {
            accum.append(text);
        }

        // hit when the node is first seen
        public void head(Node node, int depth) {
            String name = node.nodeName();
            if (node instanceof TextNode) {
                append(((TextNode) node).text().trim()); // TextNodes carry all user-readable text in the DOM.
                return;
            }
            if (name.equals("sub")) {
                append("<sub>");
                return;
            } else if (name.equals("sup")) {
                append("<sup>");
                return;
            }
            String klass = node.attr("class");
            if (klass.equals("h3")) {
                append("## ");
                return;
            } else if (klass.equals("h4")) {
                append("### ");
                return;
            } else if (klass.equals("h5")) {
                append("#### ");
                return;
            }
            switch (name) {
                case "h1":
                    append("# ");
                    break;
                case "h2":
                    append("## ");
                    break;
                case "h3":
                    append("### ");
                    break;
                case "h4":
                    append("#### ");
                    break;
                case "h5":
                    append("#### ");
                    break;
                case "img":

                    String imgSrc = node.attr("src");
                    imgSrc = euphoria.common.Strings.substringAfterLast(imgSrc, '/');

                    append(String.format(mImageFormat, mImagePrefix + "_" + imgSrc));
                    break;
                default:
                    append("\n");
            }

        }

        // hit when all of the node's children (if any) have been visited
        public void tail(Node node, int depth) {
            String name = node.nodeName();
            if (name.equals("sub")) {
                append("</sub>");
                return;
            } else if (name.equals("sup")) {
                append("</sup>");
                return;
            }

            if (StringUtil.in(name, "br", "img", "li", "div", "p", "h1", "h2", "h3", "h4", "h5", "h6"))
                append("\n\n");

//            else if (name.equals("a"))
//                append(String.format(" <%s>", node.absUrl("href")));
        }

        @Override
        public String toString() {
            return accum.toString();
        }
    }

    private static class FormattingVisitor3 implements NodeVisitor {
        private StringBuilder accum = new StringBuilder(); // holds the accumulated text
        private final String mImagePrefix;
        private final String mImageFormat;

        private FormattingVisitor3(String imagePrefix, String imageFormat) {
            mImagePrefix = imagePrefix;
            mImageFormat = imageFormat;
        }

        // appends text to the string builder with a simple word wrap method
        private void append(String text) {
            accum.append(text);
        }

        // hit when the node is first seen
        public void head(Node node, int depth) {
            String name = node.nodeName();
            if (node instanceof TextNode) {
                append(((TextNode) node).text().trim()); // TextNodes carry all user-readable text in the DOM.
                return;
            }
            if (name.equals("sub")) {
                append("<sub>");
                return;
            } else if (name.equals("sup")) {
                append("<sup>");
                return;
            }
            String klass = node.attr("class");
            if (klass.equals("h3")) {
                append("### ");
                return;
            } else if (klass.equals("h4")) {
                append("## ");
                return;
            }
            switch (name) {
                case "h1":
                    append("# ");
                    break;
                case "h2":
                    append("## ");
                    break;
                case "h3":
                    append("### ");
                    break;
                case "h4":
                    append("#### ");
                    break;
                case "h5":
                    append("#### ");
                    break;
                case "img":

                    String imgSrc = node.attr("src");
                    imgSrc = Strings.substringAfterLast(imgSrc, '/');

                    append(String.format(mImageFormat, mImagePrefix + "_" + imgSrc));
                    break;
                default:
                    append("\n");
            }

        }

        // hit when all of the node's children (if any) have been visited
        public void tail(Node node, int depth) {
            String name = node.nodeName();
            if (name.equals("sub")) {
                append("</sub>");
                return;
            } else if (name.equals("sup")) {
                append("</sup>");
                return;
            }

            if (StringUtil.in(name, "br", "img", "li", "div", "p", "h1", "h2", "h3", "h4", "h5", "h6"))
                append("\n\n");

//            else if (name.equals("a"))
//                append(String.format(" <%s>", node.absUrl("href")));
        }

        @Override
        public String toString() {
            return accum.toString();
        }
    }
}