package com.reliance.grievance.service.impl;

import com.reliance.grievance.service.UserService;
import com.reliance.grievance.util.RelianceConstants;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

@Service
public class UserServiceImpl implements UserService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserServiceImpl.class);

    public String checkLDAPAuth(String webmailID, String password) {
        log.info("UserServiceImpl :: checkLDAPAuth() ");
        String statusCode="";
        statusCode = checkLDAPConn(RelianceConstants.ldapPrimURL, webmailID, password);
        if (("F".equalsIgnoreCase(statusCode)) || ("E".equalsIgnoreCase(statusCode)))
            statusCode = checkLDAPConn(RelianceConstants.ldapSecURL, webmailID, password);
        return statusCode;
    }

    public String checkLDAPConn(String ldapURL, String webmailID, String password) {
        log.info("UserServiceImpl :: checkLDAPConn() ");

        Hashtable<String, String> env = new Hashtable<String, String>();
        String status = "";
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapURL);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, webmailID);
        env.put(Context.SECURITY_CREDENTIALS, password);

        try {
            if ("".equals(password))
                status = "F";
            else{
                DirContext ctx = new InitialDirContext(env);
                status = "T";  // Success
                ctx.close();
            }
        } catch (AuthenticationNotSupportedException ex) {
            status = "E";  // Error
        } catch (AuthenticationException ex) {
            status = "F";  // Failure
        } catch (NamingException ex) {
            status = "E";  // Error
        }
        return status;
    }

}
