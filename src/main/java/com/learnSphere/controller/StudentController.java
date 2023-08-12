package com.learnSphere.controller;

import java.io.Console;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.learnSphere.entity.Comments;
import com.learnSphere.entity.Course;
import com.learnSphere.entity.Lesson;
import com.learnSphere.entity.Users;

import com.learnSphere.services.RazorpayService;
import com.learnSphere.services.StudentServices;
import com.razorpay.RazorpayException;

import jakarta.servlet.http.HttpSession;

@Controller
public class StudentController {
	@Autowired
	private StudentServices service;
	
	
	 @Autowired
	 private RazorpayService razorpayService;
	
	@GetMapping("/showPurchaseCourse")
	public String showPurchaseCourse(Model model,HttpSession session ) {
		List<Course> courseList=service.courseList();
		model.addAttribute("courseList",courseList);
		Integer userId=(Integer) session.getAttribute("userId");
		model.addAttribute("userId", userId);
		return"purchaseCourse";
	}
	
	@PostMapping("/buy-course/{id}")
	public String getUserById(@PathVariable("id") int id,Model model,HttpSession session,RedirectAttributes res) throws RazorpayException {
		Optional<Course> course=service.findCourseById(id);			
		Integer userId=(Integer) session.getAttribute("userId");		
		Users user = service.findById(userId).orElse(null);
		if(user.getCourses().contains(course.get())) {	
			res.addFlashAttribute("message", "⚠ You already purchased! Choose another course.");
			return "redirect:/showPurchaseCourse";
		}
		else {		
			 String razorpayOrderId = razorpayService.createOrder(course.get().getCoursePrice());
		     model.addAttribute("course", course.get());
		     model.addAttribute("user", user);
		     model.addAttribute("razorpayOrderId", razorpayOrderId);   
		     razorpayService.addCourseOrderIdMapping(razorpayOrderId, course.get());
			return "payment-page";
		}	
	}
	
	@PostMapping("/verify-payment")
    public String verifyPayment(@RequestBody  Map<String, String> paymentData, HttpSession session) {	
		String paymentId = paymentData.get("razorpay_payment_id");
        String orderId = paymentData.get("razorpay_order_id");
        String signature = paymentData.get("razorpay_signature");
        
        // Verify the payment signature using Razorpay's provided verification methods
        boolean isSignatureValid = razorpayService.verifyPaymentSignature(paymentId, orderId, signature);
        if (isSignatureValid) {  	
        	Integer userId=(Integer) session.getAttribute("userId");
            Users user = service.findById(userId).orElse(null);
            Course course = razorpayService.fetchCourseByOrderId(orderId); 
            if (course != null && user != null) {
            	user.getCourses().add(course);
            	service.saveCourse(user);// Save the updated user

                return "redirect:/payment-success"; // Redirect to success page
            } else {
                return "redirect:/payment-failure"; // Handle errors
            }
        } else {
            return "redirect:/payment-failure"; // Payment verification failed
        }
    }

//	@PostMapping("/buy")
//    public String buyCourse(@RequestParam int courseId, @RequestParam int userId) {
//        Course course = service.findCourseById(courseId).orElse(null);
//        Users user = service.findById(userId).orElse(null);
//
//        if (course != null && user != null) {
//        	user.getCourses().add(course);
//        	service.saveCourse(user);
//        	
//        }
//
//        return "redirect:/showPurchaseCourse";
//    } 
	
	
	@GetMapping("/user/{id}")
	public String myCourses(@PathVariable("id") int id, Model model) {
		Users user=service.getUserById(id);
		if (user != null) {
			model.addAttribute("user", user);
		    model.addAttribute("courseSet", user.getCourses());
		    return "myCourses";		

        } else {
            return "redirect:/studentHome"; // Redirect to a students list page or error page
        }	
	}
	@GetMapping("/user/course/{courseId}")
	public String myLessons(@PathVariable int courseId,Model model,HttpSession session) {
		
		Integer userId=(Integer) session.getAttribute("userId");
		Users user = service.findById(userId).orElse(null);
		Course course=service.getCourseBtIdCourse(courseId);
		if(course!=null) {
			model.addAttribute("user", user);
			model.addAttribute("course", course);
			model.addAttribute("lesson", course.getLessons());
			return "myLessons";
		}
		else {
			return "redirect:/myLessons";
		}
		
	}
	
	@GetMapping("/user/course/lessons/{lessonId}")
	public String myLectures(@PathVariable int lessonId,Model model, HttpSession session) {
		Lesson lesson=service.getLessonById(lessonId);
		Integer userId=(Integer) session.getAttribute("userId");
		Users user = service.findById(userId).orElse(null);
		
		model.addAttribute("user", user);
		model.addAttribute("lesson", lesson);
		model.addAttribute("comments", lesson.getComments());
		model.addAttribute("course", lesson.getCourse());
		model.addAttribute("newComment", new Comments());
		return"lecture";
		
	}
	
	
	
	@PostMapping("/user/course/lessons/{lessonId}/comments")
    public String postComment(@PathVariable int lessonId, @ModelAttribute Comments newComment) {
        Lesson lesson = service.findLessonById(lessonId);
        if (lesson != null) {
            Comments comment = new Comments();
            comment.setEmail(newComment.getEmail());
            comment.setComment(newComment.getComment());  
            comment.setLesson(lesson);
            service.saveCooment(comment);
        }
        return "redirect:/user/course/lessons/" + lessonId;
    }
	
	
	
}
