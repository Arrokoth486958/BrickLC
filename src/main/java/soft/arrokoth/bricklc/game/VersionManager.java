package soft.arrokoth.bricklc.game;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import soft.arrokoth.bricklc.BrickLauncherCore;
import soft.arrokoth.bricklc.util.FileUtils;
import soft.arrokoth.bricklc.util.NetworkUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class VersionManager
{
    public static List<String> getManifestAvailableVersions(boolean release, boolean snapshots, boolean oldAlpha) throws IOException
    {
        List<String> output = new ArrayList<>();
        JSONArray json = new JSONObject(readJson()).getJSONArray("versions");

        for (Object o : json.toList())
        {
            if (o instanceof HashMap)
            {
                if (release && Objects.equals(((HashMap<?, ?>) o).get("type").toString(), "release"))
                {
                    output.add(((HashMap<?, ?>) o).get("id").toString());
                }
                if (snapshots && Objects.equals(((HashMap<?, ?>) o).get("type").toString(), "snapshot"))
                {
                    output.add(((HashMap<?, ?>) o).get("id").toString());
                }
                if (oldAlpha && Objects.equals(((HashMap<?, ?>) o).get("type").toString(), "old_alpha") || Objects.equals(((HashMap<?, ?>) o).get("type").toString(), "old_beta"))
                {
                    output.add(((HashMap<?, ?>) o).get("id").toString());
                }
            }
        }
        return output;
    }

    public static List<String> getLocalAvailableVersions(File gameDictPath)
    {
        File versionsDict = new File(gameDictPath.getAbsolutePath() + "\\versions");
        List<String> result = new ArrayList<>();
        versionsDict.mkdirs();
        for (File file : Objects.requireNonNull(versionsDict.listFiles()))
        {
            if (file.isDirectory())
            {
                for (File file1 : Objects.requireNonNull(file.listFiles()))
                {
                    if ((file.getName() + ".json").equals(file1.getName()))
                    {
                        result.add(file.getName());
                    }
                }
            }
        }
        return result;
    }

    public static String readVersionJSON(File gameDictPath, String version) throws IOException
    {
        return FileUtils.readFile(new File(gameDictPath.getAbsolutePath() + "\\versions\\" + version + "\\" + version + ".json"));
    }

    public static HashMap getMapByVersion(String version) throws IOException
    {
        JSONArray json = new JSONObject(readJson()).getJSONArray("versions");
        for (Object o : json.toList())
        {
            if (o instanceof HashMap && Objects.equals(((HashMap<?, ?>) o).get("id").toString(), version))
            {
                return (HashMap) o;
            }
        }
        throw new IOException("Could not find version: " + version);
    }

    private static String readJson() throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json").openConnection();
        connection.setDoInput(true);

        StringBuilder result = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null)
        {
            result.append(line);
        }
        return result.toString();
    }

    public static void downloadVersionJson(String version, File gameDictPath)
    {
        try
        {
            System.out.println(new URL(getMapByVersion(version).get("url").toString()).openConnection());
            NetworkUtils.downloadFileWithRedirect(new URL(BrickLauncherCore.replaceUrl(BrickLauncherCore.replaceUrl(getMapByVersion(version).get("url").toString()))), new File(gameDictPath.getAbsolutePath() + "\\versions\\" + version + "\\" + version + ".json"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void downloadLibraries(String version, File gameDictPath, File dataPath) throws IOException
    {
        JSONObject json = new JSONObject(FileUtils.readFile(new File(gameDictPath.getAbsolutePath() + "\\versions\\" + version + "\\" + version + ".json")));
        json.getJSONArray("libraries");
        for (Object o : json.getJSONArray("libraries"))
        {
            if (o instanceof JSONObject)
            {
                try
                {
                    // TODO: Debug
                    System.out.println(BrickLauncherCore.replaceUrl(((JSONObject) o).getJSONObject("downloads").getJSONObject("artifact").getString("url")));
                    NetworkUtils.downloadFileWithRedirect(new URL(BrickLauncherCore.replaceUrl(((JSONObject) o).getJSONObject("downloads").getJSONObject("artifact").getString("url"))), new File(gameDictPath.getAbsolutePath() + "\\libraries\\" + ((JSONObject) o).getJSONObject("downloads").getJSONObject("artifact").getString("path")));
                }
                catch (JSONException ignored)
                {
                }
                try
                {
                    if (System.getProperty("os.name").contains("Windows"))
                    {
                        // TODO: Debug
                        System.out.println(BrickLauncherCore.replaceUrl(((JSONObject) o).getJSONObject("downloads").getJSONObject("classifiers").getJSONObject("natives-windows").getString("url")));
                        NetworkUtils.downloadFileWithRedirect(new URL(BrickLauncherCore.replaceUrl(((JSONObject) o).getJSONObject("downloads").getJSONObject("classifiers").getJSONObject("natives-windows").getString("url"))), new File(dataPath.getAbsolutePath() + "\\cache\\natives.jar"));
                        FileUtils.unzipFile(new File(dataPath.getAbsolutePath() + "\\cache\\natives.jar"), new File(gameDictPath.getAbsolutePath() + "\\versions\\" + version + "\\natives"));
                        try
                        {
                            for (File f : Objects.requireNonNull(new File(gameDictPath.getAbsolutePath() + "\\versions\\" + version + "\\natives").listFiles()))
                            {
                                if (f.isDirectory())
                                {
                                    f.delete();
                                }
                                if (!f.getName().substring(f.getName().lastIndexOf(".") + 1).equals("dll"))
                                {
                                    f.delete();
                                }
                            }
                        }
                        catch (NullPointerException ignored)
                        {
                        }
                    }
                    if (System.getProperty("os.name").contains("Mac"))
                    {
                        System.out.println(BrickLauncherCore.replaceUrl(((JSONObject) o).getJSONObject("downloads").getJSONObject("classifiers").getJSONObject("natives-macos").getString("url")));
                        NetworkUtils.downloadFileWithRedirect(new URL(BrickLauncherCore.replaceUrl(((JSONObject) o).getJSONObject("downloads").getJSONObject("classifiers").getJSONObject("natives-macos").getString("url"))), new File(dataPath.getAbsolutePath() + "\\cache\\natives.jar"));
                        FileUtils.unzipFile(new File(dataPath.getAbsolutePath() + "\\cache\\natives.jar"), new File(gameDictPath.getAbsolutePath() + "\\versions\\" + version + "\\natives"));
                    }
                    if (System.getProperty("os.name").contains("Linux"))
                    {
                        System.out.println(BrickLauncherCore.replaceUrl(((JSONObject) o).getJSONObject("downloads").getJSONObject("classifiers").getJSONObject("natives-linux").getString("url")));
                        NetworkUtils.downloadFileWithRedirect(new URL(BrickLauncherCore.replaceUrl(((JSONObject) o).getJSONObject("downloads").getJSONObject("classifiers").getJSONObject("natives-linux").getString("url"))), new File(dataPath.getAbsolutePath() + "\\cache\\natives.jar"));
                        FileUtils.unzipFile(new File(dataPath.getAbsolutePath() + "\\cache\\natives.jar"), new File(gameDictPath.getAbsolutePath() + "\\versions\\" + version + "\\natives"));
                    }
//                    NetworkUtils.downloadFileWithRedirect(new URL(BrickLauncherCore.replaceUrl(((JSONObject) o).getJSONObject("downloads").getJSONObject("classifiers").getString("url"))), new File(gameDictPath.getAbsolutePath() + "\\libraries\\" + ((JSONObject) o).getJSONObject("downloads").getJSONObject("artifact").getString("path")));
                }
                catch (JSONException ignored)
                {
                }
            }
        }
    }

    public static void downloadMainJar(String version, File gameDictPath) throws IOException
    {
        JSONObject json = new JSONObject(FileUtils.readFile(new File(gameDictPath.getAbsolutePath() + "\\versions\\" + version + "\\" + version + ".json")));
        NetworkUtils.downloadFileWithRedirect(new URL(BrickLauncherCore.replaceUrl(json.getJSONObject("downloads").getJSONObject("client").getString("url"))), new File(gameDictPath.getAbsolutePath() + "\\versions\\" + version + "\\" + version + ".jar"));
    }

    public static String downloadLoggerConfig(String version, File gameDictPath) throws IOException
    {
        JSONObject versionJson = new JSONObject(FileUtils.readFile(new File(gameDictPath.getAbsolutePath() + "\\versions\\" + version + "\\" + version + ".json")));
        File configPath = new File(gameDictPath.getAbsolutePath() + "\\assets\\log_configs\\" + versionJson.getJSONObject("logging").getJSONObject("client").getJSONObject("file").getString("id"));
        if (!configPath.exists())
        {
            NetworkUtils.downloadFileWithRedirect(new URL(versionJson.getJSONObject("logging").getJSONObject("client").getJSONObject("file").getString("url")), configPath);
        }
        return versionJson.getJSONObject("logging").getJSONObject("client").getString("argument").replace("${path}", "\"" + configPath.getAbsolutePath() + "\"");
    }

    public static void downloadAssets(String version, File gameDictPath) throws IOException
    {
        NetworkUtils.downloadFileWithRedirect(new URL(BrickLauncherCore.replaceUrl(new JSONObject(FileUtils.readFile(new File(gameDictPath.getAbsolutePath() + "\\versions\\" + version + "\\" + version + ".json"))).getJSONObject("assetIndex").getString("url"))), new File(gameDictPath.getAbsolutePath() + "\\assets\\indexes\\" + new JSONObject(FileUtils.readFile(new File(gameDictPath.getAbsolutePath() + "\\versions\\" + version + "\\" + version + ".json"))).getString("assets") + ".json"));
        JSONObject versionJson = new JSONObject(FileUtils.readFile(new File(gameDictPath.getAbsolutePath() + "\\versions\\" + version + "\\" + version + ".json")));
        JSONObject assetsJson = new JSONObject(FileUtils.readFile(new File(gameDictPath.getAbsolutePath() + "\\assets\\indexes\\" + versionJson.getString("assets") + ".json")));
//        System.out.println(assetsJson.getJSONObject("objects").names());
        for (Object o : assetsJson.getJSONObject("objects").names())
        {
            if (o instanceof String)
            {
                try
                {
                    String assetToDownload = assetsJson.getJSONObject("objects").getJSONObject((String) o).getString("hash");
                    String path = (String) o;
                    path = path.replace("minecraft/", "");
                    path = path.replace("realms/", "");
                    File file = new File(gameDictPath + "\\assets\\objects\\" + assetToDownload.substring(0, 2) + "\\" + assetToDownload);
                    if (!file.exists())
                    {
                        try
                        {
                            // TODO: Debug
                            System.out.println(new URL("https://resources.download.minecraft.net/" + assetToDownload.substring(0, 2) + "/" + assetToDownload).openConnection());
                            NetworkUtils.downloadFileWithRedirect(new URL(BrickLauncherCore.replaceUrl("https://resources.download.minecraft.net/" + assetToDownload.substring(0, 2) + "/" + assetToDownload)), file);
                            FileUtils.copyFile(file, new File(gameDictPath + "\\assets\\virtual\\legacy\\" + String.valueOf(o).replace("minecraft/", "").replace("realms/", "").replace("/", "\\")));
                        }
                        catch (FileNotFoundException ignored)
                        {
                        }
                    }
                }
                catch (JSONException ignored)
                {
                }
            }
        }
    }
}
