package com.finebi.cube.conf.utils;

import com.finebi.cube.conf.BICubeConfigureCenter;
import com.finebi.cube.conf.pack.data.BIBasicBusinessPackage;
import com.finebi.cube.conf.pack.data.IBusinessPackageGetterService;
import com.finebi.cube.conf.table.BIBusinessTable;
import com.finebi.cube.conf.table.BusinessTable;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.general.ComparatorUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by GUY on 2015/3/24.
 */
public class BIPackUtils {

    public static BIBasicBusinessPackage getBusiPackByName(Set<BIBasicBusinessPackage> packs, String name) {
        for (BIBasicBusinessPackage pack : packs) {
            if (ComparatorUtils.equals(pack.getName().getValue(), name)) {
                return pack;
            }
        }
        return null;
    }

    public static Set<BusinessTable> getAllBusiTableKeys(Set<IBusinessPackageGetterService> packs) {
        Set<BusinessTable> keys = new HashSet<BusinessTable>();
        Iterator<IBusinessPackageGetterService> itPacks = packs.iterator();

        while (itPacks.hasNext()) {
            Set<BIBusinessTable> busiTable = itPacks.next().getBusinessTables();

            Iterator<BIBusinessTable> itTable = busiTable.iterator();
            while (itTable.hasNext()) {
                BIBusinessTable table = itTable.next();
                try {
                    keys.add(table);
                } catch (Exception e) {
                    BILoggerFactory.getLogger().error(e.getMessage(), e);
                }
            }
        }
        return keys;
    }

    public static boolean isNoPackageChange(long userId) {
        return getPackageChangeCounts(userId) == 0;
    }

    public static int getPackageChangeCounts(long userId) {
        int count = 0;
        if (BICubeConfigureCenter.getPackageManager().isPackageDataChanged(userId)) {
            count++;
        }
        if (count > 0) {
            return count;
        }
        count += BICubeConfigureCenter.getTableRelationManager().isChanged(userId) ? 1 : 0;
        if (count > 0) {
            return count;
        }
        return count;
    }

    public static boolean isNoGeneratingChange(long userId) {
        return getGeneratingChangeCounts(userId) == 0;
    }


    public static int getGeneratingChangeCounts(long userId) {
        int count = 0;


        if (BICubeConfigureCenter.getPackageManager().isPackageDataChanged(userId)) {
            return 1;
        }

        count += BICubeConfigureCenter.getTableRelationManager().isChanged(userId) ? 1 : 0;
        if (count > 0) {
            return count;
        }
        //TODO Connery
        return count;
    }


}