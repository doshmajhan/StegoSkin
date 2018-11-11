package covert.minecraft.skinmod;

public class MainTest {
    private static final String MESSAGE = "hey there friend";
    private static final String TEST_USER = "Doshmajhan";

    public static void main(String args[]) {

        System.out.println("Storing message");
        StegoSkin.skinPath = String.format(StegoSkin.skinPath, TEST_USER);
        StegoSkin.encodedSkinPath = String.format(StegoSkin.encodedSkinPath, TEST_USER);
        StegoEncoding.storeMessage(MESSAGE);
        System.out.println("Done");

        System.out.println("Decoding message");
        String message = StegoDecoding.retrieveMessageFromImage(TEST_USER);
        System.out.printf("Message recieved: %s %n", message);
        System.out.println("Done");

        //System.out.println("Updating skin");
        //UpdateSkinCommand.updateSkin();
        //System.out.println("Done");

    }
}
