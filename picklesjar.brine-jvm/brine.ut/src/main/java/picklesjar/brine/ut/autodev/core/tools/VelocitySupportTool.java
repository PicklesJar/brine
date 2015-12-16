package picklesjar.brine.ut.autodev.core.tools;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picklesjar.brine.ut.autodev.core.DevelopmentProcessingException;

public class VelocitySupportTool
	implements DevelopmentSupportTool {
	
	public static final String VELOCITY_CONFIGURATION_KEY_NAME = "velocity.init.properties.key";
	
	public static final String VELOCITY_CONFIGURATION_VALUE_NAME = "velocity.init.properties.value";
	
	/**
	 * 
	 */
	private static final Logger log = LoggerFactory.getLogger( VelocitySupportTool.class );
	
	/**
	 * 
	 */
	private ProcessingEnvironment processingEnv = null;
	
	/**
	 * 
	 */
	private StringBuilder strBuilder = null;
	
	/**
	 * 
	 * 
	 * 
	 */
	public VelocitySupportTool( ProcessingEnvironment processingEnv, PropertiesConfiguration config ) {
	
		if( ( processingEnv == null ) || ( config == null ) ) {
			throw new IllegalArgumentException();
		}
		
		this.processingEnv = processingEnv;
		this.strBuilder = new StringBuilder();
		
		initVelocity( config );
		
	}
	
	/**
	 * 
	 * 
	 * 
	 */
	public final boolean initVelocity( PropertiesConfiguration config ) {
	
		boolean result = false;
		
		if( config != null ) {
			
			Properties velocityProperties = null;
			String[] keys = config.getStringArray( VELOCITY_CONFIGURATION_KEY_NAME );
			String[] vals = config.getStringArray( VELOCITY_CONFIGURATION_VALUE_NAME );
			
			strBuilder.setLength( 0 );
			strBuilder.append( "Run velocity initialize.\n" );
			
			if( keys != null ) {
				
				velocityProperties = new Properties();
				
				strBuilder.append( "Velocity init properties is ...\n" );
				
				final int length = keys.length;
				for( int i = 0; i < length; ++i ) {
					
					velocityProperties.setProperty( keys[ i ], vals[ i ] );
					
					strBuilder.append( "\t" );
					strBuilder.append( keys[ i ] );
					strBuilder.append( " : " );
					strBuilder.append( vals[ i ] );
					strBuilder.append( "\n" );
					
				}
				
			}
			
			Velocity.init( velocityProperties );
			log.debug( strBuilder.toString() );
			
		}
		
		return result;
		
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param context
	 */
	public int createVelocitySourceFile( String className, VelocityContext context, Template template )
		throws DevelopmentProcessingException {
	
		if( ( context == null ) || ( template == null ) ) {
			throw new IllegalArgumentException();
		}
		
		int result = 0;
		
		if( StringUtils.isNotEmpty( className ) ) {
			
			Filer filer = processingEnv.getFiler();
			JavaFileObject file = null;
			Writer writer = null;
			
			strBuilder.setLength( 0 );
			strBuilder.append( className );
			strBuilder.append( " is creating ..." );
			log.info( strBuilder.toString() );
			
			Writer velocityWriter = null;
			try {
				
				file = filer.createSourceFile( className );
				if( file != null ) {
					
					velocityWriter = new StringWriter();
					template.merge( context, velocityWriter );
					
					writer = file.openWriter();
					template.merge( context, writer );
					writer.flush();
					
					++result;
					log.info( "Completed file creation." );
					
				}
				
			} catch( IOException exp ) {
				
				log.warn( "Failed file creation.", exp );
				throw new DevelopmentProcessingException( "Failed file creation.", exp );
				
			} finally {
				
				if( velocityWriter != null ) {
					try {
						velocityWriter.close();
					} catch( IOException exp ) {}
				}
				
				if( writer != null ) {
					try {
						writer.close();
					} catch( IOException exp ) {}
				}
				
			}
			
		}
		
		return result;
		
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param packageName
	 * @param className
	 * @return
	 * @throws IOException
	 */
	public boolean deleteVelocityFile( String packageName, String className )
		throws IOException {
	
		boolean result = false;
		
		FileObject sourceFile = processingEnv.getFiler()
			.getResource( StandardLocation.CLASS_OUTPUT, packageName, className );
		FileObject classFile = processingEnv.getFiler()
			.getResource( StandardLocation.SOURCE_OUTPUT, packageName, className );
		
		if( ( sourceFile != null ) && ( classFile != null ) ) {
			result = sourceFile.delete() && classFile.delete();
		}
		return result;
		
	}
	
}
