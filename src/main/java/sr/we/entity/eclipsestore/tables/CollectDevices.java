package sr.we.entity.eclipsestore.tables;

import java.util.List;

public class CollectDevices {
    private List<Device> pos_devices;
    private String cursor;
    private List<Error> errors;

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public List<Device> getPos_devices() {
        return pos_devices;
    }

    public void setPos_devices(List<Device> pos_devices) {
        this.pos_devices = pos_devices;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
}
