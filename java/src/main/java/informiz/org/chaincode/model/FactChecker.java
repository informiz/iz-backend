package informiz.org.chaincode.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

// TODO: add validation to setters, add to tests
/**
 * A data-type managed by the fact-checker contract, representing a fact-checker:
 *  * - the id of the fact-checker
 *  * - the fact-checker's name
 *  * - the current reliability/confidence score
 *  * - the fact-checker's email address
 *  * - a link to the fact-checker's personal profile
 *  * - a boolean indicating if the fact-checker is active (a fact-checker is never deleted, only deactivated)
 * Any additional metadata should be saved on a separate CMS
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@DataType()
public final class FactChecker {

    @Property()
    private String fcid;

    @Property()
    private Score score;

    @Property()
    private String name;

    @Property()
    private String email;

    @Property()
    private String link;

    @Property()
    private Boolean active;


    private FactChecker() {
        setFcid(Utils.createUuid(ContractType.FACT_CHECKER));
        setScore(new Score());
        setActive(true);
    }

    public static FactChecker createFactChecker(String name, Score score) {
        FactChecker factChecker = new FactChecker();
        factChecker.setName(name);
        if (score != null) {
            factChecker.setScore(score);
        }
        return factChecker;
    }

    public static FactChecker createFactChecker(String name, float reliability, float confidence) {
        return createFactChecker(name, new Score(reliability, confidence));
    }

    public String getFcid() {
        return fcid;
    }

    private void setFcid(String fcid) {
        this.fcid = fcid;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public int hashCode() {
        return this.fcid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        FactChecker other = (FactChecker) obj;

        return this.fcid.equals(other.fcid);
    }

    @Override
    public String toString() {
        return String.format("{ \"name\": \"%s\", \"score\": %s }", name, score.toString());
    }

    // TODO: test
    public void updateInfo(FactChecker updated) {
        this.setName(updated.getName());
        this.setEmail(updated.getEmail());
        this.setLink(updated.getLink());
        this.setScore(updated.getScore());
        this.setActive(updated.getActive());
    }
}
