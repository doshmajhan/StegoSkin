package covert.minecraft.skinmod;

public class MainTest {
    public static void main(String args[]) {

        System.out.println("Storing message");
        LSB.storeMessage();
        System.out.println("Done");

        System.out.println("Decoding message");
        LSB.retrieveMessageFromImage();
        System.out.println("Done");

    }
}
