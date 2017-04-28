package com.fr.bi.test;

import com.fr.bi.stable.utils.program.BIPhoneticismUtils;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.bi.stable.utils.CubeBaseUtils;
import com.fr.general.ComparatorUtils;
import com.fr.json.JSONArray;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;
import com.fr.stable.StableUtils;
import com.fr.web.core.ActionNoSessionCMD;
import com.fr.web.utils.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guy on 14-10-18.
 */
public class BIGetTreeSearchNodeAction extends ActionNoSessionCMD {

    private static final int DEFAULTROW = 10;
    private static final int CIRCLECOUNT = 20;
    private static final int CHILDCOUNT = 20;
    private static final int RANDOMCIRCLETIME = 3;
    private int TIMES =2;

    /**
     * 方法
     *
     * @param req 参数
     * @param res 参数
     * @throws Exception
     */
    @Override
    public void actionCMD(HttpServletRequest req, HttpServletResponse res) throws Exception {

//        String timesString = WebUtils.getHTTPRequestParameter(req, "times");//第几次加载
        String floorsString = WebUtils.getHTTPRequestParameter(req, "floors");

        String keyword = WebUtils.getHTTPRequestParameter(req, "keyword");
        keyword = keyword == null ? "" : keyword;

        String lastSearchValue = WebUtils.getHTTPRequestParameter(req, "lastSearchValue");

        String selectedValuesString = WebUtils.getHTTPRequestParameter(req, "selectedValues");
//        int times = 1;
//        if (timesString != null) {
//            times = Integer.parseInt(timesString);
//        }
        int floors = 1;
        if (floorsString != null) {
            floors = Integer.parseInt(floorsString);
        }

        JSONObject selectedValues = new JSONObject();
        if (selectedValuesString != null) {
            selectedValues = new JSONObject(selectedValuesString);
        }

        JSONArray result = new JSONArray();
        List<String> output = search(result, floors, keyword, selectedValues, lastSearchValue);

        JSONObject jo = new JSONObject();
        jo.put("hasNext", output.size() > getRows());
        jo.put("items", result);
        if (!output.isEmpty()) {
            jo.put("lastSearchValue", output.get(output.size() - 1));
        }
        WebUtils.printAsJSON(res, jo);
    }

    private List<String> search(JSONArray result, int deep, String keyword, JSONObject selectedValues, String lastSearchValue) throws Exception {

        String[] values = new String[0];
        List<String> vl = randomData(values, -1);

        int i, start = vl.size();
        if (lastSearchValue != null) {
            for (int j = 0, len = vl.size(); j < len; j++) {
                if (ComparatorUtils.equals(vl.get(j), lastSearchValue)) {
                    start = j + 1;
                }
            }
        } else {
            start = 0;
        }

        List<String> output = new ArrayList<String>();

        List threadList = new ArrayList();
        for (i = start; i < vl.size() && i < start + getRows(); i++) {
            threadList.add(new NodeSearch(result, vl.get(i), selectedValues, keyword, String.valueOf(i + 1), deep, output));
        }

        try {
            CubeBaseUtils.invokeCalculatorThreads(threadList);
        } catch (InterruptedException e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }

        for (int len = vl.size(); i < len; i++) {
            if (output.size() < getRows()) {
                new NodeSearch(result, vl.get(i), selectedValues, keyword, String.valueOf(i + 1), deep, output).call();
            } else if (output.size() == getRows()) {
                new NodeSearch(null, vl.get(i), selectedValues, keyword, String.valueOf(i + 1), deep, output).call();
            } else {
                break;
            }
        }
        return output;
    }

    private class Status {
        public boolean finded = false;
        public boolean checked = false;

        public Status(boolean finded, boolean checked) {
            this.finded = finded;
            this.checked = checked;
        }
    }

    private class NodeSearch implements java.util.concurrent.Callable {
        private JSONArray result;
        private String parentValue;
        private JSONObject selectedValues;
        private String keyword;
        private String id;
        private int deep;
        private List<String> output;

        private NodeSearch(JSONArray result,
                           String parentValue, JSONObject selectedValues,
                           String keyword, String id, int deep, List<String> output) {
            this.result = result;
            this.parentValue = parentValue;
            this.selectedValues = selectedValues;
            this.keyword = keyword;
            this.id = id;
            this.deep = deep;
            this.output = output;
        }

        @Override
        public Object call() throws Exception {
            if (search(1, new String[0], parentValue, id, false).finded) {
                synchronized (output) {
                    output.add(parentValue);
                }
            }
            return null;
        }

        public Status search(int deep, String[] parents, String value, String id, boolean isAllSelected) throws JSONException {

            if (isMatch(value)) {
                boolean checked = isAllSelected || isSelected(parents, value);
                createOneJson(parents, getHTMLName(value), value, getPID(id), id, deep != this.deep, false, checked, !isAllSelected && isHalf(parents, value), true);
                return new Status(true, checked);
            }
            if (deep >= this.deep) {
                return new Status(false, false);
            }
            String[] newParents = new String[parents.length + 1];
            for (int i = 0; i < parents.length; i++) {
                newParents[i] = parents[i];
            }
            newParents[parents.length] = value;
            List<String> vl = randomData(newParents);

            boolean can = false;
            boolean checked = false;

            boolean isCurAllSelected = isAllSelected || isAllSelected(parents, value);
            for (int i = 0, len = vl.size(); i < len; i++) {
                Status status = search(deep + 1, newParents, vl.get(i), id + "_" + (i + 1), isCurAllSelected);
                if (status.checked) {
                    checked = true;
                }
                if (status.finded) {
                    can = true;
                }
            }
            if (can) {
                checked = isCurAllSelected || (isSelected(parents, value) && checked);
                createOneJson(parents, value, value, getPID(id), id, true, true, checked, false, false);
            }
            return new Status(can, checked);
        }

        public String getPID(String id) {
            String pId = "0";
            if (id.lastIndexOf('_') > -1) {
                pId = id.substring(0, id.lastIndexOf('_'));
            }
            return pId;
        }

        public String getHTMLName(String name) {
            return name;
//            String py = BIPhoneticismUtils.getPingYin(name);
//            String kPY = BIPhoneticismUtils.getPingYin(this.keyword);
//            int start = py.indexOf(kPY);
//            int end = start + kPY.length();
//            String A = name.substring(0, start);
//            String B = name.substring(start, start + kPY.length());
//            String C = name.substring(end);
//            return A + "<font color='#FF6600'>" + B + "</font>" + C;
        }

        /**
         * 是否为选中状态
         *
         * @param value 尾值
         * @return 是否
         */
        public boolean isSelected(String[] parents, String value) {

            JSONObject find = findSelectedObj(parents);
            if (find == null) {
                return false;
            }
            JSONArray names = find.names();
            if (names == null) {
                return false;
            }
            for (int i = 0; i < names.length(); i++) {
                String name = names.optString(i);
                if (ComparatorUtils.equals(name, value)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 是否为全选状态
         *
         * @param value 尾值
         * @return 是否
         */
        public boolean isAllSelected(String[] parents, String value) {

            JSONObject find = findSelectedObj(parents);
            if (find == null) {
                return false;
            }
            JSONArray names = find.names();
            if (names == null) {
                return false;
            }
            for (int i = 0; i < names.length(); i++) {
                String name = names.optString(i);
                if (ComparatorUtils.equals(name, value)) {
                    JSONObject nextObj = find.optJSONObject(name);
                    if (nextObj == null || nextObj.names() == null || nextObj.names().length() == 0) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean isHalf(String[] parents, String value) {
            JSONObject find = findSelectedObj(parents);
            if (find == null) {
                return false;
            }
            JSONArray names = find.names();

            if (names == null) {
                return false;
            }
            for (int i = 0; i < names.length(); i++) {
                String name = names.optString(i);
                if (ComparatorUtils.equals(name, value)) {
                    JSONObject nextObj = find.optJSONObject(name);
                    if (nextObj != null && nextObj.names() != null && nextObj.names().length() > 0) {
                        return true;
                    }
                }
            }
            return false;
        }

        public JSONObject findSelectedObj(String[] parents) {
            JSONObject find = selectedValues;
            if (find == null) {
                return null;
            }
            for (int i = 0; i < parents.length; i++) {
                find = find.optJSONObject(parents[i]);
                if (find == null) {
                    return null;
                }
            }
            return find;
        }

        public boolean isMatch(String name) {
            String py = BIPhoneticismUtils.getPingYin(name);
            if (name.toUpperCase().contains(keyword.toUpperCase())
                    || py.toUpperCase().contains(keyword.toUpperCase())) {
                return true;
            }
            return false;
        }

        /**
         * {id:"",pId:"",name:"",checked:true,open:true,isParent:true}
         *
         * @param parents
         * @param name
         * @param pId
         * @param id
         * @param isParent
         * @param isOpen
         * @param checked
         * @throws com.fr.json.JSONException
         */
        //flag标记是否是搜到的点
        public void createOneJson(String[] parents, String name, String title, String pId, String id,
                                  boolean isParent, boolean isOpen, boolean checked, boolean half, boolean flag) throws JSONException {
            if (result != null) {
                synchronized (result) {
                    JSONArray parentJson = new JSONArray();
                    for (int i = 0; i < parents.length; i++) {
                        parentJson.put(parents[i]);
                    }
                    JSONObject obj = new JSONObject();
//                    obj.put("parentValues", parentJson);
                    obj.put("id", id);
                    obj.put("pId", pId);
                    obj.put("text", name);
                    obj.put("title", title);
                    obj.put("isParent", isParent);
                    obj.put("open", isOpen);
                    obj.put("checked", checked);
                    obj.put("halfCheck", half);
                    obj.put("flag", flag);
                    result.put(obj);
                }
            }
        }
    }


    private List<String> randomData(String[] parentValues, int times) {
        List<String> res = new ArrayList<String>();
        if (times <= 0) {
            String v = StableUtils.join(parentValues, ",");
            for (int i = 0; i < CIRCLECOUNT; i++) {
                res.add(v + "_" + i);
            }
            return res;
        }
        if (times < RANDOMCIRCLETIME) {
            String v = StableUtils.join(parentValues, ",");
            for (int i = (times - 1) * 10; i < times * DEFAULTROW; i++) {
                res.add(v + "_" + i);
            }
        }
        return res;
    }

    private List<String> randomData(String[] parentValues) {
        List<String> res = new ArrayList<String>();
        String v = StableUtils.join(parentValues, ",");
        for (int i = 0; i < CIRCLECOUNT; i++) {
            res.add(v + "_" + i);
        }
        return res;
    }

    private boolean hasNext(int times) {
        return times < TIMES;
    }

    private int getChildCount(String[] values) {
        return CHILDCOUNT;
    }


    private int getRows() {
        return DEFAULTROW;
    }

    @Override
    public String getCMD() {
        return "get_search_tree_node";
    }
}