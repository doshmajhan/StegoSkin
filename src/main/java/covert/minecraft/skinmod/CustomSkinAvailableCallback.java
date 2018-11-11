package covert.minecraft.skinmod;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.commons.codec.digest.DigestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;


public class CustomSkinAvailableCallback implements SkinManager.SkinAvailableCallback{

    private String playerName;

    public CustomSkinAvailableCallback(String playerName){
        this.playerName = playerName;
    }

    public void skinAvailable(MinecraftProfileTexture.Type typeIn,
                       ResourceLocation location,
                       MinecraftProfileTexture profileTexture){

        Minecraft minecraft = Minecraft.getMinecraft();
        String imageSavePath = String.format(StegoHelper.ENCODED_IMAGE_TEMPLATE, this.playerName);
        String md5;

        System.out.println(location);
        System.out.println(profileTexture.getHash());
        System.out.println("Loading skin");
        System.out.println(profileTexture.getUrl());

        // Get the image from the game
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

            ImageIO.write(image, "png", new File(imageSavePath));
        }
        catch (Exception e){
            System.out.printf("Error loading skin for: %s %n", location);
            System.out.println(e.getMessage());
            for (StackTraceElement el : e.getStackTrace()) {
                System.out.println(el);
            }
            return;
        }


        System.out.printf("Loaded skin to %s %n", imageSavePath);

        // Get md5 of image
        try {
            FileInputStream fis = new FileInputStream(new File(imageSavePath));
            md5 = DigestUtils.md5Hex(fis);

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return;
        }

        // Check if md5 is the same
        if (StegoSkin.userSkins.containsKey(this.playerName)){
            System.out.println("Player seen before, checking if new message");
            if (StegoSkin.userSkins.get(this.playerName).equals(md5)){
                System.out.println("No new message seen");
                // Update the users login status
                StegoSkin.usersRelogged.put(this.playerName, false);
                return;
            }
            System.out.println("New message detected");
        }

        System.out.println("Decoding");
        String message = StegoDecoding.retrieveMessageFromImage(this.playerName);
        System.out.printf("Message received: %s %n", message);
        System.out.println("Done");

        // Display message to user in game
        minecraft.player.sendMessage(
                new TextComponentString("Message received from " + this.playerName + " : " + message));

        // Update skins dictionary with new md5 hash
        StegoSkin.userSkins.put(this.playerName, md5);

        // Update the users login status
        StegoSkin.usersRelogged.put(this.playerName, false);
    }
}
