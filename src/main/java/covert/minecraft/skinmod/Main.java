package com.company;


public class Main {

    private static final String IMAGE = "orig_skin.png";
    private static final String MESSAGE_FILE = "message.txt";
    private static final String ENCODED_IMAGE = "encoded.png";

    public static void main(String[] args) {
        System.out.println("Encoding");
        LSBv2.storeMessage(IMAGE, MESSAGE_FILE);
        System.out.println("Done");

        System.out.println("Decoding");
        LSBv2.retrieveMessageFromImage(ENCODED_IMAGE);
        System.out.println("Done");
    }

}
