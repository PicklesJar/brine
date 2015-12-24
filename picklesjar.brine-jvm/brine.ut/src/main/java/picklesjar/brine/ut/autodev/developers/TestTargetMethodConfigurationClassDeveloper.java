package picklesjar.brine.ut.autodev.developers;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

import picklesjar.brine.autodev.annotation.InjectDevTool;
import picklesjar.brine.autodev.annotation.InjectEnvironment;
import picklesjar.brine.autodev.tools.VelocitySupportTool;

public class TestTargetMethodConfigurationClassDeveloper {
	
	@InjectEnvironment
	private ProcessingEnvironment processingEnv;
	
	@InjectEnvironment
	private RoundEnvironment roundEnv;
	
	@InjectDevTool
	private VelocitySupportTool velocityTool;
	
}
