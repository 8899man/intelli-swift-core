package com.finebi.cube.conf.trans;

import com.finebi.cube.conf.BIAliasManagerProvider;
import com.finebi.cube.conf.BISystemDataManager;
import com.fr.bi.exception.BIKeyAbsentException;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.bi.exception.BIKeyDuplicateException;
import com.fr.bi.stable.utils.program.BINonValueUtils;
import com.fr.fs.control.UserControl;
import com.fr.json.JSONObject;

/**
 * Created by GUY on 2015/4/2.
 *
 * @author Connery
 */
public class BIAliasManager extends BISystemDataManager<UserAliasManager> implements BIAliasManagerProvider {
    @Override
    public UserAliasManager constructUserManagerValue(Long userId) {
        return new UserAliasManager(userId);
    }

    @Override
    public String managerTag() {
        return BIAliasManagerProvider.XML_TAG;
    }

    @Override
    public String persistUserDataName(long key) {
        return managerTag();
    }

    @Override
    public UserAliasManager getTransManager(long userId) {
        try {
            return getValue(userId);
        } catch (BIKeyAbsentException e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            throw BINonValueUtils.beyondControl(e);
        }
    }
    @Override
    public void setTransManager(long userId,UserAliasManager value){
        try {
            if (containsKey(userId)){
                remove(userId);
            }
            putKeyValue(userId,value);
        } catch (BIKeyAbsentException e) {
            BILoggerFactory.getLogger(this.getClass()).error(e.getMessage(), e);
            throw BINonValueUtils.beyondControl(e);
        } catch (BIKeyDuplicateException e) {
            BILoggerFactory.getLogger(this.getClass()).error(e.getMessage(), e);
        }
    }
    @Override
    public void setAliasName(String id, String name, long userId) {
        getTransManager(userId).setTransName(id, name);
    }

    @Override
    public String getAliasName(String id, long userId) {
        return getTransManager(userId).getTransName(id);
    }

    @Override
    public void removeAliasName(String id, long userId) {
        getTransManager(userId).removeTransName(id);
    }

    public JSONObject getAliasJSON(long userID) {
        try {
            JSONObject jo = getTransManager(userID).createJSON();
            if (userID != UserControl.getInstance().getSuperManagerID()) {
                jo.join(getTransManager(UserControl.getInstance().getSuperManagerID()).createJSON());
            }
            return jo;
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            throw BINonValueUtils.beyondControl(e);
        }
    }

    @Override
    public void persistData(long userId) {
        persistUserData(userId);
    }

    /**
     * 更新
     */
    @Override
    public void envChanged() {
        clear();
    }
}