package gcroes.thesis.docproc.jee.worker;

import gcroes.thesis.docproc.jee.entity.Task;
import gcroes.thesis.docproc.jee.tasks.TaskResult;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Collect all files in a workflow and zip them when an end of workflow task is
 * received.
 * 
 * @author Bart Vanbrabant <bart.vanbrabant@cs.kuleuven.be>
 */
public class ZipWorker extends Worker {

	private static Logger logger = LogManager
			.getLogger(TestFileWriterWorker.class.getName());

	/**
	 * Creates a new work with the name blob-to-cache
	 */
	public ZipWorker(Task task) {
		super(task);
	}

	/**
	 * Zip all files
	 */
	@SuppressWarnings("unchecked")
	public TaskResult work() {
		TaskResult result = new TaskResult();
		List<byte []> data = null;

		data = (List<byte []>) task.getParamValue("arg0");

		try {
			if (data.size() == 0) {
				logger.warn("empty zip file");
			}

			// create the zip stream
			ByteArrayOutputStream boas = new ByteArrayOutputStream();
			ZipOutputStream out = new ZipOutputStream(boas);

			// save the files in the zip
			int i = 0;
			for (byte [] ref : data) {

				out.putNextEntry(new ZipEntry(++i + ".pdf"));
				byte[] pdfData = ref;
				out.write(pdfData);
			}
			out.close();
			boas.flush();

			byte[] zipData = boas.toByteArray();
			boas.close();
			
			FileOutputStream fos = new FileOutputStream("../appdocs/out.zip");
			fos.write(zipData);
			fos.flush();
			fos.close();

			Task newTask = new Task(task.getJob(), task,
					this.task.getNextWorkerName());
			newTask.putParam("arg0", zipData);
			result.addNextTask(newTask);
		} catch (FileNotFoundException e) {
			result.setResult(TaskResult.Result.EXCEPTION);
			result.setException(e);
		} catch (IOException e) {
			result.setResult(TaskResult.Result.EXCEPTION);
			result.setException(e);
		}

		return result.setResult(TaskResult.Result.SUCCESS);
	}
}