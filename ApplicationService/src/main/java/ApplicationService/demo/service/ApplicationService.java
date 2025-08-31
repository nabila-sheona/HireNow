package ApplicationService.demo.service;

import ApplicationService.demo.model.JobApplication;
import ApplicationService.demo.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    public JobApplication createApplication(JobApplication application) {
        return applicationRepository.save(application);
    }

    public List<JobApplication> getAllApplications() {
        return applicationRepository.findAll();
    }

    public Optional<JobApplication> getApplicationById(String id) {
        return applicationRepository.findById(id);
    }

    public List<JobApplication> getApplicationsByJob(String jobId) {
        return applicationRepository.findByJobId(jobId);
    }

    public List<JobApplication> getApplicationsByJobSeeker(String jobSeekerId) {
        return applicationRepository.findByJobSeekerId(jobSeekerId);
    }

    public List<JobApplication> getApplicationsByJobAndStatus(String jobId, String status) {
        return applicationRepository.findByJobIdAndStatus(jobId, status);
    }

    public List<JobApplication> getApplicationsByJobSeekerAndStatus(String jobSeekerId, String status) {
        return applicationRepository.findByJobSeekerIdAndStatus(jobSeekerId, status);
    }

    public JobApplication updateApplicationStatus(String id, String status) {
        JobApplication application = applicationRepository.findById(id).orElseThrow();
        application.setStatus(status);
        return applicationRepository.save(application);
    }

    public void deleteApplication(String id) {
        applicationRepository.deleteById(id);
    }
}
