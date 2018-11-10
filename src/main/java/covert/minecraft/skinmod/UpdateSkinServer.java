package covert.minecraft.skinmod;

import com.google.common.collect.Lists;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class UpdateSkinServer implements ICommand {
    private static final String AUTH_URL = "https://authserver.mojang.com/authenticate";
    private static final String SKIN_UPLOAD_URL = "https://api.mojang.com/user/profile/%s/skin";
    private static final String CREDS_FILE = "../creds.txt";
    private static String AUTH_TEMPLATE = "{\"username\": \"%s\", \"password\": \"%s\", \"captchaSupported\": \"%s\", \"requestUser\": \"%b\" }";

    @Override
    public int compareTo(ICommand arg0) {
        return 0;
    }

    @Override
    public String getName() {
        return "skinMessage";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Set message to be stored in skin";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender icommandsender, String[] args) {
        if (args.length != 1) {
            return;
        }
        String message = args[0];
        LSB.storeMessage(message);
        boolean result = updateSkin();
        System.out.println(result);
    }

    @Override
    public List<String> getAliases() {
        List<String> aliases = Lists.<String>newArrayList();
        aliases.add("/skinMessage");
        return aliases;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          BlockPos targetPos) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    /**
     * Authorizes our user usings credentials stored in the CREDS_FILE
     * and returns the authorization token as a result
     * @return authorization token for user
     */
    public static String authorizeUser(){
        System.out.println("Authorizing");
        List<String> creds;
        try {
            creds = Files.readAllLines(Paths.get(CREDS_FILE));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return "";
        }

        HttpClient client = HttpClients.createDefault();

        String json = String.format(AUTH_TEMPLATE, creds.get(0), creds.get(1), "None", true);
        StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        requestEntity.setChunked(true);

        HttpPost post = new HttpPost(AUTH_URL);
        post.addHeader("content-type", "application/json;charset=UTF-8");
        post.setEntity(requestEntity);

        String response;
        try {
            HttpResponse resp = client.execute(post);
            HttpEntity respEntity = resp.getEntity();
            response = EntityUtils.toString(respEntity);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return "";
        }

        Gson g = new Gson();
        ServerResponse user = g.fromJson(response, ServerResponse.class);

        return user.accessToken;
    }

    /**
     * Makes a request to update the skin with our newly encoded skin store in the skin path provided
     *
     * @return true if successful, false if not
     */
    public static boolean updateSkin(){
        System.out.println("Updating skin");
        String skinPath = LSB.MY_ENCODED_SKIN;
        String fileName = skinPath.substring(skinPath.lastIndexOf("\\") + 1);

        String accessToken = authorizeUser();
        HttpClient client = HttpClients.createDefault();

        HttpPut put = new HttpPut(String.format(SKIN_UPLOAD_URL, StegSkin.skinLocation));
        put.addHeader("authorization", "Bearer " + accessToken);

        HttpEntity httpEntity = MultipartEntityBuilder.create()
                .addTextBody("model", "classic")
                .addBinaryBody("file", new File(skinPath), ContentType.create("image/png"), fileName)
                .build();

        put.setEntity(httpEntity);
        try {
            HttpResponse resp = client.execute(put);
            System.out.println(resp.getStatusLine().getStatusCode());
            if (resp.getStatusLine().getStatusCode() == 204) {
                return true;
            }
            return false;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }
}
