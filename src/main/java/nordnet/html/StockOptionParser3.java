package nordnet.html;

import critter.repos.StockMarketRepository;
import critter.stock.Stock;
import critter.stock.StockPrice;
import critter.stockoption.StockOption;
import critter.stockoption.StockOptionPrice;
import critter.util.StockOptionUtil;
import nordnet.downloader.PageInfo;
import nordnet.downloader.TickerInfo;
import nordnet.redis.NordnetRedis;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import vega.financial.calculator.OptionCalculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class StockOptionParser3 extends  StockOptionParserBase implements StockOptionParser {

    private final int SP_CLS = 4;
    private final int SP_HI = 7;
    private final int SP_LO = 8;

    private final int CALL_TICKER = 1;
    private final int CALL_BID = 3;
    private final int CALL_ASK = 4;

    private final int X = 7;

    private final int PUT_BID = 9;
    private final int PUT_ASK = 10;
    private final int PUT_TICKER = 13;

    public StockOptionParser3(OptionCalculator optionCalculator,
                              NordnetRedis nordnetRedis,
                              StockMarketRepository stockMarketRepos,
                              StockOptionUtil stockOptionUtils) {
        super(optionCalculator, nordnetRedis, stockMarketRepos, stockOptionUtils);
    }


    @Override
    public StockPrice stockPrice(TickerInfo tickerInfo, PageInfo pageInfo) {
        Stock stock = stockMarketRepos.findStock(tickerInfo.getTicker());
        var doc = Jsoup.parse(pageInfo.getPage().getWebResponse().getContentAsString());
        return createStockPrice(doc, stock);
    }

    private StockPrice createStockPrice(Document doc, Stock stock) {
        double opn = nordnetRedis.fetchPrice(stock.getTicker());
        var elements = doc.select(".bRPJha");
        try {
            var spRow = elements.get(0).children();
            var cls = elementToDouble(elementFor(spRow.get(SP_CLS)));
            var hi = elementToDouble(elementFor(spRow.get(SP_HI)));
            var lo = elementToDouble(elementFor(spRow.get(SP_LO)));
            var stockPrice = createStockPrice(opn, hi, lo, cls, stock);
            return stockPrice;
        }
        catch (Exception ex) {
            return null;
        }
    }

    private Element elementFor(Element el) {
        return el.children().get(0);
    }

    @Override
    public Collection<StockOptionPrice> options(PageInfo pageInfo, StockPrice stockPrice) {
        var result = new ArrayList<StockOptionPrice>();

        var doc = Jsoup.parse(pageInfo.getPage().getWebResponse().getContentAsString());
        var elements = doc.select(".bRPJha");

        for (var el : elements) {
            if (el.children().size() == 16) {
                //System.out.println(el);
                var row = el.children();
                var callTickerText = el2str(row.get(CALL_TICKER));
                System.out.println(callTickerText);
                var callTicker = elementToTicker(callTickerText);
                if (callTicker == null) {
                    continue;
                }
                var putTicker = elementToTicker(el2str(row.get(PUT_TICKER)));
                var callBid = str2double(el2str(row.get(CALL_BID)));
                var callAsk = str2double(el2str(row.get(CALL_ASK)));
                var putBid = str2double(el2str(row.get(PUT_BID)));
                var putAsk = str2double(el2str(row.get(PUT_ASK)));
                var x = str2double(el2str(row.get(X)));
            }
        }

        return result;
    }

    private StockOptionPrice createOption(String ticker,
                                          double x,
                                          StockPrice stockPrice,
                                          StockOption.OptionType ot) {
        StockOption stockOption = fetchOrCreateStockOption(ticker, x, ot, stockPrice);
        return null;
    }

    public String elementToTicker(String elementText) {
        var l1 = elementText.split("\\s");
        var l2 = l1[1].split("\\.");
        if (l2.length > 1) {
            return null;
        }
        else {
            return l2[0];
        }
    }

    private String el2str(Element el) {
        return el.children().get(0).text();
    }
    private double str2double(String s) {
        return Double.parseDouble(s.replace(",", "."));
    }
}

/*
package com.cyanogenmod.updater.utils;|

        import android.text.TextUtils;
        import android.util.Log;

        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileNotFoundException;
        import java.io.IOException;
        import java.io.InputStream;
        import java.math.BigInteger;
        import java.security.MessageDigest;
        import java.security.NoSuchAlgorithmException;

public class MD5 {
    private static final String TAG = "MD5";

    public static boolean checkMD5(String md5, File updateFile) {
        if (TextUtils.isEmpty(md5) || updateFile == null) {
            Log.e(TAG, "MD5 string empty or updateFile null");
            return false;
        }

        String calculatedDigest = calculateMD5(updateFile);
        if (calculatedDigest == null) {
            Log.e(TAG, "calculatedDigest null");
            return false;
        }

        Log.v(TAG, "Calculated digest: " + calculatedDigest);
        Log.v(TAG, "Provided digest: " + md5);

        return calculatedDigest.equalsIgnoreCase(md5);
    }

    public static String calculateMD5(File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Exception while getting digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }
}
 */