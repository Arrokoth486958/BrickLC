package soft.arrokoth.bricklc.game;

import org.json.JSONException;
import org.json.JSONObject;
import soft.arrokoth.bricklc.auth.account.GameAccount;
import soft.arrokoth.bricklc.util.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameLauncher
{
    public static String generateJVMArguments(File gameDict, String version, int xmn, int xmx, String loggingConfig, String launcherName, String launcherVersion, boolean gameDictSeparate)
    {
        String gameDictPath;
        if (gameDictSeparate)
        {
            gameDictPath = gameDict.getAbsolutePath() + "\\versions\\" + version;
        }
        else
        {
            gameDictPath = gameDict.getAbsolutePath();
        }

        StringBuilder result = new StringBuilder();
        result.append("-XX:+UseG1GC ");
        result.append("-XX:-UseAdaptiveSizePolicy ");
        result.append("-XX:-OmitStackTraceInFastThrow ");
        result.append("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump ");

        if (System.getProperty("os.name").contains("Windows"))
        {
            result.append("-Dos.name=\"Windows 10\" -Dos.version=10.0 ");
        }
        if (System.getProperty("os.name").contains("Mac"))
        {
            result.append("-XstartOnFirstThread ");
        }
        if (System.getProperty("os.arch").contains("86"))
        {
            result.append("-Xss1M ");
        }

        result.append("-Dminecraft.launcher.brand=" + launcherName + " -Dminecraft.launcher.version=" + launcherVersion + " ");
        result.append("-Djava.library.path=\"" + gameDictPath + "\\natives\" ");
        result.append(loggingConfig);

        return result.toString();
    }

    private static String generateClassPath(File gameDictPath, String version) throws IOException
    {
        JSONObject jsonFile = new JSONObject(FileUtils.readFile(new File(gameDictPath.getAbsolutePath() + "\\versions\\" + version + "\\" + version + ".json")));
        List<String> names = new ArrayList<>();
//        List<String> versions = new ArrayList<>();
        List<String> paths = new ArrayList<>();
        for (Object o : jsonFile.getJSONArray("libraries"))
        {
            if (o instanceof JSONObject)
            {
                String name = ((JSONObject) o).getString("name").substring(0, ((JSONObject) o).getString("name").lastIndexOf(":"));
//                String version1 = ((JSONObject) o).getString("name").substring(((JSONObject) o).getString("name").lastIndexOf(":") + 1);
                try
                {
                    String path = ((JSONObject) o).getJSONObject("downloads").getJSONObject("artifact").getString("path");
                    int index = 0;
                    boolean exists = false;
                    for (String s : names)
                    {
                        if (Objects.equals(s, name))
                        {
                            index = names.indexOf(s);
                            exists = true;
                        }
                    }
                    if (exists)
                    {
                        names.remove(index);
//                    versions.remove(index);
                        paths.remove(index);
                    }
                    names.add(name);
//                versions.add(version1);
                    paths.add(path);
                }
                catch (JSONException ignored)
                {
                }
            }
        }
        StringBuilder result = new StringBuilder();
        for (String s : paths)
        {
            result.append(gameDictPath.getAbsolutePath()).append("\\libraries\\").append(s.replace("/", "\\")).append(";");
        }
        return result.toString();
    }

    private static String generateGameArguments(File gameDictPath, String version) throws IOException
    {
        JSONObject jsonFile = new JSONObject(FileUtils.readFile(new File(gameDictPath.getAbsolutePath() + "\\versions\\" + version + "\\" + version + ".json")));
        String result = "";
        try
        {
            StringBuilder result1 = new StringBuilder();
            for (Object o : jsonFile.getJSONObject("arguments").getJSONArray("game"))
            {
                if (o instanceof String)
                {
                    result1.append(o).append(" ");
                }
            }
            result = result1.toString();
            result = result.substring(0, result.length() - 1);
        }
        catch (JSONException e)
        {
            result = jsonFile.getString("minecraftArguments");
        }
        return result;
    }

    private static String getAssetsIndex(File gameDictPath, String version) throws IOException
    {
        JSONObject jsonFile = new JSONObject(FileUtils.readFile(new File(gameDictPath.getAbsolutePath() + "\\versions\\" + version + "\\" + version + ".json")));
        return jsonFile.getString("assets");
    }

    private static String getMainClass(File gameDictPath, String version) throws IOException
    {
        JSONObject jsonFile = new JSONObject(FileUtils.readFile(new File(gameDictPath.getAbsolutePath() + "\\versions\\" + version + "\\" + version + ".json")));
        return jsonFile.getString("mainClass");
    }

    public static void generateLaunchBatch(File gameDict, File exportDict, String javaBinPath, String jvmArgs, String version, GameAccount account, boolean gameDictSeparate) throws IOException
    {
        File batchFile = new File(exportDict.getAbsolutePath() + "\\launch.bat");
        batchFile.getParentFile().mkdirs();
        if (batchFile.exists())
        {
            batchFile.delete();
        }
        batchFile.createNewFile();

        String gameDictPath;
        if (gameDictSeparate)
        {
            gameDictPath = gameDict.getAbsolutePath() + "\\versions\\" + version;
        }
        else
        {
            gameDictPath = gameDict.getAbsolutePath();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(batchFile));
        writer.write("@echo off\n");
        writer.write("set APPDATA=" + gameDictPath + "\n");
        writer.write("cd " + gameDictPath + "\n");

        writer.write("\"" + javaBinPath + "\\java.exe\" " + jvmArgs + " ");
        writer.write("-cp ");
        writer.write("\"" + generateClassPath(gameDict, version) + gameDict.getAbsolutePath() + "\\versions\\" + version + "\\" + version + ".jar\"");

        writer.write(" ");
        writer.write(getMainClass(gameDict, version));

        String gameArguments = generateGameArguments(gameDict, version);
        gameArguments = gameArguments.replace("${auth_player_name}", "\"" + account.getUsername() + "\"");
        gameArguments = gameArguments.replace("${version_name}", "\"" + version + "\"");
        gameArguments = gameArguments.replace("${game_directory}", gameDictPath);
        gameArguments = gameArguments.replace("${assets_root}", "\"" + gameDict.getAbsolutePath() + "\\assets\"");
        gameArguments = gameArguments.replace("${game_assets}", "\"" + gameDict.getAbsolutePath() + "\\assets\"");
        gameArguments = gameArguments.replace("${assets_index_name}", "\"" + getAssetsIndex(gameDict, version) + "\"");
        gameArguments = gameArguments.replace("${auth_uuid}", "\"" + account.getUuid() + "\"");
        gameArguments = gameArguments.replace("${auth_access_token}", "\"" + account.getAccessToken() + "\"");
        gameArguments = gameArguments.replace("${user_type}", "\"" + account.getUserType() + "\"");
        gameArguments = gameArguments.replace("${version_type}", "\"Minecraft - BrickLC\"");

        gameArguments = gameArguments.replace("${user_properties}", "{}");

        writer.write(" ");
        writer.write(gameArguments);

        writer.flush();
        writer.close();
    }

    public static void launchBatch(File settingsDict) throws InterruptedException, IOException
    {

        String[] cmd = new String[]{"cmd.exe", "/C", "launch.bat"};
        Process process = Runtime.getRuntime().exec(cmd, null, settingsDict);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
        {
            result.append(line).append("\n");
        }
        reader.close();
        System.out.println("Exit with exit code " + process.exitValue());
    }
}
