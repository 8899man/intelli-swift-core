package com.finebi.cube.conf.relation.relation;

import com.finebi.cube.relation.BITableRelation;

import java.io.Serializable;
import java.util.Set;

/**
 * This class created on 2016/4/22.
 *
 * @author Connery
 * @since 4.0
 */
public interface IRelationContainer extends Serializable{
    boolean isEmpty();

    Boolean contain(BITableRelation element);

    Set<BITableRelation> getContainer();
}
