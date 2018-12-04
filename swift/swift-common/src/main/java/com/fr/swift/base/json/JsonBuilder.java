package com.fr.swift.base.json;

import com.fr.swift.base.json.annotation.JsonMapper;
import com.fr.swift.base.json.mapper.BeanMapper;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yee
 * @date 2018-12-04
 */
public class JsonBuilder {
    private static final Map<Class<? extends BeanMapper>, BeanMapper> MAPPERS = new ConcurrentHashMap<Class<? extends BeanMapper>, BeanMapper>();
    private static final BeanMapper DEFAULT = defaultBeanMapper();

    private static BeanMapper defaultBeanMapper() {
        try {
            Class clazz = JsonBuilder.class.getClassLoader().loadClass("com.fr.swift.config.json.SwiftBeanMapper");
            Constructor constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (BeanMapper) constructor.newInstance();
        } catch (Exception e) {
//            SwiftLoggers.getLogger().warn(e);
            return null;
        }
    }

    public static <T> T readValue(String jsonString, Class<T> reference) throws Exception {
        BeanMapper mapper = DEFAULT;
        if (reference.isAnnotationPresent(JsonMapper.class)) {
            JsonMapper jsonMapper = reference.getAnnotation(JsonMapper.class);
            mapper = getMapper(jsonMapper);
        }
        return mapper.readValue(jsonString, reference);
    }

    public static String writeJsonString(Object o) throws Exception {
        Class reference = o.getClass();
        BeanMapper mapper = writeMapper(reference);
        if (null == mapper) {
            mapper = DEFAULT;
        }
        return mapper.writeValueAsString(o);
    }

    private static BeanMapper writeMapper(Class ref) throws Exception {
        for (Class anInterface : ref.getInterfaces()) {
            if (anInterface.isAnnotationPresent(JsonMapper.class)) {
                JsonMapper jsonMapper = (JsonMapper) anInterface.getAnnotation(JsonMapper.class);
                return getMapper(jsonMapper);
            }
        }
        Class superClass = ref.getSuperclass();
        if (superClass.equals(Object.class)) {
            return null;
        }
        return writeMapper(superClass);
    }

    private static BeanMapper getMapper(JsonMapper jsonMapper) throws Exception {
        BeanMapper mapper;
        Class<? extends BeanMapper> mapperClass = jsonMapper.value();
        if (MAPPERS.containsKey(mapperClass)) {
            mapper = MAPPERS.get(mapperClass);
        } else {
            Constructor<? extends BeanMapper> constructor = mapperClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            mapper = constructor.newInstance();
            MAPPERS.put(mapperClass, mapper);
        }
        return mapper;
    }
}
