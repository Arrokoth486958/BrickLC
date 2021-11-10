package soft.arrokoth.bricklc.util;

import java.util.ArrayList;
import java.util.List;

public class JavaUtils
{
    public static List<String> getAvilableJava()
    {
        List<String> result = new ArrayList<>();
        String input = System.getProperty("java.library.path");
        System.out.println(input);
        for (String s : input.split(";"))
        {
            System.out.println(s);
        }
        return result;
    }
}
