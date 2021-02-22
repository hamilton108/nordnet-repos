package nordnet.downloader;

import java.time.LocalDate;
import java.util.Map;

public interface OpeningPrices {
    void savePrices(LocalDate curDate, Map<String,String> prices);
    Map<String,String> fetchPrices(LocalDate curDate);
    double fetchPrice(LocalDate curDate, String ticker);
}
