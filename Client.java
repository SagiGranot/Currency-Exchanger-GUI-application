/**
 * Client is the UI class that creates GUI elements using Swing
 * providing the user some input options
 * <ul>
 * <li>The amount of money to convert
 * <li>From currency
 * <li>To currency
 * <li>Convert option
 * <li>Show all rates option
 * </ul>
 * <p>
 * @see         sagi.neta.CurrencyExchanger.Model
 * @see         sagi.neta.CurrencyExchanger.xmlParser
 * @see         sagi.neta.CurrencyExchanger.Currency
 * @author      Netta Richer
 * @author      Sagi Granot
 */
package sagi.neta.CurrencyExchanger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import static javax.swing.JFrame.EXIT_ON_CLOSE;
public class Client extends JPanel{
    /**
     * This class is responsible for all UI objects
     *
     */
    private static final String XML_PATH = "gui.xml";
    private Map<CurrencyPair, Double> exchangeRates;
    static Logger logger = Logger.getLogger("Client GUI");
    private String Date;
    //

    public Client() {
        /**
         * Class constructor
         *
         */
        super(new FlowLayout(FlowLayout.LEADING));
        exchangeRates = new HashMap<>();
        BasicConfigurator.configure();
        logger.info("GUI Constructor init");
        // Amount
        JTextField amountInput = new JTextField(20);
        JPanel amount = new JPanel();
        amount.add(amountInput);
        amount.setBorder(BorderFactory.createTitledBorder("Enter Ammount"));
        add(amount, BorderLayout.CENTER);
        //Date updated
        JLabel dateText = new JLabel();
        add(dateText, BorderLayout.CENTER);
        dateText.setText("(Click 'Rates Table' to update)");

        // From
        JPanel from = new JPanel();
        JComboBox fromOptions = new JComboBox(Currency.values());
        from.add(fromOptions);
        from.setBorder(BorderFactory.createTitledBorder("Select currency"));
        add(from, BorderLayout.CENTER);

        // To
        JComboBox toOptions = new JComboBox(Currency.values());
        JPanel to = new JPanel();
        to.add(toOptions);
        to.setBorder(BorderFactory.createTitledBorder("Convert to"));
        add(to, BorderLayout.CENTER);

        // Convert Action
        JLabel convertText = new JLabel();
        JButton convertCmd = new JButton("Convert");
        JPanel convert = new JPanel();
        convert.add(convertCmd);
        convert.add(convertText);
        JButton getRates = new JButton("Rates Table");
        add(getRates);
        add(convert);

        //Table
        Object rows[][] = new Object[Currency.values().length*Currency.values().length][];
        Object columns[] = { "From", "To","Rate" };
        DefaultTableModel model = new DefaultTableModel(rows, columns);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        /**handeling convertion*/
        ActionListener convertAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                logger.info("Performing convertion...");
                String amountInputText = amountInput.getText();
                if ("".equals(amountInputText)) { return; }
                // Convert
                Double conversion = convertCurrency(amountInputText);
                convertText.setText(NumberFormat
                        .getCurrencyInstance(Locale.US)
                        .format(conversion));
            }
            private Double convertCurrency(String amountInputText) {
                logger.info("Convert Calculating...");
                CurrencyPair currencyPair = new CurrencyPair(
                        (Currency) fromOptions.getSelectedItem(),
                        (Currency) toOptions.getSelectedItem());
                Double rate = exchangeRates.get(currencyPair);
                Double amount = Double.parseDouble(amountInputText);
                return amount*rate;
            }
        };
        convertCmd.addActionListener(convertAction);
        //
        /** handeling rates table display*/
        ActionListener showAllRates = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                logger.info("Displaying all rates map");
                int i = 0;
                for (Currency from : Currency.values()) {
                    for (Currency to : Currency.values()) {
                        BigDecimal val = new BigDecimal(exchangeRates.get(new CurrencyPair(from, to)));
                        val = val.setScale(2, RoundingMode.CEILING);
                        model.setValueAt(from, i, 0);   //Set from currency
                        model.setValueAt(to, i, 1);     //Set to currency
                        model.setValueAt(val, i, 2);    //Set rate value
                        ++i;
                    }
                }
                dateText.setText("(Updated: "+Date+")");
            }
        };
        getRates.addActionListener(showAllRates);
    }
    //
    public void setExchangeRates(Map<CurrencyPair, Double> exchangeRates) {
        logger.info("Updated rates map.");
        this.exchangeRates = exchangeRates;
    }
    public void setDate(String newdate) {
        logger.info("Updateding date");
        this.Date = newdate;
    }


    public static void main(String[] args) {
        /**
         * Updates the hashmap with all rates
         * creates the graphic UI
         * Runs intervals for updating the rates hashmap
         *
         */
        try {
            //First, get the rates xml file and update hash table
            xmlParser Rates = new xmlParser();
            Rates.run();

            //Create and run GUI
            Client GUI = new Client();    //Send the c'tor and updated hashmap containing all data parsed
            Rates.updateHashMap();            //update map according to fetched data
            //Get updated map with all rates
            GUI.setExchangeRates(Rates.getExchangeRates());
            GUI.setDate(Rates.getDate());
            JFrame frame = new JFrame();
            frame.getContentPane().add(GUI);
            frame.setTitle("Currency Exchanger");
            frame.setSize(500, 620);
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            //Fetch new data intervals
            int delay = 15*60000; //milliseconds (15 min)
            ActionListener taskPerformer = new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    logger.info("Update xml and hashmap interval started....");
                    try {
                        Rates.run();
                        Rates.updateHashMap();
                        //Get updated map with all rates
                        GUI.setExchangeRates(Rates.getExchangeRates());
                        GUI.setDate(Rates.getDate());
                    }catch(CurrencyException e){e.printStackTrace();}
                }
            };
            new javax.swing.Timer(delay, taskPerformer).start();
            //
        }catch (CurrencyException e){
            e.getMessage();
        }catch (Exception e){e.printStackTrace();}
    }

}
