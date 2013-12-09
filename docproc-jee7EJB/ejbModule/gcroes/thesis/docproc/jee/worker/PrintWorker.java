package gcroes.thesis.docproc.jee.worker;

import gcroes.thesis.docproc.jee.entity.Task;
import gcroes.thesis.docproc.jee.tasks.TaskResult;
import gcroes.thesis.docproc.jee.tasks.TaskResult.Result;

public class PrintWorker extends Worker {

	public PrintWorker(Task task) {
		super(task);
	}

	@Override
	public TaskResult work() {
		TaskResult result = new TaskResult();
		System.out.println("COMMENCING PRINTING");
		for(int i = 0; i < 10; i++){
			System.out.println("PRINTING: " + i);
		}
		System.out.println("FINISHED PRINTING");
		result.setResult(Result.SUCCESS);
		return result;
	}

}
