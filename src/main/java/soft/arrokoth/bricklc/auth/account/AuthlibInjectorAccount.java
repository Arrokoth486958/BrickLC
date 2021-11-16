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

// TODO: WIP!
public class AuthlibInjectorAccount extends GameAccount
{
    private AuthlibInjectorAccount(String username, String uuid, String accessToken, String email, String clientToken)
    {
        this.username = username;
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.email = email;
        this.clientToken = clientToken;
        this.userType = "Mojang";
    }

    public String getAccessToken()
    {
        return accessToken;
    }

    public String getEmail()
    {
        return email;
    }

    public static AuthlibInjectorAccount authenticate(String username, String password, String url) throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
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
            System.out.println(resultJson);
            return new AuthlibInjectorAccount("", "", resultJson.getString("signaturePublickey").replace("-----BEGIN PUBLIC KEY-----\\n", "").replace("\\n-----END PUBLIC KEY-----\\n", "").replace("-----END PUBLIC KEY-----", "").replace("-----BEGIN PUBLIC KEY-----", ""), username, clientToken);
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
