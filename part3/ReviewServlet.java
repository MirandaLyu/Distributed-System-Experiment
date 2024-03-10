package org.example;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;
import redis.clients.jedis.Jedis;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import redis.clients.jedis.JedisPool;

@WebServlet("/review/*")
public class ReviewServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final String QUEUE_NAME = "reviewQueue";
  private JedisPool jedisPool;
  private Connection connection;

  @Override
  public void init() throws ServletException {
    super.init();
    // create a jedis connection when servlet starts
    // JedisPool is thread-safe
    jedisPool = new JedisPool("localhost", 6379);
    // create a RabbitMQ connection as well
    try {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost("localhost");
      // Create a new connection
      connection = factory.newConnection();
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }


  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String pathInfo = request.getPathInfo(); // the pathInfo begins with a "/", such as "/like/ab87a973-ba7f-40e5-8f4f-d37dbf471025"

    // Validate pathInfo
    if (pathInfo == null || pathInfo.split("/").length != 3) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid request");
      return;
    }

    // get data
    String[] pathParts = pathInfo.split("/");
    String likeOrNot = pathParts[1];
    String albumID = pathParts[2];

    // Validate likeOrNot input
    if (!likeOrNot.equals("like") && !likeOrNot.equals("dislike")) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid like or not value");
      return;
    }

    // Validate whether the albumID exists in the DB
    try (Jedis redis = jedisPool.getResource()) {
      if (!redis.exists(albumID)) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write("Invalid album ID");
        return;
      }
    }

    // Prepare message for RabbitMQ (the message could be another format)
    String message = likeOrNot + ":" + albumID;

    // create a channel
    try (Channel channel = connection.createChannel()) {
      // Declare a queue for the channel
      channel.queueDeclare(QUEUE_NAME, true, false, false, null); // queue declarations are idempotent here
      // Publish the message to the queue
      channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
    } catch (Exception e) {
      throw new ServletException(e);
    }

    // Send response
    response.setStatus(HttpServletResponse.SC_CREATED);
    response.getWriter().write("Review request accepted");
  }

  @Override
  public void destroy() {
    super.destroy();
    if (jedisPool != null) {
      jedisPool.close(); // Close the pool when the servlet is destroyed
    }
    if (connection != null) {
      try {
        connection.close(); // Attempt to close the connection
      } catch (IOException e) {
        // Log the exception or take further action
        log("Error closing RabbitMQ connection", e);
      }
    }
  }
}
