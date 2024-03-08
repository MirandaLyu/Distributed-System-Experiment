import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;
import com.google.gson.Gson;

@WebServlet("/albums/*")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 1024 * 1024 * 10, // 10 MB
    maxRequestSize = 1024 * 1024 * 50 // 50 MB
)
public class AlbumServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final Gson gson = new Gson();

  // Use a ConcurrentHashMap for thread safety
  private final ConcurrentHashMap<String, Album> albumsMap = new ConcurrentHashMap<>();

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {


    // Now, handle other form fields
    // Read the JSON profile data using getPart
    Part profilePart = request.getPart("profile");
    String profileJson = new String(profilePart.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    Profile profile = gson.fromJson(profileJson, Profile.class);

    if (profile.getArtist() == null || profile.getTitle() == null || profile.getYear() == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid request");
      return;
    }

    // Get the image part from the request
    Part imagePart = request.getPart("image");

    // Read the image data
    InputStream imageStream = imagePart.getInputStream();
    byte[] imageBytes = imageStream.readAllBytes();

    // Process the image data
    int imageSizeInBytes = imageBytes.length;
    String imageSizeString = imageSizeInBytes + " bytes";

    // Generate a unique albumID using UUID
    String albumID = UUID.randomUUID().toString();
    albumsMap.put(albumID, new Album(profile.getArtist(), profile.getTitle(), profile.getYear()));

    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("application/json");
    response.getWriter().write("{\"albumID\": \"" + albumID + "\", \"imageSize\": \"" + imageSizeString + "\"}");
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String pathInfo = request.getPathInfo();
    if (pathInfo == null || pathInfo.length() <= 1) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid request");
      return;
    }

    // Extract albumID from the path
    String albumID = pathInfo.substring(1);

    if (albumID == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid request");
      return;
    }

    Album album = albumsMap.get(albumID);

    if (album == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("Key not found");
      return;
    }

    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("application/json");
    response.getWriter().write("{\"artist\": \"" + album.getArtist() +
        "\", \"title\": \"" + album.getTitle() +
        "\", \"year\": \"" + album.getYear() + "\"}");
  }
}
