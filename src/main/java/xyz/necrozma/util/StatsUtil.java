package xyz.necrozma.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.necrozma.Client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

class StatsJson {
    int users;
}

public class StatsUtil {

    private ObjectMapper mapper = new ObjectMapper();

    private Map<String, String> parameters = new HashMap<>();

    private final Logger logger = LogManager.getLogger();

    public StatsUtil() {
        parameters.put("Content-Type", "application/json; charset=UTF-8");
        parameters.put("Accept", "application/json");
        //parameters.put("X-Master-Key", "$2b$10$kI0FvME2p.QZSZ5UWyIr4edTGfSY/bJpfFHujB3mbAyPz/YhBcp/2");
        parameters.put("X-Access-Key", "$2a$10$l72y3VUCy/nmUZJd4fF1t.mLJBwd02iXzlhTg01U4UF.hKfxWm1Ue");
    }

    public StatsJson getStats() throws IOException {
        StatsJson stats = new StatsJson();

        URL url = new URL("https://api.jsonbin.io/v3/b/65dfd5d1266cfc3fde90d2bc");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestProperty("X-Access-Key", "$2a$10$l72y3VUCy/nmUZJd4fF1t.mLJBwd02iXzlhTg01U4UF.hKfxWm1Ue");

        InputStream responseStream = connection.getInputStream();

        stats = mapper.readValue(responseStream, StatsJson.class);

        logger.info(stats.users);


        return stats;
    }

    public void setStats(StatsJson stats) throws IOException {
        URL url = new URL("https://api.jsonbin.io/v3/b/65dfd5d1266cfc3fde90d2bc");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setRequestProperty("Accept", "application/json");

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }

        con.setDoOutput(true);
        con.getOutputStream().write(mapper.writeValueAsBytes(stats));
    }

}
