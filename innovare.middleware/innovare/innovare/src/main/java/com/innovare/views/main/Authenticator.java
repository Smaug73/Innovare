package com.innovare.views.main;

import java.util.Optional;
import java.util.UUID;

public class Authenticator {
	Optional < User > authenticate ( String username , String password )
    {
        User user = new User( UUID.fromString( "76317e14-20a5-11ea-978f-2e728ce88125" ) , username );
        return Optional.of( user );
        //        return Optional.empty();
    }

}
