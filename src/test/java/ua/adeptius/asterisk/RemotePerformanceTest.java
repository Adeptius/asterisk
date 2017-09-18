package ua.adeptius.asterisk;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RemotePerformanceTest {

    private static Logger LOGGER = LoggerFactory.getLogger("-=TESTING=-");
    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(
            20, 20, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(30));


    @BeforeClass
    public static void preparingDb() throws Exception {

    }


    @Before
    public void prepareUser() throws Exception {


    }


    private static int activeThreads = 0;
    private static Random random = new Random();

    @Test
    public void test() throws Exception {
        for (int j = 0; j < 3; j++) {


            EXECUTOR.submit(() -> {
                activeThreads++;
                System.out.println("started script/e404/rutracker");
                try {
                    for (int i = 0; i < 10000; i++) {
                        HttpResponse<String> response = Unirest
                                .get("https://cstat.nextel.com.ua:8443/tracking/script/e404/rutracker")
                                .asString();
                    }
                } catch (UnirestException e) {
                    e.printStackTrace();
                }
                activeThreads--;
            });
            EXECUTOR.submit(() -> {
                activeThreads++;
                System.out.println("started phones/getAllPhones");

                try {
                    for (int i = 0; i < 10000; i++) {
                        HttpResponse<String> response = Unirest
                                .post("https://cstat.nextel.com.ua:8443/tracking/phones/getAllPhones")
                                .header("Authorization", "0c16b6b83fe6659be9aade5ef9788f00")
                                .asString();
//                    System.out.println(response.getBody());
                    }
                } catch (UnirestException e) {
                    e.printStackTrace();
                }
                activeThreads--;
            });
            EXECUTOR.submit(() -> {
                activeThreads++;
                System.out.println("started scenario/getAll");

                try {
                    for (int i = 0; i < 10000; i++) {
                        HttpResponse<String> response = Unirest
                                .post("https://cstat.nextel.com.ua:8443/tracking/scenario/getAll")
                                .header("Authorization", "0c16b6b83fe6659be9aade5ef9788f00")
                                .asString();
//                    System.out.println(response.getBody());
                    }
                } catch (UnirestException e) {
                    e.printStackTrace();
                }
                activeThreads--;
            });
            EXECUTOR.submit(() -> {
                activeThreads++;
                System.out.println("started sites/get");

                try {
                    for (int i = 0; i < 10000; i++) {
                        HttpResponse<String> response = Unirest
                                .post("https://cstat.nextel.com.ua:8443/tracking/sites/get")
                                .header("Authorization", "0c16b6b83fe6659be9aade5ef9788f00")
                                .asString();
//                    System.out.println(response.getBody());
                    }
                } catch (UnirestException e) {
                    e.printStackTrace();
                }
                activeThreads--;
            });

            EXECUTOR.submit(() -> {
                activeThreads++;
                System.out.println("started blacklist/get");
                try {
                    for (int i = 0; i < 10000; i++) {
                        HttpResponse<String> response = Unirest
                                .post("https://cstat.nextel.com.ua:8443/tracking/blacklist/get")
                                .header("Authorization", "0c16b6b83fe6659be9aade5ef9788f00")
                                .field("siteName", "rutracker")
                                .asString();
//                    System.out.println(response.getBody());
                    }
                } catch (UnirestException e) {
                    e.printStackTrace();
                }
                activeThreads--;
            });
            EXECUTOR.submit(() -> {
                activeThreads++;
                System.out.println("started blacklist/add");

                try {
                    for (int i = 0; i < 10000; i++) {
                        HttpResponse<String> response = Unirest
                                .post("https://cstat.nextel.com.ua:8443/tracking/blacklist/add")
                                .header("Authorization", "0c16b6b83fe6659be9aade5ef9788f00")
                                .field("siteName", "rutracker")
                                .field("ip", "78.159.55." + ((int) ((Math.random() * 250) + 1)))
                                .asString();
                    }
                } catch (UnirestException e) {
                    e.printStackTrace();
                }
                activeThreads--;
            });

            EXECUTOR.submit(() -> {
                activeThreads++;
                System.out.println("started history/get");

                try {
                    for (int i = 0; i < 10000; i++) {

                        String direction = "";
                        int i1 = random.nextInt(3) + 1;
                        if (i1 == 1) {
                            direction = "IN";
                        } else if (i1 == 2) {
                            direction = "OUT";
                        } else if (i1 == 3) {
                            direction = "BOTH";
                        }

                        String queryJson = "{\"dateFrom\": \"2017-0" + (random.nextInt(9) + 1) + "-16 00:00:00\"," +
                                " \"dateTo\": \"2017-0" + (random.nextInt(9) + 1) + "-17 00:00:00\"," +
                                " \"direction\": \"" + direction + "\"," +
                                " \"limit\": " + (random.nextInt(250) + 2) + "," +
                                " \"offset\": 0" +
                                "}";

//                System.out.println(queryJson);
                        HttpResponse<String> response = Unirest
                                .post("https://cstat.nextel.com.ua:8443/tracking/history/get")
                                .header("content-type", "application/json")
                                .header("Authorization", "0c16b6b83fe6659be9aade5ef9788f00")
                                .body(queryJson)
                                .asString();
//                    System.out.println(response.getBody());
                    }
                } catch (UnirestException e) {
                    e.printStackTrace();
                }
                activeThreads--;
            });

        }
        Thread.sleep(20);

        while (activeThreads != 0) {
            Thread.sleep(3);
        }
    }
}
