package com.fr.bi.conf.data.source.operator;

import com.finebi.cube.api.ICubeDataLoader;
import com.fr.bi.base.*;
import com.fr.bi.common.inter.Traversal;
import com.fr.bi.stable.data.db.BIDataValue;
import com.fr.bi.stable.data.db.IPersistentTable;
import com.fr.bi.stable.data.db.PersistentTable;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.stable.xml.XMLPrintWriter;
import com.fr.stable.xml.XMLableReader;

import java.util.List;

/**
 * Created by GUY on 2015/3/5.
 */
public abstract class AbstractETLOperator implements IETLOperator {

    private static final long serialVersionUID = 1642178405376794890L;
    //    protected transient String md5;
    protected BIUser user;

    public AbstractETLOperator(long userId) {
        user = new BIUser(userId);
    }

    public AbstractETLOperator() {
        super();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public void readXML(XMLableReader reader) {
        if (reader.isAttr()) {
            if (user == null) {
                user = new BIUser(-1);
            }
            user.readXML(reader);
//            md5 = reader.getAttrAsString("md5", StringUtils.EMPTY);
        }
    }

    /**
     * Write XML.<br>
     * The method will be invoked when save data to XML file.<br>
     * May override the method to save your own data.
     * 从性能上面考虑，大家用writer.print(), 而不是writer.println()
     *
     * @param writer XML写入对象
     */
    @Override
    public void writeXML(XMLPrintWriter writer) {
        user.writeXML(writer);
//        writer.attr("md5", fetchObjectCore());
    }

    protected IPersistentTable getBITable() {
        return new PersistentTable(null, fetchObjectCore().getIDValue(), null);
    }

    @Override
    public abstract String xmlTag();

    @Override
    public int writeIndexWithParents(Traversal<BIDataValue> travel, List<? extends CubeTableSource> parents, ICubeDataLoader loader, int startCol) {
        return writeSimpleIndex(travel, parents, loader);
    }

    @Override
    public BICore fetchObjectCore() {

        try {
            return new BICoreGenerator(this).fetchObjectCore();
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
        return BIBasicCore.EMPTY_CORE;
    }

    protected CubeTableSource getSingleParentMD5(List<? extends CubeTableSource> parents) {
        if (parents == null || parents.size() != 1) {
            return null;
        }
        return parents.get(0);
    }
}