package com.coforge.training.ims.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.coforge.training.ims.model.Address;
import com.coforge.training.ims.model.Dealer;
import com.coforge.training.ims.model.Product;
import com.coforge.training.ims.service.LoginService;
import com.coforge.training.ims.service.ProductService;

@Controller
public class LoginController {
	
	@Autowired
	private LoginService lService;
	
	@Autowired
	private ProductService pService;
	
	@RequestMapping("/")
    public String viewHomePage() {

        return "index";
    }
	
	@RequestMapping("/register")
	public String viewRegisterPage(Model model) {
		
		Dealer dealer=new Dealer();
		model.addAttribute("dealer", dealer);
		return "register"; //returns model and view
		
	}
	
	@PostMapping("/saveDealer")
	public String saveDealer(HttpServletRequest req,@ModelAttribute("dealer") Dealer dealer) {
		
		String s=req.getParameter("street");
		String c=req.getParameter("city");
		int p=Integer.parseInt(req.getParameter("pincode"));
		
		Address a=new Address();
		a.setStreet(s);
		a.setCity(c);
		a.setPincode(p);
		
		dealer.setAddress(a);
		a.setDealer(dealer);
			
		//User Form -> Controller -> Service -> Repository(DAO) -> JPA Repository -> Database
		
		lService.saveDealer(dealer); //Invokes Service Layer Methods for Database Logic Implementation
		
		return "index";
	}
	

	@GetMapping("/login")
	public String showLoginForm(Model theModel) {
	return "login";
	}
	
	@PostMapping("/loginDealer")
	public ModelAndView loginDealer(HttpServletRequest req,@ModelAttribute("dealer") Dealer dealer) {
		
		String email=req.getParameter("email");
		String pass=req.getParameter("password");
		String pass2=encryptPass(pass); //invokes encryptPass() method
		
		StringTokenizer st = new StringTokenizer(email, "@"); // breaks the email id based on '@' token
		String s2 = st.nextToken(); //email id without domain
		
		ModelAndView mav=null;
		
		Dealer d=lService.findByEmail(email);//Fetch Record/Object From Table Matching The EmailId 
		
		if(d==null) {
			
			mav =new ModelAndView("login");
			mav.addObject("error","User does not exist ");
			
		 }
		else if(email.equals(d.getEmail()) && pass2.equals(d.getPassword()))
		{

		req.getSession().setAttribute("user", s2); // creating a session

		mav = new ModelAndView("products");
		mav.addObject("dealer", d);
		
		List<Product> listProducts=pService.listAll(); //Fetches all records from Product table
		mav.addObject("listProducts", listProducts);
		}
		
		else
		{
		mav= new ModelAndView("login");
		mav.addObject("error", "Invalid Password");
		}

		return mav;
	}
	

	private String encryptPass(String pass) {
	Base64.Encoder encoder = Base64.getEncoder();
	String normalString = pass;
	String encodedString = encoder.encodeToString(
	normalString.getBytes(StandardCharsets.UTF_8) );
	return encodedString;
	}
	
	@GetMapping("/logout")
	public String logout(HttpServletRequest req) {
	req.getSession().removeAttribute("user");
	req.getSession().invalidate();
	return "index";
	}







}
