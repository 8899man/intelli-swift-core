package com.fr.swift.config.convert.hibernate;

import com.fr.swift.config.json.ConfigBeanMapper;
import com.fr.swift.source.alloter.AllotRule;
import org.easymock.EasyMock;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author yee
 * @date 2018-11-29
 */
public class BaseAllotRuleConverterTest {

    private String writeString = "testString";

    @Test
    public void convertToDatabaseColumn() throws Exception {
        ConfigBeanMapper mockConfigBeanMapper = PowerMock.createMock(ConfigBeanMapper.class);
        AllotRule mockAllotRule = PowerMock.createMock(AllotRule.class);
        EasyMock.expect(mockAllotRule.getType()).andReturn(AllotRule.Type.LINE).anyTimes();
        EasyMock.expect(mockConfigBeanMapper.writeValueAsString(EasyMock.notNull())).andReturn(writeString).anyTimes();
        EasyMock.expect(mockConfigBeanMapper.writeValueAsString(EasyMock.isNull())).andThrow(new Exception("Just Test Exception")).anyTimes();
        BaseAllotRuleConverter converter = PowerMock.createMock(BaseAllotRuleConverter.class, mockConfigBeanMapper);
        PowerMock.replayAll();
        assertEquals(converter.convertToDatabaseColumn(mockAllotRule), writeString);
        assertNull(converter.convertToDatabaseColumn(null));
        PowerMock.verifyAll();
    }

    @Test
    public void convertToEntityAttribute() throws Exception {
        ConfigBeanMapper mockConfigBeanMapper = PowerMock.createMock(ConfigBeanMapper.class);
        AllotRule mockAllotRule = PowerMock.createMock(AllotRule.class);
        EasyMock.expect(mockAllotRule.getType()).andReturn(AllotRule.Type.LINE).anyTimes();
        EasyMock.expect(mockConfigBeanMapper.readValue(EasyMock.notNull(String.class), EasyMock.eq(AllotRule.class))).andReturn(mockAllotRule).anyTimes();
        EasyMock.expect(mockConfigBeanMapper.readValue(EasyMock.isNull(String.class), EasyMock.eq(AllotRule.class))).andThrow(new Exception("Just Test Exception")).anyTimes();
        BaseAllotRuleConverter converter = PowerMock.createMock(BaseAllotRuleConverter.class, mockConfigBeanMapper);
        PowerMock.replayAll();
        AllotRule rule = converter.convertToEntityAttribute(writeString);
        assertEquals(rule.getType(), AllotRule.Type.LINE);
        rule = converter.convertToEntityAttribute(null);
        assertNull(rule);
        PowerMock.verifyAll();
    }
}