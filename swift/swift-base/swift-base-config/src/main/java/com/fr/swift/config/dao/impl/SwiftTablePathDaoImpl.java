package com.fr.swift.config.dao.impl;

import com.fr.swift.config.bean.SwiftTablePathBean;
import com.fr.swift.config.dao.BasicDao;
import com.fr.swift.config.dao.SwiftTablePathDao;
import com.fr.swift.config.oper.impl.RestrictionFactoryImpl;

/**
 * @author yee
 * @date 2018-11-28
 */
public class SwiftTablePathDaoImpl extends BasicDao<SwiftTablePathBean> implements SwiftTablePathDao {
    public SwiftTablePathDaoImpl() {
        super(SwiftTablePathBean.TYPE, RestrictionFactoryImpl.INSTANCE);
    }
}
