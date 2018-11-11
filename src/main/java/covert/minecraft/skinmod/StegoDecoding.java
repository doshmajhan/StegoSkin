package covert.minecraft.skinmod;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

class StegoDecoding {

    private static final String DECODE_OUTPUT_TEMPLATE = "../config/%s_decoded.txt";

    /**
     * Retrieves a message from a given input image by collecting the last 2 bits
     * of each Red, Green and Blue component of each pixel starting at the
     * top left of the picture. The retrieval process stops once the end block, which is
     * "111111" is encountered. The message is then saved to a text file.
     *
     * @param playerName    The name of the player we're reading the skin of
     */
    static String retrieveMessageFromImage(String playerName) {
        String imagePath = String.format(StegoHelper.ENCODED_IMAGE_TEMPLATE, playerName);
        BufferedImage image = StegoHelper.loadImage(imagePath);
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
                ArrayList<String> rgba = StegoHelper.getColorBinaries(color);

                String block = retrieveLastBinaryPair(rgba);

                if (block.equals(StegoHelper.END_CHUNK_FLAG)) {
                    break outer;
                } else {
                    retrievedMessage += block;
                }
            }
        }

        retrievedMessage = convertBinaryToAscii(retrievedMessage);

        // Clean up the string, keeping alphanumeric characters and spaces
        retrievedMessage = retrievedMessage.replaceAll("[^A-Za-z0-9\\s]", "");

        // Save message to text file incase they want for later
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
     * Saves a String to a .txt file at the location of this program.
     * @param text              The String to be saved.
     * @param playerName        The name of the player we got the message from
     */
    private static void saveText(String text, String playerName) {
        Path filePath = Paths.get(String.format(DECODE_OUTPUT_TEMPLATE, playerName));

        try (BufferedWriter writer = Files.newBufferedWriter(
                filePath,
                Charset.defaultCharset(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {

            writer.append(text);
            writer.newLine();
            writer.flush();

            System.out.println("Saved output to: " + filePath.toAbsolutePath());

        } catch (FileNotFoundException e) {
            System.err.println("Could not create output text. Check program writing permissions.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Could not write output to file.");
            e.printStackTrace();
        }
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
