package net.sourceforge.jnlp.security.windows;

import java.security.KeyStore;
import java.util.ArrayList;

import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.security.KeyStores;

public class WindowsKeyStoresManager {
	
	private boolean accessible;
	private WindowsKeyStoreType type;	
	
	private static final ArrayList<KeyStores.Type> ADMITTED_STORE_TYPES;
	private static final String KS_PROVIDER_NAME = "SunMSCAPI";
	
	private static final String OS_NAME = System.getProperty("os.name") == null ? "" : System.getProperty("os.name");
	
	private static final WindowsKeyStoresManager INSTANCE;
	private static final WindowsKeyStoresManager EMPTY_INSTANCE = new WindowsKeyStoresManager();
	
	static {
		//Admitted Stores - Only Root CA admitted
		ADMITTED_STORE_TYPES = new ArrayList<KeyStores.Type>();		
		ADMITTED_STORE_TYPES.add(KeyStores.Type.CA_CERTS);
		
		boolean isWindowsMachine = OS_NAME.indexOf("Windows") >= 0;
		if(isWindowsMachine) {
			//Load configurations. Only for Windows OS machines.
			WindowsKeyStoresManager windowsStoresManager = new WindowsKeyStoresManager();
			
	        DeploymentConfiguration config = JNLPRuntime.getConfiguration();
	        
	        boolean windowsRoot = Boolean.parseBoolean(config.getProperty(ConfigurationConstants.KEY_SECURITY_USE_ROOTCA_STORE_TYPE_WINDOWS_ROOT));
	        boolean windowsMy = Boolean.parseBoolean(config.getProperty(ConfigurationConstants.KEY_SECURITY_USE_ROOTCA_STORE_TYPE_WINDOWS_MY));		
			
	        windowsStoresManager.accessible = windowsRoot || windowsMy;
	        
	        if(windowsRoot && windowsMy) {
	        	windowsStoresManager.type = WindowsKeyStoreType.WINDOWS_ROOT;
	        }
	        else if(!windowsRoot && windowsMy) {
	        	windowsStoresManager.type = WindowsKeyStoreType.WINDOWS_MY;
	        }
	        else if(windowsRoot && !windowsMy) {
	        	windowsStoresManager.type = WindowsKeyStoreType.WINDOWS_ROOT;
	        }
	        else {
	        	windowsStoresManager.type = null;
	        }
	        
	        INSTANCE = windowsStoresManager;	        
		}
		else {
			INSTANCE = EMPTY_INSTANCE;
		}

		
	}

	private WindowsKeyStoresManager () {
		
	}
	
	public enum WindowsKeyStoreType {
		WINDOWS_ROOT("Windows-ROOT","Windows Trusted Root Certification Authorities"),
		WINDOWS_MY("Windows-MY","Windows Personal");
		
		private String type;
		private String description;

		WindowsKeyStoreType(String storeType, String storeDescription){
			this.type = storeType;
			this.description = storeDescription;
		}

		public String getStoreType() {
			return type;
		}
		
		
		public String getDescription() {
			return description;
		}		
		
		public static WindowsKeyStoreType getValue(String storeType) {
		    for (WindowsKeyStoreType st : WindowsKeyStoreType.values()) {
		        if (st.getStoreType().equals(storeType)) {
		            return st;
		        }
		    }
		    
		    return null;
		}		
	}
	
	public boolean isAccessible() {
		return accessible;
	}

	public WindowsKeyStoreType getType() {
		return type;
	}	
	
	public static WindowsKeyStoresManager getInfo(KeyStores.Type storeType) {		
		
		boolean admitted = ADMITTED_STORE_TYPES.contains(storeType);
		
		if(!admitted)
			return EMPTY_INSTANCE;
		else
			return INSTANCE;
		
		
	}
	
	public static boolean isWindowsStore(KeyStore ks) {
		
		if(ks!= null && ks.getProvider() != null)
			return KS_PROVIDER_NAME.equals(ks.getProvider().getName());
		else
			return false;
		
	}	
}