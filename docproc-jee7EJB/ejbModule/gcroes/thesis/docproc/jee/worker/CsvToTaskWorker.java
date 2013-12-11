package gcroes.thesis.docproc.jee.worker;

import gcroes.thesis.docproc.jee.entity.Task;
import gcroes.thesis.docproc.jee.tasks.TaskResult;
import gcroes.thesis.docproc.jee.util.FileUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

public class CsvToTaskWorker extends Worker {

    private static Logger logger = LogManager
            .getLogger(CsvToTaskWorker.class.getName());

	public CsvToTaskWorker(Task task) {
		super(task);
	}

	@Override
	public TaskResult work() {
		TaskResult result = new TaskResult();
        
        String csv_data = null;
        //csv_data = (String)task.getParamValue("arg0");
        try {
			csv_data = FileUtil.readFile("../appdocs/invoice.csv", Charset.defaultCharset());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		assert(csv_data != null);
		
        // read in the csv
        try {
        		logger.info("Parsing data from file: " + csv_data);
                CSVReader parser = new CSVReader(new StringReader(csv_data), ';');
                List<String[]> rows = parser.readAll();
                String[] headers = rows.get(0);
                logger.info("Headers: ");
                for(String header : headers){
                	logger.info(header);
                }
                
                for (int i = 1; i < rows.size(); i++) {
                        String[] row = rows.get(i);
                        Task newTask = new Task(task.getJob(), task, this.task.getNextWorkerName());
                        for (int j = 0; j < row.length; j++) {
                                newTask.putParam(headers[j], row[j]);
                        }
                        
                        result.addNextTask(newTask);
                }
                parser.close();
                
                result.setResult(TaskResult.Result.SUCCESS);
        } catch (FileNotFoundException e) {
        		logger.warn("CSV file not found");
                result.setResult(TaskResult.Result.EXCEPTION);
                result.setException(e);
        } catch (IOException e) {
        		logger.warn("IO error parsing CSV input file");
                result.setResult(TaskResult.Result.EXCEPTION);
                result.setException(e);
        }
        
        return result;
	}

}
