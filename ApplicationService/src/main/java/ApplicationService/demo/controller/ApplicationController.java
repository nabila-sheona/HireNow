package ApplicationService.demo.controller;

import ApplicationService.demo.model.JobApplication;
import ApplicationService.demo.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping
    public JobApplication createApplication(@RequestBody JobApplication application) {
        return applicationService.createApplication(application);
    }

    @GetMapping
    public List<JobApplication> getAllApplications() {
        return applicationService.getAllApplications();
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplication> getApplicationById(@PathVariable String id) {
        Optional<JobApplication> application = applicationService.getApplicationById(id);
        return application.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/job/{jobId}")
    public List<JobApplication> getApplicationsByJob(@PathVariable String jobId) {
        return applicationService.getApplicationsByJob(jobId);
    }

    @GetMapping("/jobseeker/{jobSeekerId}")
    public List<JobApplication> getApplicationsByJobSeeker(@PathVariable String jobSeekerId) {
        return applicationService.getApplicationsByJobSeeker(jobSeekerId);
    }

    @GetMapping("/job/{jobId}/status/{status}")
    public List<JobApplication> getApplicationsByJobAndStatus(@PathVariable String jobId, @PathVariable String status) {
        return applicationService.getApplicationsByJobAndStatus(jobId, status);
    }

    @GetMapping("/jobseeker/{jobSeekerId}/status/{status}")
    public List<JobApplication> getApplicationsByJobSeekerAndStatus(@PathVariable String jobSeekerId, @PathVariable String status) {
        return applicationService.getApplicationsByJobSeekerAndStatus(jobSeekerId, status);
    }

    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<JobApplication> updateApplicationStatus(@PathVariable String id, @PathVariable String status) {
        try {
            JobApplication updatedApplication = applicationService.updateApplicationStatus(id, status);
            return ResponseEntity.ok(updatedApplication);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable String id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}
