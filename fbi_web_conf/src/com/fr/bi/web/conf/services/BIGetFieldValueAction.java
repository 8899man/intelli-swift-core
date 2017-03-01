package com.fr.bi.web.conf.services;

import com.finebi.cube.api.BICubeManager;
import com.fr.bi.conf.data.source.TableSourceFactory;
import com.fr.bi.stable.constant.BIJSONConstant;
import com.fr.bi.stable.constant.DBConstant;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.utils.program.BIJsonUtils;
import com.fr.bi.web.conf.AbstractBIConfigureAction;
import com.fr.fs.web.service.ServiceUtils;
import com.fr.general.ComparatorUtils;
import com.fr.general.GeneralUtils;
import com.fr.json.JSONArray;
import com.fr.json.JSONObject;
import com.fr.stable.StringUtils;
import com.fr.web.utils.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by User on 2015/12/2.
 */
public class BIGetFieldValueAction extends AbstractBIConfigureAction {
    @Override
    protected void actionCMDPrivilegePassed(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String fieldName = WebUtils.getHTTPRequestParameter(req, "field");
        if(StringUtils.isEmpty(fieldName)){
            WebUtils.printAsJSON(res, new JSONObject());
            return;
        }
        long userId = ServiceUtils.getCurrentUserID(req);
        CubeTableSource source = TableSourceFactory.createTableSource(new JSONObject(WebUtils.getHTTPRequestParameter(req, "table")), userId);
        Set initialSet = source.getFieldDistinctNewestValues(fieldName, BICubeManager.getInstance().fetchCubeLoader(userId), userId);
        Set set = new HashSet();
        for (Object s: initialSet) {
            if(s!=null){
                set.add(GeneralUtils.objectToString(s));
            }
        }
        String filterConfigString = WebUtils.getHTTPRequestParameter(req, "filterConfig");
        String keyword = null;
        List<String> selected_value = new ArrayList<String>();
        int times = -1;
        int type = -2;
        if (filterConfigString != null) {
            JSONObject filterConfig = new JSONObject(filterConfigString);
            if(filterConfig.has("keyword")){
                keyword = filterConfig.optString("keyword");
            }
            if(filterConfig.has("times")){
                times = filterConfig.optInt("times");
            }
            if(filterConfig.has("type")){
                type = filterConfig.optInt("type");
            }
            String selectedValueString = filterConfig.optString("selected_value");
            if(selectedValueString != null && StringUtils.isNotEmpty(selectedValueString)){
                JSONArray selectedValueArray = new JSONArray(selectedValueString);
                selected_value = Arrays.asList(BIJsonUtils.jsonArray2StringArray(selectedValueArray));
            }
            set = getSearchResult(set, keyword, selected_value);
        }
        if(type == DBConstant.REQ_DATA_TYPE.REQ_GET_DATA_LENGTH){
            JSONObject jo = new JSONObject();
            jo.put(BIJSONConstant.JSON_KEYS.VALUE, set.size());
            WebUtils.printAsJSON(res, jo);
            return;
        }
        if(type == DBConstant.REQ_DATA_TYPE.REQ_GET_ALL_DATA){
            JSONArray ja = new JSONArray(set);
            JSONObject jo = new JSONObject();
            jo.put(BIJSONConstant.JSON_KEYS.VALUE, ja);
            WebUtils.printAsJSON(res, jo);
            return;
        }
        JSONArray ja = new JSONArray(getItemsByTimes(set, times));
        JSONObject jo = new JSONObject();
        jo.put(BIJSONConstant.JSON_KEYS.VALUE, ja);
        jo.put(BIJSONConstant.JSON_KEYS.HAS_NEXT, hasNextByTimes(set, times));
        WebUtils.printAsJSON(res, jo);
    }

    private boolean hasNextByTimes(Set items, int times) {
        return times * 100 < items.size();
    }

    private Set getItemsByTimes(Set items, int times) {
        if(times == -1){
            return items;
        }
        Set result = new HashSet();
        int i = 0;
        Iterator it = items.iterator();
        while (it.hasNext() && i < (times - 1) * 100){
            it.next();
            ++i;
        }
        while (it.hasNext() && i < times * 100){
            result.add(it.next());
            i++;
        }
        return result;
    }

    private Set getSearchResult(Set source, String keyword, List<String> selectedValue){
        if(keyword != null && StringUtils.isNotEmpty(keyword)){
            Set<String> find = new HashSet<String>();
            Set<String> match = new HashSet<String>();

            keyword = keyword.toUpperCase();
            Iterator it = source.iterator();
            while (it.hasNext()){
                String str = String.valueOf(it.next());
                if(str.contains(keyword) && !selectedValue.contains(str)){
                    if(ComparatorUtils.equals(keyword, str)){
                        match.add(str);
                    }else{
                        find.add(str);
                    }
                }
            }
            match.addAll(find);
            return match;
        }
        return source;
    }

    @Override
    public String getCMD() {
        return "get_field_value";
    }
}