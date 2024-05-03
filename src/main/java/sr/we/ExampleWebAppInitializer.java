package sr.we;

import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.spring.SpringServlet;
import com.vaadin.flow.spring.annotation.EnableVaadin;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

//@EnableVaadin
public abstract class ExampleWebAppInitializer /*implements WebApplicationInitializer*/ {

//    @Bean
//    @ConditionalOnClass(VaadinServlet.class)
//    public ServletRegistrationBean<VaadinServlet> vaadinServletRegistration() {
//        ServletRegistrationBean<VaadinServlet> registration = new ServletRegistrationBean<>(new VaadinServlet(), "/app/*");
//        registration.addInitParameter("disable-xsrf-protection", "true");
//        return registration;
//    }

//    @Override
//    public void onStartup(ServletContext servletContext) throws ServletException {
//        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
//        registerConfiguration(context);
//        servletContext.addListener(new ContextLoaderListener(context));
//
//        ServletRegistration.Dynamic registration = servletContext.addServlet("webhook", new SpringServlet(context, true));
//        registration.setLoadOnStartup(1);
//        registration.addMapping("/webhook/*");
//    }
//
//    private void registerConfiguration(AnnotationConfigWebApplicationContext context) {
//        // register your configuration classes here
//    }

}
