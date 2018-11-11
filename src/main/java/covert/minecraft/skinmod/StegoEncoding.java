package covert.minecraft.skinmod;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class StegoEncoding {

    private static final int MAX_MESSAGE_SIZE = 350;

    public static void storeMessage(String message) {
        if (message.length() > MAX_MESSAGE_SIZE) {
            System.out.println("Message is too big");
            return;
        }
        BufferedImage image = StegoHelper.loadImage(StegoSkin.skinPath);
        addMessageToImage(message, image);
    }

    /**
     * The main method to add a message to an image. It runs through every pixel starting
     * at the top left and adds a character represented by 8 bits to the 3 color channels
     * (Red, Green, Blue) of the pixel. It replaces the last 2 bits of the binary value
     * for each channel by 2 bits of the character binaries. The adding process is finished
     * by adding an end block of "111111" to a pixel.
     * @param secretMessage     The message to add to the image.
     * @param image             The image as BufferedImage to which the message is added.
     */
    private static void addMessageToImage(String secretMessage, BufferedImage image) {
        ArrayList<String> binaryChunks = convertStringToSixBitChunks(secretMessage);
        int count = 0;

        outer:
        for (int row = 0; row < image.getHeight(); row++) {
            for (int column = 0; column < image.getWidth(); column++) {

                int rgb = image.getRGB(column, row);
                Color oldColor = new Color(rgb, true);

                // If we write to anything that has a 0 for alpha channel, minecraft will overwrite it
                if (oldColor.getAlpha() == 0) {
                    continue;

                }

                // We have stored all the data
                if (count == binaryChunks.size()){
                    break outer;
                }
                String messageBlock = binaryChunks.get(count);
                count ++;

                ArrayList<String> rgbaOld = StegoHelper.getColorBinaries(oldColor);
                ArrayList<String> rgbaNew = new ArrayList<>();

                // - 1 on size because we don't want to include the last entry which is the alpha channel
                for (int i = 0; i < rgbaOld.size() - 1; i++) {
                    rgbaNew.add(rgbaOld.get(i).substring(0, 6) + messageBlock.substring(i * 2, (i + 1) * 2));
                }
                // Add the alpha channel back in because we need it to create the image correctly
                rgbaNew.add(rgbaOld.get(rgbaOld.size() - 1));

                ArrayList<Integer> newColorComponents = rgbaToInt(rgbaNew);
                newColorComponents = checkMaxValuesOfComponents(newColorComponents);

                Color newColor = new Color(
                        newColorComponents.get(0),
                        newColorComponents.get(1),
                        newColorComponents.get(2),
                        newColorComponents.get(3));

                image.setRGB(column, row, newColor.getRGB());
            }
        }
        saveImage(image);
    }

    /**
     * Checks whether the changed values of the color components are larger than 255.
     * If they are they are replaced with 255. This functions as a safeguard in case something
     * goes wrong somewhere else as the maximum value of an 8 bit Integer representation can
     * only be 255 max. However, I left this in for safety reasons.
     * @param input     The changed color component values as Integers.
     * @return          Color component values which are not larger than 255.
     */
    private static ArrayList<Integer> checkMaxValuesOfComponents(ArrayList<Integer> input) {
        for (Integer e : input)
            input.set(input.indexOf(e), e < 255 ? e : 255);

        return input;
    }

    /**
     * Converts an ArrayList of Binaries as Strings to an ArrayList of their Integer values.
     * @param input     An ArrayList of color component values as binaries as strings.
     * @return          An ArrayList of color component values as Integers.
     */
    private static ArrayList<Integer> rgbaToInt(ArrayList<String> input) {
        ArrayList<Integer> out = new ArrayList<>();

        for (String e : input)
            out.add(Integer.parseInt(e, 2));

        return out;
    }


    /**
     * Saves a BufferedImage to a .png file at the location of this program.
     * @param image     The BufferedImage to be saved.
     */
    private static void saveImage(BufferedImage image) {
        try {
            File output = new File(StegoSkin.encodedSkin);
            ImageIO.write(image, "png", output);
            System.out.println("Saved output to: " + output.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Could not write the output to .png file.");
            e.printStackTrace();
        }
    }

    /**
     * Converts a String of Characters to a array of 6 bit binary chunks
     * @param input     The String to be converted.
     * @return          An array of 6 bit chunks of the original string
     */
    private static ArrayList<String> convertStringToSixBitChunks(String input) {
        int index = 0;
        String binary = "";
        ArrayList<String> chunks = new ArrayList<String>();

        // Convert whole string to binary digits
        char[] charMessage = input.toCharArray();
        for (char c : charMessage){
            binary += StegoHelper.intToBinaryString(c);
        }

        // Go through and split up into 6 bit chunks so we can write to the RGB channel easier
        while (index < binary.length()) {
            String chunk = binary.substring(index, Math.min(index + 6, binary.length()));

            // If the chunk comes out smaller than 6 just pad with 0's
            if(chunk.length() < 6) {
                while (chunk.length() < 6) {
                    chunk += "0";
                }
            }
            chunks.add(chunk);
            index += 6;
        }

        // Finish off with our flag for the end chunk
        chunks.add(StegoHelper.END_CHUNK_FLAG);
        return chunks;
    }
}
