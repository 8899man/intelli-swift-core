package com.fr.swift.config.convert.hibernate;

import com.fr.finedb.FineDBProperties;
import com.fr.stable.db.option.DBOption;
import com.fr.third.springframework.beans.factory.annotation.Autowired;
import com.fr.third.springframework.beans.factory.annotation.Value;
import com.fr.third.springframework.stereotype.Service;

import java.util.Properties;

/**
 * @author yee
 * @date 2018/6/29
 */
@Service
public class SwiftConfigProperties {
    private DBOption option;
    private boolean selfStart;

    public SwiftConfigProperties() {
        this.option = new DBOption().addRawProperty("hibernate.connection.autocommit", false);
        this.option = new DBOption().addRawProperty("hibernate.connection.provider_class", "com.fr.third.alibaba.druid.support.hibernate.DruidConnectionProvider");
    }

    public Properties getProperties() {
        return selfStart ? this.option.getProperties() : FineDBProperties.getInstance().get().getProperties();
    }

    public String getDriverClass() {
        return this.option.getDriverClass();
    }

    @Autowired
    public void setDriverClass(@Value("${swift.configDb.driver}") String driverClass) {
        this.option.setDriverClass(driverClass);
    }

    public String getDialectClass() {
        return this.option.getDialectClass();
    }

    @Autowired
    public void setDialectClass(@Value("${swift.configDb.dialect}") String dialectClass) {
        this.option.setDialectClass(dialectClass);
    }

    public String getUrl() {
        return this.option.getUrl();
    }

    @Autowired
    public void setUrl(@Value("${swift.configDb.url}") String url) {
        this.option.setUrl(url);
    }

    public String getUsername() {
        return this.option.getUsername();
    }

    @Autowired
    public void setUsername(@Value("${swift.configDb.username}") String username) {
        this.option.setUsername(username);
    }

    public String getPassword() {
        return this.option.getPassword();
    }

    @Autowired
    public void setPassword(@Value("${swift.configDb.passwd}") String password) {
        this.option.setPassword(password);
    }

    public boolean isSelfStart() {
        return selfStart;
    }

    @Autowired
    public void setSelfStart(@Value("${swift.selfStart}") boolean selfStart) {
        this.selfStart = selfStart;
    }
}
