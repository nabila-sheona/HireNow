package ApplicationService.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "applications")
public class JobApplication {

    @Id
    private String id;
    private String jobId;
    private String jobSeekerId;
    private String name;
    private String phone;
    private String email;
    private String cvFileUrl;
    private List<String> skills;
    private String experience;
    private String degree;
    private String status; // PENDING, ACCEPTED, REJECTED
    private LocalDateTime applicationDate;

    // Constructors
    public JobApplication() {
        this.status = "PENDING";
        this.applicationDate = LocalDateTime.now();
    }

    public JobApplication(String jobId, String jobSeekerId, String name, String phone,
            String email, String cvFileUrl, List<String> skills,
            String experience, String degree) {
        this.jobId = jobId;
        this.jobSeekerId = jobSeekerId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.cvFileUrl = cvFileUrl;
        this.skills = skills;
        this.experience = experience;
        this.degree = degree;
        this.status = "PENDING";
        this.applicationDate = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobSeekerId() {
        return jobSeekerId;
    }

    public void setJobSeekerId(String jobSeekerId) {
        this.jobSeekerId = jobSeekerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCvFileUrl() {
        return cvFileUrl;
    }

    public void setCvFileUrl(String cvFileUrl) {
        this.cvFileUrl = cvFileUrl;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = applicationDate;
    }
}
