package covert.minecraft.skinmod;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;


public class CustomSkinAvailable implements SkinManager.SkinAvailableCallback{

    private String playerName;

    public CustomSkinAvailable(String playerName){
        this.playerName = playerName;
    }

    public void skinAvailable(MinecraftProfileTexture.Type typeIn,
                       ResourceLocation location,
                       MinecraftProfileTexture profileTexture){

        Minecraft minecraft = Minecraft.getMinecraft();
        System.out.println("Loading skin");
        System.out.println(profileTexture.getUrl());

        try {
            TextureManager manager = minecraft.getTextureManager();

            ThreadDownloadImageData data = (ThreadDownloadImageData) manager.getTexture(location);
            BufferedImage image = ObfuscationReflectionHelper.getPrivateValue(
                    ThreadDownloadImageData.class,
                    data,
                    "bufferedImage",
                    "field_110560_d");

            if (image == null) {
                throw new Exception("skin is null");
            }

            ImageIO.write(image, "png", new File(LSB.ENCODED_IMAGE));
        }
        catch (Exception e){
            System.out.printf("Error loading skin for: %s %n", location);
            System.out.println(e.getMessage());
            for (StackTraceElement el : e.getStackTrace()) {
                System.out.println(el);
            }
            return;
        }

        System.out.printf("Loaded skin to %s %n", LSB.ENCODED_IMAGE);
        System.out.println("Decoding");
        String message = LSB.retrieveMessageFromImage();
        System.out.printf("Message received: %s %n", message);
        System.out.println("Done");
        minecraft.player.sendChatMessage("Message received from " + this.playerName + " : " + message);
        StegSkin.BEEN_READ = true;
    }
}
