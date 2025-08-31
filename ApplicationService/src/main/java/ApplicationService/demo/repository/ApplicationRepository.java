package ApplicationService.demo.repository;

import ApplicationService.demo.model.JobApplication;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ApplicationRepository extends MongoRepository<JobApplication, String> {

    List<JobApplication> findByJobId(String jobId);

    List<JobApplication> findByJobSeekerId(String jobSeekerId);

    List<JobApplication> findByJobIdAndStatus(String jobId, String status);

    List<JobApplication> findByJobSeekerIdAndStatus(String jobSeekerId, String status);
}
