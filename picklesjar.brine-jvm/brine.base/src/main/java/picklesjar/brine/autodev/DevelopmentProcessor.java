package picklesjar.brine.autodev;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picklesjar.brine.autodev.annotation.Developer;
import picklesjar.brine.autodev.annotation.DevelopmentMethod;
import picklesjar.brine.autodev.annotation.DevelopmentTrigger;
import picklesjar.brine.autodev.annotation.InjectDevTool;
import picklesjar.brine.autodev.annotation.InjectEnvironment;
import picklesjar.brine.autodev.tools.VelocitySupportTool;

@SupportedSourceVersion( SourceVersion.RELEASE_8 )
@SupportedAnnotationTypes( { "*" } )
public class DevelopmentProcessor
	extends AbstractProcessor {
	
	/**
	 * 
	 */
	protected static final String CONFIGURATION_FILE = "META-INF/picklesjar.brine.autodev/config.properties";
	
	/**
	 * 
	 */
	protected static final String DEVELOPMENT_TRIGGER_CLASS_NAME = DevelopmentTrigger.class.getName();
	
	/**
	 * 
	 */
	private Logger log = LoggerFactory.getLogger( DevelopmentProcessor.class );
	
	/**
	 * 
	 */
	private PropertiesConfiguration config = null;
	
	/**
	 * 
	 */
	private static StringBuilder strBuilder = null;
	
	static {
		strBuilder = new StringBuilder();
	}
	
	/**
	 * 
	 * 
	 * 
	 * @throws ConfigurationException
	 */
	public DevelopmentProcessor() {
	
		try {
			
			ClassLoader loader = this.getClass().getClassLoader();
			config = new PropertiesConfiguration( loader.getResource( CONFIGURATION_FILE ) );
			
		} catch( ConfigurationException exp ) {
			
			log.error( "Failed load configure file.", exp );
			throw new IllegalStateException( exp );
		}
		
	}
	
	/**
	 * 
	 * 
	 * @param annotations
	 * @param roundEnv
	 */
	@Override
	public final boolean process(
		@Nonnull Set< ? extends TypeElement > annotations, @Nonnull RoundEnvironment roundEnv ) {
	
		boolean result = false;
		
		if( annotations.size() > 0 ) {
			
			try {
				result = execute( annotations, roundEnv );
			} catch( DevelopmentProcessingException exp ) {
				log.error( "Failed development execution.", exp );
				result = false;
			}
			
		}
		
		return result;
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param roundEnv
	 * @throws DevelopmentProcessingException
	 */
	protected boolean execute(
		@Nonnull Set< ? extends TypeElement > annotations, @Nonnull RoundEnvironment roundEnv )
		throws DevelopmentProcessingException {
	
		boolean[] result = { false };
		
		Map< Integer, List< Object > > developerMap = exchangeToPrioriticalMap( createDeveloperSet( annotations, roundEnv ) );
		if( developerMap != null ) {
			
			developerMap
				.forEach( ( key, developers ) -> {
					if( ( developers != null ) && !( developers.isEmpty() ) ) {
						
						developers
							.stream()
							.filter(
								( developer ) -> {
									return developer != null;
								} )
							.forEach(
								( developer ) -> {
									strBuilder.setLength( 0 );
									strBuilder.append( "\n\n\n" );
									strBuilder.append( "_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/\n" );
									strBuilder.append( developer );
									strBuilder.append( "\n    will be starting development.(priority=" );
									strBuilder.append( key );
									strBuilder.append( ")\n" );
									strBuilder.append( "_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/" );
									log.debug( strBuilder.toString() );
									
									try {
										if( !result[ 0 ] ) {
											result[ 0 ] = execute( roundEnv, developer );
										} else {
											execute( roundEnv, developer );
										}
									} catch( DevelopmentProcessingException exp ) {
										throw new IllegalStateException( exp );
									}
									
									strBuilder.setLength( 0 );
									strBuilder.append( "  Finished developer's execution, Now result stat is " );
									strBuilder.append( result[ 0 ] );
									strBuilder.append( ".\n\n\n" );
									log.debug( strBuilder.toString() );
								} );
						
					}
				} );
			
		}
		
		return result[ 0 ];
	}
	
	/**
	 * Create developers Set.
	 * 
	 * @param annotations
	 * @param roundEnv
	 * @return
	 */
	protected Set< Object > createDeveloperSet(
		@Nonnull Set< ? extends TypeElement > annotations, @Nonnull RoundEnvironment roundEnv ) {
	
		Set< Object > result = new HashSet<>();
		
		// 1. load @Developer annotated Objects.
		roundEnv.getElementsAnnotatedWith( Developer.class ).stream()
			.filter(
				( element ) -> {
					return element != null;
				} )
			.forEach(
				( element ) -> {
					result.add( exchangeElementToDeveloper( element ) );
				} );
		
		// 2. load DEVELOPMENT_TRIGGER_CLASS_NAME refer Developer Objects.
		List< List< ? extends AnnotationMirror > > mirrorsList = new LinkedList<>();
		annotations
			.stream()
			.filter(
				( annotation ) -> {
					return annotation != null;
				} )
			.forEach(
				( annotation ) -> {
					mirrorsList.add( annotation.getAnnotationMirrors() );
				} );
		mirrorsList.stream()
			.filter(
				( mirrors ) -> {
					return mirrors != null;
				} )
			.forEach(
				( mirrors ) -> {
					
					mirrors.stream()
						.filter(
							( AnnotationMirror mirror ) -> {
								return mirror != null
									&& mirror.getElementValues() != null
									&& !( mirror.getElementValues().isEmpty() )
									&& ( DEVELOPMENT_TRIGGER_CLASS_NAME.equals(
										mirror.getAnnotationType().toString() ) );
							} )
						.forEach(
							( mirror ) -> {
								
								Class< ? > clazz = null;
								try {
									clazz = Class.forName(
										( ( AnnotationValue )CollectionUtils.get( mirror.getElementValues().values(), 0 )
										).getValue().toString() );
								} catch( ClassNotFoundException exp ) {
									log.warn( "Failed to load Class of annotation value.", exp );
								}
								if( clazz != null ) {
									try {
										result.add( clazz.getConstructor().newInstance() );
									} catch( NoSuchMethodException | InstantiationException
										| IllegalAccessException | InvocationTargetException exp ) {
										log.warn( "Failed to create developer instance.", exp );
									}
								}
								
							} );
				} );
		
		return result;
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param roundEnv
	 * @param developer
	 * @return
	 * @throws DevelopmentProcessingException
	 */
	private final boolean execute( RoundEnvironment roundEnv, Object developer )
		throws DevelopmentProcessingException {
	
		initDeveloper( developer, roundEnv );
		
		// Create sorted method list by PRIORITY in Developer
		Map< Integer, List< Method > > initializeMethodList = new TreeMap< Integer, List< Method > >();
		Map< Integer, List< Method > > prepareMethodList = new TreeMap< Integer, List< Method > >();
		Map< Integer, List< Method > > mainMethodList = new TreeMap< Integer, List< Method > >();
		Map< Integer, List< Method > > finishMethodList = new TreeMap< Integer, List< Method > >();
		Map< Integer, List< Method > > finalizeMethodList = new TreeMap< Integer, List< Method > >();
		Arrays.stream( developer.getClass().getDeclaredMethods() )
			.filter(
				( method ) -> {
					return method != null
						&& method.getDeclaredAnnotation( DevelopmentMethod.class ) != null;
				} )
			.forEach(
				( method ) -> {
					DevelopmentMethod developmentExecute = method.getDeclaredAnnotation( DevelopmentMethod.class );
					
					strBuilder.setLength( 0 );
					strBuilder.append( "\t" );
					strBuilder.append( method.getName() );
					strBuilder.append( " method is configurating.( " );
					strBuilder.append( developmentExecute );
					strBuilder.append( " )" );
					log.debug( strBuilder.toString() );
					
					switch( developmentExecute.phase() ) {
						case MAIN :
							affectToSamePhaseMethodMap( mainMethodList, method );
							break;
						case INITIALIZE :
							affectToSamePhaseMethodMap( initializeMethodList, method );
							break;
						case PREPARE :
							affectToSamePhaseMethodMap( prepareMethodList, method );
							break;
						case FINISH :
							affectToSamePhaseMethodMap( finishMethodList, method );
							break;
						case FINALIZE :
							affectToSamePhaseMethodMap( finalizeMethodList, method );
							break;
						default :
							break;
					}
				} );
		
		// Execute methods in Developer
		boolean[] result = { !( mainMethodList.isEmpty() ) };
		
		if( result[ 0 ] ) {
			
			// Initialize
			result[ 0 ] = initializeMethodList.isEmpty()
				|| executionMethods( developer, initializeMethodList );
			
			if( result[ 0 ] ) {
				
				mainMethodList.forEach(
					( key, value ) -> {
						if( ( key != null ) && ( value != null ) ) {
							
							value.stream()
								.filter(
									( method ) -> {
										return method != null;
									} )
								.forEach(
									( method ) -> {
										result[ 0 ] = prepareMethodList.isEmpty()
											|| executionMethods( developer, prepareMethodList );
										
										if( !result[ 0 ] ) {
											result[ 0 ] = executionMethod( developer, method );
											
											if( !result[ 0 ] ) {
												result[ 0 ] = finishMethodList.isEmpty()
													|| executionMethods( developer, finishMethodList );
											}
										}
									} );
							
						}
					} );
				
				// Finalize
				if( result[ 0 ] ) {
					result[ 0 ] = finalizeMethodList.isEmpty()
						|| executionMethods( developer, finalizeMethodList );
				}
			}
			
		}
		
		return result[ 0 ];
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param element
	 * @return
	 */
	protected final Class< ? > exchangeElementToClass( Element element ) {
	
		Class< ? > result = null;
		
		if( element != null ) {
			
			strBuilder.setLength( 0 );
			strBuilder.append( super.processingEnv.getElementUtils().getPackageOf( element ) );
			strBuilder.append( "." );
			strBuilder.append( element.getSimpleName() );
			
			try {
				result = Class.forName( strBuilder.toString() );
			} catch( SecurityException | ClassNotFoundException exp ) {
				log.warn( "Failed to exchange element to java.lang.Class .", exp );
			}
			
		}
		
		return result;
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param element
	 * @return
	 */
	protected final Object exchangeElementToDeveloper( Element element ) {
	
		Object result = null;
		
		if( element != null ) {
			
			Class< ? > developer = exchangeElementToClass( element );
			if( developer != null ) {
				
				try {
					result = developer.getConstructor().newInstance();
				} catch( InstantiationException | IllegalAccessException
					| InvocationTargetException | NoSuchMethodException exp ) {
					log.warn( "Failed to create instanse of " + developer.getName(), exp );
					result = null;
				}
				
			}
			
		}
		
		return result;
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param developer
	 * @param roundEnv
	 */
	protected final void initDeveloper( Object developer, RoundEnvironment roundEnv ) {
	
		if( ( developer == null ) || ( roundEnv == null ) ) {
			throw new IllegalArgumentException();
		}
		
		Field[] fields = developer.getClass().getDeclaredFields();
		Arrays.stream( fields )
			.filter(
				( field ) -> {
					field.setAccessible( true );
					return field.getDeclaredAnnotation( InjectEnvironment.class ) != null
						|| field.getDeclaredAnnotation( InjectDevTool.class ) != null;
				} )
			.forEach(
				( field ) -> {
					@Nonnull
					String fieldTypeName = field.getType().getName();
					
					if( field.getDeclaredAnnotation( InjectEnvironment.class ) != null ) {
						
						if( fieldTypeName.equals( ProcessingEnvironment.class.getName() ) ) {
							
							try {
								field.set( developer, super.processingEnv );
							} catch( IllegalArgumentException | IllegalAccessException exp ) {
								throw new IllegalStateException( exp );
							}
							
						} else if( fieldTypeName.equals( RoundEnvironment.class.getName() ) ) {
							
							try {
								field.set( developer, roundEnv );
							} catch( IllegalArgumentException | IllegalAccessException exp ) {
								throw new IllegalStateException( exp );
							}
							
						}
						
					} else {
						
						if( fieldTypeName.equals( VelocitySupportTool.class.getName() ) ) {
							
							try {
								field.set( developer, new VelocitySupportTool( processingEnv, config ) );
							} catch( IllegalArgumentException | IllegalAccessException exp ) {
								throw new IllegalStateException( exp );
							}
							
						}
						
					}
				} );
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param map
	 * @param method
	 * @return
	 */
	protected final boolean affectToSamePhaseMethodMap( Map< Integer, List< Method > > map, Method method ) {
	
		boolean result = false;
		
		if( ( map != null ) && ( method != null ) ) {
			
			DevelopmentMethod developmentExecute = method.getDeclaredAnnotation( DevelopmentMethod.class );
			if( developmentExecute != null ) {
				
				int priority = developmentExecute.priority();
				if( map.containsKey( priority ) ) {
					
					List< Method > methodList = map.get( priority );
					if( methodList == null ) {
						methodList = new LinkedList< Method >();
						map.put( priority, methodList );
					}
					
					methodList.add( method );
					result = true;
					
				} else {
					
					List< Method > methodList = new LinkedList< Method >();
					methodList.add( method );
					map.put( priority, methodList );
				}
				
			}
			
		}
		
		return result;
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param developers
	 * @return
	 */
	protected final Map< Integer, List< Object > > exchangeToPrioriticalMap( Set< Object > developers ) {
	
		final Map< Integer, List< Object > > result = new TreeMap< Integer, List< Object > >();
		
		if( developers != null ) {
			
			developers.stream()
				.filter(
					( developer ) -> {
						return developer != null;
					} )
				.forEach(
					( developer ) -> {
						int priority = 0;
						Developer developerAnnotation = developer.getClass().getDeclaredAnnotation( Developer.class );
						if( developerAnnotation != null ) {
							priority = developerAnnotation.priority();
						}
						
						if( result.containsKey( priority ) ) {
							result.get( priority ).add( developer );
						} else {
							List< Object > developerList = new ArrayList< Object >();
							developerList.add( developer );
							result.put( priority, developerList );
						}
					} );
			
		}
		
		if( result.isEmpty() ) {
			return null;
		} else {
			return result;
		}
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param developer
	 * @param map
	 * @return
	 */
	protected final boolean executionMethods( Object developer, Map< Integer, List< Method > > map ) {
	
		boolean[] result = { false };
		
		if( ( map != null ) && ( map.size() > 0 ) ) {
			
			map.forEach(
				( key, value ) -> {
					if( ( key != null ) && ( value != null ) ) {
						
						value.stream()
							.filter(
								( method ) -> {
									return method != null;
								} )
							.forEach(
								( method ) -> {
									if( result[ 0 ] ) {
										result[ 0 ] = executionMethod( developer, method );
									}
								} );
						
					}
				} );
			
		}
		
		return result[ 0 ];
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param developer
	 * @param method
	 * @return
	 */
	protected final boolean executionMethod( Object developer, Method method ) {
	
		boolean result = false;
		
		if( ( developer != null ) && ( method != null ) ) {
			
			strBuilder.setLength( 0 );
			strBuilder.append( "  " );
			strBuilder.append( method.getName() );
			strBuilder.append( " method will execute ..." );
			log.debug( strBuilder.toString() );
			
			try {
				// NOTE :: No care static case, cause be executable method if static method can execute with null at first
				// argument on invoke.
				method.invoke( developer, new Object[ method.getParameterTypes().length ] );
				result = true;
				
				strBuilder.setLength( 0 );
				strBuilder.append( "      Succeed method execution." );
				log.debug( strBuilder.toString() );
				
			} catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException exp ) {
				log.warn( "      Failed to execute named method " + method.getName(), exp );
				result = false;
			}
			
		}
		
		return result;
	}
	
}
