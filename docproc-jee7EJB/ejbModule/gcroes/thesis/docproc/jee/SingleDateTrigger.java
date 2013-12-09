package gcroes.thesis.docproc.jee;

import java.util.Date;

import javax.enterprise.concurrent.LastExecution;
import javax.enterprise.concurrent.Trigger;

 /**
  * A trigger that only returns a single date.
  */
  public class SingleDateTrigger implements Trigger {
      private Date fireTime;
      
      public SingleDateTrigger(Date newDate) {
          fireTime = newDate;
      }

      public Date getNextRunTime(
         LastExecution lastExecutionInfo, Date taskScheduledTime) {
         
         if(taskScheduledTime.after(fireTime)) {
             return null;
         }
         return fireTime;
      }

      public boolean skipRun(LastExecution lastExecutionInfo, Date scheduledRunTime) {
          return scheduledRunTime.after(fireTime);
      }
  }