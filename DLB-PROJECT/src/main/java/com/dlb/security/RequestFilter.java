package com.dlb.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestFilter extends OncePerRequestFilter {

	@Autowired
	private UserDetailsSerrviceImpl userDetailsSerrviceImpl;
	@Autowired
	private JwtUtill jwtUtil;

	@Override
	protected void doFilterNestedErrorDispatch(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		HttpSession oldsession = request.getSession(false); // getting existing session
		String autherizationKey = null;
		String userName = null;
		String jwtToken = null;
		if(oldsession!=null && oldsession.getAttribute("Bearer")!=null) {
			autherizationKey=oldsession.getAttribute("Bearer").toString();
		}
		if (autherizationKey != null && autherizationKey.startsWith("Bearer")) {
			jwtToken = autherizationKey.substring(7);
			userName = jwtUtil.extraxtUserName(jwtToken);
		}
		if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = this.userDetailsSerrviceImpl.loadUserByUsername(userName);
			if (jwtUtil.validateToken(jwtToken, userDetails)) {
				UsernamePasswordAuthenticationToken upaToken = new UsernamePasswordAuthenticationToken(userDetails,
						null, userDetails.getAuthorities());
				upaToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(upaToken);
			}

		}//if
		filterChain.doFilter(request, response);
	}
	


}
