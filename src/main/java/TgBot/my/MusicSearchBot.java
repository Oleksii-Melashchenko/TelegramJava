package TgBot.my;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MusicSearchBot extends TelegramLongPollingBot {
    private static final Dotenv dotenv = Dotenv.load();
    private static final YouTubeSearchService youtubeSearchService = new YouTubeSearchService();

    @Override
    public String getBotUsername() {
        return "MusicSearchBot";
    }

    @Override
    public String getBotToken() {
        return dotenv.get("TELEGRAM_API_KEY");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String[] data = callbackData.split(":");
            String action = data[0];
            String query = data[1];
            switch (action) {
                case "search_youtube":
                    youtubeSearchService.searchOnYouTube(chatId, query, this);
                    break;
                case "download_mp3":
                    youtubeSearchService.downloadMp3(chatId, query, this);
                    break;
            }
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            String query = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            sendOptionMessage(chatId, query);
        }
    }

    public void sendOptionMessage(long chatId, String query) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton searchButton = new InlineKeyboardButton();
                searchButton.setText("Search on YouTube");
                searchButton.setCallbackData("search_youtube:" + query);
        InlineKeyboardButton downloadButton = new InlineKeyboardButton();
                downloadButton.setText("Download MP3");
                downloadButton.setCallbackData("download_mp3:" + query);
        List<InlineKeyboardButton> row = Arrays.asList(searchButton, downloadButton);
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);
        SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText("Choose an action for your query: " + query);
                message.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
