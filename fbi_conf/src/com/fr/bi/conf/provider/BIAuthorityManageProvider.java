package com.fr.bi.conf.provider;

import com.finebi.cube.conf.pack.data.BIPackageID;
import com.fr.bi.conf.base.auth.data.BIPackageAuthority;
import com.fr.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by kary on 16/4/25.
 */
public interface BIAuthorityManageProvider {
    String XML_TAG = "BIPackageAuthManageProvider";

    void savePackageAuth(BIPackageID packageID, List<BIPackageAuthority> auth, long userId);

    Map<BIPackageID, List<BIPackageAuthority>> getPackagesAuth(long userId);

    List<BIPackageAuthority> getPackageAuthByID(BIPackageID packageID, long userId);

    List<BIPackageAuthority> getPackageAuthBySession(BIPackageID packageID, String sessionId);

    List<BIPackageID> getAuthPackagesByUser(long userId);

    List<BIPackageID> getAuthPackagesBySession(String sessionId);

    boolean hasAuthPackageByUser(long userId, String sessionId);

    JSONObject createJSON(long userId) throws Exception;

    void clear(long userId);

    void removeAuthPackage(BIPackageID packageID);

    @Deprecated
    void persistData(long userId);
}
