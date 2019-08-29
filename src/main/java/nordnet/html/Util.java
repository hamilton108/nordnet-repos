package nordnet.html;

public class Util {

    public static double parseExercisePrice(String s) {
        String[] sx = s.split("\\s");
        return Double.parseDouble(sx[0]);
    }
}
