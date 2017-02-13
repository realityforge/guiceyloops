package javax.jms;

import com.google.inject.BindingAnnotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A patched version of JMSConnectionFactory that adds @BindingAnnotation annotation for testing.
 */
@Retention( RUNTIME )
@Target( { METHOD, FIELD, PARAMETER, TYPE } )
@BindingAnnotation
public @interface JMSConnectionFactory
{
  String value();
}
