package com.fr.bi.common.persistent.xml.writer;

import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.stable.xml.XMLPrintWriter;
import com.fr.stable.xml.XMLWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Connery on 2015/12/29.
 */
public class XMLPersistentWriter implements XMLWriter {

    private BIBeanXMLWriterWrapper beanWrapper;
    private Map<String, ArrayList<BIBeanXMLWriterWrapper>> disposedBeans;
    private Boolean isSimpleModel;


    public XMLPersistentWriter(BIBeanXMLWriterWrapper beanWrapper, Map<String, ArrayList<BIBeanXMLWriterWrapper>> disposedBeans, Boolean isSimpleModel) {
        this.beanWrapper = beanWrapper;
        this.disposedBeans = disposedBeans;
        this.isSimpleModel = isSimpleModel;

    }

    public XMLPersistentWriter(BIBeanXMLWriterWrapper beanWrapper) {
        this(beanWrapper, new HashMap<String, ArrayList<BIBeanXMLWriterWrapper>>(), false);
    }

    protected void setUUID(String UUID) {
        if (isSimpleModel) {
            beanWrapper.setUUID(UUID);
        }
    }

    @Override
    public void writeXML(XMLPrintWriter xmlPrintWriter) {
        try {
            beanWrapper.generateWriter(disposedBeans).writeValue(xmlPrintWriter);
            xmlPrintWriter.flush();
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}