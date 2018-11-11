package covert.minecraft.skinmod;

public class ServerResponseTemplate {
    public String accessToken;
    public String clientToken;
    public User user;

    public ServerResponseTemplate(String accessToken, String clientToken, User user) {
        this.accessToken = accessToken;
        this.clientToken = clientToken;
        this.user = user;
    }

    public class User {

        public String id;

        public User(String id) {
            this.id = id;
        }
    }
}
