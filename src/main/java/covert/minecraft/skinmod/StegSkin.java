package covert.minecraft.skinmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.fixes.EntityId;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.Level;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Mod(modid = StegSkin.modId, name = StegSkin.name, version = StegSkin.version, acceptedMinecraftVersions = "[1.12.2]", useMetadata = true, clientSideOnly = true)
public class StegSkin {

    public static final String modId = "stegskin";
    public static final String name = "StegSkin";
    public static final String version = "1.12.2";

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
    public void onTick(TickEvent.PlayerTickEvent event){
        List nearbyPlayers = new ArrayList();
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player != null){
            List<EntityPlayer> entitiesNearby = Minecraft.getMinecraft().player.world.playerEntities;
            if (Minecraft.getMinecraft().player.world.playerEntities.size() > 1){
                for (int x = 1; x < Minecraft.getMinecraft().player.world.playerEntities.size(); x++){
                    nearbyPlayers.add(entitiesNearby.get(x));
                }
                EntityOtherPlayerMP dosh = (EntityOtherPlayerMP)nearbyPlayers.get(0);
                ResourceLocation skin = new ResourceLocation(dosh.getLocationSkin().toString());
                ThreadDownloadImageData data = (ThreadDownloadImageData)minecraft.getTextureManager().getTexture(skin);

                try{
                    BufferedImage image = ReflectionHelper.getPrivateValue(ThreadDownloadImageData.class, data, "bufferedImage", "field_110560_d");
                    ImageIO.write(image, "png", new File("D://Documents/image.png"));
                } catch (java.io.IOException e){
                    System.out.println(e.getMessage());
                }
                //FMLLog.getLogger().log(Level.INFO, dosh.getLocationSkin());
                //FMLLog.getLogger().log(Level.INFO, dosh.getSkinType());
            }
        }

    }

}