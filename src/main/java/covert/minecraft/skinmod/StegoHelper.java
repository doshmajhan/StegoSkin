package covert.minecraft.skinmod;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;

public class StegoHelper {

    static final String ENCODED_IMAGE_TEMPLATE = "../config/%s_encoded_skin.png";
    static final String END_CHUNK_FLAG = "111111";

    /**
     * Loads an image from the given input path.
     * It is important that the BufferedImage received from the ImageIO.read() function
     * has a ColorModel which does not support the saving of alpha channels. Therefore
     * we have to create a new BufferedImage with a different ColorModel that supports
     * changing the alpha channel.
     * @param input     path to image
     * @return          BufferedImage of image
     */
    static BufferedImage loadImage(String input) {
        try {
            File file = new File(input);
            BufferedImage in = ImageIO.read(file);
            BufferedImage newImage = new BufferedImage(
                    in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = newImage.createGraphics();
            g.drawImage(in, 0, 0, null);
            g.dispose();

            return newImage;
        } catch (IOException e) {
            System.err.println("Could not read image with give file path.");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Converts the Integer values of the color components to Binary values as Strings.
     * @param color     The color which color components are wanted.
     * @return          An ArrayList of Strings containing the color component values as binaries.
     */
    static ArrayList<String> getColorBinaries(Color color) {
        ArrayList<String> out = new ArrayList<>();
        out.add(intToBinaryString(color.getRed()));
        out.add(intToBinaryString(color.getGreen()));
        out.add(intToBinaryString(color.getBlue()));
        out.add(intToBinaryString(color.getAlpha()));

        return out;
    }

    /**
     * Converts an Integer to a Binary String.
     * @param input     The Integer to be converted.
     * @return          A String containing the binary value of the input Integer.
     */
    static String intToBinaryString(int input) {
        return fillString(Integer.toBinaryString(input));
    }

    /**
     * Adds 0's to the beginning of a String of binaries to ensure that the string
     * is exactly 8 bits long. This is necessary as the Integer.toBinaryString() does not always
     * return a String with 8 bits. The leading bits are omitted from the Integer function if
     * they are 0's.
     * @param input     A String of bits.
     * @return          A String of exactly 8 bits.
     */
    private static String fillString(String input) {
        for (int i = input.length(); i < 8; i++)
            input = "0" + input;

        return input;
    }

}