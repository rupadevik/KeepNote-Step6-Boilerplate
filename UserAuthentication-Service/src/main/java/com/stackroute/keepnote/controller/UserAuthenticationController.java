package com.stackroute.keepnote.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.stackroute.keepnote.exception.UserAlreadyExistsException;
import com.stackroute.keepnote.exception.UserNotFoundException;
import com.stackroute.keepnote.model.User;
import com.stackroute.keepnote.service.UserAuthenticationService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/*
 * As in this assignment, we are working on creating RESTful web service, hence annotate
 * the class with @RestController annotation. A class annotated with the @Controller annotation
 * has handler methods which return a view. However, if we use @ResponseBody annotation along
 * with @Controller annotation, it will return the data directly in a serialized 
 * format. Starting from Spring 4 and above, we can use @RestController annotation which 
 * is equivalent to using @Controller and @ResposeBody annotation
 */
@RestController
@CrossOrigin(origins = {"*"})
public class UserAuthenticationController {

	/*
	 * Autowiring should be implemented for the UserAuthenticationService. (Use
	 * Constructor-based autowiring) Please note that we should not create an
	 * object using the new keyword
	 */
	@Autowired
	private UserAuthenticationService authenticationService;

	public UserAuthenticationController(UserAuthenticationService authicationService) {
		this.authenticationService = authicationService;
	}

	/*
	 * Define a handler method which will create a specific user by reading the
	 * Serialized object from request body and save the user details in the
	 * database. This handler method should return any one of the status
	 * messages basis on different situations: 1. 201(CREATED) - If the user
	 * created successfully. 2. 409(CONFLICT) - If the userId conflicts with any
	 * existing user
	 * 
	 * This handler method should map to the URL "/api/v1/auth/register" using
	 * HTTP POST method
	 */

	@PostMapping(value = "/api/v1/auth/register")
	public ResponseEntity<User> registerUser(@RequestBody User user) {

		try {
			this.authenticationService.saveUser(user);
		} catch (UserAlreadyExistsException e) {
			// TODO Auto-generated catch block
			return new ResponseEntity<User>(HttpStatus.CONFLICT);
			// e.printStackTrace();
		}
		return new ResponseEntity<User>(user, HttpStatus.CREATED);
	}

	/*
	 * Define a handler method which will authenticate a user by reading the
	 * Serialized user object from request body containing the username and
	 * password. The username and password should be validated before proceeding
	 * ahead with JWT token generation. The user credentials will be validated
	 * against the database entries. The error should be return if validation is
	 * not successful. If credentials are validated successfully, then JWT token
	 * will be generated. The token should be returned back to the caller along
	 * with the API response. This handler method should return any one of the
	 * status messages basis on different situations: 1. 200(OK) - If login is
	 * successful 2. 401(UNAUTHORIZED) - If login is not successful
	 * 
	 * This handler method should map to the URL "/api/v1/auth/login" using HTTP
	 * POST method
	 */
	@PostMapping("/api/v1/auth/login")
	public ResponseEntity<?> getUser(@RequestBody User user) {
		String jwToken = "";
		Map<String, String> map = new HashMap<>();
		try {
			User userVo = authenticationService.findByUserIdAndPassword(user.getUserId(), user.getUserPassword());
			if (null != userVo) {
				jwToken = getToken(user.getUserId(), user.getUserPassword());
				map.clear();
				map.put("message", "User Successfully logged in");
				map.put("token", jwToken);
				return new ResponseEntity<>(map, HttpStatus.OK);
			}
			return new ResponseEntity<String>("LogIn Failed", HttpStatus.UNAUTHORIZED);
		} catch (Exception e) {
			map.clear();
			map.put("message", e.getMessage());
			map.put("token", null);
			return new ResponseEntity<String>("User not found", HttpStatus.UNAUTHORIZED);
		}
			
		}
		
	
	

	// Generate JWT token
	public String getToken(String username, String password) throws Exception {
		String SECRET = "secret";
		long EXPIRATION_TIME = 864_000_000; // 10 days
		Claims claims = Jwts.claims().setSubject(username);
		claims.put("UserId", username + "");
		claims.put("password", password);

		return Jwts.builder().setClaims(claims).setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(SignatureAlgorithm.HS512, SECRET).compact();

	}

}