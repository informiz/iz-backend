package informiz.org.chaincode.model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class Utils {

    protected static String createUuid(ContractType contractType) {
        try {
            return InetAddress.getLocalHost().getHostName() + "-" + contractType + "-" + UUID.randomUUID().toString();
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Cannot generate unique id, need access to local host-name");
        }
    }
}
