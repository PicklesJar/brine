package picklesjar.brine.autodev;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( value = { ElementType.METHOD } )
public @interface DevelopmentMethod {
	
	public DevelopmentPhase phase() default DevelopmentPhase.MAIN;
	
	public int priority() default 0;
	
}
