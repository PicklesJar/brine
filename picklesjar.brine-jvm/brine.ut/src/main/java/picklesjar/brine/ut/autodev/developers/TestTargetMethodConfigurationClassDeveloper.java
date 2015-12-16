package picklesjar.brine.ut.autodev.developers;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

import picklesjar.brine.ut.autodev.core.annotation.Developer;
import picklesjar.brine.ut.autodev.core.annotation.InjectDevTool;
import picklesjar.brine.ut.autodev.core.annotation.InjectEnvironment;
import picklesjar.brine.ut.autodev.core.tools.VelocitySupportTool;

@Developer
public class TestTargetMethodConfigurationClassDeveloper {
	
	@InjectEnvironment
	private ProcessingEnvironment processingEnv;
	
	@InjectEnvironment
	private RoundEnvironment roundEnv;
	
	@InjectDevTool
	private VelocitySupportTool velocityTool;
	
}
