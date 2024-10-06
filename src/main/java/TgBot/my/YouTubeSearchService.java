package TgBot.my;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.List;

public class YouTubeSearchService {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("YOUTUBE_API");
    private static final long NUMBER_OF_VIDEOS_RETURNED = 1;

    public String searchVideo(String query) {
        try {
            YouTube youtubeService = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(), null)
                    .setApplicationName("youtube-search")
                    .build();

            YouTube.Search.List search = youtubeService.search().list("id,snippet");
            search.setKey(API_KEY);
            search.setQ(query);
            search.setType("video");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResults = searchResponse.getItems();

            if (!searchResults.isEmpty()) {
                SearchResult firstResult = searchResults.getFirst();
                return "https://www.youtube.com/watch?v=" + firstResult.getId().getVideoId();
            } else {
                return "No video for your request";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Error while searching video";
        }
    }
}
