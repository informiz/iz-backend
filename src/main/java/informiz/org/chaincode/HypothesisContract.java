package informiz.org.chaincode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import informiz.org.chaincode.model.Hypothesis;
import informiz.org.chaincode.model.PaginatedResults;
import informiz.org.chaincode.model.Score;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIteratorWithMetadata;

import java.io.IOException;
import java.util.Locale;
import java.util.function.Function;

/**
 * Java implementation of the Hypothesis Contract. The record contains:
 * - The record's key in the ledger, as the hypothesis-id
 * - The factual claim
 * - The locale of the text
 * - References for the factual claim
 * - Reviews by fact-checkers
 * - The current reliability/confidence score of the hypothesis
 */
@Contract(
        name = "Hypothesis",
        info = @Info(
                title = "Hypothesis contract",
                description = "A contract for creating and managing hypothesis records",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "support@informiz.org",
                        name = "Informiz Support Team",
                        url = "https://informiz.org")))

public class HypothesisContract implements ContractInterface {

    private static ObjectMapper mapper = new ObjectMapper();

    public enum HypothesisErrors {
        HYPOTHESIS_NOT_FOUND,
        HYPOTHESIS_ALREADY_EXISTS
    }

    /**
     * Retrieves a hypothesis with the specified key (hid) from the ledger.
     *
     * @param ctx the transaction context
     * @param hid the hypothesis ID
     * @return the hypothesis found on the ledger if there was one
     */
    @Transaction()
    public Hypothesis queryHypothesis(final Context ctx, final String hid) {
        ChaincodeStub stub = ctx.getStub();
        String hypothesisState = stub.getStringState(hid);

        if (StringUtils.isBlank(hypothesisState)) {
            throw new ChaincodeException(String.format("Hypothesis %s does not exist", hid),
                    HypothesisErrors.HYPOTHESIS_NOT_FOUND.toString());
        }

        Hypothesis hypothesis = null;
        try {
            hypothesis = mapper.readValue(hypothesisState, Hypothesis.class);
        } catch (IOException e) {
            throw new ChaincodeException("Failed to deserialize hypothesis info", e);
        }

        return hypothesis;
    }

    /**
     * Creates a new hypothesis on the ledger.
     *
     * @param ctx the transaction context
     * @param claim the claim
     * @param locale the locale of the claim
     * @return the created Hypothesis
     */
    @Transaction()
    public Hypothesis createHypothesis(final Context ctx, final String claim, final Locale locale) {
        ChaincodeStub stub = ctx.getStub();

        Hypothesis hypothesis = Hypothesis.createHypothesis(claim, locale);
        try {
            String hypothesisState = mapper.writeValueAsString(hypothesis);
            stub.putStringState(hypothesis.getHid(), hypothesisState);
        } catch (JsonProcessingException e) {
            throw new ChaincodeException("Failed to serialize hypothesis info", e);
        }
        return hypothesis;
    }

    /**
     * Returns all the hypothesis currently on the ledger, in pages.
     * When an empty string is passed as a value to the <code>bookmark</code> argument,
     * the returned iterator contains the first <code>pageSize</code> keys. When the
     * <code>bookmark</code> is a non-empty string, the iterator contains the next <code>pageSize</code>
     * keys after the bookmark. Note that only the bookmark present in a prior page of query results
     * can be used as a value to the bookmark argument.
     *
     * @param ctx the transaction context
     * @param pageSize the page size
     * @param bookmark the bookmark
     * @return a page of hypothesiss found on the ledger, in json format, starting from the given bookmark
     */
    @Transaction()
    public PaginatedResults queryAllHypothesis(final Context ctx, final int pageSize, final String bookmark) {
        ChaincodeStub stub = ctx.getStub();
        QueryResultsIteratorWithMetadata<KeyValue> states =
                stub.getStateByRangeWithPagination("", "", pageSize, bookmark);

        return new PaginatedResults(states);
    }

    /**
     * Changes the score of a hypothesis on the ledger.
     *
     * @param ctx the transaction context
     * @param hid the hypothesis id (its key on the ledger)
     * @param reliability the new reliability
     * @param confidence the new confidence
     * @return the updated Hypothesis
     */
    @Transaction()
    public Hypothesis updateHypothesisScore(final Context ctx, final String hid, float reliability, float confidence) {
        Function<Hypothesis, Hypothesis> updateScore = (hypothesis) -> {
            hypothesis.setScore(new Score(reliability, confidence));; return hypothesis;
        };
        return updateHypothesis(ctx, hid, updateScore);
    }

    /**
     * Update the locale of a hypothesis on the ledger.
     *
     * @param ctx the transaction context
     * @param hid the hypothesis id (its key on the ledger)
     * @param locale the new locale
     * @return the updated Hypothesis
     */
    @Transaction()
    public Hypothesis updateHypothesisLocale(final Context ctx, final String hid, Locale locale) {
        Function<Hypothesis, Hypothesis> updateLocale = (hypothesis) -> {
            hypothesis.setLocale(locale); return hypothesis;
        };
        return updateHypothesis(ctx, hid, updateLocale);
    }

    /**
     * Add or update a fact-checker's review of a hypothesis on the ledger.
     *
     * @param ctx the transaction context
     * @param hid the hypothesis id (its key on the ledger)
     * @param fcid the hypothesis's id
     * @param reliability the reliability assigned to the hypothesis by the fact-checker
     * @return the updated Hypothesis
     */
    @Transaction()
    public Hypothesis addOrUpdateReview(final Context ctx, final String hid, String fcid, float reliability) {
        Function<Hypothesis, Hypothesis> addRefTextReview = (hypothesis) -> {
            hypothesis.addReview(fcid, reliability); return hypothesis;
        };
        return updateHypothesis(ctx, hid, addRefTextReview);
    }

    /**
     * Remove a fact-checker's review from a hypothesis on the ledger.
     *
     * @param ctx the transaction context
     * @param hid the hypothesis (its key on the ledger)
     * @param fcid the fact-checker's id
     * @return the updated Hypothesis
     */
    @Transaction()
    public Hypothesis removeReview(final Context ctx, final String hid, String fcid) {
        Function<Hypothesis, Hypothesis> removeRefTextReview = (hypothesis) -> {
            hypothesis.removeReview(fcid); return hypothesis;
        };
        return updateHypothesis(ctx, hid, removeRefTextReview);
    }

    /**
     * Add a reference to a hypothesis on the ledger.
     *
     * @param ctx the transaction context
     * @param hid the hypothesis id (its key on the ledger)
     * @param tid the reference-text id (its key on the ledger)
     * @return the updated Hypothesis
     */
    @Transaction()
    public Hypothesis addReference(final Context ctx, final String hid, final String tid) {
        Function<Hypothesis, Hypothesis> addRefTextReview = (hypothesis) -> {
            hypothesis.addReference(tid); return hypothesis;
        };
        return updateHypothesis(ctx, hid, addRefTextReview);
    }

    /**
     * Remove a reference from a hypothesis on the ledger.
     *
     * @param ctx the transaction context
     * @param hid the hypothesis (its key on the ledger)
     * @param tid the reference-text id (its key on the ledger)
     * @return the updated Hypothesis
     */
    @Transaction()
    public Hypothesis removeReference(final Context ctx, final String hid, final String tid) {
        Function<Hypothesis, Hypothesis> removeRefTextReview = (hypothesis) -> {
            hypothesis.removeReference(tid); return hypothesis;
        };
        return updateHypothesis(ctx, hid, removeRefTextReview);
    }

    /**
     * Update a Hypothesis record on the ledger
     * @param ctx thr transaction context
     * @param hid the hypothesis id (its key on the ledger)
     * @param updateFunc an update function to execute on the hypothesis
     * @return the updated hypothesis
     */
    private Hypothesis updateHypothesis(Context ctx, String hid,
                                              Function<Hypothesis, Hypothesis> updateFunc) {
        ChaincodeStub stub = ctx.getStub();

        String hypothesisState = stub.getStringState(hid);

        if (StringUtils.isBlank(hypothesisState)) {
            String errorMessage = String.format("Reference-text %s does not exist", hid);
            throw new ChaincodeException(errorMessage, HypothesisErrors.HYPOTHESIS_NOT_FOUND.toString());
        }

        try {
            Hypothesis hypothesis = mapper.readValue(hypothesisState, Hypothesis.class);
            updateFunc.apply(hypothesis);
            String newFcState = mapper.writeValueAsString(hypothesis);
            stub.putStringState(hid, newFcState);
            return hypothesis;
        } catch (IOException e) {
            throw new ChaincodeException("Failed to de/serialize hypothesis info", e);
        }
    }
}
