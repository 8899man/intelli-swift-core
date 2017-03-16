package com.fr.bi.conf.session;

import com.fr.bi.conf.report.BIReport;
import com.fr.bi.base.provider.UserProvider;
import com.finebi.cube.api.ICubeDataLoader;
import com.fr.fs.base.entity.CompanyRole;
import com.fr.fs.base.entity.CustomRole;

import java.util.List;

/**
 * Created by GUY on 2015/4/8.
 */
public interface BISessionProvider extends UserProvider {
    /**
     * 是不是分享
     *
     * @return
     */
    boolean isSharedReq();

    /**
     * 保存Report
     *
     * @return
     */
    BIReport getBIReport();

    /**
     * 保存Loader
     *
     * @return
     */
    ICubeDataLoader getLoader();

    List<CustomRole> getCustomRoles();

    List<CompanyRole> getCompanyRoles();
}