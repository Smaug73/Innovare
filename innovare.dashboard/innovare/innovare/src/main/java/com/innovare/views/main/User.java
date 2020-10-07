package com.innovare.views.main;

import java.util.Objects;
import java.util.UUID;

public class User {
	private UUID id;
    private String username;

    public User ( UUID id , String username )
    {
        this.id = Objects.requireNonNull( id );
        this.username = Objects.requireNonNull( username );
    }

    // ------------|  Object  |---------------------------------
    @Override
    public boolean equals ( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        User user = ( User ) o;
        return id.equals( user.id );
    }

    @Override
    public int hashCode ( )
    {
        return Objects.hash( id );
    }

    @Override
    public String toString ( )
    {
        return "User{ " +
                "id=" + id +
                " | username='" + username + '\'' +
                " }";
    }

}
