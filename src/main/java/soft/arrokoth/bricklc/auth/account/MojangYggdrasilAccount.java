package soft.arrokoth.bricklc.auth.account;

import org.json.JSONObject;
import soft.arrokoth.bricklc.auth.AuthenticateException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class MojangYggdrasilAccount extends GameAccount
{
    private MojangYggdrasilAccount(String username, String uuid, String accessToken, String email, String clientToken)
    {
        this.username = username;
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.email = email;
        this.clientToken = clientToken;
        this.userType = "mojang";
    }

    public boolean isAccessTokenAvailable() throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) new URL("https://authserver.mojang.com/validate").openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        JSONObject data = new JSONObject();
        data.put("accessToken", this.accessToken);
        data.put("clientToken", this.clientToken);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(data.toString().getBytes());
        outputStream.flush();
        outputStream.close();

        return connection.getResponseCode() == 204;
    }

    public void refreshAccessToken() throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) new URL("https://authserver.mojang.com/refresh").openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        JSONObject data = new JSONObject();
        data.put("accessToken", this.accessToken);
        data.put("clientToken", this.clientToken);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(data.toString().getBytes());
        outputStream.flush();
        outputStream.close();

        if (connection.getResponseCode() == 200)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
            {
                result.append(line).append("\n");
            }
            reader.close();
            JSONObject resultJson = new JSONObject(result.toString());
            this.accessToken = resultJson.getString("accessToken");
            this.clientToken = resultJson.getString("clientToken");
            this.uuid = resultJson.getJSONObject("selectedProfile").getString("id");
            this.username = resultJson.getJSONObject("selectedProfile").getString("name");
        }
        else
        {
            String str = "";
            BufferedReader reader1 = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            String str1;

            while ((str1 = reader1.readLine()) != null)
            {
                str += str1;
            }
            throw new AuthenticateException(new JSONObject(str).getString("errorMessage"));
        }
    }

    public String getAccessToken()
    {
        return accessToken;
    }

    public String getEmail()
    {
        return email;
    }

    public static MojangYggdrasilAccount authenticate(String username, String password) throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) new URL("https://authserver.mojang.com/authenticate").openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        String clientToken = UUID.randomUUID().toString();
        JSONObject data = new JSONObject();
        data.put("agent", new JSONObject().put("name", "Minecraft").put("version", 1));
        data.put("username", username);
        data.put("password", password);
        data.put("clientToken", clientToken);
        data.put("requestUser", true);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(data.toString().getBytes());
        outputStream.flush();
        outputStream.close();

        if (connection.getResponseCode() == 200)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
            {
                result.append(line).append("\n");
            }
            reader.close();
            JSONObject resultJson = new JSONObject(result.toString());
            return new MojangYggdrasilAccount(resultJson.getJSONObject("selectedProfile").getString("name"), resultJson.getJSONObject("selectedProfile").getString("id"), resultJson.getString("accessToken"), username, clientToken);
        }
        else
        {
            String str = "";
            BufferedReader reader1 = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            String str1;

            while ((str1 = reader1.readLine()) != null)
            {
                str += str1;
            }
            throw new AuthenticateException(new JSONObject(str).getString("errorMessage"));
        }
    }
}
