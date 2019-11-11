package restfulplugin.wizards.restlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jst.j2ee.common.CommonFactory;
import org.eclipse.jst.j2ee.common.ParamValue;
import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
import org.eclipse.jst.j2ee.webapplication.InitParam;
import org.eclipse.jst.j2ee.webapplication.Servlet;
import org.eclipse.jst.j2ee.webapplication.ServletMapping;
import org.eclipse.jst.j2ee.webapplication.ServletType;
import org.eclipse.jst.j2ee.webapplication.WebApp;
import org.eclipse.jst.j2ee.webapplication.WebapplicationFactory;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.wst.common.componentcore.ArtifactEdit;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;

/**
 * 
 * @author karthy
 * 
 */
@SuppressWarnings("restriction")
public class RestletWebserviceWizard extends Wizard implements INewWizard {
	private RestletWebservicePage page;
	private ISelection selection;

	private String[] restletJars;
	private String downloadSite = "http://karthy.me/repository/jars/";
	private String restletClass = "org.restlet.ext.servlet.ServerServlet";

	public static String containerName = null;
	public static String packageName = null;
	public static String fileName = null;

	/**
	 * Constructor for RESTfulWebserviceWizard.
	 */
	public RestletWebserviceWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	public void loadProperties() {
		Properties props = new Properties();
		InputStream is = null;
		try {
			is = getClass()
					.getResourceAsStream("/properties/wizard.properties");
			props.load(is);
		} catch (IOException e) {
			try {
				if (is != null)
					is.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		this.restletClass = props.getProperty("restlet.class", restletClass);
		this.downloadSite = (props.getProperty("download.site") + "restlet/");
		StringTokenizer st = new StringTokenizer(
				props.getProperty("restlet.jars"), ",");
		String[] temp = new String[st.countTokens()];
		for (int i = 0; st.hasMoreTokens(); i++) {
			temp[i] = st.nextToken();
		}
		this.restletJars = temp;

	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new RestletWebservicePage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {
		containerName = page.getContainerName();
		packageName = page.getPackageName();
		fileName = page.getClassName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, packageName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error",
					realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * The worker method. It will find the container, create the file if missing
	 * or just replace its contents, and open the editor on the newly created
	 * file.
	 * 
	 * @throws Exception
	 */

	@SuppressWarnings("unused")
	private void doFinish(String containerName, String fileName,
			String packageName, IProgressMonitor monitor) throws Exception {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName
					+ "\" does not exist.");
		}

		IContainer container = (IContainer) resource;
		IJavaProject project = (IJavaProject) container.getProject().getNature(
				"org.eclipse.jdt.core.javanature");
		IPackageFragment ipackage = project.getAllPackageFragmentRoots()[0]
				.createPackageFragment(packageName, false, monitor);
		String contents = getRESTfulClass(containerName);
		final ICompilationUnit cu = ipackage.createCompilationUnit(fileName
				+ ".java", contents, false, monitor);

		contents = getRestletApplicationClass(containerName);
		final ICompilationUnit cua = ipackage.createCompilationUnit(fileName
				+ "Application" + ".java", contents, false, monitor);

		monitor.worked(1);
	}

	/**
	 * We will initialize file contents with a sample text.
	 * 
	 * @throws Exception
	 */

	@SuppressWarnings("unused")
	private InputStream openContentStream(String containerName)
			throws Exception {
		String contents = getRESTfulClass(containerName);
		return new ByteArrayInputStream(contents.getBytes());
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, "RESTfulPlugin", IStatus.OK,
				message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	private String getRESTfulClass(String containerName) throws Exception {
		addServlet(containerName);
		StringBuffer sb = new StringBuffer();
		BufferedReader reader;
		InputStream input = this.getClass().getResourceAsStream(
				"/templates/restlet.template");
		reader = new BufferedReader(new InputStreamReader(input));
		String line = null;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
			line = line.replaceAll("#classname", fileName);
			line = line.replaceAll("#package", packageName);
			boolean skip = false;
			line = line.replaceAll("#projectname", containerName);
			if (!skip) {
				sb.append(line);
				sb.append("\n");
			}

		}
		reader.close();
		reader.close();
		return sb.toString();

	}

	private String getRestletApplicationClass(String containerName)
			throws Exception {

		StringBuffer sb = new StringBuffer();
		BufferedReader reader;
		InputStream input = this.getClass().getResourceAsStream(
				"/templates/restletapplication.template");
		reader = new BufferedReader(new InputStreamReader(input));
		String line = null;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
			line = line.replaceAll("#classname", fileName);
			line = line.replaceAll("#package", packageName);
			boolean skip = false;
			line = line.replaceAll("#projectname", containerName);
			if (!skip) {
				sb.append(line);
				sb.append("\n");
			}

		}
		reader.close();
		reader.close();
		return sb.toString();

	}

	@SuppressWarnings({ "unchecked", "unused"})
	public void addServlet(String containerName) throws Exception {
		loadProperties();

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		IContainer container = (IContainer) resource;
		IJavaProject project = (IJavaProject) container.getProject().getNature(
				"org.eclipse.jdt.core.javanature");
		IProject ip = (IProject) project.getCorrespondingResource();
		Servlet servlet = null;
		EList<Servlet> servlets;
		boolean exists;
		WebArtifactEdit artifact = WebArtifactEdit
				.getWebArtifactEditForWrite(ip);
		WebApp webApp = artifact.getWebApp();
		exists = false;
		servlets = webApp.getServlets();
		Iterator<Servlet> iterator = servlets.iterator();
		while (iterator.hasNext()) {
			Servlet ser = iterator.next();
			if (ser.getServletClass().getJavaName().equals(this.restletClass)) {
				exists = true;
				break;
			}
		}
		if (!exists) {

			servlet = WebapplicationFactory.eINSTANCE.createServlet();
			servlet.setServletName("RestletServletAdaptor");
			ServletType type = WebapplicationFactory.eINSTANCE
					.createServletType();
			type.setClassName(restletClass);
			servlet.setWebType(type);
			servlet.setLoadOnStartup(Integer.valueOf(1));

			if (webApp.getJ2EEVersionID() >= J2EEVersionConstants.J2EE_1_4_ID) {
				ParamValue param = CommonFactory.eINSTANCE.createParamValue();
				param.setName("org.restlet.application");
				param.setValue(packageName + "." + fileName
						+ "Application");
				servlet.getInitParams().add(param);

			} else {

				InitParam initParam = WebapplicationFactory.eINSTANCE
						.createInitParam();
				initParam.setParamName("org.restlet.application");
				initParam.setParamValue(packageName + "." + fileName
						+ "Application");
				servlet.getInitParams().add(initParam);
			}
			webApp.getServlets().add(servlet);

			ServletMapping mapping = WebapplicationFactory.eINSTANCE
					.createServletMapping();
			mapping.setServlet(servlet);
			mapping.setUrlPattern("/" + "rs" + "/*");
			webApp.getServletMappings().add(mapping);

		}

		artifact.saveIfNecessary(null);
		artifact.dispose();

		IFolder dir = getWebInfLibDir(ip);
		if (dir.exists()) {
			int len = this.restletJars.length;
			for (int i = 0; i < len; i++) {

				String jar = restletJars[i];

				if (!dir.getFile(jar).exists()) {
					try {
						int split = jar.lastIndexOf('-');
						String folder = jar.substring(0, split) + ".jar/";
						dir.getFile(jar).create(
								new URL(this.downloadSite + jar).openStream(),
								false, null);
					} catch (Exception e) {
						System.err.println(e.getMessage());
					}
				}
			}
		}

	}

	public IFolder getWebInfLibDir(IProject pj) {
		IVirtualComponent vc = ComponentCore.createComponent(pj);
		IVirtualFolder vf = vc.getRootFolder().getFolder("WEB-INF/lib");
		return (IFolder) vf.getUnderlyingFolder();
	}

	public void saveEdit(ArtifactEdit edit) {
		edit.saveIfNecessary(null);
		edit.dispose();
	}

}