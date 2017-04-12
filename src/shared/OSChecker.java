package shared;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class OSChecker {
	public static boolean isWindows       = false;
	public static boolean isMac			  = false;
	public static boolean isLinux         = false;
	public static boolean isHpUnix        = false;
	public static boolean isPiUnix        = false;
	public static boolean isSolaris       = false;
	public static boolean isSunOS         = false;
	public static boolean archDataModel32 = false;
	public static boolean archDataModel64 = false;

	static {
	    final String os = System.getProperty("os.name").toLowerCase();
	    if (os.indexOf("windows") >= 0) {
	        isWindows = true;
	    }
	    if (os.indexOf("mac") >= 0) {
	    	isMac = true;
	    }
	    if (os.indexOf("linux") >= 0) {
	        isLinux = true;
	    }
	    if (os.indexOf("hp-ux") >= 0) {
	        isHpUnix = true;
	    }
	    if (os.indexOf("hpux") >= 0) {
	        isHpUnix = true;
	    }
	    if (os.indexOf("solaris") >= 0) {
	        isSolaris = true;
	    }
	    if (os.indexOf("sunos") >= 0) {
	        isSunOS = true;
	    }
	    if (System.getProperty("sun.arch.data.model").equals("32")) {
	        archDataModel32 = true;
	    }
	    if (System.getProperty("sun.arch.data.model").equals("64")) {
	        archDataModel64 = true;
	    }
	    if (isLinux) {
	        final File file = new File("/etc", "os-release");
	        try (FileInputStream fis = new FileInputStream(file);
	             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis))) {
	            String string;
	            while ((string = bufferedReader.readLine()) != null) {
	                if (string.toLowerCase().contains("raspbian")) {
	                    if (string.toLowerCase().contains("name")) {
	                        isPiUnix = true;
	                        break;
	                    }
	                }
	                else {
	                	System.out.println(string);
	                }
	            }
	        } catch (final Exception e) {
	            e.printStackTrace();
	        }
	    }
	}

}
