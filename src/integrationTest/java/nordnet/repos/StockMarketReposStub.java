package nordnet.repos;

import oahu.exceptions.FinancialException;
import oahu.financial.*;
import oahu.financial.repository.StockMarketRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class StockMarketReposStub implements StockMarketRepository {
    @Override
    public void insertDerivative(Derivative derivative, Consumer<Exception> errorHandler) {

    }

    @Override
    public Optional<Derivative> findDerivative(String derivativeTicker) {
        return Optional.empty();
    }

    @Override
    public Stock findStock(String ticker) {
        return null;
    }

    @Override
    public Collection<Stock> getStocks() {
        return null;
    }

    @Override
    public Collection<StockPrice> findStockPrices(String ticker, LocalDate fromDx) {
        return null;
    }

    @Override
    public void registerOptionPurchase(DerivativePrice purchase, int purchaseType, int volume) {

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
    public Collection<OptionPurchase> purchasesWithSalesAll(int purchaseType, int status, Derivative.OptionType ot) {
        return null;
    }

    @Override
    public String getTickerFor(int oid) {
        return null;
    }
}
