package util;


import java.io.*;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * @author TianGuogong
 * 2011-12-31
 */
public class SystemMessage
{
//    public static final String ABSOLUTE_PATH= ClassLoader.getSystemResource("").toString().replace("file:/", "");
//    private static final String BUNDLE_NAME = ABSOLUTE_PATH+"system.properties"; //$NON-NLS-1$

    public static final String ABSOLUTE_PATH= System.getProperty("user.dir");
    private static final String BUNDLE_NAME = ABSOLUTE_PATH+"\\system.properties"; //$NON-NLS-1$
    private static ResourceBundle RESOURCE_BUNDLE = SystemMessage.init();
    private SystemMessage() {
    }
public static ResourceBundle init(){
   
    InputStream in=null;
    try {
        System.out.println("-----system.properties  is -----"+BUNDLE_NAME);
        if(RESOURCE_BUNDLE==null){
            if(!new File(BUNDLE_NAME).exists()){
                System.out.println("-----system.properties  is not exists-----");
            }
            in = new BufferedInputStream(new FileInputStream(BUNDLE_NAME));
            RESOURCE_BUNDLE = new PropertyResourceBundle(in);
        }
    }
    catch (Exception e) {
        e.printStackTrace();
    }finally{
        if(in!=null){
            try {
                in.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    return RESOURCE_BUNDLE;
}
    /**
     * get the value from the properties file
     * 
     * @param key
     *            the key in the properties file
     * @return
     */
    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
    
    
    /**获取非-1的整数配置项
     * @param key
     * @return
     */
    public static int getInt(String key) {
        try {
            String  value = RESOURCE_BUNDLE.getString(key);
            return Integer.parseInt(value);
        } catch (MissingResourceException e) {
            return -1;
        }
    }
    
    
}
