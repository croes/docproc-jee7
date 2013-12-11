package gcroes.thesis.docproc.jee.worker;

import gcroes.thesis.docproc.jee.entity.Join;
import gcroes.thesis.docproc.jee.entity.Task;
import gcroes.thesis.docproc.jee.tasks.TaskResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * A worker that renders a template
 * 
 * @author Bart Vanbrabant <bart.vanbrabant@cs.kuleuven.be>
 */
public class TemplateWorker extends Worker {
	private static final Logger logger = LogManager.getLogger(TemplateWorker.class
			.getClass().getName());
	
	public TemplateWorker(Task task) {
		super(task);
	}

	@Override
	public TaskResult work() {
		TaskResult result = new TaskResult();

		try {
			
			ArrayList<Join> joinList = (ArrayList<Join>) task.getParamValue(Task.JOIN_PARAM);
			logger.debug("joinList empty on entry templateworker: " + joinList.isEmpty());
			logger.debug("joinlist size: " + joinList.size() );
			
			VelocityEngine ve = new VelocityEngine();
			ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
			ve.setProperty("classpath.resource.loader.class",
					ClasspathResourceLoader.class.getName());
			ve.setProperty("runtime.log.logsystem.class",
					"org.apache.velocity.runtime.log.NullLogSystem");
			ve.init();
 
			final String templatePath = "invoice-template.xsl";

			VelocityContext context = new VelocityContext();

			for (String name : task.getParamNames()) {
				context.put(name, task.getParamValue(name));
			}

			Template template = ve.getTemplate(templatePath, "UTF-8");

			StringWriter writer = new StringWriter();
			template.merge(context, writer);
			writer.flush();

			Task newTask = new Task(task.getJob(), task, this.task.getNextWorkerName());
			newTask.putParam("arg0", writer.toString());
			result.addNextTask(newTask);
			
			ArrayList<Join> joinList1 = (ArrayList<Join>) newTask.getParamValue(Task.JOIN_PARAM);
			logger.debug("joinList empty on new xsl-fo-render task right after creation: " + joinList1.isEmpty());
			
			result.setResult(TaskResult.Result.SUCCESS);
			writer.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
}