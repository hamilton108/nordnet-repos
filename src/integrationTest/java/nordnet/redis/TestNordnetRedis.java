package nordnet.redis;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.offset;

@RunWith(SpringRunner.class)
public class TestNordnetRedis {

    @Test
    public void test_lastUpdateTimeStockPrice() {
        NordnetRedis nordnetRedis = new NordnetRedis("172.20.1.2", 5);
        long actual = nordnetRedis.getLastUpdateTimeStockPrices("EQNR");
        assertThat(actual).isEqualTo(1620594773);
    }
}
