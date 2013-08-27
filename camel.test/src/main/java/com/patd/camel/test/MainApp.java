package com.patd.camel.test;

import java.io.File;
import java.util.HashMap;

import org.apache.camel.CamelContext;
import org.apache.camel.Predicate;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.model.language.SimpleExpression;
import org.apache.camel.processor.idempotent.FileIdempotentRepository;

import org.apache.log4j.Logger;

/**
 * A Camel Application
 */
public class MainApp {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
    	// Setup the registry to allow us to plugin the File repository if we're not using spring
    	SimpleRegistry registry = new SimpleRegistry();
    	registry.put("repository", new FileIdempotentRepository(new File("src/registry"),new HashMap()));
    	
    	
    	Logger log = Logger.getLogger(Class.forName("com.patd.camel.test.MainApp"));
    	// create and start the context
    	CamelContext ctx = new DefaultCamelContext(registry);
    	
    	MyRouteBuilder routes = new MyRouteBuilder();
    	
    	//routes.theFrom = "file:src/data?noop=true&sendEmptyMessageWhenIdle=true&idempotentRepository=#repository";
    	routes.theFrom = "smb://boxee:boxee@192.168.10.4/media/mediaRAID/data?noop=true&sendEmptyMessageWhenIdle=true&idempotentRepository=#repository";
      	System.setProperty("jcifs.netbios.wins","192.168.1.220");
    	System.setProperty("jcifs.util.loglevel","3");
      	ctx.addRoutes(routes);
    	ctx.start();
    	
    	
    	Predicate pred = new SimpleExpression("${body} == null").createPredicate(ctx);
    	
    	
    	
    	// now let's wait for the jobs to complete.
    	NotifyBuilder notify = new NotifyBuilder(ctx);
    	NotifyBuilder successfulRoute = new NotifyBuilder(ctx);
    	
    	
    	successfulRoute.whenDone(1).wereSentTo("direct:processFurther").create();
    	// when 3 messages empty polls have passed we're done.
    	notify.whenDone(3).wereSentTo("direct:test").create();
    	
    	while(!notify.matches())
    	{
    	//	System.out.println("sleeping");
    		Thread.sleep(100);
    		
    	}
    	
    	if(successfulRoute.matches())
    		log.info("processed messages");
    		else
    			log.info("nothing was processed");
  		ctx.stop();
    	
    }

}

