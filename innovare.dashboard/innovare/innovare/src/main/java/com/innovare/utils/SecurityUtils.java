package com.innovare.utils;

import java.util.Arrays;
import java.util.List;

import org.springframework.core.annotation.AnnotationUtils;


public class SecurityUtils {

	/*public static boolean isAccessGranted(Class<?> securedClass) {

		Authentication userAuthentication = SecurityContextHolder.getContext().getAuthentication();


		// Allow if no roles are required.
		Secured secured = AnnotationUtils.findAnnotation(securedClass, Secured.class);
		if (secured == null) {
			return true;
		}

		List<String> allowedRoles = Arrays.asList(secured.value());
		return userAuthentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.anyMatch(allowedRoles::contains);
	}*/
}
