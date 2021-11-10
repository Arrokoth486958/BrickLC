package soft.arrokoth.bricklc.auth.account;

import java.util.UUID;

public class OfflineAccount extends GameAccount
{
    public OfflineAccount(String username)
    {
        this.username = username;
        this.uuid = "";
        this.accessToken = "";
        this.email = "";
        this.userType = "offline";
        this.clientToken = UUID.randomUUID().toString();
    }
}
