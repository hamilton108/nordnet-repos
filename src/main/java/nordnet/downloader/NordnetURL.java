package nordnet.downloader;

import java.time.LocalDate;
import java.util.List;

public interface NordnetURL<T> {

    List<T> url(String ticker);
    List<T> url(String ticker, LocalDate currentDate);
}
