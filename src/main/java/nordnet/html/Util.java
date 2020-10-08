package nordnet.html;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

public class Util {

    public static double parseExercisePrice(String s) {
        String[] sx = s.split("\\s");
        return Double.parseDouble(sx[0]);
    }
    public static Element getTd(Element row) {
        Node node = row.childNode(0).childNode(0).childNode(0);
        return (Element)node;
    }

    public static double parseDerivativePrice(String s) {
        String[] sx = s.split(",");
        String sx2 = String.format("%s.%s", sx[0],sx[1]);
        return Double.parseDouble(sx2);
    }
}
