package picklesjar.brine.autodev.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import picklesjar.brine.autodev.DevelopmentPhase;

@Retention( RetentionPolicy.RUNTIME )
@Target( value = { ElementType.METHOD } )
@Inherited
public @interface DevelopmentMethod {
	
	public DevelopmentPhase phase() default DevelopmentPhase.MAIN;
	
	public int priority() default 0;
	
}
