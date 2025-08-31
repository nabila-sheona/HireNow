package JobService.demo.service;

import JobService.demo.model.Job;
import JobService.demo.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    public Job createJob(Job job) {
        return jobRepository.save(job);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Optional<Job> getJobById(String id) {
        return jobRepository.findById(id);
    }

    public List<Job> getJobsByHirer(String hirerId) {
        return jobRepository.findByHirerId(hirerId);
    }

    public List<Job> searchJobsByCompany(String companyName) {
        return jobRepository.findByCompanyNameContainingIgnoreCase(companyName);
    }

    public List<Job> searchJobsByTitle(String jobTitle) {
        return jobRepository.findByJobTitleContainingIgnoreCase(jobTitle);
    }

    public Job updateJob(String id, Job jobDetails) {
        Job job = jobRepository.findById(id).orElseThrow();
        job.setCompanyName(jobDetails.getCompanyName());
        job.setJobTitle(jobDetails.getJobTitle());
        job.setExpectedSalary(jobDetails.getExpectedSalary());
        job.setPreference(jobDetails.getPreference());
        job.setRequiredSkills(jobDetails.getRequiredSkills());
        job.setExperience(jobDetails.getExperience());
        job.setWorkingHours(jobDetails.getWorkingHours());
        job.setPrerequisites(jobDetails.getPrerequisites());
        return jobRepository.save(job);
    }

    public void deleteJob(String id) {
        jobRepository.deleteById(id);
    }
}