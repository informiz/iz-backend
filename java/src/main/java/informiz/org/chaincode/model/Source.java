package informiz.org.chaincode.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.HashMap;
import java.util.Map;

/**
 * A data-type managed by the source contract, representing a source for references (e.g the NASA website):
 * - the id of the source
 * - the source's name
 * - the current reliability/confidence score
 * - reviews by fact-checkers
 * Any additional metadata should be saved on a separate CMS
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@DataType()
public class Source {

    @Property()
    private String sid;

    @Property()
    private String name;

    @Property()
    private Score score;

    @Property()
    private HashMap<String, Float> reviews = new HashMap<>();

    private Source() {
        setSid(Utils.createUuid(ContractType.SOURCE));
        setScore(new Score());
    }

    public static Source createSource(String name, Score score) {
        Source source = new Source();
        source.setName(name);
        if (score != null) {
            source.setScore(score);
        }
        return source;
    }

    public static Source createSource(String name, float reliability, float confidence) {
        return createSource(name, new Score(reliability, confidence));
    }

    public String getSid() {
        return sid;
    }

    private void setSid(String sid) {
        this.sid = sid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    /**
     * Add a review by a fact-checker to this source
     * @param fcid the fact-checker's id
     * @param reliability the score given by the fact-checker
     * @return the previous score given by this fact-checker, if she reviewed this source before
     * @see Map#put(Object, Object)
     */
    public Float addReview(String fcid, float reliability) {
        return reviews.put(fcid, reliability);
    }

    /**
     * Remove a review by a fact-checker from this source
     * @param fcid the fact-checker's id
     * @return the score given by this fact-checker, if one was found
     * @see Map#remove(Object)
     */
    public Float removeReview(String fcid) {
        return reviews.remove(fcid);
    }

    public Map<String, Float> getReviews() {
        return reviews;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Source other = (Source) obj;

        return this.sid.equals(other.sid);
    }

    @Override
    public int hashCode() {
        return this.sid.hashCode();
    }

    @Override
    public String toString() {
        return String.format("{ \"name\": \"%s\", \"score\": %s }", name, score.toString());
    }
}
