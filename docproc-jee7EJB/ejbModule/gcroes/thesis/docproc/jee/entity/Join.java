package gcroes.thesis.docproc.jee.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: Join
 *
 */
@Entity
@Table(name="join")
public class Join implements Serializable {

	   
	/**
	 * 
	 */
	private static final long serialVersionUID = 218260316362193525L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	@JoinColumn(name="job_id")
	private Job job;
	
	@Column(name="n_tasks")
	private int n_tasks;
	
	@OneToMany
	@JoinTable(
			name="join_parents",
			joinColumns=@JoinColumn(name="join_id"),
			inverseJoinColumns=@JoinColumn(name="task_id"))
	private List<Task> parents;
	
	public Join(){
		super();
	}

	public Join(int n_tasks) {
		super();
		this.n_tasks = n_tasks;
		this.parents = new ArrayList<Task>();
	}   
	public int getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}   
	public Job getJob() {
		return this.job;
	}

	public void setJob(Job job) {
		this.job = job;
	}   
	public int getN_tasks() {
		return this.n_tasks;
	}

	public void setN_tasks(int n_tasks) {
		this.n_tasks = n_tasks;
	}
	
	public void decrementJoin(){
		this.n_tasks--;
	}
	
	public void addParent(Task parentTask){
		this.parents.add(parentTask);
	}
	
	public void removeParent(Task parentTask){
		this.parents.remove(parentTask);
	}
	
	public List<Task> getParents(){
		return parents;
	}
}
