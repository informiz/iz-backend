package informiz.org.chaincode.model;

import org.apache.commons.lang3.LocaleUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.UUID;

public class Utils {

    protected static String createUuid(ContractType contractType) {
        try {
            // TODO: consider creating a complex key for couchDB key-based queries
            return contractType + "-" + UUID.randomUUID().toString() + "-" + InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Cannot generate unique id, need access to local host-name");
        }
    }

    public static Score createScore(String reliability, String confidence) {
        float r, c;
        try {
            r = Float.valueOf(reliability);
            c = Float.valueOf(confidence);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Reliability and confidence must be numbers in the range [0.0-1.0]");
        }

        return new Score(r, c);
    }

    public static int pageSizeFromString(String pageSize) {
        int size = 0;
        try {
            size = Integer.valueOf(pageSize);
            size = (size < 0) ? 10 : (size > 100) ? 100 : size;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Page-size must be a positive integer");
        }
        return size;
    }

    public static Locale localeFromString(String localeStr) {
        return LocaleUtils.toLocale(localeStr);
    }

    public static float reliabilityFromString(String reliability) {
        try {
            return Float.valueOf(reliability);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Reliability must be a number in the range [0.0-1.0]");
        }
    }
}
