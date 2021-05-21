package nordnet.financial;

import java.time.LocalDate;
import java.util.Map;

public interface OpeningPrices {
    //void savePrices(LocalDate curDate, Map<String,String> prices);
    double fetchPrice(String ticker);
}
