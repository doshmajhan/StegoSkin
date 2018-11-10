package covert.minecraft.skinmod;

public class ServerResponse {
    public String accessToken;
    public String clientToken;
    public User user;

    public ServerResponse(String accessToken, String clientToken, User user) {
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
