package JobService.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "jobs")
public class Job {
    @Id
    private String id;
    private String companyName;
    private String jobTitle;
    private Double expectedSalary;
    private String preference; 
    private List<String> requiredSkills;
    private String experience; 
    private String workingHours;
    private String prerequisites;
    private String hirerId;
    private LocalDateTime postedDate;

    public Job() {
        this.postedDate = LocalDateTime.now();
    }

    public Job(String companyName, String jobTitle, Double expectedSalary, String preference,
               List<String> requiredSkills, String experience, String workingHours,
               String prerequisites, String hirerId) {
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.expectedSalary = expectedSalary;
        this.preference = preference;
        this.requiredSkills = requiredSkills;
        this.experience = experience;
        this.workingHours = workingHours;
        this.prerequisites = prerequisites;
        this.hirerId = hirerId;
        this.postedDate = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public Double getExpectedSalary() { return expectedSalary; }
    public void setExpectedSalary(Double expectedSalary) { this.expectedSalary = expectedSalary; }
    public String getPreference() { return preference; }
    public void setPreference(String preference) { this.preference = preference; }
    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }
    public String getPrerequisites() { return prerequisites; }
    public void setPrerequisites(String prerequisites) { this.prerequisites = prerequisites; }
    public String getHirerId() { return hirerId; }
    public void setHirerId(String hirerId) { this.hirerId = hirerId; }
    public LocalDateTime getPostedDate() { return postedDate; }
    public void setPostedDate(LocalDateTime postedDate) { this.postedDate = postedDate; }
}