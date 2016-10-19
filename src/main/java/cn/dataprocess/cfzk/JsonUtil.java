/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.dataprocess.cfzk;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.zookeeper.KeeperException;

/**
 *
 * @author lfh
 */
public class JsonUtil {
    
    public static String toJsonString(Object sourceObject) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String ret = mapper.writeValueAsString(sourceObject);
        mapper = null;
        return ret;
    }
    
    public static <K> K toJavaBean(String content, Class<K> valueType) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false);
         mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        K ret = mapper.readValue(content, valueType);
        mapper = null;
        return ret;
    }
    

}
