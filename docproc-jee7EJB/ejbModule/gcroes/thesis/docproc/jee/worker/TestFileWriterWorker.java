package gcroes.thesis.docproc.jee.worker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gcroes.thesis.docproc.jee.entity.Task;
import gcroes.thesis.docproc.jee.tasks.TaskResult;
import gcroes.thesis.docproc.jee.tasks.TaskResult.Result;

public class TestFileWriterWorker extends Worker {
	
	private static Logger logger = LogManager
            .getLogger(TestFileWriterWorker.class.getName());

	public TestFileWriterWorker(Task task) {
		super(task);
	}

	@Override
	public TaskResult work() {
		logger.entry();
		TaskResult result = new TaskResult();
		File testFile = new File("../appdocs/testfile.txt");
		FileWriter fw;
		try {
			fw = new FileWriter(testFile);
			fw.write("FIND ME");
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		result.setResult(Result.SUCCESS);
		return logger.exit(result);
	}

}
