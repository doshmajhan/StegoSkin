package covert.minecraft.skinmod;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.text.ChatType;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.HashMap;



@Mod(modid = StegSkin.modId, name = StegSkin.name, version = StegSkin.version, acceptedMinecraftVersions = "[1.12.2]", useMetadata = true, clientSideOnly = true)
public class StegSkin {

    public static final String modId = "stegskin";
    public static final String name = "StegSkin";
    public static final String version = "1.12.2";
    public static HashMap<String, String> userSkins = new HashMap<>();
    public static HashMap<String, Boolean> usersRelogged = new HashMap<>();

    @Mod.Instance(modId)
    public static StegSkin instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

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
        CustomSkinAvailable callback = new CustomSkinAvailable(event.getEntityPlayer().getName());

        // Force the skin manager to load the skins for this profile
        skinManager.loadProfileTextures(profile, callback, false);
    }

}
