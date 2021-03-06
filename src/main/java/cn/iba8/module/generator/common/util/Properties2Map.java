package cn.iba8.module.generator.common.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class Properties2Map {

    public static Map<String, String> converter(String properties) {
        Properties p = new Properties();
        InputStream is = null;
        Reader reader = null;
        try {
            is = new ByteArrayInputStream(properties.getBytes());
            reader = new InputStreamReader(is);
            p.load(reader);
            return new HashMap<String, String>((Map) p);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (null != reader) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
