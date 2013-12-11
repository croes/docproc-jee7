package gcroes.thesis.docproc.jee;

import gcroes.thesis.docproc.jee.entity.Job;
import gcroes.thesis.docproc.jee.entity.Task;
import gcroes.thesis.docproc.jee.worker.CsvToTaskWorker;
import gcroes.thesis.docproc.jee.worker.EndWorker;
import gcroes.thesis.docproc.jee.worker.JoinWorker;
import gcroes.thesis.docproc.jee.worker.TemplateWorker;
import gcroes.thesis.docproc.jee.worker.Worker;
import gcroes.thesis.docproc.jee.worker.XslFoRenderWorker;
import gcroes.thesis.docproc.jee.worker.ZipWorker;







//import gcroes.thesis.docproc.jee.schedule.WeightedRoundRobin;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FopFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Session Bean implementation class Service
 */
@Stateless
@WebService
public class Service implements ServiceRemote, Serializable {

	/**
     * 
     */
	private static final long serialVersionUID = 820984664601318701L;

	private static Logger logger = LogManager
			.getLogger(Service.class.getName());

	@PersistenceContext
	EntityManager em;

	@Resource(lookup = "concurrent/docprocMSES")
	ManagedScheduledExecutorService mses;

	//
	/**
	 * Default constructor.
	 */
	public Service() {
	}

	@Override
	public List<Job> getAllJobs() {
		logger.debug("Fetching all jobs");
		return em.createNamedQuery("Job.findAll", Job.class).getResultList();
	}

	@Override
	public int getNbOfJobs() {
		logger.debug("Fetching number of jobs");
		return em.createNamedQuery("Job.findAllCount", Number.class)
				.getSingleResult().intValue();
	}

	@Override
	public Job addAJob(String workflowName) {
		logger.debug("Fetching workflow: " + workflowName);
		Job j = new Job(workflowName);
		j.newStartTask();
		em.persist(j);
		em.flush();
		return j;
	}

	@Override
	public void addJob(Job job) {
		logger.debug("Adding job: " + job);
		em.persist(job);
		em.flush();
	}

	@Override
	public Job findJobByID(int id) {
		logger.debug("Fetching job. ID: " + id);
		return em.createNamedQuery("Job.findByID", Job.class)
				.setParameter("id", id).getSingleResult();
	}

	@Override
	public void testScheduleWorker() {
		Job j = addAJob("invoices");
		Worker worker = null;
		try {
			worker = workerForTask(j.getStartTask());
		} catch (WorkerNotFoundException e) {
			e.printStackTrace();
		}
		Calendar calendar = Calendar.getInstance(); // gets a calendar using the
													// default time zone and
													// locale.
		calendar.add(Calendar.SECOND, 5);
		mses.schedule(worker, 5, TimeUnit.SECONDS);
	}

	public String getNextWorker(int workflowId, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	// public void addWorkflowStateListener(JobStateListener listener) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// public void removeWorkflowStateListener(JobStateListener listener) {
	// // TODO Auto-generated method stub
	//
	// }

	// public WeightedRoundRobin getPriorities(String workerType) {
	// // TODO Auto-generated method stub
	// return null;
	// }

	public void removeJobPriority(Job job, List<String> workerTypes) {
		// TODO Auto-generated method stub

	}

	// public void setPriorities(String workerType, WeightedRoundRobin wrr) {
	// // TODO Auto-generated method stub
	//
	// }

	public Task getTask(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public void jobFinished(Job job) {
		logger.debug("Marking job finished");
	}

	public void killJob(Job job) {
		// TODO Auto-generated method stub

	}

	public void deleteTask(Task task) {
		logger.debug("deleteTask()");
	}

	public void queueTask(Task task) {
		try {
			Worker worker = workerForTask(task);
			mses.schedule(worker, 5, TimeUnit.SECONDS);
			em.merge(task.getJob());
		} catch (WorkerNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	private Worker workerForTask(Task task) throws WorkerNotFoundException {
		String name = task.getWorkerName();
		switch (name) {
		case "csv-to-task":
			return new CsvToTaskWorker(task);
		case "template-to-xml":
			return new TemplateWorker(task);
		case "xsl-fo-render":
			return new XslFoRenderWorker(task);
		case "join":
			return new JoinWorker(task);
		case "zip-files":
			return new ZipWorker(task);
		case "end":
			return new EndWorker(task);
		default:
			throw new WorkerNotFoundException(
					"Could not find worker with name: " + name);
		}
	}

}
