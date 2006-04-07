package net.q00p.bots.util;

import java.util.HashMap;
import java.util.Map;

public class ServiceRegistrar {
	static ServiceRegistrar singleton = null;
	Map<Class, Object> services = new HashMap<Class, Object>();
	
	static public ServiceRegistrar get() {
		if (singleton == null) singleton = new ServiceRegistrar();
		return singleton;
	}
	
	
	public <T> void register(Class<T> clazz, T instance) {
		services.put(clazz, instance);
	}

	public <T> T service(Class<T> clazz) {
		return (T) services.get(clazz);
	}
}
