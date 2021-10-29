package soft.arrokoth.bricklc.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class VersionsManager
{
    private String readJson() throws IOException
    {
        HttpsURLConnection connection = (HttpsURLConnection) new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json").openConnection();
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

    public List<String> getManifestAvailableVersions(boolean release, boolean snapshots, boolean oldAlpha) throws IOException
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

    public List<String> getLocalAvailableVersions(String gameDictPath)
    {
        File versionsDict = new File(gameDictPath + "/versions");
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

    public HashMap getMapByVersion(String version) throws IOException
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

    public void downloadVersionJson(String version, String gameDictPath)
    {
        try
        {
            Downloader.downloadFile(new URL(this.getMapByVersion(version).get("url").toString()), new File(gameDictPath + "/versions/" + version + "/" + version + ".json"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
