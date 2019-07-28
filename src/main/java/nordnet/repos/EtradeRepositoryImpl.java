package nordnet.repos;

import oahu.financial.Derivative;
import oahu.financial.DerivativePrice;
import oahu.financial.StockPrice;
import oahu.financial.repository.EtradeRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

public class EtradeRepositoryImpl implements EtradeRepository {
    @Override
    public Optional<DerivativePrice> findDerivativePrice(Object optionInfo) {
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
}
