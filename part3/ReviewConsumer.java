package org.example;

import com.rabbitmq.client.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class ReviewConsumer {

  private static final String QUEUE_NAME = "reviewQueue";

  public static void main(String[] argv) throws Exception {
    // establish connections and processing logic
    try (Jedis redis = new Jedis("localhost", 6379)) {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost("localhost");
      try (Connection connection = factory.newConnection();
          Channel channel = connection.createChannel()) {

        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");  //delivery contains the message received from RabbitMQ
          System.out.println(" [x] Received '" + message + "'");
          try {
            // validate message before processing
            String[] parts = message.split(":");
            if (parts.length != 2) {
              // Invalid message format, reject it
              throw new IllegalArgumentException("Invalid message format");
            }

            String likeOrNot = parts[0];
            String albumID = parts[1];

            // Check if likeOrNot is either "like" or "dislike"
            if (!likeOrNot.equals("like") && !likeOrNot.equals("dislike")) {
              // Invalid likeOrNot value, reject the message
              throw new IllegalArgumentException("Invalid likeOrNot value");
            }
            // Check if albumID exists in Redis (you may need to implement this check)
            if (!isValidAlbumID(redis, albumID)) {
              // Invalid albumID, reject the message
              throw new IllegalArgumentException("Invalid albumID");
            }

            // Process the message only if it passes all checks
            processMessage(likeOrNot, albumID, redis);

            // Manually acknowledge the message after successful processing in Redis
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          } catch (IllegalArgumentException e) {
            // Handle invalid messages by rejecting them
            e.printStackTrace();
            channel.basicReject(delivery.getEnvelope().getDeliveryTag(), false);
          } catch (Exception e) {
            // Handle exceptions here, optionally reject the message
            e.printStackTrace();
            channel.basicReject(delivery.getEnvelope().getDeliveryTag(), false);
          }
        };

        // initiates the process of receiving and processing messages
        // Set autoAck to false, so messages are not automatically acknowledged
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
        });

        // Keep the application running in a coarse way
        while (true) {
          Thread.sleep(1000); // Sleep to avoid busy-waiting
        }
      }
    }
  }

  private static boolean isValidAlbumID(Jedis redis, String albumID) {
    return redis.exists(albumID);
  }

  private static void processMessage(String likeOrNot, String albumID, Jedis redis) {
    // update database
    String field = likeOrNot.equals("like") ? "likes" : "dislikes";
    redis.hincrBy(albumID, field, 1);
  }
}
