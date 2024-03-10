package io.swagger.client;

import io.swagger.client.api.DefaultApi;
import io.swagger.client.api.LikeApi;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClientPart2 {

  private ConcurrentLinkedQueue<RequestRecord> requestQueue = new ConcurrentLinkedQueue<>();
  private Integer numOfFailedRequests = 0;

  public static void main(String[] args) {

    // 4 arguments
    int threadGroupSize = 10;
    int numThreadGroups = 30;
    int delay = 2;
    String serverUrl = "http://52.37.212.59:8080/javaServer_war";

    ClientPart2 clientPart2 = new ClientPart2();
    clientPart2.runLoadTest(threadGroupSize, numThreadGroups, delay, serverUrl);
  }

  public void runLoadTest(int threadGroupSize, int numThreadGroups, int delay, String serverUrl) {
    ExecutorService executorService = Executors.newFixedThreadPool(
        threadGroupSize * numThreadGroups);

    // start recording time
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < numThreadGroups; i++) {
      System.out.println("Starting Thread Group: " + (i + 1));
      for (int j = 0; j < threadGroupSize; j++) {
        executorService.submit(() -> {
          ApiClient apiClient = new ApiClient(); // create new httpclient for each thread
          apiClient.setBasePath(serverUrl);
//          apiClient.setReadTimeout(30000);
          try {
            this.runApiCalls(100, apiClient);
          } catch (ApiException e) {
            throw new RuntimeException(e);
          }
        });
      }
      // delay 2 seconds, then start another group
      try {
        Thread.sleep(delay * 1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    // Shutdown the executor service when done
    executorService.shutdown();

    try {
      // Wait for all threads to finish or a timeout to occur
      if (!executorService.awaitTermination(9, TimeUnit.MINUTES)) {
        // Handle the case where not all threads finished within the timeout
        System.out.println("Some threads did not finish within the timeout.");
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    long endTime = System.currentTimeMillis();
    long wallTime = (endTime - startTime) / 1000; // in seconds
    long totalRequests = numThreadGroups * threadGroupSize * 400; // total requests
    double throughput = (double) totalRequests / wallTime;
    System.out.println("Wall Time: " + wallTime + " seconds");
    System.out.println("Throughput: " + throughput + " requests per second");
//    System.out.println("Number of successful requests: " + (totalRequests - numOfFailedRequests));
//    System.out.println("Number of failed requests: " + numOfFailedRequests + "\n");

    // Generate performance statistics
//    generatePerformanceStatistics();
  }

  private void runApiCalls(int numCalls, ApiClient httpClient)
      throws ApiException {
    // construct POST data
    DefaultApi apiInstance = new DefaultApi(httpClient);
    LikeApi likeApiInstance = new LikeApi(httpClient);
    File image = new File("/Users/echo/Downloads/nmtb.png"); // File |
    AlbumsProfile profile = new AlbumsProfile(); // AlbumsProfile |
    profile.setArtist("Taylor Swift");
    profile.setTitle("1989");
    profile.setYear("2023");

    for (int i = 0; i < numCalls; i++) {
      try {
        // sends 1000 POST/GET APIs pairs
//        Instant startTime = Instant.now();
//        int sCode = apiInstance.newAlbum(image, profile);
//        Instant endTime = Instant.now();
//        long latency = Duration.between(startTime, endTime).toMillis();
//        requestQueue.add(new RequestRecord(startTime, "POST", latency, sCode));
//        if (sCode != 200) {
//          numOfFailedRequests++;
//        }
//
//        startTime = Instant.now();
//        sCode = apiInstance.getAlbumByKey("bfb071f9-6c5e-4905-8db9-a5d2968c5e60");
//        endTime = Instant.now();
//        latency = Duration.between(startTime, endTime).toMillis();
//        requestQueue.add(new RequestRecord(startTime, "GET", latency, sCode));
//        if (sCode != 200) {
//          numOfFailedRequests++;
//        }
        // print for test only
        ImageMetaData result1 = apiInstance.newAlbum(image, profile);
        likeApiInstance.review("like", result1.getAlbumID());
        likeApiInstance.review("like", result1.getAlbumID());
        likeApiInstance.review("dislike", result1.getAlbumID());
//        System.out.println(result1);
//        AlbumInfo result2 = apiInstance.getAlbumByKey("6904e33b-03c9-4baa-a5b7-eb83ac4677d4");
//        System.out.println(result2);
      } catch (ApiException e) {
        e.printStackTrace();
        for (int retryCount = 0; retryCount < 5; retryCount++) {
          try {
            TimeUnit.SECONDS.sleep(1);

//            Instant startTime = Instant.now();
//            int sCode = apiInstance.newAlbum(image, profile);
//            Instant endTime = Instant.now();
//            long latency = Duration.between(startTime, endTime).toMillis();
//            requestQueue.add(new RequestRecord(startTime, "POST", latency, sCode));
//            if (sCode != 200) {
//              numOfFailedRequests++;
//            }
//
//            startTime = Instant.now();
//            sCode = apiInstance.getAlbumByKey("bfb071f9-6c5e-4905-8db9-a5d2968c5e60");
//            endTime = Instant.now();
//            latency = Duration.between(startTime, endTime).toMillis();
//            requestQueue.add(new RequestRecord(startTime, "GET", latency, sCode));
//            if (sCode != 200) {
//              numOfFailedRequests++;
//            }
            ImageMetaData result1 = apiInstance.newAlbum(image, profile);
            likeApiInstance.review("like", result1.getAlbumID());
            likeApiInstance.review("like", result1.getAlbumID());
            likeApiInstance.review("dislike", result1.getAlbumID());

            break;
          } catch (ApiException | InterruptedException e1) {
            e1.printStackTrace();
          }
        }
      }
    }
  }

  private void generatePerformanceStatistics() {
    // Calculate and display statistics
    // Mean, median, p99, min, max response time for POST
    calculateAndDisplayStatistics("POST");
    // Mean, median, p99, min, max response time for GET
    calculateAndDisplayStatistics("GET");

    // Write performance data to CSV file
//    writePerformanceDataToCSV();
  }

  private void calculateAndDisplayStatistics(String requestType) {
    List<Long> latencies = new ArrayList<>();
    for (RequestRecord record : requestQueue) {
      if (record.getRequestType().equals(requestType)) {
        latencies.add(record.getLatency());
      }
    }

    // Calculate statistics
    double meanResponseTime = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
    Collections.sort(latencies);
    double medianResponseTime = latencies.get(latencies.size() / 2);
    double p99ResponseTime = latencies.get((int) (latencies.size() * 0.99));
    long minResponseTime = latencies.stream().mapToLong(Long::longValue).min().orElse(0);
    long maxResponseTime = latencies.stream().mapToLong(Long::longValue).max().orElse(0);

    // Display statistics
    System.out.println("Statistics for " + requestType + " requests:");
    System.out.println("Mean Response Time: " + meanResponseTime + " milliseconds");
    System.out.println("Median Response Time: " + medianResponseTime + " milliseconds");
    System.out.println("P99 Response Time: " + p99ResponseTime + " milliseconds");
    System.out.println("Min Response Time: " + minResponseTime + " milliseconds");
    System.out.println("Max Response Time: " + maxResponseTime + " milliseconds\n");
  }

  //  private void writePerformanceDataToCSV() {
//    try (FileWriter writer = new FileWriter("performance_data_Go30.csv")) {
//      writer.write("Start Time,Request Type,Latency,Response Code\n");
//      for (RequestRecord record : requestQueue) {
//        writer.write(record.getStartTime() + "," + record.getRequestType() + "," + record.getLatency() + "," + record.getResponseCode() + "\n");
//      }
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }
  private static class RequestRecord {
    private Instant startTime;
    private String requestType;
    private long latency;
    private int responseCode;

    public RequestRecord(Instant startTime, String requestType, long latency, int responseCode) {
      this.startTime = startTime;
      this.requestType = requestType;
      this.latency = latency;
      this.responseCode = responseCode;
    }

    public Instant getStartTime() {
      return startTime;
    }

    public String getRequestType() {
      return requestType;
    }

    public long getLatency() {
      return latency;
    }

    public int getResponseCode() {
      return responseCode;
    }
  }
}