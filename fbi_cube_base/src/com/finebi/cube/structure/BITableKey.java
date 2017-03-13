package com.finebi.cube.structure;

import com.fr.bi.stable.data.source.CubeTableSource;

/**
 * This class created on 2016/4/6.
 *
 * @author Connery
 * @since 4.0
 */
public class BITableKey implements ITableKey {
    private static final long serialVersionUID = -4307699484676276880L;
    private String sourceID;

    public BITableKey(String sourceID) {
        this.sourceID = sourceID;
    }

    public BITableKey(CubeTableSource tableSource) {
        this(tableSource.getSourceID());
    }

    @Override
    public String getSourceID() {
        return sourceID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BITableKey)) return false;

        BITableKey that = (BITableKey) o;

        return !(sourceID != null ? !sourceID.equals(that.sourceID) : that.sourceID != null);

    }

    @Override
    public int hashCode() {
        return sourceID != null ? sourceID.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("BITableKey{");
        sb.append("sourceID='").append(sourceID).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
