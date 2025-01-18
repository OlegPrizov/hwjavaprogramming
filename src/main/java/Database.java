import java.util.HashMap;
import java.util.UUID;
import java.util.Optional;

// класс с простой базой данных и основным функционалом
public class Database {
    private HashMap<UUID, HashMap<String, LinkInfo>> information = new HashMap<>();

    // метод создания пользователя
    public UUID createUser() {
        UUID uuid = UUID.randomUUID();
        HashMap<String, LinkInfo> userLinks = new HashMap<>();
        information.put(uuid, userLinks);
        return uuid;
    }

    // метод сохранения ссылки
    public void saveLink(String longLink, String shortLink, UUID userUuid, long linkCreatedDate, int limit) {
        HashMap<String, LinkInfo> links = information.get(userUuid);
        if (links == null) {
            throw new IllegalArgumentException("User does not exist.");
        }
        LinkInfo link = new LinkInfo(longLink, linkCreatedDate, limit);
        links.put(shortLink, link);
    }

    // метод получения ссылки
    public Optional<LinkInfo> getLink(UUID userUuid, String shortLink) {
        HashMap<String, LinkInfo> links = information.get(userUuid);
        if (links == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(links.get(shortLink));
    }

    // метод удаления ссылки
    public void deleteLink(UUID userUuid, String shortLink) {
        HashMap<String, LinkInfo> links = information.get(userUuid);
        if (links == null || !links.containsKey(shortLink)) {
            throw new IllegalArgumentException("Link not found.");
        }
        links.remove(shortLink);
    }

    // метод обновления лимита ссылки
    public void updateLimit(UUID userUuid, int limit, String shortLink) {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit cannot be negative.");
        }
        LinkInfo link = information.get(userUuid).get(shortLink);
        if (link != null) {
            link.setLimit(limit);
        } else {
            throw new IllegalArgumentException("Link not found.");
        }
    }
}