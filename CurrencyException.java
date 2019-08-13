/**
 * The CurrencyException class defines an exception
 * with a message
 *
 * @author      Netta Richer
 * @author      Sagi Granot
 */
package sagi.neta.CurrencyExchanger;

public class CurrencyException extends Exception {
    CurrencyException(String msg, Throwable e){
        super(msg,e);
    }
}
