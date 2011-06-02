package cz.repaymentplan.logic.enums;

/**
 * @author Tomas Pinos
 */
public enum Country {

    CZE("CZE", "Česká republika"), SVK("SVK", "Slovenská republika");

    private String code;
    private String label;

    Country(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
