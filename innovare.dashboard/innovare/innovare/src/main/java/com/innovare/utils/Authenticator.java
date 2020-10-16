package com.innovare.utils;

import java.util.Optional;

public class Authenticator {
	Optional < User > authenticate ( String email , String password )
    {
		
        User user = new User();
        return Optional.of( user );
        //        return Optional.empty();
    }

}
