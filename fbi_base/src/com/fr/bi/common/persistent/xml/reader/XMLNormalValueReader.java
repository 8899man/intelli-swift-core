package com.fr.bi.common.persistent.xml.reader;

import com.finebi.cube.common.log.BILogger;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.bi.common.persistent.BIBeanHistoryManager;
import com.fr.bi.common.persistent.xml.BIIgnoreField;
import com.fr.bi.common.persistent.xml.BIXMLTag;
import com.fr.bi.stable.utils.program.BITypeUtils;
import com.fr.general.ComparatorUtils;
import com.fr.stable.xml.XMLableReader;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by Connery on 2016/1/2.
 */
public class XMLNormalValueReader extends XMLValueReader {
    public static boolean IS_IGNORED_FIELD_USABLE = true;
    private static final BILogger LOGGER = BILoggerFactory.getLogger(XMLNormalValueReader.class);

    public XMLNormalValueReader(BIBeanXMLReaderWrapper beanWrapper, Map<String, BIBeanXMLReaderWrapper> generatedBean) {
        super(beanWrapper, generatedBean);
    }

    @Override
    protected void readerContent(XMLableReader xmLableReader) {
        try {
            String fieldName = xmLableReader.getAttrAsString(BIXMLTag.FIELD_NAME, "null");
            String persistentFieldClass = xmLableReader.getAttrAsString("class", "null");

            String fieldClass = BIBeanHistoryManager.getInstance().getCurrentClassName(persistentFieldClass);

            String uuid = xmLableReader.getAttrAsString(BIXMLTag.APPEND_INFO, "null");
            BIBeanXMLReaderWrapper wrapper;

            Object fieldValue = null;
            try {
                fieldValue = beanWrapper.getOriginalValue(fieldName);
            } catch (IllegalArgumentException e) {
                LOGGER.warn(" \ncurrent class:" + beanWrapper.getBeanClass() + ",and the field:" + fieldName + " is absent");
//                throw BINonValueUtils.beyondControl(e.getMessage() + " \ncurrent class:" + beanWrapper.getBeanClass() + ",and the field:" + fieldName + " is absent", e);
            }
            if (useFieldDefaultValue(fieldValue)) {
                /**
                 * 由于使用了对象的强制构造，那么
                 * 如果属性在对象构造的时候可能被new了一个对象，那么这里fieldValue
                 * 不是null，因此不会经过反射构造，也因此该属性对象没有被注册到
                 * 已生成的集合中。需要使用getObjectWrapper，进行生成对象的注册
                 * */
                wrapper = getObjectWrapper(uuid, fieldValue);
//                new BIBeanXMLReaderWrapper(fieldValue);

            } else if (!ComparatorUtils.equals(fieldClass, "null")) {
                wrapper = getObjectWrapper(uuid, fieldClass);
            } else {
                return;
            }
            wrapper.generateReader(generatedBean).readValue(xmLableReader);
            if (wrapper.getBean() != null) {
                Field field = beanWrapper.getField(fieldName);
                if (needSetValue(field)) {
                    beanWrapper.setOriginalValue(fieldName, wrapper.getBean());
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    private boolean useFieldDefaultValue(Object fieldValue) {
        /**
         * 增加对数组类型的判断
         * 因为数组在对象构造过程中被初始化，
         * 而初始化的大小往往就是一个空数组而已，但不是null，那么就会把
         * 空数组当做初始化过的来使用，这里就会抛错。
         * 例如Info对象中有一个contents是String[]，在构造过程中
         * 会将contents赋值为String[0]，于是这里fieldValue不为null而直接
         * 进入赋值阶段，那么在空数组上赋值就产生了越界的异常。
         *
         * 所有要判断如果当前fieldValue非空但是数组的话，仍然走构造的过程。
         * 通过记录的数组大小来构造一个非空数组，然后在进行赋值。
         *
         */
        /**
         * 添加false注释掉下面方法。
         * 因为如果该fieldValue的默认类型和xml中保存的类型不同，应该优先
         * 使用XML中保存的类型
         */
        return (fieldValue != null && !BITypeUtils.isArrayType(fieldValue.getClass()) && false);
    }

    private boolean needSetValue(Field field) {
        return !isIgnore(field);
    }

    /*
    * author Kary
    * BI-6550 4.0升级至4.02bug
    * field被删除时应该直接ignore
    * */
    private boolean isIgnore(Field field) {
        if (field != null) {
            return IS_IGNORED_FIELD_USABLE && field.isAnnotationPresent(BIIgnoreField.class);
        } else {
            return true;
        }
    }
}