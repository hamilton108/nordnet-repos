package nordnet.repos;

import critterrepos.beans.StockBean;
import oahu.exceptions.FinancialException;
import oahu.financial.*;
import oahu.financial.repository.StockMarketRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class StockMarketReposStub implements StockMarketRepository {
    @Override
    public void insertDerivative(StockOption stockOption, Consumer<Exception> errorHandler) {

    }

    @Override
    public Optional<StockOption> findDerivative(String derivativeTicker) {
        return Optional.empty();
    }

    @Override
    public Stock findStock(String ticker) {
        return createStock(getOidFor(ticker), ticker);
    }

    @Override
    public Stock findStock(int oid) {
        return null;
    }

    @Override
    public Collection<Stock> getStocks() {
        Collection<Stock> result = new ArrayList<>();
        result.add(createStock(1, "NHY"));
        result.add(createStock(2, "EQNR"));
        result.add(createStock(3, "YAR"));
        return result;
    }

    private int getOidFor(String ticker) {
        switch (ticker) {
            case "NHY": return 1;
            case "EQNR": return 2;
            case "YAR": return 3;
            case "TEL": return 6;
            case "ORK": return 9;
            case "REC": return 11;
            case "PGS": return 12;
            case "STB": return 14;
            case "TGS": return 16;
            case "TOM": return 17;
            case "AKSO": return 18;
            case "DNB": return 19;
            case "DNO": return 20;
            case "GJF": return 21;
            case "SUBC": return 23;
            case "AKERBP": return 25;
            case "BLWPG": return 26;
            case "BAKKA": return 27;
            case "GOGL": return 28;
            case "NAS": return 29;
            default: return -1;
        }
    }

    private Stock createStock(int oid, String ticker) {
        StockBean s = new StockBean();
        s.setOid(oid);
        s.setTicker(ticker);
        return s;
    }

    @Override
    public Collection<StockPrice> findStockPrices(String ticker, LocalDate fromDx) {
        return null;
    }

    @Override
    public Collection<StockPrice> findStockPrices(int oid, LocalDate fromDx) {
        return null;
    }

    @Override
    public void registerOptionPurchase(StockOptionPrice purchase, int purchaseType, int volume) {

    }

    @Override
    public OptionPurchase registerOptionPurchase(int purchaseType, String opName, double price, int volume, double spotAtPurchase, double buyAtPurchase) throws FinancialException {
        return null;
    }

    @Override
    public Collection<SpotOptionPrice> findOptionPrices(int opxId) {
        return null;
    }

    @Override
    public Collection<SpotOptionPrice> findOptionPricesStockId(int stockId, LocalDate fromDate, LocalDate toDate) {
        return null;
    }

    @Override
    public Collection<SpotOptionPrice> findOptionPricesStockIds(List<Integer> stockIds, LocalDate fromDate, LocalDate toDate) {
        return null;
    }

    @Override
    public Collection<SpotOptionPrice> findOptionPricesStockTix(List<String> stockTix, LocalDate fromDate, LocalDate toDate) {
        return null;
    }

    @Override
    public Collection<OptionPurchase> purchasesWithSalesAll(int purchaseType, int status, StockOption.OptionType ot) {
        return null;
    }

    @Override
    public String getTickerFor(int oid) {
        switch (oid) {
            case 1: return "NHY";
            case 2: return "EQNR";
            case 3: return "YAR";
            default: return "";
        }
    }
}
