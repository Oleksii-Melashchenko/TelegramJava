package TgBot.my;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.*;

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
            search.setMaxResults(1L);
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

    public void downloadAndSendMp3(long chatId, String videoUrl, String userQuery, MusicSearchBot bot) {
        File tempMp3File = null;
        try {
            String sanitizedQuery = userQuery.replaceAll("[^a-zA-Z0-9\\s]", "").trim();
            String fileName = sanitizedQuery.isEmpty() ? "audio" : sanitizedQuery.replaceAll("\\s+", "_");
            tempMp3File = new File("C:\\Path\\To\\Your\\Project\\" + fileName + ".mp3");
            String searchQuery = "ytsearch:" + videoUrl;
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "yt-dlp", "-x", "--audio-format", "mp3", "-o", tempMp3File.getAbsolutePath(), searchQuery
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("yt-dlp завершился с ошибкой. Код: " + exitCode);
            }
            if (tempMp3File.length() > 0) {
                SendAudio sendAudio = new SendAudio();
                sendAudio.setChatId(String.valueOf(chatId));
                sendAudio.setAudio(new InputFile(tempMp3File));
                bot.execute(sendAudio);
            } else {
                SendMessage errorMessage = new SendMessage();
                errorMessage.setChatId(String.valueOf(chatId));
                errorMessage.setText("Ошибка при загрузке MP3, файл пустой.");
                bot.execute(errorMessage);
            }
        } catch (IOException | InterruptedException | TelegramApiException e) {
            e.printStackTrace();
            try {
                SendMessage errorMessage = new SendMessage();
                errorMessage.setChatId(String.valueOf(chatId));
                errorMessage.setText("Ошибка при загрузке MP3.");
                bot.execute(errorMessage);
            } catch (TelegramApiException ex) {
                ex.printStackTrace();
            }
        } finally {
            if (tempMp3File != null && tempMp3File.exists()) {
                tempMp3File.delete();
            }
        }
    }
}





