package edu.cwi;

import io.javalin.security.RouteRole;

public enum AppRole implements RouteRole {
    PUBLIC, USER, ADMIN
}