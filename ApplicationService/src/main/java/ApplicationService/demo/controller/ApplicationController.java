package ApplicationService.demo.controller;

import ApplicationService.demo.model.JobApplication;
import ApplicationService.demo.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private RestTemplate restTemplate; // Add this injection

    @PostMapping
    public ResponseEntity<?> createApplication(@RequestBody JobApplication application) {
        try {
            JobApplication createdApplication = applicationService.createApplication(application);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdApplication);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Validation Error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Application Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal Server Error", "Failed to create application"));
        }
    }

    @GetMapping
    public ResponseEntity<List<JobApplication>> getAllApplications() {
        try {
            List<JobApplication> applications = applicationService.getAllApplications();
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getApplicationById(@PathVariable String id) {
        try {
            Optional<JobApplication> application = applicationService.getApplicationById(id);
            return application.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Validation Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<?> getApplicationsByJob(@PathVariable String jobId) {
        try {
            List<JobApplication> applications = applicationService.getApplicationsByJob(jobId);
            return ResponseEntity.ok(applications);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Validation Error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/jobseeker/{jobSeekerId}")
    public ResponseEntity<?> getApplicationsByJobSeeker(@PathVariable String jobSeekerId) {
        try {
            List<JobApplication> applications = applicationService.getApplicationsByJobSeeker(jobSeekerId);
            return ResponseEntity.ok(applications);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Validation Error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/job/{jobId}/status/{status}")
    public ResponseEntity<?> getApplicationsByJobAndStatus(@PathVariable String jobId, @PathVariable String status) {
        try {
            List<JobApplication> applications = applicationService.getApplicationsByJobAndStatus(jobId, status);
            return ResponseEntity.ok(applications);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Validation Error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/jobseeker/{jobSeekerId}/status/{status}")
    public ResponseEntity<?> getApplicationsByJobSeekerAndStatus(@PathVariable String jobSeekerId, @PathVariable String status) {
        try {
            List<JobApplication> applications = applicationService.getApplicationsByJobSeekerAndStatus(jobSeekerId, status);
            return ResponseEntity.ok(applications);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Validation Error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable String id, @PathVariable String status) {
        try {
            JobApplication updatedApplication = applicationService.updateApplicationStatus(id, status);
            return ResponseEntity.ok(updatedApplication);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Validation Error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal Server Error", "Failed to update application status"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApplication(@PathVariable String id) {
        try {
            applicationService.deleteApplication(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Validation Error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal Server Error", "Failed to delete application"));
        }
    }

    // Additional endpoints for business logic
    @GetMapping("/job/{jobId}/count")
    public ResponseEntity<?> getApplicationCountForJob(@PathVariable String jobId) {
        try {
            long count = applicationService.getApplicationCountForJob(jobId);
            return ResponseEntity.ok(new CountResponse(count));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Validation Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/job/{jobId}/pending/count")
    public ResponseEntity<?> getPendingApplicationsCountForJob(@PathVariable String jobId) {
        try {
            long count = applicationService.getPendingApplicationsCountForJob(jobId);
            return ResponseEntity.ok(new CountResponse(count));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Validation Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/can-apply")
    public ResponseEntity<?> canUserApply(@RequestParam String jobSeekerId, @RequestParam String jobId) {
        try {
            boolean canApply = applicationService.canUserApply(jobSeekerId, jobId);
            return ResponseEntity.ok(new CanApplyResponse(canApply));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Diagnostic endpoints
    @GetMapping("/debug/user/{userId}")
    public ResponseEntity<?> debugUserCheck(@PathVariable String userId) {
        try {
            String url = "http://UserService/api/users/" + userId;
            Object userResponse = restTemplate.getForObject(url, Object.class);
            return ResponseEntity.ok().body(new DebugResponse("SUCCESS", "User found", userResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new DebugResponse("ERROR", "User check failed: " + e.getMessage(), null));
        }
    }

    @GetMapping("/debug/job/{jobId}")
    public ResponseEntity<?> debugJobCheck(@PathVariable String jobId) {
        try {
            String url = "http://JobService/api/jobs/" + jobId;
            Object jobResponse = restTemplate.getForObject(url, Object.class);
            return ResponseEntity.ok().body(new DebugResponse("SUCCESS", "Job found", jobResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new DebugResponse("ERROR", "Job check failed: " + e.getMessage(), null));
        }
    }

    @GetMapping("/debug/services")
    public ResponseEntity<?> debugServices() {
        try {
            // Test User Service
            String userServiceUrl = "http://UserService/api/users";
            restTemplate.getForObject(userServiceUrl, Object.class);

            // Test Job Service
            String jobServiceUrl = "http://JobService/api/jobs";
            restTemplate.getForObject(jobServiceUrl, Object.class);

            return ResponseEntity.ok().body(new DebugResponse("SUCCESS", "All services are reachable", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new DebugResponse("ERROR", "Service connectivity issue: " + e.getMessage(), null));
        }
    }

    // Helper methods
    private ErrorResponse createErrorResponse(String error, String message) {
        return new ErrorResponse(error, message);
    }

    // Response DTOs
    private static class ErrorResponse {
        private final String error;
        private final String message;

        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }

        public String getError() { return error; }
        public String getMessage() { return message; }
    }

    private static class CountResponse {
        private final long count;

        public CountResponse(long count) {
            this.count = count;
        }

        public long getCount() { return count; }
    }

    private static class CanApplyResponse {
        private final boolean canApply;

        public CanApplyResponse(boolean canApply) {
            this.canApply = canApply;
        }

        public boolean isCanApply() { return canApply; }
    }

    private static class DebugResponse {
        private final String status;
        private final String message;
        private final Object data;

        public DebugResponse(String status, String message, Object data) {
            this.status = status;
            this.message = message;
            this.data = data;
        }

        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
    }
}