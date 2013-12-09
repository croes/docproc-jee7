package gcroes.thesis.docproc.jee;

import gcroes.thesis.docproc.jee.entity.Job;

import java.util.List;

import javax.ejb.Remote;

@Remote
public interface ServiceRemote {

    public List<Job> getAllJobs();

    public int getNbOfJobs();

    public Job addAJob(String workflowName);
    
    public void addJob(Job job);

    public Job findJobByID(int id);
    
    public void testScheduleWorker();
    
}
