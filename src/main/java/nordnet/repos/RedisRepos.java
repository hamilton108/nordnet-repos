package nordnet.repos;

import nordnet.downloader.URLInfo;
import redis.clients.jedis.Jedis;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RedisRepos {
    /*
    private final String host;
    private final int port;
    private final int db;
    private Jedis jedis;

    public RedisRepos(String host, int port, int db) {
        this.host = host;
        this.port = port;
        this.db = db;
    }
    
    private Jedis getJedis() {
        if (jedis != null) {
            jedis = new Jedis(host,port);
            if (db != 0) {
                jedis.select(db);
            }
        }
        return jedis;
    }
    
    private List<URLInfo> tickerCategoryX(String ticker, LocalDate currentDate, String redisKey) {
        List<URLInfo> result = new ArrayList<>();
        
        Map<String,String> exipiry = getJedis().hgetAll(redisKey);

        for (var entry : exipiry.entrySet()) {
            var ed = LocalDate.parse(entry.getKey());
            if (ed.isAfter(currentDate)) {
                var edUrl = urlFor(ticker, entry.getValue());
                result.add(new URLInfo(edUrl.toString(),entry.getValue()));
            }
        }
        return result;
    }
    
     */

}
