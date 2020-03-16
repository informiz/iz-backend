package informiz.org.chaincode.model;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIteratorWithMetadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@DataType()
public final class PaginatedResults {
    List<String> results;

    String bookmark;


    public PaginatedResults(QueryResultsIteratorWithMetadata<KeyValue> states) {
        assert states != null;
        results = new ArrayList<>(states.getMetadata().getFetchedRecordsCount());
        Iterator<KeyValue> iterator = states.iterator();
        iterator.forEachRemaining(keyval -> results.add(keyval.getStringValue()));
        bookmark = states.getMetadata().getBookmark();
    }

    public List<String> getResults() {
        return results;
    }

    public void setResults(List<String> results) {
        this.results = results;
    }

    public String getBookmark() {
        return bookmark;
    }

    public void setBookmark(String bookmark) {
        this.bookmark = bookmark;
    }
}
