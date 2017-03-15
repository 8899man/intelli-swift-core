package com.fr.bi.web.conf.services.datalink;

import com.fr.bi.stable.data.db.DataLinkInformation;
import com.fr.bi.stable.utils.BIDBUtils;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.bi.web.conf.AbstractBIConfigureAction;
import com.fr.bi.web.conf.services.datalink.data.BIConnectionTestUtils;
import com.fr.data.core.DataCoreUtils;
import com.fr.data.impl.JDBCDatabaseConnection;
import com.fr.general.ComparatorUtils;
import com.fr.json.JSONArray;
import com.fr.json.JSONObject;
import com.fr.web.utils.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * User: Sheldon
 * Date: 13-10-30
 * Time: 下午2:47
 * describe:  test dataLink
 */
public class BITestDataLinkAction extends AbstractBIConfigureAction {

    @Override
    protected void actionCMDPrivilegePassed(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String linkData = WebUtils.getHTTPRequestParameter(req, "linkData");
        BIConnectionTestUtils utils = new BIConnectionTestUtils();
        JSONObject jo = utils.processConnectionTest(linkData);
        if(ComparatorUtils.equals(jo.getString("success"), true)){
            try {
                com.fr.data.impl.Connection dbc = fetchConnection(linkData);
                JSONArray ja = new JSONArray();
                String[] schemas = DataCoreUtils.getDatabaseSchema(dbc);
                for(int i = 0; i < schemas.length; i++){
                    ja.put(schemas[i]);
                }
                jo.put("schemas", ja);
            } catch (Exception e) {
                BILoggerFactory.getLogger().error(e.getMessage(), e);
            }
        }
        WebUtils.printAsJSON(res, jo);
    }

    private com.fr.data.impl.Connection fetchConnection(String configData) throws Exception {
        JSONObject linkDataJo = new JSONObject(configData);
        DataLinkInformation dl = new DataLinkInformation();
        dl.parseJSON(linkDataJo);
        com.fr.data.impl.Connection c = dl.createDatabaseConnection();
        return c;
    }

    @Override
    public String getCMD() {
        return "test_data_link";
    }
}