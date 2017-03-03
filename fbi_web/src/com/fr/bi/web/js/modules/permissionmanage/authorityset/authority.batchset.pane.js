/**
 * Created by Young's on 2016/5/16.
 */
BI.AuthorityBatchSetPane = BI.inherit(BI.Widget, {

    _constants: {
        SHOW_EMPTY: 1,
        SHOW_PANE: 2,
        SHOW_SEARCHER: 3
    },

    _defaultConfig: function () {
        return BI.extend(BI.AuthorityBatchSetPane.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-authority-batch-set-pane"
        })
    },

    _init: function () {
        BI.AuthorityBatchSetPane.superclass._init.apply(this, arguments);
        this.packageName = BI.createWidget({
            type: "bi.label",
            text: "",
            height: 30,
            cls: "package-title"
        });

        this.tab = BI.createWidget({
            type: "bi.tab",
            tab: "",
            direction: "custom",
            cardCreator: BI.bind(this._createTab, this)
        });
        this.tab.setSelect(this._constants.SHOW_EMPTY);
        BI.createWidget({
            type: "bi.vtape",
            element: this.element,
            items: [{
                el: {
                    type: "bi.left",
                    cls: "authority-set-title",
                    items: [{
                        type: "bi.label",
                        text: BI.i18nText("BI-Muti_Auth_Set"),
                        height: 30,
                        cls: "package-title"
                    }, this.packageName],
                    vgap: 5,
                    hgap: 2
                },
                height: 40
            }, {
                el: this.tab,
                height: "fill"
            }],
            hgap: 20
        });
    },

    _createTab: function (v) {
        var self = this;
        switch (v) {
            case this._constants.SHOW_EMPTY:
                this.addRolePane = BI.createWidget({
                    type: "bi.authority_batch_add_role_empty_pane"
                });
                this.addRolePane.on(BI.AuthorityBatchAddRoleEmptyPane.EVENT_CHANGE, function () {
                    self._showRoleSearcherPane();
                });
                return this.addRolePane;
            case this._constants.SHOW_PANE:
                this.roles = BI.createWidget({
                    type: "bi.authority_batch_add_role_pane"
                });
                this.roles.on(BI.AuthorityBatchAddRolePane.EVENT_ADD_ROLE, function () {
                    self._showRoleSearcherPane();
                });
                this.roles.on(BI.AuthorityBatchAddRolePane.EVENT_SAVE, function () {
                    self._updatePackageRoles(self.roles.getRoles());
                    self.fireEvent(BI.AuthorityBatchSetPane.EVENT_CHANGE);
                });
                this.roles.on(BI.AuthorityBatchAddRolePane.EVENT_CANCEL, function () {
                    self.roles.clearRoles();
                    self._showEmptyPane();
                });
                return BI.createWidget({
                    type: "bi.absolute",
                    items: [{
                        el: this.roles,
                        top: 0,
                        left: -220,
                        right: 0,
                        bottom: 0
                    }]
                });

            case this._constants.SHOW_SEARCHER:
                this.searcher = BI.createWidget({
                    type: "bi.batch_add_role_searcher"
                });
                this.searcher.on(BI.BatchAddRoleSearcher.EVENT_CANCEL, function () {
                    self.tab.setSelect(self._constants.SHOW_PANE);
                });
                this.searcher.on(BI.BatchAddRoleSearcher.EVENT_SAVE, function (roles) {
                    self.tab.setSelect(self._constants.SHOW_PANE);
                    self.roles.populatePackageTree(self.packageIds);
                    self.roles.addRoles(roles);
                });
                return BI.createWidget({
                    type: "bi.absolute",
                    items: [{
                        el: this.searcher,
                        top: 0,
                        left: -220,
                        bottom: 0,
                        right: 0
                    }]
                })
        }
    },

    _showRoleSearcherPane: function () {
        this.tab.setSelect(this._constants.SHOW_SEARCHER);
        BI.isNotNull(this.searcher) && this.searcher.populate(this.packageIds, BI.isNotNull(this.roles) ? this.roles.getRoles() : []);
        BI.isNotNull(this.roles) && this.roles.populatePackageTree(this.packageIds);
    },

    _showEmptyPane: function () {
        this.tab.setSelect(this._constants.SHOW_EMPTY);
    },

    _updatePackageRoles: function (roles) {
        var authSettings = Data.SharingPool.get("authority_settings");
        var packagesAuth = authSettings.packages_auth;
        var pAuth = packagesAuth[this.packageId] || [];
        var newPAuth = pAuth.concat(roles);
        BI.each(this.packageIds, function (i, pId) {
            packagesAuth[pId] = newPAuth;
        });
        Data.SharingPool.put("authority_settings", authSettings);
        BI.Utils.savePackageAuthority({
            package_ids: this.packageIds,
            roles: newPAuth
        }, BI.emptyFn);
    },

    setValue: function (v) {
        //去掉分组名（分组里无业务包的）
        var self = this;
        var allPackIds = BI.Utils.getAllPackageIDs4Conf();
        this.packageIds = [];
        BI.each(v, function (i, pId) {
            allPackIds.contains(pId) && self.packageIds.push(pId);
        });
        this.packageName.setText(BI.isNotNull(this.packageIds) ? BI.i18nText("BI-N_Packages", BI.uniq(this.packageIds).length) : "");
        this.tab.setSelect(this._constants.SHOW_EMPTY);
        BI.isNotNull(this.addRolePane) && this.addRolePane.setValue(this.packageIds);
    },

    populate: function () {

    }
});
BI.AuthorityBatchSetPane.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut("bi.authority_batch_set_pane", BI.AuthorityBatchSetPane);