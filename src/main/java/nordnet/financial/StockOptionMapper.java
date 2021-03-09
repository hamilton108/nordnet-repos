package nordnet.financial;

import java.time.LocalDate;

public class StockOptionMapper {

    public static LocalDate mapOptionTickerToDate(String ticker) {
        return LocalDate.now();
    }

    public static LocalDate mapUnixTimeToDate(int unixTime) {
        return LocalDate.now();
    }
}
