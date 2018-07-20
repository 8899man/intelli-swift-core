package com.fr.swift.manager;

import com.fr.swift.config.entity.SwiftTablePathEntity;
import com.fr.swift.config.service.SwiftTablePathService;
import com.fr.swift.source.SourceKey;

/**
 * @author yee
 * @date 2018/7/19
 */
public class IndexingSegmentManager extends LineSegmentManager {
    @Override
    protected Integer getCurrentFolder(SwiftTablePathService service, SourceKey sourceKey) {
        SwiftTablePathEntity entity = service.get(sourceKey.getId());
        if (null == entity) {
            entity = new SwiftTablePathEntity(sourceKey.getId(), 0);
            service.saveOrUpdate(entity);
        }
        return entity.getTmpDir();
    }
}
