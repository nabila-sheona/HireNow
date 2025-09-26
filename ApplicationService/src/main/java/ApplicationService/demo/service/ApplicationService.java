package ApplicationService.demo.service;

import ApplicationService.demo.model.JobApplication;
import ApplicationService.demo.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private RestTemplate restTemplate;

    // CORRECTED: Use service discovery names and proper endpoints
    private static final String USER_SERVICE_URL = "http://UserService/api/users";
    private static final String JOB_SERVICE_URL = "http://JobService/api/jobs";

    public JobApplication createApplication(JobApplication application) {
        // Validate required fields
        validateApplication(application);

        // Check if user exists - FIXED: Call User Service correctly
        if (!userExists(application.getJobSeekerId())) {
            throw new RuntimeException("Job seeker not found with ID: " + application.getJobSeekerId());
        }

        // Check if job exists
        if (!jobExists(application.getJobId())) {
            throw new RuntimeException("Job not found with ID: " + application.getJobId());
        }

        // Check if user has already applied for this job
        if (hasAlreadyApplied(application.getJobSeekerId(), application.getJobId())) {
            throw new RuntimeException("User has already applied for this job");
        }

        // Validate application meets job requirements (basic validation)
        validateApplicationRequirements(application);

        return applicationRepository.save(application);
    }

    public List<JobApplication> getAllApplications() {
        return applicationRepository.findAll();
    }

    public Optional<JobApplication> getApplicationById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Application ID cannot be null or empty");
        }
        return applicationRepository.findById(id);
    }

    public List<JobApplication> getApplicationsByJob(String jobId) {
        if (jobId == null || jobId.trim().isEmpty()) {
            throw new IllegalArgumentException("Job ID cannot be null or empty");
        }

        // Verify job exists
        if (!jobExists(jobId)) {
            throw new RuntimeException("Job not found with ID: " + jobId);
        }

        return applicationRepository.findByJobId(jobId);
    }

    public List<JobApplication> getApplicationsByJobSeeker(String jobSeekerId) {
        if (jobSeekerId == null || jobSeekerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Job seeker ID cannot be null or empty");
        }

        // Verify user exists
        if (!userExists(jobSeekerId)) {
            throw new RuntimeException("Job seeker not found with ID: " + jobSeekerId);
        }

        return applicationRepository.findByJobSeekerId(jobSeekerId);
    }

    public List<JobApplication> getApplicationsByJobAndStatus(String jobId, String status) {
        if (jobId == null || jobId.trim().isEmpty()) {
            throw new IllegalArgumentException("Job ID cannot be null or empty");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        validateStatus(status);

        // Verify job exists
        if (!jobExists(jobId)) {
            throw new RuntimeException("Job not found with ID: " + jobId);
        }

        return applicationRepository.findByJobIdAndStatus(jobId, status);
    }

    public List<JobApplication> getApplicationsByJobSeekerAndStatus(String jobSeekerId, String status) {
        if (jobSeekerId == null || jobSeekerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Job seeker ID cannot be null or empty");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        validateStatus(status);

        // Verify user exists
        if (!userExists(jobSeekerId)) {
            throw new RuntimeException("Job seeker not found with ID: " + jobSeekerId);
        }

        return applicationRepository.findByJobSeekerIdAndStatus(jobSeekerId, status);
    }

    public JobApplication updateApplicationStatus(String id, String status) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Application ID cannot be null or empty");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        validateStatus(status);

        JobApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + id));

        // Prevent updating status if already in final state
        if (isFinalStatus(application.getStatus())) {
            throw new RuntimeException("Cannot update application status from final state: " + application.getStatus());
        }

        application.setStatus(status.toUpperCase());
        return applicationRepository.save(application);
    }

    public void deleteApplication(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Application ID cannot be null or empty");
        }

        if (!applicationRepository.existsById(id)) {
            throw new RuntimeException("Application not found with ID: " + id);
        }

        applicationRepository.deleteById(id);
    }

    // Validation methods
    private void validateApplication(JobApplication application) {
        if (application == null) {
            throw new IllegalArgumentException("Application cannot be null");
        }

        if (application.getJobId() == null || application.getJobId().trim().isEmpty()) {
            throw new IllegalArgumentException("Job ID is required");
        }

        if (application.getJobSeekerId() == null || application.getJobSeekerId().trim().isEmpty()) {
            throw new IllegalArgumentException("Job seeker ID is required");
        }

        if (application.getName() == null || application.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (application.getEmail() == null || application.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (application.getPhone() == null || application.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        // Email format validation
        if (!isValidEmail(application.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Phone number validation (basic)
        if (!isValidPhone(application.getPhone())) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

        if (application.getSkills() == null || application.getSkills().isEmpty()) {
            throw new IllegalArgumentException("At least one skill is required");
        }
    }

    private void validateApplicationRequirements(JobApplication application) {
        // Basic validation - in a real scenario, you might want to fetch job details
        // and validate against specific requirements

        if (application.getExperience() == null || application.getExperience().trim().isEmpty()) {
            throw new IllegalArgumentException("Experience information is required");
        }

        if (application.getDegree() == null || application.getDegree().trim().isEmpty()) {
            throw new IllegalArgumentException("Degree information is required");
        }

        // Validate CV file URL (if provided)
        if (application.getCvFileUrl() != null && !application.getCvFileUrl().trim().isEmpty()) {
            if (!isValidFileUrl(application.getCvFileUrl())) {
                throw new IllegalArgumentException("Invalid CV file URL");
            }
        }
    }

    private void validateStatus(String status) {
        if (!status.equalsIgnoreCase("PENDING")
                && !status.equalsIgnoreCase("ACCEPTED")
                && !status.equalsIgnoreCase("REJECTED")) {
            throw new IllegalArgumentException("Invalid status. Must be PENDING, ACCEPTED, or REJECTED");
        }
    }

    private boolean isFinalStatus(String status) {
        return "ACCEPTED".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status);
    }

    // External service calls
    private boolean userExists(String userId) {
        try {
            String url = USER_SERVICE_URL + "/" + userId;
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            // Log the error but don't throw - might be temporary service unavailability
            System.err.println("Error checking user existence: " + e.getMessage());
            return false; // Or handle differently based on requirements
        }
    }

    private boolean jobExists(String jobId) {
        try {
            String url = JOB_SERVICE_URL + "/" + jobId;
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Error checking job existence: " + e.getMessage());
            return false;
        }
    }

    private boolean hasAlreadyApplied(String jobSeekerId, String jobId) {
        List<JobApplication> existingApplications = applicationRepository.findByJobSeekerIdAndStatus(jobSeekerId, "PENDING");
        return existingApplications.stream()
                .anyMatch(app -> app.getJobId().equals(jobId));
    }

    // Utility validation methods
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[+]?[0-9]{10,15}$");
    }

    private boolean isValidFileUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("/uploads/"));
    }

    // Additional business logic methods
    public long getApplicationCountForJob(String jobId) {
        if (jobId == null || jobId.trim().isEmpty()) {
            throw new IllegalArgumentException("Job ID cannot be null or empty");
        }
        return applicationRepository.findByJobId(jobId).size();
    }

    public long getPendingApplicationsCountForJob(String jobId) {
        if (jobId == null || jobId.trim().isEmpty()) {
            throw new IllegalArgumentException("Job ID cannot be null or empty");
        }
        return applicationRepository.findByJobIdAndStatus(jobId, "PENDING").size();
    }

    public boolean canUserApply(String jobSeekerId, String jobId) {
        return userExists(jobSeekerId) && jobExists(jobId) && !hasAlreadyApplied(jobSeekerId, jobId);
    }
}
