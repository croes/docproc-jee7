package gcroes.thesis.docproc.jee;

import gcroes.thesis.docproc.jee.entity.Task;
import gcroes.thesis.docproc.jee.tasks.TaskResult;
import gcroes.thesis.docproc.jee.worker.Worker;

public class EndWorker extends Worker {

	public EndWorker(Task task) {
		super(task);
	}

	@Override
	public TaskResult work() {
		TaskResult result = new TaskResult();
        return result.setResult(TaskResult.Result.FINISHED);
	}

}
