package io.lombardio.identityaccess.auth.application;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;

public class TotpCodeService {

    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private final Clock clock;
    private final SecureRandom secureRandom;

    public TotpCodeService(Clock clock) {
        this(clock, new SecureRandom());
    }

    TotpCodeService(Clock clock, SecureRandom secureRandom) {
        this.clock = clock;
        this.secureRandom = secureRandom;
    }

    public String generateSecret() {
        byte[] bytes = new byte[20];
        secureRandom.nextBytes(bytes);
        return base32Encode(bytes);
    }

    public boolean verifyCode(String secret, String code) {
        if (code == null || !code.matches("\\d{6}")) {
            return false;
        }

        long currentWindow = clock.instant().getEpochSecond() / 30;
        byte[] secretBytes = base32Decode(secret);

        for (long offset = -1; offset <= 1; offset++) {
            if (generateCode(secretBytes, currentWindow + offset).equals(code)) {
                return true;
            }
        }

        return false;
    }

    public String currentCode(String secret) {
        return generateCode(base32Decode(secret), clock.instant().getEpochSecond() / 30);
    }

    private String generateCode(byte[] secret, long counter) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret, "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(counter).array());
            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);
            return String.format("%06d", binary % 1_000_000);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to generate TOTP code", exception);
        }
    }

    private String base32Encode(byte[] data) {
        StringBuilder builder = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;

        for (byte value : data) {
            buffer = (buffer << 8) | (value & 0xff);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                builder.append(BASE32_ALPHABET.charAt((buffer >> (bitsLeft - 5)) & 0x1f));
                bitsLeft -= 5;
            }
        }

        if (bitsLeft > 0) {
            builder.append(BASE32_ALPHABET.charAt((buffer << (5 - bitsLeft)) & 0x1f));
        }

        return builder.toString();
    }

    private byte[] base32Decode(String secret) {
        String normalized = secret.replace("=", "").replace(" ", "").toUpperCase();
        ByteBuffer buffer = ByteBuffer.allocate(normalized.length() * 5 / 8 + 1);
        int value = 0;
        int bits = 0;

        for (char character : normalized.toCharArray()) {
            int index = BASE32_ALPHABET.indexOf(character);
            if (index < 0) {
                throw new IllegalArgumentException("Invalid base32 secret");
            }
            value = (value << 5) | index;
            bits += 5;
            if (bits >= 8) {
                buffer.put((byte) ((value >> (bits - 8)) & 0xff));
                bits -= 8;
            }
        }

        byte[] decoded = new byte[buffer.position()];
        buffer.flip();
        buffer.get(decoded);
        return decoded;
    }
}
