package informiz.org.chaincode;

import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIteratorWithMetadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TestUtils {
    public static final class MockKeyValue implements KeyValue {

        private final String key;
        private final String value;

        MockKeyValue(final String key, final String value) {
            super();
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getStringValue() {
            return this.value;
        }

        @Override
        public byte[] getValue() {
            return this.value.getBytes();
        }

    }

    // TODO: is this usable for mocking?
    public static final class MockResultsIterator implements QueryResultsIteratorWithMetadata<KeyValue> {

        private final List<KeyValue> resultsList;

        MockResultsIterator(Map<String, String> keyValMap) {
            super();

            resultsList = new ArrayList<KeyValue>();
            for (Map.Entry<String, String> keyval: keyValMap.entrySet()) {
                resultsList.add(new MockKeyValue(keyval.getKey(), keyval.getValue()));
            }
        }

        @Override
        public Iterator<KeyValue> iterator() {
            return resultsList.iterator();
        }

        @Override
        public void close() throws Exception {
            // do nothing
        }

        @Override
        public ChaincodeShim.QueryResponseMetadata getMetadata() {
            return null;
        }
    }
}
