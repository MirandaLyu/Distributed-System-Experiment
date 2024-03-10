package org.example;


import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;
import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@WebServlet("/albums/*")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 1024 * 1024 * 10, // 10 MB
    maxRequestSize = 1024 * 1024 * 50 // 50 MB
)
public class AlbumServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final Gson gson = new Gson();
  private JedisPool jedisPool;

  // Use a ConcurrentHashMap for thread safety
//  private final ConcurrentHashMap<String, Album> albumsMap = new ConcurrentHashMap<>();
  @Override
  public void init() throws ServletException {
    super.init();
    // create a jedis connection when servlet starts
    // JedisPool is thread-safe
    jedisPool = new JedisPool("localhost", 6379);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // Read the JSON profile data using getPart
    Part profilePart = request.getPart("profile");
    String profileJson = new String(profilePart.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    Profile profile = gson.fromJson(profileJson, Profile.class);

    // handle error
    if (profile.getArtist() == null || profile.getTitle() == null || profile.getYear() == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid request");
      return;
    }

    // Get the image part from the request
    Part imagePart = request.getPart("image");
    // Read the image data
    byte[] imageBytes = imagePart.getInputStream().readAllBytes();
    // handle error
    if (imageBytes == null || imageBytes.length == 0) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid request");
      return;
    }
    // Process the image data
    int imageSizeInBytes = imageBytes.length;
    String imageSizeString = imageSizeInBytes + " bytes";

    // Generate a unique albumID using UUID
    String albumID = UUID.randomUUID().toString();
    // Store the album into DB
    try (Jedis redis = jedisPool.getResource()) {
      redis.hset(albumID, "artist", profile.getArtist());
      redis.hset(albumID, "title", profile.getTitle());
      redis.hset(albumID, "year", profile.getYear());
    }

//    albumsMap.put(albumID, new Album(profile.getArtist(), profile.getTitle(), profile.getYear()));

    // send response
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("application/json");
    response.getWriter().write("{\"albumID\": \"" + albumID + "\", \"imageSize\": \"" + imageSizeString + "\"}");
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String pathInfo = request.getPathInfo();
    // handle error
    if (pathInfo == null || pathInfo.length() <= 1) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid request");
      return;
    }

    // Extract albumID from the path
    String albumID = pathInfo.substring(1); // pathInfo begins with "/"

    // retrieve info from DB
    String artist;
    String title;
    String year;
    try (Jedis redis = jedisPool.getResource()) {
      if (redis.exists(albumID)) {
        artist = redis.hget(albumID, "artist");
        title = redis.hget(albumID, "title");
        year = redis.hget(albumID, "year");
      } else {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write("Key not found");
        return;
      }
    }

    //    Album album = albumsMap.get(albumID);

    // send response
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("application/json");
    response.getWriter().write("{\"artist\": \"" + artist +
        "\", \"title\": \"" + title +
        "\", \"year\": \"" + year + "\"}");
  }

  @Override
  public void destroy() {
    super.destroy();
    if (jedisPool != null) {
      jedisPool.close(); // Close the pool when the servlet is destroyed
    }
  }
}
