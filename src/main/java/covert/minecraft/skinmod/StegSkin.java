package covert.minecraft.skinmod;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.fixes.EntityId;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.Level;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Mod(modid = StegSkin.modId, name = StegSkin.name, version = StegSkin.version, acceptedMinecraftVersions = "[1.12.2]", useMetadata = true, clientSideOnly = true)
public class StegSkin {

    public static final String modId = "stegskin";
    public static final String name = "StegSkin";
    public static final String version = "1.12.2";
    public final String IMAGE_PATH = "/home/dosh/StegoSkin/skin.png";
    public final String SKINS_URL = "http://skins.minecraft.net/MinecraftSkins/%s.png";

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

    /*@SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event){
        List nearbyPlayers = new ArrayList();
        Minecraft minecraft = Minecraft.getMinecraft();

        if (minecraft.player != null){
            List<EntityPlayer> entitiesNearby = minecraft.player.world.playerEntities;
            if (entitiesNearby.size() > 1){
                for (int x = 1; x < entitiesNearby.size(); x++){
                    nearbyPlayers.add(entitiesNearby.get(x));
                }
                EntityOtherPlayerMP dosh = (EntityOtherPlayerMP)nearbyPlayers.get(0);
                ResourceLocation skin = dosh.getLocationSkin();
                ThreadDownloadImageData data = (ThreadDownloadImageData)minecraft.getTextureManager().getTexture(skin);

                try{
                    BufferedImage image = ObfuscationReflectionHelper.getPrivateValue(ThreadDownloadImageData.class, data, "field_110560_d", "bufferedImage");
                    ImageIO.write(image, "png", new File(IMAGE_PATH));
                } catch (java.io.IOException e){
                    System.out.println(e.getMessage());
                }
                //FMLLog.getLogger().log(Level.INFO, dosh.getLocationSkin());
                //FMLLog.getLogger().log(Level.INFO, dosh.getSkinType());
            }
        }


    }*/

    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Post event){
        Minecraft minecraft = Minecraft.getMinecraft();
        SkinManager skinManager = minecraft.getSkinManager();

        // Load the players game profile
        GameProfile profile = event.getEntityPlayer().getGameProfile();


        // This is a function that will be called once the skin has been downloaded and cached
        CustomSkinAvailable callback = new CustomSkinAvailable();

        // Force the skin manager to load the skins for this profile
        skinManager.loadProfileTextures(profile, callback, false);
    }

}