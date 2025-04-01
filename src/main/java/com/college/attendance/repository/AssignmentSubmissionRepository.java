package com.college.attendance.repository;

import com.college.attendance.model.Assignment;
import com.college.attendance.model.AssignmentSubmission;
import com.college.attendance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    List<AssignmentSubmission> findByAssignment(Assignment assignment);
    
    List<AssignmentSubmission> findByStudent(User student);
    
    List<AssignmentSubmission> findByAssignmentAndStudent(Assignment assignment, User student);
    
    Optional<AssignmentSubmission> findTopByAssignmentAndStudentOrderBySubmissionDateDesc(
        Assignment assignment, User student);
    
    List<AssignmentSubmission> findByAssignmentAndGraded(Assignment assignment, boolean graded);
} 