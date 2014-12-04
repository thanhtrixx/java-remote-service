package com.l3v.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Trí
 *
 */
public class AppConfig {
	// AppConfig variable
	private static final Logger log = LogManager.getLogger(AppConfig.class);
	private static Properties properties = new Properties();
	private static String propertiesFilePath = "configs/config.properties";
	// Port properties
	public static int remotingPort;
	public static int remotedPort;
	public static int remotingScreenPort;
	public static int remotedScreenPort;
	public static int remotingMKPort;
	public static int remotedMKPort;
	// Max buffer size. Max is: 65536
	public static int maxBufferSize;
	// Max size of package
	public static int maxPacketSize;

	public static int numberConnection;
	// Number active connection.
	public static int activeRemote;
	// Max size of forwarder queue
	public static int maxQueueSize;
	// Time out to add and get data from queue
	public static int queueTimeOut;

	static {
		// Load properties
		log.info("Load properties from file: {}", propertiesFilePath);
		try (InputStream is = new FileInputStream(propertiesFilePath)) {
			properties.load(is);
		} catch (IOException e) {
			log.error("Load error {" + propertiesFilePath + "}", e);
		}

		remotingPort = getIntProperties("remotingPort", 5435);
		remotedPort = getIntProperties("remotedPort", 5436);
		remotingScreenPort = getIntProperties("remotingScreenPort", 5437);
		remotedScreenPort = getIntProperties("remotedScreenPort", 5438);
		remotingMKPort = getIntProperties("remotingMKPort", 5439);
		remotedMKPort = getIntProperties("remotedMKPort", 5440);

		// 64KB
		maxBufferSize = getIntProperties("maxBufferSize", 65536);
		// 50MB
		maxPacketSize = getIntProperties("maxPacketSize", 10 * 1024 * 1024);

		numberConnection = getIntProperties("numberConnection", 150);
		activeRemote = getIntProperties("activeRemote", 20);

		maxQueueSize = getIntProperties("maxQueueSize", 25);
		queueTimeOut = getIntProperties("queueTimeOut", 15000);
	}

	private static int getIntProperties(String propertyName, int defaultVal) {
		int result;
		try {
			result = Integer.parseInt(properties.getProperty(propertyName));
		} catch (NumberFormatException e) {
			result = defaultVal;
			log.warn("Load [{}] error, load default value [{}]", propertyName, defaultVal);
		}
		log.info("Load [{}] = [{}]", propertyName, result);
		return result;
	}
}
