package io.swagger.client;

import com.squareup.okhttp.OkHttpClient;
import io.swagger.client.model.*;
import io.swagger.client.api.DefaultApi;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClientPart1 {

  public static void main(String[] args) {

    // 4 arguments
    int threadGroupSize = 10;
    int numThreadGroups = 30;
    int delay = 2;
    String serverUrl = "http://35.167.5.236:8082";

    ClientPart1 clientPart1 = new ClientPart1();
    clientPart1.runLoadTest(threadGroupSize, numThreadGroups, delay, serverUrl);
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
          apiClient.setReadTimeout(30000);
          try {
            this.runApiCalls(1000, apiClient);
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
    long totalRequests = numThreadGroups * threadGroupSize * 2000; // total requests
    double throughput = (double) totalRequests / wallTime;

    System.out.println("Wall Time: " + wallTime + " seconds");
    System.out.println("Throughput: " + throughput + " requests per second");

  }

  private void runApiCalls(int numCalls, ApiClient httpClient)
      throws ApiException {
    // construct POST data
    DefaultApi apiInstance = new DefaultApi(httpClient);
    File image = new File("/Users/echo/Downloads/nmtb.png"); // File |
    AlbumsProfile profile = new AlbumsProfile(); // AlbumsProfile |
    profile.setArtist("Taylor Swift");
    profile.setTitle("1989");
    profile.setYear("2023");

    for (int i = 0; i < numCalls; i++) {
      try {
        // sends 1000 POST/GET APIs pairs
        apiInstance.newAlbum(image, profile);
        apiInstance.getAlbumByKey("f02f86c4-8b1b-44b3-8bd6-ddb52347076a");
        // print for test only
//        ImageMetaData result1 = apiInstance.newAlbum(image, profile);
//        System.out.println(result1);
//        AlbumInfo result2 = apiInstance.getAlbumByKey("6904e33b-03c9-4baa-a5b7-eb83ac4677d4");
//        System.out.println(result2);
      } catch (ApiException e) {
        e.printStackTrace();
        for (int retryCount = 0; retryCount < 5; retryCount++) {
          try {
            TimeUnit.SECONDS.sleep(1);
            apiInstance.newAlbum(image, profile);
            apiInstance.getAlbumByKey("f02f86c4-8b1b-44b3-8bd6-ddb52347076a");
            break;
          } catch (ApiException | InterruptedException e1) {
            e1.printStackTrace();
          }
        }
      }
    }
  }
}