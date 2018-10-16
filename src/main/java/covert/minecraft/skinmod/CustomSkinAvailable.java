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
import java.io.File;


public class CustomSkinAvailable implements SkinManager.SkinAvailableCallback{

    private final String IMAGE_PATH = "/home/dosh/StegoSkin/skin.png";

    public CustomSkinAvailable(){}

    public void skinAvailable(MinecraftProfileTexture.Type typeIn,
                       ResourceLocation location,
                       MinecraftProfileTexture profileTexture){

        Minecraft minecraft = Minecraft.getMinecraft();
        System.out.println("Loading skin");

        try {
            TextureManager manager = minecraft.getTextureManager();

            ThreadDownloadImageData data = (ThreadDownloadImageData) manager.getTexture(location);
            BufferedImage image = ObfuscationReflectionHelper.getPrivateValue(
                    ThreadDownloadImageData.class,
                    data,
                    "bufferedImage",
                    "field_110560_d");

            ImageIO.write(image, "png", new File(IMAGE_PATH));
        }
        catch (Exception e){
            System.out.printf("Error loading skin for: %s %n", location);
            System.out.println(e.getMessage());
            for (StackTraceElement el : e.getStackTrace()) {
                System.out.println(el);
            }
        }

        System.out.printf("Loaded skin to %s %n", IMAGE_PATH);
    }

}
