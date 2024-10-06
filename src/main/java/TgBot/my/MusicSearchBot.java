package TgBot.my;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import io.github.cdimascio.dotenv.Dotenv;

public class MusicSearchBot extends TelegramLongPollingBot {
    private static final Dotenv dotenv = Dotenv.load();
    private static final YouTubeSearchService youtubeSearchService = new YouTubeSearchService();

    @Override
    public String getBotUsername() {
        return "MusicSearchBot";
    }

    @Override
    public String getBotToken() {
        return dotenv.get("BOT_TOKEN");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")) {
                sendMessage(chatId, "Hello, print name for search");
            } else {
                String videoLink = youtubeSearchService.searchVideo(messageText); // Вызов метода
                sendMessage(chatId, "Results for request: " + messageText + "\n" + videoLink);
            }
        }
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}



