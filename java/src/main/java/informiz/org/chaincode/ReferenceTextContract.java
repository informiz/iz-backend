package informiz.org.chaincode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import informiz.org.chaincode.model.PaginatedResults;
import informiz.org.chaincode.model.ReferenceText;
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
 * Java implementation of the ReferenceText Contract. The record contains:
 * - The record's key in the ledger, as the reference-text's id
 * - The text
 * - The locale of the text
 * - A reference source-id (e.g for  the NASA website)
 * - A link to the reference (e.g the specific web page on the NASA website)
 * - Reviews by fact-checkers
 * - The current reliability/confidence score of the reference-text
 */
@Contract(
        name = "ReferenceText",
        info = @Info(
                title = "Reference-text contract",
                description = "A contract for creating and managing reference texts",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "support@informiz.org",
                        name = "Informiz Support Team",
                        url = "https://informiz.org")))

public class ReferenceTextContract implements ContractInterface {

    private static ObjectMapper mapper = new ObjectMapper();

    public enum ReferenceTextErrors {
        REFERENCE_TEXT_NOT_FOUND,
        REFERENCE_TEXT_ALREADY_EXISTS
    }

    // TODO: ************************************** TEST CODE, REMOVE THIS!! ******************************************
    /**
     * Creates some initial reference-texts on the ledger.
     *
     * @param ctx the transaction context
     */
    @Transaction()
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        String[] srcData = {
                "{ \"tid\": \"text-1\", \"text\": \"blah-1\", \"locale\": { \"language\": \"en\", \"country\": \"US\" }, \"sid\": \"source-1\", \"score\": { \"reliability\": 0.85, \"confidence\": 0.7 }}",
                "{ \"tid\": \"text-2\", \"text\": \"blah-2\", \"locale\": { \"language\": \"en\", \"country\": \"US\" }, \"sid\": \"source-2\", \"score\": { \"reliability\": 0.7, \"confidence\": 0.85 }}",
                "{ \"tid\": \"text-3\", \"text\": \"blah-3\", \"locale\": { \"language\": \"en\", \"country\": \"US\" }, \"sid\": \"source-3\", \"score\": { \"reliability\": 0.9, \"confidence\": 0.7 }}",
                "{ \"tid\": \"text-4\", \"text\": \"blah-4\", \"locale\": { \"language\": \"en\", \"country\": \"US\" }, \"sid\": \"source-4\", \"score\": { \"reliability\": 0.94, \"confidence\": 0.73 }}"
        };

        for (int i = 0; i < srcData.length; i++) {
            try {
                ReferenceText src = mapper.readValue(srcData[i], ReferenceText.class);
                String srcState = mapper.writeValueAsString(src);
                stub.putStringState(src.getSid(), srcState);
            } catch (IOException e) {
                throw new ChaincodeException("Failed to initialize reference-text info", e);
            }
        }
    }
    // TODO: ************************************** TEST CODE, REMOVE THIS!! ******************************************


    /**
     * Retrieves a reference-text with the specified key (tid) from the ledger.
     *
     * @param ctx the transaction context
     * @param tid the reference-text's ID
     * @return the reference-text found on the ledger if there was one
     */
    @Transaction()
    public ReferenceText queryReferenceText(final Context ctx, final String tid) {
        ChaincodeStub stub = ctx.getStub();
        String refTextState = stub.getStringState(tid);

        if (StringUtils.isBlank(refTextState)) {
            throw new ChaincodeException(String.format("ReferenceText %s does not exist", tid),
                    ReferenceTextErrors.REFERENCE_TEXT_NOT_FOUND.toString());
        }

        ReferenceText refText = null;
        try {
            refText = mapper.readValue(refTextState, ReferenceText.class);
        } catch (IOException e) {
            throw new ChaincodeException("Failed to deserialize reference-text info", e);
        }

        return refText;
    }

    /**
     * Creates a new reference-text on the ledger.
     *
     * @param ctx the transaction context
     * @param text the text
     * @param sid the id of the source for the text (the source's key on the ledger)
     * @param locale the locale of the text
     * @return the created ReferenceText
     */
    @Transaction()
    public ReferenceText createReferenceText(final Context ctx, final String text,
                                             final String sid, final String link, final Locale locale) {
        ChaincodeStub stub = ctx.getStub();

        ReferenceText refText = ReferenceText.createRefText(text, sid, link, locale);
        try {
            String srcState = mapper.writeValueAsString(refText);
            stub.putStringState(refText.getTid(), srcState);
        } catch (JsonProcessingException e) {
            throw new ChaincodeException("Failed to serialize refText info", e);
        }
        return refText;
    }

    /**
     * Returns all the reference-texts currently on the ledger, in pages.
     * When an empty string is passed as a value to the <code>bookmark</code> argument,
     * the returned iterator contains the first <code>pageSize</code> keys. When the
     * <code>bookmark</code> is a non-empty string, the iterator contains the next <code>pageSize</code>
     * keys after the bookmark. Note that only the bookmark present in a prior page of query results
     * can be used as a value to the bookmark argument.
     *
     * @param ctx the transaction context
     * @param pageSize the page size
     * @param bookmark the bookmark
     * @return a page of reference-texts found on the ledger, in json format, starting from the given bookmark
     */
    @Transaction()
    public PaginatedResults queryAllReferenceTexts(final Context ctx, final int pageSize, final String bookmark) {
        ChaincodeStub stub = ctx.getStub();
        QueryResultsIteratorWithMetadata<KeyValue> states =
                stub.getStateByRangeWithPagination("", "", pageSize, bookmark);

        return new PaginatedResults(states);
    }

    /**
     * Changes the score of a reference-text on the ledger.
     *
     * @param ctx the transaction context
     * @param tid the reference-text id (its key on the ledger)
     * @param reliability the new reliability
     * @param confidence the new confidence
     * @return the updated ReferenceText
     */
    @Transaction()
    public ReferenceText updateReferenceTextScore(final Context ctx, final String tid, float reliability, float confidence) {
        Function<ReferenceText, ReferenceText> updateScore = (refText) -> {
            refText.setScore(new Score(reliability, confidence));; return refText;
        };
        return updateReferenceText(ctx, tid, updateScore);
    }

    /**
     * Update the link to a reference-text on the ledger.
     *
     * @param ctx the transaction context
     * @param tid the reference-text id (its key on the ledger)
     * @param link the new link
     * @return the updated ReferenceText
     */
    @Transaction()
    public ReferenceText updateReferenceTextLink(final Context ctx, final String tid, String link) {
        Function<ReferenceText, ReferenceText> updateSource = (refText) -> {
            refText.setLink(link); return refText;
        };
        return updateReferenceText(ctx, tid, updateSource);
    }

    /**
     * Update the source-id of a reference-text on the ledger.
     *
     * @param ctx the transaction context
     * @param tid the reference-text id (its key on the ledger)
     * @param sid the new source id
     * @return the updated ReferenceText
     */
    @Transaction()
    public ReferenceText updateReferenceTextSource(final Context ctx, final String tid, String sid) {
        Function<ReferenceText, ReferenceText> updateSource = (refText) -> {
            refText.setSid(sid); return refText;
        };
        return updateReferenceText(ctx, tid, updateSource);
    }

    /**
     * Update the locale of a reference-text on the ledger.
     *
     * @param ctx the transaction context
     * @param tid the reference-text id (its key on the ledger)
     * @param locale the new locale
     * @return the updated ReferenceText
     */
    @Transaction()
    public ReferenceText updateReferenceTextLocale(final Context ctx, final String tid, Locale locale) {
        Function<ReferenceText, ReferenceText> updateLocale = (refText) -> {
            refText.setLocale(locale); return refText;
        };
        return updateReferenceText(ctx, tid, updateLocale);
    }

    /**
     * Add or update a fact-checker's review of a reference-text on the ledger.
     *
     * @param ctx the transaction context
     * @param tid the reference-text id (its key on the ledger)
     * @param fcid the fact-checker's id
     * @param reliability the reliability assigned to the reference-text by the fact-checker
     * @return the updated ReferenceText
     */
    @Transaction()
    public ReferenceText addOrUpdateReview(final Context ctx, final String tid, String fcid, float reliability) {
        Function<ReferenceText, ReferenceText> addRefTextReview = (refText) -> {
            refText.addReview(fcid, reliability); return refText;
        };
        return updateReferenceText(ctx, tid, addRefTextReview);
    }

    /**
     * Remove a fact-checker's review from a reference-text on the ledger.
     *
     * @param ctx the transaction context
     * @param tid the reference-text id (its key on the ledger)
     * @param fcid the fact-checker's id
     * @return the updated ReferenceText
     */
    @Transaction()
    public ReferenceText removeReview(final Context ctx, final String tid, String fcid) {
        Function<ReferenceText, ReferenceText> removeRefTextReview = (refText) -> {
            refText.removeReview(fcid); return refText;
        };
        return updateReferenceText(ctx, tid, removeRefTextReview);
    }

    /**
     * Update a ReferenceText record on the ledger
     * @param ctx thr transaction context
     * @param tid the reference-text id (its key on the ledger)
     * @param updateFunc an update function to execute on the reference-text
     * @return the updated reference-text
     */
    private ReferenceText updateReferenceText(Context ctx, String tid,
                                              Function<ReferenceText, ReferenceText> updateFunc) {
        ChaincodeStub stub = ctx.getStub();

        String refTextState = stub.getStringState(tid);

        if (StringUtils.isBlank(refTextState)) {
            String errorMessage = String.format("Reference-text %s does not exist", tid);
            throw new ChaincodeException(errorMessage, ReferenceTextErrors.REFERENCE_TEXT_NOT_FOUND.toString());
        }

        try {
            ReferenceText refText = mapper.readValue(refTextState, ReferenceText.class);
            updateFunc.apply(refText);
            String newFcState = mapper.writeValueAsString(refText);
            stub.putStringState(tid, newFcState);
            return refText;
        } catch (IOException e) {
            throw new ChaincodeException("Failed to de/serialize reference-text info", e);
        }
    }
}
