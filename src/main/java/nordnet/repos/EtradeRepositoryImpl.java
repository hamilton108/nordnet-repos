package nordnet.repos;

import com.gargoylesoftware.htmlunit.Page;
import critterrepos.beans.StockPriceBean;
import nordnet.downloader.TickerInfo;
import nordnet.exception.SecurityNotFoundException;
import nordnet.html.DerivativesEnum;
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
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.print.Doc;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static nordnet.html.DerivativesEnum.*;
import static nordnet.html.DerivativesStringEnum.*;

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
        return Optional.empty();
    }

    @Override
    public Optional<StockPrice> stockPrice(int oid) {
        return Optional.empty();
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
        return null;
    }

    @Override
    public Collection<DerivativePrice> calls(int oid) {
        return null;
    }

    @Override
    public Collection<Derivative> callPutDefs(String ticker) {
        return null;
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
            FileWriter writer = new FileWriter(getOpeningPricesFileName());
            PrintWriter printWriter = new PrintWriter(writer);
            Collection<Stock> stocks = stockMarketRepos.getStocks();
            for (Stock stock : stocks) {
                String tik = stock.getTicker();
                TickerInfo tickerInfo = new TickerInfo(tik);
                Document doc = getDocument(tickerInfo);
                Elements tds = stockPriceTds(doc);

                double close = elementTextToDouble(stockPriceElement(tds, STOCK_PRICE_CLOSE));

                printWriter.println(String.format(Locale.US,"%s:%.2f", tik, close));

                openingPrices.put(tik, close);

                /*
                String tik = stock.getTicker();
                TickerInfo tickerInfo = new TickerInfo(tik);
                Document doc = getDocument(tickerInfo);
                Optional<StockPrice> stockPrice = createStockPrice(doc, stock);
                if (!stockPrice.isPresent()) {
                    throw new SecurityNotFoundException(String.format("Stock price for %s was Optional.empty()", tik));
                }
                stockPrice.ifPresent(s -> {
                    double opn = s.getCls();
                    printWriter.println(String.format(Locale.US,"%s:%.2f", tik, opn));
                    StockPriceBean price = new StockPriceBean();
                    price.setOpn(opn);
                    price.setStock(stock);
                    openingPrices.add(price);
                });
                 */
            }
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------------------------
    //-------------- Package/private methods --------------------
    //-----------------------------------------------------------
    Optional<StockPrice> createStockPrice(Document doc, Stock stock) {
        Elements tds =  stockPriceTds(doc);

        double close = elementTextToDouble(stockPriceElement(tds, STOCK_PRICE_CLOSE));

        double hi = elementTextToDouble(stockPriceElement(tds, STOCK_PRICE_Hi));

        double lo = elementTextToDouble(stockPriceElement(tds, STOCK_PRICE_Lo));

        return Optional.empty();
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
