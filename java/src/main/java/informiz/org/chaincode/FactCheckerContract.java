package informiz.org.chaincode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import informiz.org.chaincode.model.FactChecker;
import informiz.org.chaincode.model.PaginatedResults;
import informiz.org.chaincode.model.Utils;
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
 * Java implementation of the Fact-Checker Contract. The record contains:
 * - The record's key in the ledger, as the fact-checker's id
 * - The current reliability/confidence score of the fact-checker
 * It is expected that any metadata about the fact-checker will be stored on an external CMS (e.g SQL database). The
 * fcid should be used as reference key.
 */
@Contract(
        name = "FactCheckerContract",
        info = @Info(
                title = "Fact-Checker contract",
                description = "A contract for creating and managing Fact-Checkers",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "support@informiz.org",
                        name = "Informiz Support Team",
                        url = "https://informiz.org")))
public final class FactCheckerContract implements ContractInterface {

    private static ObjectMapper mapper = new ObjectMapper();

    public enum FactCheckerErrors {
        FACT_CHECKER_NOT_FOUND,
        FACT_CHECKER_ALREADY_EXISTS
    }

    /**
     * Init is called when initializing or updating chaincode. Use this to set
     * initial world state
     * TODO: Can init/recover with non-empty couchDB? Otherwise - can parallelize without overloading HLF?
     *
     * @param ctx
     * @return Response with message and payload
     */
    @Transaction
    public void init(final Context ctx) {}

    /**
     * Retrieves a fact-checker with the specified key (fcid) from the ledger.
     *
     * @param ctx the transaction context
     * @param key the fact-checker ID
     * @return the fact-checker found on the ledger if there was one
     */
    @Transaction()
    public FactChecker queryFactChecker(final Context ctx, final String key) {
        ChaincodeStub stub = ctx.getStub();
        return updateFactChecker(key, stub, fc -> {});
    }

    /**
     * Creates a new fact-checker on the ledger.
     *
     * @param ctx the transaction context
     * @param name the fact-checker's name
     * @param reliability the initial reliability, in the range [0.0 - 1.0], as string
     * @param confidence the initial confidence, in the range [0.0 - 1.0], as string
     * @param email the fact-checker's email address
     * @param link a link to the fact-checker's personal profile
     * @return the created FactChecker
     */
    @Transaction()
    public FactChecker createFactChecker(final Context ctx, final String name,
                                         final String reliability, final String confidence,
                                         final String email, final String link) {
        ChaincodeStub stub = ctx.getStub();

        // TODO: authentication and authorization?
        // ClientIdentity id = ctx.getClientIdentity(); ...

        FactChecker factChecker = FactChecker.createFactChecker(name, Utils.createScore(reliability, confidence));
        factChecker.setEmail(email);
        factChecker.setLink(link);
        try {
            String fcState = mapper.writeValueAsString(factChecker);
            stub.putStringState(factChecker.getFcid(), fcState);
        } catch (JsonProcessingException e) {
            throw new ChaincodeException("Failed to serialize fact-checker info", e);
        }
        return factChecker;
    }

    /**
     * Returns all the fact-checkers currently on the ledger, in pages.
     * When an empty string is passed as a value to the <code>bookmark</code> argument,
     * the returned iterator contains the first <code>pageSize</code> keys. When the
     * <code>bookmark</code> is a non-empty string, the iterator contains the next <code>pageSize</code>
     * keys after the bookmark. Note that only the bookmark present in a prior page of query results
     * can be used as a value to the bookmark argument.
     *
     * @param ctx the transaction context
     * @param pageSize the page size, as a string. Size is limited to 10-100 items per page
     * @param bookmark the bookmark
     * @return a page of fact-checkers found on the ledger, in json format, starting from the given bookmark
     */
    @Transaction()
    public PaginatedResults queryAllFactCheckers(final Context ctx, final String pageSize, final String bookmark) {
        ChaincodeStub stub = ctx.getStub();
        int size = Utils.pageSizeFromString(pageSize);
        QueryResultsIteratorWithMetadata<KeyValue> states =
                stub.getStateByRangeWithPagination("", "", size, bookmark);

        return new PaginatedResults(states);
    }

    /**
     * Changes the name of a fact-checker on the ledger.
     *
     * @param ctx the transaction context
     * @param fcid the key associated with the fact-checker on the ledger
     * @param name the new name
     * @return the updated FactChecker
     */
    @Transaction()
    public FactChecker updateFactCheckerName(final Context ctx, final String fcid, final String name) {
        ChaincodeStub stub = ctx.getStub();

        return updateFactChecker(fcid, stub, fc -> fc.setName(name));
    }

    /**
     * Changes the score of a fact-checker on the ledger.
     *
     * @param ctx the transaction context
     * @param fcid the key associated with the fact-checker on the ledger
     * @param reliability the new reliability
     * @param confidence the new confidence
     * @return the updated FactChecker
     */
    @Transaction()
    public FactChecker updateFactCheckerScore(final Context ctx, final String fcid,
                                              final String reliability, final String confidence) {
        ChaincodeStub stub = ctx.getStub();
        return updateFactChecker(fcid, stub, fc -> fc.setScore(Utils.createScore(reliability, confidence)));
    }

    /**
     * Changes the email address of a fact-checker on the ledger.
     *
     * @param ctx the transaction context
     * @param fcid the key associated with the fact-checker on the ledger
     * @param email the new email address
     * @return the updated FactChecker
     */
    @Transaction()
    public FactChecker updateFactCheckerEmail(final Context ctx, final String fcid, final String email) {
        ChaincodeStub stub = ctx.getStub();

        return updateFactChecker(fcid, stub, fc -> fc.setEmail(email));
    }


    /**
     * Changes the personal-profile link of a fact-checker on the ledger.
     *
     * @param ctx the transaction context
     * @param fcid the key associated with the fact-checker on the ledger
     * @param link the new personal-profile link
     * @return the updated FactChecker
     */
    @Transaction()
    public FactChecker updateFactCheckerLink(final Context ctx, final String fcid, final String link) {
        ChaincodeStub stub = ctx.getStub();

        return updateFactChecker(fcid, stub, fc -> fc.setLink(link));
    }

    /**
     * Changes the personal-profile link of a fact-checker on the ledger.
     *
     * @param ctx the transaction context
     * @param fcid the key associated with the fact-checker on the ledger
     * @param jsonStr the json representation of the updated fact-checker
     * @return the updated FactChecker
     */
    @Transaction()
    public FactChecker updateFactCheckerInfo(final Context ctx, final String fcid, final String jsonStr) {
        ChaincodeStub stub = ctx.getStub();
        FactChecker updated;
        try {
            updated = mapper.readValue(jsonStr, FactChecker.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to deserialize facte-checker info", e);
        }

        return updateFactChecker(fcid, stub, factChecker -> factChecker.updateInfo(updated));
    }

    /**
     * Delete a fact-checker (mark it as inactive on the ledger).
     *
     * @param ctx the transaction context
     * @param fcid the key associated with the fact-checker on the ledger
     * @return the updated FactChecker
     */
    @Transaction()
    public FactChecker deleteFactChecker(final Context ctx, final String fcid) {
        ChaincodeStub stub = ctx.getStub();

        return updateFactChecker(fcid, stub, fc -> fc.setActive(false));
    }

    /**
     * A utility function for updating a fact-checker on the ledger
     * @param fcid the fact-checker's id on the ledger
     * @param stub a chaincode stub
     * @param updateFunc a consumer function for updating the fact-checker
     * @return the updated fact-checker
     */
    private FactChecker updateFactChecker(final String fcid, final ChaincodeStub stub,
                                          final Consumer<FactChecker> updateFunc) {
        String factCheckerState = stub.getStringState(fcid);

        if (StringUtils.isBlank(factCheckerState)) {
            String errorMessage = String.format("Fact-checker %s does not exist", fcid);
            throw new ChaincodeException(errorMessage, FactCheckerErrors.FACT_CHECKER_NOT_FOUND.toString());
        }

        FactChecker factChecker;
        try {
            factChecker = mapper.readValue(factCheckerState, FactChecker.class);
            updateFunc.accept(factChecker);
            String newFcState = mapper.writeValueAsString(factChecker);
            stub.putStringState(fcid, newFcState);
        } catch (IOException e) {
            throw new ChaincodeException("Failed to de/serialize fact-checker info", e);
        }
        return factChecker;
    }

}
