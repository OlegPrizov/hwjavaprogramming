import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    private static final Database DATABASE = new Database();
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final int LIVE_TIME_LINK_HOUR = Constants.LINK_LIFE_TIME_HOURS;

    public static void main(String[] args) {

        System.out.println("Доступные команды:\n" +
                "1. 'register user' - зарегистрировать нового пользователя. Этот процесс создаст уникальный идентификатор (UUID) для управления вашими ссылками.\n" +
                "2. 'shorten link' - сократить длинную ссылку. Введите URL, который вы хотите сократить, а также установите лимит переходов и время жизни ссылки.\n" +
                "3. 'visit link' - перейти по короткой ссылке. Введите UUID и короткую ссылку, чтобы открыть длинный URL, если ссылка ещё действительна и лимит переходов не исчерпан.\n" +
                "4. 'update limit' - изменить лимит переходов для уже существующей короткой ссылки. Укажите новое количество доступных переходов для этой ссылки.\n" +
                "5. 'remove link' - удалить короткую ссылку. Введите UUID и короткую ссылку, чтобы полностью удалить её из системы, если такая существует.\n" +
                "Для выхода из программы используйте Ctrl+C.");


        while (true) {
            String input = SCANNER.nextLine();
            if (input.equals("register user")) {
                UUID uuid = DATABASE.createUser();
                System.out.println("Пользователь создан: " + uuid);
            } else if (input.equals("shorten link")) {
                insertLink();
            } else if (input.equals("visit link")) {
                linkTo();
            } else if (input.equals("update limit")) {
                editLink();
            } else if (input.equals("remove link")) {
                deleteLink();
            } else if (input.isEmpty()) {
                System.out.println("Вы ничего не ввели, попробуйте заново");
            } else {
                System.out.println("Вы ввели неверную команду");
            }
        }
    }

    public static void insertLink() {
        try {
            System.out.println("Вставьте ссылку для сокращения");
            String longLink = SCANNER.nextLine();

            String shortLink = "clck.ru/" + LinkGenerator.generateRandomString(7);

            System.out.println("Введите ваш UUID");
            UUID userUuid = UUID.fromString(SCANNER.nextLine());

            long time = System.currentTimeMillis();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            String formattedDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).format(formatter);
            System.out.println("Дата создания: " + formattedDate);

            System.out.println("Введите лимит переходов");
            int limit = SCANNER.nextInt();
            SCANNER.nextLine(); // Очистка буфера после nextInt

            if (limit < 0) {
                System.out.println("Лимит не может быть отрицательным");
                return;
            }

            DATABASE.saveLink(longLink, shortLink, userUuid, time, limit);

            System.out.println("Запрос обработан. Ваша короткая ссылка: " + shortLink);
        } catch (Exception e) {
            System.out.println("Что-то пошло не так. Попробуйте снова");
        }
    }

    public static void linkTo() {
        try {
            System.out.println("Введите ваш UUID:");
            UUID userUuid = UUID.fromString(SCANNER.nextLine());
            System.out.println("Какая короткая ссылка вас интересует? Вставьте ссылку:");
            String shortLink = SCANNER.nextLine();

            Optional<LinkInfo> optionalLink = DATABASE.getLink(userUuid, shortLink);
            if (!optionalLink.isPresent()) {
                System.out.println("Данная короткая ссылка не была найдена, проверьте введенные вами данные");
                return;
            }

            LinkInfo link = optionalLink.get();
            long timeCreate = link.getLinkCreatedDate();
            long lifeTime = System.currentTimeMillis() - timeCreate;
            long linkLiveTime = (long) LIVE_TIME_LINK_HOUR * 60 * 60 * 1000;

            if (linkLiveTime < lifeTime) {
                System.out.println("Ссылка недействительна, истек срок действия");
                DATABASE.deleteLink(userUuid, shortLink);
                return;
            }

            int limit = link.getLimit();
            if (limit > 1) {
                int newLimit = limit - 1;
                System.out.println("Осталось " + newLimit + " переходов");
                DATABASE.updateLimit(userUuid, newLimit, shortLink);
            } else if (limit == 1) {
                int newLimit = -1;
                System.out.println("Осталось 0 переходов, далее ссылка будет недоступна");
                DATABASE.updateLimit(userUuid, newLimit, shortLink);
            } else if (limit == -1) {
                System.out.println("Ссылка недоступна");
                return;
            }

            String longLink = link.getLongLink();
            try {
                Desktop.getDesktop().browse(new URI(longLink));
            } catch (IOException | URISyntaxException e) {
                System.out.println("Не удалось перейти по ссылке");
            }
        } catch (Exception e) {
            System.out.println("Что-то пошло не так. Попробуйте снова");
        }
    }

    public static void editLink() {
        try {
            System.out.println("Введите ваш UUID");
            UUID userUuid = UUID.fromString(SCANNER.nextLine());
            System.out.println("Какая короткая ссылка вас интересует? Вставьте ссылку:");
            String shortLink = SCANNER.nextLine();
            System.out.println("Какой новый лимит переходов установить? Старый счетчик и лимит будет сброшен:");
            int newLimit = SCANNER.nextInt();
            SCANNER.nextLine(); // Очистка буфера после nextInt

            if (newLimit < 0) {
                System.out.println("Лимит не может быть отрицательным");
                return;
            }

            DATABASE.updateLimit(userUuid, newLimit, shortLink);
            System.out.println("Новый лимит: " + newLimit);
        } catch (Exception e) {
            System.out.println("Вы сделали что-то неправильно.Попробуйте снова");
        }
    }

    public static void deleteLink() {
        try {
            System.out.println("Введите ваш UUID");
            UUID userUuid = UUID.fromString(SCANNER.nextLine());
            System.out.println("Какая короткая ссылка вас интересует? Вставьте ссылку:");
            String shortLink = SCANNER.nextLine();

            Optional<LinkInfo> link = DATABASE.getLink(userUuid, shortLink);
            if (link.isPresent()) {
                DATABASE.deleteLink(userUuid, shortLink);
                System.out.println("Ссылка удалена");
            } else {
                System.out.println("Данная короткая ссылка не была найдена, проверьте введенные вами данные");
            }
        } catch (Exception e) {
            System.out.println("Что-то пошло не так. Попробуйте снова");
        }
    }
}

