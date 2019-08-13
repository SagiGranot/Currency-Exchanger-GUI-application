/**
 * The CurrencyPair class defines a pair of currencies
 * which represent a FROM currency and a TO currency
 *
 * @author      Netta Richer
 * @author      Sagi Granot
 */
package sagi.neta.CurrencyExchanger;

public class CurrencyPair{
    private final Currency from;
    private final Currency to;

    public Currency getFrom() {
        return from;
    }

    public Currency getTo() {
        return to;
    }

    public CurrencyPair(Currency from, Currency to) {
        this.from = from;
        this.to = to;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CurrencyPair that = (CurrencyPair) o;
        if (from != that.from) return false;
        return to == that.to;
    }

    @Override public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        return result;
    }
}
