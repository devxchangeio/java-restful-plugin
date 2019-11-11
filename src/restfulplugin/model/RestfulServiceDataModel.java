/**
 * 
 */
package restfulplugin.model;

/**
 * @author karthy
 * 
 */
public class RestfulServiceDataModel {

	private String containerName;
	private String fileName;
	private String packageName;
	private boolean useException;
	private boolean addJars;
	private boolean addServlet;
	private String urlPattern;
	private boolean applLog;
	private String applLogClass;
	private String applLogPackage;
	private String subSystem;
	private String programId;
	private String projectName;
	private String exceptionClass;
	private String exceptionPackage;
	private static RestfulServiceDataModel model;
	private static Object lock = new Object();

	public static RestfulServiceDataModel getInstance() {
		if (model == null) {
			synchronized (lock) {
				if (model == null)
					model = new RestfulServiceDataModel();
			}
		}
		return model;
	}

	public String getProjectName() {
		return this.projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getContainerName() {
		return this.containerName;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getPackage() {
		return this.packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public boolean useException() {
		return this.useException;
	}

	public void setUseException(boolean useException) {
		this.useException = useException;
	}

	public boolean addJars() {
		return this.addJars;
	}

	public void setAddJars(boolean addJars) {
		this.addJars = addJars;
	}

	public boolean addServlet() {
		return this.addServlet;
	}

	public void setAddServlet(boolean addServlet) {
		this.addServlet = addServlet;
	}

	public String getUrlPattern() {
		return this.urlPattern;
	}

	public void setUrlPattern(String urlPattern) {
		this.urlPattern = urlPattern;
	}

	public boolean isApplLog() {
		return this.applLog;
	}

	public void setApplLog(boolean applLog) {
		this.applLog = applLog;
	}

	public String getApplLogClass() {
		return this.applLogClass;
	}

	public void setApplLogClass(String applLogClass) {
		this.applLogClass = applLogClass;
	}

	public String getApplLogPackage() {
		return this.applLogPackage;
	}

	public void setApplLogPackage(String applLogPackage) {
		this.applLogPackage = applLogPackage;
	}

	public String getSubSystem() {
		return this.subSystem;
	}

	public void setSubSystem(String subSystem) {
		this.subSystem = subSystem;
	}

	public String getProgramId() {
		return this.programId;
	}

	public void setProgramId(String programId) {
		this.programId = programId;
	}

	public String getExceptionClass() {
		return this.exceptionClass;
	}

	public void setExceptionClass(String exceptionClass) {
		this.exceptionClass = exceptionClass;
	}

	public String getExceptionPackage() {
		return this.exceptionPackage;
	}

	public void setExceptionPackage(String exceptionPackage) {
		this.exceptionPackage = exceptionPackage;
	}

}
