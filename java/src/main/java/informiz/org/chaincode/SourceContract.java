package informiz.org.chaincode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import informiz.org.chaincode.model.PaginatedResults;
import informiz.org.chaincode.model.Score;
import informiz.org.chaincode.model.Source;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIteratorWithMetadata;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Java implementation of the Source Contract. The record contains:
 * - The record's key in the ledger, as the source's id
 * - The current reliability/confidence score of the source
 * - The source's name
 * - Reviews by fact-checkers
 * It is expected that any additional metadata about the source will be stored on an external CMS (e.g SQL database).
 * The sid should be used as a reference key.
 */
@Default
@Contract(
        name = "SourceContract",
        info = @Info(
                title = "Source contract",
                description = "A contract for creating and managing sources for references (e.g the NASA website)",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "support@informiz.org",
                        name = "Informiz Support Team",
                        url = "https://informiz.org")))

public final class SourceContract implements ContractInterface {

    private static ObjectMapper mapper = new ObjectMapper();

    public enum SourceErrors {
        SOURCE_NOT_FOUND,
        SOURCE_ALREADY_EXISTS
    }

    /**
     * Init is called when initializing or updating chaincode. Use this to set
     * initial world state
     *
     * @param ctx
     * @return Response with message and payload
     */
    @Transaction
    public void init(final Context ctx) {}

    /**
     * Retrieves a source with the specified key (sid) from the ledger.
     *
     * @param ctx the transaction context
     * @param sid the source ID
     * @return the source found on the ledger if there was one
     */
    @Transaction()
    public Source querySource(final Context ctx, final String sid) {
        ChaincodeStub stub = ctx.getStub();
        return updateSource(sid, stub, src -> {});
    }

    /**
     * Returns all the sources currently on the ledger, in pages.
     * When an empty string is passed as a value to the <code>bookmark</code> argument,
     * the returned iterator contains the first <code>pageSize</code> keys. When the
     * <code>bookmark</code> is a non-empty string, the iterator contains the next <code>pageSize</code>
     * keys after the bookmark. Note that only the bookmark present in a prior page of query results
     * can be used as a value to the bookmark argument.
     *
     * @param ctx the transaction context
     * @param pageSize the page size TODO: should be int
     * @param bookmark the bookmark
     * @return the source found on the ledger if there was one
     */
    @Transaction()
    public String queryAllSources(final Context ctx, final String pageSize, final String bookmark) {
        ChaincodeStub stub = ctx.getStub();
        QueryResultsIteratorWithMetadata<KeyValue> states =
                stub.getStateByRangeWithPagination("", "", Integer.valueOf(pageSize), bookmark);

        try {
            return mapper.writeValueAsString(new PaginatedResults(states));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize query result", e);
        }

    }

    /**
     * Creates a new source on the ledger.
     *
     * @param ctx the transaction context
     * @param name the source's name
     * @param reliability the initial reliability
     * @param confidence the initial confidence
     * @return the created Source
     */
    @Transaction()
    public Source createSource(final Context ctx, String name, float reliability, float confidence) {
        ChaincodeStub stub = ctx.getStub();

        Source source = Source.createSource(name, reliability, confidence);
        try {
            String srcState = mapper.writeValueAsString(source);
            stub.putStringState(source.getSid(), srcState);
        } catch (JsonProcessingException e) {
            throw new ChaincodeException("Failed to serialize source info", e);
        }
        return source;
    }

    /**
     * Changes the name of a source on the ledger.
     *
     * @param ctx the transaction context
     * @param sid the key associated with the source on the ledger
     * @param name the new name
     * @return the updated Source
     */
    @Transaction()
    public Source updateSourceName(final Context ctx, final String sid, final String name) {
        ChaincodeStub stub = ctx.getStub();

        return updateSource(sid, stub, src -> src.setName(name));
    }

    /**
     * Add a review to a source on the ledger.
     *
     * @param ctx the transaction context
     * @param sid the key associated with the source on the ledger
     * @param factCheckerId the reviewing fact-checker's id
     * @param reliability the reliability given by the fact-checker to the souorce
     * @return the updated Source
     */
    @Transaction()
    public Source addReview(final Context ctx, final String sid, final String factCheckerId, float reliability) {
        ChaincodeStub stub = ctx.getStub();

        return updateSource(sid, stub, src -> src.addReview(factCheckerId, reliability));
    }

    /**
     * Remove a review from a source on the ledger.
     *
     * @param ctx the transaction context
     * @param sid the key associated with the source on the ledger
     * @param factCheckerId the reviewing fact-checker's id
     * @return the updated Source
     */
    @Transaction()
    public Source removeReview(final Context ctx, final String sid, final String factCheckerId) {
        ChaincodeStub stub = ctx.getStub();

        return updateSource(sid, stub, src -> src.removeReview(factCheckerId));
    }

    /**
     * Changes the score of a source on the ledger.
     *
     * @param ctx the transaction context
     * @param sid the key associated with the source on the ledger
     * @param reliability the new reliability
     * @param confidence the new confidence
     * @return the updated Source
     */
    @Transaction()
    public Source updateSourceScore(final Context ctx, final String sid, float reliability, float confidence) {
        ChaincodeStub stub = ctx.getStub();

        return updateSource(sid, stub, src -> src.setScore(new Score(reliability, confidence)));
    }

    /**
     * A utility function for updating a source on the ledger
     * @param sid the source's id on the ledger
     * @param stub a chaincode stub
     * @param updateFunc a consumer function for updating the source
     * @return the updated source
     */
    private Source updateSource(String sid, ChaincodeStub stub, Consumer<Source> updateFunc) {
        String srcState = stub.getStringState(sid);

        if (StringUtils.isBlank(srcState)) {
            String errorMessage = String.format("Source %s does not exist", sid);
            throw new ChaincodeException(errorMessage, SourceErrors.SOURCE_NOT_FOUND.toString());
        }

        Source source;
        try {
            source = mapper.readValue(srcState, Source.class);
            updateFunc.accept(source);
            String newFcState = mapper.writeValueAsString(source);
            stub.putStringState(sid, newFcState);
        } catch (IOException e) {
            throw new ChaincodeException("Failed to de/serialize source info", e);
        }
        return source;
    }

}
