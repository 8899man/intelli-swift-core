/**
 * Created by Young's on 2016/5/30.
 */
BI.AllReports = BI.inherit(BI.Widget, {

    _constant: {
        SHOW_FILTER: 1,
        HIDE_FILTER: 2
    },

    _defaultConfig: function () {
        return BI.extend(BI.AllReports.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-all-reports"
        })
    },

    _init: function () {
        BI.AllReports.superclass._init.apply(this, arguments);
        var self = this;
        this.hangout = BI.createWidget({
            type: "bi.label",
            text: "",
            height: 40
        });
        this.filterPane = BI.createWidget({
            type: "bi.all_reports_filter"
        });
        this.filterPane.on(BI.AllReportsFilter.EVENT_CHANGE, function(){
            self._getReportFilterResult();
        });
        this.wrapper = BI.createWidget({
            type: "bi.vtape",
            element: this.element,
            items: [{
                el: BI.createWidget({
                    type: "bi.left",
                    cls: "hangout-count",
                    items: [{
                        type: "bi.label",
                        text: BI.i18nText("BI-Current_Apply_For_Hangout"),
                        height: 40
                    }, this.hangout]
                }),
                height: 40
            }, {
                el: this._createPushBar(),
                height: 40
            }, {
                el: this.filterPane,
                height: 92
            }, {
                el: BI.createWidget(),
                height: 20
            }, {
                el: this._createReports()
            }, {
                el: BI.createWidget(),
                height: 20
            }],
            hgap: 20
        })
    },

    _createPushBar: function () {
        var self = this;
        this.reportsCount = BI.createWidget({
            type: "bi.label",
            cls: "",
            text: ""
        });
        var pushButton = BI.createWidget({
            type: "bi.icon_change_button",
            cls: "push-button",
            width: 16,
            height: 16
        });
        pushButton.setIcon("report-filter-close-font");
        pushButton.on(BI.IconChangeButton.EVENT_CHANGE, function () {
            var filterShow = self.filterPane.isVisible();
            this.setIcon(filterShow ? "report-filter-open-font" : "report-filter-close-font");
            self.filterPane.setVisible(!filterShow);
            self.wrapper.attr("items")[2].height = filterShow ? 0 : 90;
            self.wrapper.resize();
        });
        return BI.createWidget({
            type: "bi.center_adapt",
            cls: "push-bar",
            items: [{
                type: "bi.left_right_vertical_adapt",
                items: {
                    left: [{
                        type: "bi.label",
                        text: BI.i18nText("BI-Filter_Report"),
                        cls: "filter-report-label",
                        height: 40,
                        width: 70
                    }, this.reportsCount],
                    right: [pushButton]
                },
                width: 130,
                height: 40
            }],
            height: 40
        });
    },

    _createReports: function () {
        this.reportGroup = BI.createWidget({
            type: "bi.all_reports_group",
            cls: "all-reports"
        });
        return this.reportGroup;
    },

    //过滤reports
    _getReportFilterResult: function(){
        var self = this;
        var filter = this.filterPane.getValue();
        var departs = filter.departs,
            roles = filter.roles,
            users = filter.users,
            status = filter.status,
            start = filter.start,
            end = filter.end;
        //简单一点的办法就是都过一遍，可能效率太低
        var reports = [];
        BI.each(this.reports, function(i, report){
            if(departs.length > 0){
                var currDeparts = self._getDepartsByUserId(report.createBy);
                var isContain = false;
                BI.some(currDeparts, function(j, d){
                     if(departs.contains(d)){
                         return isContain = true;
                     }
                });
                if(isContain === false) {
                    return;
                }
            }
            if(roles.length > 0) {
                var currRoles = self._getRolesByUserId(report.createBy);
                var isContain = false;
                BI.some(currRoles, function(j, r){
                    if(roles.contains(r)){
                        return isContain = true;
                    }
                });
                if(isContain === false) {
                    return;
                }
            }
            if(users.length > 0) {
                var isContain = false;
                BI.some(self.users, function(j, user){
                    if(user.contains(user.id)){
                        return isContain = true;
                    }
                });
                if(isContain === false){
                    return;
                }
            }
            //TODO 状态
            if(BI.isNotNull(start)) {
                if(report.lastModify < start){
                    return;
                }
            }
            if(BI.isNotNull(end)){
                if(report.lastModify > end){
                    return;
                }
            }
            reports.push(report);
        });
        this.reportGroup.populate(reports, self.roles, self.users);
    },

    _getDepartsByUserId: function(userId){
        var departs = [];
        BI.each(this.roles, function(i, role){
             if(role.users.contains(userId)) {
                 departs.push(role.departmentid);
             }
        });
        return departs;
    },

    _getRolesByUserId: function(userId) {
        var roles = [];
        BI.each(this.roles, function(i, role){
            if(role.users.contains(userId)) {
                roles.push(role.id);
            }
        });
        return roles;
    },

    populate: function () {
        var self = this;
        var mask = BI.createWidget({
            type: "bi.loading_mask",
            masker: BICst.BODY_ELEMENT,
            text: BI.i18nText("BI-Loading")
        });
        BI.Utils.getAllReportsData(function (data) {
            self.departs = data.departs;
            self.roles = data.roles;
            self.users = data.users;
            self.reports = data.reports;
            self.filterPane.populate(self.departs, self.roles, self.users);
            self.reportGroup.populate(self.reports, self.roles, self.users);
            mask.destroy();
        });
    }
});
BI.extend(BI.AllReports, {
    SHOW_LIST: 1,
    SHOW_CARD: 2
});
$.shortcut("bi.all_reports", BI.AllReports);