package software.momento.kotlin.sdk.responses.cache.signing

import java.util.Date

/** Metadata about a signing key. */
public class SigningKey(
    public val keyId: String,
    public val expiresAt: Date,
    public val endpoint: String
)
