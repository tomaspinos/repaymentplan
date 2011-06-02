package cz.repaymentplan.logic.enums;

/**
 * @author Tomas Pinos
 */
public enum LastPaymentType {

    CALCULATED("CALCULATED", "nebude upravena"), LEAST_DIFFERENCE("LEAST_DIFFERENCE", "min. rozdíl od řádné splátky");

    private String code;
    private String label;

    LastPaymentType(String code, String label) {
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
