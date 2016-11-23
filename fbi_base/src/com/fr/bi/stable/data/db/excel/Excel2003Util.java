package com.fr.bi.stable.data.db.excel;

import com.fr.general.ComparatorUtils;
import com.fr.general.Inter;
import com.fr.stable.StringUtils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Young's on 2015/7/3.
 */
public class Excel2003Util extends AbstractExcel2003Util {

    public Excel2003Util(String filename) throws IOException {
        super(filename);
    }

    public Excel2003Util(String filePath, boolean preview) throws Exception {
        super(filePath,preview);
    }

    protected void initFieldNames() {
        Object[] firstRow = rowDataList.get(0);
        columnNames = new String[firstRow.length];
        //如果是首行含有空值或特殊字符
        for (int i = 0; i < firstRow.length; i++) {
            //特殊字符替换
            String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~\\s]";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(firstRow[i].toString());
            columnNames[i] = m.replaceAll(StringUtils.EMPTY).trim();
            if (ComparatorUtils.equals(StringUtils.EMPTY, columnNames[i])) {
                columnNames[i] = Inter.getLocText("BI-Field") + (i + 1);
            }
        }
    }
}