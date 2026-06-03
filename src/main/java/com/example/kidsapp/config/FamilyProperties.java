package com.example.kidsapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "kidsapp.family")
public class FamilyProperties {

    private List<UserProfile> parents = new ArrayList<>();
    private List<UserProfile> kids = new ArrayList<>();

    public List<UserProfile> getParents() { return parents; }
    public void setParents(List<UserProfile> parents) { this.parents = parents; }

    public List<UserProfile> getKids() { return kids; }
    public void setKids(List<UserProfile> kids) { this.kids = kids; }

    public static class UserProfile {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }

        public void setPassword(String password) { this.password = password; }
    }
}
