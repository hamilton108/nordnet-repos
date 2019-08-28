package nordnet.html;

public enum DerivativesStringEnum {
    TABLE_CLASS("c01444"),
    TD_CLASS("c01474");

    private final String text;

    DerivativesStringEnum (String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}