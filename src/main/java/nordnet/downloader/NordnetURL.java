package nordnet.downloader;

import java.time.LocalDate;
import java.util.List;

public interface NordnetURL {

    List<String> url(String ticker);
    List<String> url(String ticker, LocalDate currentDate);
}
