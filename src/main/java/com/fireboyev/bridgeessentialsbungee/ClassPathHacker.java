package com.fireboyev.bridgeessentialsbungee;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class ClassPathHacker {
	/**
	 * Parameters of the method to add an URL to the System classes.
	 */
	private static final Method ADD_URL_METHOD;

	static {
		try {
			ADD_URL_METHOD = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			ADD_URL_METHOD.setAccessible(true);
		} catch (NoSuchMethodException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private final URLClassLoader classLoader;

	public ClassPathHacker(Object plugin) throws IllegalStateException {
		ClassLoader classLoader = plugin.getClass().getClassLoader();
		if (classLoader instanceof URLClassLoader) {
			this.classLoader = (URLClassLoader) classLoader;
		} else {
			throw new IllegalStateException("ClassLoader is not instance of URLClassLoader");
		}
	}

	public void loadJar(Path file) {
		try {
			ADD_URL_METHOD.invoke(this.classLoader, file.toUri().toURL());
			
		} catch (IllegalAccessException | InvocationTargetException | MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
