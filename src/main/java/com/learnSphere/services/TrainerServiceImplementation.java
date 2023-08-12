package com.learnSphere.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.learnSphere.entity.Course;
import com.learnSphere.entity.Lesson;
import com.learnSphere.repository.CourseRepository;
import com.learnSphere.repository.lessonRepository;
@Service
public class TrainerServiceImplementation implements TrainerService{
    
	@Autowired
	private CourseRepository courseRepo;
	
	@Autowired
	private lessonRepository lessonRepo;
	@Override
	public String addCourse(Course course) {
		courseRepo.save(course);
		return "Course add successfully!";
	}
	@Override
	public String addLesson(Lesson lesson) {
		lessonRepo.save(lesson);
		return "lesson added successfully!";
	}
	@Override
	public Course getCourse(int courseId) {
		return courseRepo.findById(courseId).get();
	}
	@Override
	public List<Course> courseList() {
		return courseRepo.findAll();
	}
	@Override
	public Optional<Course> findCourseById(int id) {
		
		return courseRepo.findById(id);
	}
	@Override
	public boolean getById(int courseId) {
		
		return courseRepo.existsById(courseId);
	}
	@Override
	public boolean getByIdLesson(int lessonId) {
		
		return lessonRepo.existsById(lessonId);
	}

	
	


}
