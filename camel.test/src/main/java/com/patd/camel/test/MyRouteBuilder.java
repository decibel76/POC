package com.patd.camel.test;

import org.apache.camel.builder.RouteBuilder;

/**
 * A Camel Java DSL Router
 */
public class MyRouteBuilder extends RouteBuilder {
	
	public String theFrom = null;

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {

        // here is a sample which processes the input files
        // (leaving them in place - see the 'noop' flag)
        // then performs content based routing on the message using XPath
        from(theFrom)
        	.routeId("XML Route")
            .choice()
            	.when(simple("${body} == null"))
            	.to("direct:test")
            	.otherwise().log("Not null message sending to next route")
            	.to("direct:processFurther");
        
        from("direct:processFurther").routeId("FutherProcess")
        		.choice()
                .when(xpath("/person/city = 'London'"))
                    .log("UK message")
                    .to("file:target/messages/uk")
                .otherwise()
                    .log("Other message")
                    .to("file:target/messages/others");
        
        from("direct:test")
        .routeId("EmptyRoute")
        .log("empty")
        .to("log:com.patd.camel.test.MyRouteBuilder?showAll=true&showProperties=true");
    }

}
