package buyhub.prom.ua.buyhubworkbot.services;

import buyhub.prom.ua.buyhubworkbot.configs.BotConfig;
import buyhub.prom.ua.buyhubworkbot.models.Product;
import buyhub.prom.ua.buyhubworkbot.repositories.ProductRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final ProductRepository productRepository;
    private final BotConfig config;

    public TelegramBot(BotConfig config, ProductRepository productRepository) {
        this.config = config;
        this.productRepository = productRepository;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Начальное приветствие"));
        listOfCommands.add(new BotCommand("/tags", "Привести теги к нужному формату"));
        listOfCommands.add(new BotCommand("/getproductinfo", "Получить теги и категории товара"));
        listOfCommands.add(new BotCommand("/createproductinfo", "Добавить информацию о товаре"));
        listOfCommands.add(new BotCommand("/help", "Помощь по командам"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (message.startsWith("/start") && message.length() == 6)
                startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
            else if (message.startsWith("/tags "))
                tagsCommandReceived(chatId, update.getMessage().getText());
            else if (message.startsWith("/tags"))
                sendMessage(chatId, "Введите теги");
            else if (message.startsWith("/help") && message.length() == 5)
                helpCommandReceived(chatId);
            else if (message.startsWith("/getproductinfo "))
                getProductInfo(chatId, update.getMessage().getText());
            else if (message.startsWith("/getproductinfo"))
                sendMessage(chatId, "Введите название товара");
            else if (message.startsWith("/createproductinfo "))
                createProductInfo(chatId, update.getMessage().getText());
            else if (message.startsWith("/createproductinfo"))
                sendMessage(chatId, "Введите информацию о товаре");
            else
                sendMessage(chatId, "На данный момент такой команды не существует.");
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "Привет, " + name + ". BuyHubWorkBot - это телеграмм бот магазина BuyHub, созданный для упрощения работы его персонала.\n" +
                "На данный момент в боте есть функция для упрощения копирования тегов, но в будущем будет добавлена база данных с тегами и категориями.\n" +
                "С жалобами и предложениями к - https://t.me/laymonttt";
        sendMessage(chatId, answer);
    }

    private void tagsCommandReceived(long chatId, String text) {
        String tags = Arrays.toString(text.substring(6).replace('×', ',').split(","));
        sendMessage(chatId, tags.substring(1, tags.length() - 1));
    }

    private void helpCommandReceived(long chatId) {
        sendMessage(chatId, "Список доступных команд: \n" +
                "\n" +
                "/tags [список тегов] - на вход команде дается скопированный список тегов из карточки на проме (манипуляций с тегами не должно проводиться никаких)," +
                " а на выходе получаем преобразованный к нужному виду список тегов который можно сразу же вставлять в карточку.\n" +
                "\n" +
                "!НЕДОСТУПНО! /getproductinfo [название] - поиск тегов и категории для карточки по названию. Пример: /getproductinfo обогреватель\n" +
                "\n" +
                "!НЕДОСТУПНО! /createproductinfo [название] [категории] [теги (в преобразованном виде)]");
    }

    private void createProductInfo(long chatId, String text) {
        String[] productInfo = text.substring(19).split(", ");
        String productName = productInfo[0];
        List<String> categories = new ArrayList<>();
        for (int i = 1; i < 5; i++)
            categories.add(productInfo[i]);
        String categoriesString = categories.toString();

        List<String> tags = new ArrayList<>();
        for (int i = 5; i < productInfo.length; i++)
            tags.add(productInfo[i]);
        String tagsString = tags.toString();

        if (productRepository.findByName(productInfo[0]).isPresent()) {
            sendMessage(chatId, "Такой товар уже есть");
            return;
        }

        Product product = new Product();
        product.setName(productName);
        product.setCategories(categoriesString.substring(1, categoriesString.length() - 1));
        product.setTags(tagsString.substring(1, tagsString.length() - 1));

        productRepository.save(product);
        sendMessage(chatId, "Информация о товаре добавлена в базу данных");
    }

    private void getProductInfo(long chatId, String text) {
        String productName = text.substring(16);
        if (productRepository.findByName(productName).isEmpty()) {
            sendMessage(chatId, "Информации об этом товаре нет");
            return;
        }
        Product product = productRepository.findByName(productName).orElseThrow();
        sendMessage(chatId, "Название товара: " + product.getName());
        sendMessage(chatId, "Категории товара: " + product.getCategories());
        sendMessage(chatId, "Теги товара: " + product.getTags());
    }

    private void sendMessage(long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(text);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }
}
