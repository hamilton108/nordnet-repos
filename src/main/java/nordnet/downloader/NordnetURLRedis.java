package nordnet.downloader;

import org.springframework.cglib.core.Local;
import redis.clients.jedis.Jedis;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

public class NordnetURLRedis implements NordnetURL {

    private final String host;
    private final int port;
    private final int db;
    private URL baseURL;

    public NordnetURLRedis(String host, int port, int db) {
        this.host = host;
        this.port = port;
        this.db = db;
        try {
            this.baseURL =
                    new URL("https://www.nordnet.no/market/options/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public NordnetURLRedis(String host) {
        this(host,6379,0);
    }
    public NordnetURLRedis(String host, int db) {
        this(host,6379,db);
    }

    private String urlFileFor(String ticker, String nordnetUnixTime) {
        return String.format("/market/options?currency=NOK&underlyingSymbol=%s&expireDate=%s", ticker,  nordnetUnixTime);
    }

    public URL urlFor(String ticker, String currentDate, Jedis jedis) {
        try {
            if (jedis == null) {
                jedis = getJedis();
            }
            var nordnetUnixTime = jedis.get(currentDate);
            if (nordnetUnixTime == null) {
                throw new RuntimeException(String.format("No such Redis element: %s", currentDate));
            }
            return new URL("https", "www.nordnet.no",urlFileFor(ticker,nordnetUnixTime));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> url(String ticker) {
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

    private List<String> tickerCategory3() {
        return new ArrayList<>();
    }
    private List<String> tickerCategory2() {
        return new ArrayList<>();
    }
    private List<String> tickerCategory1(String ticker, LocalDate currentDate, Jedis jedis) {
        List<String> result = new ArrayList<>();
        Set<String> expiry_1 = jedis.smembers("expiry-1");
        for (var e : expiry_1) {
            var ed = LocalDate.parse(e);
            if (ed.isAfter(currentDate)) {
                var edUrl = urlFor(ticker, e, jedis);
                result.add(edUrl.toString());
            }
        }
        return result;
    }
    @Override
    public List<String> url(String ticker, LocalDate currentDate) {
        List<String> result = new ArrayList<>();

        var jedis = getJedis();
        /*
        var jedis = new Jedis(host,port);
        if (db != 0) {
            jedis.select(db);
        }
        //jedis.hset("b","1","one");
        Set<String> expiry_1 = jedis.smembers("expiry-1");
        Set<String> expiry_2 = jedis.smembers("expiry-2");
        Set<String> expiry_3 = jedis.smembers("expiry-3");
         */

        Map<String,String> expiry = jedis.hgetAll("expiry");

        String tickerClass = expiry.get(ticker);

        switch (tickerClass) {
            case "1":
                return tickerCategory1(ticker,currentDate,jedis);
            case "2":
                return tickerCategory2();
            case "3":
                return tickerCategory3();
        }

        System.out.println(expiry);
        return result;

        /*
        return withConnection(
                conn -> {
                   return result;
                });

         */
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
