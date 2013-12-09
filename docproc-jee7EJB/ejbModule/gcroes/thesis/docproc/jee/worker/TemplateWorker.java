package gcroes.thesis.docproc.jee.worker;

import gcroes.thesis.docproc.jee.entity.Task;
import gcroes.thesis.docproc.jee.tasks.TaskResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

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
	
	public TemplateWorker(Task task) {
		super(task);
	}

	@Override
	public TaskResult work() {
		TaskResult result = new TaskResult();

		try {
			VelocityEngine ve = new VelocityEngine();
			ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
			ve.setProperty("classpath.resource.loader.class",
					ClasspathResourceLoader.class.getName());
			ve.setProperty("runtime.log.logsystem.class",
					"org.apache.velocity.runtime.log.NullLogSystem");
			ve.init();

			final String templatePath = "invoice-template.xsl";
			InputStream input = this.getClass().getClassLoader()
					.getResourceAsStream(templatePath);
			if (input == null) {
				throw new IOException("Template file doesn't exist");
			}

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

			result.setResult(TaskResult.Result.SUCCESS);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
}