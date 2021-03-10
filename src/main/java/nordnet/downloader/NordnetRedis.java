package nordnet.downloader;

import redis.clients.jedis.Jedis;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

public class NordnetRedis implements NordnetURL<URLInfo>, OpeningPrices {

    private final ZoneId EUROPE_OSLO = ZoneId.of("Europe/Oslo");
    private final String host;
    private final int port;
    private final int db;
    //private final int
    //private final URL baseURL;

    public NordnetRedis(String host, int port, int db) {
        this.host = host;
        this.port = port;
        this.db = db;
        /*
        try {
            this.baseURL =
                    new URL("https://www.nordnet.no/market/options/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
         */
    }
    public NordnetRedis(String host) {
        this(host,6379,0);
    }
    public NordnetRedis(String host, int db) {
        this(host,6379,db);
    }

    private String urlFileFor(String ticker, String nordnetUnixTime) {
        return String.format("/market/options?currency=NOK&underlyingSymbol=%s&expireDate=%s", ticker,  nordnetUnixTime);
    }

    public URL urlFor(String ticker, String nordnetUnixTime) {
        try {
            return new URL("https", "www.nordnet.no",urlFileFor(ticker,nordnetUnixTime));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<URLInfo> url(String ticker) {
        List<String> result = new ArrayList<>();

        return url(ticker, LocalDate.now());
    }

    private Jedis getJedis() {
        var jedis = new Jedis(host,port);
        if (db != 0) {
            jedis.select(db);
        }
        return jedis;
    }

    /*
    private List<URLInfo> tickerCategoryX(String ticker, LocalDate currentDate, String redisKey, Jedis jedis) {
        List<URLInfo> result = new ArrayList<>();
        Map<String,String>  exipiry = jedis.hgetAll(redisKey);

        for (var entry : exipiry.entrySet()) {
            var ed = LocalDate.parse(entry.getKey());
            if (ed.isAfter(currentDate)) {
                var edUrl = urlFor(ticker, entry.getValue());
                result.add(new URLInfo(edUrl.toString(),entry.getValue()));
            }
        }
        return result;
    }
    private List<URLInfo> tickerCategory1(String ticker, LocalDate currentDate, Jedis jedis) {
        return tickerCategoryX(ticker,currentDate,"expiry-1", jedis);
    }
    private List<URLInfo> tickerCategory2(String ticker, LocalDate currentDate, Jedis jedis) {
        var expiry_1 = tickerCategoryX(ticker,currentDate,"expiry-1", jedis);
        var expiry_2 = tickerCategoryX(ticker,currentDate,"expiry-2", jedis);
        expiry_1.addAll(expiry_2);
        return  expiry_1;
    }
     */

    @Override
    public List<URLInfo> url(String ticker, LocalDate currentDate) {

        LocalDateTime tm = currentDate.atStartOfDay();

        var utc =
                tm.atZone(EUROPE_OSLO).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime().toInstant(ZoneOffset.UTC);

        var nordnetMillis = utc.getEpochSecond() * 1000;

        List<URLInfo> result = new ArrayList<>();

        var jedis = getJedis();

        Set<String> expiry = jedis.smembers("expiry");

        for (var e : expiry) {
            var eI = Long.parseLong(e);
            if (eI < nordnetMillis) {
                continue;
            }
            var edUrl = urlFor(ticker, e);
            result.add(new URLInfo(edUrl.toString(),e));
        }

        return result;
        /*
        String tickerClass = expiry.get(ticker);

        switch (tickerClass) {
            case "1":
                return tickerCategory1(ticker,currentDate,jedis);
            case "2":
                return tickerCategory2(ticker,currentDate,jedis);
            case "3":
                return tickerCategory3();
        }

         */

    }

    /******************************************************************
    ********************* Interface OpeningPrices *********************
    ******************************************************************/
    @Override
    public void savePrices(LocalDate curDate, Map<String, String> prices) {
        var jedis = getJedis();
        jedis.hset(redisKey(curDate), prices);
    }

    @Override
    public Map<String, String> fetchPrices(LocalDate curDate) {
        var jedis = getJedis();
        return jedis.hgetAll(redisKey(curDate));

        /*
        var jedisResult = jedis.hgetAll(key);
        for (Map.Entry<String,String> items = jedisResult.entrySet()) {

        }
         */
    }

    @Override
    public double fetchPrice(LocalDate curDate, String ticker) {
        var jedis = getJedis();
        var hashKey= redisKey(curDate);
        String priceS = jedis.hget(hashKey, ticker);
        return Double.parseDouble(priceS);
    }

    @Override
    public double fetchPrice(String ticker) {
        var jedis = getJedis();
        String priceS = jedis.hget("openingprices", ticker);
        return Double.parseDouble(priceS);
    }

    private String redisKey(LocalDate curDate) {
        return String.format("%d-%d-%d",
                curDate.getYear(),
                curDate.getMonth().getValue(),
                curDate.getDayOfMonth());
    }
    /*
    private List<String> withConnection(Function<StatefulRedisConnection<String,String>,List<String>> fn) {
        var uri = RedisURI.Builder.redis(host, port).withDatabase(db).build();
        var client = RedisClient.create(uri);
        try (var conn = client.connect()) {
            var result = fn.apply(conn);
            client.shutdown();
            return result;
        }
    }
     */
}
