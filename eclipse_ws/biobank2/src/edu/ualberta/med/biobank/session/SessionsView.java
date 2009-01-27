package edu.ualberta.med.biobank.session;

import java.util.HashMap;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import edu.ualberta.med.biobank.Activator;
import edu.ualberta.med.biobank.SessionCredentials;
import edu.ualberta.med.biobank.model.RootNode;
import edu.ualberta.med.biobank.model.SiteNode;
import edu.ualberta.med.biobank.model.SessionNode;
import edu.ualberta.med.biobank.model.ISessionNodeListener;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.client.ApplicationServiceProvider;
import gov.nih.nci.system.query.SDKQuery;
import gov.nih.nci.system.query.example.InsertExampleQuery;
import edu.ualberta.med.biobank.model.Site;

public class SessionsView extends ViewPart {
	public static final String ID =
	      "edu.ualberta.med.biobank.views.sessions";

	private TreeViewer treeViewer;
	
	private RootNode rootNode;
	
	private HashMap<String, WritableApplicationService> sessions;
	
	public SessionsView() {
		super();
		Activator.getDefault().setSessionView(this);
		rootNode = new RootNode();
		sessions = new  HashMap<String, WritableApplicationService>();
	}

	@Override
	public void createPartControl(Composite parent) {
		
		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI
				| SWT.V_SCROLL);
		getSite().setSelectionProvider(treeViewer);
		treeViewer.setLabelProvider(new SessionLabelProvider());
		treeViewer.setContentProvider(new SessionContentProvider());
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}
	
	public void loginFailed(SessionCredentials sc) {
		// pop up a dialog box here
		
	}
	
	public void createSession(final SessionCredentials sc) {
		Job job = new Job("logging in") {
			protected IStatus run(IProgressMonitor monitor) {
				
				monitor.beginTask("Logging in ... ", 100);					
				try {
					final WritableApplicationService appService;
					final String userName = sc.getUserName(); 
					final String url = "http://" + sc.getServer() + "/biobank2";
					
					if (userName.length() == 0) {
						appService =  (WritableApplicationService) 
						ApplicationServiceProvider.getApplicationServiceFromUrl(url);
					}
					else {
						appService = (WritableApplicationService) 
						ApplicationServiceProvider.getApplicationServiceFromUrl(url, userName, sc.getPassword());
					}

					
					Display.getDefault().asyncExec(new Runnable() {
				          public void run() {
				        	  Activator.getDefault().addSession(appService, sc.getServer());
				          }
					});
				}
				catch (Exception exp) {	
					exp.printStackTrace();
					
					Display.getDefault().asyncExec(new Runnable() {
				          public void run() {
				        	  Activator.getDefault().addSessionFailed(sc);
				          }
					});
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}
	
	public void addSession(final WritableApplicationService appService, final String name) {
		sessions.put(name, appService);
		final SessionNode sessionNode = new SessionNode(appService, name);
		rootNode.addSessionNode(sessionNode);
		
		treeViewer.setInput(rootNode);
		sessionNode.addListener(new ISessionNodeListener() {
			public void sessionChanged(SessionNode sessionNode, SiteNode siteNode) {
				treeViewer.refresh();
			}
		});

		// get the Site sites stored on this server
		Job job = new Job("logging in") {
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Querying Sites ... ", 100);
				
				Site site = new Site();				
				try {
					final List<Object> sites = appService.search(Site.class, site);
					
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							for (Object obj : sites) {
								Site site = (Site) obj;
								sessionNode.addSite(site);
							}
						}
					});
				}
				catch (Exception exp) {
					exp.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}
	
	public void deleteSession(String name) throws Exception {
		rootNode.deleteSessionNode(name);
	}
	
	public int getSessionCount() {
		return rootNode.getChildCount();
	}
	
	public String[] getSessionNames() {
		return sessions.keySet().toArray(new String[sessions.size()]);
	}
	
	public void createObject(final String sessionName, final Object o) throws Exception {
		if (!sessions.containsKey(sessionName)) {
			throw new Exception();
		}
		
		final SDKQuery query = new InsertExampleQuery(o);
		
		Job job = new Job("Creating Object") {
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Submitting information ... ", 100);
				
				try {
					sessions.get(sessionName).executeQuery(query);
				}
				catch (Exception exp) {
					exp.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}
}
