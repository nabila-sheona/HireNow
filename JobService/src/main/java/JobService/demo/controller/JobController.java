package JobService.demo.controller;

import JobService.demo.model.Job;
import JobService.demo.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    // === EXISTING ENDPOINTS ===
    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody Job job) {
        try {
            Job createdJob = jobService.createJob(job);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdJob);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Validation Error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ErrorResponse("Authorization Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Server Error", "An error occurred while creating the job"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllJobs() {
        try {
            List<Job> jobs = jobService.getAllJobs();
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Server Error", "An error occurred while fetching jobs"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJobById(@PathVariable String id) {
        try {
            Optional<Job> job = jobService.getJobById(id);
            return job.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Validation Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Server Error", "An error occurred while fetching the job"));
        }
    }

    @GetMapping("/hirer/{hirerId}")
    public ResponseEntity<?> getJobsByHirer(@PathVariable String hirerId) {
        try {
            List<Job> jobs = jobService.getJobsByHirer(hirerId);
            return ResponseEntity.ok(jobs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Validation Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Server Error", "An error occurred while fetching jobs"));
        }
    }

    @GetMapping("/search/company")
    public ResponseEntity<?> searchJobsByCompany(@RequestParam String companyName) {
        try {
            List<Job> jobs = jobService.searchJobsByCompany(companyName);
            return ResponseEntity.ok(jobs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Validation Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Server Error", "An error occurred while searching jobs"));
        }
    }

    @GetMapping("/search/title")
    public ResponseEntity<?> searchJobsByTitle(@RequestParam String jobTitle) {
        try {
            List<Job> jobs = jobService.searchJobsByTitle(jobTitle);
            return ResponseEntity.ok(jobs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Validation Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Server Error", "An error occurred while searching jobs"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateJob(@PathVariable String id, @RequestBody Job jobDetails) {
        try {
            Job updatedJob = jobService.updateJob(id, jobDetails);
            return ResponseEntity.ok(updatedJob);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Validation Error", e.getMessage()));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ErrorResponse("Authorization Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Server Error", "An error occurred while updating the job"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable String id) {
        try {
            jobService.deleteJob(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Validation Error", e.getMessage()));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Server Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Server Error", "An error occurred while deleting the job"));
        }
    }

    // === NEW SEARCH AND SORT ENDPOINTS ===
    @GetMapping("/search")
    public ResponseEntity<?> searchJobs(@RequestParam String keyword) {
        try {
            List<Job> jobs = jobService.searchJobsByKeyword(keyword);
            return ResponseEntity.ok(jobs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Validation Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Server Error", "An error occurred while searching jobs"));
        }
    }

    @GetMapping("/search/skills")
    public ResponseEntity<?> searchJobsBySkill(@RequestParam String skill) {
        try {
            List<Job> jobs = jobService.searchJobsBySkill(skill);
            return ResponseEntity.ok(jobs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Validation Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Server Error", "An error occurred while searching jobs by skill"));
        }
    }

    @GetMapping("/search/advanced")
    public ResponseEntity<?> advancedSearch(
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) List<String> skills,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        try {
            List<Job> jobs = jobService.advancedSearch(companyName, jobTitle, skills, minSalary, maxSalary);
            
            if (sortBy != null) {
                jobs = jobService.sortJobs(jobs, sortBy, sortOrder != null ? sortOrder : "asc");
            }
            
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Server Error", "An error occurred while performing advanced search"));
        }
    }

    @GetMapping("/sorted/date")
    public ResponseEntity<?> getJobsSortedByDate(@RequestParam(defaultValue = "desc") String order) {
        try {
            boolean ascending = "asc".equalsIgnoreCase(order);
            List<Job> jobs = jobService.getAllJobsSortedByDate(ascending);
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Server Error", "An error occurred while fetching sorted jobs"));
        }
    }

    @GetMapping("/sorted/salary")
    public ResponseEntity<?> getJobsSortedBySalary(@RequestParam(defaultValue = "desc") String order) {
        try {
            boolean ascending = "asc".equalsIgnoreCase(order);
            List<Job> jobs = jobService.getAllJobsSortedBySalary(ascending);
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Server Error", "An error occurred while fetching sorted jobs"));
        }
    }

    @GetMapping("/sorted/company")
    public ResponseEntity<?> getJobsSortedByCompany() {
        try {
            List<Job> jobs = jobService.getAllJobsSortedByCompany();
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Server Error", "An error occurred while fetching sorted jobs"));
        }
    }

    @GetMapping("/search-sort")
    public ResponseEntity<?> searchAndSortJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String skill,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        try {
            List<Job> jobs;
            
            if (keyword != null) {
                jobs = jobService.searchJobsByKeyword(keyword);
            } else if (company != null) {
                jobs = jobService.searchJobsByCompany(company);
            } else if (title != null) {
                jobs = jobService.searchJobsByTitle(title);
            } else if (skill != null) {
                jobs = jobService.searchJobsBySkill(skill);
            } else {
                jobs = jobService.getAllJobs();
            }
            
            jobs = jobService.sortJobs(jobs, sortBy, sortOrder);
            
            return ResponseEntity.ok(jobs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Validation Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Server Error", "An error occurred while searching and sorting jobs"));
        }
    }

    public static class ErrorResponse {
        private String error;
        private String message;
        private long timestamp;

        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}