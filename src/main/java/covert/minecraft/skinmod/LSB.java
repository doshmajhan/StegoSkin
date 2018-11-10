package covert.minecraft.skinmod;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class LSB {

    public static final String ENCODED_IMAGE = "../%s_encoded.png";
    private static final String SKIN_PATH = "../orig_skin.png";
    public static final String MY_ENCODED_SKIN = "../my_skin_encoded.png";
    private static final String DECODE_OUTPUT = "../%s_decoded.txt";
    private static final String MESSAGE_PATH = "../message.txt";
    private static final int MAX_MESSAGE_SIZE = 350;
    private static final String END_CHUNK_FLAG = "111111";
    public static final String TEST_USER = "my_skin";

    public static void storeMessage(String message) {
        if (message.length() > MAX_MESSAGE_SIZE) {
            System.out.println("Message is too big");
            return;
        }
        BufferedImage image = loadImage(SKIN_PATH);
        addMessageToImage(message, image);
    }

    /**
     * Loads an image from the given input path.
     * It is important that the BufferedImage received from the ImageIO.read() function
     * has a ColorModel which does not support the saving of alpha channels. Therefore
     * we have to create a new BufferedImage with a different ColorModel that supports
     * changing the alpha channel.
     * @param input     path to image
     * @return          BufferedImage of image
     */
    private static BufferedImage loadImage(String input) {
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
     * Reads the input text from the file and saves it as a String.
     * @param input     the path to the input text file
     * @return          a String of the text of the input text file
     */
    public static String readFile(String input) {
        try {
            FileReader fileReader = new FileReader(input);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            String out = "";
            while((line = bufferedReader.readLine()) != null)
                out += line + " ";

            return out;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves a message from a given input image by collecting the last 2 bits
     * of each Red, Green and Blue component of each pixel starting at the
     * top left of the picture. The retrieval process stops once the end block, which is
     * "111111" is encountered. The message is then saved to a text file.
     *
     * @param playerName    The name of the player we're reading the skin of
     */
    public static String retrieveMessageFromImage(String playerName) {
        String imagePath = String.format(ENCODED_IMAGE, playerName);
        File file = new File(imagePath);
        BufferedImage image;

        try {
            image = ImageIO.read(file);
        }
        catch (IOException ex){
            System.out.println(ex.getMessage());
            return "";
        }

        String retrievedMessage = "";

        outer:
        for (int row = 0; row < image.getHeight(); row++) {
            for (int column = 0; column < image.getWidth(); column++) {
                int rgb = image.getRGB(column, row);
                Color color = new Color(rgb, true);

                // Nothing was written to this pixel cuz it had a 0 for alpha so skip
                if (color.getAlpha() == 0) {
                    continue ;
                }
                ArrayList<String> rgba = getColorBinaries(color);

                String block = retrieveLastBinaryPair(rgba);

                if (block.equals(END_CHUNK_FLAG)) {
                    break outer;
                } else {
                    retrievedMessage += block;
                }
            }
        }

        retrievedMessage = convertBinaryToAscii(retrievedMessage);
        saveText(retrievedMessage, playerName);
        return retrievedMessage;
    }

    /**
     * Retrieves the last 2 bits of the Red, Green, and Blue channel of a
     * given ArrayList containing the values for these channels as a binary String.
     * @param rgba      The binary strings of the Red, Green, and Blue channels of a pixel
     * @return          A concatenation of all last 2 bits of the binary strings.
     */
    private static String retrieveLastBinaryPair(ArrayList<String> rgba) {
        String out = "";

        for(int i = 0; i < rgba.size() - 1; i++){
            out += rgba.get(i).substring(6);
        }

        return out;
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

                ArrayList<String> rgbaOld = getColorBinaries(oldColor);
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


    private static void readImage(BufferedImage image) {

        int count = 0;
        for (int row = 0; row < image.getHeight(); row++) {
            for (int column = 0; column < image.getWidth(); column++) {


                int rgb = image.getRGB(column, row);
                Color oldColor = new Color(rgb, true);
                if (oldColor.getAlpha() == 0 ){
                    continue;
                }
                count += 1;
            }
        }
        System.out.println(count/6);
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
     * Converts the Integer values of the color components to Binary values as Strings.
     * @param color     The color which color components are wanted.
     * @return          An ArrayList of Strings containing the color component values as binaries.
     */
    private static ArrayList<String> getColorBinaries(Color color) {
        ArrayList<String> out = new ArrayList<>();
        out.add(intToBinaryString(color.getRed()));
        out.add(intToBinaryString(color.getGreen()));
        out.add(intToBinaryString(color.getBlue()));
        out.add(intToBinaryString(color.getAlpha()));

        return out;
    }

    /**
     * Saves a BufferedImage to a .png file at the location of this program.
     * @param image     The BufferedImage to be saved.
     */
    private static void saveImage(BufferedImage image) {
        try {
            File output = new File(MY_ENCODED_SKIN);
            ImageIO.write(image, "png", output);
            System.out.println("Saved output to: " + output.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Could not write the output to .png file.");
            e.printStackTrace();
        }
    }

    /**
     * Saves a String to a .txt file at the location of this program.
     * @param text              The String to be saved.
     * @param playerName        The name of the player we got the message from
     */
    private static void saveText(String text, String playerName) {
        try {
            String filePath = String.format(DECODE_OUTPUT, playerName);
            File output = new File(filePath);
            FileOutputStream fos = new FileOutputStream(output);

            byte[] contentInBytes = text.getBytes();

            fos.write(contentInBytes);
            fos.flush();
            fos.close();

            System.out.println("Saved output to: " + output.getAbsolutePath());

        } catch (FileNotFoundException e) {
            System.err.println("Could not create output text. Check program writing permissions.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Could not write output to file.");
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
            String temp = Integer.toBinaryString(c);
            binary += fillString(temp);
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
        chunks.add(END_CHUNK_FLAG);
        return chunks;
    }

    /**
     * Converts an Integer to a Binary String.
     * @param input     The Integer to be converted.
     * @return          A String containing the binary value of the input Integer.
     */
    private static String intToBinaryString(int input) {
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

    /**
     * Takes in a binary string and converts it to an ascii string
     * @param binary    string of binary digits
     * @return          A ascii string
     */
    private static String convertBinaryToAscii(String binary){
        String ascii = "";
        int index = 0;

        while (index < binary.length()) {
            String bite = binary.substring(index, Math.min(index + 8, binary.length()));
            int charCode = Integer.parseInt(bite, 2);
            String character = Character.toString((char)charCode);
            ascii = ascii.concat(character);
            index += 8;
        }
        return ascii;
    }
}