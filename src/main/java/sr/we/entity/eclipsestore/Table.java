package sr.we.entity.eclipsestore;

import sr.we.entity.eclipsestore.tables.SuperDao;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

    Class<? extends SuperDao> value();
}
