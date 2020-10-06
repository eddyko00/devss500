package com.sprhib.init;


import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;


@EnableScheduling
public class Initializer implements WebApplicationInitializer {

    public void onStartup(ServletContext servletContext)
            throws ServletException {
        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
        ctx.register(WebAppConfig.class);
        servletContext.addListener(new ContextLoaderListener(ctx));

        ctx.setServletContext(servletContext);
        
//        ThreadListenerExample tl = new ThreadListenerExample();
//        tl.schedulerInitialized();

    }

}
