package JobService.demo.service;

import JobService.demo.model.Job;
import JobService.demo.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private RestTemplate restTemplate;

    // === EXISTING METHODS ===
    public Job createJob(Job job) {
        validateHirer(job.getHirerId());
        validateJobData(job);
        return jobRepository.save(job);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Optional<Job> getJobById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Job ID cannot be null or empty");
        }
        return jobRepository.findById(id);
    }

    public List<Job> getJobsByHirer(String hirerId) {
        if (hirerId == null || hirerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Hirer ID cannot be null or empty");
        }
        return jobRepository.findByHirerId(hirerId);
    }

    public List<Job> searchJobsByCompany(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be null or empty");
        }
        return jobRepository.findByCompanyNameContainingIgnoreCase(companyName);
    }

    public List<Job> searchJobsByTitle(String jobTitle) {
        if (jobTitle == null || jobTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Job title cannot be null or empty");
        }
        return jobRepository.findByJobTitleContainingIgnoreCase(jobTitle);
    }

    public Job updateJob(String id, Job jobDetails) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Job ID cannot be null or empty");
        }

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));

        validateHirer(jobDetails.getHirerId());
        validateJobData(jobDetails);

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
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Job ID cannot be null or empty");
        }

        if (!jobRepository.existsById(id)) {
            throw new RuntimeException("Job not found with id: " + id);
        }

        jobRepository.deleteById(id);
    }

    private void validateHirer(String hirerId) {
        if (hirerId == null || hirerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Hirer ID cannot be null or empty");
        }

        try {
            String userServiceUrl = "http://UserService/api/users/" + hirerId;
            ResponseEntity<UserResponse> response = restTemplate.getForEntity(userServiceUrl, UserResponse.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("User not found with id: " + hirerId);
            }

            UserResponse user = response.getBody();
            if (user == null || !"JOB_HIRER".equalsIgnoreCase(user.getRole())) {
                throw new RuntimeException("User is not authorized to post jobs. Only JOB_HIRER role allowed.");
            }

        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("User not found with id: " + hirerId);
        } catch (Exception e) {
            throw new RuntimeException("Error validating user: " + e.getMessage());
        }
    }

    private void validateJobData(Job job) {
        if (job.getCompanyName() == null || job.getCompanyName().trim().isEmpty()) {
            throw new IllegalArgumentException("Company name is required");
        }

        if (job.getJobTitle() == null || job.getJobTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Job title is required");
        }

        if (job.getExpectedSalary() == null || job.getExpectedSalary() <= 0) {
            throw new IllegalArgumentException("Expected salary must be greater than 0");
        }

        if (job.getPreference() == null || job.getPreference().trim().isEmpty()) {
            throw new IllegalArgumentException("Job preference is required");
        }

        if (!isValidPreference(job.getPreference())) {
            throw new IllegalArgumentException("Invalid job preference. Must be REMOTE, ONSITE, or HYBRID");
        }

        if (job.getRequiredSkills() == null || job.getRequiredSkills().isEmpty()) {
            throw new IllegalArgumentException("At least one required skill is needed");
        }

        if (job.getExperience() == null || job.getExperience().trim().isEmpty()) {
            throw new IllegalArgumentException("Experience requirement is required");
        }

        if (job.getWorkingHours() == null || job.getWorkingHours().trim().isEmpty()) {
            throw new IllegalArgumentException("Working hours are required");
        }
    }

    private boolean isValidPreference(String preference) {
        return "REMOTE".equalsIgnoreCase(preference) ||
                "ONSITE".equalsIgnoreCase(preference) ||
                "HYBRID".equalsIgnoreCase(preference);
    }

    // === NEW SEARCH AND SORT METHODS ===
    public List<Job> searchJobsByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword cannot be null or empty");
        }
        return jobRepository.findByCompanyNameContainingIgnoreCaseOrJobTitleContainingIgnoreCase(keyword, keyword);
    }

    public List<Job> searchJobsBySkill(String skill) {
        if (skill == null || skill.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill cannot be null or empty");
        }
        
        List<Job> allJobs = jobRepository.findAll();
        return allJobs.stream()
                .filter(job -> job.getRequiredSkills() != null)
                .filter(job -> job.getRequiredSkills().stream()
                        .anyMatch(jobSkill -> jobSkill.toLowerCase().contains(skill.toLowerCase())))
                .collect(Collectors.toList());
    }

    public List<Job> advancedSearch(String companyName, String jobTitle, List<String> skills, Double minSalary, Double maxSalary) {
        List<Job> allJobs = jobRepository.findAll();
        
        return allJobs.stream()
                .filter(job -> companyName == null || 
                        (job.getCompanyName() != null && job.getCompanyName().toLowerCase().contains(companyName.toLowerCase())))
                .filter(job -> jobTitle == null || 
                        (job.getJobTitle() != null && job.getJobTitle().toLowerCase().contains(jobTitle.toLowerCase())))
                .filter(job -> skills == null || skills.isEmpty() || 
                        (job.getRequiredSkills() != null && 
                         job.getRequiredSkills().stream()
                            .anyMatch(jobSkill -> skills.stream()
                                .anyMatch(searchSkill -> jobSkill.toLowerCase().contains(searchSkill.toLowerCase())))))
                .filter(job -> minSalary == null || 
                        (job.getExpectedSalary() != null && job.getExpectedSalary() >= minSalary))
                .filter(job -> maxSalary == null || 
                        (job.getExpectedSalary() != null && job.getExpectedSalary() <= maxSalary))
                .collect(Collectors.toList());
    }

    public List<Job> getAllJobsSortedByDate(boolean ascending) {
        return ascending ? jobRepository.findAllByOrderByPostedDateAsc() 
                        : jobRepository.findAllByOrderByPostedDateDesc();
    }

    public List<Job> getAllJobsSortedBySalary(boolean ascending) {
        return ascending ? jobRepository.findAllByOrderByExpectedSalaryAsc() 
                        : jobRepository.findAllByOrderByExpectedSalaryDesc();
    }

    public List<Job> getAllJobsSortedByCompany() {
        return jobRepository.findAllByOrderByCompanyNameAsc();
    }

    public List<Job> sortJobs(List<Job> jobs, String sortBy, String sortOrder) {
        if (jobs == null) {
            return new ArrayList<>();
        }
        
        List<Job> sortedJobs = new ArrayList<>(jobs);
        boolean ascending = !"desc".equalsIgnoreCase(sortOrder);
        
        switch (sortBy.toLowerCase()) {
            case "salary":
                sortedJobs.sort((j1, j2) -> {
                    if (j1.getExpectedSalary() == null && j2.getExpectedSalary() == null) return 0;
                    if (j1.getExpectedSalary() == null) return ascending ? -1 : 1;
                    if (j2.getExpectedSalary() == null) return ascending ? 1 : -1;
                    return ascending ? 
                        Double.compare(j1.getExpectedSalary(), j2.getExpectedSalary()) :
                        Double.compare(j2.getExpectedSalary(), j1.getExpectedSalary());
                });
                break;
                
            case "date":
                sortedJobs.sort((j1, j2) -> ascending ? 
                    j1.getPostedDate().compareTo(j2.getPostedDate()) :
                    j2.getPostedDate().compareTo(j1.getPostedDate()));
                break;
                
            case "company":
                sortedJobs.sort((j1, j2) -> {
                    if (j1.getCompanyName() == null && j2.getCompanyName() == null) return 0;
                    if (j1.getCompanyName() == null) return ascending ? -1 : 1;
                    if (j2.getCompanyName() == null) return ascending ? 1 : -1;
                    return ascending ? 
                        j1.getCompanyName().compareToIgnoreCase(j2.getCompanyName()) :
                        j2.getCompanyName().compareToIgnoreCase(j1.getCompanyName());
                });
                break;
                
            case "title":
                sortedJobs.sort((j1, j2) -> {
                    if (j1.getJobTitle() == null && j2.getJobTitle() == null) return 0;
                    if (j1.getJobTitle() == null) return ascending ? -1 : 1;
                    if (j2.getJobTitle() == null) return ascending ? 1 : -1;
                    return ascending ? 
                        j1.getJobTitle().compareToIgnoreCase(j2.getJobTitle()) :
                        j2.getJobTitle().compareToIgnoreCase(j1.getJobTitle());
                });
                break;
                
            default:
                sortedJobs.sort((j1, j2) -> j2.getPostedDate().compareTo(j1.getPostedDate()));
        }
        
        return sortedJobs;
    }

    public static class UserResponse {
        private String id;
        private String username;
        private String email;
        private String role;
        private String phone;
        private String companyName;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
    }
}