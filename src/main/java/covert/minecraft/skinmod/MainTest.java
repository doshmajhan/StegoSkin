package covert.minecraft.skinmod;

public class MainTest {
    private static final String MESSAGE = "hey there friend";

    public static void main(String args[]) {

        //System.out.println("Storing message");
        //String message = StegoHelper.readFile(MESSAGE_PATH);
        //StegoEncoding.storeMessage(message);
        //System.out.println("Done");

        System.out.println("Decoding message");
        String message = StegoDecoding.retrieveMessageFromImage("greengiraffe1");
        System.out.printf("Message recieved: %s %n", message);
        System.out.println("Done");

        //System.out.println("Updating skin");
        //UpdateSkinCommand.updateSkin();
        //System.out.println("Done");

    }
}
