package soft.arrokoth.bricklc.auth;

public class AuthenticateException extends RuntimeException
{
    private static final long serialVersionUID = 0;

    public AuthenticateException(final String message)
    {
        super(message);
    }

    public AuthenticateException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public AuthenticateException(final Throwable cause)
    {
        super(cause.getMessage(), cause);
    }
}
