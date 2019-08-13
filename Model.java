/**
 * The model interface is responsible for updating an hashmap
 * with all currency exchange rates.
 *
 * @author      Netta Richer
 * @author      Sagi Granot
 */
package sagi.neta.CurrencyExchanger;

import java.util.Map;

public interface Model {
    /**
     * Get a reference to the hashmap
     *
     * @return          <code>Map<CurrencyPair, Double></code> with ratio rates
     *                  between all currencies
     */
    public abstract Map<CurrencyPair, Double> getExchangeRates() throws CurrencyException;
    /**
     * Getting from the xml file (which was created in a different
     * thread) rates and calculates
     * all rates and updates in an hash map.
     *
     * @see         xmlParser
     */
    public abstract void updateHashMap() throws CurrencyException;
}
