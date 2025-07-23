package com.reliance.grievance.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaRedirectController {

    /**
     * Match any path that:
     *  • Does NOT contain a dot (so we ignore .js/.css/.png/etc)
     *  • Does NOT start with 'api' (or whatever your REST prefix is)
     */
    @GetMapping("/{path:[^\\.]*}")
    public String forwardRoot() {
        return "forward:/index.html";
    }

    @GetMapping("/**/{path:[^\\.]*}")
    public String forwardSubpaths() {
        return "forward:/index.html";
    }
}
