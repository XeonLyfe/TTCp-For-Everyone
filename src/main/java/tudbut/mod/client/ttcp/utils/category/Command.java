package tudbut.mod.client.ttcp.utils.category;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import tudbut.mod.client.ttcp.utils.category.Category;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
@Category
public @interface Command {
}
