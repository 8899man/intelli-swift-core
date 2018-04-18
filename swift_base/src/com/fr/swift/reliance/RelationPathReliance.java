package com.fr.swift.reliance;

import com.fr.swift.source.DataSource;
import com.fr.swift.source.SourceKey;
import com.fr.swift.source.SourcePath;

import java.util.HashMap;
import java.util.List;

/**
 * @author yee
 * @date 2018/4/18
 */
public class RelationPathReliance extends AbstractRelationReliance<RelationPathNode, SourcePath> {

    public RelationPathReliance(List<SourcePath> allRelationSource, SourceReliance sourceReliance) {
        super(allRelationSource, sourceReliance);
    }

    public RelationPathReliance(List<SourcePath> allRelationSource, List<DataSource> sourceReliance) {
        super(allRelationSource, sourceReliance);
    }

    @Override
    protected void handleSingleRelationSource(List<SourcePath> allRelationSource) {
        this.allRelationSource = new HashMap<SourceKey, SourcePath>();
        for (SourcePath source : allRelationSource) {
            if (source.getRelations().size() > 1 && !this.allRelationSource.containsKey(source.getSourceKey())) {
                this.allRelationSource.put(source.getSourceKey(), source);
            }
        }
    }
}
