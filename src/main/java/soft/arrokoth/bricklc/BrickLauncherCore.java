package soft.arrokoth.bricklc;

public class BrickLauncherCore
{
    public static final String brickLCName = "brick-launcher-core";
    public static final String brickLCVersion = "a0.1.0-experimental";

    public static String metaUrl = "https://launchermeta.mojang.com";
    public static String assetsUrl = "https://resources.download.minecraft.net";
    public static String librariesUrl = "https://libraries.minecraft.net";

//    Example: BMCLAPI
//    public static String metaUrl = "https://bmclapi2.bangbang93.com";
//    public static String assetsUrl = "https://bmclapi2.bangbang93.com/assets";
//    public static String librariesUrl = "https://bmclapi2.bangbang93.com/maven";

    public static String replaceUrl(String input)
    {
        String result = input;
        result = result.replace("https://launchermeta.mojang.com", metaUrl);
        result = result.replace("http://resources.download.minecraft.net", assetsUrl);
        result = result.replace("https://resources.download.minecraft.net", assetsUrl);
        result = result.replace("https://libraries.minecraft.net", librariesUrl);
        return result;
    }
}
