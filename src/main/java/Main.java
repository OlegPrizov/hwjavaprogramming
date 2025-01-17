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
                shortenLink();
            } else if (input.equals("visit link")) {
                visitLink();
            } else if (input.equals("update limit")) {
                updateLimit();
            } else if (input.equals("remove link")) {
                removeLink();
            } else if (input.isEmpty()) {
                System.out.println("Вы ничего не ввели, попробуйте заново");
            } else {
                System.out.println("Вы ввели неверную команду");
            }
        }
    }

    public static void shortenLink() {
        try {
            System.out.println("Вставьте ссылку для сокращения или напишите \"cancel\", чтобы вернуться в главное меню:");
            String longLink = SCANNER.nextLine();

            if (longLink.equalsIgnoreCase("cancel")) {
                System.out.println("Возвращение в главное меню...");
                return; // Выход в главное меню
            }

            String shortLink = "clck.ru/" + LinkGenerator.generateRandomString(7);

            UUID userUuid = null;
            while (true) {
                System.out.println("Введите ваш UUID или напишите \"cancel\", чтобы вернуться в главное меню:");
                String uuidInput = SCANNER.nextLine();

                if (uuidInput.equalsIgnoreCase("cancel")) {
                    System.out.println("Возвращение в главное меню...");
                    return; // Выход в главное меню
                }

                try {
                    userUuid = UUID.fromString(uuidInput);
                    break; // Если UUID корректен, выходим из цикла
                } catch (IllegalArgumentException e) {
                    System.out.println("Неверный формат UUID. Попробуйте снова.");
                }
            }

            long time = System.currentTimeMillis();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            String formattedDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).format(formatter);
            System.out.println("Дата создания: " + formattedDate);

            int limit = -1;
            while (true) {
                System.out.println("Введите лимит переходов (положительное число) или напишите \"cancel\", чтобы вернуться в главное меню:");
                String limitInput = SCANNER.nextLine();

                if (limitInput.equalsIgnoreCase("cancel")) {
                    System.out.println("Возвращение в главное меню...");
                    return; // Выход в главное меню
                }

                try {
                    limit = Integer.parseInt(limitInput);
                    if (limit < 0) {
                        System.out.println("Лимит не может быть отрицательным. Попробуйте снова.");
                    } else {
                        break; // Если лимит корректен, выходим из цикла
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Некорректный ввод. Введите положительное число.");
                }
            }

            DATABASE.saveLink(longLink, shortLink, userUuid, time, limit);
            System.out.println("Запрос обработан. Ваша короткая ссылка: " + shortLink);

        } catch (Exception e) {
            System.out.println("Что-то пошло не так. Попробуйте снова.");
        }
    }

    public static void visitLink() {
        try {
            UUID userUuid = null;
            while (true) {
                System.out.println("Введите ваш UUID или напишите \"cancel\", чтобы вернуться в главное меню:");
                String uuidInput = SCANNER.nextLine();

                if (uuidInput.equalsIgnoreCase("cancel")) {
                    System.out.println("Возвращение в главное меню...");
                    return; // Выход в главное меню
                }

                try {
                    userUuid = UUID.fromString(uuidInput);
                    break; // Если UUID корректен, выходим из цикла
                } catch (IllegalArgumentException e) {
                    System.out.println("Неверный формат UUID. Попробуйте снова.");
                }
            }

            String shortLink;
            while (true) {
                System.out.println("Какая короткая ссылка вас интересует? Вставьте ссылку или напишите \"cancel\", чтобы вернуться в главное меню:");
                shortLink = SCANNER.nextLine();

                if (shortLink.equalsIgnoreCase("cancel")) {
                    System.out.println("Возвращение в главное меню...");
                    return; // Выход в главное меню
                }

                Optional<LinkInfo> optionalLink = DATABASE.getLink(userUuid, shortLink);
                if (optionalLink.isPresent()) {
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
                    if (limit > 0) {
                        int newLimit = limit - 1;
                        System.out.println("Осталось " + newLimit + " переходов");
                        DATABASE.updateLimit(userUuid, newLimit, shortLink);
                    } else if (limit == 0) {
                        System.out.println("Осталось 0 переходов, далее ссылка будет недоступна. Возвращение в главное меню...");
                        return;
                    }

                    String longLink = link.getLongLink();
                    try {
                        Desktop.getDesktop().browse(new URI(longLink));
                    } catch (IOException | URISyntaxException e) {
                        System.out.println("Не удалось перейти по ссылке");
                    }
                    return; // Успешное завершение
                } else {
                    System.out.println("Данная короткая ссылка не была найдена. Проверьте введенные вами данные.");
                }
            }
        } catch (Exception e) {
            System.out.println("Что-то пошло не так. Попробуйте снова.");
        }
    }


    public static void updateLimit() {
        while (true) {
            try {
                // Ввод UUID с проверкой на корректность
                UUID userUuid = null;
                while (true) {
                    System.out.println("Введите ваш UUID (или 'cancel' для возврата в главное меню):");
                    String uuidInput = SCANNER.nextLine();

                    if (uuidInput.equalsIgnoreCase("cancel")) {
                        System.out.println("Возвращаемся в главное меню...");
                        return;  // Возвращаемся в главное меню
                    }

                    // Попробуем преобразовать введенный UUID
                    try {
                        userUuid = UUID.fromString(uuidInput);
                        break;  // Прерываем цикл, если UUID корректен
                    } catch (IllegalArgumentException e) {
                        System.out.println("Неверный формат UUID. Попробуйте снова.");
                    }
                }

                // Ввод короткой ссылки
                String shortLink = null;
                while (true) {
                    System.out.println("Какая короткая ссылка вас интересует? Вставьте ссылку (или 'cancel' для возврата):");
                    shortLink = SCANNER.nextLine();

                    if (shortLink.equalsIgnoreCase("cancel")) {
                        System.out.println("Возвращаемся в главное меню...");
                        return;  // Возвращаемся в главное меню
                    }

                    // Проверка, существует ли короткая ссылка
                    Optional<LinkInfo> linkInfo = DATABASE.getLink(userUuid, shortLink);
                    if (linkInfo.isPresent()) {
                        break;  // Если ссылка найдена, выходим из цикла
                    } else {
                        System.out.println("Короткая ссылка не найдена. Проверьте правильность ввода или создайте новую ссылку.");
                    }
                }

                // Ввод нового лимита
                int newLimit = -1;
                while (true) {
                    System.out.println("Какой новый лимит переходов установить? Старый счетчик и лимит будет сброшен:");
                    try {
                        newLimit = SCANNER.nextInt();
                        SCANNER.nextLine(); // Очистка буфера после nextInt
                        if (newLimit < 0) {
                            System.out.println("Лимит не может быть отрицательным. Попробуйте снова.");
                            continue;
                        }
                        break;  // Прерываем цикл, если новый лимит корректен
                    } catch (Exception e) {
                        System.out.println("Некорректный ввод. Попробуйте снова.");
                        SCANNER.nextLine();  // Очистка буфера после некорректного ввода
                    }
                }

                // Обновление лимита в базе данных
                DATABASE.updateLimit(userUuid, newLimit, shortLink);
                System.out.println("Новый лимит: " + newLimit + ". Возвращение в главное меню...");
                break;  // Выход из цикла после успешного обновления

            } catch (Exception e) {
                System.out.println("Что-то пошло не так. Попробуйте снова.");
            }
        }
    }

    public static void removeLink() {
        while (true) {
            try {
                // Ввод UUID с проверкой на корректность
                UUID userUuid = null;
                while (true) {
                    System.out.println("Введите ваш UUID (или 'cancel' для возврата в главное меню):");
                    String uuidInput = SCANNER.nextLine();

                    if (uuidInput.equalsIgnoreCase("cancel")) {
                        System.out.println("Возвращаемся в главное меню...");
                        return;  // Возвращаемся в главное меню
                    }

                    // Попробуем преобразовать введенный UUID
                    try {
                        userUuid = UUID.fromString(uuidInput);
                        break;  // Прерываем цикл, если UUID корректен
                    } catch (IllegalArgumentException e) {
                        System.out.println("Неверный формат UUID. Попробуйте снова.");
                    }
                }

                // Ввод короткой ссылки
                while (true) {
                    System.out.println("Какая короткая ссылка вас интересует? Вставьте ссылку (или 'cancel' для возврата):");
                    String shortLink = SCANNER.nextLine();

                    if (shortLink.equalsIgnoreCase("cancel")) {
                        System.out.println("Возвращаемся в главное меню...");
                        break;  // Выход из внутреннего цикла, возвращение в главное меню
                    }

                    Optional<LinkInfo> link = DATABASE.getLink(userUuid, shortLink);
                    if (link.isPresent()) {
                        DATABASE.deleteLink(userUuid, shortLink);
                        System.out.println("Ссылка удалена. Возвращаемся в главное меню...");
                        break;  // Выход из внешнего цикла, если все прошло успешно
                    } else {
                        System.out.println("Данная короткая ссылка не была найдена, проверьте введенные вами данные");
                        // Цикл продолжается, если ссылка не найдена
                    }
                }

                // Если мы достигли этого момента, значит, операция завершена или прервана
                break; // Выход из основного цикла после успешного удаления или отмены операции
            } catch (Exception e) {
                System.out.println("Что-то пошло не так. Попробуйте снова.");
            }
        }
}
}