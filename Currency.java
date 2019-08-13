/**
 * The currency enum defines all currencies CODES
 * and DESCRIPTION
 *
 * @author      Netta Richer
 * @author      Sagi Granot
 */
package sagi.neta.CurrencyExchanger;

enum Currency {
    USD("United States Dollar"),
    GBP("Great Britain Pound"),
    JPY("Yen - Japan"),
    EUR("Euro"),
    AUD("Dollar Australia"),
    CAD("Dollar Canada"),
    DKK("Krone Denmark"),
    NOK("Krone Norway"),
    ZAR("Rand South Africa"),
    SEK("Krona Sweden"),
    CHF("Franc Switzerland"),
    JOD("Dinar Jordan"),
    LBP("Pound Lebanon"),
    EGP("Pound Egypt");

    private String description;

    Currency(String description) {
        this.description = description;
    }

    @Override public String toString() {
        return this.name() + " - " + this.description;
    }
}
