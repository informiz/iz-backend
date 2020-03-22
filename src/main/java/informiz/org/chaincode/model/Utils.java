package informiz.org.chaincode.model;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
}
