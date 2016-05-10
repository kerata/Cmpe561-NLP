package POSTagger.Utils;

import java.util.Arrays;

public class Token {

    private int id;
    private String form;
    private String lemma;
    private String cPosTag;
    private String posTag;
    private String[] feats;
    private int head;
    private String deprel;
    /**
     * not available in training set
     */
    private String pHead;
    /**
     * not available in training set
     */
    private String pDeprel;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form.equals("_") ? null : form;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma.equals("_") ? null : lemma;
    }

    public String getcPosTag() {
        return cPosTag;
    }

    public void setcPosTag(String cPosTag) {
        this.cPosTag = cPosTag.equals("_") ? null : cPosTag;
    }

    public String getPosTag() {
        return posTag;
    }

    public void setPosTag(String posTag) {
        this.posTag = posTag.equals("_") ? null : posTag;
    }

    public String[] getFeats() {
        return feats;
    }

    public void setFeats(String feats) {
        this.feats = feats.equals("_") ? null : feats.split("\\|");
    }

    public int getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head.equals("_") ? -1 : Integer.valueOf(head);
    }

    public String getDeprel() {
        return deprel;
    }

    public void setDeprel(String deprel) {
        this.deprel = deprel.equals("_") ? null : deprel;
    }

    public String getpHead() {
        return pHead;
    }

    public void setpHead(String pHead) {
        this.pHead = pHead.equals("_") ? null : pHead;
    }

    public String getpDeprel() {
        return pDeprel;
    }

    public void setpDeprel(String pDeprel) {
        this.pDeprel = pDeprel.equals("_") ? null : pDeprel;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(String.format("%-3d", id))
                .append(String.format("%-20s", form != null ? form : "_"))
                .append(String.format("%-20s", lemma != null ? lemma : "_"))
                .append(String.format("%-10s", cPosTag != null ? cPosTag : "_"))
                .append(String.format("%-10s", posTag != null ? posTag : "_"))
                .append(String.format("%-30s", feats != null ? Arrays.asList(feats).toString() : "_"))
                .append(String.format("%-3d", head))
                .append(String.format("%-25s", deprel != null ? deprel : "_"))
                .append(String.format("%-2s", pHead != null ? pHead : "_"))
                .append(String.format("%-2s", pDeprel != null ? pDeprel : "_"))
                .toString();
    }
}
