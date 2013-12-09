/*
    Copyright 2013 KU Leuven Research and Development - iMinds - Distrinet

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

    Administrative Contact: dnet-project-office@cs.kuleuven.be
    Technical Contact: bart.vanbrabant@cs.kuleuven.be
 */

package gcroes.thesis.docproc.jee.worker;

import java.util.List;

import javax.ejb.EJB;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.codahale.metrics.Timer.Context;

import gcroes.thesis.docproc.jee.App;
import gcroes.thesis.docproc.jee.Service;
import gcroes.thesis.docproc.jee.monitoring.Metrics;
import gcroes.thesis.docproc.jee.entity.Join;
import gcroes.thesis.docproc.jee.entity.Task;
import gcroes.thesis.docproc.jee.tasks.TaskResult;
import gcroes.thesis.docproc.jee.tasks.TaskResult.Result;

/**
 * A work class that fetches work from a pull queue
 * 
 * TODO: add workflow abort
 * 
 * @author Bart Vanbrabant <bart.vanbrabant@cs.kuleuven.be>
 */
public abstract class Worker implements Runnable {
	private static final Logger logger = LogManager.getLogger(App.class
            .getClass().getName());

	@EJB
	Service service;
	
	protected Task task;

	/**
	 * Create a new work for a task
	 * 
	 * @param task
	 *            The task this worker should perform
	 */
	public Worker(Task task) {
		this.task = task;
		//logger.info("Worker started for task " + this.task.getWorkerName());
	}

	/**
	 * Get the name of the next worker if the workflow id is known
	 */
	public String getNextWorker(int workflowId) {
		return service.getNextWorker(workflowId, this.task.getWorkerName());
	}

	/**
	 * Do the work for the given task.
	 */
	public abstract TaskResult work();

	/**
	 * The main loop that handles the tasks.
	 */
	@Override
	public void run() {
		logger.info("Started worker " + this.toString());

		try {
			Context tcLease = Metrics.timer("worker.lease").time();

			if (task != null) {
				tcLease.stop();
				Context tc = Metrics.timer("worker.work." + this.task.getWorkerName()).time();

				trace("FETCHED", task);

				// execute the task
				TaskResult result = null;
				task.setStartedAt();
				try {
					result = this.work();
				} catch (Exception e) {
					result = new TaskResult();

					result.setException(e);
					result.setResult(Result.EXCEPTION);
					result.fail();
				}
				task.setFinishedAt();
				task.saveTiming();

				if (result == null) {
					result = new TaskResult();
					result.setResult(Result.ERROR);
					result.fail();
					logger.warn("Worker returns null. Ouch ...");
				}

				// process the result
				if (result.getResult() == TaskResult.Result.FINISHED) {
					//service.jobFinished(task.getJob());

				} else if (result.getResult() == TaskResult.Result.SUCCESS) {
					trace("DONE", task);
					if(service == null)
						logger.info("SERVICE NULL, DAMMIT!");
					List<Task> tasks = result.getNextTasks();
					// is this is a split, do the split
					if (tasks.size() > 1) {
						// allocate a new uuid that will become the
						// taskid of the joined task
						Join join = new Join(tasks.size());
						task.getJob().addJoin(join);
						for (Task newTask : tasks) {
							 newTask.markSplit(join);
							//service.queueTask(newTask);
							trace("NEW", newTask);
						}
					} else if (tasks.size() == 1) {
						logger.debug("should be queueing task now. service cannot be injected though.");
						//service.queueTask(tasks.get(0));
					} else {
						logger.debug("No next task to queue");
						// do nothing
					}
					//service.deleteTask(task);

				} else {
					trace("FAILED", task);
					logger.warn(String.format("[%s] failed %s: %s",
							this.task.getWorkerName(), task.toString(), result.getResult()
									.toString()));
					if (result.getResult() == TaskResult.Result.EXCEPTION) {
						result.getException().printStackTrace();
					}

					if (result.isFatal()) {
						// if this task is fatal, kill the current workflow
						//service.killJob(task.getJob());
					} 
				}

				tc.stop();
			}
		} catch (Exception e) {
			logger.warn(this.task.getWorkerName() + " failed", e);
		}
	}

	private void trace(String cmd, Task task) {
		logger.info(String.format("[%s] %s %s", this.task.getWorkerName(), cmd, task.toString()));
	}
}
