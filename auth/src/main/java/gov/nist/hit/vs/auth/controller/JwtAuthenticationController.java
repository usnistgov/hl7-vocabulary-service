/**
 * @author Ismail Mellouli (NIST)
 *
 */
package gov.nist.hit.vs.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.nist.hit.vs.auth.config.JwtTokenUtil;
import gov.nist.hit.vs.auth.domain.ApplicationUser;
import gov.nist.hit.vs.auth.domain.JwtRequest;
import gov.nist.hit.vs.auth.domain.JwtResponse;
import gov.nist.hit.vs.auth.service.UserService;
import gov.nist.hit.vs.auth.service.impl.UserDetailsServiceImpl;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@CrossOrigin
@ApiIgnore
@RequestMapping("/auth")
public class JwtAuthenticationController {

	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	@Autowired
	private UserService userService;
	
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
		authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
		final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
		final String token = jwtTokenUtil.generateToken(userDetails);
		return ResponseEntity.ok(new JwtResponse(token));
	}

//	@PostMapping("/register")
//	public String signUp(@RequestBody ApplicationUser user) throws Exception {
//
//		if (user.getUsername() != null && user.getPassword() != null) {
//
//			if (userService.usernameExist(user.getUsername())) {
//				throw new Exception("Username already exists");
//			} else {
//				BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//				user.setPassword(passwordEncoder.encode(user.getPassword()));
//				userService.saveUser(user);
//				return "Account successfully created";
//			}
//
//		} else {
//			throw new Exception("INVALID_CREDENTIALS");
//		}
//
//	}

	private void authenticate(String username, String password) throws Exception {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}
	}
}
