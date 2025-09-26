package JobService.demo.repository;

import JobService.demo.model.Job;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface JobRepository extends MongoRepository<Job, String> {
    List<Job> findByHirerId(String hirerId);
    List<Job> findByCompanyNameContainingIgnoreCase(String companyName);
    List<Job> findByJobTitleContainingIgnoreCase(String jobTitle);
    
    List<Job> findByCompanyNameContainingIgnoreCaseOrJobTitleContainingIgnoreCase(String companyName, String jobTitle);
    
    List<Job> findAllByOrderByPostedDateDesc();
    List<Job> findAllByOrderByPostedDateAsc();
    List<Job> findAllByOrderByExpectedSalaryDesc();
    List<Job> findAllByOrderByExpectedSalaryAsc();
    List<Job> findAllByOrderByCompanyNameAsc();
}