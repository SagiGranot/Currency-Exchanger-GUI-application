/**
 * The xmlParser class is the Model implementation.
 * it implements Runnable in order to provide an option
 * to run a thread for continuous checking of the up-to-date XML data
 * and updating the data stored local in a file.
 * it has an hashmap with all currency rates. -> (currency)FROM, (currency)TO, RATE
 *
 *
 * @author      Netta Richer
 * @author      Sagi Granot
 * @see         sagi.neta.CurrencyExchanger.Currency
 * @see         sagi.neta.CurrencyExchanger.CurrencyPair
 */
package sagi.neta.CurrencyExchanger;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class xmlParser implements Runnable, Model{
    private static final String BACKUP = "currency.xml";        //path for local xml file in case internet fails
    private static final String XML_PATH = "gui.xml";           //path for new updated xml created locally
    private Map<CurrencyPair, Double> exchangeRates;            //hashmap to hold all rates
    static Logger logger = Logger.getLogger("xmlParser");       //Logger
    private String Date;                                        //Date of update
    /**
     * Class constructor sets a new hashmap
     */
    public xmlParser() {
        this.exchangeRates = new HashMap<>();
        BasicConfigurator.configure();
        logger.info("xmlParser Constructor init");
    }
    /**
     * Hashmap getter
     * @return the hash map
     */
    public Map<CurrencyPair, Double> getExchangeRates() throws CurrencyException{
        if (exchangeRates.size() <= 0) {
            logger.info("HAsmap empty ... throwing exception");
            throw new CurrencyException("HashMap is empty!", new Error());
        }
        logger.info("returning hashmap ref");
        return exchangeRates;
    }
    /**
     * Date getter
     * @return the date of last update
     */
    public String getDate(){
        logger.info("Responding with date");
        return Date;
    }

    /**
     * This method opens a locally saved xml file,
     * and parsing the data into an hashmap
     * the method calculates convertion ratio between
     * every currency to every other currency
     *
     */
    public void updateHashMap() throws CurrencyException{
        NodeList LAST_UPDATE    = null;
        NodeList RATE           = null;
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document doc;

        try {
            logger.info("Reading locally saved updated xml file...");
            factory = DocumentBuilderFactory.newDefaultInstance();
            builder = factory.newDocumentBuilder();
            doc = builder.parse(new InputSource(XML_PATH));
            RATE            = doc.getElementsByTagName("RATE");
            LAST_UPDATE     = doc.getElementsByTagName("LAST_UPDATE");
            Date = LAST_UPDATE.item(0).getFirstChild().getNodeValue();
        } catch (java.net.MalformedURLException e) {
            e.printStackTrace();
            throw new CurrencyException("MalformedURLException",e);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            throw new CurrencyException("IOException",e);
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            e.printStackTrace();
            throw new CurrencyException("ParserConfigurationException",e);
        } catch (org.xml.sax.SAXException e) {
            e.printStackTrace();
            throw new CurrencyException("SAXException",e);
        }
        //
        int i = 0, j;
        for (sagi.neta.CurrencyExchanger.Currency from : sagi.neta.CurrencyExchanger.Currency.values()) {
            Double toShekels = Double.parseDouble(RATE.item(i).getFirstChild().getNodeValue());
            j = 0 ;
            logger.info("Calculating rates from " + from + " to all other currencies...");
            for (sagi.neta.CurrencyExchanger.Currency to : sagi.neta.CurrencyExchanger.Currency.values()) {
                Double toCurr = Double.parseDouble(RATE.item(j).getFirstChild().getNodeValue());
                Double newRate = toShekels / toCurr;
                if (from == sagi.neta.CurrencyExchanger.Currency.JPY) {
                    newRate /= 100;
                } else if (from == sagi.neta.CurrencyExchanger.Currency.LBP) {
                    newRate /= 10;
                } else if (to == sagi.neta.CurrencyExchanger.Currency.JPY) {
                    newRate *= 100;
                } else if (to == sagi.neta.CurrencyExchanger.Currency.LBP) {
                    newRate *= 10;
                }
                exchangeRates.put(new CurrencyPair(from, to), newRate);
                ++j;
            }
            ++i;
        }
        logger.info("Done calculating hashmap with all rates.");

    }
    /**
     * This run method is responsible of fetching new data from
     * bank api, and creating a new identical xml file,
     * and storing that locally.
     *
     */
    @Override
    public void run() {
        InputStream is          = null;
        HttpURLConnection con   = null;
        NodeList LAST_UPDATE    = null;
        NodeList NAME           = null;
        NodeList UNIT           = null;
        NodeList CURRENCYCODE   = null;
        NodeList COUNTRY        = null;
        NodeList RATE           = null;
        NodeList CHANGE         = null;
        URL url;
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document doc = null;
        //Fetch XML from server
        try {
            logger.info("trying to fetch xml from server...");
            url = new URL("https://www.boi.org.il/currency.xml");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();
            is = con.getInputStream();
            factory = DocumentBuilderFactory.newDefaultInstance();
            builder = factory.newDocumentBuilder();
            doc = builder.parse(is);

        } catch (java.net.MalformedURLException e) {
            //e.printStackTrace();
        } catch (java.io.IOException e) {
//            If could not GET xml file, open it locally.
            try {
                logger.info("fetch from server failed. opening xml locally");
                File fXmlFile = new File(BACKUP);
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                doc = dBuilder.parse(fXmlFile);
            }catch(javax.xml.parsers.ParserConfigurationException ex){
                ex.printStackTrace();
            }
            catch (org.xml.sax.SAXException ex){ ex.printStackTrace();}
            catch (IOException ex){ ex.printStackTrace();}
        }
        catch (javax.xml.parsers.ParserConfigurationException e) {
          //  e.printStackTrace();
        }
        catch (org.xml.sax.SAXException e) {
          //  e.printStackTrace();
        }
        NAME            = doc.getElementsByTagName("NAME");
        UNIT            = doc.getElementsByTagName("UNIT");
        CURRENCYCODE    = doc.getElementsByTagName("CURRENCYCODE");
        COUNTRY         = doc.getElementsByTagName("COUNTRY");
        RATE            = doc.getElementsByTagName("RATE");
        CHANGE          = doc.getElementsByTagName("CHANGE");
        LAST_UPDATE     = doc.getElementsByTagName("LAST_UPDATE");
        //Create new XML for local storage
        Document dom        = null;
        Element e           = null;
        Element currencyEle = null;
        String _LAST_UPDATE = null;
        String _NAME        = null;
        String _UNIT        = null;
        String _CURRENCYCODE = null;
        String _COUNTRY     = null;
        String _RATE        = null;
        String _CHANGE      = null;
        Element rootEle     = null;
        //
        // instance of a DocumentBuilderFactory
        logger.info("Building a new xml copy");
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            dom = db.newDocument();
            // create the root element
            rootEle = dom.createElement("CURRENCIES");
            // create data elements and place them under root
            e = dom.createElement("LAST_UPDATE");
            _LAST_UPDATE = LAST_UPDATE.item(0).getFirstChild().getNodeValue();
            e.appendChild(dom.createTextNode(_LAST_UPDATE));
            rootEle.appendChild(e);
        }catch(ParserConfigurationException ex){ex.printStackTrace();}
        //
        //Create all currencies
        int i = 0;
        for (sagi.neta.CurrencyExchanger.Currency from : Currency.values()) {
            // create node currency
            currencyEle = dom.createElement("CURRENCY");
            rootEle.appendChild(currencyEle);
            //
            // add children to node currency
            e = dom.createElement("NAME");
            _NAME = NAME.item(i).getFirstChild().getNodeValue();
            e.appendChild(dom.createTextNode(_NAME));
            currencyEle.appendChild(e);

            e = dom.createElement("UNIT");
            _UNIT = UNIT.item(i).getFirstChild().getNodeValue();
            e.appendChild(dom.createTextNode(_UNIT));
            currencyEle.appendChild(e);

            e = dom.createElement("CURRENCYCODE");
            _CURRENCYCODE = CURRENCYCODE.item(i).getFirstChild().getNodeValue();
            e.appendChild(dom.createTextNode(_CURRENCYCODE));
            currencyEle.appendChild(e);

            e = dom.createElement("COUNTRY");
            _COUNTRY = COUNTRY.item(i).getFirstChild().getNodeValue();
            e.appendChild(dom.createTextNode(_COUNTRY));
            currencyEle.appendChild(e);

            e = dom.createElement("RATE");
            _RATE = RATE.item(i).getFirstChild().getNodeValue();
            e.appendChild(dom.createTextNode(_RATE));
            currencyEle.appendChild(e);

            e = dom.createElement("CHANGE");
            _CHANGE = CHANGE.item(i).getFirstChild().getNodeValue();
            e.appendChild(dom.createTextNode(_CHANGE));
            currencyEle.appendChild(e);
            ++i;
        }
        dom.appendChild(rootEle);
        // write the content into xml file
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(dom);
            StreamResult result = new StreamResult(new File("gui.xml"));
            transformer.transform(source, result);
            logger.info("updated xml file saved!");
        } catch (TransformerException te) {
            logger.info("Failed to save file");
            logger.info(te.getMessage());
        }

    }
}
