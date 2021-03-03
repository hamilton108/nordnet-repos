package nordnet.repos;

import critterrepos.beans.StockPriceBean;
import critterrepos.beans.options.StockOptionBean;
import critterrepos.beans.options.StockOptionPriceBean;
import nordnet.downloader.NordnetRedis;
import nordnet.downloader.PageInfo;
import nordnet.downloader.TickerInfo;
import nordnet.html.StockOptionEnum;
import nordnet.html.Util;
import oahu.dto.Tuple;
import oahu.financial.*;
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

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static nordnet.html.StockOptionEnum.*;
import static nordnet.html.StockOptionStringEnum.INPUT_LABEL_CLASS;

@Component
public class EtradeRepositoryImpl implements EtradeRepository<Tuple<String>> {
    private EtradeDownloader<PageInfo, TickerInfo, Serializable> downloader;
    private StockMarketRepository stockMarketRepos;
    private LocalDate currentDate = LocalDate.now();

    private String storePath;
    private OptionCalculator optionCalculator;

    //private String openingPricesFileName;

    //private List<StockPrice> openingPrices = new ArrayList<>();
    //private final Map<String,Double> openingPrices = new HashMap<>();
    private NordnetRedis nordnetRedis;

    @Override
    public Optional<StockOptionPrice> findDerivativePrice(Tuple<String> optionInfo) {
        return Optional.empty();
    }

    @Override
    public Optional<StockPrice> stockPrice(String ticker) {
        try {
            TickerInfo tickerInfo = new TickerInfo(ticker);
            Document doc = getDocument(tickerInfo);
            Stock stock = stockMarketRepos.findStock(ticker);
            return createStockPrice(doc, stock);
        } catch (Exception e) {
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
    public Collection<StockOptionPrice> puts(String ticker) {
        try {
            Document doc = getDocument(new TickerInfo(ticker));
            Stock stock = stockMarketRepos.findStock(ticker);
            Optional<StockPrice> stockPrice = createStockPrice(doc, stock);
            if (stockPrice.isPresent()) {
                return createDerivatives(doc,stock,stockPrice.get()).stream().filter(d -> d.getDerivative().getOpType() == StockOption.OptionType.PUT).collect(Collectors.toList());
            }
            else {
                throw new RuntimeException("[EtradeRepositoryImpl.calls] stockPrice is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Collection<StockOptionPrice> puts(int oid) {
        String ticker = stockMarketRepos.getTickerFor(oid);
        return puts(ticker);
    }

    @Override
    public Collection<StockOptionPrice> calls(String ticker) {
        try {
            Document doc = getDocument(new TickerInfo(ticker));
            Stock stock = stockMarketRepos.findStock(ticker);
            Optional<StockPrice> stockPrice = createStockPrice(doc, stock);
            if (stockPrice.isPresent()) {
                return createDerivatives(doc,stock,stockPrice.get()).stream().filter(d -> d.getDerivative().getOpType() == StockOption.OptionType.CALL).collect(Collectors.toList());
            }
            else {
                throw new RuntimeException("[EtradeRepositoryImpl.calls] stockPrice is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Collection<StockOptionPrice> calls(int oid) {
        String ticker = stockMarketRepos.getTickerFor(oid);
        return calls(ticker);
    }

    @Override
    public Collection<StockOption> callPutDefs(int oid) {
        String ticker = stockMarketRepos.getTickerFor(oid);
        return callPutDefs(ticker);
    }

    @Override
    public Collection<StockOption> callPutDefs(String ticker) {
        try {
            Document doc = getDocument(new TickerInfo(ticker));
            return createDefs(doc);
        } catch (Exception e) {
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
    public void setDownloader(EtradeDownloader<PageInfo, TickerInfo, Serializable> downloader) {
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
    
    /*
    public String getOpeningPricesFileName() {
        return String.format("%s/%s",storePath,openingPricesFileName);
    }
    public void setOpeningPricesFileName(String openingPricesFileName) {
        this.openingPricesFileName = openingPricesFileName;
    }

    public Map<String,Double> getOpeningPrices() {
        return openingPrices;
    }
    
    public void setOpeningPrices(List<StockPrice> openingPrices) {
        this.openingPrices = openingPrices;
    }
     */

    //-----------------------------------------------------------
    //-------------- Public methods --------------------
    //-----------------------------------------------------------
    public void initOpeningPrices() {
        var prices = nordnetRedis.fetchPrices(currentDate);
        if (prices.isEmpty()) {

        }
        else {
            Map<String, Double> openingPrices = mapOpeningPrices(prices);
        }
    }

    private Map<String, Double> mapOpeningPrices(Map<String, String> prices) {
        var result = new HashMap<String,Double>();
        for (var items : prices.entrySet()) {
            var value = Double.parseDouble(items.getValue());
            result.put(items.getKey(), value);
        }
        return result;
    }

    private double fetchOpeningPrice(String ticker) {
        return nordnetRedis.fetchPrice(ticker);
    }

    //-----------------------------------------------------------
    //-------------- Package/private methods --------------------
    //-----------------------------------------------------------
    private Collection<StockOption> createDefs(Document doc) {
        Collection<StockOption> result = new ArrayList<>();
        //Elements tables = doc.getElementsByClass(TABLE_CLASS.getText());
        Elements tables = doc.getElementsByTag("tbody");
        Element table2 = tables.get(TABLE_DERIVATIVES.getIndex());
        Elements rows = table2.getElementsByTag("tr");
        for (Element row : rows)  {
            Elements tds = row.getElementsByTag("td");
            StockOptionBean d = new StockOptionBean();
            d.setLifeCycle(StockOption.LifeCycle.FROM_HTML);
            d.setOpType(StockOption.OptionType.CALL);

            Element ticker = tds.get(CALL_TICKER.getIndex());
            d.setTicker(ticker.text());

            Element td_X = tds.get(X.getIndex());
            double x = Util.parseExercisePrice(td_X.text());
            d.setX(x);

            result.add(d);
        }
        return result;
    }

    private Collection<StockOptionPrice> createDerivatives(Document doc, Stock stock, StockPrice stockPrice) {
        Collection<StockOptionPrice> result = new ArrayList<>();
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
                StockOptionPriceBean callPrice = new StockOptionPriceBean();
                Optional<StockOption> callDerivative = fetchOrCreateDerivative(tds, StockOption.OptionType.CALL);
                callDerivative.ifPresent(cx -> {
                    ((StockOptionBean) cx).setStock(stock);
                    //((DerivativeBean)cx).setExpiry(expiry.get());
                    callPrice.setDerivative(cx);
                    callPrice.setStockPrice(stockPrice);

                    Element bid_e = tds.get(CALL_BID.getIndex());
                    double bid = Util.decimalStringToDouble(bid_e.text());
                    callPrice.setBuy(bid);

                    Element ask_e = tds.get(CALL_ASK.getIndex());
                    double ask = Util.decimalStringToDouble(ask_e.text());
                    callPrice.setSell(ask);
                    callPrice.setCalculator(optionCalculator);
                    result.add(callPrice);
                });

                //-------------------- PUTS -----------------------
                StockOptionPriceBean putPrice = new StockOptionPriceBean();
                Optional<StockOption> putDerivative = fetchOrCreateDerivative(tds, StockOption.OptionType.PUT);
                putDerivative.ifPresent(px -> {
                    ((StockOptionBean) px).setStock(stock);
                    //((DerivativeBean)px).setExpiry(expiry.get());
                    putPrice.setDerivative(px);
                    putPrice.setStockPrice(stockPrice);

                    Element bid_e = tds.get(PUT_BID.getIndex());
                    double bid = Util.decimalStringToDouble(bid_e.text());
                    putPrice.setBuy(bid);

                    Element ask_e = tds.get(PUT_ASK.getIndex());
                    double ask = Util.decimalStringToDouble(ask_e.text());
                    putPrice.setSell(ask);

                    putPrice.setCalculator(optionCalculator);
                    result.add(putPrice);
                });

            }
            catch (Exception e) {
                //System.out.println(e.getMessage());
            }
        }
        return result;
    }

    private Optional<StockOption> fetchOrCreateDerivative(Elements tds, StockOption.OptionType optionType) {
        StockOptionEnum du = optionType == StockOption.OptionType.CALL ? CALL_TICKER : PUT_TICKER;
        Element ticker = tds.get(du.getIndex());
        Optional<StockOption> found = stockMarketRepos.findDerivative(ticker.text());

        if (found.isEmpty()) {
            StockOptionBean derivative2 = new StockOptionBean();

            derivative2.setTicker(ticker.text());
            derivative2.setLifeCycle(StockOption.LifeCycle.FROM_HTML);
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

        double open = fetchOpeningPrice(stock.getTicker()); //  openingPrices.get(stock.getTicker());

        StockPriceBean result = new StockPriceBean();
        result.setOpn(open);
        result.setHi(hi);
        result.setLo(lo);
        result.setCls(close);
        result.setStock(stock);
        result.setVolume(1000);
        result.setLocalDx(currentDate);
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
    
    /*
    private double textNodeToDouble(TextNode el) {
        return Double.parseDouble(el.text());
    }
     */

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

    Document getDocument(TickerInfo tickerInfo) {
        return getDocuments(tickerInfo).get(0);
    }
    
    List<Document> getDocuments(TickerInfo tickerInfo) {
        List<Document> result = new ArrayList<>();
        List<PageInfo> pages = downloader.downloadDerivatives(tickerInfo);

        for (var page : pages) {
            result.add(Jsoup.parse(page.getPage().getWebResponse().getContentAsString()));
        }
        return result;
    }

    public void setNordnetRedis(NordnetRedis nordnetRedis) {
        this.nordnetRedis = nordnetRedis;    
    }

    public void setCurrentDate(LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    public void setOptionCalculator(OptionCalculator optionCalculator) {
        this.optionCalculator = optionCalculator;
    }
    /*
    Tuple2<LocalDate, LocalTime> getTimeInfo(Element el) {
        // fredag 23/11-2018 18:30:05
        String txt = el.text();
        String[] txts = txt.split("\\s");
        LocalDate ld = LocalDate.parse(txts[1], dateFormatter);
        LocalTime tm = LocalTime.parse(txts[2], timeFormatter);
        return new Tuple2<>(ld,tm);
    }
     */
    
    
    //private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM-yyyy");
    //private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

}
