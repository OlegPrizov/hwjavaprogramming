import java.util.HashMap;
import java.util.UUID;
import java.util.Optional;

public class Database {
    private HashMap<UUID, HashMap<String, LinkInfo>> information = new HashMap<>();

    public UUID createUser() {
        UUID uuid = UUID.randomUUID();
        HashMap<String, LinkInfo> userLinks = new HashMap<>();
        information.put(uuid, userLinks);
        return uuid;
    }

    public void saveLink(String longLink, String shortLink, UUID userUuid, long linkCreatedDate, int limit) {
        HashMap<String, LinkInfo> links = information.get(userUuid);
        if (links == null) {
            throw new IllegalArgumentException("User does not exist.");
        }
        LinkInfo link = new LinkInfo(longLink, linkCreatedDate, limit);
        links.put(shortLink, link);
    }

    public Optional<LinkInfo> getLink(UUID userUuid, String shortLink) {
        HashMap<String, LinkInfo> links = information.get(userUuid);
        if (links == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(links.get(shortLink));
    }

    public void deleteLink(UUID userUuid, String shortLink) {
        HashMap<String, LinkInfo> links = information.get(userUuid);
        if (links == null || !links.containsKey(shortLink)) {
            throw new IllegalArgumentException("Link not found.");
        }
        links.remove(shortLink);
    }

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