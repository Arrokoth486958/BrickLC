package soft.arrokoth.bricklc.auth.account;

import org.json.JSONArray;
import org.json.JSONObject;
import soft.arrokoth.bricklc.auth.AuthenticateException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;

public class MicrosoftAccount extends GameAccount
{
    public static final String microsoftUrl = "https://login.live.com/oauth20_authorize.srf?client_id=00000000402b5328&response_type=code&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf";
    public static final String tokenUrl = "https://login.live.com/oauth20_token.srf";
    public static final String xblUrl = "https://user.auth.xboxlive.com/user/authenticate";
    public static final String xstsUrl = "https://xsts.auth.xboxlive.com/xsts/authorize";
    public static final String minecraftToken = "https://api.minecraftservices.com/authentication/login_with_xbox";
    public static final String minecraftOwnshipUrl = "https://api.minecraftservices.com/entitlements/mcstore";
    public static final String minecraftProfilUrl = "https://api.minecraftservices.com/minecraft/profile";

    protected String refreshToken;

    private MicrosoftAccount(String username, String uuid, String accessToken)
    {
        this.username = username;
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.email = "";
        this.clientToken = UUID.randomUUID().toString();
        this.userType = "microsoft";
    }

    public static String detachCode(String url)
    {
        return url.substring(url.indexOf("code=") + "code=".length(), url.lastIndexOf("&"));
    }

    public static MicrosoftAccount authenticate(String code) throws IOException
    {
        String username;
        String uuid;
        String accessToken;

        String authCode = new JSONObject(getAuthToken(code)).getString("access_token");
        String xblToken = new JSONObject(xblAuth(authCode)).getString("Token");
        JSONObject xstsResult = new JSONObject(xstsAuth(xblToken));
        String xstsUhs = xstsResult.getJSONObject("DisplayClaims").getJSONArray("xui").getString(0);
        String xstsToken = xstsResult.getString("Token");
        accessToken = new JSONObject(minecraftToken(xstsUhs, xstsToken)).getString("access_token");
        if (minecraftOwnship(accessToken))
        {
            JSONObject profile = new JSONObject(getMinecraftProfil(accessToken));
            uuid = profile.getString("id");
            username = profile.getString("name");

            return new MicrosoftAccount(username, uuid, accessToken);
        }
        throw new AuthenticateException("Errors with authenticate!");
    }

    private static String getAuthToken(String code) throws IOException
    {
        String urlString = tokenUrl + "?client_id=00000000402b5328&code=" + code + "&grant_type=authorization_code&redirect_uri=https://login.live.com/oauth20_desktop.srf&scope=service::user.auth.xboxlive.com::MBI_SSL";
        HttpURLConnection connection;
        connection = (HttpURLConnection) new URL(urlString).openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        if (connection.getResponseCode() == 200)
        {
            return inputStreamReader(connection.getInputStream());
        }
        throw new AuthenticateException("Errors with getting authenticate token");
    }

    private static String xblAuth(String token) throws IOException
    {
        JSONObject properties = new JSONObject();
        properties.put("AuthMethod", "RPS");
        properties.put("SiteName", "user.auth.xboxlive.com");
        properties.put("RpsTicket", token);
        JSONObject data = new JSONObject();
        data.put("Properties", properties);
        data.put("RelyingParty", "http://auth.xboxlive.com");
        data.put("TokenType", "JWT");

        URL url = new URL(xblUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setDoInput(true);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(data.toString().getBytes());
        outputStream.flush();

        if (connection.getResponseCode() == 200)
        {
            return inputStreamReader(connection.getInputStream());
        }
        throw new AuthenticateException("Errors with xbl authenticate");
    }

    private static String xstsAuth(String token) throws IOException
    {
        JSONObject data = new JSONObject();
        JSONObject properties = new JSONObject();
        properties.put("SandboxId", "RETAIL");
        properties.put("UserTokens", new JSONArray().put(0, token));
        data.put("Properties", properties);
        data.put("RelyingParty", "rp://api.minecraftservices.com/");
        data.put("TokenType", "JWT");

        URL url = new URL(xstsUrl);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setDoInput(true);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(data.toString().getBytes());
        outputStream.flush();

        if (connection.getResponseCode() == 200)
        {
            return inputStreamReader(connection.getInputStream());
        }
        throw new AuthenticateException("Errors with xsts authenticate");
    }

    public static String minecraftToken(String uhs, String xsts_token) throws IOException
    {
        JSONObject data = new JSONObject();
        data.put("identityToken", "XBL3.0 x=" + uhs + ";" + xsts_token);
        URL url = new URL(minecraftToken);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setDoInput(true);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(data.toString().getBytes());
        outputStream.flush();

        if (connection.getResponseCode() == 200)
        {
            return inputStreamReader(connection.getInputStream());
        }
        throw new AuthenticateException("Errors with getting Minecraft access token!");
    }

    public static boolean minecraftOwnship(String token) throws IOException
    {
        URL url = new URL(minecraftOwnshipUrl);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setDoInput(true);

        if (connection.getResponseCode() == 200)
        {
            boolean productMC = false;
            boolean gameMC = false;
            JSONObject json = new JSONObject(inputStreamReader(connection.getInputStream()));
            for (Object o : json.getJSONArray("items"))
            {
                if (o instanceof JSONObject)
                {
                    productMC = Objects.equals(((JSONObject) o).getString("name"), "product_minecraft");
                    gameMC = Objects.equals(((JSONObject) o).getString("name"), "game_minecraft");
                }
            }
            if (productMC && gameMC)
            {
                return true;
            }
            throw new AuthenticateException("The user don't have Minecraft!");
        }
        throw new AuthenticateException("Errors with checking Minecraft ownship!");
    }

    public static String getMinecraftProfil(String token) throws IOException
    {
        URL url = new URL(minecraftProfilUrl);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setDoInput(true);

        if (connection.getResponseCode() == 200)
        {
            return inputStreamReader(connection.getInputStream());
        }
        throw new AuthenticateException("Could not get user's Profile!");
    }

    private static String inputStreamReader(InputStream stream) throws IOException
    {
        String str = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String str1;

        while ((str1 = reader.readLine()) != null)
        {
            str += str1;
        }

        return str;
    }
}
