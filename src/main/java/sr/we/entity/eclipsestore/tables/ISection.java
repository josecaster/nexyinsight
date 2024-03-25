package sr.we.entity.eclipsestore.tables;

import java.time.LocalDateTime;

public interface ISection /*extends Entity*/ {

    Long getBusinessId();

    String getId();

    String getName();

    String getAddress();

    String getCity();

    String getRegion();

    String getPostalCode();

    String getCountryCode();

    String getPhoneNumber();

    String getDescription();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAtt();

    LocalDateTime getDeletedAt();

}
