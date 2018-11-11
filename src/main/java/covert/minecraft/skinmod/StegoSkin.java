package covert.minecraft.skinmod;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ChatType;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;



@Mod(modid = StegoSkin.modId, name = StegoSkin.name, version = StegoSkin.version, acceptedMinecraftVersions = "[1.12.2]", useMetadata = true, clientSideOnly = true)
public class StegoSkin {

    // Constants
    public static final String modId = "stegskin";
    public static final String name = "StegoSkin";
    public static final String version = "1.12.2";

    // Global Variables
    public static HashMap<String, String> userSkins = new HashMap<>();
    public static HashMap<String, Boolean> usersRelogged = new HashMap<>();
    public static String playerUUID = "";
    public static String skinPath= "../config/%s_skin.png";
    public static String encodedSkinPath = "../config/%s_encoded_skin.png";



    @Mod.Instance(modId)
    public static StegoSkin instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new UpdateSkinCommand());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) { }

    @SubscribeEvent
    public void chatRecieved(ClientChatReceivedEvent event){
        if (event.getType().equals(ChatType.SYSTEM)) {
            String message = event.getMessage().getUnformattedText();
            if (message.contains("left the game")) {
                String[] splitted = message.split("\\s+");
                String username = splitted[0];
                usersRelogged.put(username, true);
            }
        }
    }

    @SubscribeEvent
    public void login(EntityJoinWorldEvent event){
        Entity ent = event.getEntity();
        if (ent instanceof EntityPlayer) {
            // Load the players game profile
            GameProfile profile = ((EntityPlayer) ent).getGameProfile();

            // Store there UUID to be used later to update skin
            playerUUID = profile.getId().toString().replace("-", "");
            skinPath = String.format(skinPath, ent.getName());
            encodedSkinPath = String.format(encodedSkinPath, ent.getName());
        }
    }


    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Post event){
        if (usersRelogged.containsKey(event.getEntityPlayer().getName())) {
            if (!usersRelogged.get(event.getEntityPlayer().getName())) {
                return;
            }
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        SkinManager skinManager = minecraft.getSkinManager();

        // Load the players game profile
        GameProfile profile = event.getEntityPlayer().getGameProfile();


        // This is a function that will be called once the skin has been downloaded and cached
        CustomSkinAvailableCallback callback = new CustomSkinAvailableCallback(event.getEntityPlayer().getName());

        // Force the skin manager to load the skins for this profile
        skinManager.loadProfileTextures(profile, callback, false);
    }
}
