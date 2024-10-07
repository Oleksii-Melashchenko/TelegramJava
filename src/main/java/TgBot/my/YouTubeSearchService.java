package TgBot.my;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.util.List;

public class YouTubeSearchService {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("YOUTUBE_API_KEY");

    private YouTube youtubeService;

    public YouTubeSearchService() {
        try {
            youtubeService = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(), null)
                    .setApplicationName("youtube-search")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void searchOnYouTube(long chatId, String query, MusicSearchBot bot) {
        try {
            YouTube.Search.List search = youtubeService.search().list("id,snippet");
            search.setKey(API_KEY);
            search.setQ(query);
            search.setType("video");
            search.setMaxResults(5L);
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResults = searchResponse.getItems();

            StringBuilder responseText = new StringBuilder("Results for query: " + query + "\n");
            if (searchResults.isEmpty()) {
                responseText.append("No video found for your query.");
            } else {
                for (SearchResult result : searchResults) {
                    String videoUrl = "https://www.youtube.com/watch?v=" + result.getId().getVideoId();
                    responseText.append(videoUrl).append("\n");
                }
            }
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(responseText.toString());

            bot.execute(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadMp3(long chatId, String videoUrl, MusicSearchBot bot) {
        try {
            String command = "youtube-dl -x --audio-format mp3 " + videoUrl;
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("MP3 файл загружен.");
            bot.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                SendMessage errorMessage = new SendMessage();
                errorMessage.setChatId(String.valueOf(chatId));
                errorMessage.setText("Ошибка при загрузке MP3.");
                bot.execute(errorMessage);
            } catch (TelegramApiException ex) {
                ex.printStackTrace();
            }
        }
    }
}

