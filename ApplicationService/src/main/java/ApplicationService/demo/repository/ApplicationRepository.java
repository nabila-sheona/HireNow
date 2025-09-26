package ApplicationService.demo.repository;

import ApplicationService.demo.model.JobApplication;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ApplicationRepository extends MongoRepository<JobApplication, String> {

    List<JobApplication> findByJobId(String jobId);
    List<JobApplication> findByJobId(String jobId, Sort sort);

    List<JobApplication> findByJobSeekerId(String jobSeekerId);
    List<JobApplication> findByJobSeekerId(String jobSeekerId, Sort sort);

    List<JobApplication> findByJobIdAndStatus(String jobId, String status);
    List<JobApplication> findByJobIdAndStatus(String jobId, String status, Sort sort);

    List<JobApplication> findByJobSeekerIdAndStatus(String jobSeekerId, String status);
    List<JobApplication> findByJobSeekerIdAndStatus(String jobSeekerId, String status, Sort sort);

    List<JobApplication> findBySkillsContainingIgnoreCase(String skill);
    List<JobApplication> findBySkillsContainingIgnoreCase(String skill, Sort sort);

    List<JobApplication> findByExperienceContainingIgnoreCase(String experience);
    List<JobApplication> findByExperienceContainingIgnoreCase(String experience, Sort sort);

    List<JobApplication> findByDegreeContainingIgnoreCase(String degree);
    List<JobApplication> findByDegreeContainingIgnoreCase(String degree, Sort sort);

    List<JobApplication> findByStatus(String status);
    List<JobApplication> findByStatus(String status, Sort sort);

    List<JobApplication> findAll(Sort sort);
}