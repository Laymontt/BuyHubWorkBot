package buyhub.prom.ua.buyhubworkbot.services;

import buyhub.prom.ua.buyhubworkbot.configs.BotConfig;
import buyhub.prom.ua.buyhubworkbot.models.Employee;
import buyhub.prom.ua.buyhubworkbot.models.Product;
import buyhub.prom.ua.buyhubworkbot.repositories.EmployeeRepository;
import buyhub.prom.ua.buyhubworkbot.repositories.ProductRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final ProductRepository productRepository;
    private final EmployeeRepository employeeRepository;
    private final BotConfig botConfig;
    private final ProductService productService;
    private final EmployeeService employeeService;

    public TelegramBot(BotConfig botConfig, ProductRepository productRepository, EmployeeRepository employeeRepository, ProductService productService, EmployeeService employeeService) {
        this.botConfig = botConfig;
        this.productRepository = productRepository;
        this.employeeRepository = employeeRepository;
        this.productService = productService;
        this.employeeService = employeeService;

        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Начальное приветствие"));
        listOfCommands.add(new BotCommand("/tags", "Привести теги к нужному формату"));
        listOfCommands.add(new BotCommand("/getproductinfo", "Получить теги и категории товара"));
        listOfCommands.add(new BotCommand("/createproductinfo", "Добавить информацию о товаре"));
        listOfCommands.add(new BotCommand("/deleteproductinfo", "Удалить информацию о товаре"));
        listOfCommands.add(new BotCommand("/editproductinfo", "Изменить информацию о товаре"));
        listOfCommands.add(new BotCommand("/getlistproducts", "Список всех товаров"));
        listOfCommands.add(new BotCommand("/addemployee", "Добавить сотрудника"));
        listOfCommands.add(new BotCommand("/removeemployee", "Уволить сотрудника"));
        listOfCommands.add(new BotCommand("/getlistemployees", "Список сотрудников"));
        listOfCommands.add(new BotCommand("/getemployeeinfo", "Получить информацию о сотруднике"));
//        listOfCommands.add(new BotCommand("/guides", "Получить список гайдов"));
        listOfCommands.add(new BotCommand("/help", "Помощь по командам"));
//        listOfCommands.add(new BotCommand("/createavatar", "Создать аватарку"));
        listOfCommands.add(new BotCommand("/sendmessagefor", "Отправить сообщение сотруднику"));
        listOfCommands.add(new BotCommand("/sendmessageall", "Отправить сообщение всем сотрудникам"));
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
            else if (message.startsWith("/getproductinfo ")) {
                if (employeeRepository.findByUsername(update.getMessage().getChat().getUserName()).isEmpty()) {
                    sendMessage(chatId, "Вы не сотрудник BuyHub.");
                    return;
                }
                getProductInfo(chatId, update.getMessage().getText());
            } else if (message.startsWith("/getproductinfo")) {
                if (employeeRepository.findByUsername(update.getMessage().getChat().getUserName()).isEmpty()) {
                    sendMessage(chatId, "Вы не сотрудник BuyHub.");
                    return;
                }
                sendMessage(chatId, "Введите название товара");
            } else if (message.startsWith("/createproductinfo ")) {
                if (employeeRepository.findByUsername(update.getMessage().getChat().getUserName()).isEmpty()) {
                    sendMessage(chatId, "Вы не сотрудник BuyHub.");
                    return;
                }
                createProductInfo(chatId, update.getMessage().getText());
            } else if (message.startsWith("/createproductinfo")) {
                if (employeeRepository.findByUsername(update.getMessage().getChat().getUserName()).isEmpty()) {
                    sendMessage(chatId, "Вы не сотрудник BuyHub.");
                    return;
                }
                sendMessage(chatId, "Введите информацию о товаре");
            } else if (message.startsWith("/getlistproducts")) {
                if (employeeRepository.findByUsername(update.getMessage().getChat().getUserName()).isEmpty()) {
                    sendMessage(chatId, "Вы не сотрудник BuyHub.");
                    return;
                }
                getListProducts(chatId);
            } else if (message.startsWith("/deleteproductinfo ")) {
                if (employeeRepository.findByUsername(update.getMessage().getChat().getUserName()).isEmpty()) {
                    sendMessage(chatId, "Вы не сотрудник BuyHub.");
                    return;
                }
                deleteProductInfo(chatId, Long.parseLong(update.getMessage().getText().substring(19)));
            } else if (message.startsWith("/deleteproductinfo")) {
                if (employeeRepository.findByUsername(update.getMessage().getChat().getUserName()).isEmpty()) {
                    sendMessage(chatId, "Вы не сотрудник BuyHub.");
                    return;
                }
                sendMessage(chatId, "Введите ID товара");
            } else if (message.startsWith("/editproductinfo ")) {
                if (employeeRepository.findByUsername(update.getMessage().getChat().getUserName()).isEmpty()) {
                    sendMessage(chatId, "Вы не сотрудник BuyHub.");
                    return;
                }
                editProductInfo(chatId, update.getMessage().getText());
            } else if (message.startsWith("/editproductinfo")) {
                if (employeeRepository.findByUsername(update.getMessage().getChat().getUserName()).isEmpty()) {
                    sendMessage(chatId, "Вы не сотрудник BuyHub.");
                    return;
                }
                sendMessage(chatId, "Введите данные");
            } else if (message.startsWith("/addemployee ")) {
                if (employeeRepository.findByUsername(update.getMessage().getChat().getUserName()).isEmpty()) {
                    sendMessage(chatId, "Вы не сотрудник BuyHub.");
                    return;
                }
                addEmployee(chatId, update.getMessage().getText());
            } else if (message.startsWith("/removeemployee ")) {
                if (employeeRepository.findByUsername(update.getMessage().getChat().getUserName()).isEmpty()) {
                    sendMessage(chatId, "Вы не сотрудник BuyHub.");
                    return;
                }
                removeEmployee(chatId, update.getMessage().getText());
            } else if (message.startsWith("/getlistemployees")) {
                if (employeeRepository.findByUsername(update.getMessage().getChat().getUserName()).isEmpty()) {
                    sendMessage(chatId, "Вы не сотрудник BuyHub.");
                    return;
                }
                getListEmployees(chatId);
            } else if (message.startsWith("/getemployeeinfo ")) {
                if (employeeRepository.findByUsername(update.getMessage().getChat().getUserName()).isEmpty()) {
                    sendMessage(chatId, "Вы не сотрудник BuyHub.");
                    return;
                }
                getEmployeeInfo(chatId, update.getMessage().getText());
            } else if (message.startsWith("/guides")) {
                if (employeeRepository.findByUsername(update.getMessage().getChat().getUserName()).isEmpty()) {
                    sendMessage(chatId, "Вы не сотрудник BuyHub.");
                    return;
                }
                // TODO
            } else if (message.startsWith("/sendmessagefor ")) {
                if (employeeRepository.findByUsername(update.getMessage().getChat().getUserName()).isEmpty()) {
                    sendMessage(chatId, "Вы не сотрудник BuyHub.");
                    return;
                }
                String sendedMessage = sendMessageFor(update.getMessage().getText());
                sendMessage(chatId, "Сообщение отправлено.\nТекст сообщения: " + sendedMessage);
            } else if (message.startsWith("/sendmessageall ")) {
                if (employeeRepository.findByUsername(update.getMessage().getChat().getUserName()).isEmpty()) {
                    sendMessage(chatId, "Вы не сотрудник BuyHub.");
                    return;
                }
                sendMessageForEmployees(message);
            } else
                sendMessage(chatId, "На данный момент такой команды не существует.");
        }
    }

    private HashMap<String, Long> getEmployeesChatId() {
        HashMap<String, Long> employeesChatId = new HashMap<>();
        employeesChatId.put("Олег", 5400824243L);
        employeesChatId.put("Александр", 445155581L);
        employeesChatId.put("Алексей", 1459337756L);
        employeesChatId.put("Станислав", 1054266579L);
        employeesChatId.put("Оперштаб", -898717011L);
        return employeesChatId;
    }

    private void sendMessageForEmployees(String text) {
        text = text.substring(16);
        HashMap<String, Long> employeesChatId = getEmployeesChatId();
        sendMessage(employeesChatId.get("Олег"), text);
        sendMessage(employeesChatId.get("Александр"), text);
        sendMessage(employeesChatId.get("Алексей"), text);
        sendMessage(employeesChatId.get("Станислав"), text);
    }

    private String sendMessageFor(String text) {
        HashMap<String, Long> employeesChatId = getEmployeesChatId();
        String[] textArr = text.substring(16).split(", ");
        String employee = textArr[0];
        String message = textArr[1];
        sendMessage(employeesChatId.get(employee), message);
        return message;
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "Привет, " + name + ". BuyHubWorkBot - это телеграмм бот магазина BuyHub, созданный для упрощения работы его персонала.\n" +
                "С жалобами и предложениями по улучшению бота к - https://t.me/laymonttt.";
        sendMessage(chatId, answer);
    }

    private void tagsCommandReceived(long chatId, String text) {
        String tags = Arrays.toString(text.substring(6).replace('×', ',').split(","));
        sendMessage(chatId, tags.substring(1, tags.length() - 1));
    }

    private void helpCommandReceived(long chatId) {
        sendMessage(chatId,
                "Список доступных команд:\n" +
                        "\n" +
                        "/tags [список тегов] - на вход команде дается скопированный список тегов из карточки на проме (манипуляций с тегами не должно проводиться никаких)," +
                        " а на выходе получаем преобразованный к нужному виду список тегов, который можно сразу же вставлять в карточку.\n" +
                        "----------------------------------------------------------------------------------------------------" +
                        "\n" +
                        "/getlistproducts - получить список всех доступных товаров на данный момент\n" +
                        "----------------------------------------------------------------------------------------------------" +
                        "\n" +
                        "/getproductinfo [название] - поиск тегов и категорий для карточки по названию." +
                        "\n\n" +
                        "Пример: /getproductinfo Обогреватель\n" +
                        "----------------------------------------------------------------------------------------------------" +
                        "\n" +
                        "/createproductinfo [название] [категории] [теги (в преобразованном виде)]" +
                        "\n\n" +
                        "Пример:\n\n" +
                        "/createproductinfo Шлем, Шоломи, бронешоломи та каски, Військторг, Аксесуари та комплектуючі до екіпірування та спорядження, Бронешлем, Бронешлем кевларовый, Шлемы, бронешлемы и каски, Военный шлем, Кевларовый шлем, Баллистический шлем, Шлем тактический, Шлем боевой, Боевой шлем, Каски и шлема, Тактический шлем для страйкбола, Тактический шлем военный, Шлем каска кевлар, Шлем кевлар, Кевлар каска, Кевлар, Шлемы и каски кевларовые, Защитное снаряжение, Тактический военный шлем, Спортивное защитное снаряжение, Шлем 3а класса, Шлем 3 уровень, Шлем, Шлем 3а, Военное обмундирование, Военное снаряжение, Обмундирование, Охота военное обмундирование, Военное обмундирование магазин, Тактический защитный военный шлем, пуленепробиваемый тактический шлем, кевларовый шлем класса iiia, Тактический шлем сша, Армейский шлем, Шлем армейский 3 класса, Спортивные защитное снаряжение\n" +
                        "----------------------------------------------------------------------------------------------------" +
                        "\n" +
                        "/deleteproductinfo [ID] - удаляет информацию о товаре по айдишнику (айди можно узнать с помощью /getproductinfo)" +
                        "\n\n" +
                        "Пример:\n\n" +
                        "/deleteproductinfo 13\n" +
                        "----------------------------------------------------------------------------------------------------" +
                        "\n" +
                        "/editproductinfo [ID] [Параметр (1-3, 1 - название, 2 - категории, 3 - теги)] [Значение]" +
                        "\n\n" +
                        "Пример:\n\n" +
                        "/editproductinfo 1, 1, Тест\n" +
                        "----------------------------------------------------------------------------------------------------" +
                        "\n" +
                        "/getlistemployees - получить список всех сотрудников\n" +
                        "----------------------------------------------------------------------------------------------------" +
                        "\n" +
                        "/addemployee [TG_ID] [Name] - добавить сотрудника" +
                        "\n\n" +
                        "Пример:\n\n" +
                        "/addemployee laymontt, Алексей\n" +
                        "----------------------------------------------------------------------------------------------------" +
                        "\n" +
                        "/removeemployee [TG_ID] - удалить сотрудника" +
                        "\n\n" +
                        "Пример:\n\n" +
                        "/removeemployee laymontt\n" +
                        "----------------------------------------------------------------------------------------------------" +
                        "\n" +
                        "/getemployeeinfo [Name] - получить информацию о сотруднике" +
                        "\n\n" +
                        "Пример:\n\n" +
                        "/getemployeeinfo Алексей\n" +
                        "----------------------------------------------------------------------------------------------------" +
                        "\n" +
                        "/sendmessagefor [Name] - отправить сообщение сотруднику от лица бота." +
                        "\n\n" +
                        "Пример:\n\n" +
                        "/sendmessagefor Алексей, Тест\n" +
                        "----------------------------------------------------------------------------------------------------" +
                        "\n" +
                        "/sendmessageall [Text] - отправить сообщение всем сотрудникам (Александр, Алексей, Станислав, Олег) от лица бота." +
                        "\n\n" +
                        "Пример:\n\n" +
                        "/sendmessageall Тест");
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

        sendMessage(chatId, productService.createProductInfo(productName, categoriesString, tagsString));
    }

    private void getProductInfo(long chatId, String text) {
        String productName = text.substring(16);
        if (productRepository.findByName(productName).isEmpty()) {
            sendMessage(chatId, "Информации об этом товаре нет");
            return;
        }
        Product product = (Product) productService.getProductInfo(productName);
        sendMessage(chatId, "ID товара: " + product.getId() + "\n\n" +
                "Название товара: " + product.getName() + "\n\n" +
                "Категории товара: " + product.getCategories() + "\n\n" +
                "Теги товара: " + product.getTags());
    }

    private void getListProducts(long chatId) {
        sendMessage(chatId, productService.getListProducts().toString());
    }

//    private void getListGuides(long chatId) {
//
//    }

    private void deleteProductInfo(long chatId, long id) {
        String productName = productService.deleteProductInfo(id).toString();
        sendMessage(chatId, "Информация о товаре " + productName + " успешно удалена.");
    }

    private void editProductInfo(long chatId, String text) {
        String[] textArr = text.substring(17).split(", ");
        String answer = productService.editProductInfo(textArr).toString();
        sendMessage(chatId, answer);
    }

    private void addEmployee(long chatId, String text) {
        String employeeName = employeeService.addEmployee(text);
        sendMessage(chatId, "Сотрудник " + employeeName + " успешно добавлен.");
    }

    private void removeEmployee(long chatId, String text) {
        String employeeName = employeeService.removeEmployee(text);
        sendMessage(chatId, "Сотрудник " + employeeName + " удален.");
    }

    private void getListEmployees(long chatId) {
        String employeeNamesString = employeeService.getListEmployees();
        sendMessage(chatId, employeeNamesString.substring(1, employeeNamesString.length() - 1));
    }

    private void getEmployeeInfo(long chatId, String text) {
        String name = text.substring(17);
        if (employeeRepository.findByName(name).isEmpty())
            sendMessage(chatId, "Такого сотрудника нету");
        Employee employee = employeeService.getEmployeeInfo(name);
        sendMessage(chatId, "ID сотрудника: " + employee.getId() + "\n\n" +
                "Имя сотрудника: " + employee.getName() + "\n\n" +
                "Telegram ID: " + employee.getUsername());
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

    // TODO
    private void sendVideo(long chatId) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(String.valueOf(chatId));
//        sendVideo.setVideo(video);
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }
}