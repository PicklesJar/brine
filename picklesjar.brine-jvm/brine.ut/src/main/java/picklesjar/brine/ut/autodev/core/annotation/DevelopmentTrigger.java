package picklesjar.brine.ut.autodev.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( value = { ElementType.ANNOTATION_TYPE } )
@Inherited
public @interface DevelopmentTrigger {
	
	public Class< ? > value();
	
}
