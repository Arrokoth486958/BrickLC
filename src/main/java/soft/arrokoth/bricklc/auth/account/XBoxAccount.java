package soft.arrokoth.bricklc.auth.account;

import java.util.UUID;

// TODO: WIP!
public class XBoxAccount extends GameAccount
{
    private XBoxAccount(String username, String uuid, String accessToken)
    {
        this.username = username;
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.email = "";
        this.clientToken = UUID.randomUUID().toString();
        this.userType = "xbox";
    }

    private static XBoxAccount authenticate()
    {
        return null;
    }
}
