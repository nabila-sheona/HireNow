package ApplicationService.demo.service;

import ApplicationService.demo.model.JobApplication;
import ApplicationService.demo.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private RestTemplate restTemplate;

    private static final String USER_SERVICE_URL = "http://UserService/api/users";
    private static final String JOB_SERVICE_URL = "http://JobService/api/jobs";

    public JobApplication createApplication(JobApplication application) {
        validateApplication(application);

        System.out.println("DEBUG: Validating user with ID: " + application.getJobSeekerId());

        if (!userExists(application.getJobSeekerId())) {
            throw new RuntimeException("Job seeker not found with ID: " + application.getJobSeekerId() +
                    ". Please ensure the user exists and has proper roles assigned.");
        }

        System.out.println("DEBUG: Validating job with ID: " + application.getJobId());

        if (!jobExists(application.getJobId())) {
            throw new RuntimeException("Job not found with ID: " + application.getJobId());
        }

        if (hasAlreadyApplied(application.getJobSeekerId(), application.getJobId())) {
            throw new RuntimeException("User has already applied for this job");
        }

        validateApplicationRequirements(application);

        return applicationRepository.save(application);
    }

    public List<JobApplication> getAllApplications() {
        return applicationRepository.findAll();
    }

    public List<JobApplication> getAllApplications(String sortBy, String sortDirection) {
        Sort sort = createSort(sortBy, sortDirection);
        return applicationRepository.findAll(sort);
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

        if (!jobExists(jobId)) {
            throw new RuntimeException("Job not found with ID: " + jobId);
        }

        return applicationRepository.findByJobId(jobId);
    }

    public List<JobApplication> getApplicationsByJob(String jobId, String sortBy, String sortDirection) {
        if (jobId == null || jobId.trim().isEmpty()) {
            throw new IllegalArgumentException("Job ID cannot be null or empty");
        }

        if (!jobExists(jobId)) {
            throw new RuntimeException("Job not found with ID: " + jobId);
        }

        Sort sort = createSort(sortBy, sortDirection);
        return applicationRepository.findByJobId(jobId, sort);
    }

    public List<JobApplication> getApplicationsByJobSeeker(String jobSeekerId) {
        if (jobSeekerId == null || jobSeekerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Job seeker ID cannot be null or empty");
        }

        if (!userExists(jobSeekerId)) {
            throw new RuntimeException("Job seeker not found with ID: " + jobSeekerId);
        }

        return applicationRepository.findByJobSeekerId(jobSeekerId);
    }

    public List<JobApplication> getApplicationsByJobSeeker(String jobSeekerId, String sortBy, String sortDirection) {
        if (jobSeekerId == null || jobSeekerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Job seeker ID cannot be null or empty");
        }

        if (!userExists(jobSeekerId)) {
            throw new RuntimeException("Job seeker not found with ID: " + jobSeekerId);
        }

        Sort sort = createSort(sortBy, sortDirection);
        return applicationRepository.findByJobSeekerId(jobSeekerId, sort);
    }

    public List<JobApplication> getApplicationsByJobAndStatus(String jobId, String status) {
        if (jobId == null || jobId.trim().isEmpty()) {
            throw new IllegalArgumentException("Job ID cannot be null or empty");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        validateStatus(status);

        if (!jobExists(jobId)) {
            throw new RuntimeException("Job not found with ID: " + jobId);
        }

        return applicationRepository.findByJobIdAndStatus(jobId, status);
    }

    public List<JobApplication> getApplicationsByJobAndStatus(String jobId, String status, String sortBy, String sortDirection) {
        if (jobId == null || jobId.trim().isEmpty()) {
            throw new IllegalArgumentException("Job ID cannot be null or empty");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        validateStatus(status);

        if (!jobExists(jobId)) {
            throw new RuntimeException("Job not found with ID: " + jobId);
        }

        Sort sort = createSort(sortBy, sortDirection);
        return applicationRepository.findByJobIdAndStatus(jobId, status, sort);
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

    public List<JobApplication> getApplicationsByJobSeekerAndStatus(String jobSeekerId, String status, String sortBy, String sortDirection) {
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

        Sort sort = createSort(sortBy, sortDirection);
        return applicationRepository.findByJobSeekerIdAndStatus(jobSeekerId, status, sort);
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

    // Sorting helper method

    public List<JobApplication> getApplicationsBySkill(String skill) {
        if (skill == null || skill.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill cannot be null or empty");
        }
        return applicationRepository.findBySkillsContainingIgnoreCase(skill);
    }

    public List<JobApplication> getApplicationsBySkill(String skill, String sortBy, String sortDirection) {
        if (skill == null || skill.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill cannot be null or empty");
        }
        Sort sort = createSort(sortBy, sortDirection);
        return applicationRepository.findBySkillsContainingIgnoreCase(skill, sort);
    }

    public List<JobApplication> getApplicationsByExperience(String experience) {
        if (experience == null || experience.trim().isEmpty()) {
            throw new IllegalArgumentException("Experience cannot be null or empty");
        }
        return applicationRepository.findByExperienceContainingIgnoreCase(experience);
    }

    public List<JobApplication> getApplicationsByExperience(String experience, String sortBy, String sortDirection) {
        if (experience == null || experience.trim().isEmpty()) {
            throw new IllegalArgumentException("Experience cannot be null or empty");
        }
        Sort sort = createSort(sortBy, sortDirection);
        return applicationRepository.findByExperienceContainingIgnoreCase(experience, sort);
    }

    public List<JobApplication> getApplicationsByDegree(String degree) {
        if (degree == null || degree.trim().isEmpty()) {
            throw new IllegalArgumentException("Degree cannot be null or empty");
        }
        return applicationRepository.findByDegreeContainingIgnoreCase(degree);
    }

    public List<JobApplication> getApplicationsByDegree(String degree, String sortBy, String sortDirection) {
        if (degree == null || degree.trim().isEmpty()) {
            throw new IllegalArgumentException("Degree cannot be null or empty");
        }
        Sort sort = createSort(sortBy, sortDirection);
        return applicationRepository.findByDegreeContainingIgnoreCase(degree, sort);
    }

    public List<JobApplication> getApplicationsByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        validateStatus(status);
        return applicationRepository.findByStatus(status.toUpperCase());
    }

    public List<JobApplication> getApplicationsByStatus(String status, String sortBy, String sortDirection) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        validateStatus(status);
        Sort sort = createSort(sortBy, sortDirection);
        return applicationRepository.findByStatus(status.toUpperCase(), sort);
    }

    // Advanced search with multiple criteria
    public List<JobApplication> searchApplications(String skill, String experience, String degree, String status, String sortBy, String sortDirection) {
        // Start with all applications
        List<JobApplication> applications = applicationRepository.findAll();

        // Apply filters
        if (skill != null && !skill.trim().isEmpty()) {
            applications = applications.stream()
                    .filter(app -> app.getSkills() != null &&
                            app.getSkills().stream()
                                    .anyMatch(s -> s.toLowerCase().contains(skill.toLowerCase())))
                    .toList();
        }

        if (experience != null && !experience.trim().isEmpty()) {
            applications = applications.stream()
                    .filter(app -> app.getExperience() != null &&
                            app.getExperience().toLowerCase().contains(experience.toLowerCase()))
                    .toList();
        }

        if (degree != null && !degree.trim().isEmpty()) {
            applications = applications.stream()
                    .filter(app -> app.getDegree() != null &&
                            app.getDegree().toLowerCase().contains(degree.toLowerCase()))
                    .toList();
        }

        if (status != null && !status.trim().isEmpty()) {
            validateStatus(status);
            applications = applications.stream()
                    .filter(app -> app.getStatus() != null &&
                            app.getStatus().equalsIgnoreCase(status))
                    .toList();
        }

        // Apply sorting
        return sortApplications(applications, sortBy, sortDirection);
    }

    // Enhanced sorting helper method
    private Sort createSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "applicationDate"; 
        }

        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            sortDirection = "desc";
        }

        // Validate and map field names
        String validatedSortBy = validateAndMapSortField(sortBy);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        return Sort.by(direction, validatedSortBy);
    }

    private String validateAndMapSortField(String sortBy) {
        // Map request field names to entity field names
        switch (sortBy.toLowerCase()) {
            case "date":
            case "applicationdate":
                return "applicationDate";
            case "name":
                return "name";
            case "email":
                return "email";
            case "status":
                return "status";
            case "experience":
                return "experience";
            case "degree":
                return "degree";
            case "jobid":
                return "jobId";
            case "jobseekerid":
                return "jobSeekerId";
            case "skills":
                return "skills";
            case "phone":
                return "phone";
            default:
                return "applicationDate"; 
        }
    }

    // Manual sorting method for advanced searches
    private List<JobApplication> sortApplications(List<JobApplication> applications, String sortBy, String sortDirection) {
        if (sortBy == null && sortDirection == null) {
            return applications;
        }

        String validatedSortBy = validateAndMapSortField(sortBy != null ? sortBy : "applicationDate");
        boolean ascending = !"desc".equalsIgnoreCase(sortDirection);

        List<JobApplication> sortedApplications = applications.stream()
                .sorted((a1, a2) -> {
                    int result = 0;
                    switch (validatedSortBy.toLowerCase()) {
                        case "applicationdate":
                            result = a1.getApplicationDate().compareTo(a2.getApplicationDate());
                            break;
                        case "name":
                            result = a1.getName().compareToIgnoreCase(a2.getName());
                            break;
                        case "email":
                            result = a1.getEmail().compareToIgnoreCase(a2.getEmail());
                            break;
                        case "status":
                            result = a1.getStatus().compareToIgnoreCase(a2.getStatus());
                            break;
                        case "experience":
                            result = a1.getExperience().compareToIgnoreCase(a2.getExperience());
                            break;
                        case "degree":
                            result = a1.getDegree().compareToIgnoreCase(a2.getDegree());
                            break;
                        case "skills":
                            // Sort by first skill alphabetically
                            String skills1 = a1.getSkills() != null && !a1.getSkills().isEmpty() ?
                                    a1.getSkills().get(0) : "";
                            String skills2 = a2.getSkills() != null && !a2.getSkills().isEmpty() ?
                                    a2.getSkills().get(0) : "";
                            result = skills1.compareToIgnoreCase(skills2);
                            break;
                        case "phone":
                            result = a1.getPhone().compareToIgnoreCase(a2.getPhone());
                            break;
                        default:
                            result = a1.getApplicationDate().compareTo(a2.getApplicationDate());
                    }
                    return ascending ? result : -result;
                })
                .toList();

        return sortedApplications;
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

    // External service calls - IMPROVED with better error handling
    private boolean userExists(String userId) {
        try {
            String url = USER_SERVICE_URL + "/" + userId;
            System.out.println("DEBUG: Calling User Service URL: " + url);

            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            System.out.println("DEBUG: User Service response status: " + response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("DEBUG: User validation successful for ID: " + userId);
                return true;
            } else {
                System.out.println("DEBUG: User validation failed with status: " + response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            System.err.println("ERROR checking user existence for ID " + userId + ": " + e.getMessage());
            // Log the full exception for debugging
            e.printStackTrace();
            return false;
        }
    }

    private boolean jobExists(String jobId) {
        try {
            String url = JOB_SERVICE_URL + "/" + jobId;
            System.out.println("DEBUG: Calling Job Service URL: " + url);

            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            System.out.println("DEBUG: Job Service response status: " + response.getStatusCode());

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Error checking job existence: " + e.getMessage());
            e.printStackTrace();
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