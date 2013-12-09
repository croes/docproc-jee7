package gcroes.thesis.docproc.jee.worker;

import gcroes.thesis.docproc.jee.entity.Task;
import gcroes.thesis.docproc.jee.tasks.TaskResult;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

/**
 * A worker that renders an invoice based
 * 
 * @author Bart Vanbrabant <bart.vanbrabant@cs.kuleuven.be>
 */
public class XslFoRenderWorker extends Worker {

	public XslFoRenderWorker(Task task) {
		super(task);
	}

	@Override
	public TaskResult work() {
		TaskResult result = new TaskResult();
		String invoice_source = null;

		invoice_source = (String) task.getParamValue("arg0");

		try {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			FopFactory fopFactory = FopFactory.newInstance();

			// Load the stylesheet
			Templates templates = tFactory.newTemplates(new StreamSource(
					new StringReader(invoice_source)));

			// Second run (the real thing)
			ByteArrayOutputStream boas = new ByteArrayOutputStream();
			OutputStream out = new java.io.BufferedOutputStream(boas);
			try {
				FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
				foUserAgent.setURIResolver(new ClassPathURIResolver());
				Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF,
						foUserAgent, out);

				Transformer transformer = templates.newTransformer();
				transformer.transform(new StreamSource(new StringReader(
						"<invoice></invoice>")),
						new SAXResult(fop.getDefaultHandler()));

			} finally {
				out.close();
			}

			// store the fileData
			Task newTask = new Task(task.getJob(), task,
					this.task.getNextWorkerName());
			newTask.putParam("arg0", boas.toByteArray());
			result.addNextTask(newTask);
			result.setResult(TaskResult.Result.SUCCESS);
		} catch (Exception e) {
			result.setResult(TaskResult.Result.EXCEPTION);
			result.setException(e);
		}
		return result;
	}
}