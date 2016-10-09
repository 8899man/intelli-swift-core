package com.fr.bi.cal.stable.index;

import com.fr.base.FRContext;
import com.fr.bi.cal.stable.cube.file.TableCubeFile;
import com.fr.bi.conf.log.BIRecord;
import com.fr.bi.stable.data.db.BICubeFieldSource;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.general.DateUtils;
import com.fr.json.JSONException;

import java.util.*;
import java.util.Map.Entry;

/**
 * Created by GUY on 2015/3/10.
 */
public class BeforeIndexGenerator extends AbstractIndexGenerator {

    public BeforeIndexGenerator(TableCubeFile cube, CubeTableSource dataSource, Set<CubeTableSource> derivedDataSources, BIRecord log) {
        super(cube, dataSource, derivedDataSources, log);
    }


    private ICubeFieldSource[] getFieldsArray() {
        return dataSource.getFieldsArray(derivedDataSources);
    }

    /**
     * 取数部分
     * 将包中的表从数据库中读取到本地cube磁盘，
     * cube中primary文件
     */
    @Override
    public void generateCube() {
        BILoggerFactory.getLogger().info("table: " + dataSource.toString() + " loading fields info : ");
        long start = System.currentTimeMillis();

        try {
            ICubeFieldSource[] columns = getFieldsArray();
            List<String> columnList = new ArrayList<String>();
            Map<String, Integer> columnNameSet = new HashMap<String, Integer>();
            for (ICubeFieldSource col : columns) {
                ICubeFieldSource field = new BICubeFieldSource(dataSource, col.getFieldName(), col.getClassType(), col.getFieldSize());
                try {
                    columnList.add(field.createJSON().toString());
                } catch (JSONException e) {
                    FRContext.getLogger().error(e.getMessage(), e);
                }
                Integer count = columnNameSet.get(field.getFieldName());
                if (count == null) {
                    count = 0;
                }
                count++;
                columnNameSet.put(field.getFieldName(), count);
            }
            if (columnNameSet.size() != columns.length) {
                StringBuffer sb = new StringBuffer();
                for (Entry<String, Integer> entry : columnNameSet.entrySet()) {
                    if (entry.getValue() > 1) {
                        sb.append(entry.getKey());
                        sb.append(",");
                    }
                }
                if (sb.length() > 0) {
                    throw new Exception("field list contains duplicate field names:" + sb.substring(0, sb.length() - 1).toString());
                }
            }
            cube.mkDir();
            cube.writeMain(columnList);
            BILoggerFactory.getLogger().info("table: " + dataSource.toString() + " loading fields info , Costs : " + DateUtils.timeCostFrom(start));
        } catch (Throwable e) {
            FRContext.getLogger().error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}