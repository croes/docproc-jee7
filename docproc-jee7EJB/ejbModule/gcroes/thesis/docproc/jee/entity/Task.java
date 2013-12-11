package gcroes.thesis.docproc.jee.entity;

import gcroes.thesis.docproc.jee.util.Serializer;

import java.io.IOException;
import java.io.Serializable;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The persistent class for the task database table.
 * 
 */
@Entity
@Table(name = "task")
@NamedQueries({ @NamedQuery(name = "Task.findAll", query = "SELECT t FROM Task t") })
public class Task implements Serializable {

	private static Logger logger = LogManager.getLogger(Task.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 8377642051361748432L;

	public static final String JOIN_PARAM = ".meta.join";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Temporal(TemporalType.DATE)
	@Column(name = "created_at")
	private Date createdAt;

	@Temporal(TemporalType.DATE)
	@Column(name = "finished_at")
	private Date finishedAt;

	@Temporal(TemporalType.DATE)
	@Column(name = "started_at")
	private Date startedAt;

	@Lob
	@Column(name = "worker_name")
	private String workerName;

	@ManyToOne
	@JoinColumn(name = "job_id")
	private Job job;

	@OneToMany
	@JoinColumn(name = "parent_id")
	private List<Task> parents;

	// @OneToMany(cascade=CascadeType.ALL)
	// @JoinColumn(name = "task_id")
	// private List<Parameter> params;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "param", joinColumns = @JoinColumn(name = "task_id"))
	@Column(name = "param_value")
	@MapKeyColumn(name = "param_key")
	@Lob
	private Map<String, byte[]> params;

	public Task() {
	}

	public Task(Job job, Task parentTask, String workerName) {
		this.job = job;
		this.parents = new ArrayList<Task>();
		this.workerName = workerName;
		this.createdAt = new Date();
		this.finishedAt = null;
		this.startedAt = null;
		this.params = new HashMap<String, byte[]>();
		initJoin();
		if (parentTask != null) {
			addParent(parentTask);
		}
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Date getFinishedAt() {
		return this.finishedAt;
	}

	public boolean isFinished() {
		return this.finishedAt != null;
	}

	public void setFinishedAt() {
		this.finishedAt = new Date();
	}

	public void setFinishedAt(Date date) {
		this.finishedAt = date;
	}

	public Date getStartedAt() {
		return this.startedAt;
	}

	public void setStartedAt() {
		this.startedAt = new Date();
	}

	public String getWorkerName() {
		return this.workerName;
	}

	public void setWorkerName(String workerName) {
		this.workerName = workerName;
	}

	@XmlTransient
	// to avoid infinite xml
	public Job getJob() {
		return this.job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public List<Task> getParents() {
		return this.parents;
	}

	public void addParent(Task parent) {
		@SuppressWarnings("unchecked")
		ArrayList<Join> parentJoinList = (ArrayList<Join>) parent
				.getParamValue(JOIN_PARAM);
		@SuppressWarnings("unchecked")
		ArrayList<Join> thisJoinList = (ArrayList<Join>) this
				.getParamValue(JOIN_PARAM);
		if (thisJoinList.isEmpty()) {
			logger.info(String
					.format("Adding joinlist of size %1$d from parent [%2$s] to task [%3$s].",
							parentJoinList.size(), parent.getWorkerName(),
							this.getWorkerName()));
			thisJoinList.addAll(parentJoinList);
			putParam(JOIN_PARAM, thisJoinList);
		}
		this.parents.add(parent);
	}

	public void saveTiming() {
		// TODO Auto-generated method stub

	}

	public void markSplit(Join join) {
		@SuppressWarnings("unchecked")
		ArrayList<Join> joinList = (ArrayList<Join>) getParamValue(JOIN_PARAM);
		joinList.add(join);
		putParam(JOIN_PARAM, joinList);
	}

	public void initJoin() {
		this.putParam(JOIN_PARAM, new ArrayList<Join>());
	}

	/**
	 * Remove a parameter from the parameter map
	 * 
	 * @param key
	 * @return the value of the (key, value) pair that was removed, or null if
	 *         no pair was present
	 */
	public Object removeParam(String key) {
		try {
			return Serializer.deserialize(params.remove(key));
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Object getParamValue(String key) {
		try {
			if (params.containsKey(key)) {
				Object value = Serializer.deserialize(params.get(key));
				if (value == null) {
					logger.warn("Null value for key: " + key);
				}
				return value;
			} else {
				logger.info("Param map does not contain key: " + key);
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		logger.warn("Could not get param with key:" + key);
		return null;
	}

	/**
	 * Insert or update a (key,value) pair into the parameter map
	 * 
	 * @param key
	 * @param value
	 * @return null if new (key,value) pair, previous value if update
	 */
	public Object putParam(String key, Object value) {
		try {
			return Serializer.deserialize(params.put(key,
					Serializer.serialize(value)));
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		logger.warn("Could not insert param for key: " + key);
		return null;
	}

	public String getNextWorkerName() {
		return job.getWorkflowConfig().getNextStep(workerName, "next");
	}

	public Set<String> getParamNames() {
		return params.keySet();
	}

	public List<Task> getAncestors() {
		List<Task> ancestors = new ArrayList<Task>();
		if (this.parents.isEmpty()) {
			return ancestors;
		} else {
			for (Task parent : this.parents) {
				ancestors.addAll(parent.getAncestors());
			}
		}
		return ancestors;
	}

}