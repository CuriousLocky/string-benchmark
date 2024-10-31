package utils;

import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;

public class Utils {
    public static String makeRandomString(int stringSize, Random rand) {
        char[] chars = new char[stringSize];
        for (int i = 0; i < stringSize; i++) {
            chars[i] = (char) ((32 + rand.nextInt()) % 127);
        }
        return new String(chars);
    }
}
