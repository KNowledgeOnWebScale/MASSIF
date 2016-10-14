package massif.service.notification;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

public class BundleDummy implements Bundle{
	
	private String path;

	public BundleDummy(String path){
		this.path	= path;
	}

	@Override
	public int compareTo(Bundle o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getState() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void start(int options) throws BundleException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() throws BundleException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop(int options) throws BundleException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() throws BundleException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(InputStream input) throws BundleException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update() throws BundleException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void uninstall() throws BundleException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Dictionary<String, String> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getBundleId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceReference<?>[] getRegisteredServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceReference<?>[] getServicesInUse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasPermission(Object permission) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public URL getResource(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dictionary<String, String> getHeaders(String locale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSymbolicName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getEntryPaths(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getEntry(String path) {
		// TODO Auto-generated method stub
		try {
			return new  File(path).toURI().toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public long getLastModified() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BundleContext getBundleContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<X509Certificate, List<X509Certificate>> getSignerCertificates(int signersType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Version getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A> A adapt(Class<A> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getDataFile(String filename) {
		// TODO Auto-generated method stub
		return null;
	}

}
