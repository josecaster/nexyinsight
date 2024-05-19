package sr.we.entity.eclipsestore.tables;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Objects;

public class SuperDao implements Serializable {

    @Id
    String uuId;

    protected SuperDao() {
        super();
    }

    public String getUuId() {
        return uuId;
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SuperDao superDao)) return false;
        return Objects.equals(uuId, superDao.uuId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuId);
    }
}
