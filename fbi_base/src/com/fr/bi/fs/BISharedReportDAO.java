package com.fr.bi.fs;

import com.fr.fs.base.entity.User;

import java.util.List;

/**
 * @author richie
 * @date 2015-04-28
 * @since 8.0
 */
public interface BISharedReportDAO {

    void resetSharedByReportIdAndUsers(long reportId, long createBy, long[] userids, boolean isReset) throws Exception;

    List<User> findUsersByReport(long reportId, long createBy) throws Exception;

    List<BISharedReportNode> findReportsByShare2User(long userId) throws Exception;

    void removeSharedByReport(long reportId, long createBy) throws Exception;

    void transfer(BISharedReportNode var1) throws Exception;
}