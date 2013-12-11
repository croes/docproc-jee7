package gcroes.thesis.docproc.jee.worker;

import gcroes.thesis.docproc.jee.App;
import gcroes.thesis.docproc.jee.entity.Job;
import gcroes.thesis.docproc.jee.entity.Join;
import gcroes.thesis.docproc.jee.entity.Task;
import gcroes.thesis.docproc.jee.tasks.ParameterFoundException;
import gcroes.thesis.docproc.jee.tasks.TaskResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A generic worker that joins a workflow by collecting all tasks of a workflow
 * and sending out a new task with all previous tasks in it.
 * 
 * This worker collects all arguments of the joined tasks and sends out a new
 * task with the same arguments in list form.
 * 
 * This worker can only be used once in a workflow with the same worker name.
 * 
 * @author Bart Vanbrabant <bart.vanbrabant@cs.kuleuven.be>
 */
public class JoinWorker extends Worker {
	private static final Logger logger = LogManager.getLogger(JoinWorker.class
			.getClass().getName());

	/**
	 * Creates a new work with the name blob-to-cache
	 */
	public JoinWorker(Task task) {
		super(task);
	}

	@Override
	public TaskResult work() {
		TaskResult result = new TaskResult();

		@SuppressWarnings("unchecked")
		ArrayList<Join> joinQueue = (ArrayList<Join>) task
				.getParamValue(Task.JOIN_PARAM);
		
		Join join = null; 
		if(!joinQueue.isEmpty()){
			join = joinQueue.remove(joinQueue.size() - 1);
		}else{ 
			logger.info("empty joinqueue");
		}

		// decrement the join counter
		// Job.decrementJoin(task.getJobId(), joinId);
		int joinValue = -1;
		if(join != null){
			synchronized (join) {
				join.decrementJoin();
				joinValue = join.getN_tasks();
				logger.debug("Decremented join [" + join + "] - new value: " + joinValue);
			}
			// register this task as a parent of the future joined task
			join.addParent(task);
		}

		// if the joinValue is zero, we need to "materialize" the join task
		// WARNING: creating this task has to be idempotent because
		// retrieving
		// the join counter has a race, so two possible tasks are joining
		if (joinValue == 0) {
			// GO!
			Task newTask = new Task(task.getJob(), task,
					task.getNextWorkerName());

			// load all parents and build the map of parameters
			Map<String, List<Object>> varMap = new HashMap<>();
			List<Task> parents = join.getParents();
			logger.info("Joining the results of " + parents.size() + " parents");

			for (Task parentTask : parents) {
				for (String paramName : parentTask.getParamNames()) {
					if (!varMap.containsKey(paramName)) {
						varMap.put(paramName, new ArrayList<Object>());
					}
					varMap.get(paramName).add(
							parentTask.getParamValue(paramName));
				}
			}

			// put the param maps in the new task
			for (String varName : varMap.keySet()) {
				newTask.putParam(varName, varMap.get(varName));
			}

			// add the new join queue
			newTask.putParam(Task.JOIN_PARAM, joinQueue);

			// return the new task
			result.addNextTask(newTask);
		}

		result.setResult(TaskResult.Result.SUCCESS);
		return result;
	}
}
