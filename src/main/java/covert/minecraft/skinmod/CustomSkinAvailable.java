package covert.minecraft.skinmod;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class CustomSkinAvailable implements SkinManager.SkinAvailableCallback{

    private final String IMAGE_PATH = "/home/dosh/StegoSkin/skin.png";
    private static final String DECODE_OUTPUT = "/home/dosh/decoded.txt";
    
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

        //System.out.println("Encoding");
        //leastSignificantBitEncryption(IMAGE, MESSAGE, ENCODED_IMAGE);
        //System.out.println("Done");

        System.out.println("Decoding");
        leastSignificantBitDecryption(IMAGE_PATH, DECODE_OUTPUT);
        System.out.println("Done");
    }

    public static void leastSignificantBitEncryption(String imageSource, String message, String newPath) {
        BufferedImage image = returnImage(imageSource);
        //prepare variables
        String[] messageBinString = null;
        String[] pixelBinString = null;
        final byte[] messageBin = message.getBytes(StandardCharsets.UTF_8);
        final byte[] pixelsBin = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        //convert message and image to binary string array
        try {
            messageBinString = stringToBinaryStrings(messageBin);
            pixelBinString = stringToBinaryStrings(pixelsBin);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String[] messageBinStringCut = splitIn2Bit(messageBinString);   //split message binary into 2 bit strings
        String[] pixelBinStringNew = pixelBinString.clone();    //insert 2 bit strings in last 2 bits of bytes from bitmap
        insert2Bit(messageBinStringCut, pixelBinStringNew);
        byte[] pixelsBinNew = stringArrayToByteArray(pixelBinStringNew);    //Convert string array to byte array
        try {   //Create new image out of bitmap
            int w = image.getWidth();
            int h = image.getHeight();
            BufferedImage imageNew = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
            imageNew.setData(Raster.createRaster(imageNew.getSampleModel(), new DataBufferByte(pixelsBinNew, pixelsBinNew.length), new Point()));
            File imageFile = new File(newPath);
            ImageIO.write(imageNew, "png", imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void leastSignificantBitDecryption(String imageSource, String outputPath) {
        BufferedImage image = returnImage(imageSource);
        //prepare variables
        String[] pixelsBinString = null;
        final byte[] pixelsBin = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        //convert image to string array with binary values
        try {
            pixelsBinString = stringToBinaryStrings(pixelsBin);
        } catch (UnsupportedEncodingException e) {
            System.out.println("UnsupportedEncodingException.\nDECRPYTION FAILED.");
        }
        //convert last 2 bits of every byte into message as binary values
        String[] messageBin = new String[pixelsBinString.length];
        for(int i = 0; i < pixelsBinString.length; i ++) {	//generate array of 2 bit strings containing message
            messageBin[i] = pixelsBinString[i].substring(6, 8);
        }
        String[] messageBinByte = new String[(int) (messageBin.length / 4) + 3];
        for(int i = 0; i < messageBin.length; i += 4) {	//build string array containing message as bytes
            try {
                messageBinByte[i / 4] = messageBin[i] + messageBin[i + 1] + messageBin[i + 2] + messageBin[i + 3];
            } catch(ArrayIndexOutOfBoundsException e) {}
        }
        //convert binary value message to normal message string
        try {
            PrintWriter out = new PrintWriter(outputPath);
            out.print(binaryStringsToString(messageBinByte));
            out.close();
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            System.out.println("Exception.\nDECRPYTION FAILED.");
        }
        System.out.println("DECRYPTION SUCCESS.");
    }

    private static String[] stringToBinaryStrings(byte[] messageBin) throws UnsupportedEncodingException{
        String[] bytes = new String[messageBin.length];
        int i = 0;
        for(byte b : messageBin) {
            bytes[i] = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            i++;
        }
        return bytes;
    }

    private static String binaryStringsToString(String[] messageBin) throws UnsupportedEncodingException{
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        while(messageBin[i] != null) {
            stringBuilder.append((char) Integer.parseInt(messageBin[i], 2));
            i++;
        }
        return stringBuilder.toString();
    }

    private static BufferedImage returnImage(String imageSource) {
        try{
            try {
                return ImageIO.read(new URL(imageSource));
            } catch (MalformedURLException e) {
                return ImageIO.read(new File(imageSource));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    private static byte[] stringArrayToByteArray(String[] stringArray) {
        byte[] byteArray = new byte[stringArray.length];
        for(int i = 0; i < stringArray.length; i++) {
            byteArray[i] = (byte) Integer.parseInt(stringArray[i], 2);
        }
        return byteArray;
    }

    private static String[] splitIn2Bit(String[] inputArray) {
        String[] outputArray = new String[inputArray.length * 4];
        for(int i = 0; i < outputArray.length; i += 4) {
            String[] splitByte = inputArray[i / 4].split("(?<=\\G..)");
            outputArray[i] = splitByte[0];
            outputArray[i + 1] = splitByte[1];
            outputArray[i + 2] = splitByte[2];
            outputArray[i + 3] = splitByte[3];
        }
        return outputArray;
    }

    private static String[] insert2Bit(String[] twoBitArray, String[] insertArray) {
        for(int i = 0; i < twoBitArray.length; i++) {
            insertArray[i] = insertArray[i].substring(0, 6) + twoBitArray[i];
        }
        return insertArray;
    }

}
