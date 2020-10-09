package nordnet.repos;

import com.gargoylesoftware.htmlunit.Page;
import critterrepos.beans.StockPriceBean;
import critterrepos.beans.options.DerivativeBean;
import critterrepos.beans.options.DerivativePriceBean;
import nordnet.downloader.TickerInfo;
import nordnet.html.DerivativesEnum;
import nordnet.html.Util;
import oahu.dto.Tuple;
import oahu.dto.Tuple2;
import oahu.financial.Derivative;
import oahu.financial.DerivativePrice;
import oahu.financial.Stock;
import oahu.financial.StockPrice;
import oahu.financial.html.EtradeDownloader;
import oahu.financial.repository.EtradeRepository;
import oahu.financial.repository.StockMarketRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static nordnet.html.DerivativesEnum.*;
import static nordnet.html.DerivativesStringEnum.INPUT_LABEL_CLASS;

@Component
public class EtradeRepositoryImpl implements EtradeRepository<Tuple<String>> {
    private EtradeDownloader<Page, TickerInfo, Serializable> downloader;
    private StockMarketRepository stockMarketRepos;

    private String storePath;

    private String openingPricesFileName;

    //private List<StockPrice> openingPrices = new ArrayList<>();
    private Map<String,Double> openingPrices = new HashMap<>();

    @Override
    public Optional<DerivativePrice> findDerivativePrice(Tuple<String> optionInfo) {
        return Optional.empty();
    }

    @Override
    public Optional<StockPrice> stockPrice(String ticker) {
        try {
            TickerInfo tickerInfo = new TickerInfo(ticker);
            Document doc = getDocument(tickerInfo);
            Stock stock = stockMarketRepos.findStock(ticker);
            return createStockPrice(doc, stock);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<StockPrice> stockPrice(int oid) {
        String ticker = stockMarketRepos.getTickerFor(oid);
        return stockPrice(ticker);
    }

    @Override
    public Collection<DerivativePrice> puts(String ticker) {
        try {
            Document doc = getDocument(new TickerInfo(ticker));
            Stock stock = stockMarketRepos.findStock(ticker);
            return createDerivatives(doc,stock).stream().filter(d -> d.getDerivative().getOpType() == Derivative.OptionType.PUT).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Collection<DerivativePrice> puts(int oid) {
        String ticker = stockMarketRepos.getTickerFor(oid);
        return puts(ticker);
    }

    @Override
    public Collection<DerivativePrice> calls(String ticker) {
        try {
            Document doc = getDocument(new TickerInfo(ticker));
            Stock stock = stockMarketRepos.findStock(ticker);
            return createDerivatives(doc,stock).stream().filter(d -> d.getDerivative().getOpType() == Derivative.OptionType.CALL).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Collection<DerivativePrice> calls(int oid) {
        String ticker = stockMarketRepos.getTickerFor(oid);
        return calls(ticker);
    }

    @Override
    public Collection<Derivative> callPutDefs(int oid) {
        String ticker = stockMarketRepos.getTickerFor(oid);
        return callPutDefs(ticker);
    }

    @Override
    public Collection<Derivative> callPutDefs(String ticker) {
        try {
            Document doc = getDocument(new TickerInfo(ticker));
            return createDefs(doc);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public void setDownloadDate(LocalDate localDate) {

    }

    @Override
    public void invalidateCache() {

    }

    //-----------------------------------------------------------
    //--------------------- Properties --------------------------
    //-----------------------------------------------------------
    @Autowired
    public void setDownloader(EtradeDownloader<Page, TickerInfo, Serializable> downloader) {
        this.downloader = downloader;
    }

    @Autowired
    public void setStockMarketRepository(StockMarketRepository stockMarketRepos) {
        this.stockMarketRepos = stockMarketRepos;
    }

    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }

    public String getStorePath() {
        return storePath;
    }
    public String getOpeningPricesFileName() {
        return String.format("%s/%s",storePath,openingPricesFileName);
    }
    public void setOpeningPricesFileName(String openingPricesFileName) {
        this.openingPricesFileName = openingPricesFileName;
    }

    public Map<String,Double> getOpeningPrices() {
        return openingPrices;
    }
    /*
    public void setOpeningPrices(List<StockPrice> openingPrices) {
        this.openingPrices = openingPrices;
    }
     */

    //-----------------------------------------------------------
    //-------------- Public methods --------------------
    //-----------------------------------------------------------
    public void initOpeningPrices() {
        if (openingPrices.size() > 0) {
            return;
        }
        try {
            File cachedOpeningPrices = new File(getOpeningPricesFileName());
            if (cachedOpeningPrices.exists()) {
                List<String> lines = Files.readAllLines(Paths.get(getOpeningPricesFileName()));
                for (String line : lines) {
                    String[] linex = line.split(":");
                    Double price = Double.parseDouble(linex[1]);
                    openingPrices.put(linex[0], price);
                }
            }
            else {
                FileWriter writer = new FileWriter(getOpeningPricesFileName());
                PrintWriter printWriter = new PrintWriter(writer);
                Collection<Stock> stocks = stockMarketRepos.getStocks();
                for (Stock stock : stocks) {
                    String tik = stock.getTicker();
                    TickerInfo tickerInfo = new TickerInfo(tik);
                    Document doc = getDocument(tickerInfo);
                    Elements tds = stockPriceTds(doc);

                    double close = getLast(tds); //textNodeToDouble(stockPriceElement(tds, STOCK_PRICE_CLOSE));

                    printWriter.println(String.format(Locale.US, "%s:%.2f", tik, close));

                    openingPrices.put(tik, close);
                }
                printWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------------------------
    //-------------- Package/private methods --------------------
    //-----------------------------------------------------------
    private Collection<Derivative> createDefs(Document doc) {
        Collection<Derivative> result = new ArrayList<>();
        //Elements tables = doc.getElementsByClass(TABLE_CLASS.getText());
        Elements tables = doc.getElementsByTag("tbody");
        Element table2 = tables.get(TABLE_DERIVATIVES.getIndex());
        Elements rows = table2.getElementsByTag("tr");
        for (Element row : rows)  {
            Elements tds = row.getElementsByTag("td");
            DerivativeBean d = new DerivativeBean();
            d.setLifeCycle(Derivative.LifeCycle.FROM_HTML);
            d.setOpType(Derivative.OptionType.CALL);

            Element ticker = tds.get(CALL_TICKER.getIndex());
            d.setTicker(ticker.text());

            Element td_X = tds.get(X.getIndex());
            double x = Util.parseExercisePrice(td_X.text());
            d.setX(x);

            result.add(d);
        }
        return result;
    }

    private Collection<DerivativePrice> createDerivatives(Document doc, Stock stock) {
        Collection<DerivativePrice> result = new ArrayList<>();
        //Elements tables = doc.getElementsByClass(TABLE_CLASS.getText());
        Elements tables = doc.getElementsByTag("tbody");
        Element table2 = tables.get(TABLE_DERIVATIVES.getIndex());
        Elements rows = table2.getElementsByTag("tr");
        /*
        Optional<LocalDate> expiry = htmlDate(doc);
        if (expiry.isEmpty()) {
            throw new SecurityParseErrorException("Error parsing expiry date");
        }

         */
        for (Element row : rows) {
            try {
                Elements tds = row.getElementsByTag("td");

                //-------------------- CALLS -----------------------
                DerivativePriceBean callPrice = new DerivativePriceBean();
                Optional<Derivative> callDerivative = fetchOrCreateDerivative(tds, Derivative.OptionType.CALL);
                callDerivative.ifPresent(cx -> {
                    ((DerivativeBean) cx).setStock(stock);
                    //((DerivativeBean)cx).setExpiry(expiry.get());
                    callPrice.setDerivative(cx);

                    Element bid_e = tds.get(CALL_BID.getIndex());
                    double bid = Util.decimalStringToDouble(bid_e.text());
                    callPrice.setBuy(bid);

                    Element ask_e = tds.get(CALL_ASK.getIndex());
                    double ask = Util.decimalStringToDouble(ask_e.text());
                    callPrice.setSell(ask);

                    result.add(callPrice);
                });

                //-------------------- PUTS -----------------------
                DerivativePriceBean putPrice = new DerivativePriceBean();
                Optional<Derivative> putDerivative = fetchOrCreateDerivative(tds, Derivative.OptionType.PUT);
                putDerivative.ifPresent(px -> {
                    ((DerivativeBean) px).setStock(stock);
                    //((DerivativeBean)px).setExpiry(expiry.get());
                    putPrice.setDerivative(px);

                    Element bid_e = tds.get(PUT_BID.getIndex());
                    double bid = Util.decimalStringToDouble(bid_e.text());
                    putPrice.setBuy(bid);

                    Element ask_e = tds.get(PUT_ASK.getIndex());
                    double ask = Util.decimalStringToDouble(ask_e.text());
                    putPrice.setSell(ask);

                    result.add(putPrice);
                });

            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return result;
    }

    private Optional<Derivative> fetchOrCreateDerivative(Elements tds, Derivative.OptionType optionType) {
        DerivativesEnum du = optionType == Derivative.OptionType.CALL ? CALL_TICKER : PUT_TICKER;
        Element ticker = tds.get(du.getIndex());
        Optional<Derivative> found = stockMarketRepos.findDerivative(ticker.text());

        if (found.isEmpty()) {
            DerivativeBean derivative2 = new DerivativeBean();

            derivative2.setTicker(ticker.text());
            derivative2.setLifeCycle(Derivative.LifeCycle.FROM_HTML);
            derivative2.setOpType(optionType);

            Element xe = tds.get(X.getIndex());
            double x = Util.parseExercisePrice(xe.text());
            derivative2.setX(x);

            found = Optional.of(derivative2);

            /*
            double x = Double.parseDouble(el.child(2).text());
            String child3txt = el.child(3).text();
            LocalDate exp = LocalDate.parse(child3txt, dateFormatter);

            derivative2.setTicker(optionName);
            derivative2.setX(x);
            derivative2.setExpiry(exp);
            derivative2.setStock(stock);

             */
        }
        return found;
    }

    Optional<LocalDate> htmlDate(Document doc) {
        Elements inputSelectValues = doc.getElementsByClass(INPUT_LABEL_CLASS.getText());
        //assertThat(inputSelectValues.size()).as("input__value-label").isEqualTo(3);

        Element dateEl = inputSelectValues.get(SELECT_INPUT_DATE.getIndex());

        String[] split = dateEl.text().split("\\.");

        int year = Integer.parseInt(split[2]);
        int month = Integer.parseInt(split[1]);
        int day = Integer.parseInt(split[0]);
        //assertThat(dateEl.text()).as("date input").isEqualTo("20.12.2019");
        return Optional.of(LocalDate.of(year,month,day));
    }

    Optional<StockPrice> createStockPrice(Document doc, Stock stock) {
        Elements tds =  stockPriceTds(doc);

        double close = getLast(tds); //textNodeToDouble(stockPriceElement(tds, STOCK_PRICE_CLOSE));

        double hi = getHi(tds); //textNodeToDouble(stockPriceElement(tds, STOCK_PRICE_Hi));

        double lo = getLo(tds); //textNodeToDouble(stockPriceElement(tds, STOCK_PRICE_Lo));

        double open = openingPrices.get(stock.getTicker());

        StockPriceBean result = new StockPriceBean();
        result.setOpn(open);
        result.setHi(hi);
        result.setLo(lo);
        result.setCls(close);
        result.setStock(stock);
        result.setVolume(1000);
        return Optional.of(result);
    }

    private Elements stockPriceTds(Document doc)  {
        //Elements tables = doc.getElementsByClass("c01408");
        //Elements tables = doc.getElementsByClass(TABLE_CLASS.getText());
        Elements tables = doc.getElementsByTag("tbody");

        //Element table1 = tables.first();
        Element table = tables.get(TABLE_STOCK_PRICE.getIndex());
        Elements rows = table.getElementsByTag("tr");

        Element row1 = rows.first();
        return row1.getElementsByTag("td");
    }

    private double textNodeToDouble(TextNode el) {
        return Double.parseDouble(el.text());
    }

    /*
    private double elementTextToDouble(Element el) {
        return Double.parseDouble(el.text());
    }
    private TextNode stockPriceElement(Elements tds, DerivativesEnum rowIndex) {
        Element row = tds.get(rowIndex.getIndex());
        //return row.getElementsByClass("c01438").first();
        //return row.getElementsByClass(TD_CLASS.getText()).first();
        return Util.getTd(row);
    }
     */

    private double getLast(Elements tds) {
        Element td = tds.get(STOCK_PRICE_CLOSE.getIndex());
        TextNode node = (TextNode)td.childNode(0).childNode(0).childNode(2);
        return Double.parseDouble(node.text());
    }
    private double getHi(Elements tds) {
        Element td = tds.get(STOCK_PRICE_HI.getIndex());
        return Util.decimalStringToDouble(td.text());
    }
    private double getLo(Elements tds) {
        Element td = tds.get(STOCK_PRICE_LO.getIndex());
        return Util.decimalStringToDouble(td.text());
    }

    Document getDocument(TickerInfo tickerInfo) throws IOException {
        /*
        Page page = downloader.downloadDerivatives(tickerInfo);
        return Jsoup.parse(page.getWebResponse().getContentAsString());

         */
        return getDocuments(tickerInfo).get(0);
    }
    List<Document> getDocuments(TickerInfo tickerInfo) {
        List<Document> result = new ArrayList<>();
        List<Page> pages = downloader.downloadDerivatives(tickerInfo);

        for (var page : pages) {
            result.add(Jsoup.parse(page.getWebResponse().getContentAsString()));
        }
        return result;
    }

    Tuple2<LocalDate, LocalTime> getTimeInfo(Element el) {
        // fredag 23/11-2018 18:30:05
        String txt = el.text();
        String[] txts = txt.split("\\s");
        LocalDate ld = LocalDate.parse(txts[1], dateFormatter);
        LocalTime tm = LocalTime.parse(txts[2], timeFormatter);
        return new Tuple2<>(ld,tm);
    }
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM-yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

}
