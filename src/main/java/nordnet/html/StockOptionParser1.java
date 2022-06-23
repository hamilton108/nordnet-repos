package nordnet.html;

import critter.repos.StockMarketRepository;
import critter.stock.Stock;
import critter.stock.StockPrice;
import critter.stockoption.StockOption;
import critter.stockoption.StockOptionPrice;
import critter.util.StockOptionUtil;
import nordnet.redis.NordnetRedis;
import nordnet.downloader.PageInfo;
import nordnet.downloader.TickerInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import vega.financial.calculator.OptionCalculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static nordnet.html.StockOptionEnum.*;
import static nordnet.html.StockOptionEnum.STOCK_PRICE_LO;
import static vega.financial.StockOption.OptionType.CALL;
import static vega.financial.StockOption.OptionType.PUT;

public class StockOptionParser1 extends  StockOptionParserBase implements StockOptionParser {

    public StockOptionParser1(OptionCalculator optionCalculator,
                              NordnetRedis nordnetRedis,
                              StockMarketRepository<String,String> stockMarketRepos,
                              StockOptionUtil stockOptionUtil) {
        //super(stockOptionUtil);
        super(optionCalculator, nordnetRedis, stockMarketRepos, stockOptionUtil);
    }

    @Override
    public StockPrice stockPrice(TickerInfo tickerInfo, PageInfo pageInfo) {
        Stock stock = stockMarketRepos.findStock(tickerInfo.getTicker());
        var doc = Jsoup.parse(pageInfo.getPage().getWebResponse().getContentAsString());
        return createStockPrice(doc, stock);
    }

    StockPrice createStockPrice(Document doc, Stock stock) {
        Elements tds =  stockPriceTds(doc);

        double close = getLast(tds); //textNodeToDouble(stockPriceElement(tds, STOCK_PRICE_CLOSE));

        double hi = getHi(tds); //textNodeToDouble(stockPriceElement(tds, STOCK_PRICE_Hi));

        double lo = getLo(tds); //textNodeToDouble(stockPriceElement(tds, STOCK_PRICE_Lo));

        double open = fetchOpeningPrice(stock.getTicker()); //  openingPrices.get(stock.getTicker());
        var result = createStockPrice(open, hi, lo, close, stock);
        return result;
    }

    public Collection<StockOptionPrice> calls(Collection<StockOptionPrice> options) {
        return callsOrPuts(options,CALL);
    }
    public Collection<StockOptionPrice> puts(Collection<StockOptionPrice> options) {
        return callsOrPuts(options,PUT);
    }
    public Collection<StockOptionPrice> callsOrPuts(Collection<StockOptionPrice> options, StockOption.OptionType optionType) {
        return options.stream().filter(x -> x.getStockOption().getOpType().equals(optionType)).collect(Collectors.toList());
    }

    @Override
    public Collection<StockOptionPrice> options(PageInfo pageInfo, StockPrice stockPrice) {
        Collection<StockOptionPrice> result = new ArrayList<>();
        var doc = Jsoup.parse(pageInfo.getPage().getWebResponse().getContentAsString());
        Elements tables = doc.getElementsByTag("tbody");
        Element table2 = tables.get(TABLE_DERIVATIVES.getIndex());
        Elements rows = table2.getElementsByTag("tr");

        for (Element row : rows) {
            try {
                Elements tds = row.getElementsByTag("td");

                //-------------------- CALLS -----------------------
                StockOptionPrice callPrice = new StockOptionPrice();
                Optional<StockOption> callDerivative = fetchOrCreateDerivative(tds, CALL);
                callDerivative.ifPresent(cx -> {
                    cx.setStock(stockPrice.getStock());
                    //((DerivativeBean)cx).setExpiry(expiry.get());
                    callPrice.setStockOption(cx);
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
                var putPrice = new StockOptionPrice();
                Optional<StockOption> putDerivative = fetchOrCreateDerivative(tds, PUT);
                putDerivative.ifPresent(px -> {
                    px.setStock(stockPrice.getStock());
                    //((DerivativeBean)px).setExpiry(expiry.get());
                    putPrice.setStockOption(px);
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
        StockOptionEnum du = optionType == CALL ? CALL_TICKER : PUT_TICKER;
        Element ticker = tds.get(du.getIndex());
        Optional<StockOption> found = stockMarketRepos.findStockOption(ticker.text());

        if (found.isEmpty()) {
            var derivative2 = new StockOption();

            derivative2.setTicker(ticker.text());
            derivative2.setLifeCycle(StockOption.LifeCycle.FROM_HTML);
            derivative2.setOpType(optionType);

            Element xe = tds.get(X.getIndex());
            double x = Util.parseExercisePrice(xe.text());
            derivative2.setX(x);
            derivative2.setStockOptionUtil(stockOptionUtil);

            // ===>>>

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
    private double fetchOpeningPrice(String ticker) {
        return nordnetRedis.fetchPrice(ticker);
    }
}

