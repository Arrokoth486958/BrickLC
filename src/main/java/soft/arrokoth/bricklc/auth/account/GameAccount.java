package soft.arrokoth.bricklc.auth.account;

public abstract class GameAccount
{
    protected String uuid;
    protected String username;
    protected String accessToken;
    protected String email;
    protected String clientToken;

    protected String userType;

    public GameAccount()
    {
    }

    public String getAccessToken()
    {
        return accessToken;
    }

    public String getEmail()
    {
        return email;
    }

    public String getUuid()
    {
        return uuid;
    }

    public String getUsername()
    {
        return username;
    }

    public String getUserType()
    {
        return userType;
    }
}
