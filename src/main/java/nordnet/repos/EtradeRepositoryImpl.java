package nordnet.repos;

import com.gargoylesoftware.htmlunit.Page;
import critterrepos.beans.StockPriceBean;
import critterrepos.beans.options.DerivativeBean;
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
import org.jsoup.nodes.Node;
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

import static nordnet.html.DerivativesEnum.*;
import static nordnet.html.DerivativesStringEnum.TABLE_CLASS;
import static nordnet.html.DerivativesStringEnum.TD_CLASS;

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
        return null;
    }

    @Override
    public Collection<DerivativePrice> puts(int oid) {
        return null;
    }

    @Override
    public Collection<DerivativePrice> calls(String ticker) {
        try {
            Document doc = getDocument(new TickerInfo(ticker));
            return createDerivatives(doc);
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
    public void setDownloader(EtradeDownloader downloader) {
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

                    double close = elementTextToDouble(stockPriceElement(tds, STOCK_PRICE_CLOSE));

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
    Collection<Derivative> createDefs(Document doc) {
        Collection<Derivative> result = new ArrayList<>();
        Elements tables = doc.getElementsByClass(TABLE_CLASS.getText());
        Element table2 = tables.get(2);
        Elements rows = table2.getElementsByTag("tr");
        for (Element row : rows)  {
            Elements tds = row.getElementsByTag("td");
            DerivativeBean d = new DerivativeBean();
            d.setLifeCycle(Derivative.LifeCycle.FROM_HTML);

            Element ticker = tds.get(CALL_TICKER.getIndex());
            d.setTicker(ticker.text());

            Element td_X = tds.get(X.getIndex());
            double x = Util.parseExercisePrice(td_X.text());
            d.setX(x);

            result.add(d);
        }
        return result;
    }

    Collection<DerivativePrice> createDerivatives(Document doc) {
        Elements tables = doc.getElementsByClass(TABLE_CLASS.getText());
        Element table2 = tables.get(2);
        Elements rows = table2.getElementsByTag("tr");
        for (Element row : rows)  {
            Elements tds = row.getElementsByTag("td");
            Element ticker = tds.get(CALL_TICKER.getIndex());

        }
        return new ArrayList<>();
    }

    Optional<StockPrice> createStockPrice(Document doc, Stock stock) {
        Elements tds =  stockPriceTds(doc);

        double close = elementTextToDouble(stockPriceElement(tds, STOCK_PRICE_CLOSE));

        double hi = elementTextToDouble(stockPriceElement(tds, STOCK_PRICE_Hi));

        double lo = elementTextToDouble(stockPriceElement(tds, STOCK_PRICE_Lo));

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
        Elements tables = doc.getElementsByClass(TABLE_CLASS.getText());

        Element table1 = tables.first();
        Elements rows = table1.getElementsByTag("tr");

        Element row1 = rows.first();
        return row1.getElementsByTag("td");
    }

    private double elementTextToDouble(Element el) {
        return Double.parseDouble(el.text());
    }
    private Element stockPriceElement(Elements tds, DerivativesEnum rowIndex) {
        Element row = tds.get(rowIndex.getIndex());
        //return row.getElementsByClass("c01438").first();
        return row.getElementsByClass(TD_CLASS.getText()).first();
    }

    Document getDocument(TickerInfo tickerInfo) throws IOException {
        Page page = downloader.downloadDerivatives(tickerInfo);
        return Jsoup.parse(page.getWebResponse().getContentAsString());
    }

    Tuple2<LocalDate, LocalTime> getTimeInfo(Element el) {
        // fredag 23/11-2018 18:30:05
        String txt = el.text();
        String[] txts = txt.split("\\s");
        LocalDate ld = LocalDate.parse(txts[1], dateFormatter);
        LocalTime tm = LocalTime.parse(txts[2], timeFormatter);
        return new Tuple2<>(ld,tm);
    }
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM-yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

}
